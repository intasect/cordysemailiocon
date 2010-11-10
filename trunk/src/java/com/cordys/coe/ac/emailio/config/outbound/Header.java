package com.cordys.coe.ac.emailio.config.outbound;

import com.cordys.coe.ac.emailio.exception.OutboundEmailException;
import com.cordys.coe.ac.emailio.localization.OutboundEmailExceptionMessages;
import com.cordys.coe.ac.emailio.util.StringUtil;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * This class wraps the data for a mail header.
 *
 * @author  pgussow
 */
class Header
    implements IHeader
{
    /**
     * Holds the name for this header.
     */
    private String m_sName;
    /**
     * Holds the value for this header.
     */
    private String m_sValue;

    /**
     * Creates a new Header object.
     */
    public Header()
    {
    }

    /**
     * Creates a new Header object.
     *
     * @param   iHeader  The XML data for the header.
     * @param   xmi      The XPathMetaInfo with the prefix 'ns' mapped to the proper namespace.
     *
     * @throws  OutboundEmailException  In case of any exceptions.
     */
    public Header(int iHeader, XPathMetaInfo xmi)
           throws OutboundEmailException
    {
        m_sName = XPathHelper.getStringValue(iHeader, "./ns:" + TAG_NAME, xmi, "");
        m_sValue = XPathHelper.getStringValue(iHeader, "./ns:" + TAG_VALUE, xmi, "");

        if (!StringUtil.isSet(m_sName))
        {
            throw new OutboundEmailException(OutboundEmailExceptionMessages.OEE_MISSING_PARAMETER,
                                             TAG_NAME);
        }

        if (!StringUtil.isSet(m_sValue))
        {
            throw new OutboundEmailException(OutboundEmailExceptionMessages.OEE_MISSING_PARAMETER,
                                             TAG_VALUE);
        }
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.IHeader#getName()
     */
    @Override public String getName()
    {
        return m_sName;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.IHeader#getValue()
     */
    @Override public String getValue()
    {
        return m_sValue;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.IXMLSerializable#toXML(int)
     */
    @Override public int toXML(int iParent)
    {
        int iReturn = Node.createElementWithParentNS(TAG_HEADER, null, iParent);

        Node.createElementWithParentNS(TAG_NAME, getName(), iParent);
        Node.createElementWithParentNS(TAG_VALUE, getValue(), iParent);

        return iReturn;
    }

    /**
     * This method sets the name for the header.
     *
     * @param  sName  The name of the header.
     */
    protected void setName(String sName)
    {
        m_sName = sName;
    }

    /**
     * This method sets the value for the header.
     *
     * @param  sValue  The value for the header.
     */
    protected void setValue(String sValue)
    {
        m_sValue = sValue;
    }
}
