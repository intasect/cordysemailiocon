

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

import com.cordys.coe.ac.emailio.EmailIOConnectorConstants;
import com.cordys.coe.ac.emailio.config.rule.IRuleContext;
import com.cordys.coe.ac.emailio.config.token.IReplacementToken;
import com.cordys.coe.ac.emailio.config.token.IStorageToken;
import com.cordys.coe.ac.emailio.config.token.TokenFactory;
import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;
import com.cordys.coe.ac.emailio.exception.TriggerEngineException;
import com.cordys.coe.ac.emailio.localization.EmailIOConfigurationExceptionMessages;
import com.cordys.coe.ac.emailio.localization.TriggerEngineExceptionMessages;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

import java.util.ArrayList;

/**
 * Base class for patterns.
 *
 * @author  pgussow
 */
public abstract class BasePattern
    implements IPattern
{
    /**
     * Holds the logger to use.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(BasePattern.class);
    /**
     * Holds an empty string.
     */
    protected static final String EMPTY_STRING = "";
    /**
     * Holds the name of the tag 'pattern'.
     */
    private static final String TAG_PATTERN = "pattern";
    /**
     * Holds the name of the attribute 'type'.
     */
    private static final String ATTR_TYPE = "type";
    /**
     * Holds the name of the tag 'replacement'.
     */
    private static final String TAG_REPLACEMENT = "replacement";
    /**
     * Holds the name of the tag 'store'.
     */
    private static final String TAG_STORE = "store";
    /**
     * Holds the name of the tag 'value'.
     */
    private static final String TAG_VALUE = "value";
    /**
     * Holds the replacement options for this pattern.
     */
    private ArrayList<IReplacementToken> m_alReplacementTokens = new ArrayList<IReplacementToken>();
    /**
     * Holds the intermediate storage token definitions for this pattern.
     */
    private ArrayList<IStorageToken> m_alStorageTokens = new ArrayList<IStorageToken>();
    /**
     * Holds whether or not the pattern is optional.
     */
    private boolean m_bOptional;
    /**
     * Holds the value for this pattern.
     */
    private String m_sValue;

    /**
     * Creates a new BasePattern object.
     *
     * @param   iNode  The configuration XML.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    public BasePattern(int iNode)
                throws EmailIOConfigurationException
    {
        XPathMetaInfo xmi = new XPathMetaInfo();
        xmi.addNamespaceBinding("ns", EmailIOConnectorConstants.NS_CONFIGURATION);

        if (iNode == 0)
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_ERROR_CREATING_PATTERN);
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Pattern value with /text(): '" +
                      XPathHelper.getStringValue(iNode, "./ns:value/text()", xmi, EMPTY_STRING) +
                      "'\nPattern without /text(): '" +
                      XPathHelper.getStringValue(iNode, "./ns:value", xmi, EMPTY_STRING) + "'");
        }
        m_sValue = XPathHelper.getStringValue(iNode, "./ns:value", xmi, EMPTY_STRING);

        if (m_sValue.length() == 0)
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_ERROR_CREATING_PATTERN_MISSING_VALUE);
        }

        // Make sure no whitespace is used.
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Pattern value before trim: '" + m_sValue + "'\nValue after trim: '" +
                      m_sValue.trim() + "'");
        }
        m_sValue = m_sValue.trim();

        m_bOptional = Boolean.parseBoolean(Node.getAttribute(iNode, "optional", "false"));

        // Get the replacements.
        int[] aiReplacementTokens = XPathHelper.selectNodes(iNode, "./ns:replacement/ns:token",
                                                            xmi);

        for (int iCount = 0; iCount < aiReplacementTokens.length; iCount++)
        {
            IReplacementToken rtNew = TokenFactory.createReplacementToken(aiReplacementTokens[iCount]);
            m_alReplacementTokens.add(rtNew);
        }

        // Get the storage.
        int[] aiStorageTokens = XPathHelper.selectNodes(iNode, "./ns:store/ns:token", xmi);

        for (int iCount = 0; iCount < aiStorageTokens.length; iCount++)
        {
            IStorageToken stNew = TokenFactory.createStorageToken(aiStorageTokens[iCount]);
            m_alStorageTokens.add(stNew);
        }
    }

    /**
     * This method gets the replacement tokens.
     *
     * @return  The replacement tokens.
     *
     * @see     com.cordys.coe.ac.emailio.config.pattern.IPattern#getReplacementTokens()
     */
    public IReplacementToken[] getReplacementTokens()
    {
        return m_alReplacementTokens.toArray(new IReplacementToken[0]);
    }

    /**
     * This method gets the storage tokens.
     *
     * @return  The storage tokens.
     *
     * @see     com.cordys.coe.ac.emailio.config.pattern.IPattern#getStorageTokens()
     */
    public IStorageToken[] getStorageTokens()
    {
        return m_alStorageTokens.toArray(new IStorageToken[0]);
    }

    /**
     * This method gets the value for this pattern.
     *
     * @return  The value for this pattern.
     *
     * @see     com.cordys.coe.ac.emailio.config.pattern.IPattern#getValue()
     */
    public String getValue()
    {
        return m_sValue;
    }

    /**
     * This method sets the value for this pattern.
     *
     * @param  sValue  The value for this pattern.
     */
    public void setValue(String sValue)
    {
        m_sValue = sValue;
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

        sbReturn.append("Type:").append(getClass().getSimpleName()).append("\nPattern value: " +
                                                                           getValue());

        if (m_alReplacementTokens.size() > 0)
        {
            sbReturn.append("\n-------------------");
            sbReturn.append("\nReplacement tokens:");
            sbReturn.append("\n-------------------\n");

            for (IReplacementToken rtToken : m_alReplacementTokens)
            {
                sbReturn.append(rtToken.toString());
                sbReturn.append("\n");
            }
        }

        if (m_alStorageTokens.size() > 0)
        {
            sbReturn.append("\n---------------");
            sbReturn.append("\nStorage tokens:");
            sbReturn.append("\n---------------\n");

            for (IStorageToken stToken : m_alStorageTokens)
            {
                sbReturn.append(stToken.toString());
                sbReturn.append("\n");
            }
        }

        return sbReturn.toString();
    }

    /**
     * This method dumps the configuration of this pattern to XML.
     *
     * @param   iParent  The parent element.
     *
     * @return  The created XML structure.
     *
     * @see     com.cordys.coe.ac.emailio.config.pattern.IPattern#toXML(int)
     */
    public int toXML(int iParent)
    {
        int iReturn = 0;

        iReturn = Node.createElementNS(TAG_PATTERN, EMPTY_STRING, EMPTY_STRING,
                                       EmailIOConnectorConstants.NS_CONFIGURATION, iParent);

        Node.createElementWithParentNS(TAG_VALUE, getValue(), iReturn);
        Node.setAttribute(iReturn, ATTR_TYPE, getType().toString());

        if (m_bOptional == true)
        {
            Node.setAttribute(iReturn, "optional", String.valueOf(m_bOptional));
        }

        if ((m_alReplacementTokens != null) && (m_alReplacementTokens.size() > 0))
        {
            int iReplacement = Node.createElementWithParentNS(TAG_REPLACEMENT, EMPTY_STRING,
                                                              iReturn);

            for (IReplacementToken tToken : m_alReplacementTokens)
            {
                tToken.toXML(iReplacement);
            }
        }

        if ((m_alStorageTokens != null) && (m_alStorageTokens.size() > 0))
        {
            int iReplacement = Node.createElementWithParentNS(TAG_STORE, EMPTY_STRING, iReturn);

            for (IStorageToken tToken : m_alStorageTokens)
            {
                tToken.toXML(iReplacement);
            }
        }

        return iReturn;
    }

    /**
     * This method is called to validate the configuration. This method is used to i.e. check if the
     * email server is reachable or that the messages configured are actually sendable.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     *
     * @see     com.cordys.coe.ac.emailio.config.pattern.IPattern#validate()
     */
    @Override public void validate()
                            throws EmailIOConfigurationException
    {
    }

    /**
     * This method returns whether or not this pattern works on the headers of an email message.
     *
     * @return  true if this patterns needs the headers. Otherwise false.
     *
     * @see     com.cordys.coe.ac.emailio.config.pattern.IPattern#worksOnHeader()
     */
    public boolean worksOnHeader()
    {
        return false;
    }

    /**
     * This method returns the type of pattern.
     *
     * @return  The pattern type.
     */
    protected abstract EPatternType getType();

    /**
     * This method returns whether or not this pattern is optional.
     *
     * @return  Whether or not this pattern is optional.
     */
    protected boolean isOptional()
    {
        return m_bOptional;
    }

    /**
     * This method replaces the value with all the replacement tokens and the values in the context.
     *
     * @param   pcContext  The current context.
     *
     * @return  The replaced value.
     *
     * @throws  TriggerEngineException  In case of any exceptions.
     */
    protected String replaceValue(IRuleContext pcContext)
                           throws TriggerEngineException
    {
        String sReturn = getValue();

        IReplacementToken[] artTokens = getReplacementTokens();

        for (int iCount = 0; iCount < artTokens.length; iCount++)
        {
            IReplacementToken rtToken = artTokens[iCount];
            Object oTemp = rtToken.getValue(pcContext);

            if (oTemp instanceof String)
            {
                String sValue = (String) oTemp;
                sReturn = sReturn.replaceAll(rtToken.getName(), sValue);
            }
            else
            {
                throw new TriggerEngineException(TriggerEngineExceptionMessages.TEE_THE_VALUE_FOR_FIELD__CANNOT_BE_REPLACED_BECAUSE_ITS_NOT_A_STRING_VALUE,
                                                 rtToken.toString());
            }
        }

        return sReturn;
    }
}
