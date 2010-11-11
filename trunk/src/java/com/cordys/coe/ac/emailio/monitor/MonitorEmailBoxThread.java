/**
 * Copyright 2005 Cordys R&D B.V. 
 * 
 * This file is part of the Cordys SAP Connector. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 /**
 * Copyright 2005 Cordys R&D B.V. 
 * 
 * This file is part of the Cordys SAP Connector. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package com.cordys.coe.ac.emailio.monitor;

import com.cordys.coe.ac.emailio.config.IEmailBox;
import com.cordys.coe.ac.emailio.config.trigger.ITrigger;
import com.cordys.coe.ac.emailio.connection.EmailConnectionFactory;
import com.cordys.coe.ac.emailio.connection.IEmailConnection;
import com.cordys.coe.ac.emailio.exception.EmailConnectionException;
import com.cordys.coe.ac.emailio.exception.EmailIOException;
import com.cordys.coe.ac.emailio.exception.InboundEmailException;
import com.cordys.coe.ac.emailio.localization.InboundEmailExceptionMessages;
import com.cordys.coe.ac.emailio.localization.LogMessages;
import com.cordys.coe.ac.emailio.outbound.ISMIMEConfiguration;
import com.cordys.coe.ac.emailio.storage.IEmailStorageProvider;
import com.cordys.coe.ac.emailio.triggerengine.ITriggerEngine;
import com.cordys.coe.ac.emailio.triggerengine.TriggerEngineFactory;
import com.cordys.coe.util.general.Util;

import com.eibus.connector.nom.Connector;

import com.eibus.management.AlertLevel;
import com.eibus.management.IAlertDefinition;
import com.eibus.management.IManagedComponent;
import com.eibus.management.IParameterDefinition;
import com.eibus.management.ISettingsCollection;
import com.eibus.management.OperationImpact;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.threadpool.Dispatcher;

import java.text.SimpleDateFormat;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import javax.mail.Message;

import org.apache.log4j.NDC;

/**
 * This thread will be started to monitor the email boxes for new mails.
 *
 * @author  pgussow
 */
