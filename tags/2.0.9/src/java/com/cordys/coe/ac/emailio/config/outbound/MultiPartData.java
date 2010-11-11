

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
package com.cordys.coe.ac.emailio.config.outbound;

import com.cordys.coe.ac.emailio.exception.OutboundEmailException;
import com.cordys.coe.ac.emailio.localization.OutboundEmailExceptionMessages;
import com.cordys.coe.ac.emailio.util.StringUtil;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds the data for the mime part.
 *
 * @author  pgussow
 */
class MultiPartData
    implements IMultiPart
{
    /**
     * Holds the default content type.
     */
    private static final String DEFAULT_SUB_TYPE = "mixed";
    /**
     * Holds the nested multi parts for the mail.
     */
    private List<IMultiPart> m_lmpMultiParts = new ArrayList<IMultiPart>();
    /**
     * Holds the actual mail data for this multi part.
     */
    private IMailData m_mdData;
    /**
     * Holds the name of the tag to use when serializing back to XML.
     */
    private String m_sRootTag;
    /**
     * Holds the subtype for this multi part.
     */
    private String m_sSubType;

    /**
     * This method sets the root tag for the multipart data.
     *
     * @param  sRootTag  The root tag.
     */
    public MultiPartData(String sRootTag)
    {
        m_sRootTag = sRootTag;
    }

    /**
     * This method sets the root tag for the multipart data.
     *
     * @param  sRootTag  The root tag.
     * @param  mdData    The data for this multipart.
     */
    public MultiPartData(String sRootTag, IMailData mdData)
    {
        m_sRootTag = sRootTag;
        m_mdData = mdData;
    }

    /**
     * Creates a new MimePartData object.
     *
     * @param   iContent  The XML definition.
     * @param   xmi       The XPathMetaInfo with the prefix 'ns' mapped to the proper namespace.
     * @param   sRootTag  The root tag for this mime part.
     *
     * @throws  OutboundEmailException  In case of any exceptions.
     */
    public MultiPartData(int iContent, XPathMetaInfo xmi, String sRootTag)
                  throws OutboundEmailException
    {
        m_sRootTag = sRootTag;

        // A multipart contains EITHER 1 or more nested multiparts (in that case the subtype tag
        // MIGHT be filled, but there MUST be 1 or more multipart tags) or it contains simple data.
        // Parse the sub type.
        m_sSubType = XPathHelper.getStringValue(iContent, "./ns:" + TAG_SUB_TYPE, xmi,
                                                DEFAULT_SUB_TYPE);

        // Parse nested multi parts.
        int[] aiMultiParts = XPathHelper.selectNodes(iContent, "./ns:" + TAG_MULTI_PART, xmi);

        if (aiMultiParts.length > 0)
        {
            for (int iMimePart : aiMultiParts)
            {
                IMultiPart mpTemp = MultiPartFactory.parseMultiPart(iMimePart, xmi);
                m_lmpMultiParts.add(mpTemp);
            }
        }

        // Now parse the data.
        int iData = XPathHelper.selectSingleNode(iContent, "./ns:" + TAG_DATA, xmi);

        if (iData != 0)
        {
            m_mdData = MailDataFactory.parseMailData(iData, xmi);
        }

        // Check that either data or nested parts are available.
        if ((m_mdData == null) && (m_lmpMultiParts.size() == 0))
        {
            throw new OutboundEmailException(OutboundEmailExceptionMessages.OEE_FOR_A_MULTIPART_EITHER_NESTED_MULTIPART_SHOULD_BE_DEFINED_OR_DATA_SHOULD_BE_PRESENT);
        }
    }

    /**
     * This method adds the multipart to the current list.
     *
     * @param  mpMultipart  The multipart to add.
     */
    public void addMultipart(IMultiPart mpMultipart)
    {
        m_lmpMultiParts.add(mpMultipart);
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.IMultiPart#getMailData()
     */
    @Override public IMailData getMailData()
    {
        return m_mdData;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.IMultiPart#getMultiParts()
     */
    @Override public IMultiPart[] getMultiParts()
    {
        return m_lmpMultiParts.toArray(new IMultiPart[0]);
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.IMultiPart#getSubType()
     */
    @Override public String getSubType()
    {
        return m_sSubType;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.IMultiPart#hasData()
     */
    @Override public boolean hasData()
    {
        return m_mdData != null;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.IMultiPart#hasNestedParts()
     */
    @Override public boolean hasNestedParts()
    {
        return m_lmpMultiParts.size() > 0;
    }

    /**
     * This method sets the data for this multipart.
     *
     * @param  mdData  The data for this multipart.
     */
    public void setData(IMailData mdData)
    {
        m_mdData = mdData;
    }

    /**
     * This method sets the subtype for this multipart.
     *
     * @param  sSubType  The subtype.
     */
    public void setSubType(String sSubType)
    {
        m_sSubType = sSubType;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.IXMLSerializable#toXML(int)
     */
    @Override public int toXML(int iParent)
    {
        int iReturn = Node.createElementWithParentNS(m_sRootTag, null, iParent);

        // Do the content type.
        if (hasNestedParts())
        {
            if (StringUtil.isSet(m_sSubType))
            {
                Node.createElementWithParentNS(TAG_SUB_TYPE, m_sSubType, iReturn);
            }

            for (IMultiPart mpPart : m_lmpMultiParts)
            {
                mpPart.toXML(iReturn);
            }
        }
        else
        {
            // Only data in this part.
            m_mdData.toXML(iReturn);
        }

        return iReturn;
    }
}
