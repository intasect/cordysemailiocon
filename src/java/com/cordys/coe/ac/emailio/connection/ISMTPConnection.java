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
