package com.cordys.coe.ac.emailio.config.outbound;

import com.cordys.coe.ac.emailio.exception.OutboundEmailException;

import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * This factory parses the mime part definition.
 *
 * @author  pgussow
 */
public class MultiPartFactory
{
    /**
     * This method parses the mime part.
     *
     * @param   iContent  The details of the mime part.
     * @param   xmi       The XPathMetaInfo with the prefix 'ns' mapped to the proper namespace.
     *
     * @return  The parsed Mime part.
     *
     * @throws  OutboundEmailException  In case of any exceptions.
     */
    public static IMultiPart parseMultiPart(int iContent, XPathMetaInfo xmi)
                                     throws OutboundEmailException
    {
        IMultiPart mpReturn = null;

        mpReturn = new MultiPartData(iContent, xmi, IMultiPart.TAG_MULTI_PART);

        return mpReturn;
    }
}
