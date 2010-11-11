package com.cordys.coe.ac.emailio.connection;

import com.cordys.coe.ac.emailio.config.IEmailBox;
import com.cordys.coe.ac.emailio.exception.EmailConnectionException;
import com.cordys.coe.ac.emailio.localization.EmailConnectionExceptionMessages;

import com.sun.mail.pop3.POP3SSLStore;

import java.util.HashMap;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

/**
 * This class wraps the POP3 connections.
 *
 * @author  pgussow
 */
public class POP3Connection extends BaseEmailConnection
{
    /**
     * Creates a new POP3Connection object.
     *
     * @param   ebBox  The configuration of the email box.
     *
     * @throws  EmailConnectionException  In case of any exceptions.
     */
    public POP3Connection(IEmailBox ebBox)
                   throws EmailConnectionException
    {
        super(ebBox.isSSLEnabled() ? "pop3s" : "pop3", ebBox);
        // With every polling cycle we'll reconnect, since otherwise the messages won't get
        // refreshed.
        setAlwaysReconnect(true);
    }

    /**
     * This method is called to expunge the deleted messages. For POP3 this might not be supported.
     *
     * @throws  EmailConnectionException  In case of any exception.
     *
     * @see     com.cordys.coe.ac.emailio.connection.BaseEmailConnection#expunge()
     */
    @Override public void expunge()
                           throws EmailConnectionException
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Expunge is not supported for POP3. We will do a reconnect instead");
        }
        reconnect();
    }

    /**
     * This method closes the current connections.
     *
     * @throws  EmailConnectionException  In case of any exceptions.
     */
    @Override protected void closeCurrentConnection()
                                             throws EmailConnectionException
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Closing current connection");
        }

        HashMap<String, Folder> hmOpenFolder = getOpenFolders();

        if (hmOpenFolder.size() > 0)
        {
            for (Folder fFolder : hmOpenFolder.values())
            {
                try
                {
                    if (fFolder.isOpen())
                    {
                        if (LOG.isDebugEnabled())
                        {
                            // To check the message flags.
                            Message[] am = fFolder.getMessages();

                            for (Message message : am)
                            {
                                StringBuilder sbTemp = new StringBuilder(2048);
                                Flags f = message.getFlags();
                                sbTemp.append("Answered: " + f.contains(Flags.Flag.ANSWERED))
                                      .append(", ");
                                sbTemp.append("Deleted: " + f.contains(Flags.Flag.DELETED)).append(", ");
                                sbTemp.append("Draft: " + f.contains(Flags.Flag.DRAFT)).append(", ");
                                sbTemp.append("Flagged: " + f.contains(Flags.Flag.FLAGGED)).append(", ");
                                sbTemp.append("Recent: " + f.contains(Flags.Flag.RECENT)).append(", ");
                                sbTemp.append("Seen: " + f.contains(Flags.Flag.SEEN)).append(", ");
                                sbTemp.append("User: " + f.contains(Flags.Flag.USER)).append(", ");

                                if (LOG.isDebugEnabled())
                                {
                                    LOG.debug(sbTemp.toString());
                                }
                            }
                        }

                        fFolder.close(true);
                    }
                }
                catch (MessagingException e)
                {
                    throw new EmailConnectionException(e,
                                                       EmailConnectionExceptionMessages.ECE_ERROR_CLOSING_THE_FOLDER,
                                                       fFolder.getFullName());
                }
            }
        }

        closeStore();
    }

    /**
     * @see  com.cordys.coe.ac.emailio.connection.BaseEmailConnection#createAndConnectEmailStore(javax.mail.Session,
     *       com.cordys.coe.ac.emailio.config.IEmailBox)
     */
    @Override protected Store createAndConnectEmailStore(Session session, IEmailBox emailBox)
                                                  throws NoSuchProviderException, MessagingException
    {
        Store returnValue = null;

        if (getEmailBox().isSSLEnabled())
        {
            URLName url = new URLName("pop3", emailBox.getHost(), emailBox.getPort(), "",
                                      emailBox.getUsername(), emailBox.getPassword());

            returnValue = new POP3SSLStore(session, url);
            returnValue.connect();
        }
        else
        {
            returnValue = super.createAndConnectEmailStore(session, emailBox);
        }

        return returnValue;
    }

    /**
     * This method can be overloaded for a connection to fill the list of properties with connection
     * specific properties. For IMAP it could also be an SSL connection.
     *
     * @param  pJavaMailProps  The current list of properties.
     *
     * @see    com.cordys.coe.ac.emailio.connection.BaseEmailConnection#fillAdditionalProperties(java.util.Properties)
     */
    @Override protected void fillAdditionalProperties(Properties pJavaMailProps)
    {
        // Can't use m_ebBox because this method is called as an adapter from the parent's
        // constructor.
        IEmailBox ebBox = getEmailBox();

        if (ebBox.isSSLEnabled())
        {
            // Note: SSL code has not been tested.
            pJavaMailProps.setProperty("mail." + getStoreType() + ".socketFactory.class",
                                       SSL_FACTORY);

            pJavaMailProps.setProperty("mail." + getStoreType() + ".socketFactory.fallback",
                                       "false");

            pJavaMailProps.setProperty("mail." + getStoreType() + ".socketFactory.port",
                                       String.valueOf(ebBox.getPort()));

            // java.security.Security.setProperty("ssl.SocketFactory.provider", SSL_FACTORY);
        }
    }
}
