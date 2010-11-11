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
 package com.cordys.coe.ac.emailio.monitor;

import com.cordys.coe.ac.emailio.config.message.IMapping;
import com.cordys.coe.ac.emailio.config.message.IMessage;
import com.cordys.coe.ac.emailio.config.rule.IRuleContext;
import com.cordys.coe.ac.emailio.exception.TriggerEngineException;
import com.cordys.coe.ac.emailio.localization.TriggerEngineExceptionMessages;
import com.cordys.coe.util.soap.SOAPException;
import com.cordys.coe.util.soap.SOAPWrapper;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;

/**
 * This class will have some methods to assist the creation of the message from the configuration
 * (IMessage object) and the action rule context (RuleContext).
 *
 * @author  pgussow
 */
public class MessageExecutionHelper
{
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(MessageExecutionHelper.class);

    /**
     * This method creates the method for the message.
     *
     * @param   swSoap    The soap wrapper to use.
     * @param   mMessage  The message definition.
     *
     * @return  The created message.
     *
     * @throws  SOAPException  In case of any exceptions.
     */
    public static int createMethod(SOAPWrapper swSoap, IMessage mMessage)
                            throws SOAPException
    {
        int iReturn = 0;

        // No current method, so we need to create new one.
        if ((mMessage.getSoapDN() != null) && (mMessage.getSoapDN().length() > 0))
        {
            iReturn = swSoap.createSoapMethod(mMessage.getSoapDN(), mMessage.getMethod(),
                                              mMessage.getNamespace());
        }
        else
        {
            iReturn = swSoap.createSoapMethod(mMessage.getMethod(), mMessage.getNamespace());
        }

        return iReturn;
    }

    /**
     * This method fills the parameters for the request. There are 2 scenarios:<br>
     * 1. A basic call which means: Parse the input XML and execute the mappings.<br>
     * 2. A combining call which means: Parse the input XML, copy that into the method if it does
     * not exist, find the repetitive structure and execute the mappings.
     *
     * @param   iMethod     The current method node.
     * @param   m_mMessage  The message definition.
     * @param   pcContext   The rule context to use for filling the message parameters.
     *
     * @throws  TriggerEngineException  In case of any exceptions.
     */
    public static void fillParameters(int iMethod, IMessage m_mMessage, IRuleContext pcContext)
                               throws TriggerEngineException
    {
        int iParsed = 0;
        Document dDoc = Node.getDocument(iMethod);
        String sInputXML = m_mMessage.getInputXML();

        if ((sInputXML != null) && (sInputXML.length() > 0))
        {
            try
            {
                iParsed = dDoc.parseString(sInputXML);

                int iMethodParam = 0;

                // Now the structure is parsed. If the iMethod has no child elements
                // this structure will be appended to the method.
                if (Node.getFirstElement(iMethod) == 0)
                {
                    Node.appendToChildren(Node.getFirstChild(iParsed), Node.getLastChild(iParsed),
                                          iMethod);
                    iMethodParam = iMethod;

                    if ((m_mMessage.getRepeatingXPath() != null) &&
                            (m_mMessage.getRepeatingXPath().length() > 0))
                    {
                        iMethodParam = XPathHelper.selectSingleNode(iMethod,
                                                                    m_mMessage.getRepeatingXPath());
                    }
                }
                else
                {
                    // Structure already exists, so only append the repetitive structure.
                    int iTemplate = XPathHelper.selectSingleNode(iParsed,
                                                                 m_mMessage.getRepeatingXPath());

                    if (iTemplate == 0)
                    {
                        throw new TriggerEngineException(TriggerEngineExceptionMessages.TEE_COULD_NOT_FIND_THE_NODE_REFLECTED_BY_XPATH_IN_THE_XML_STRUCTURE,
                                                         m_mMessage.getRepeatingXPath(),
                                                         Node.writeToString(iTemplate, false));
                    }

                    int iTemplateInMessage = XPathHelper.selectSingleNode(iMethod,
                                                                          m_mMessage
                                                                          .getRepeatingXPath());

                    if (iTemplateInMessage == 0)
                    {
                        throw new TriggerEngineException(TriggerEngineExceptionMessages.TEE_COULD_NOT_FIND_THE_NODE_REFLECTED_BY_XPATH_IN_THE_XML_STRUCTURE,
                                                         m_mMessage.getRepeatingXPath(),
                                                         Node.writeToString(iMethod, false));
                    }

                    iTemplate = Node.appendToChildren(iTemplate,
                                                      Node.getParent(iTemplateInMessage));
                }

                // Do the mapping and fill it.
                IMapping[] amMappings = m_mMessage.getMappings();

                for (int iCount = 0; iCount < amMappings.length; iCount++)
                {
                    IMapping mMapping = amMappings[iCount];
                    mMapping.execute(pcContext, iMethodParam, m_mMessage);
                }
            }
            catch (TriggerEngineException tee)
            {
                throw tee;
            }
            catch (Exception e)
            {
                throw new TriggerEngineException(e,
                                                 TriggerEngineExceptionMessages.TEE_ERROR_CREATING_INPUT_XML_STRUCTURE);
            }
            finally
            {
                if (iParsed != 0)
                {
                    Node.delete(iParsed);
                }
            }
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Fixed XML message:\n" + Node.writeToString(iMethod, false));
        }
    }
}
