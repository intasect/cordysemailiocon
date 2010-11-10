package com.cordys.coe.ac.emailio.util;

import com.cordys.coe.ac.emailio.EmailIOConnectorConstants;
import com.cordys.coe.ac.emailio.localization.LogMessages;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import java.util.Enumeration;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Header;
import javax.mail.Message;

import javax.mail.Message.RecipientType;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;

import javax.mail.internet.MimeMessage;

/**
 * This class contains utility functions for email messages.
 *
 * @author  pgussow
 */
public class MailMessageUtil
{
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(MailMessageUtil.class);
    /**
     * Identifies an empty string.
     */
    private static final String EMPTY_STRING = "";
    /**
     * Holds the dummy mail session to be able to read back .eml files.
     */
    private static Session s_sDummyMailSession;

    static
    {
        // In order to be able to reload .eml file we need a dummy session.
        Properties props = System.getProperties();
        props.setProperty("mail.smtp.host", "dummy");
        props.put("mail.transport.protocol", "smtp");

        s_sDummyMailSession = Session.getDefaultInstance(props, null);
    }

    /**
     * This method creates a new Email message from the RFC string using a dummy session.
     *
     * @param   sMailContent  The raw content of a mail.
     *
     * @return  The mail message object.
     *
     * @throws  MessagingException  In case the creation of the mail object failed.
     */
    public static MimeMessage createMimeMessage(String sMailContent)
                                         throws MessagingException
    {
        return createMimeMessage(new ByteArrayInputStream(sMailContent.getBytes()));
    }

    /**
     * This method creates a new Email message from the RFC string using a dummy session.
     *
     * @param   isMailContent  The raw content of a mail.
     *
     * @return  The mail message object.
     *
     * @throws  MessagingException  In case the creation of the mail object failed.
     */
    public static MimeMessage createMimeMessage(InputStream isMailContent)
                                         throws MessagingException
    {
        return new MimeMessage(getDummySession(), isMailContent);
    }

    /**
     * This method creates a new Email message from the RFC string using a dummy session.
     *
     * @param   fMail  The raw content of a mail.
     *
     * @return  The mail message object.
     *
     * @throws  FileNotFoundException  In case the source file was not found.
     * @throws  MessagingException     In case the creation of the mail object failed.
     */
    public static MimeMessage createMimeMessage(File fMail)
                                         throws FileNotFoundException, MessagingException
    {
        return createMimeMessage(new FileInputStream(fMail));
    }

    /**
     * This method dumps the list of addresses to a string.
     *
     * @param   aaAddresses  The list of addresses to dump.
     *
     * @return  The string containing all addresses.
     */
    public static String dumpAddressList(Address[] aaAddresses)
    {
        StringBuffer sbReturn = new StringBuffer(1024);

        for (int iCount = 0; iCount < aaAddresses.length; iCount++)
        {
            Address aAddress = aaAddresses[iCount];
            sbReturn.append(aAddress.toString());

            if (iCount < (aaAddresses.length - 1))
            {
                sbReturn.append("; ");
            }
        }
        return sbReturn.toString();
    }

    /**
     * This method writes all data of a mail to a nicely formatted stream.
     *
     * @param   mMessage  The message to dump.
     *
     * @return  The formatted message.
     */
    public static String dumpMessage(Message mMessage)
    {
        return dumpMessage("", mMessage);
    }

