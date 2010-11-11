

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
package com.cordys.coe.ac.emailio.config.pattern;

import com.cordys.coe.ac.emailio.config.rule.IRule;
import com.cordys.coe.ac.emailio.config.rule.IRuleContext;
import com.cordys.coe.ac.emailio.config.token.EStoreSource;
import com.cordys.coe.ac.emailio.config.token.IStorageToken;
import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;
import com.cordys.coe.ac.emailio.exception.TriggerEngineException;
import com.cordys.coe.ac.emailio.localization.TriggerEngineExceptionMessages;
import com.cordys.coe.ac.emailio.util.NOMUtil;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.NodeType;
import com.eibus.xml.xpath.NodeSet;
import com.eibus.xml.xpath.ResultNode;
import com.eibus.xml.xpath.XPath;
import com.eibus.xml.xpath.XPathResult;

/**
 * This class implements an XPath based pattern.
 *
 * @author  pgussow
 */
class XPathPattern extends BasePattern
{
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(XPathPattern.class);
    /**
     * This document holds the XML nodes this pattern tries to execute.
     */
    private static Document s_dDoc = new Document();

    /**
     * Constructor.
     *
     * @param   iNode  The configuration node.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    public XPathPattern(int iNode)
                 throws EmailIOConfigurationException
    {
        super(iNode);
    }

    /**
     * This method evaluates an XPath expression. It assumes that the main content is an int which
     * referes to a NOM XML node.
     *
     * @param   pcContext  The context for the pattern.
     * @param   oValue     The value to operate on.
     * @param   rRule      The parent rule.
     *
     * @return  true if the passed on value matches the pattern. Otherwise false.
     *
     * @throws  TriggerEngineException  In case of any exception.
     *
     * @see     com.cordys.coe.ac.emailio.config.pattern.IPattern#evaluate(IRuleContext, Object,
     *          IRule)
     */
    @Override public boolean evaluate(IRuleContext pcContext, Object oValue, IRule rRule)
                               throws TriggerEngineException
    {
        boolean bReturn = false;

        if (oValue == null)
        {
            throw new TriggerEngineException(TriggerEngineExceptionMessages.TEE_THE_VALUE_CAN_NOT_BE_NULL);
        }

        int iXMLNode = 0;

        if (oValue instanceof Integer)
        {
            iXMLNode = (Integer) oValue;
        }
        else if (pcContext.containsValue(IRuleContext.SYS_XML_CURRENT))
        {
            // First see if the XMLCURRENT context variable is set.
            iXMLNode = (Integer) pcContext.getValue(IRuleContext.SYS_XML_CURRENT);
        }
        else if (oValue instanceof String)
        {
            String sXMLString = (String) oValue;

            // Now try to parse it. We must be aware of the character encoding which is defined in
            // the XML. Gregor Ottman explained it in a nice way: it's ugly as hell and qualifies as
            // a hack, but it actually complies with the xml spec and it works reliably. Java uses
            // utf-16 internally and the problem is that the xml parser ultimately doesn't as nom
            // has no clue what a java string is. NOM receives a byte[] and it derives the encoding
            // from the xml declaration. So if you have <?xml encoding="iso-8859-1"?>, then do
            // getBytes("UTF-8") and parse that with nom... well, nom will try to parse utf-8 as
            // iso-8859-1 which will FAIL.
            try
            {
                String sEncoding = NOMUtil.getXmlEncodingName(sXMLString);
                iXMLNode = s_dDoc.load(sXMLString.getBytes(sEncoding));
            }
            catch (Exception e)
            {
                throw new TriggerEngineException(e,
                                                 TriggerEngineExceptionMessages.TEE_THE_XML_COULD_NOT_BE_PARSED,
                                                 sXMLString);
            }

            // Now we need to be very carefull with memory leaks. In the context we will put a
            // global variable name XML_CURRENT. If this is already set, we'll delete it.
            Object oTemp = pcContext.getValue(IRuleContext.SYS_XML_CURRENT);

            if ((oTemp != null) && (oTemp instanceof Integer))
            {
                Node.delete((Integer) oTemp);
            }

            // Now add the currently parsed node.
            pcContext.putValue(IRuleContext.SYS_XML_CURRENT, iXMLNode);
        }
        else
        {
            throw new TriggerEngineException(TriggerEngineExceptionMessages.TEE_THE_TYPE_GIVEN_IS_NOT_SUPPORTED,
                                             oValue.getClass().getName());
        }

        // Now we always have an integer pointing to XML. Otherwise an exception was thrown.
        String sRealXPath = replaceValue(pcContext);

        // Now we have the real XPath we can evaluate it.
        XPath xpath = XPath.getXPathInstance(sRealXPath);
        XPathResult xrResult = xpath.evaluate(iXMLNode, rRule.getXPathMetaInfo());
        Object oStoreValue = null;

        switch (xrResult.getType())
        {
            case XPathResult.XPATH_NODESET:

                NodeSet nsNodeSet = xrResult.removeNodeSetFromResult();
                if (nsNodeSet.hasNext())
                {
                    long lResultNode = nsNodeSet.next();

                    // The XPath gave a result, so it matches the pattern.
                    bReturn = true;

                    // We have different options:
                    // - An attribute
                    // - An element
                    // - String value (/text())
                    // First we need to see if we need to store the value.
                    // In case of an XPath expression only 1 store object is allowed.
                    IStorageToken[] astStore = getStorageTokens();

                    if (astStore.length > 0)
                    {
                        IStorageToken stToken = astStore[0];

                        String sStoreKind = stToken.getValue();

                        if (stToken.getSource() == EStoreSource.FIXED)
                        {
                            pcContext.putValue(stToken.getName(), sStoreKind);
                        }
                        else
                        {
                            if (sStoreKind.equals("xml"))
                            {
                                // We need to store the XML result set.
                                // Known limitation: If the pattern matches multiple nodes,
                                // only the first node gets stored.
                                if (!ResultNode.isElement(lResultNode))
                                {
                                    throw new TriggerEngineException(TriggerEngineExceptionMessages.TEE_THE_RESULT_OF_EXPRESSION_IS_NOT_AN_ELEMENT_SO_WE_CANNOT_STORE_IT_AS_XML,
                                                                     sRealXPath);
                                }

                                int iNode = ResultNode.getElementNode(lResultNode);

                                // We have to duplicate the node, since we don't know what will
                                // happen with the source XML. When the context is cleared all XML
                                // nodes in it will be deleted.
                                pcContext.putValue(stToken.getName(),
                                                   new Integer(Node.duplicate(iNode)));
                            }
                            else if (sStoreKind.equals("value"))
                            {
                                String sValue = null;

                                if (ResultNode.isAttribute(lResultNode))
                                {
                                    sValue = ResultNode.getStringValue(lResultNode);
                                }
                                else
                                {
                                    int iNode = ResultNode.getElementNode(lResultNode);

                                    if (Node.getType(iNode) == NodeType.ELEMENT)
                                    {
                                        sValue = Node.getDataWithDefault(iNode, null);
                                    }
                                    else if ((Node.getType(iNode) == NodeType.DATA) ||
                                                 (Node.getType(iNode) == NodeType.CDATA))
                                    {
                                        sValue = Node.getData(iNode);
                                    }

                                    if (sValue == null)
                                    {
                                        throw new TriggerEngineException(TriggerEngineExceptionMessages.TEETHE_RESULT_OF_EXPRESSION_IS_A_NULL_STRING_VALUE,
                                                                         sRealXPath);
                                    }

                                    pcContext.putValue(stToken.getName(), sValue);
                                }
                            }
                            else
                            {
                                throw new TriggerEngineException(TriggerEngineExceptionMessages.TEE_THE_STORE_TYPE_MUST_BE_EITHER_XML_OR_VALUE,
                                                                 sStoreKind);
                            }
                        }
                    }
                }
                break;

            case XPathResult.XPATH_BOOLEAN:
                oStoreValue = xrResult.getBooleanResult();

            case XPathResult.XPATH_NUMBER:
                oStoreValue = xrResult.getNumberResult();

            case XPathResult.XPATH_STRING:
                oStoreValue = xrResult.getStringResult();

                bReturn = true;

                // Now we need to store it if there are storage tokens defined.
                IStorageToken[] astStore = getStorageTokens();

                if (astStore.length > 0)
                {
                    IStorageToken stToken = astStore[0];

                    pcContext.putValue(stToken.getName(), oStoreValue);
                }
                break;

            case XPathResult.XPATH_INVALID:
                bReturn = false;
                break;
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Pattern match: " + bReturn + "\nPattern details: " + this.toString());
        }

        if ((bReturn == false) && isOptional())
        {
            bReturn = true;
        }

        return bReturn;
    }

    /**
     * This method returns the type of pattern.
     *
     * @return  The pattern type.
     *
     * @see     com.cordys.coe.ac.emailio.config.pattern.BasePattern#getType()
     */
    @Override protected EPatternType getType()
    {
        return EPatternType.XPATH;
    }
}
