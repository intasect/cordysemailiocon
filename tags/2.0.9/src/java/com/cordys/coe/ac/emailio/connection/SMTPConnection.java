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
 package com.cordys.coe.ac.emailio.connection;

import com.cordys.coe.ac.emailio.config.outbound.ISMTPServer;
import com.cordys.coe.ac.emailio.exception.OutboundEmailException;
import com.cordys.coe.ac.emailio.localization.OutboundEmailExceptionMessages;

import com.eibus.util.logger.CordysLogger;

import java.io.ByteArrayOutputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;

import javax.mail.internet.MimeMessage;

/**
 * This class implements an actual SMTP connection. It will be capable of sending a JavaMail message
 * to the given server.
 *
 * @author  pgussow
 */
class SMTPConnection
    implements ISMTPConnection
{
    /**
     * Holds the logger to use.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(SMTPConnection.class);
    /**
     * Holds the ID of this connection.
     */
    private String m_sConnectionID;
    /**
     * Holds the connection pool this connection belongs to.
     */
    private ISMTPConnectionPool m_scpPool;
    /**
     * Holds the JavaMail session wrapping the current connection parameters.
     */
    private Session m_sSession;
    /**
     * Holds the configuration.
     */
    private ISMTPServer m_ssServer;

    /**
     * Creates a new SMTPConnection object.
     *
     * @param  ssServer       The server configuration.
     * @param  scpPool        A reference to the pool to which this connection belongs. It is used
     *                        to be able to release itself back to the pool when it's done.
     * @param  sConnectionID  The ID for this connection.
     */
    public SMTPConnection(ISMTPServer ssServer, ISMTPConnectionPool scpPool, String sConnectionID)
    {
        m_ssServer = ssServer;
        m_scpPool = scpPool;
        m_sConnectionID = sConnectionID;

        // Determine SSL
        String sProtocol = "smtp";

        if (m_ssServer.isSSLEnabled())
        {
            sProtocol += "s";
        }

        // Create the session object for this connection.
        Properties pProps = new Properties();

        pProps.setProperty("mail." + sProtocol + ".host", m_ssServer.getHost());
        pProps.setProperty("mail." + sProtocol + ".port", String.valueOf(m_ssServer.getPort()));

        Authenticator aAuthenticator = null;

        if ((m_ssServer.getUsername() != null) && (m_ssServer.getUsername().trim().length() > 0))
        {
            aAuthenticator = new Authenticator()
                {
                    @Override public PasswordAuthentication getPasswordAuthentication()
                    {
                        return new PasswordAuthentication(m_ssServer.getUsername(),
                                                          m_ssServer.getPassword());
                    }
                };
        }

        m_sSession = Session.getInstance(pProps, aAuthenticator);
    }

    /**
     * @see  com.cordys.coe.ac.emailio.connection.ISMTPConnection#getConnectionID()
     */
    @Override public String getConnectionID()
    {
        return m_sConnectionID;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.connection.ISMTPConnection#getConnectionPool()
     */
    public ISMTPConnectionPool getConnectionPool()
    {
        return m_scpPool;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.connection.ISMTPConnection#getSession()
     */
    public Session getSession()
    {
        return m_sSession;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.connection.ISMTPConnection#releaseConnection()
     */
    @Override public void releaseConnection()
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Releasing connection (" + getConnectionID() + ")");
        }

        if (m_scpPool != null)
        {
            m_scpPool.releaseConnection(this);
        }
    }

    /**
     * @see  com.cordys.coe.ac.emailio.connection.ISMTPConnection#sendMessage(MimeMessage)
     */
    @Override public void sendMessage(MimeMessage mMessage)
                               throws OutboundEmailException
    {
        List<MimeMessage> lTemp = new ArrayList<MimeMessage>();
        lTemp.add(mMessage);

        sendMessage(lTemp);
    }

    /**
     * @see  com.cordys.coe.ac.emailio.connection.ISMTPConnection#sendMessage(java.util.List)
     */
    @Override public void sendMessage(List<MimeMessage> lMessages)
                               throws OutboundEmailException
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Going to send " + lMessages.size() + " messages.");
        }
        m_scpPool.incNrOfMessagesProcessed(lMessages.size());

        try
        {
            for (Message mMessage : lMessages)
            {
                if (LOG.isDebugEnabled())
                {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    mMessage.writeTo(baos);

                    LOG.debug("Sending mail:\n" + baos.toString("ISO-8859-1"));
                }

                Transport.send(mMessage);
            }

            m_scpPool.incNrOfMessagesSuccess(lMessages.size());
        }
        catch (Exception e)
        {
            m_scpPool.incNrOfMessagesFailed(lMessages.size());

            OutboundEmailException oee = new OutboundEmailException(e,
                                                                    OutboundEmailExceptionMessages.OEE_ERROR_SENDING_0_MESSAGES_USING_SERVER,
                                                                    m_sConnectionID,
                                                                    lMessages.size(),
                                                                    m_ssServer.getHost(),
                                                                    m_ssServer.getPort());

            // Send the JMX alert
            m_scpPool.notifyProcessingError(oee, lMessages.toString());

            // Make sure the exception is thrown.
            throw oee;
        }
    }
}
