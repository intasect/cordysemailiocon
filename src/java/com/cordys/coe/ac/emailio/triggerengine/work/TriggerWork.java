package com.cordys.coe.ac.emailio.triggerengine.work;

import com.cordys.coe.ac.emailio.config.IEmailBox;
import com.cordys.coe.ac.emailio.config.action.EEventType;
import com.cordys.coe.ac.emailio.config.action.IAction;
import com.cordys.coe.ac.emailio.config.message.IMessage;
import com.cordys.coe.ac.emailio.config.rule.IRuleContext;
import com.cordys.coe.ac.emailio.config.trigger.ITrigger;
import com.cordys.coe.ac.emailio.exception.StorageProviderException;
import com.cordys.coe.ac.emailio.localization.LogMessages;
import com.cordys.coe.ac.emailio.monitor.IJMXEmailBoxPoller;
import com.cordys.coe.ac.emailio.monitor.MessageExecutionHelper;
import com.cordys.coe.ac.emailio.monitor.RuleContext;
import com.cordys.coe.ac.emailio.monitor.RuleContextContainer;
import com.cordys.coe.ac.emailio.storage.IEmailStorageProvider;
import com.cordys.coe.ac.emailio.util.MailMessageUtil;
import com.cordys.coe.util.general.ExceptionUtil;
import com.cordys.coe.util.general.Util;
import com.cordys.coe.util.soap.SOAPWrapper;

import com.eibus.connector.nom.Connector;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Node;

import java.util.ArrayList;

import javax.mail.Message;

/**
 * This class holds the definition of the work that needs to be executed. It will contain the
 * configuration and a number of RuleContext objects. If the message configuration contains a
 * repeatingxpath definition all contexts will be put into a single SOAP message. If this definition
 * is not set each rule context will result into a new SOAP message.
 *
 * @author  pgussow
 */
