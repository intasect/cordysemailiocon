package com.cordys.coe.ac.emailio.connection;

import com.cordys.coe.ac.emailio.config.IIMAPEmailBox;
import com.cordys.coe.ac.emailio.exception.EmailConnectionException;

import java.util.Properties;

/**
 * This class makes the connection to an IMAP mailbox.
 *
 * @author  pgussow
 */
class IMAPConnection extends BaseEmailConnection
    implements IEmailConnection
{
    /**
     * Creates a new IMAPConnection object.
     *
     * @param   ebBox  The email box to connect to.
     *
     * @throws  EmailConnectionException  In case of any exceptions.
     */
    public IMAPConnection(IIMAPEmailBox ebBox)
                   throws EmailConnectionException
    {
        super("imap", ebBox);
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
        IIMAPEmailBox ebBox = (IIMAPEmailBox) getEmailBox();

        if (ebBox.isSSLEnabled())
        {
            // Note: SSL code has not been tested.
            pJavaMailProps.setProperty("mail.imap.socketFactory.class", SSL_FACTORY);

            pJavaMailProps.setProperty("mail.imap.socketFactory.fallback", "false");

            pJavaMailProps.setProperty("mail.imap.socketFactory.port",
                                       String.valueOf(ebBox.getPort()));

            java.security.Security.setProperty("ssl.SocketFactory.provider", SSL_FACTORY);
        }
    }
}
