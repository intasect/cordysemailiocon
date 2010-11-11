

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
package com.cordys.coe.ac.emailio.config.token;

import com.cordys.coe.ac.emailio.config.rule.IRuleContext;
import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;
import com.cordys.coe.ac.emailio.localization.EmailIOConfigurationExceptionMessages;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.xml.nom.Node;

/**
 * This class wraps around an replacement token.
 *
 * @author  pgussow
 */
class ReplacementToken extends Token
    implements IReplacementToken
{
    /**
     * Holds an empty string.
     */
    private static final String EMPTY_STRING = "";
    /**
     * Holds the anme of the attribute 'src'.
     */
    private static final String ATTR_SRC = "src";
    /**
     * The replacement value source.
     */
    private EReplacementSource m_rsSource;

    /**
     * Creates a new ReplacementToken object.
     *
     * @param   iNode  The configuration XML.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    public ReplacementToken(int iNode)
                     throws EmailIOConfigurationException
    {
        super(iNode);

        // Get the source for this replacement token.
        String sType = XPathHelper.getStringValue(iNode, "./ns:value/@src", getXPathMetaInfo(),
                                                  EMPTY_STRING).toUpperCase();

        try
        {
            m_rsSource = EReplacementSource.valueOf(sType);
        }
        catch (IllegalArgumentException iae)
        {
            throw new EmailIOConfigurationException(iae,
                                                    EmailIOConfigurationExceptionMessages.EICE_INVALID_SRC_SPECIFIED_FOR_THE_REPLACEMENT_TOKEN);
        }
    }

    /**
     * This method gets the source for the value.
     *
     * @return  The source for the value.
     */
    public EReplacementSource getSource()
    {
        return m_rsSource;
    }

    /**
     * This method gets the value for this replacement token based on the current context.
     *
     * @param   pcContext  The current context.
     *
     * @return  The value for this replacement token based on the current context.
     *
     * @see     com.cordys.coe.ac.emailio.config.token.IReplacementToken#getValue(com.cordys.coe.ac.emailio.config.rule.IRuleContext)
     */
    public Object getValue(IRuleContext pcContext)
    {
        Object oReturn = getValue();

        if (getSource() == EReplacementSource.STORAGE)
        {
            oReturn = pcContext.getValue(getValue());
        }

        return oReturn;
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
        return super.toString() + ", Source: " + m_rsSource;
    }

    /**
     * This method dumps the configuration of this token to XML.
     *
     * @param   iParent  The parent element.
     *
     * @return  The created XML structure.
     *
     * @see     com.cordys.coe.ac.emailio.config.token.Token#toXML(int)
     */
    @Override public int toXML(int iParent)
    {
        int iReturn = super.toXML(iParent);

        int iValue = XPathHelper.selectSingleNode(iReturn, "./ns:value", getXPathMetaInfo());
        Node.setAttribute(iValue, ATTR_SRC, m_rsSource.toString());

        return iReturn;
    }
}
