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
 package com.cordys.coe.ac.emailio.config.rule;

import com.cordys.coe.ac.emailio.EmailIOConnectorConstants;
import com.cordys.coe.ac.emailio.config.EEmailSection;
import com.cordys.coe.ac.emailio.config.NamespaceBinding;
import com.cordys.coe.ac.emailio.config.pattern.IPattern;
import com.cordys.coe.ac.emailio.config.pattern.PatternFactory;
import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;
import com.cordys.coe.ac.emailio.localization.EmailIOConfigurationExceptionMessages;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

import java.util.ArrayList;

/**
 * This class wraps around the configuration of a rule.
 *
 * @author  pgussow
 */
class Rule
    implements IRule
{
    /**
     * Holds the name of the attribute 'section'.
     */
    private static final String ATTR_SECTION = "section";
    /**
     * Holds the name of the tag 'rule'.
     */
    private static final String TAG_RULE = "rule";
    /**
     * Holds an empty string.
     */
    private static final String EMPTY_STRING = "";
    /**
     * Holds the list of patterns for this rule.
     */
    private ArrayList<IPattern> m_alPatterns = new ArrayList<IPattern>();
    /**
     * Holds the section to which this rule applies.
     */
    private EEmailSection m_eSection;
    /**
     * Holds the namespace binding configured for this rule.
     */
    private NamespaceBinding m_nbBinding = null;

    /**
     * Creates a new Rule object.
     *
     * @param   iNode  The configuration node.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    public Rule(int iNode)
         throws EmailIOConfigurationException
    {
        XPathMetaInfo xmi = new XPathMetaInfo();
        xmi.addNamespaceBinding("ns", EmailIOConnectorConstants.NS_CONFIGURATION);

        String sTemp = XPathHelper.getStringValue(iNode, "./@section", xmi, EMPTY_STRING)
                                  .toUpperCase();

        if (sTemp.length() == 0)
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_NO_SECTION_DEFINED_FOR_RULE,
                                                    Node.writeToString(iNode, false));
        }

        try
        {
            m_eSection = EEmailSection.valueOf(sTemp);
        }
        catch (IllegalArgumentException iae)
        {
            throw new EmailIOConfigurationException(iae,
                                                    EmailIOConfigurationExceptionMessages.EICE_UNKNOWN_SECTION_DEFINED_FOR_RULE,
                                                    Node.writeToString(iNode, false));
        }

        // Now we need to parse the rule patterns.
        int[] aiPatterns = XPathHelper.selectNodes(iNode, "./ns:pattern", xmi);

        if (aiPatterns.length == 0)
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_NO_PATTERNS_DEFINED_FOR_RULE,
                                                    Node.writeToString(iNode, false));
        }

        for (int iCount = 0; iCount < aiPatterns.length; iCount++)
        {
            IPattern pPattern = PatternFactory.createPattern(aiPatterns[iCount]);
            m_alPatterns.add(pPattern);
        }

        // Parse the namespace prefix bindings.
        int iBinding = XPathHelper.selectSingleNode(iNode, "./ns:namespacemappings", xmi);

        if (iBinding != 0)
        {
            m_nbBinding = new NamespaceBinding(iBinding);
        }
    }

    /**
     * This method gets the patterns for this rule.
     *
     * @return  The patterns for this rule.
     *
     * @see     com.cordys.coe.ac.emailio.config.rule.IRule#getPatterns()
     */
    public IPattern[] getPatterns()
    {
        return m_alPatterns.toArray(new IPattern[0]);
    }

    /**
     * This method gets the section where to apply this rule.
     *
     * @return  The section where to apply this rule.
     *
     * @see     com.cordys.coe.ac.emailio.config.rule.IRule#getSection()
     */
    public EEmailSection getSection()
    {
        return m_eSection;
    }

    /**
     * This method gets the namespace binding for this rule.
     *
     * @return  The namespace binding for this rule.
     *
     * @see     com.cordys.coe.ac.emailio.config.rule.IRule#getXPathMetaInfo()
     */
    public XPathMetaInfo getXPathMetaInfo()
    {
        if (m_nbBinding != null)
        {
            return m_nbBinding.getXPathMetaInfo();
        }
        else
        {
            return new XPathMetaInfo();
        }
    }

    /**
     * This method sets the section where to apply this rule.
     *
     * @param  eSection  The section where to apply this rule.
     */
    public void setSection(EEmailSection eSection)
    {
        m_eSection = eSection;
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

        sbReturn.append("Rule for section " + getSection());
        sbReturn.append("\nPatterns:");
        sbReturn.append("\n---------\n");

        for (IPattern pPattern : m_alPatterns)
        {
            sbReturn.append(pPattern.toString());
            sbReturn.append("\n");
        }

        return sbReturn.toString();
    }

    /**
     * This method dumps the configuration of this trigger to XML.
     *
     * @param   iParent  The parent element.
     *
     * @return  The created XML structure.
     *
     * @see     com.cordys.coe.ac.emailio.config.rule.IRule#toXML(int)
     */
    public int toXML(int iParent)
    {
        int iReturn = 0;

        iReturn = Node.createElementNS(TAG_RULE, EMPTY_STRING, EMPTY_STRING,
                                       EmailIOConnectorConstants.NS_CONFIGURATION, iParent);

        Node.setAttribute(iReturn, ATTR_SECTION, m_eSection.toString());

        if (m_nbBinding != null)
        {
            m_nbBinding.toXML(iReturn);
        }

        for (IPattern pPattern : m_alPatterns)
        {
            pPattern.toXML(iReturn);
        }

        return iReturn;
    }

    /**
     * This method is called to validate the configuration. This method is used to i.e. check if the
     * email server is reachable or that the messages configured are actually sendable.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     *
     * @see     com.cordys.coe.ac.emailio.config.rule.IRule#validate()
     */
    @Override public void validate()
                            throws EmailIOConfigurationException
    {
        for (IPattern pPattern : m_alPatterns)
        {
            pPattern.validate();
        }
    }
}
