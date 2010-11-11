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
 package com.cordys.coe.ac.emailio.config.outbound;

import com.cordys.coe.ac.emailio.exception.OutboundEmailException;
import com.cordys.coe.ac.emailio.localization.OutboundEmailExceptionMessages;
import com.cordys.coe.ac.emailio.util.StringUtil;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

import javax.mail.internet.InternetAddress;

/**
 * This class wraps the internet address.
 *
 * @author  pgussow
 */
class EmailAddress
    implements IEmailAddress
{
    /**
     * Holds the Internet address object to use.
     */
    private InternetAddress m_iaInternetAddress;
    /**
     * Holds the display name for this Internet address.
     */
    private String m_sDisplayName;
    /**
     * Holds the actual email address.
     */
    private String m_sEmailAddress;

    /**
     * Creates a new EmailAddress object.
     */
    public EmailAddress()
    {
    }

    /**
     * Constructor.
     *
     * @param   iEmailAddress  The XML definition of the address.
     * @param   xmi            The XPathMetaInfo object. The prefix 'ns' must be mapped to the
     *                         proper namespace.
     *
     * @throws  OutboundEmailException  In case of any exceptions.
     */
    public EmailAddress(int iEmailAddress, XPathMetaInfo xmi)
                 throws OutboundEmailException
    {
        m_sEmailAddress = XPathHelper.getStringValue(iEmailAddress, "./ns:" + TAG_EMAIL_ADDRESS,
                                                     xmi, "");

        if (!StringUtil.isSet(m_sEmailAddress))
        {
            throw new OutboundEmailException(OutboundEmailExceptionMessages.OEE_THE_TAG_0_MUST_BE_FILLED,
                                             TAG_EMAIL_ADDRESS);
        }

        m_sDisplayName = XPathHelper.getStringValue(iEmailAddress, "./ns:" + TAG_DISPLAY_NAME, xmi,
                                                    "");

        parseInternetAddress();
    }

    /**
     * Creates a new EmailAddress object.
     *
     * @param   sEmailAddress  The actual email address.
     * @param   sDisplayName   The display name for the email address.
     *
     * @throws  OutboundEmailException  In case of any exceptions.
     */
    public EmailAddress(String sEmailAddress, String sDisplayName)
                 throws OutboundEmailException
    {
        m_sEmailAddress = sEmailAddress;
        m_sDisplayName = sDisplayName;

        parseInternetAddress();
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.IEmailAddress#getDisplayName()
     */
    @Override public String getDisplayName()
    {
        return m_sDisplayName;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.IEmailAddress#getEmailAddress()
     */
    @Override public String getEmailAddress()
    {
        return m_sEmailAddress;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.IEmailAddress#getInternetAddress()
     */
    @Override public InternetAddress getInternetAddress()
    {
        return m_iaInternetAddress;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.IEmailAddress#toXML(int)
     */
    @Override public int toXML(int iParent)
    {
        int iReturn = Node.createElementWithParentNS(TAG_ADDRESS, null, iParent);

        Node.createElementWithParentNS(TAG_EMAIL_ADDRESS, m_sEmailAddress, iReturn);

        if (StringUtil.isSet(m_sDisplayName))
        {
            Node.createElementWithParentNS(TAG_DISPLAY_NAME, m_sDisplayName, iReturn);
        }

        return iReturn;
    }

    /**
     * This method parses the given address details in a real JavaMAil InternetAddress object.
     *
     * @throws  OutboundEmailException  In case the address is not correct.
     */
    protected void parseInternetAddress()
                                 throws OutboundEmailException
    {
        try
        {
            if (StringUtil.isSet(m_sDisplayName))
            {
                m_iaInternetAddress = new InternetAddress(getEmailAddress(), getDisplayName());
            }
            else
            {
                m_iaInternetAddress = new InternetAddress(getEmailAddress());
            }
        }
        catch (Exception e)
        {
            throw new OutboundEmailException(e,
                                             OutboundEmailExceptionMessages.OEE_ERROR_CREATING_INTERNET_ADDRESS_FROM_0,
                                             (StringUtil.isSet(m_sDisplayName)
                                              ? (m_sDisplayName + "<" + m_sEmailAddress + ">")
                                              : getEmailAddress()));
        }
    }

    /**
     * This method sets the display name for this address.
     *
     * @param  sDisplayName  The display name for this address.
     */
    protected void setDisplayName(String sDisplayName)
    {
        m_sDisplayName = sDisplayName;
    }

    /**
     * This method sets the email address for this object.
     *
     * @param  sEmailAddress  The email address for this object.
     */
    protected void setEmailAddress(String sEmailAddress)
    {
        m_sEmailAddress = sEmailAddress;
    }
}
