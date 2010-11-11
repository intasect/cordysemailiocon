

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
package com.cordys.coe.ac.emailio.config.message;

import com.cordys.coe.ac.emailio.EmailIOConnectorConstants;
import com.cordys.coe.ac.emailio.config.rule.IRuleContext;
import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;
import com.cordys.coe.ac.emailio.exception.TriggerEngineException;
import com.cordys.coe.ac.emailio.localization.EmailIOConfigurationExceptionMessages;
import com.cordys.coe.ac.emailio.localization.TriggerEngineExceptionMessages;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.NodeType;
import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * This class wraps the configuration of the mapping for the given trigger.
 *
 * @author  pgussow
 */
class Mapping
    implements IMapping
{
    /**
     * Holds the name of the tag 'value'.
     */
    private static final String TAG_VALUE = "value";
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(Mapping.class);
    /**
     * Holds an empty string.
     */
    private static final String EMPTY_STRING = "";
    /**
     * Holds the name of the attribute 'src'.
     */
    private static final String ATTR_SRC = "src";
    /**
     * Holds the tag name for the 'source' tag.
     */
    private static final String TAG_SOURCE = "source";
    /**
     * Holds the tag name for the 'mapping' tag.
     */
    private static final String TAG_MAPPING = "mapping";
    /**
     * Holds the operation for this mapping.
     */
    private static final String ATTR_OPERATION = "operation";
    /**
     * Holds the name of the custom mapping class.
     */
    private static final String ATTR_CLASS = "class";
    /**
     * Holds the optional custom mapping class.
     */
    private ICustomMapping m_cmCustom;
    /**
     * Holds the mapping operation.
     */
    private EMappingOperation m_moOperation = EMappingOperation.STRING_REPLACE;
    /**
     * Holds the name of the custom class mapper.
     */
    private String m_sCustomClassName;
    /**
     * Holds the value from the context that should be inserted.
     */
    private String m_sLookupValue;
    /**
     * Holds the source XPath expression.
     */
    private String m_sSource;

    /**
     * Creates a new Mapping object.
     *
     * @param   iNode  The configuration node.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    public Mapping(int iNode)
            throws EmailIOConfigurationException
    {
        XPathMetaInfo xmi = new XPathMetaInfo();
        xmi.addNamespaceBinding("ns", EmailIOConnectorConstants.NS_CONFIGURATION);

        if (iNode == 0)
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_ERROR_CREATING_MAPPING_CONFIGURATION);
        }

        m_sSource = XPathHelper.getStringValue(iNode, "./ns:source/text()", xmi, EMPTY_STRING);

        if (m_sSource.length() == 0)
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_MISSING_MAPPING_SOURCE);
        }

        int iValue = XPathHelper.selectSingleNode(iNode, "./ns:value", xmi);

        if (iValue == 0)
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_MISSING_MAPPING_VALUE);
        }

        String m_sOperation = Node.getAttribute(iValue, ATTR_OPERATION,
                                                EMappingOperation.STRING_REPLACE.name());

        m_sLookupValue = Node.getAttribute(iValue, ATTR_SRC, EMPTY_STRING);

        try
        {
            m_moOperation = EMappingOperation.valueOf(m_sOperation);
        }
        catch (Exception e)
        {
            throw new EmailIOConfigurationException(e,
                                                    EmailIOConfigurationExceptionMessages.EICE_INVALID_OPERATION_TYPE,
                                                    m_sOperation);
        }

        // The src attribute is only optional when the operation is custom.
        if ((m_sLookupValue.length() == 0) && (m_moOperation != EMappingOperation.CUSTOM))
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_MISSING_VALUE_SOURCE);
        }

        if (m_moOperation == EMappingOperation.CUSTOM)
        {
            // Now we'll parse class name as see if it's accessible.
            m_sCustomClassName = Node.getAttribute(iValue, ATTR_CLASS, EMPTY_STRING);

            if (m_sCustomClassName.length() == 0)
            {
                throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_MISSING_THE_CLASS_NAME_DEFINITION_FOR_CUSTOM_OPERATION);
            }

            Class<?> cTemp = null;

            try
            {
                cTemp = Class.forName(m_sCustomClassName);
            }
            catch (Exception e)
            {
                throw new EmailIOConfigurationException(e,
                                                        EmailIOConfigurationExceptionMessages.EICE_COULD_NOT_LOAD_CLASS_FOR_CUSTOM_OPERATION,
                                                        m_sCustomClassName);
            }

            if (!ICustomMapping.class.isAssignableFrom(cTemp))
            {
                throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_CLASS_0_DOES_NOT_IMPLEMENT_THE_INTERFACE_ICUSTOMMAPPING,
                                                        m_sCustomClassName);
            }

            try
            {
                m_cmCustom = (ICustomMapping) cTemp.newInstance();
            }
            catch (Exception e)
            {
                throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_COULD_NOT_INSTANTIATE_CLASS_CUSTOM_OPERATION,
                                                        m_sCustomClassName);
            }
        }
    }

    /**
     * This method executes the mapping. It will execute the XPath on the given context node. When
     * it's found it will delete all the exeisting children of that node. Then based on the type of
     * the value in the context it will either create a text node (String value) or append an XML
     * structure (int).
     *
     * @param   rcContext     The rule context to get the values from.
     * @param   iContextNode  The context node.
     * @param   mMessage      The parent message.
     *
     * @throws  TriggerEngineException  In case of any exceptions.
     *
     * @see     com.cordys.coe.ac.emailio.config.message.IMapping#execute(IRuleContext, int,
     *          IMessage)
     */
    public void execute(IRuleContext rcContext, int iContextNode, IMessage mMessage)
                 throws TriggerEngineException
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Executing mapping: " + toString() + "\nContext:\n" +
                      Node.writeToString(iContextNode, false));
        }

        int iNode = XPathHelper.selectSingleNode(iContextNode, getSourceXPath(),
                                                 mMessage.getXPathMetaInfo());

        if (iNode == 0)
        {
            throw new TriggerEngineException(TriggerEngineExceptionMessages.TEE_COULD_NOT_FIND_THE_XPATH_IN_THE_GIVEN_XML,
                                             getSourceXPath(),
                                             Node.writeToString(iContextNode, false));
        }

        // Remove all children.
        while (Node.getFirstChild(iNode) != 0)
        {
            Node.delete(Node.getFirstChild(iNode));
        }

        // Get the value.
        Object oValue = rcContext.getValue(getLookupValue());

        if ((m_moOperation == EMappingOperation.CUSTOM) && (m_cmCustom != null))
        {
            m_cmCustom.execute(rcContext, iContextNode, mMessage, this);
        }
        else if (oValue == null)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("The value for " + getLookupValue() +
                          " is not found. The mapping won't be executed.\nMapping details: " +
                          toString());
            }
        }
        else
        {
            if (oValue instanceof String)
            {
                String sValue = (String) oValue;
                Node.getDocument(iNode).createText(sValue, iNode);
            }
            else if (oValue instanceof Integer)
            {
                int iXML = (Integer) oValue;

                // An XML structure. The reason for doing a duplicate here is that
                // the RuleContext will do a clean up when it's finished. In that cleanup
                // it will do a Node.delete. If we don't duplicate here we could run into
                // double-delete problems.
                // There can be several operations that should be done
                switch (m_moOperation)
                {
                    case STRING_REPLACE:
                    case XML_APPEND_CHILD:
                        Node.duplicateAndAppendToChildren(iXML, iXML, iNode);
                        break;

                    case XML_APPEND_CHILD_WITH_TARGET_NS:

                        int iDuplicate = Node.duplicate(iXML);
                        try
                        {
                            replaceNS(iDuplicate, Node.getNamespaceURI(iNode));
                            Node.appendToChildren(iDuplicate, iNode);

                            // Set it to 0 to avoid deletion.
                            iDuplicate = 0;
                        }
                        finally
                        {
                            // Only needed in case of exceptions.
                            if (iDuplicate != 0)
                            {
                                Node.delete(iDuplicate);
                            }
                        }
                        break;

                    case XML_REPLACE:

                        // The children of iXML will be appended to iNode
                        int iCurrent = Node.getFirstChild(iXML);
                        while (iCurrent != 0)
                        {
                            Node.appendToChildren(Node.duplicate(iCurrent), iNode);
                            iCurrent = Node.getNextSibling(iCurrent);
                        }
                        break;

                    case XML_REPLACE_WITH_TARGET_NS:
                        // The children of iXML will be appended to iNode
                        iCurrent = Node.getFirstChild(iXML);
                        while (iCurrent != 0)
                        {
                            iDuplicate = Node.duplicate(iCurrent);

                            try
                            {
                                replaceNS(iDuplicate, Node.getNamespaceURI(iNode));
                                Node.appendToChildren(iDuplicate, iNode);

                                // To avoid deletion problems.
                                iDuplicate = 0;
                            }
                            finally
                            {
                                // Only needed in case of exceptions.
                                if (iDuplicate != 0)
                                {
                                    Node.delete(iDuplicate);
                                }
                            }
                            iCurrent = Node.getNextSibling(iCurrent);
                        }
                        break;
                }
            }
        }
    }

    /**
     * This method gets the class name for the custom mapping handler.
     *
     * @return  The class name for the custom mapping handler.
     *
     * @see     com.cordys.coe.ac.emailio.config.message.IMapping#getCustomClassName()
     */
    public String getCustomClassName()
    {
        return m_sCustomClassName;
    }

    /**
     * This method gets the lookup value for this mapping. It is the name of the variable in the
     * pattern context.
     *
     * @return  The lookup value for this mapping. It is the name of the variable in the pattern
     *          context.
     *
     * @see     com.cordys.coe.ac.emailio.config.message.IMapping#getLookupValue()
     */
    public String getLookupValue()
    {
        return m_sLookupValue;
    }

    /**
     * This method gets the mapping operation. When the source value is a non-xml value the default
     * operation is STRING_REPLACE. When the value is XML the default operation XML_APPEND_CHILD.
     *
     * @return  The mapping operation.
     *
     * @see     com.cordys.coe.ac.emailio.config.message.IMapping#getMappingOperation()
     */
    public EMappingOperation getMappingOperation()
    {
        return m_moOperation;
    }

    /**
     * This method gets the source XPath for this mapping.
     *
     * @return  The source XPath for this mapping.
     *
     * @see     com.cordys.coe.ac.emailio.config.message.IMapping#getSourceXPath()
     */
    public String getSourceXPath()
    {
        return m_sSource;
    }

    /**
     * This method sets the lookup value for this mapping. It is the name of the variable in the
     * pattern context.
     *
     * @param  sLookupValue  The lookup value for this mapping. It is the name of the variable in
     *                       the pattern context.
     */
    public void setLookupValue(String sLookupValue)
    {
        m_sLookupValue = sLookupValue;
    }

    /**
     * This method sets the source XPath for this mapping.
     *
     * @param  sSource  The source XPath for this mapping.
     */
    public void setSourceXPath(String sSource)
    {
        m_sSource = sSource;
    }

    /**
     * This method returns a string representation of the object.
     *
     * @return  A string representation of the object.
     *
     * @see     java.lang.Object#toString()
     */
    @Override public String toString()
    {
        StringBuilder sbReturn = new StringBuilder();

        sbReturn.append("Source XPath: " + getSourceXPath());
        sbReturn.append("\nLookup value: " + getLookupValue());
        sbReturn.append("\nOperation: " + getMappingOperation().name());

        return sbReturn.toString();
    }

    /**
     * This method dumps the configuration of this mapping to XML.
     *
     * @param   iParent  The parent element.
     *
     * @return  The created XML structure.
     *
     * @see     com.cordys.coe.ac.emailio.config.message.IMapping#toXML(int)
     */
    public int toXML(int iParent)
    {
        int iReturn = 0;

        iReturn = Node.createElementNS(TAG_MAPPING, EMPTY_STRING, EMPTY_STRING,
                                       EmailIOConnectorConstants.NS_CONFIGURATION, iParent);

        Node.createElementWithParentNS(TAG_SOURCE, getSourceXPath(), iReturn);

        int iValue = Node.createElementWithParentNS(TAG_VALUE, EMPTY_STRING, iReturn);
        Node.setAttribute(iValue, ATTR_SRC, getLookupValue());
        Node.setAttribute(iValue, ATTR_OPERATION, getMappingOperation().name());

        return iReturn;
    }

    /**
     * This method replaces the namespace of iNode with the new namespace. The method will check if
     * the passed on node is indeed an element node.
     *
     * @param  iNode          The node to change.
     * @param  sNewNamespace  The new namespace.
     */
    private void replaceNS(int iNode, String sNewNamespace)
    {
        // The namespace replacement should only be done on element nodes of course, not on
        // text/CDATA nodes.
        if (Node.getType(iNode) == NodeType.ELEMENT)
        {
            // Use a NOM hack to change the namespace.
            String sCurrentPrefix = Node.getPrefix(iNode);

            if ((sCurrentPrefix == null) || (sCurrentPrefix.length() == 0))
            {
                // It's the default namespace.
                Node.setAttribute(iNode, "xmlns", sNewNamespace);
            }
            else
            {
                // A defined prefix.
                Node.setNSDefinition(iNode, sCurrentPrefix, sNewNamespace);
            }
        }
        else if (LOG.isDebugEnabled())
        {
            LOG.debug("Not replacing namespace for this XML because node type is " +
                      Node.getType(iNode) + "\nXML: " + Node.writeToString(iNode, false) + "\n");
        }
    }
}
