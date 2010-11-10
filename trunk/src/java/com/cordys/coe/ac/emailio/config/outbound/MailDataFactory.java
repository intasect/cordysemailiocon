package com.cordys.coe.ac.emailio.config.outbound;

import com.cordys.coe.ac.emailio.exception.OutboundEmailException;

import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * This factory can create IMailData objects.
 *
 * @author  pgussow
 */
public class MailDataFactory
{
    /**
     * This method creates a new MailData object based on the given configuration XML.
     *
     * @param   iContent  The actual content.
     * @param   xmi       The XPathMetaInfo with the prefix 'ns' mapped to the proper namespace.
     *
     * @return  The created mail data.
     *
     * @throws  OutboundEmailException  In case of any exceptions.
     */
    public static IMailData parseMailData(int iContent, XPathMetaInfo xmi)
                                   throws OutboundEmailException
    {
        IMailData mdReturn = null;

        mdReturn = new MailData(iContent, xmi);

        return mdReturn;
    }
}
