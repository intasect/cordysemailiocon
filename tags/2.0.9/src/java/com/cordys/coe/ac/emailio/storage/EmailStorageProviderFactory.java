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
 package com.cordys.coe.ac.emailio.storage;

import com.cordys.coe.ac.emailio.EmailIOConnectorConstants;
import com.cordys.coe.ac.emailio.config.IEmailBox;
import com.cordys.coe.ac.emailio.exception.StorageProviderException;
import com.cordys.coe.ac.emailio.localization.StorageProviderExceptionMessages;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.management.IManagedComponent;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * This factory creates the storage provider which can be used for storing actual emails, but also
 * storing dynamic triggers.
 *
 * @author  pgussow
 */
public class EmailStorageProviderFactory
{
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(EmailStorageProviderFactory.class);
    /**
     * Holds the singleton instance.
     */
    private static EmailStorageProviderFactory s_espfSingleton = new EmailStorageProviderFactory();
    /**
     * Holds the XPath meta info to use.
     */
    private XPathMetaInfo m_xmi;

    /**
     * Creates a new EmailStorageProviderFactory object.
     */
    private EmailStorageProviderFactory()
    {
        m_xmi = new XPathMetaInfo();
        m_xmi.addNamespaceBinding("ns", EmailIOConnectorConstants.NS_CONFIGURATION);
    }

    /**
     * This method will create the proper storage implementation for this connector. The provider
     * will be initialized.
     *
     * @param   ebEmailBox                  The email box for this storage provider.
     * @param   sSoapProcessorDN            The DN of the SOAP processor in which the storage
     *                                      provider is running.
     * @param   bInitializeStorageProvider  Whether or not the Storage provider should be
     *                                      initialized automatically
     * @param   mcParent                    The parent managed component.
     *
     * @return  The storage provider to use.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    public static IEmailStorageProvider createStorageProvider(IEmailBox ebEmailBox,
                                                              String sSoapProcessorDN,
                                                              boolean bInitializeStorageProvider,
                                                              IManagedComponent mcParent)
                                                       throws StorageProviderException
    {
        return s_espfSingleton.internalCreateStorageProvider(ebEmailBox, sSoapProcessorDN,
                                                             bInitializeStorageProvider, mcParent);
    }

    /**
     * This method will create the proper storage implementation for this connector. The provider
     * will be initialized.
     *
     * @param   ebEmailBox                  The email box for this storage provider.
     * @param   sSoapProcessorDN            The DN of the SOAP processor in which the storage
     *                                      provider is running.
     * @param   bInitializeStorageProvider  Whether or not the Storage provider should be
     *                                      initialized automatically
     * @param   mcParent                    The parent managed component.
     *
     * @return  The storage provider to use.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    private IEmailStorageProvider internalCreateStorageProvider(IEmailBox ebEmailBox,
                                                                String sSoapProcessorDN,
                                                                boolean bInitializeStorageProvider,
                                                                IManagedComponent mcParent)
                                                         throws StorageProviderException
    {
        IEmailStorageProvider espReturn = null;
        int iConfiguration = ebEmailBox.getStorageProviderConfiguration();

        // This implicitly means that if no storage configuration is defined the
        // DefaultMemoryStore will be used
        String sClass = DefaultMemoryStore.class.getName();
        int iParameters = 0;

        if (iConfiguration != 0)
        {
            sClass = XPathHelper.getStringValue(iConfiguration, "ns:class", m_xmi,
                                                DefaultMemoryStore.class.getName());

            iParameters = XPathHelper.selectSingleNode(iConfiguration, "ns:parameters", m_xmi);

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Creating storage provider with class " + sClass + "\nParameters:\n" +
                          Node.writeToString(iParameters, false));
            }
        }

        // Validate the class is proper.
        Class<?> cClass = null;

        try
        {
            cClass = Class.forName(sClass);
        }
        catch (Exception e)
        {
            throw new StorageProviderException(e,
                                               StorageProviderExceptionMessages.SPE_COULD_NOT_LOAD_CLASS,
                                               sClass);
        }

        if (!IEmailStorageProvider.class.isAssignableFrom(cClass))
        {
            throw new StorageProviderException(StorageProviderExceptionMessages.SPE_THE_CLASS_DOES_NOT_IMPLEMENT_INTERFACE,
                                               sClass, IEmailStorageProvider.class.getName());
        }

        // Instantiate the provider
        try
        {
            espReturn = (IEmailStorageProvider) cClass.newInstance();
        }
        catch (Exception e)
        {
            throw new StorageProviderException(e,
                                               StorageProviderExceptionMessages.ERROR_INSTANTIATING_CLASS_0,
                                               sClass);
        }

        // Initialize the provider
        if (bInitializeStorageProvider)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Initializing storage provider");
            }

            espReturn.initialize(ebEmailBox, iParameters, m_xmi, sSoapProcessorDN, mcParent);
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("StorageProvider " + sClass + " created and initialized.");
        }

        return espReturn;
    }
}
