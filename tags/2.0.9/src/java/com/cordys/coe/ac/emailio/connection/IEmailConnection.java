package com.cordys.coe.ac.emailio.connection;

import com.cordys.coe.ac.emailio.exception.EmailConnectionException;

import javax.mail.Message;

/**
 * This interface describes the interface for working with an email connection.
 *
 * @author  pgussow
 */
public interface IEmailConnection
{
    /**
     * Holds the name of the SSL socket factory to use.
     */
    String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

    /**
     * This method closes the current connection. NOTE: You MUST call this method when you're done.
     * If you don't the messages at the server won't get updated.
     *
     * @throws  EmailConnectionException  In case of any exceptions.
     */
    void close()
        throws EmailConnectionException;

    /**
     * This method makes sure that all messages marked for deletion are really deleted.
     *
     * @throws  EmailConnectionException  In case of any exceptions.
     */
    void expunge()
          throws EmailConnectionException;

    /**
     * This method gets the headers of all messages that are in the default folder. The default
     * folder is the folder that was registered first.
     *
     * @return  The headers of all messages that are in the inbox folder.
     *
     * @throws  EmailConnectionException  In case of any exceptions.
     */
    Message[] getEmailHeaders()
                       throws EmailConnectionException;

    /**
     * This method gets the headers of all messages that are in the specified folder.
     *
     * @param   sFolder  The name of the folder to get the messages for.
     *
     * @return  The headers of all messages that are in the inbox folder.
     *
     * @throws  EmailConnectionException  In case of any exceptions.
     */
    Message[] getEmailHeaders(String sFolder)
                       throws EmailConnectionException;

    /**
     * This method removes the given message from the given folder.
     *
     * @param   sFolderName  The name of the folder.
     * @param   mMessage     The message to remove.
     *
     * @throws  EmailConnectionException  In case of any exceptions.
     */
    void removeMessage(String sFolderName, Message mMessage)
                throws EmailConnectionException;
}