    /**
     * This method writes all data of a mail to a nicely formatted stream.
     *
     * @param   sIdent    The ident for the nice formatter.
     * @param   mMessage  The message to dump.
     *
     * @return  The formatted message.
     */
    public static String dumpMessage(String sIdent, Message mMessage)
    {
        StringBuffer sbReturn = new StringBuffer(1024);

        try
        {
            if (mMessage.isExpunged() || mMessage.isSet(Flags.Flag.DELETED))
            {
                sbReturn.append("Message has been deleted");
            }
            else
            {
                // From
                try
                {
                    Address[] aa = mMessage.getFrom();

                    if ((aa != null) && (aa.length > 0))
                    {
                        sbReturn.append(sIdent + "    From: ");
                        sbReturn.append(dumpAddressList(aa));
                    }
                }
                catch (MessagingException e)
                {
                    // Ignore it.
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Error printing from address.", e);
                    }
                }

                // To
                try
                {
                    Address[] aa = mMessage.getRecipients(RecipientType.TO);

                    if ((aa != null) && (aa.length > 0))
                    {
                        sbReturn.append("\n" + sIdent + "      To: ");
                        sbReturn.append(dumpAddressList(aa));
                    }
                }
                catch (MessagingException e)
                {
                    // Ignore it.
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Error printing to address.", e);
                    }
                }

                // CC
                try
                {
                    Address[] aa = mMessage.getRecipients(RecipientType.CC);

                    if ((aa != null) && (aa.length > 0))
                    {
                        sbReturn.append("\n" + sIdent + "      CC: ");
                        sbReturn.append(dumpAddressList(aa));
                    }
                }
                catch (MessagingException e)
                {
                    // Ignore it.
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Error printing CC address.", e);
                    }
                }

