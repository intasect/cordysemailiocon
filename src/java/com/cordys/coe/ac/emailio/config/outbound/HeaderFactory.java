package com.cordys.coe.ac.emailio.config.outbound;

import com.cordys.coe.ac.emailio.exception.OutboundEmailException;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.xml.xpath.XPathMetaInfo;

import java.util.ArrayList;

/**
 * This class can create IHeader objects from XML.
 *
 * @author  pgussow
 */
public class HeaderFactory
{
    /**
     * This method parses the XML email address.
     *
     * @param   iHeaders  The XML definition of the headers.
     * @param   xmi       The XPathMetaInfo with the prefix 'ns' mapped to the proper namespace.
     *
     * @return  The parsed address.
     *
     * @throws  OutboundEmailException  In case of any exceptions.
     */
    public static IHeader[] parseHeaders(int iHeaders, XPathMetaInfo xmi)
                                  throws OutboundEmailException
    {
        ArrayList<IHeader> alTemp = new ArrayList<IHeader>();

        int[] aiHeaders = XPathHelper.selectNodes(iHeaders, "./ns:" + IHeader.TAG_HEADER, xmi);

        for (int iHeader : aiHeaders)
        {
            IHeader hTemp = new Header(iHeader, xmi);
            alTemp.add(hTemp);
        }

        return alTemp.toArray(new IHeader[0]);
    }

    /**
     * This method parses the XML headers for the mail that needs to be send. This method is to
     * support the standard Cordys SendMail method.
     *
     * @param   iHeaders  The XML definition of the headers.
     * @param   xmi       The XPathMetaInfo with the prefix 'ns' mapped to the proper namespace.
     *
     * @return  The parsed address.
     *
     * @throws  OutboundEmailException  In case of any exceptions.
     */
    public static IHeader[] parseHeadersCompatibility(int iHeaders, XPathMetaInfo xmi)
                                               throws OutboundEmailException
    {
        ArrayList<IHeader> alTemp = new ArrayList<IHeader>();

        int[] aiHeaders = XPathHelper.selectNodes(iHeaders, "./ns:" + IHeader.TAG_HEADER, xmi);

        for (int iHeader : aiHeaders)
        {
            IHeader hTemp = new CompatibilityHeader(iHeader);
            alTemp.add(hTemp);
        }

        return alTemp.toArray(new IHeader[0]);
    }
}
