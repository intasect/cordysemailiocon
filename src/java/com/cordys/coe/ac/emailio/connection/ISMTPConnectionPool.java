package com.cordys.coe.ac.emailio.connection;

import com.cordys.coe.ac.emailio.config.outbound.ISMTPServer;
import com.cordys.coe.ac.emailio.exception.OutboundEmailException;

import java.util.List;

import javax.mail.internet.MimeMessage;

/**
 * This interface describes the conn.
 *
 * @author  pgussow
 */
public interface ISMTPConnectionPool extends IJMXSMTPConnectionPool
{
    /**
     * This method returns the configuration of the SMTP server.
     *
     * @return  The configuration of the SMTP server.
     */
    ISMTPServer getConfiguration();

    /**
     * This method returns the connection that can be used to send a mail. If no pool is used it
     * will return a new SMTP connection.
     *
     * @return  The connection to use.
     */
    ISMTPConnection getConnection();

    /**
     * This method releases the passed on connection to the pool again. If no pool is used this
     * method call will do nothing.
     *
     * @param  scConnection  The connection to release.
     */
    void releaseConnection(ISMTPConnection scConnection);

    /**
     * This method sends the message. It will get a connection from the pool and use it to send the
     * message.
     *
     * @param   mMessage  The message to send.
     *
     * @throws  OutboundEmailException  In case of any exceptions.
     */
    void sendMessage(MimeMessage mMessage)
              throws OutboundEmailException;

    /**
     * This method sends the messages. It will get a connection from the pool and use it to send the
     * messages.
     *
     * @param   lMessages  The messages to send.
     *
     * @throws  OutboundEmailException  In case of any exceptions.
     */
    void sendMessage(List<MimeMessage> lMessages)
              throws OutboundEmailException;
}
