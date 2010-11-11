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
