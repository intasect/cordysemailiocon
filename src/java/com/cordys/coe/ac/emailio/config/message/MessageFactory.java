

 /**
 * Copyright 2007 Cordys R&D B.V. 
 * 
 * This file is part of the Cordys Email IO Connector. 
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
