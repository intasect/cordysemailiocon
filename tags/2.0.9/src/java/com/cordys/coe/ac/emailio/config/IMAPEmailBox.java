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
import com.cordys.coe.ac.emailio.localization.EmailIOConfigurationExceptionMessages;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.xml.nom.Node;

import java.util.ArrayList;

/**
 * This class wraps the configuration of an IMAP email box.
 *
 * @author  pgussow
 */
class IMAPEmailBox extends EmailBox
    implements IIMAPEmailBox
{
    /**
     * Default IMAP port.
     */
    private static final int DEFAULT_PORT = 143;
    /**
     * Holds the list folders that should be monitored.
     */
    private ArrayList<String> m_alFolders = new ArrayList<String>();

    /**
     * Creates a new IMAPEmailBox object.
     *
     * @param   iNode                       The configuration XML.
     * @param   iGlobalStorage              The global storage configuration to use.
     * @param   sSoapProcessorDN            The DN of the SOAP processor in which the storage
     *                                      provider is running.
     * @param   bInitializeStorageProvider  Indicates whether or not the storage provider should be
     *                                      created.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    public IMAPEmailBox(int iNode, int iGlobalStorage, String sSoapProcessorDN,
                        boolean bInitializeStorageProvider)
                 throws EmailIOConfigurationException
    {
        super(iNode, iGlobalStorage, sSoapProcessorDN, bInitializeStorageProvider);

        // if no port was set, we use the default.
        if (getPort() == 0)
        {
            setPort(DEFAULT_PORT);
        }

        // Get the folders to watch
        int[] aiFolders = XPathHelper.selectNodes(iNode, "./ns:folders/ns:folder", getXMI());

        for (int iCount = 0; iCount < aiFolders.length; iCount++)
        {
            String sName = Node.getDataWithDefault(aiFolders[iCount], "");

            if (sName.length() > 0)
            {
                m_alFolders.add(sName);
            }
        }

        if (m_alFolders.size() == 0)
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_FOR_IMAP_A_MINIMUM_OF_1_FOLDER_NEEDS_TO_BE_DEFINED);
        }
    }

    /**
     * This method gets the folder in which to poll for email messages.
     *
     * @return  The folder in which to poll for email messages.
     *
     * @see     com.cordys.coe.ac.emailio.config.IIMAPEmailBox#getEmailFolders()
     */
    @Override public String[] getEmailFolders()
    {
        return m_alFolders.toArray(new String[0]);
    }

    /**
     * This method dumps the configuration of this email box to XML.
     *
     * @param   iParent  The parent element.
     *
     * @return  The created XML structure.
     *
     * @see     com.cordys.coe.ac.emailio.config.EmailBox#toXML(int)
     */
    @Override public int toXML(int iParent)
    {
        int iReturn = super.toXML(iParent);

        return iReturn;
    }
}
