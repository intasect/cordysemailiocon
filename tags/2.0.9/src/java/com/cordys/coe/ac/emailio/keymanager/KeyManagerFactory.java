

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
package com.cordys.coe.ac.emailio.keymanager;

import com.cordys.coe.ac.emailio.EmailIOConnectorConstants;
import com.cordys.coe.ac.emailio.config.IEmailIOConfiguration;
import com.cordys.coe.ac.emailio.exception.KeyManagerException;
import com.cordys.coe.ac.emailio.localization.KeyManagerExceptionMessages;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * This factory creates the key managers that are configured.
 *
 * @author  pgussow
 */
public class KeyManagerFactory
{
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(KeyManagerFactory.class);

    /**
     * This method will create the proper key manager based on the given configuration XML.
     *
     * @param   ecConfiguration  The connector configuration.
     * @param   iConfiguration   The configuration XML.
     *
     * @return  The key manager base don the given configuration.
     *
     * @throws  KeyManagerException  In case of any exceptions.
     */
    public static IKeyManager createKeyManager(IEmailIOConfiguration ecConfiguration,
                                               int iConfiguration)
                                        throws KeyManagerException
    {
        IKeyManager kmReturn = null;

        XPathMetaInfo xmi = new XPathMetaInfo();
        xmi.addNamespaceBinding("ns", EmailIOConnectorConstants.NS_CONFIGURATION);

        String sClass = "";
        int iParameters = 0;

        if (iConfiguration != 0)
        {
            sClass = XPathHelper.getStringValue(iConfiguration, "ns:class", xmi, "");

            iParameters = XPathHelper.selectSingleNode(iConfiguration, "ns:parameters", xmi);

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Creating key manager with class " + sClass + "\nParameters:\n" +
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
            throw new KeyManagerException(e, KeyManagerExceptionMessages.KME_COULD_NOT_LOAD_CLASS,
                                          sClass);
        }

        if (!IKeyManager.class.isAssignableFrom(cClass))
        {
            throw new KeyManagerException(KeyManagerExceptionMessages.KME_THE_CLASS_DOES_NOT_IMPLEMENT_INTERFACE,
                                          sClass, IKeyManager.class.getName());
        }

        // Instantiate the provider
        try
        {
            kmReturn = (IKeyManager) cClass.newInstance();
        }
        catch (Exception e)
        {
            throw new KeyManagerException(e,
                                          KeyManagerExceptionMessages.KME_ERROR_INSTANTIATING_CLASS,
                                          sClass);
        }

        // Get the name for this manager
        String sName = XPathHelper.getStringValue(iConfiguration, "ns:name", xmi, "");
        kmReturn.setName(sName);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Initializing key manager");
        }

        // For now the validator will not be passed on.
        kmReturn.initialize(ecConfiguration, iParameters, xmi, null);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("KeyManager " + kmReturn.getName() + "(" + sClass +
                      ") created and initialized.");
        }

        return kmReturn;
    }
}