class TriggerWork
    implements ITriggerWork
{
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(TriggerWork.class);
    /**
     * Holds the connector to use for sending SOAP messages.
     */
    private Connector m_cConnector;
    /**
     * Holds the originating email box.
     */
    private IEmailBox m_ebEmailBox;
    /**
     * Holds teh message counter to report success or failure to.
     */
    private IJMXEmailBoxPoller m_emcCounter;
    /**
     * Holds the storage provider for this context.
     */
    private IEmailStorageProvider m_espStorage;
    /**
     * Holds the message definition that has to be sent.
     */
    private IMessage m_mMessage;
    /**
     * Holds the list of context that have to be sent.
     */
    private RuleContextContainer m_rccContext;
    /**
     * Holds the trigger that was matched.
     */
    private ITrigger m_tTrigger;

    /**
     * This method executes the actual work.
     *
     * @see  com.eibus.util.threadpool.Work#execute()
     */
    public void execute()
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Executing work. The current context:\n" + m_rccContext.toString() +
                      "\n=================\nMessage template:\n=================\n" +
                      m_mMessage.toString());
        }

        SOAPWrapper swSoap = createSoapWrapper();
        int iMethod = 0;

        try
        {
            // Make sure the emails are put to status 'in progress'
            try
            {
                m_espStorage.setContainerStatusInProgress(m_rccContext);
            }
            catch (StorageProviderException spe)
            {
                LOG.error(spe, LogMessages.ERROR_UPDATING_THE_STATUS_IN_THE_STORAGE_LAYER);
            }

            StringBuilder sbExceptionDetails = new StringBuilder();

            for (RuleContext rcContext : m_rccContext)
            {
                // The actions only work on a mail-to-mail bases. This means that SUCCES or ERROR
                // events are sent for every send-and-wait.
                boolean bOK = true;

                try
                {
                    // Create the base method.
                    if (iMethod == 0)
                    {
                        iMethod = MessageExecutionHelper.createMethod(swSoap, m_mMessage);
                    }
                    else
                    {
                        iMethod = swSoap.addMethod(iMethod, m_mMessage.getMethod(),
                                                   m_mMessage.getNamespace());
                    }

                    // Now fill in the parameters.
                    MessageExecutionHelper.fillParameters(iMethod, m_mMessage, rcContext);

                    if ((m_tTrigger.getCombineEmails() == false) ||
                            !m_mMessage.supportsMultipleEmails())
                    {
                        if (LOG.isDebugEnabled())
                        {
                            LOG.debug("Sending request:\n" +
                                      Node.writeToString(Node.getRoot(iMethod), false));
                        }

                        if (m_mMessage.getSynchronous())
                        {
                            int iResponse = swSoap.sendAndWait(iMethod);

                            if (LOG.isDebugEnabled())
                            {
                                LOG.debug("Response:\n" +
                                          Node.writeToString(Node.getRoot(iResponse), false));
                            }
                        }
                        else
                        {
                            swSoap.sendAndForget(iMethod);
                        }
                        m_emcCounter.incNrOfMessagesSuccess(1);
                    }
                }
                catch (Exception e)
                {
                    // Error handling the message.
                    LOG.error(e, LogMessages.ERR_TW_ERROR_SENDING_TRIGGER, rcContext);

                    if (!m_mMessage.supportsMultipleEmails())
                    {
                        m_emcCounter.incNrOfMessagesFailed(1);
                    }

                    // Build up the JMX context.
                    StringBuffer sbTemp = new StringBuffer(2048);
                    sbTemp.append("SOAP Envelope:\n");
                    sbTemp.append(Node.writeToString(Node.getRoot(iMethod), false));

                    sbTemp.append("\nRule context:\n");
                    sbTemp.append(rcContext.toString());
                    sbTemp.append("\n");

                    m_emcCounter.notifyProcessingError(e, sbTemp.toString());

                    // Build up the error report
                    bOK = false;
                    buildExceptionReport(rcContext, e);

                    // Store the exception so that it can be written to the storage to provide
                    // error details.
                    sbExceptionDetails.append("Exception sending message to receiver\n");
                    sbExceptionDetails.append(sbTemp.toString()).append("\nStacktrace:\n");
                    sbExceptionDetails.append(Util.getStackTrace(e)).append("\n\n");
                }
                finally
                {
                    if ((m_tTrigger.getCombineEmails() == false) ||
                            !m_mMessage.supportsMultipleEmails())
                    {
                        // Clean up the current.
                        swSoap.freeXMLNodes();

                        // Create a new one
                        swSoap = createSoapWrapper();

                        iMethod = 0;
                    }
                }

                // Handle the events.
                boolean bOneActionOK = false;
                boolean bActionError = false;

                try
                {
                    ArrayList<IAction> alActions = null;

                    if (bOK == true)
                    {
                        alActions = m_tTrigger.getActions(EEventType.SUCCESS);
                    }
                    else
                    {
                        alActions = m_tTrigger.getActions(EEventType.ERROR);

                        // If there are NO error actions defined we won't delete the files.
                        if (alActions.size() == 0)
                        {
                            if (LOG.isWarningEnabled())
                            {
                                LOG.warn(null,
                                         LogMessages.WRN_FOR_TRIGGER_THERE_ARE_NO_ERROR_ACTIONS_DEFINED,
                                         m_tTrigger.getName());
                            }
                        }
                    }

                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Executing " + alActions.size() + " actions.");
                    }

                    for (IAction aAction : alActions)
                    {
                        try
                        {
                            aAction.execute(rcContext,
                                            (Message) rcContext.getValue(IRuleContext.SYS_MESSAGE_OBJECT),
                                            createSoapWrapper());
                            bOneActionOK = true;
                        }
                        catch (Exception e)
                        {
                            bActionError = true;
                            LOG.error(e, LogMessages.ERROR_EXECUTING_ACTION_0_FOR_CONTEXTN1,
                                      aAction.getID(), rcContext.toString());
                            sbExceptionDetails.append("Exception sending action\n")
                                              .append(aAction.toString()).append("\n");
                            sbExceptionDetails.append(Util.getStackTrace(e)).append("\n\n");
                        }
                    }
                }
                catch (Exception e)
                {
                    LOG.error(e, LogMessages.IE_ERROR_HANDLING_EVENT_FOR_MAIL,
                              MailMessageUtil.rawMessage((Message) rcContext.getValue(IRuleContext.SYS_MESSAGE_OBJECT)));
                    // Store the exception so that it can be written to the storage to provide
                    // error details.
                    sbExceptionDetails.append("Exception sending action to receiver\n");
                    sbExceptionDetails.append(Util.getStackTrace(e)).append("\n\n");
                }
                finally
                {
                    try
                    {
                        // We need to set the status to completed if everything went ok. If any
                        // error occurred during sending of the trigger message the status will be
                        // set to MESAGE_ERROR. If there was an error during sending of the actions
                        // the status will be set to ACTION_ERROR.
                        if (bOK && !bActionError)
                        {
                            m_espStorage.setContainerStatusCompleted(m_rccContext);
                        }
                        else if (bOK && bActionError)
                        {
                            m_espStorage.setContainerStatusActionError(m_rccContext,
                                                                       sbExceptionDetails
                                                                       .toString());
                        }
                        else if (!bOK)
                        {
                            m_espStorage.setContainerStatusError(m_rccContext,
                                                                 sbExceptionDetails.toString());
                        }
                        else
                        {
                            m_espStorage.setContainerStatusError(m_rccContext,
                                                                 "Could not determine proper state. bOK: " +
                                                                 bOK + ", bOneActionOK: " +
                                                                 bOneActionOK + ", bActionError: " +
                                                                 bActionError + "\n" +
                                                                 sbExceptionDetails.toString());
                        }
                    }
                    catch (StorageProviderException spe)
                    {
                        LOG.error(spe, LogMessages.ERROR_UPDATING_THE_STATUS_IN_THE_STORAGE_LAYER);
                    }
                }
            }

            // Now all rule context have been processed. So we need to send the message
            if ((m_tTrigger.getCombineEmails() == true) && m_mMessage.supportsMultipleEmails())
            {
                try
                {
                    if (m_mMessage.getSynchronous())
                    {
                        swSoap.sendAndWait(iMethod);
                    }
                    else
                    {
                        swSoap.sendAndForget(iMethod);
                    }

                    m_emcCounter.incNrOfMessagesSuccess(m_rccContext.size());
                    m_espStorage.setContainerStatusCompleted(m_rccContext);
                }
                catch (Exception e)
                {
                    // Error handling the message.
                    LOG.error(e, LogMessages.ERR_TW_ERROR_SENDING_TRIGGER,
                              "multiple context: " + m_rccContext.size());

                    // Build up the JMX context.
                    StringBuffer sbTemp = new StringBuffer(2048);
                    sbTemp.append("SOAP Envelope:\n");
                    sbTemp.append(Node.writeToString(Node.getRoot(iMethod), false));

                    for (RuleContext rcContext : m_rccContext)
                    {
                        sbTemp.append("\nRule context:\n");
                        sbTemp.append(rcContext.toString());
                        sbTemp.append("\n");
                    }

                    if (!m_mMessage.supportsMultipleEmails())
                    {
                        m_emcCounter.incNrOfMessagesFailed(m_rccContext.size());
                    }

                    m_emcCounter.notifyProcessingError(e, sbTemp.toString());

                    // Store the exception so that it can be written to the storage to provide
                    // error details.
                    sbExceptionDetails.append("Exception sending message to receiver\n");
                    sbExceptionDetails.append(sbTemp.toString()).append("\nStacktrace:\n");
                    sbExceptionDetails.append(Util.getStackTrace(e)).append("\n\n");

                    try
                    {
                        m_espStorage.setContainerStatusError(m_rccContext,
                                                             sbExceptionDetails.toString());
                    }
                    catch (StorageProviderException spe)
                    {
                        LOG.error(spe, LogMessages.ERROR_UPDATING_THE_STATUS_IN_THE_STORAGE_LAYER);
                    }
                }
                finally
                {
                    swSoap.freeXMLNodes();
                }
            }
        }
        finally
        {
            // Clean up all the context that are in this unit of work to prevent memory leaks.
            for (RuleContext rcContext : m_rccContext)
            {
                rcContext.clear();
            }
        }
    }

    /**
     * This method gets the corresponding email box.
     *
     * @return  The corresponding email box.
     */
    public IEmailBox getEmailBox()
    {
        return m_ebEmailBox;
    }

    /**
     * This method initializes the work object.
     *
     * @param  rccContext  All the message context that have to be sent.
     * @param  mMessage    The message template.
     * @param  tTrigger    The trigger that was matched.
     * @param  cConnector  The Cordys connector to use.
     * @param  emcCounter  The email message counter to report success or failure to.
     * @param  ebEmailBox  The email box this work originated from.
     *
     * @see    com.cordys.coe.ac.emailio.triggerengine.work.ITriggerWork#initialize(com.cordys.coe.ac.emailio.monitor.RuleContextContainer,
     *         com.cordys.coe.ac.emailio.config.message.IMessage,
     *         com.cordys.coe.ac.emailio.config.trigger.ITrigger, com.eibus.connector.nom.Connector,
     *         com.cordys.coe.ac.emailio.monitor.IJMXEmailBoxPoller,
     *         com.cordys.coe.ac.emailio.config.IEmailBox)
     */
    @Override public void initialize(RuleContextContainer rccContext, IMessage mMessage,
                                     ITrigger tTrigger, Connector cConnector,
                                     IJMXEmailBoxPoller emcCounter, IEmailBox ebEmailBox)
    {
        m_rccContext = rccContext;
        m_mMessage = mMessage;
        m_cConnector = cConnector;
        m_emcCounter = emcCounter;
        m_tTrigger = tTrigger;
        m_ebEmailBox = ebEmailBox;
        m_espStorage = ebEmailBox.getStorageProvider();
    }

    /**
     * This method builds the exception report and stores it in the rule context.
     *
     * @param   rcContext   The context of the rule.
     * @param   eException  The exception that occurred.
     *
     * @return  The string exception report.
     */
    private String buildExceptionReport(RuleContext rcContext, Exception eException)
    {
        String sReturn = null;

        StringBuilder sbTemp = new StringBuilder(2048);
        sbTemp.append(ExceptionUtil.getStackTrace(eException));
        sReturn = sbTemp.toString();

        rcContext.putValue(IRuleContext.SYS_EXCEPTION_REPORT, sReturn);

        return sReturn;
    }

    /**
     * This method creates a SOAP wrapper to use.
     *
     * @return  A fully initialized soap wrapper.
     */
    private SOAPWrapper createSoapWrapper()
    {
        SOAPWrapper swReturn = new SOAPWrapper(m_cConnector);

        if ((m_mMessage.getOrganization() != null) && (m_mMessage.getOrganization().length() > 0))
        {
            swReturn.setOrganization(m_mMessage.getOrganization());
        }

        if ((m_mMessage.getUserDN() != null) && (m_mMessage.getUserDN().length() > 0))
        {
            swReturn.setUser(m_mMessage.getUserDN());
        }
        swReturn.setTimeOut(m_mMessage.getTimeout());

        return swReturn;
    }
}
