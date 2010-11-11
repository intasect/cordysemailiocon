package com.cordys.coe.ac.emailio.config.message;

import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;

/**
 * This method creates the message object that holds the configuration of the SOAP message that has
 * to be sent when an email message is received.
 *
 * @author  pgussow
 */
public class MessageFactory
{
    /**
     * This method creates a new message object that holds the configuration of the trigger SOAP
     * message.
     *
     * @param   iNode  The configuration node.
     *
     * @return  The message object to use.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    public static IMessage createMessage(int iNode)
                                  throws EmailIOConfigurationException
    {
        return new Message(iNode);
    }
}
