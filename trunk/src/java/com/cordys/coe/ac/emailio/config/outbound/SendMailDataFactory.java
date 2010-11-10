package com.cordys.coe.ac.emailio.config.outbound;

import com.cordys.coe.ac.emailio.EmailIOConnectorConstants;
import com.cordys.coe.ac.emailio.exception.OutboundEmailException;
import com.cordys.coe.ac.emailio.outbound.ISMIMEConfiguration;

import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * This factory class parses the data for the send mail call. The factory can parse the Email IO
 * connectors own format, but it can also parse the default Cordys SendMail method for compatibility
 * reasons.
 *
 * @author  pgussow
 */
public class SendMailDataFactory
{
    /**
     * This method parses the data based on the Email IO connector's SendMail method.
     *
     * @param   iData     The data for the SendMail method.
     * @param   scConfig  The configuration for S/MIME.
     *
     * @return  The parsed SendMail data.
     *
     * @throws  OutboundEmailException  In case of any exceptions.
     */
    public static ISendMailData parseSendMailData(int iData, ISMIMEConfiguration scConfig)
                                           throws OutboundEmailException
    {
        ISendMailData smdReturn = null;

        XPathMetaInfo xmi = new XPathMetaInfo();
        xmi.addNamespaceBinding("ns", EmailIOConnectorConstants.NS_OUTBOUND);

        smdReturn = new SendMailData(iData, xmi, scConfig);

        return smdReturn;
    }

    /**
     * This method parses the data based on the Email IO connector's SendMail method.
     *
     * @param   iData     The data for the SendMail method.
     * @param   scConfig  The configuration for S/MIME.
     *
     * @return  The parsed SendMail data.
     *
     * @throws  OutboundEmailException  In case of any exceptions.
     */
    public static ISendMailData parseSendMailDataCompatibility(int iData,
                                                               ISMIMEConfiguration scConfig)
                                                        throws OutboundEmailException
    {
        ISendMailData smdReturn = null;

        XPathMetaInfo xmi = new XPathMetaInfo();
        xmi.addNamespaceBinding("ns", EmailIOConnectorConstants.NS_SEND_MAIL_COMPATIBILITY);

        smdReturn = new CompatibilitySendMailData(iData, xmi, scConfig);

        return smdReturn;
    }
}
