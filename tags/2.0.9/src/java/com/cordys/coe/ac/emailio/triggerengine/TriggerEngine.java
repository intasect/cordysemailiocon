package com.cordys.coe.ac.emailio.triggerengine;

import com.cordys.coe.ac.emailio.config.IEmailBox;
import com.cordys.coe.ac.emailio.config.action.EEventType;
import com.cordys.coe.ac.emailio.config.action.IAction;
import com.cordys.coe.ac.emailio.config.trigger.ITrigger;
import com.cordys.coe.ac.emailio.connection.IEmailConnection;
import com.cordys.coe.ac.emailio.exception.EmailConnectionException;
import com.cordys.coe.ac.emailio.exception.StorageProviderException;
import com.cordys.coe.ac.emailio.exception.TriggerEngineException;
import com.cordys.coe.ac.emailio.localization.LogMessages;
import com.cordys.coe.ac.emailio.localization.TriggerEngineExceptionMessages;
import com.cordys.coe.ac.emailio.monitor.IJMXEmailBoxPoller;
import com.cordys.coe.ac.emailio.monitor.RuleContext;
import com.cordys.coe.ac.emailio.monitor.RuleContextContainer;
import com.cordys.coe.ac.emailio.outbound.ISMIMEConfiguration;
import com.cordys.coe.ac.emailio.storage.IEmailStorageProvider;
import com.cordys.coe.ac.emailio.triggerengine.tcb.ITriggerContextBuilder;
import com.cordys.coe.ac.emailio.triggerengine.tcb.TriggerContextBuilderFactory;
import com.cordys.coe.ac.emailio.triggerengine.work.ITriggerWork;
import com.cordys.coe.ac.emailio.triggerengine.work.TriggerWorkFactory;
import com.cordys.coe.ac.emailio.util.MailMessageUtil;
import com.cordys.coe.util.general.Util;
import com.cordys.coe.util.soap.SOAPWrapper;

import com.eibus.connector.nom.Connector;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.threadpool.Dispatcher;

import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.log4j.NDC;

/**
 * This class is the linking pin between trigger definitions and actual email messages. It will take
 * an email message and a trigger and it will evaluate both of them against eachother.
 *
 * @author  pgussow
 */
