package com.cordys.coe.ac.emailio.connection;

import com.cordys.coe.ac.emailio.exception.OutboundEmailException;

import java.util.List;

import javax.mail.Session;

import javax.mail.internet.MimeMessage;

/**
 * This interface describes the operations for a SMTP Connection.
 *
 * @author  pgussow
 */
public interface ISMTPConnection
{
    /**
     * This method gets the ID for this connection.
     *
     * @return  The ID for this connection.
     */
    String getConnectionID();

    /**
     * This method gets the connection pool this connection belongs to.
     *
     * @return  The connection pool this connection belongs to.
     */
    ISMTPConnectionPool getConnectionPool();

    /**
     * This method gets the session for this connection.
     *
     * @return  The session for this connection.
     */
    Session getSession();

    /**
     * This method releases the passed on connection to the pool again. If no pool is used this
     * method call will do nothing.
     */
    void releaseConnection();

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
     * This method sends the given messages using this connection.
     *
     * @param   lMessages  The messages to send.
     *
     * @throws  OutboundEmailException  In case of any exceptions.
     */
    void sendMessage(List<MimeMessage> lMessages)
              throws OutboundEmailException;
}
