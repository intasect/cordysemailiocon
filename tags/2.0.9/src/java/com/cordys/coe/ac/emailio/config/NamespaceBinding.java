

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
package com.cordys.coe.ac.emailio.config;

import com.cordys.coe.ac.emailio.EmailIOConnectorConstants;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class is used to parse the namespace prefix mappings.
 *
 * @author  pgussow
 */
public class NamespaceBinding
{
    /**
     * Holds the name of the tag namespacemapping.
     */
    private static final String TAG_NAMESPACE_MAPPING = "namespacemapping";
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(NamespaceBinding.class);
    /**
     * Holds the name of the tag namespacemappings.
     */
    private static final String TAG_NAMESPACE_MAPPINGS = "namespacemappings";
    /**
     * Holds the name of the tag prefix.
     */
    private static final String TAG_PREFIX = "prefix";
    /**
     * Holds the name of the tag namespace.
     */
    private static final String TAG_NAMESPACE = "namespace";
    /**
     * Holds an empty string.
     */
    private static final String EMPTY_STRING = "";
    /**
     * Holds teh defined namespaces and their prefixes.
     */
    private Map<String, String> m_mBindings = new LinkedHashMap<String, String>();
    /**
     * Holds the reusable version of the meta info.
     */
    private XPathMetaInfo m_xmi;

    /**
     * Creates a new NamespaceBinding object.
     *
     * @param  iNode  The configuration node.
     */
    public NamespaceBinding(int iNode)
    {
        XPathMetaInfo xmi = new XPathMetaInfo();
        xmi.addNamespaceBinding("ns", EmailIOConnectorConstants.NS_CONFIGURATION);

        int[] anMappings = XPathHelper.selectNodes(iNode, "./ns:" + TAG_NAMESPACE_MAPPING, xmi);

        if (anMappings.length == 0)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("No namespace mappings found during binding init: " +
                          Node.write(iNode, false));
            }
        }

        for (int iMapping : anMappings)
        {
            String sPrefix = XPathHelper.getStringValue(iMapping, "./ns:" + TAG_PREFIX + "/text()",
                                                        xmi, "");
            String sNamespace = XPathHelper.getStringValue(iMapping,
                                                           "./ns:" + TAG_NAMESPACE + "/text()", xmi,
                                                           "");

            if ((sPrefix.length() > 0) && (sNamespace.length() > 0))
            {
                m_mBindings.put(sPrefix, sNamespace);
            }
        }

        m_xmi = createXPathMetaInfo();
    }

    /**
     * This method creates a new XPathMetaInfo object for the current mapping.
     *
     * @return  The newly created XPathmetaInfo.
     */
    public XPathMetaInfo createXPathMetaInfo()
    {
        XPathMetaInfo xmiReturn = new XPathMetaInfo();

        for (String sPrefix : m_mBindings.keySet())
        {
            String sNamespace = m_mBindings.get(sPrefix);
            xmiReturn.addNamespaceBinding(sPrefix, sNamespace);
        }

        return xmiReturn;
    }

    /**
     * This method gets the cached version of the XPath mappings.
     *
     * @return  The cached version of the XPath mappings.
     */
    public XPathMetaInfo getXPathMetaInfo()
    {
        return m_xmi;
    }

    /**
     * This method writes the current configuration to XML.
     *
     * @param  iParent  The parent XML.
     */
    public void toXML(int iParent)
    {
        int iMappings = Node.createElementNS(TAG_NAMESPACE_MAPPINGS, EMPTY_STRING, EMPTY_STRING,
                                             EmailIOConnectorConstants.NS_CONFIGURATION, iParent);

        for (String sPrefix : m_mBindings.keySet())
        {
            String sNamespace = m_mBindings.get(sPrefix);
            int iMapping = Node.createElementWithParentNS(TAG_NAMESPACE_MAPPING, "", iMappings);
            Node.createElementWithParentNS(TAG_PREFIX, sPrefix, iMapping);
            Node.createElementWithParentNS(TAG_NAMESPACE, sNamespace, iMapping);
        }
    }
}