                // BCC
                try
                {
                    Address[] aa = mMessage.getRecipients(RecipientType.BCC);

                    if ((aa != null) && (aa.length > 0))
                    {
                        sbReturn.append("\n" + sIdent + "     BCC: ");
                        sbReturn.append(dumpAddressList(aa));
                    }
                }
                catch (MessagingException e)
                {
                    // Ignore it.
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Error printing BCC address.", e);
                    }
                }

                // Reply to
                try
                {
                    Address[] aa = mMessage.getReplyTo();

                    if ((aa != null) && (aa.length > 0))
                    {
                        sbReturn.append("\n" + sIdent + "Reply-to: ");
                        sbReturn.append(dumpAddressList(aa));
                    }
                }
                catch (MessagingException e)
                {
                    // Ignore it.
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Error printing to address.", e);
                    }
                }

                try
                {
                    sbReturn.append("\n" + sIdent + " Subject: ");
                    sbReturn.append(mMessage.getSubject());
                }
                catch (MessagingException e)
                {
                    // Ignore it.
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Error printing subject.", e);
                    }
                }

                try
                {
                    sbReturn.append("\n" + sIdent + "Send date: ");
                    sbReturn.append(mMessage.getSentDate());
                    sbReturn.append("\n" + sIdent + "Receive date: ");
                    sbReturn.append(mMessage.getReceivedDate());
                }
                catch (MessagingException e)
                {
                    // Ignore it.
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Error printing subject.", e);
                    }
                }

                // Do all headers
                try
                {
                    Enumeration<?> eTemp = mMessage.getAllHeaders();

                    if (eTemp.hasMoreElements())
                    {
                        sbReturn.append("\n" + sIdent + "Headers:\n");
                        sbReturn.append("" + sIdent + "--------\n");

                        while (eTemp.hasMoreElements())
                        {
                            Header hHeader = (Header) eTemp.nextElement();
                            sbReturn.append(sIdent + "- " + hHeader.getName() + ": " +
                                            hHeader.getValue() + "\n");
                        }
                    }
                }
                catch (Exception e)
                {
                    // Ignore it.
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Error printing subject.", e);
                    }
                }

                try
                {
                    sbReturn.append("\n" + sIdent + "    Body:\n");

                    Object oContent = mMessage.getContent();

                    if (oContent instanceof String)
                    {
                        sbReturn.append("" + sIdent + "--== Body is of type text ==--\n");
                        sbReturn.append((String) oContent);
                    }
                    else if (oContent instanceof Multipart)
                    {
                        Multipart mp = (Multipart) oContent;
                        sbReturn.append(dumpMultipart(sIdent + "\t", mp));
                    }
                    else if (oContent instanceof Message)
                    {
                        // message content could be a message itself
                        sbReturn.append("" + sIdent +
                                        "====---- START OF NESTED MESSAGE ----====\n");
                        sbReturn.append(sIdent + dumpMessage((Message) oContent));
                        sbReturn.append("" + sIdent + "====---- END OF NESTED MESSAGE ----====\n");
                    }
                }
                catch (Exception e)
                {
                    // Ignore it.
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Error printing subject.", e);
                    }
                }
            }
        }
        catch (MessagingException e)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Error checking if message is expunged.", e);
            }
        }

        return sbReturn.toString();
    }

    /**
     * This method dumps the content of the multipart to string.
     *
     * @param   sIdent  The number of spaces to ident.
     * @param   mp      The actual multipart.
     *
     * @return  The string containing the content of the multipart.
     */
    public static String dumpMultipart(String sIdent, Multipart mp)
    {
        StringBuffer sbReturn = new StringBuffer(1024);

        sbReturn.append(sIdent + "--> Body is of type multi part");

        try
        {
            for (int iCount = 0; iCount < mp.getCount(); iCount++)
            {
                BodyPart bpBody = mp.getBodyPart(iCount);
                sbReturn.append("\n" + sIdent + " -> Body part " + iCount);

                try
                {
                    Enumeration<?> eTemp = bpBody.getAllHeaders();

                    if (eTemp.hasMoreElements())
                    {
                        sbReturn.append("\n" + sIdent + "\tHeaders\n");

                        while (eTemp.hasMoreElements())
                        {
                            Header hHeader = (Header) eTemp.nextElement();
                            sbReturn.append(sIdent + "\t- " + hHeader.getName() + ": " +
                                            hHeader.getValue() + "\n");
                        }
                    }
                }
                catch (Exception e)
                {
                    // Ignore it.
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Error printing body part headers.", e);
                    }
                }

                // Now see the content.
                Object oContent = bpBody.getContent();

                if (oContent instanceof String)
                {
                    sbReturn.append(sIdent + "--> Body is of type text \n");
                    sbReturn.append((String) oContent);
                }
                else if (oContent instanceof Multipart)
                {
                    Multipart mpTemp = (Multipart) oContent;
                    sbReturn.append(dumpMultipart(sIdent + "\t", mpTemp));
                }
                else if (oContent instanceof Message)
                {
                    // message content could be a message itself
                    sbReturn.append(sIdent + "\t---- START OF NESTED MESSAGE ----====\n");
                    sbReturn.append(dumpMessage(sIdent + "\t", (Message) oContent));
                    sbReturn.append(sIdent + "\t---- END OF NESTED MESSAGE ----====\n");
                }
            }
        }
        catch (Exception e)
        {
            // Ignore it.
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Error getting multipart.", e);
            }
        }

        return sbReturn.toString();
    }

    /**
     * This method gets the dummy mail session to be able to recreate email objects from .eml files.
     *
     * @return  The dummy mail session to be able to recreate email objects from .eml files.
     */
    public static Session getDummySession()
    {
        return s_sDummyMailSession;
    }

    /**
     * This method dumps the current email message to XML.
     *
     * @param  mMessage  The message to dump.
     * @param  iParent   The parent to dump.
     */
    public static void messageToXML(Message mMessage, int iParent)
    {
        Document dDoc = Node.getDocument(iParent);
        int iRoot = iParent;

        try
        {
            if (mMessage.isExpunged() || mMessage.isSet(Flags.Flag.DELETED))
            {
                dDoc.createElementWithParentNS("status", "Message has been deleted", iParent);
            }
            else
            {
                // From
                try
                {
                    Address[] aa = mMessage.getFrom();

                    if ((aa != null) && (aa.length > 0))
                    {
                        int iFrom = dDoc.createElementWithParentNS("from", EMPTY_STRING, iRoot);
                        messageToXMLAddressList(aa, iFrom);
                    }
                }
                catch (MessagingException e)
                {
                    // Ignore it.
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Error printing from address.", e);
                    }
                }

                // To
                try
                {
                    Address[] aa = mMessage.getRecipients(RecipientType.TO);

                    if ((aa != null) && (aa.length > 0))
                    {
                        int iTo = dDoc.createElementWithParentNS("to", EMPTY_STRING, iRoot);
                        messageToXMLAddressList(aa, iTo);
                    }
                }
                catch (MessagingException e)
                {
                    // Ignore it.
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Error printing to address.", e);
                    }
                }

                // CC
                try
                {
                    Address[] aa = mMessage.getRecipients(RecipientType.CC);

                    if ((aa != null) && (aa.length > 0))
                    {
                        int iCC = dDoc.createElementWithParentNS("cc", EMPTY_STRING, iRoot);
                        messageToXMLAddressList(aa, iCC);
                    }
                }
                catch (MessagingException e)
                {
                    // Ignore it.
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Error printing CC address.", e);
                    }
                }

                // BCC
                try
                {
                    Address[] aa = mMessage.getRecipients(RecipientType.BCC);

                    if ((aa != null) && (aa.length > 0))
                    {
                        int iBCC = dDoc.createElementWithParentNS("bcc", EMPTY_STRING, iRoot);
                        messageToXMLAddressList(aa, iBCC);
                    }
                }
                catch (MessagingException e)
                {
                    // Ignore it.
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Error printing BCC address.", e);
                    }
                }

                // Reply to
                try
                {
                    Address[] aa = mMessage.getReplyTo();

                    if ((aa != null) && (aa.length > 0))
                    {
                        int iReplyTo = dDoc.createElementWithParentNS("replyto", EMPTY_STRING,
                                                                      iRoot);
                        messageToXMLAddressList(aa, iReplyTo);
                    }
                }
                catch (MessagingException e)
                {
                    // Ignore it.
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Error printing to address.", e);
                    }
                }

                try
                {
                    dDoc.createElementWithParentNS("subject", mMessage.getSubject(), iRoot);
                }
                catch (MessagingException e)
                {
                    // Ignore it.
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Error printing subject.", e);
                    }
                }

                try
                {
                    if (mMessage.getSentDate() != null)
                    {
                        dDoc.createElementWithParentNS("senddate",
                                                       mMessage.getSentDate().toString(), iRoot);
                    }

                    if (mMessage.getReceivedDate() != null)
                    {
                        dDoc.createElementWithParentNS("receiveddate",
                                                       mMessage.getReceivedDate().toString(),
                                                       iRoot);
                    }
                }
                catch (MessagingException e)
                {
                    // Ignore it.
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Error printing subject.", e);
                    }
                }

                // Do all headers
                try
                {
                    Enumeration<?> eTemp = mMessage.getAllHeaders();

                    if (eTemp.hasMoreElements())
                    {
                        int iHeaders = dDoc.createElementWithParentNS("headers", EMPTY_STRING,
                                                                      iRoot);

                        while (eTemp.hasMoreElements())
                        {
                            Header hHeader = (Header) eTemp.nextElement();

                            int iHeader = dDoc.createElementWithParentNS("header", EMPTY_STRING,
                                                                         iHeaders);
                            dDoc.createElementWithParentNS("name", hHeader.getName(), iHeader);
                            dDoc.createElementWithParentNS("value", hHeader.getValue(), iHeader);
                        }
                    }
                }
                catch (Exception e)
                {
                    // Ignore it.
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Error printing subject.", e);
                    }
                }

                try
                {
                    int iBody = dDoc.createElementWithParentNS("body", EMPTY_STRING, iRoot);

                    Object oContent = mMessage.getContent();

                    if (oContent instanceof String)
                    {
                        dDoc.createText((String) oContent, iBody);
                    }
                    else if (oContent instanceof Multipart)
                    {
                        Multipart mp = (Multipart) oContent;
                        messageToXMLMultipart(mp, iBody);
                    }
                    else if (oContent instanceof Message)
                    {
                        // message content could be a message itself
                        int iMessage = dDoc.createElementWithParentNS("email", EMPTY_STRING, iBody);
                        messageToXML((Message) oContent, iMessage);
                    }
                }
                catch (Exception e)
                {
                    // Ignore it.
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Error printing subject.", e);
                    }
                }
            }
        }
        catch (MessagingException e)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Error checking if message is expunged.", e);
            }
        }
    }

    /**
     * This method dumps the given email to XML format.
     *
     * @param   mMessage  The message to dump.
     * @param   dDoc      The XML document to use.
     *
     * @return  The newly created XML structure.
     */
    public static int messageToXML(Message mMessage, Document dDoc)
    {
        int iReturn = dDoc.createElementNS("email", "", "",
                                           EmailIOConnectorConstants.NS_CONFIGURATION, 0);

        messageToXML(mMessage, iReturn);

        return iReturn;
    }

    /**
     * This method dumps the raw message to a string.
     *
     * @param   mMessage  The email message.
     *
     * @return  The raw content.
     */
    public static String rawMessage(Message mMessage)
    {
        String sReturn = null;

        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(mMessage.getSize());
            mMessage.writeTo(baos);
            sReturn = baos.toString();
        }
        catch (Exception e)
        {
            LOG.error(e, LogMessages.ERROR_DUMPING_EMAIL_MESSAGE);
        }

        return sReturn;
    }

    /**
     * This methdo dumps the passed on address list to XML.
     *
     * @param  aaAddresses  The address list.
     * @param  iParent      The parent XML node.
     */
    private static void messageToXMLAddressList(Address[] aaAddresses, int iParent)
    {
        for (int iCount = 0; iCount < aaAddresses.length; iCount++)
        {
            Address aAddress = aaAddresses[iCount];
            Node.createElementWithParentNS("address", aAddress.toString(), iParent);
        }
    }

    /**
     * This method dumps the multipart message.
     *
     * @param  mp       The multi part to dump.
     * @param  iParent  The parent node.
     */
    private static void messageToXMLMultipart(Multipart mp, int iParent)
    {
        Document dDoc = Node.getDocument(iParent);
        int iRoot = iParent;

        try
        {
            for (int iCount = 0; iCount < mp.getCount(); iCount++)
            {
                BodyPart bpBody = mp.getBodyPart(iCount);

                int iPart = dDoc.createElementWithParentNS("part", EMPTY_STRING, iRoot);
                Node.setAttribute(iPart, "id", String.valueOf(iCount));

                try
                {
                    Enumeration<?> eTemp = bpBody.getAllHeaders();

                    if (eTemp.hasMoreElements())
                    {
                        int iHeaders = dDoc.createElementWithParentNS("headers", EMPTY_STRING,
                                                                      iPart);

                        while (eTemp.hasMoreElements())
                        {
                            Header hHeader = (Header) eTemp.nextElement();

                            int iHeader = dDoc.createElementWithParentNS("header", EMPTY_STRING,
                                                                         iHeaders);
                            dDoc.createElementWithParentNS("name", hHeader.getName(), iHeader);
                            dDoc.createElementWithParentNS("value", hHeader.getValue(), iHeader);
                        }
                    }
                }
                catch (Exception e)
                {
                    // Ignore it.
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Error printing body part headers.", e);
                    }
                }

                // Now see the content.
                Object oContent = bpBody.getContent();
                int iBody = dDoc.createElementWithParentNS("body", EMPTY_STRING, iPart);

                if (oContent instanceof String)
                {
                    String sContentType = bpBody.getContentType();

                    if ((sContentType != null) && (sContentType.length() > 0))
                    {
                        if (sContentType.startsWith("text/xml"))
                        {
                            int iParsed = dDoc.parseString((String) oContent);
                            Node.appendToChildren(iParsed, iBody);
                        }
                        else
                        {
                            dDoc.createText((String) oContent, iBody);
                        }
                    }
                    else
                    {
                        dDoc.createText((String) oContent, iBody);
                    }
                }
                else if (oContent instanceof Multipart)
                {
                    Multipart mpTemp = (Multipart) oContent;
                    messageToXMLMultipart(mpTemp, iBody);
                }
                else if (oContent instanceof Message)
                {
                    // message content could be a message itself
                    int iMessage = dDoc.createElementWithParentNS("email", EMPTY_STRING, iBody);
                    messageToXML((Message) oContent, iMessage);
                }
            }
        }
        catch (Exception e)
        {
            // Ignore it.
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Error getting multipart.", e);
            }
        }
    }
}