public class MonitorEmailBoxThread extends Thread
    implements IJMXEmailBoxPoller
{
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(MonitorEmailBoxThread.class);
    /**
     * Holds the alert definition for the alert that will be sent whenever a message fails to
     * process.
     */
    private IAlertDefinition m_adProcessAlert;
    /**
     * Indicates whether or not the thread should stop.
     */
    private boolean m_bStop = false;
    /**
     * Holds the connector to use for sending requests to Cordys.
     */
    private Connector m_cConnector;
    /**
     * Holds the dispatcher where this thread should send it's work to.
     */
    private Dispatcher m_dDispatcher;
    /**
     * Holds the date on which polling was done last.
     */
    private Date m_dLastPollTime = new Date(0L);
    /**
     * Holds the email box that is being monitored.
     */
    private IEmailBox m_ebEmailBox;
    /**
     * Holds the connection to the email box.
     */
    private IEmailConnection m_ecConnection;
    /**
     * Holds the storage provider to be used.
     */
    private IEmailStorageProvider m_espStorage;
    /**
     * Holds the total number of messages that failed to process.
     */
    private long m_lNrOfMessagesFailed = 0;
    /**
     * Holds the total number of messages ignored.
     */
    private long m_lNrOfMessagesIgnored = 0;
    /**
     * Holds the total number of messages processed.
     */
    private long m_lNrOfMessagesProcessed = 0;
    /**
     * Holds the total number of messages successfully processed.
     */
    private long m_lNrOfMessagesSuccess = 0;
    /**
     * Holds the managed component for this poller.
     */
    private IManagedComponent m_mcComponent;
    /**
     * Holds the S/MIME configuration.
     */
    private ISMIMEConfiguration m_scConfig;
    /**
     * Holds the NDC of the parent thread.
     */
    private Stack<?> m_sNDC;

    /**
     * Creates a new MonitorEmailBoxThread object.
     *
     * @param   ebEmailBox   The email box that this thread should monitor.
     * @param   dDispatcher  The dispatcher to add the work to.
     * @param   cConnector   The connector to use for sending requests to Cordys.
     * @param   mcParent     The parent managed component. It is used to create a JMX component for
     *                       this thread.
     * @param   scConfig     The S/MIME configuration.
     *
     * @throws  EmailIOException  In case of any exceptions.
     */
    public MonitorEmailBoxThread(IEmailBox ebEmailBox, Dispatcher dDispatcher, Connector cConnector,
                                 IManagedComponent mcParent, ISMIMEConfiguration scConfig)
                          throws EmailIOException
    {
        m_ebEmailBox = ebEmailBox;
        m_cConnector = cConnector;
        m_dDispatcher = dDispatcher;
        m_espStorage = ebEmailBox.getStorageProvider();
        m_scConfig = scConfig;

        if (m_ebEmailBox == null)
        {
            throw new InboundEmailException(InboundEmailExceptionMessages.IEE_THE_EMAIL_BOX_IS_NULL);
        }

        if (m_dDispatcher == null)
        {
            throw new InboundEmailException(InboundEmailExceptionMessages.IEE_THE_DISPATCHER_MUST_BE_FILLED);
        }

        if (m_cConnector == null)
        {
            throw new InboundEmailException(InboundEmailExceptionMessages.IEE_THE_CONNECTOR_MUST_BE_FILLED);
        }

        setName("MET_" + ebEmailBox.getName());

        // Try to make the connection to the email box.
        try
        {
            m_ecConnection = EmailConnectionFactory.createConnection(ebEmailBox);
        }
        catch (EmailConnectionException e)
        {
            throw new InboundEmailException(e,
                                            InboundEmailExceptionMessages.IEE_ERROR_CREATING_EMAIL_CONNECTION,
                                            ebEmailBox.getName());
        }

        // Now we can create a managed component for this thread.
        m_mcComponent = mcParent.createSubComponent("EmailBoxPoller", "EBP_" + ebEmailBox.getName(),
                                                    LogMessages.JMX_SC_EMAIL_BOX_POLLER, this);

        m_mcComponent.createPropertyBasedValueCounter("nr_of_messages_failed",
                                                      LogMessages.JMX_VC_NR_OF_MESSAGES_FAILED,
                                                      "nrOfMessagesFailed", this);
        m_mcComponent.createPropertyBasedValueCounter("nr_of_messages_processed",
                                                      LogMessages.JMX_VC_NR_OF_MESSAGES_PROCESSED,
                                                      "nrOfMessagesProcessed", this);
        m_mcComponent.createPropertyBasedValueCounter("nr_of_messages_ignored",
                                                      LogMessages.JMX_VC_NR_OF_MESSAGES_IGNORED,
                                                      "nrOfMessagesIgnored", this);
        m_mcComponent.createPropertyBasedValueCounter("nr_of_messages_success",
                                                      LogMessages.JMX_VC_NR_OF_MESSAGES_SUCCESS,
                                                      "nrOfMessagesSuccess", this);

        // Cold settings
        ISettingsCollection scCollection = m_mcComponent.getSettingsCollection();
        scCollection.defineColdSetting("email_box_name", LogMessages.JMX_VC_EB_NAME, null,
                                       ebEmailBox.getName());
        scCollection.defineColdSetting("email_box_host", LogMessages.JMX_VC_EB_HOST, null,
                                       ebEmailBox.getHost());
        scCollection.defineColdSetting("email_box_port", LogMessages.JMX_VC_EB_PORT, null,
                                       ebEmailBox.getPort());
        scCollection.defineColdSetting("email_box_type", LogMessages.JMX_VC_EB_TYPE, null,
                                       ebEmailBox.getType().toString());
        scCollection.defineColdSetting("email_box_pollinterval", LogMessages.JMX_VC_EB_POLLINTERVAL,
                                       null, ebEmailBox.getPollInterval());
        scCollection.defineColdSetting("email_box_username", LogMessages.JMX_VC_EB_USERNAME, null,
                                       ebEmailBox.getUsername());

        // Define the alerts
        m_adProcessAlert = m_mcComponent.defineAlert(AlertLevel.ERROR,
                                                     LogMessages.JMX_ALERT_ERROR_PROCESSING_MESSAGE,
                                                     LogMessages.JMX_ALERT_ERROR_PROCESSING_MESSAGE_DESC);

        m_mcComponent.defineOperation("getEmailBoxDetails",
                                      LogMessages.JMX_OPERATION_GET_EMAILBOX_DETAILS,
                                      "getEmailBoxDetails", this, OperationImpact.INFO,
                                      (IParameterDefinition[]) null);

        m_mcComponent.defineOperation("pollNow", LogMessages.JMX_POLL_NOW, "pollNow", this,
                                      OperationImpact.ACTION, (IParameterDefinition[]) null);

        m_mcComponent.defineOperation("getLastPollTime", LogMessages.JMX_VC_LAST_POLL_TIME,
                                      "getLastPollTime", this, OperationImpact.INFO,
                                      (IParameterDefinition[]) null);

        // Let the email box know which managed component handles this email box.
        ebEmailBox.setManagedComponent(m_mcComponent);

        // Now we'll get the NDC of the current thread and store it. When the run method is called
        // we'll set the stack.
        m_sNDC = NDC.cloneStack();
    }

    /**
     * This method gets the email box that is monitored by this thread.
     *
     * @return  The email box that is monitored by this thread.
     */
    public IEmailBox getEmailBox()
    {
        return m_ebEmailBox;
    }

    /**
     * This method is used for the JMX operation that returns the configuration details for this
     * email box.
     *
     * @return  The XML configuration for this email box.
     */
    public String getEmailBoxDetails()
    {
        String sReturn = "";

        if (m_ebEmailBox != null)
        {
            sReturn = m_ebEmailBox.toString();
        }
        return sReturn;
    }

    /**
     * This method gets the time on which the polling was done last.
     *
     * @return  The time on which the polling was done last.
     */
    public String getLastPollTime()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

        return sdf.format(m_dLastPollTime);
    }

    /**
     * This method gets the number of messages that matched a trigger, but failed to process.
     *
     * @return  The number of messages that matched a trigger, but failed to process.
     */
    public long getNrOfMessagesFailed()
    {
        return m_lNrOfMessagesFailed;
    }

    /**
     * This method gets the number of messages this poller has examined, but didn't match any
     * pattern.
     *
     * @return  The number of messages this poller has examined, but didn't match any pattern.
     */
    public long getNrOfMessagesIgnored()
    {
        return m_lNrOfMessagesIgnored;
    }

    /**
     * This method gets the number of email messages that this poller has checked.
     *
     * @return  The number of email messages that this poller has checked.
     */
    public long getNrOfMessagesProcessed()
    {
        return m_lNrOfMessagesProcessed;
    }

    /**
     * This method gets the number of email messages that this poller has checked.
     *
     * @return  The number of email messages that this poller has checked.
     */
    public long getNrOfMessagesSuccess()
    {
        return m_lNrOfMessagesSuccess;
    }

    /**
     * This method increases the total number of messages that failed to process.
     *
     * @param  lAmount  The amount of messages to add to the counter.
     *
     * @see    com.cordys.coe.ac.emailio.monitor.IJMXEmailBoxPoller#incNrOfMessagesFailed(long)
     */
    public synchronized void incNrOfMessagesFailed(long lAmount)
    {
        m_lNrOfMessagesFailed += lAmount;
    }

    /**
     * This method increases the total number of messages that have been ignored because they didn't
     * match any trigger.
     *
     * @param  lAmount  The amount of messages to add to the counter.
     *
     * @see    com.cordys.coe.ac.emailio.monitor.IJMXEmailBoxPoller#incNrOfMessagesIgnored(long)
     */
    public synchronized void incNrOfMessagesIgnored(long lAmount)
    {
        m_lNrOfMessagesIgnored += lAmount;
    }

    /**
     * This method increases the total number of messages that have been processed.
     *
     * @param  lAmount  The amount of messages to add to the counter.
     *
     * @see    com.cordys.coe.ac.emailio.monitor.IJMXEmailBoxPoller#incNrOfMessagesProcessed(long)
     */
    public synchronized void incNrOfMessagesProcessed(long lAmount)
    {
        m_lNrOfMessagesProcessed += lAmount;
    }

    /**
     * This method increases the total number of messages that have been successfully processed.
     *
     * @param  lAmount  The amount of messages to add to the counter.
     *
     * @see    com.cordys.coe.ac.emailio.monitor.IJMXEmailBoxPoller#incNrOfMessagesSuccess(long)
     */
    public synchronized void incNrOfMessagesSuccess(long lAmount)
    {
        m_lNrOfMessagesSuccess += lAmount;
    }

    /**
     * This method can be used to send out an alert that a specific message failed to process.
     *
     * @param  tException  The exception that occurred.
     * @param  sContext    The context information.
     *
     * @see    com.cordys.coe.ac.emailio.monitor.IJMXEmailBoxPoller#notifyProcessingError(Throwable, String)
     */
    public void notifyProcessingError(Throwable tException, String sContext)
    {
        String sException = "";

        if (tException != null)
        {
            sException = Util.getStackTrace(tException);
        }

        m_adProcessAlert.issueAlert(tException, sException, sContext);
    }

    /**
     * This method will force that the polling cycle will start. This method will check to make sure
     * that the current thread calling this method is NOT the same as the actual poller thread. If
     * the current thread is the poller thread this method will do nothing.
     */
    public void pollNow()
    {
        if (Thread.currentThread() != this)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Interrupting the sleep WITHOUT stopping the poller.");
            }
            interrupt();
        }
    }

    /**
     * This method resets all counters.
     *
     * @see  com.cordys.coe.ac.emailio.monitor.IJMXEmailBoxPoller#reset()
     */
    public synchronized void reset()
    {
        m_lNrOfMessagesFailed = 0;
        m_lNrOfMessagesIgnored = 0;
        m_lNrOfMessagesProcessed = 0;
        m_lNrOfMessagesSuccess = 0;
    }

    /**
     * This is the main.
     *
     * @see  java.lang.Thread#run()
     */
    @Override public void run()
    {
        NDC.inherit(m_sNDC);

        // TODO: Make some kind of JMX problem solver so that this thread can be resumed via JMX
        // operations.
        try
        {
            while (!shouldStop())
            {
                // Monitor the email box
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Going to poll the email box: " + m_ebEmailBox.getName());
                }

                doEmailBoxPolling();

                // the connection must be closed between polling cycles because some
                // mail servers will not write new mails to the mailboxes while there
                // are still open connections!
                closeConnection(m_ecConnection);

                try
                {
                    Thread.sleep(m_ebEmailBox.getPollInterval());
                }
                catch (Exception e)
                {
                    if (LOG.isWarningEnabled())
                    {
                        LOG.warn(e, LogMessages.ERR_MET_SLEEP, m_ebEmailBox.getName());
                    }
                }
            }
        }
        catch (Exception e)
        {
            LOG.error(e, LogMessages.ERR_MET_EXCEPTION_WATCHING, m_ebEmailBox.getName());
        }
        catch (Throwable t)
        {
            LOG.fatal(t, LogMessages.ERR_MET_FATAL_WATCHING, m_ebEmailBox.getName());
        }
        finally
        {
            closeConnection(m_ecConnection);
        }
    }

    /**
     * This method sets whether or not the thread should stop.
     *
     * @param  bStop  Whether or not the thread should stop.
     */
    public void setShouldStop(boolean bStop)
    {
        m_bStop = bStop;
    }

    /**
     * This method gets whether or not the thread should stop.
     *
     * @return  Whether or not the thread should stop.
     */
    public boolean shouldStop()
    {
        return m_bStop;
    }

    /**
     * This method closes the given email connection.
     *
     * @param  con  The connection to close.
     */
    private void closeConnection(IEmailConnection con)
    {
        if (con != null)
        {
            try
            {
                con.close();
            }
            catch (EmailConnectionException e)
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Error closing email connection.", e);
                }
            }
        }
    }

    /**
     * This method does the actual polling on the email box. When a message matches the criteria set
     * it will add work to the dispatcher to process the email message.
     */
    private void doEmailBoxPolling()
    {
        // First step is to get all the email messages that are available for this connection.
        String[] asFolders = m_ebEmailBox.getEmailFolders();

        if (LOG.isDebugEnabled())
        {
            LOG.debug("EMail polling starts now, monitoring " + asFolders.length +
                      " folders for Mailbox " + m_ebEmailBox.getName());
        }

        // Set the time on which the last polling was done.
        m_dLastPollTime = new Date();

        try
        {
            for (int iCount = 0; iCount < asFolders.length; iCount++)
            {
                String sFolderName = asFolders[iCount];

                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Retrieving messages for folder " + sFolderName);
                }

                // Get the email messages for this
                try
                {
                    Message[] amMessages = m_ecConnection.getEmailHeaders(sFolderName);
                    List<Message> alMessages = Arrays.asList(amMessages);

                    if (alMessages.size() > 0)
                    {
                        if (LOG.isDebugEnabled())
                        {
                            LOG.debug("Found " + alMessages.size() + " messages in folder " +
                                      sFolderName);
                        }

                        // Increase JMX counter
                        incNrOfMessagesProcessed(alMessages.size());

                        // TODO: It could be that the emails are encrypted. This can be detected by
                        // the content type of the individual message. If the content type is
                        // application/x-pkcs7-mime we first need to decrypt the message before it
                        // goes into the processing zone.

                        // Get the triggers defined for this mail box
                        ITrigger[] atTriggers = m_ebEmailBox.getTriggers(sFolderName);

                        if (LOG.isDebugEnabled())
                        {
                            LOG.debug("Found " + atTriggers.length + " triggers for folder " +
                                      sFolderName);
                        }

                        // The triggers will be executed in sequence. If a message matches the
                        // trigger and is scheduled to be handled it is removed from the list. So
                        // the next trigger will work on the remaining messages in the list.
                        for (int iTriggerCount = 0; iTriggerCount < atTriggers.length;
                                 iTriggerCount++)
                        {
                            ITrigger tTrigger = atTriggers[iTriggerCount];

                            try
                            {
                                ITriggerEngine te = TriggerEngineFactory.createTriggerEngine(tTrigger,
                                                                                             alMessages,
                                                                                             m_cConnector,
                                                                                             m_ecConnection,
                                                                                             sFolderName,
                                                                                             this,
                                                                                             m_ebEmailBox,
                                                                                             m_espStorage,
                                                                                             m_scConfig);

                                if (te.handleTrigger(m_dDispatcher) == true)
                                {
                                    // The trigger matched and was processed. Now check if
                                    // it's a one-time only trigger. If that's the case the
                                    // trigger needs to ne removed.
                                    if (tTrigger.isOneTimeOnly())
                                    {
                                        m_ebEmailBox.removeTrigger(tTrigger);
                                    }
                                }
                            }
                            catch (Exception e)
                            {
                                // Log the error and continue with the next trigger.
                                LOG.error(e, LogMessages.ERR_MET_HANDLING_TRIGGER,
                                          tTrigger.getName(), sFolderName);
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    LOG.error(e, LogMessages.ERR_MET_HANDLING_MESSAGES, sFolderName);
                }
            }
        }
        finally
        {
            // We need to update the server to make sure that the
            // message actually is deleted.
            try
            {
                m_ecConnection.expunge();
            }
            catch (Exception e)
            {
                LOG.error(e, LogMessages.ERROR_CLOSING_FOLDERS);
            }
        }
    }
}
