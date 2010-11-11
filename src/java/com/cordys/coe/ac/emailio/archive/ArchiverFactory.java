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
 package com.cordys.coe.ac.emailio.archive;

import com.cordys.coe.ac.emailio.EmailIOConnectorConstants;
import com.cordys.coe.ac.emailio.config.IEmailIOConfiguration;
import com.cordys.coe.ac.emailio.exception.ArchiverException;
import com.cordys.coe.ac.emailio.localization.ArchiverExceptionMessages;
import com.cordys.coe.ac.emailio.storage.DefaultMemoryStore;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.management.IManagedComponent;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * This factory creates an instance of the archiver that should be used to archive the data.
 *
 * @author  pgussow
 */
public class ArchiverFactory
{
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(ArchiverFactory.class);
    /**
     * Holds the singleton instance.
     */
    private static ArchiverFactory s_espfSingleton = new ArchiverFactory();
    /**
     * Holds the namespace prefix mappings to use.
     */
    private XPathMetaInfo m_xmi;

    /**
     * Creates a new ArchiverFactory object.
     */
    private ArchiverFactory()
    {
        m_xmi = new XPathMetaInfo();
        m_xmi.addNamespaceBinding("ns", EmailIOConnectorConstants.NS_DYNAMIC);
    }

    /**
     * This method creates the archiver implementation that can be used to archive messages.
     *
     * @param   iArchiverDefinition  The definition for the archiver.
     * @param   iecConfig            The configuration of the connector.
     * @param   sOrganization        The organization the archiver runs in.
     * @param   mcParent             The managed component parent.
     *
     * @return  The created archiver.
     *
     * @throws  ArchiverException  In case of any exceptions.
     */
    public static IArchiver createArchiver(int iArchiverDefinition, IEmailIOConfiguration iecConfig,
                                           String sOrganization, IManagedComponent mcParent)
                                    throws ArchiverException
    {
        return s_espfSingleton.internalCreateArchiver(iArchiverDefinition, iecConfig, sOrganization,
                                                      mcParent);
    }

    /**
     * This method creates the archiver implementation that can be used to archive messages.
     *
     * @param   iArchiverDefinition  The definition for the archiver.
     * @param   iecConfig            The configuration of the connector.
     * @param   sOrganization        The organization the archiver runs in.
     * @param   mcParent             The managed component parent.
     *
     * @return  The created archiver.
     *
     * @throws  ArchiverException  In case of any exceptions.
     */
    public IArchiver internalCreateArchiver(int iArchiverDefinition,
                                            IEmailIOConfiguration iecConfig, String sOrganization,
                                            IManagedComponent mcParent)
                                     throws ArchiverException
    {
        IArchiver aReturn = null;

        // This implicitly means that if no storage configuration is defined the
        // DefaultMemoryStore will be used
        String sClass = XPathHelper.getStringValue(iArchiverDefinition, "ns:class", m_xmi,
                                                   DefaultMemoryStore.class.getName());
        int iParameters = XPathHelper.selectSingleNode(iArchiverDefinition, "ns:parameters", m_xmi);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Creating archiver with class " + sClass + "\nParameters:\n" +
                      Node.writeToString(iParameters, false));
        }

        // Validate the class is proper.
        Class<?> cClass = null;

        try
        {
            cClass = Class.forName(sClass);
        }
        catch (Exception e)
        {
            throw new ArchiverException(e, ArchiverExceptionMessages.ARE_COULD_NOT_LOAD_CLASS_0,
                                        sClass);
        }

        if (!IArchiver.class.isAssignableFrom(cClass))
        {
            throw new ArchiverException(ArchiverExceptionMessages.ARE_THE_CLASS_0_DOES_NOT_IMPLEMENT_INTERFACE,
                                        sClass, IArchiver.class.getName());
        }

        // Instantiate the provider
        try
        {
            aReturn = (IArchiver) cClass.newInstance();
        }
        catch (Exception e)
        {
            throw new ArchiverException(e, ArchiverExceptionMessages.ARE_ERROR_INSTANTIATING_CLASS,
                                        sClass);
        }

        // Initialize the provider
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Initializing storage archiver");
        }

        aReturn.initialize(iParameters, m_xmi, iecConfig, sOrganization, mcParent);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Archiver " + sClass + " created and initialized.");
        }

        return aReturn;
    }
}