class TriggerEngine
    implements ITriggerEngine
{
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(TriggerEngine.class);
    /**
     * Holds the list of message to check.
     */
    private List<Message> m_alMessages;
    /**
     * Holds the connector to use for sending Cordys messages.
     */
    private Connector m_cConnector;
    /**
     * Holds the email box from which the messages originate.
     */
    private IEmailBox m_ebEmailBox;
    /**
     * Holds the current email connection.
     */
    private IEmailConnection m_ecConnection;
    /**
     * Holds the message counter to use.
     */
    private IJMXEmailBoxPoller m_emcCounter;
    /**
     * Holds the storage provider for the email messages and their states.
     */
    private IEmailStorageProvider m_espStorage;
    /**
     * Holds the S/MIME configuration.
     */
    private ISMIMEConfiguration m_scConfig;
    /**
     * Holds the folder name where this message comes from.
     */
    private String m_sFolderName;
    /**
     * Holds the trigger to evaluate.
     */
    private ITrigger m_tTrigger;

    /**
     * This method gets the trigger for the current engine.
     *
     * @return  The trigger for the current engine.
     *
     * @see     com.cordys.coe.ac.emailio.triggerengine.ITriggerEngine#getTrigger()
     */
    @Override public ITrigger getTrigger()
    {
        return m_tTrigger;
    }

    /**
     * This method will handle the trigger to see if this message matches the current trigger. If
     * something goes wrong validating the trigger an exception will be thrown. If the email message
     * did not match the trigger it will return false. If the message did match the trigger and
     * needs to be handled it will return true.<br>
     * TODO: We need to be aware of timing issues. What to do with a message which is put into
     * processing mode (into the dispatcher) and then it is picked up again in the next polling
     * interval?
     *
     * @param   dDispatcher  The dispatcher to assign the work if the messages match the trigger.
     *
     * @return  true if at least 1 of the messages in the list was processed.
     *
     * @throws  TriggerEngineException  In case of any exception.
     *
     * @see     com.cordys.coe.ac.emailio.triggerengine.ITriggerEngine#handleTrigger(com.eibus.util.threadpool.Dispatcher)
     */
    @Override public boolean handleTrigger(Dispatcher dDispatcher)
                                    throws TriggerEngineException
    {
        boolean bReturn = false;

        // Add the trigger context to the NDC
        NDC.push("trigger=" + m_tTrigger.getName());

        try
        {
            RuleContextContainer rccContext = new RuleContextContainer();

            for (Message mMessage : m_alMessages)
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Checking message using trigger " + m_tTrigger.getName() +
                              ". Message details:\n" + MailMessageUtil.dumpMessage(mMessage));
                }

                try
                {
                    if (!mMessage.isExpunged() && !mMessage.isSet(Flags.Flag.DELETED))
                    {
                        RuleContext rc = new RuleContext(mMessage);
                        boolean bShouldProcess = false;

                        try
                        {
                            // Build up the context for this message
                            ITriggerContextBuilder tcb = TriggerContextBuilderFactory
                                                         .createTriggerContextBuilder(rc, mMessage,
                                                                                      m_tTrigger,
                                                                                      m_scConfig);
                            bShouldProcess = tcb.processMessage();

                            if ((bReturn == false) && (bShouldProcess == true))
                            {
                                bReturn = true;
                            }

                            if (bShouldProcess == true)
                            {
                                if (LOG.isInfoEnabled())
                                {
                                    LOG.info(LogMessages.INF_FOUND_AN_EMAIL_THAT_MATCHES_TRIGGER,
                                             m_tTrigger.getName(),
                                             MailMessageUtil.dumpMessage(mMessage),
                                             m_tTrigger.toString());
                                }
                                rccContext.add(rc);

                                if (m_tTrigger.getCombineEmails() == false)
                                {
                                    // Send a trigger per email
                                    sendSOAPMessage(rccContext, dDispatcher);

                                    rccContext = new RuleContextContainer();
                                }

                                // It could be that the trigger is a 1 time only trigger. If it is a
                                // one time only trigger you do not want it to match more then 1
                                // email. Also a onetimeonly trigger should NEVER have the
                                // combineemails set to true (enforced in the constructor).
                                if (m_tTrigger.isOneTimeOnly())
                                {
                                    break;
                                }
                            }
                            else
                            {
                                if (LOG.isInfoEnabled())
                                {
                                    LOG.info(LogMessages.INF_IGNORING_MESSAGE_FOR_TRIGGER_EMAIL_CONTENT,
                                             m_tTrigger.getName(),
                                             MailMessageUtil.dumpMessage(mMessage));
                                }
                                m_emcCounter.incNrOfMessagesIgnored(1);
                            }
                        }
                        catch (TriggerEngineException tee)
                        {
                            // We will examine the cause of the exception. It could be that we get a
                            // java.io.UnsupportedEncodingException. In that case we will move the
                            // message to the error folder and trigger the error actions for this
                            // email to make sure it's acted upon.
                            Throwable tCause = tee.getCause();
                            boolean bHandled = false;

                            while (tCause != null)
                            {
                                if (tCause instanceof UnsupportedEncodingException)
                                {
                                    UnsupportedEncodingException uee = (UnsupportedEncodingException)
                                                                           tCause;

                                    // Yup, we got the unsupported encoding. So we need to do the
                                    // folowing: 1. Write the email to the error folder. 2. Trigger
                                    // the error actions.
                                    handleUnsupportedEncodingException(uee, rc);
                                    bHandled = true;
                                    break;
                                }
                                tCause = tCause.getCause();
                            }

                            // Now log this exception because we cannot figure out why this rule
                            // fails. Maybe some other trigger can do somthing with it.
                            if (!bHandled)
                            {
                                LOG.warn(tee, LogMessages.WRN_UNABLE_TO_PROCESS_MESSAGE,
                                         m_tTrigger.getName(), tee.getMessage(),
                                         MailMessageUtil.dumpMessage(mMessage));
                            }
                        }
                        finally
                        {
                            if (bShouldProcess == false)
                            {
                                // If processing is not needed we need to clean up the context
                                // immediately. If the context has to be processed the TriggerWork
                                // class will clean it up.
                                rc.clear();
                            }
                        }
                    }
                    else
                    {
                        if (LOG.isDebugEnabled())
                        {
                            LOG.debug("Ignoring message because it's either expunged or has the deleted flag.\n" +
                                      MailMessageUtil.dumpMessage(mMessage));
                        }
                    }
                }
                catch (MessagingException e)
                {
                    throw new TriggerEngineException(e,
                                                     TriggerEngineExceptionMessages.TEE_ERROR_CHECKING_THE_DELETED_FLAG);
                }
            }

            // For all remaining context send the trigger to cordys.
            if (rccContext.size() > 0)
            {
                sendSOAPMessage(rccContext, dDispatcher);
            }
        }
        finally
        {
            NDC.pop();
        }

        return bReturn;
    }

    /**
     * @see  ITriggerEngine#initialize(ITrigger, List, Connector, IEmailConnection, String,
     *       IJMXEmailBoxPoller, IEmailBox, IEmailStorageProvider,
     *       com.cordys.coe.ac.emailio.outbound.ISMIMEConfiguration)
     */
    @Override public void initialize(ITrigger tTrigger, List<Message> alMessages,
                                     Connector cConnector, IEmailConnection ecConnection,
                                     String sFolderName, IJMXEmailBoxPoller emcCounter,
                                     IEmailBox ebEmailBox, IEmailStorageProvider espStorage,
                                     ISMIMEConfiguration scConfig)
    {
        m_tTrigger = tTrigger;
        m_alMessages = alMessages;
        m_cConnector = cConnector;
        m_ecConnection = ecConnection;
        m_sFolderName = sFolderName;
        m_emcCounter = emcCounter;
        m_ebEmailBox = ebEmailBox;
        m_espStorage = espStorage;
        m_scConfig = scConfig;
    }

    /**
     * This method will handle an email which cannot be parsed because of an unsupported encoding.
     * The email will be persisted in the error folder. Then the message is removed from the actual
     * inbox to avoid that the message will be processed over and over again. Once the message is
     * removed the error actions which are defined on the trigger are executed.
     *
     * @param  uee  The encoding exception that occurred.
     * @param  rc   The context for the current mail.
     */
    private void handleUnsupportedEncodingException(UnsupportedEncodingException uee,
                                                    RuleContext rc)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("An UnsupportedEncodingException was found. Going to remove the message from the mailbox");
        }

        RuleContextContainer rccContext = new RuleContextContainer();
        rccContext.add(rc);

        // Store the message in the error folder
        boolean bOk = false;

        try
        {
            // Add the mail to the storage and change it's status.
            m_espStorage.addRuleContext(rccContext, m_tTrigger);
            m_espStorage.setContainerStatusActionError(rccContext, Util.getStackTrace(uee));

            m_ecConnection.removeMessage(m_sFolderName, rc.getMessage());
            bOk = true;
        }
        catch (Exception eStorage)
        {
            // Try to read some context information.
            String sFrom = "unknown";
            String sSentDate = "unknown";
            String sReceivedDate = "unknown";

            try
            {
                sFrom = MailMessageUtil.dumpAddressList(rc.getMessage().getFrom());
            }
            catch (Exception e)
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Error getting the from address", e);
                }
            }

            try
            {
                Date dTemp = rc.getMessage().getSentDate();

                if (dTemp != null)
                {
                    sSentDate = dTemp.toString();
                }
            }
            catch (Exception e)
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Error getting the sent date", e);
                }
            }

            try
            {
                Date dTemp = rc.getMessage().getReceivedDate();

                if (dTemp != null)
                {
                    sReceivedDate = dTemp.toString();
                }
            }
            catch (Exception e)
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Error getting the received date", e);
                }
            }

            LOG.error(eStorage, LogMessages.ERROR_STORING_THE_ERROR_WHEN_UNSUPPORTED_ENCODING,
                      sFrom, sSentDate, sReceivedDate);
        }

        // When the storage and the removal goes OK the error actions need to be fired.
        if (bOk)
        {
            ArrayList<IAction> alErrorActions = m_tTrigger.getActions(EEventType.ERROR);

            if ((alErrorActions != null) && (alErrorActions.size() > 0))
            {
                for (IAction aAction : alErrorActions)
                {
                    try
                    {
                        aAction.execute(rc, rc.getMessage(), new SOAPWrapper(m_cConnector));
                    }
                    catch (Exception e)
                    {
                        LOG.warn(e, LogMessages.ERROR_EXECUTING_ACTION_0_FOR_CONTEXTN1,
                                 aAction.getID(), rc.toString());
                    }
                }
            }
        }
    }

    /**
     * This method will take care of preparing the work-object that will be put in the thread pool.
     *
     * @param   rccContext   The RuleContext container.
     * @param   dDispatcher  The dispatcher to assign the work if the messages match the trigger.
     *
     * @throws  TriggerEngineException  In case of any exceptions
     */
    private void sendSOAPMessage(RuleContextContainer rccContext, Dispatcher dDispatcher)
                          throws TriggerEngineException
    {
        boolean bContinue = true;

        try
        {
            // This method will persist all emails in the current context and assign a unique ID to
            // each email. The status of the context will be 'INITIAL'
            m_espStorage.addRuleContext(rccContext, m_tTrigger);
        }
        catch (StorageProviderException spe)
        {
            LOG.error(spe, LogMessages.ERROR_STORING_THE_CONTEXT_IN_THE_STORAGE_PROVIDER);
            bContinue = false;
        }

        // Remove the message from the actual inbox so that it won't be processed again.
        if (bContinue)
        {
            for (RuleContext rcContext : rccContext)
            {
                try
                {
                    m_ecConnection.removeMessage(m_sFolderName, rcContext.getMessage());
                }
                catch (EmailConnectionException e)
                {
                    throw new TriggerEngineException(e,
                                                     TriggerEngineExceptionMessages.TEE_ERROR_DELETING_MESSAGES_FROM_THE_SERVER);
                }
            }

            // Add the processing of this message to the thread pool.
            ITriggerWork twWork = TriggerWorkFactory.createTriggerWork(rccContext,
                                                                       m_tTrigger.getMessage(),
                                                                       m_tTrigger, m_cConnector,
                                                                       m_emcCounter, m_ebEmailBox);
            dDispatcher.addWork(twWork);
        }
    }
}
