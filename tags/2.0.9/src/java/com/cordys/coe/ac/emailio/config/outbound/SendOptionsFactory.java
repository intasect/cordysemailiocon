package com.cordys.coe.ac.emailio.config.outbound;

import com.cordys.coe.ac.emailio.outbound.ISMIMEConfiguration;

import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * This method creates the send options object based on the configuration.
 *
 * @author  pgussow
 */
public class SendOptionsFactory
{
    /**
     * This method creates a new ISendOptions object based on the given configuration XML.
     *
     * @param   iContent  The actual content.
     * @param   xmi       The XPathMetaInfo with the prefix 'ns' mapped to the proper namespace.
     * @param   scConfig  The configuration for S/MIME.
     *
     * @return  The created mail data.
     */
    public static ISendOptions parseSendOptions(int iContent, XPathMetaInfo xmi,
                                                ISMIMEConfiguration scConfig)
    {
        ISendOptions soReturn = null;

        soReturn = new SendOptions(iContent, xmi, scConfig);

        return soReturn;
    }
}
