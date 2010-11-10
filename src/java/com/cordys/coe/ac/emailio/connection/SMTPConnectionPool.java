package com.cordys.coe.ac.emailio.connection;

import com.cordys.coe.ac.emailio.config.outbound.ISMTPServer;
import com.cordys.coe.ac.emailio.exception.OutboundEmailException;
import com.cordys.coe.ac.emailio.localization.LogMessages;
import com.cordys.coe.ac.emailio.localization.OutboundEmailExceptionMessages;
import com.cordys.coe.util.general.Util;

import com.eibus.management.AlertLevel;
import com.eibus.management.IAlertDefinition;
import com.eibus.management.IManagedComponent;
import com.eibus.management.ISettingsCollection;

import com.eibus.util.Queue;
import com.eibus.util.logger.CordysLogger;

import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.MimeMessage;

/**
 * This class implements the SMTP connection pool. This class also creates a JMX component which
 * holds information on the current connection.
 *
 * @author  pgussow
 */
class SMTPConnectionPool
    implements ISMTPConnectionPool
{
    /**
     * Holds the logger to use.
     */
    static final CordysLogger LOG = CordysLogger.getCordysLogger(SMTPConnectionPool.class);
    /**
     * Holds whether or not a connection pool is really used.
     */
    boolean m_bUsingPool;
    /**
     * Holds the queue with the connections that are available to use.
     */
    Queue m_qConnections;
    /**
     * Holds the configuration on which the pool is based.
     */
    ISMTPServer m_ssServer;
    /**
     * Holds the JMX alert capable of sending the JMX alert when the sending of a mail fails.
     */
    private IAlertDefinition m_adProcessAlert;
    /**
     * Holds the total number of messages that failed to process.
     */
    private long m_lNrOfMessagesFailed = 0;
    /**
     * Holds the total number of messages processed.
     */
    private long m_lNrOfMessagesProcessed = 0;
    /**
     * Holds the total number of messages successfully processed.
     */
    private long m_lNrOfMessagesSuccess = 0;
    /**
     * Holds the managed component for this connection.
     */
    private IManagedComponent m_mcComponent;

    /**
     * Creates a new SMTPConnectionPool object.
     *
     * @param  ssServer  The server configuration.
     * @param  mcParent  The JMX parent to use for creating the JMX data.
     */
    SMTPConnectionPool(ISMTPServer ssServer, IManagedComponent mcParent)
    {
        m_ssServer = ssServer;

        // Create the Queue if needed.
        m_bUsingPool = m_ssServer.useConnectionPool();

        if (m_ssServer.useConnectionPool())
        {
            // Also when using authentication we can use
            m_qConnections = new Queue(m_ssServer.getMaxNrOfConnections());

            // Now create the connections to put in the pool.
            createConnectionsForPool();
        }

        if (mcParent != null)
        {
            m_mcComponent = mcParent.createSubComponent("SMTPConnectionPool",
                                                        "SCP_" + ssServer.getName(),
                                                        LogMessages.JMX_SC_SMTP_CONNECTION_POOL,
                                                        this);

            // Define the counters.
            m_mcComponent.createPropertyBasedValueCounter("nr_of_messages_failed",
                                                          LogMessages.JMX_VC_NR_OF_MESSAGES_FAILED,
                                                          "nrOfMessagesFailed", this);
            m_mcComponent.createPropertyBasedValueCounter("nr_of_messages_processed",
                                                          LogMessages.JMX_VC_NR_OF_MESSAGES_PROCESSED,
                                                          "nrOfMessagesProcessed", this);
            m_mcComponent.createPropertyBasedValueCounter("nr_of_messages_success",
                                                          LogMessages.JMX_VC_NR_OF_MESSAGES_SUCCESS,
                                                          "nrOfMessagesSuccess", this);

            // Define the alerts
            m_adProcessAlert = m_mcComponent.defineAlert(AlertLevel.ERROR,
                                                         LogMessages.JMX_ALERT_ERROR_SENDING_MESSAGE,
                                                         LogMessages.JMX_ALERT_ERROR_SENDING_MESSAGE_DESC);

            // Cold settings
            ISettingsCollection scCollection = m_mcComponent.getSettingsCollection();
            scCollection.defineColdSetting("smtp_name", LogMessages.JMX_VC_SMTP_NAME, null,
                                           m_ssServer.getName());
            scCollection.defineColdSetting("smtp_host", LogMessages.JMX_VC_SMTP_HOST, null,
                                           m_ssServer.getHost());
            scCollection.defineColdSetting("smtp_port", LogMessages.JMX_VC_SMTP_PORT, null,
                                           m_ssServer.getPort());
            scCollection.defineColdSetting("smtp_username", LogMessages.JMX_VC_SMTP_USERNAME, null,
                                           m_ssServer.getUsername());
            scCollection.defineColdSetting("smtp_ssl", LogMessages.JMX_VC_SMTP_SSL, null,
                                           m_ssServer.isSSLEnabled());
            scCollection.defineColdSetting("smtp_use_connection_pool",
                                           LogMessages.JMX_VC_SMTP_CONNECTION_POOL, null,
                                           m_ssServer.useConnectionPool());

            if (m_ssServer.useConnectionPool())
            {
                scCollection.defineColdSetting("smtp_max_nr_of_connections",
                                               LogMessages.JMX_VC_SMTP_MAX_CONN, null,
                                               m_ssServer.getMaxNrOfConnections());
                scCollection.defineColdSetting("smtp_connection_timeout",
                                               LogMessages.JMX_VC_SMTP_CONN_TIMEOUT, null,
                                               m_ssServer.getConnectionTimeout());

                scCollection.defineColdSetting("smtp_free_connections",
                                               LogMessages.JMX_VC_SMTP_CONN_TIMEOUT, null,
                                               m_qConnections.size());
            }
        }
        else if (LOG.isDebugEnabled())
        {
            LOG.debug("Cannot create managed component beacuse parent is null");
        }
    }

    /**
     * @see  com.cordys.coe.ac.emailio.connection.ISMTPConnectionPool#getConfiguration()
     */
    @Override public ISMTPServer getConfiguration()
    {
        return m_ssServer;
    }

    /**
     * This method returns the connection that can be used to send a mail. If no pool is used it
     * will return a new SMTP connection.
     *
     * @return  The connection to use.
     */
    @Override public ISMTPConnection getConnection()
    {
        ISMTPConnection scReturn = null;

        if (m_bUsingPool)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Waiting for free connection");
            }

            scReturn = (ISMTPConnection) m_qConnections.get();

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Obtained a connection (" + scReturn.getConnectionID() + ")");
            }
        }
        else
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Creating a new SMTP connection");
            }
            scReturn = new SMTPConnection(m_ssServer, this, m_ssServer.getHost() + "-connection");
        }

        return scReturn;
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
     * This method gets the configuration which is the base for this connection pool.
     *
     * @return  The configuration which is the base for this connection pool.
     */
    public ISMTPServer getSMTPServerConfiguration()
    {
        return m_ssServer;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.connection.IJMXSMTPConnectionPool#incNrOfMessagesFailed(long)
     */
    @Override public void incNrOfMessagesFailed(long lAmount)
    {
        m_lNrOfMessagesFailed += lAmount;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.connection.IJMXSMTPConnectionPool#incNrOfMessagesProcessed(long)
     */
    @Override public void incNrOfMessagesProcessed(long lAmount)
    {
        m_lNrOfMessagesProcessed += lAmount;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.connection.IJMXSMTPConnectionPool#incNrOfMessagesSuccess(long)
     */
    @Override public void incNrOfMessagesSuccess(long lAmount)
    {
        m_lNrOfMessagesSuccess += lAmount;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.connection.IJMXSMTPConnectionPool#notifyProcessingError(java.lang.Throwable,
     *       java.lang.String)
     */
    @Override public void notifyProcessingError(Throwable tException, String sContext)
    {
        String sException = "";

        if (tException != null)
        {
            sException = Util.getStackTrace(tException);
        }

        m_adProcessAlert.issueAlert(tException, sException, sContext);
    }

    /**
     * @see  com.cordys.coe.ac.emailio.connection.ISMTPConnectionPool#releaseConnection(ISMTPConnection)
     */
    @Override public void releaseConnection(ISMTPConnection scConnection)
    {
        if (m_bUsingPool)
        {
            m_qConnections.put(scConnection);
        }
    }

    /**
     * @see  com.cordys.coe.ac.emailio.connection.IJMXSMTPConnectionPool#reset()
     */
    @Override public void reset()
    {
        m_lNrOfMessagesFailed = 0;
        m_lNrOfMessagesProcessed = 0;
        m_lNrOfMessagesSuccess = 0;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.connection.ISMTPConnectionPool#sendMessage(javax.mail.internet.MimeMessage)
     */
    @Override public void sendMessage(MimeMessage mMessage)
                               throws OutboundEmailException
    {
        List<MimeMessage> lTemp = new ArrayList<MimeMessage>();
        lTemp.add(mMessage);

        sendMessage(lTemp);
    }

    /**
     * @see  com.cordys.coe.ac.emailio.connection.ISMTPConnectionPool#sendMessage(List)
     */
    @Override public void sendMessage(List<MimeMessage> lMessages)
                               throws OutboundEmailException
    {
        ISMTPConnection scConn = getConnection();

        if (scConn == null)
        {
            throw new OutboundEmailException(OutboundEmailExceptionMessages.OEE_COULD_NOT_OBTAIN_A_SMTP_CONNECTION);
        }

        scConn.sendMessage(lMessages);
    }

    /**
     * This method pre-creates the connections that are needed to send the message.
     */
    private void createConnectionsForPool()
    {
        for (int iCount = 0; iCount < m_ssServer.getMaxNrOfConnections(); iCount++)
        {
            ISMTPConnection scConn = new SMTPConnection(m_ssServer, this,
                                                        m_ssServer.getHost() + "-connection-" +
                                                        (iCount + 1));
            m_qConnections.put(scConn);
        }
    }
}
