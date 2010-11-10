package com.cordys.coe.ac.emailio.config.outbound;

import com.cordys.coe.ac.emailio.exception.OutboundEmailException;
import com.cordys.coe.ac.emailio.localization.OutboundEmailExceptionMessages;
import com.cordys.coe.ac.emailio.outbound.ISMIMEConfiguration;
import com.cordys.coe.ac.emailio.util.StringUtil;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.util.system.Native;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * This class maintains the compatibility with the default SendMail method.
 *
 * @author  pgussow
 */
class CompatibilitySendMailData extends SendMailData
{
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
    CompatibilitySendMailData(int iData, XPathMetaInfo xmi, ISMIMEConfiguration scConfig)
                       throws OutboundEmailException
    {
        super();

        // Create the default send options based on the current configuration.
        ISendOptions so = SendOptionsFactory.parseSendOptions(0, xmi, scConfig);
        setSendOptions(so);

        // Parse the mandatory FROM part.
        int iFrom = XPathHelper.selectSingleNode(iData, "./ns:" + TAG_FROM, xmi);

        if (iFrom == 0)
        {
            throw new OutboundEmailException(OutboundEmailExceptionMessages.OEE_MISSING_PARAMETER,
                                             TAG_FROM);
        }

        setFrom(AddressFactory.parseEmailAddressCompatibility(iFrom, xmi));

        // Parse the optional replyto part.
        int iReplyTo = XPathHelper.selectSingleNode(iData, "./ns:from/ns:replyTo", xmi);

        if (iReplyTo != 0)
        {
            setReplyTo(new CompatibilityEmailAddress(Node.getDataWithDefault(iReplyTo, ""), null));
        }

        // Parse the to addresses.
        int iTo = XPathHelper.selectSingleNode(iData, "./ns:" + TAG_TO, xmi);

        if (iTo != 0)
        {
            setTo(AddressFactory.parseEmailAddressesCompatibility(iTo, xmi));
        }

        // Parse the cc addresses.
        int iCC = XPathHelper.selectSingleNode(iData, "./ns:" + TAG_CC, xmi);

        if (iCC != 0)
        {
            setCC(AddressFactory.parseEmailAddressesCompatibility(iCC, xmi));
        }

        // Parse the bcc addresses.
        int iBCC = XPathHelper.selectSingleNode(iData, "./ns:" + TAG_BCC, xmi);

        if (iBCC != 0)
        {
            setBCC(AddressFactory.parseEmailAddressesCompatibility(iBCC, xmi));
        }

        // There must be at least 1 recipient address.
        validateRecipients();

        // Parse the subject.
        setSubject(XPathHelper.getStringValue(iData, "./ns:" + TAG_SUBJECT, xmi, ""));

        // Parse headers
        int iHeaders = XPathHelper.selectSingleNode(iData, "./ns:" + TAG_HEADERS, xmi);

        if (iHeaders != 0)
        {
            setHeaders(HeaderFactory.parseHeadersCompatibility(iHeaders, xmi));
        }

        // Now the real difference starts. In the default SendMail there is a body and an
        // attachments tag. If there is only a body tag with no attachments a single data block will
        // be created. But if there is a body AND attachments a multipart will be created to deal
        // with this.
        int iBody = XPathHelper.selectSingleNode(iData, "./ns:body", xmi);
        String sType = Node.getAttribute(iBody, "type");

        IMailData mdMainMailData = getMailData(iBody, sType);

        MultiPartData mpd = new MultiPartData(IMultiPart.TAG_MULTI_PART);
        mpd.setSubType("mixed");

        IMultiPart mpMainData = new MultiPartData(IMultiPart.TAG_MULTI_PART, mdMainMailData);
        mpd.addMultipart(mpMainData);

        // Now we handle the attachments.
        int[] aiAttachments = XPathHelper.selectNodes(iData, "./ns:attachments/ns:attachment", xmi);

        for (int iAttachment : aiAttachments)
        {
            // Get the name and content of the file.
            String sFileName = Node.getAttribute(iAttachment, "name", "");
            String sFileContent = Node.getData(iAttachment);

            if (!StringUtil.isSet(sFileName))
            {
                throw new OutboundEmailException(OutboundEmailExceptionMessages.OEE_COMP_ATTACHMENT_NAME_IS_NOT_GIVEN);
            }

            // Get if the data is BASe64 encoded or not.
            MailData mdAttachment = new MailData();

            // Always set the dispostion.
            mdAttachment.setContentDisposition("attachment; filename=\"" + sFileName + "\"");

            // Based on whether the data is encoded binary or plain text.
            if (Node.getAttribute(iAttachment, "encoded").equalsIgnoreCase("true"))
            {
                mdAttachment.setContentType("application/octet-stream; name=\"" + sFileName + "\"");

                // Decode the data
                byte[] baEncoded = sFileContent.getBytes();
                byte[] baData = Native.decodeBinBase64(baEncoded, baEncoded.length);
                mdAttachment.setData(baData);
            }
            else
            {
                mdAttachment.setContentType("text/plain");
                mdAttachment.setData(sFileContent.getBytes());
            }

            // Add the attachment to the main multipart.
            IMultiPart mpData = new MultiPartData(IMultiPart.TAG_MULTI_PART, mdAttachment);
            mpd.addMultipart(mpData);
        }

        setMultiPart(mpd);
    }

    /**
     * This methdo creates the mail data object for the body of the mail.
     *
     * @param   iBody  The body node.
     * @param   sType  The type (html/plain);
     *
     * @return  The mail data for this object.
     */
    private IMailData getMailData(int iBody, String sType)
    {
        MailData mdReturn = new MailData();

        if ("html".equals(sType))
        {
            mdReturn.setDataType(EDataSourceType.XML);
            mdReturn.setContentType("text/html; charset= utf-8");
        }
        else
        {
            mdReturn.setDataType(EDataSourceType.PLAIN);
            mdReturn.setContentType("text/plain");
        }

        String sData = Node.getDataWithDefault(iBody, "");
        mdReturn.setData(sData.getBytes());

        return mdReturn;
    }
}
