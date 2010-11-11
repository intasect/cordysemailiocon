

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
package com.cordys.coe.ac.emailio.custompatterns;

import com.cordys.coe.ac.emailio.EmailIOConnectorConstants;
import com.cordys.coe.ac.emailio.config.pattern.BasePattern;
import com.cordys.coe.ac.emailio.config.pattern.EPatternType;
import com.cordys.coe.ac.emailio.config.pattern.IPattern;
import com.cordys.coe.ac.emailio.config.rule.IRule;
import com.cordys.coe.ac.emailio.config.rule.IRuleContext;
import com.cordys.coe.ac.emailio.config.token.IStorageToken;
import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;
import com.cordys.coe.ac.emailio.exception.TriggerEngineException;
import com.cordys.coe.ac.emailio.localization.LogMessages;
import com.cordys.coe.ac.emailio.localization.TriggerEngineExceptionMessages;
import com.cordys.coe.ac.emailio.util.StringUtil;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

import java.lang.reflect.Method;

import java.text.SimpleDateFormat;

import java.util.Date;

import javax.mail.Message;

/**
 * This class will execute a certain getter on the message object via reflection. If the result type
 * is a date it will be formatted according to the Cordys standard.
 *
 * @author  pgussow
 */
public class ExecMessageGetter extends BasePattern
    implements IPattern
{
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(ExecMessageGetter.class);
    /**
     * Holds the name of the field that should be retrieved.
     */
    private String m_sField;
    /**
     * Holds the namespace prefix mappings.
     */
    private XPathMetaInfo m_xmi;

    /**
     * Creates a new ExecMessageGetter object.
     *
     * @param   iNode  The configuration XML.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    public ExecMessageGetter(int iNode)
                      throws EmailIOConfigurationException
    {
        super(iNode);

        m_xmi = new XPathMetaInfo();
        m_xmi.addNamespaceBinding("ns", EmailIOConnectorConstants.NS_CONFIGURATION);

        m_sField = XPathHelper.getStringValue(iNode, "./ns:custom/ns:field/text()", m_xmi,
                                              EMPTY_STRING);

        if (!StringUtil.isSet(m_sField))
        {
            // For backwards compatibility try this
            m_sField = XPathHelper.getStringValue(iNode, "./ns:field/text()", m_xmi, EMPTY_STRING);

            if (StringUtil.isSet(m_sField))
            {
                LOG.warn(null,
                         LogMessages.WRN_THE_CONFIGURATION_IS_INVALID_THE_TAG_FIELD_SHOULD_BE_MOVED_TO_CUSTOMFIELD);
            }
        }
    }

    /**
     * This method evaluates the pattern on the given data object. The data object is usually a
     * String, but can also be a JavaMail header or an XML node.
     *
     * @param   pcContext  The context information.
     * @param   oValue     The value to evaluate against.
     * @param   rRule      The parent rule.
     *
     * @return  true is the value matches the pattern. Otherwise false.
     *
     * @throws  TriggerEngineException  In case of any exceptions.
     *
     * @see     com.cordys.coe.ac.emailio.config.pattern.IPattern#evaluate(IRuleContext, Object,
     *          IRule)
     */
    public boolean evaluate(IRuleContext pcContext, Object oValue, IRule rRule)
                     throws TriggerEngineException
    {
        // Get the actual email message.
        Message mMessage = pcContext.getMessage();

        IStorageToken[] ast = getStorageTokens();

        if (ast.length != 1)
        {
            throw new TriggerEngineException(TriggerEngineExceptionMessages.TEE_SFA_INCORRECT_NUMBER_OF_STORAGE_TOKENS,
                                             1, ast.length);
        }

        try
        {
            Class<?> cMessage = mMessage.getClass();

            String sGetterName = "get" + m_sField;

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Looking for method with name " + sGetterName);
            }

            Method mGetter = cMessage.getMethod(sGetterName);

            Object oResult = mGetter.invoke(mMessage);
            String sContextValue = "null";

            if (oResult != null)
            {
                if (oResult instanceof Date)
                {
                    Date dTemp = (Date) oResult;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                    sContextValue = sdf.format(dTemp);
                }
                else if (oResult instanceof String)
                {
                    sContextValue = (String) oResult;
                }
                else
                {
                    sContextValue = oResult.toString();
                }
            }

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Putting the value " + sContextValue + " in the context");
            }

            pcContext.putValue(ast[0].getName(), sContextValue);
        }
        catch (Exception e)
        {
            throw new TriggerEngineException(e,
                                             TriggerEngineExceptionMessages.TEE_EMG_ERROR_GETTING_THE_VALUE_FOR_THE_FIELD);
        }

        // We'll always return true, since it can be applied to every method.
        return true;
    }

    /**
     * This method dumps the configuration of this pattern to XML.
     *
     * @param   iParent  The parent element.
     *
     * @return  The created XML structure.
     *
     * @see     com.cordys.coe.ac.emailio.config.pattern.BasePattern#toXML(int)
     */
    @Override public int toXML(int iParent)
    {
        int iReturn = super.toXML(iParent);

        // Create the field tag.
        int iValue = XPathHelper.selectSingleNode(iReturn, "ns:store", m_xmi);

        if (iValue != 0)
        {
            int iField = Node.getDocument(iValue).createElementNS("field", m_sField, null,
                                                                  Node.getNamespaceURI(iValue), 0);
            Node.insert(iField, iValue);
        }

        return iReturn;
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
        return EPatternType.CUSTOM;
    }
}
