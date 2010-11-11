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
 package com.cordys.coe.ac.emailio.keymanager;

import com.cordys.coe.ac.emailio.EmailIOConnectorConstants;
import com.cordys.coe.ac.emailio.config.IEmailIOConfiguration;
import com.cordys.coe.ac.emailio.exception.KeyManagerException;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class can be used as a base for the key managers. It will handle the parsing of the
 * parameters.
 *
 * @author  pgussow
 */
public abstract class AbstractKeyManager
    implements IKeyManager
{
    /**
     * Holds an empty string.
     */
    private static final String EMPTY_STRING = "";
    /**
     * Holds the logger to use. We'll use the logger for the implementation class instead of the
     * current class.
     */
    private final CordysLogger LOG = CordysLogger.getCordysLogger(this.getClass());
    /**
     * Holds the validator that can be used to validate the certificates.
     */
    private ICertificateValidator m_cvValidator = null;
    /**
     * Holds the configuration of the current processor.
     */
    private IEmailIOConfiguration m_ecConfiguration = null;
    /**
     * Holds the parameters and their value.
     */
    private Map<String, Object> m_mParameters = new LinkedHashMap<String, Object>();
    /**
     * Holds the name of this key manager.
     */
    private String m_sName;

    /**
     * Creates a new AbstractStorageProvider object.
     */
    public AbstractKeyManager()
    {
    }

    /**
     * This method gets the certificate validator.
     *
     * @return  The certificate validator.
     */
    public ICertificateValidator getCertificateValidator()
    {
        return m_cvValidator;
    }

    /**
     * This method gets the configuration of the Email IO Connector.
     *
     * @return  The configuration of the Email IO Connector.
     */
    public IEmailIOConfiguration getConfiguration()
    {
        return m_ecConfiguration;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.keymanager.IKeyManager#getName()
     */
    @Override public String getName()
    {
        return m_sName;
    }

    /**
     * Returns a map of the parameters that were present in the configuration block.
     *
     * <p>This method is public primarily to make testing easier. It should not be called by
     * external code under normal conditions.</p>
     *
     * @return  The map of the parameters that were present in the configuration block.
     */
    public Map<String, Object> getParameters()
    {
        return new LinkedHashMap<String, Object>(m_mParameters);
    }

    /**
     * This method is called to initialize the the key manager.
     *
     * <p>This method extracts the parameters from the XML configuration and puts them in a map in
     * order to make them more easily available for derived classes.</p>
     *
     * <p>After the parameters have been parsed, the template method {@link
     * #postInit(IEmailIOConfiguration, int, XPathMetaInfo, ICertificateValidator)} is called.
     * Derived classes should override that one to perform initialization tasks.</p>
     *
     * @param   ecConfiguration     The configuration of the processor
     * @param   iConfigurationNode  The XML containing the configuration of the key manager.
     * @param   xmi                 The XPath meta info to use. The prefix ns should be mapped to
     *                              the proper namespace.
     * @param   cvValidator         The certificate validator to use.
     *
     * @throws  KeyManagerException  In case of any exceptions.
     *
     * @see     com.cordys.coe.ac.emailio.keymanager.IKeyManager#initialize(IEmailIOConfiguration,int,
     *          XPathMetaInfo, ICertificateValidator)
     */
    @Override public void initialize(IEmailIOConfiguration ecConfiguration, int iConfigurationNode,
                                     XPathMetaInfo xmi, ICertificateValidator cvValidator)
                              throws KeyManagerException
    {
        m_ecConfiguration = ecConfiguration;

        if (iConfigurationNode > 0)
        {
            int[] aiParameters = XPathHelper.selectNodes(iConfigurationNode,
                                                         "./ns:" + ELEMENT_PARAMETER, xmi);

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Found " + aiParameters.length + " parameters");
            }

            for (int iParameter : aiParameters)
            {
                String sName = Node.getAttribute(iParameter, ATTRIBUTE_NAME);
                String sType = Node.getAttribute(iParameter, ATTRIBUTE_TYPE, "string");
                Object oValue = null;

                if ("string".equals(sType))
                {
                    oValue = Node.getDataWithDefault(iParameter, "");
                }
                else if ("xml".equals(sType))
                {
                    oValue = new Integer(Node.getFirstElement(iParameter));
                }

                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Name: " + sName + ", type: " + sType + ", Value: " + oValue);
                }

                if ((sName != null) && (sName.length() > 0))
                {
                    m_mParameters.put(sName, oValue);
                }
            }
        }
        else if (LOG.isDebugEnabled())
        {
            LOG.debug("No configuration found");
        }

        postInit(ecConfiguration, iConfigurationNode, xmi, cvValidator);
    }

    /**
     * @see  com.cordys.coe.ac.emailio.keymanager.IKeyManager#setName(java.lang.String)
     */
    @Override public void setName(String sName)
    {
        m_sName = sName;
    }

    /**
     * This method dumps the configuration of this storage provider to XML.
     *
     * @param   iParent  The parent element.
     *
     * @return  The created XML structure.
     *
     * @see     com.cordys.coe.ac.emailio.storage.IEmailStorageProvider#toXML(int)
     */
    @Override public int toXML(int iParent)
    {
        int iReturn = Node.createElementNS("keymanager", EMPTY_STRING, EMPTY_STRING,
                                           EmailIOConnectorConstants.NS_CONFIGURATION, iParent);

        Node.createElementWithParentNS("class", this.getClass().getName(), iReturn);
        Node.createElementWithParentNS("name", getName(), iReturn);

        int iParameters = Node.createElementWithParentNS("parameters", null, iReturn);

        for (Map.Entry<String, Object> eParameter : getParameters().entrySet())
        {
            int iParameter = Node.createElementWithParentNS(ELEMENT_PARAMETER, null, iParameters);
            Object oValue = eParameter.getValue();
            String sType = "string";

            if (oValue instanceof String)
            {
                Node.getDocument(iParameter).createText((String) oValue, iParameter);
            }
            else if (oValue instanceof Integer)
            {
                sType = "xml";
                Node.duplicateAndAppendToChildren((Integer) oValue, (Integer) oValue, iParameter);
            }
            Node.setAttribute(iParameter, ATTRIBUTE_TYPE, sType);
            Node.setAttribute(iParameter, ATTRIBUTE_NAME, eParameter.getKey());
        }

        return iReturn;
    }

    /**
     * This method gets the boolean value for a parameter.
     *
     * @param   sName  The name of the parameter.
     *
     * @return  The boolean value for a parameter.
     */
    protected boolean getBooleanParameter(String sName)
    {
        String sTemp = (String) m_mParameters.get(sName);

        return "true".equalsIgnoreCase(sTemp);
    }

    /**
     * This method gets the string value for a parameter.
     *
     * @param   sName  The name of the parameter.
     *
     * @return  The string value for a parameter.
     */
    protected String getStringParameter(String sName)
    {
        return (String) m_mParameters.get(sName);
    }

    /**
     * This method gets the value of the XML parameter.
     *
     * @param   sName  The name of the parameter.
     *
     * @return  The value of the XML parameter.
     */
    protected int getXMLParameter(String sName)
    {
        int iReturn = 0;

        Object oTemp = m_mParameters.get(sName);

        if ((oTemp != null) && (oTemp instanceof Integer))
        {
            iReturn = (Integer) oTemp;
        }

        return iReturn;
    }

    /**
     * Adapter method that is called after the parameters are parsed.
     *
     * @param   ecConfiguration     The Email IO Connector configuration
     * @param   iConfigurationNode  The XML containing the configuration of the key manager.
     * @param   xmi                 The XPath meta info to use. The prefix ns should be mapped to
     *                              the proper namespace.
     * @param   cvValidator         The certificate validator to use.
     *
     * @throws  KeyManagerException  In case of any exceptions.
     */
    protected void postInit(IEmailIOConfiguration ecConfiguration, int iConfigurationNode,
                            XPathMetaInfo xmi, ICertificateValidator cvValidator)
                     throws KeyManagerException
    {
    }
}
