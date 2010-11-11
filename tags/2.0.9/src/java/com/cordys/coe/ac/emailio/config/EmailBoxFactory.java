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

import com.cordys.coe.ac.emailio.EmailIOConnectorConstants;
import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;
import com.cordys.coe.ac.emailio.localization.EmailIOConfigurationExceptionMessages;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * This facotry creates EmailBox objects from the XML definition.
 *
 * @author  pgussow
 */
public class EmailBoxFactory
{
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(EmailBoxFactory.class);

    /**
     * This method creates an emailbox based on the given configuration. The storage provider will
     * NOT be initialized.
     *
     * @param   iConfiguration  The configuration node.
     *
     * @return  The parsed Emailbox configuration.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    public static IEmailBox createEmailBox(int iConfiguration)
                                    throws EmailIOConfigurationException
    {
        return createEmailBox(iConfiguration, 0,
                              "cn=Dummy Processor,cn=Dummy Node,cn=soap nodes,o=system,cn=cordys,o=domain.com",
                              false, true);
    }

    /**
     * This method creates an emailbox based on the given configuration. The storage provider will
     * NOT be initialized.
     *
     * @param   iConfiguration  The configuration XML.
     * @param   bValidate       Whether or not the configuration should be validated.
     *
     * @return  The parsed Emailbox configuration.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    public static IEmailBox createEmailBox(int iConfiguration, boolean bValidate)
                                    throws EmailIOConfigurationException
    {
        return createEmailBox(iConfiguration, 0,
                              "cn=Dummy Processor,cn=Dummy Node,cn=soap nodes,o=system,cn=cordys,o=domain.com",
                              false, bValidate);
    }

    /**
     * This method creates the object based on the XML definition. The storage provider will be
     * initialized.
     *
     * @param   iNode             The configuration XML.
     * @param   iGlobalStorage    Holds the configuration of the global storage.
     * @param   sSoapProcessorDN  The DN of the SOAP processor in which the storage provider is
     *                            running.
     *
     * @return  The IEmailBox to use.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    public static IEmailBox createEmailBox(int iNode, int iGlobalStorage,
                                           String sSoapProcessorDN)
                                    throws EmailIOConfigurationException
    {
        return createEmailBox(iNode, iGlobalStorage, sSoapProcessorDN, true, true);
    }

    /**
     * This method creates the object based on the XML definition.
     *
     * @param   iNode                       The configuration XML.
     * @param   iGlobalStorage              Holds the configuration of the global storage.
     * @param   sSoapProcessorDN            The DN of the SOAP processor in which the storage
     *                                      provider is running.
     * @param   bInitializeStorageProvider  Indicates whether or not the storage provider should be
     *                                      created.
     * @param   bValidate                   Whether or not the configuration should be validated.
     *
     * @return  The IEmailBox to use.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    public static IEmailBox createEmailBox(int iNode, int iGlobalStorage, String sSoapProcessorDN,
                                           boolean bInitializeStorageProvider,
                                           boolean bValidate)
                                    throws EmailIOConfigurationException
    {
        IEmailBox ebReturn = null;

        XPathMetaInfo xmi = new XPathMetaInfo();
        xmi.addNamespaceBinding("ns", EmailIOConnectorConstants.NS_CONFIGURATION);

        String sType = XPathHelper.getStringValue(iNode, "./ns:type/text()", xmi, "POP3");

        if ((sType == null) || (sType.length() == 0))
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_MISSING_EMAIL_BOX_TYPE);
        }

        EEmailBoxType ebt = null;

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Creating mailbox for type " + sType);
        }

        try
        {
            ebt = EEmailBoxType.valueOf(sType);
        }
        catch (IllegalArgumentException iae)
        {
            throw new EmailIOConfigurationException(iae,
                                                    EmailIOConfigurationExceptionMessages.EICE_MISSING_EMAIL_BOX_TYPE);
        }

        switch (ebt)
        {
            case IMAP:
                ebReturn = new IMAPEmailBox(iNode, iGlobalStorage, sSoapProcessorDN,
                                            bInitializeStorageProvider);
                break;

            case POP3:
                ebReturn = new POP3EmailBox(iNode, iGlobalStorage, sSoapProcessorDN,
                                            bInitializeStorageProvider);
                break;
        }

        if (bValidate)
        {
            ebReturn.validate();
        }

        return ebReturn;
    }
}
