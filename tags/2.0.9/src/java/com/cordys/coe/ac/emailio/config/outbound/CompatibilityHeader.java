package com.cordys.coe.ac.emailio.config.outbound;

import com.cordys.coe.ac.emailio.exception.OutboundEmailException;
import com.cordys.coe.ac.emailio.localization.OutboundEmailExceptionMessages;
import com.cordys.coe.ac.emailio.util.StringUtil;

import com.eibus.xml.nom.Node;

/**
 * This class parses the header tag as the default Cordys SendMail method holds them.
 *
 * @author  pgussow
 */
public class CompatibilityHeader extends Header
{
    /**
     * Creates a new Header object.
     *
     * @param   iHeader  The XML data for the header.
     *
     * @throws  OutboundEmailException  In case of any exceptions.
     */
    public CompatibilityHeader(int iHeader)
                        throws OutboundEmailException
    {
        String sName = Node.getAttribute(iHeader, "name");
        String sValue = Node.getDataWithDefault(iHeader, "");

        if (!StringUtil.isSet(sName))
        {
            throw new OutboundEmailException(OutboundEmailExceptionMessages.OEE_MISSING_PARAMETER,
                                             TAG_NAME);
        }

        if (!StringUtil.isSet(sValue))
        {
            throw new OutboundEmailException(OutboundEmailExceptionMessages.OEE_MISSING_PARAMETER,
                                             TAG_VALUE);
        }

        setName(sName);
        setValue(sValue);
    }
}
