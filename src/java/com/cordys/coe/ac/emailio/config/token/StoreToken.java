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
 package com.cordys.coe.ac.emailio.config.token;

import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;
import com.cordys.coe.ac.emailio.localization.EmailIOConfigurationExceptionMessages;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.xml.nom.Node;

/**
 * This class wraps around the storage tokens.
 *
 * @author  pgussow
 */
public class StoreToken extends Token
    implements IStorageToken
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
    private EStoreSource m_ssSource;

    /**
     * Constructor.
     *
     * @param   iNode  The configuration node.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    public StoreToken(int iNode)
               throws EmailIOConfigurationException
    {
        super(iNode);

        // Get the source for this replacement token.
        String sType = XPathHelper.getStringValue(iNode, "./ns:value/@src", getXPathMetaInfo(),
                                                  EMPTY_STRING).toUpperCase();

        try
        {
            if (sType.length() == 0)
            {
                // Default value is from the pattern.
                m_ssSource = EStoreSource.PATTERN;
            }
            else
            {
                m_ssSource = EStoreSource.valueOf(sType);
            }
        }
        catch (IllegalArgumentException iae)
        {
            throw new EmailIOConfigurationException(iae,
                                                    EmailIOConfigurationExceptionMessages.EICE_INVALID_SRC_SPECIFIED_FOR_THE_STORE_TOKEN);
        }
    }

    /**
     * This method gets the source for the value.
     *
     * @return  The source for the value.
     *
     * @see     com.cordys.coe.ac.emailio.config.token.IStorageToken#getSource()
     */
    public EStoreSource getSource()
    {
        return m_ssSource;
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
        return super.toString() + ", Source: " + m_ssSource;
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
        Node.setAttribute(iValue, ATTR_SRC, m_ssSource.toString());

        return iReturn;
    }
}
