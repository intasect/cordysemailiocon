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
 package com.cordys.coe.ac.emailio.config;

import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;

import java.util.ArrayList;

/**
 * This class wraps around the configuration of a POP3 email box.
 *
 * @author  pgussow
 */
public class POP3EmailBox extends EmailBox
{
    /**
     * Holds the static name of the POP3 folder.
     */
    private static final String[] EMAIL_FOLDERS = new String[] { "INBOX" };
    /**
     * Default POP3 port.
     */
    private static final int DEFAULT_PORT = 110;
    /**
     * Holds the list folders that should be monitored.
     */
    private ArrayList<String> m_alFolders = new ArrayList<String>();

    /**
     * Creates a new POP3EmailBox object.
     *
     * @param   iNode                       The configuration XML.
     * @param   iGlobalStorage              The global storage definition.
     * @param   sSoapProcessorDN            The DN of the SOAP processor in which the storage
     *                                      provider is running.*
     * @param   bInitializeStorageProvider  Indicates whether or not the storage provider should be
     *                                      created.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    public POP3EmailBox(int iNode, int iGlobalStorage, String sSoapProcessorDN,
                        boolean bInitializeStorageProvider)
                 throws EmailIOConfigurationException
    {
        super(iNode, iGlobalStorage, sSoapProcessorDN, bInitializeStorageProvider);

        // if no port was set, we use the default.
        if (getPort() == 0)
        {
            setPort(DEFAULT_PORT);
        }

        // POP3 only has an INBOX folder.
        m_alFolders.add(EMAIL_FOLDERS[0]);
    }

    /**
     * This method gets the folders in which to poll for email messages.
     *
     * @return  The folders in which to poll for email messages.
     *
     * @see     com.cordys.coe.ac.emailio.config.IEmailBox#getEmailFolders()
     */
    @Override public String[] getEmailFolders()
    {
        if (m_alFolders.size() == 0)
        {
            return EMAIL_FOLDERS;
        }
        else
        {
            return m_alFolders.toArray(new String[0]);
        }
    }
}
