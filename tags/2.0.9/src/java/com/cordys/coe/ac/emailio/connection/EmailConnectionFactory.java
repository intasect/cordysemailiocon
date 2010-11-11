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

import com.cordys.coe.ac.emailio.config.IEmailBox;
import com.cordys.coe.ac.emailio.config.IIMAPEmailBox;
import com.cordys.coe.ac.emailio.config.POP3EmailBox;
import com.cordys.coe.ac.emailio.exception.EmailConnectionException;
import com.cordys.coe.ac.emailio.localization.EmailConnectionExceptionMessages;

/**
 * This factory creates connections to email boxes based on the configuration.
 *
 * @author  pgussow
 */
public class EmailConnectionFactory
{
    /**
     * This method creates a connection to the email box. It will take care of the different email
     * box types.
     *
     * @param   ebBox  The definition of the email box.
     *
     * @return  The connection to use.
     *
     * @throws  EmailConnectionException  In case of any exceptions.
     */
    public static IEmailConnection createConnection(IEmailBox ebBox)
                                             throws EmailConnectionException
    {
        IEmailConnection ecReturn = null;

        switch (ebBox.getType())
        {
            case IMAP:
                ecReturn = createIMAPConnection(ebBox);
                break;

            case POP3:
                ecReturn = createPOP3Connection(ebBox);
                break;
        }

        return ecReturn;
    }

    /**
     * This method creates a connection to an IMAP email box.
     *
     * @param   ebBox  The email box to connect to.
     *
     * @return  The email box connection to use.
     *
     * @throws  EmailConnectionException  In case of any exceptions.
     */
    private static IEmailConnection createIMAPConnection(IEmailBox ebBox)
                                                  throws EmailConnectionException
    {
        IEmailConnection ecReturn = null;

        if (ebBox instanceof IIMAPEmailBox)
        {
            IIMAPEmailBox ieb = (IIMAPEmailBox) ebBox;
            ecReturn = new IMAPConnection(ieb);
        }
        else
        {
            throw new EmailConnectionException(EmailConnectionExceptionMessages.ECE_INVALID_CONFIGURATION_EMAIL_BOX_MUST_BE_OF_TYPE_IMAP);
        }
        return ecReturn;
    }

    /**
     * This method creates a connection to an POP3 email box.
     *
     * @param   ebBox  The email box to connect to.
     *
     * @return  The email box connection to use.
     *
     * @throws  EmailConnectionException  In case of any exceptions.
     */
    private static IEmailConnection createPOP3Connection(IEmailBox ebBox)
                                                  throws EmailConnectionException
    {
        IEmailConnection ecReturn = null;

        if (ebBox instanceof POP3EmailBox)
        {
            POP3EmailBox ieb = (POP3EmailBox) ebBox;
            ecReturn = new POP3Connection(ieb);
        }
        else
        {
            throw new EmailConnectionException(EmailConnectionExceptionMessages.ECE_INVALID_CONFIGURATION_EMAIL_BOX_MUST_BE_OF_TYPE_POP3);
        }
        return ecReturn;
    }
}
