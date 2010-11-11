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

import com.cordys.coe.ac.emailio.EmailIOConnectorConstants;
import com.cordys.coe.ac.emailio.exception.OutboundEmailException;
import com.cordys.coe.ac.emailio.localization.OutboundEmailExceptionMessages;
import com.cordys.coe.ac.emailio.outbound.ISMIMEConfiguration;
import com.cordys.coe.ac.emailio.util.StringUtil;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * This class parses the configuration data for the EmailIO connector's version of the send mail
 * method.
 *
 * @author  pgussow
 */
class SendMailData
    implements ISendMailData
{
    /**
     * Holds the BCC addresses.
     */
    private IEmailAddress[] m_aeaBCC;
    /**
     * Holds the CC addresses.
     */
    private IEmailAddress[] m_aeaCC;
    /**
     * Holds the to addresses.
     */
    private IEmailAddress[] m_aeaTo;
    /**
     * Holds the headers for this mime part.
     */
    private IHeader[] m_ahHeaders = new IHeader[0];
    /**
     * Holds the mandatory from address.
     */
    private IEmailAddress m_eaFrom;
    /**
     * Holds the optional reply-to address.
     */
    private IEmailAddress m_eaReplyTo;
    /**
     * Holds the data for the current mail.
     */
    private IMailData m_mdMailData;
    /**
     * Holds the main content of the mail.
     */
    private IMultiPart m_mpMultiPart;
    /**
     * Holds the send options for this specific method.
     */
    private ISendOptions m_soSendOptions;
    /**
     * Holds the subject for the mail.
     */
    private String m_sSubject;

    /**
     * Creates a new SendMailData object.
     */
    public SendMailData()
    {
    }

    /**
     * Creates a new SendMailData object.
     *
     * @param   iData     The XML data.
     * @param   xmi       The XPathMetaInfo object. The prefix 'ns' must be mapped to the proper
     *                    namespace.
     * @param   scConfig  The configuration for S/MIME.
     *
     * @throws  OutboundEmailException  In case of any exceptions.
     */
    SendMailData(int iData, XPathMetaInfo xmi, ISMIMEConfiguration scConfig)
          throws OutboundEmailException
    {
        // Get the server name.
        int iSendOptions = XPathHelper.selectSingleNode(iData, "./ns:" + TAG_OPTIONS, xmi);
        m_soSendOptions = SendOptionsFactory.parseSendOptions(iSendOptions, xmi, scConfig);

        // Parse the mandatory FROM part.
        int iFrom = XPathHelper.selectSingleNode(iData, "./ns:" + TAG_FROM, xmi);

        if (iFrom == 0)
        {
            throw new OutboundEmailException(OutboundEmailExceptionMessages.OEE_MISSING_PARAMETER,
                                             TAG_FROM);
        }

        m_eaFrom = AddressFactory.parseEmailAddress(iFrom, xmi);

        // Parse the optional replyto part.
        int iReplyTo = XPathHelper.selectSingleNode(iData, "./ns:" + TAG_REPLY_TO, xmi);

        if (iReplyTo != 0)
        {
            m_eaReplyTo = AddressFactory.parseEmailAddress(iReplyTo, xmi);
        }

        // Parse the to addresses.
        int iTo = XPathHelper.selectSingleNode(iData, "./ns:" + TAG_TO, xmi);

        if (iTo != 0)
        {
            m_aeaTo = AddressFactory.parseEmailAddresses(iTo, xmi);
        }

        // Parse the cc addresses.
        int iCC = XPathHelper.selectSingleNode(iData, "./ns:" + TAG_CC, xmi);

        if (iCC != 0)
        {
            m_aeaCC = AddressFactory.parseEmailAddresses(iCC, xmi);
        }

        // Parse the bcc addresses.
        int iBCC = XPathHelper.selectSingleNode(iData, "./ns:" + TAG_BCC, xmi);

        if (iBCC != 0)
        {
            m_aeaBCC = AddressFactory.parseEmailAddresses(iBCC, xmi);
        }

        // There must be at least 1 recipient address.
        validateRecipients();

        // Parse the subject.
        m_sSubject = XPathHelper.getStringValue(iData, "./ns:" + TAG_SUBJECT, xmi, "");

        // Parse headers
        int iHeaders = XPathHelper.selectSingleNode(iData, "./ns:" + TAG_HEADERS, xmi);

        if (iHeaders != 0)
        {
            m_ahHeaders = HeaderFactory.parseHeaders(iHeaders, xmi);
        }
        else
        {
            m_ahHeaders = new IHeader[0];
        }

        // Parse the content of the mail. The data for mail is either a list of
        int iMultiPart = XPathHelper.selectSingleNode(iData, "./ns:" + IMultiPart.TAG_MULTI_PART,
                                                      xmi);
        int iMailData = XPathHelper.selectSingleNode(iData, "./ns:" + IMultiPart.TAG_DATA, xmi);

        if ((iMultiPart != 0) && (iMailData != 0))
        {
            throw new OutboundEmailException(OutboundEmailExceptionMessages.OEE_ONLY_ONE_OF_THE_TAGS_0_AND_1_SHOULD_BE_SPECIFIED_FOR_THE_SEND_MAIL_DATA,
                                             IMultiPart.TAG_MULTI_PART, IMultiPart.TAG_DATA);
        }
        else if ((iMultiPart == 0) && (iMailData == 0))
        {
            throw new OutboundEmailException(OutboundEmailExceptionMessages.OEE_ONE_OF_THE_TAGS_0_OR_1_SHOULD_BE_SPECIFIED,
                                             IMultiPart.TAG_MULTI_PART, IMultiPart.TAG_DATA);
        }

        if (iMultiPart != 0)
        {
            m_mpMultiPart = MultiPartFactory.parseMultiPart(iMultiPart, xmi);
        }
        else if (iMailData != 0)
        {
            m_mdMailData = MailDataFactory.parseMailData(iMailData, xmi);
        }
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.ISendMailData#getBCC()
     */
    @Override public IEmailAddress[] getBCC()
    {
        return m_aeaBCC;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.ISendMailData#getCC()
     */
    @Override public IEmailAddress[] getCC()
    {
        return m_aeaCC;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.ISendMailData#getFrom()
     */
    @Override public IEmailAddress getFrom()
    {
        return m_eaFrom;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.ISendMailData#getHeaders()
     */
    @Override public IHeader[] getHeaders()
    {
        return m_ahHeaders;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.ISendMailData#getMailData()
     */
    @Override public IMailData getMailData()
    {
        return m_mdMailData;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.ISendMailData#getMultiPart()
     */
    @Override public IMultiPart getMultiPart()
    {
        return m_mpMultiPart;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.ISendMailData#getReplyTo()
     */
    @Override public IEmailAddress getReplyTo()
    {
        return m_eaReplyTo;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.ISendMailData#getSendOptions()
     */
    @Override public ISendOptions getSendOptions()
    {
        return m_soSendOptions;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.ISendMailData#getSubject()
     */
    @Override public String getSubject()
    {
        return m_sSubject;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.ISendMailData#getTo()
     */
    @Override public IEmailAddress[] getTo()
    {
        return m_aeaTo;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.ISendMailData#hasData()
     */
    @Override public boolean hasData()
    {
        return m_mdMailData != null;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.ISendMailData#hasNestedParts()
     */
    @Override public boolean hasNestedParts()
    {
        return m_mpMultiPart != null;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.ISendMailData#setBCC(com.cordys.coe.ac.emailio.config.outbound.IEmailAddress[])
     */
    @Override public void setBCC(IEmailAddress[] aeaBCC)
    {
        m_aeaBCC = aeaBCC;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.ISendMailData#setCC(com.cordys.coe.ac.emailio.config.outbound.IEmailAddress[])
     */
    @Override public void setCC(IEmailAddress[] aeaCC)
    {
        m_aeaCC = aeaCC;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.ISendMailData#setFrom(com.cordys.coe.ac.emailio.config.outbound.IEmailAddress)
     */
    @Override public void setFrom(IEmailAddress eaFrom)
    {
        m_eaFrom = eaFrom;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.ISendMailData#setHeaders(com.cordys.coe.ac.emailio.config.outbound.IHeader[])
     */
    @Override public void setHeaders(IHeader[] ahHeaders)
    {
        m_ahHeaders = ahHeaders;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.ISendMailData#setMailData(com.cordys.coe.ac.emailio.config.outbound.IMailData)
     */
    @Override public void setMailData(IMailData mdMailData)
    {
        m_mdMailData = mdMailData;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.ISendMailData#setMultiPart(com.cordys.coe.ac.emailio.config.outbound.IMultiPart)
     */
    @Override public void setMultiPart(IMultiPart mpMultiPart)
    {
        m_mpMultiPart = mpMultiPart;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.ISendMailData#setReplyTo(com.cordys.coe.ac.emailio.config.outbound.IEmailAddress)
     */
    @Override public void setReplyTo(IEmailAddress eaReplyTo)
    {
        m_eaReplyTo = eaReplyTo;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.ISendMailData#setSendOptions(com.cordys.coe.ac.emailio.config.outbound.ISendOptions)
     */
    @Override public void setSendOptions(ISendOptions soSendOptions)
    {
        m_soSendOptions = soSendOptions;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.ISendMailData#setSubject(java.lang.String)
     */
    @Override public void setSubject(String sSubject)
    {
        m_sSubject = sSubject;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.ISendMailData#setTo(com.cordys.coe.ac.emailio.config.outbound.IEmailAddress[])
     */
    @Override public void setTo(IEmailAddress[] aeaTo)
    {
        m_aeaTo = aeaTo;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.ISendMailData#toXML(int)
     */
    @Override public int toXML(int iParent)
    {
        int iReturn = Node.createElementNS(TAG_SEND_MAIL, null, null,
                                           EmailIOConnectorConstants.NS_OUTBOUND, iParent);

        int iFrom = Node.createElementWithParentNS(TAG_FROM, null, iReturn);
        m_eaFrom.toXML(iFrom);

        if (m_eaReplyTo != null)
        {
            int iReplyTo = Node.createElementWithParentNS(TAG_REPLY_TO, null, iReturn);
            m_eaReplyTo.toXML(iReplyTo);
        }

        if (m_aeaTo.length > 0)
        {
            int iTo = Node.createElementWithParentNS(TAG_TO, null, iReturn);

            for (IEmailAddress eaAddress : m_aeaTo)
            {
                eaAddress.toXML(iTo);
            }
        }

        if (m_aeaCC.length > 0)
        {
            int iCC = Node.createElementWithParentNS(TAG_CC, null, iReturn);

            for (IEmailAddress eaAddress : m_aeaCC)
            {
                eaAddress.toXML(iCC);
            }
        }

        if (m_aeaBCC.length > 0)
        {
            int iBCC = Node.createElementWithParentNS(TAG_BCC, null, iReturn);

            for (IEmailAddress eaAddress : m_aeaBCC)
            {
                eaAddress.toXML(iBCC);
            }
        }

        if (StringUtil.isSet(m_sSubject))
        {
            Node.createElementWithParentNS(TAG_SUBJECT, m_sSubject, iReturn);
        }

        if (m_mpMultiPart != null)
        {
            m_mpMultiPart.toXML(iReturn);
        }

        if (m_mdMailData != null)
        {
            m_mdMailData.toXML(iReturn);
        }

        return iReturn;
    }

    /**
     * This method validates that at least 1 recipietn is set.
     *
     * @throws  OutboundEmailException
     */
    protected void validateRecipients()
                               throws OutboundEmailException
    {
        if ((m_aeaTo.length == 0) && (m_aeaCC.length == 0) && (m_aeaBCC.length == 0))
        {
            throw new OutboundEmailException(OutboundEmailExceptionMessages.OEE_AT_LEAST_1_RECIPIENT_ADDRESS_MUST_BE_SPECIFIED);
        }
    }
}
