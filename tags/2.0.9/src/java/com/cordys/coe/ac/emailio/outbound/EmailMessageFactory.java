package com.cordys.coe.ac.emailio.outbound;

import com.cordys.coe.ac.emailio.config.outbound.IEmailAddress;
import com.cordys.coe.ac.emailio.config.outbound.IHeader;
import com.cordys.coe.ac.emailio.config.outbound.IMailData;
import com.cordys.coe.ac.emailio.config.outbound.IMultiPart;
import com.cordys.coe.ac.emailio.config.outbound.ISendMailData;
import com.cordys.coe.ac.emailio.exception.KeyManagerException;
import com.cordys.coe.ac.emailio.exception.OutboundEmailException;
import com.cordys.coe.ac.emailio.keymanager.ICertificateInfo;
import com.cordys.coe.ac.emailio.localization.OutboundEmailExceptionMessages;
import com.cordys.coe.ac.emailio.util.StringUtil;

import com.eibus.util.logger.CordysLogger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.security.PrivateKey;

import java.security.cert.CertStore;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.X509Certificate;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.activation.DataHandler;

import javax.mail.Address;

import javax.mail.Message.RecipientType;

import javax.mail.MessagingException;
import javax.mail.Session;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import javax.mail.util.ByteArrayDataSource;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import org.bouncycastle.asn1.smime.SMIMECapabilitiesAttribute;
import org.bouncycastle.asn1.smime.SMIMECapability;
import org.bouncycastle.asn1.smime.SMIMECapabilityVector;
import org.bouncycastle.asn1.smime.SMIMEEncryptionKeyPreferenceAttribute;
import org.bouncycastle.asn1.x509.X509Name;

import org.bouncycastle.mail.smime.SMIMEEnvelopedGenerator;
import org.bouncycastle.mail.smime.SMIMESignedGenerator;

import org.bouncycastle.util.Strings;

/**
 * This factory class is the link between the configuration data and the actual JavaMail API. Based
 * on the configuration it will create the corresponding mime message.
 *
 * @author  pgussow
 */
public class EmailMessageFactory
{
    /**
     * Holds the logger to use.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(EmailMessageFactory.class);

    /**
     * This method creates the actual MimeMessages that should be sent. First of all the
     * MimeBodypart is created. If signing is needed, the signing will take place next. When the
     * signing is done the actual Mail Message is constructed including all headers passed on.
     * Optionally the MimeMessage is encrypted as well.
     *
     * @param   smdData   The send mail data that contains the details for the actual mail.
     * @param   scConfig  The configuration to use for creating the new mail.
     * @param   sSession  The JavaMail API session to use.
     *
     * @return  The list of messages that have to be send to the mail server.
     *
     * @throws  OutboundEmailException  In case of any exceptions.
     */
    public static List<MimeMessage> createProperMimeMessages(ISendMailData smdData,
                                                             ISMIMEConfiguration scConfig,
                                                             Session sSession)
                                                      throws OutboundEmailException
    {
        List<MimeMessage> lmmReturn = new ArrayList<MimeMessage>();

        try
        {
            // Instead of directly building up the MimeMessage, we'll build up a bodypart. This is
            // easier IF it turns out that signing is required.
            MimeMessage mmFinal = new MimeMessage(sSession);

            if (smdData.hasData())
            {
                // The mail has a flat structure with only 1 block of data to send.
                IMailData mdData = smdData.getMailData();

                ByteArrayDataSource bads = new ByteArrayDataSource(mdData.getData(),
                                                                   mdData.getContentType());
                mmFinal.setDataHandler(new DataHandler(bads));

                if (StringUtil.isSet(mdData.getContentDisposition()))
                {
                    mmFinal.addHeader("Content-Disposition", mdData.getContentDisposition());
                }
            }
            else
            {
                // It consists of multiparts, so lets create them
                IMultiPart mpPart = smdData.getMultiPart();

                // Create the multipart to use.
                MimeMultipart mmMultiPart = new MimeMultipart(mpPart.getSubType());

                // Process all nested parts.
                processNestedParts(mmMultiPart, mpPart.getMultiParts());

                // Now add it to the body part.
                mmFinal.setContent(mmMultiPart);
            }

            // Now we need to determine whether or not the mail needs to be signed.
            if (scConfig.getSMIMEEnabled() && smdData.getSendOptions().getSignMail())
            {
                mmFinal = signMessage(mmFinal, scConfig, sSession,
                                      smdData.getFrom().getEmailAddress());
            }

            // Now we can add the additional headers to the mail and set the from,to, cc and bcc.
            mmFinal.setFrom(smdData.getFrom().getInternetAddress());

            if (smdData.getReplyTo() != null)
            {
                mmFinal.setReplyTo(new Address[] { smdData.getReplyTo().getInternetAddress() });
            }

            // Set the recipients.
            processRecipients(RecipientType.TO, smdData.getTo(), mmFinal);
            processRecipients(RecipientType.CC, smdData.getCC(), mmFinal);
            processRecipients(RecipientType.BCC, smdData.getBCC(), mmFinal);

            // Do the subject
            mmFinal.setSubject(smdData.getSubject());
            mmFinal.setSentDate(new Date());

            // Do the additional headers. Only the main part has complete freedom according to
            // the standards.
            IHeader[] ahHeaders = smdData.getHeaders();

            for (IHeader hHeader : ahHeaders)
            {
                mmFinal.addHeader(hHeader.getName(), hHeader.getValue());
            }

            // Make sure the object is stored properly.
            mmFinal.saveChanges();

            // Final step: do the encryption if needed.
            if (scConfig.getSMIMEEnabled() && smdData.getSendOptions().getEncryptMail())
            {
                encryptMessage(lmmReturn, mmFinal, scConfig, sSession);
            }
            else
            {
                lmmReturn.add(mmFinal);
            }
        }
        catch (Exception e)
        {
            throw new OutboundEmailException(e,
                                             OutboundEmailExceptionMessages.OEE_ERROR_CREATING_MIME_MESSAGE_FOR_EMAIL_DATA);
        }
        return lmmReturn;
    }

    /**
     * This method encrypt the given email using the public keys for all senders.
     *
     * @param   lMessages         The list to add all individual messages to.
     * @param   mmMessage         The message to encrypt.
     * @param   eicConfiguration  The configuration of the connector.
     * @param   sSession          The JavaMail session to use.
     *
     * @throws  OutboundEmailException  In case of any exceptions.
     */
    private static void encryptMessage(List<MimeMessage> lMessages, MimeMessage mmMessage,
                                       ISMIMEConfiguration eicConfiguration, Session sSession)
                                throws OutboundEmailException
    {
        // Create the encrypter object
        SMIMEEnvelopedGenerator encrypter = new SMIMEEnvelopedGenerator();

        try
        {
            // Add the public keys of all receivers to the encrypter.
            Address[] aaAdresses = mmMessage.getAllRecipients();

            for (Address address : aaAdresses)
            {
                InternetAddress ia = (InternetAddress) address;

                // Find the public key for the given email address.
                ICertificateInfo ciRecipient = eicConfiguration.getCertificateInfo(ia.getAddress());

                if ((ciRecipient == null) && !eicConfiguration.getBypassSMIME())
                {
                    throw new OutboundEmailException(OutboundEmailExceptionMessages.OEE_COULD_NOT_FIND_THE_PUBLIC_KEY_FOR_THE_EMAIL_ADDRESS_0,
                                                     ia.toString());
                }

                if (ciRecipient != null)
                {
                    encrypter.addKeyTransRecipient(ciRecipient.getX509Certificate());
                }
                else
                {
                    // Now we could have a funny situation. The following might happen: a mail has
                    // to send to 3 recipients. 1 has no certificate and the bypasssmime is enabled.
                    // What to do now? We need to create a new version of the mail in plain text and
                    // remove all recipients except this one. NOTE: We cannot avoid the recipient
                    // getting 2 mails: 1 unreadable version and 1 plain one. This is beacuse we
                    // cannot remove the recipient from the original message because that message
                    // has been signed.
                    if (eicConfiguration.getBypassSMIME())
                    {
                        if (LOG.isDebugEnabled())
                        {
                            LOG.debug("BypassSMIME is enabled, so going to send a plain text version for user " +
                                      ia);
                        }

                        MimeMessage mmPlain = new MimeMessage(mmMessage);
                        mmPlain.setRecipient(RecipientType.TO, ia);
                        mmPlain.saveChanges();
                        lMessages.add(mmPlain);
                    }
                    else
                    {
                        // We need to throw an error since we cannot encrypt to all users.
                        throw new OutboundEmailException(OutboundEmailExceptionMessages.OEE_COULD_NOT_FIND_A_CERTIFICATE_FOR_RECIPIENT_0,
                                                         ia.toString());
                    }
                }
            }

            // Encrypt the message
            MimeBodyPart encryptedPart = encrypter.generate(mmMessage,
                                                            SMIMEEnvelopedGenerator.DES_EDE3_CBC,
                                                            "BC");

            // Create a new MimeMessage that contains the encrypted and signed content
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            encryptedPart.writeTo(out);

            MimeMessage encryptedMessage = new MimeMessage(sSession,
                                                           new ByteArrayInputStream(out.toByteArray()));

            // Set all original MIME headers in the encrypted message
            Enumeration<?> headers = mmMessage.getAllHeaderLines();

            while (headers.hasMoreElements())
            {
                String headerLine = (String) headers.nextElement();

                // Make sure not to override any content-* headers from the original message
                if (!Strings.toLowerCase(headerLine).startsWith("content-"))
                {
                    encryptedMessage.addHeaderLine(headerLine);
                }
            }

            // Add the encrypted message to the list so that it will be sent.
            lMessages.add(encryptedMessage);
        }
        catch (Exception e)
        {
            throw new OutboundEmailException(e,
                                             OutboundEmailExceptionMessages.OEE_ERROR_ENCRYPTING_THE_MAIL);
        }
    }

    /**
     * This method parses the given multiparts into MimeBody parts and adds them to the given
     * MimeMultiPart object.
     *
     * @param   mmpParent  The parent MimeMultipart which will hold the body parts.
     * @param   ampParts   The actual parts that need to be created.
     *
     * @throws  MessagingException  In case of any email related exceptions.
     * @throws  IOException         In case of any errors creating the Data Source.
     */
    private static void processNestedParts(MimeMultipart mmpParent, IMultiPart[] ampParts)
                                    throws MessagingException, IOException
    {
        for (IMultiPart mpData : ampParts)
        {
            // Create the body part for this multi part.
            // The multipart can contain either data or again a nested multipart.
            MimeBodyPart mbp = new MimeBodyPart();

            if (mpData.hasNestedParts())
            {
                // Create the nested multipart which will hold the individual multi-body parts.
                MimeMultipart mmpNested = new MimeMultipart(mpData.getSubType());

                // Process the nested mime parts.
                processNestedParts(mmpNested, mpData.getMultiParts());

                // Set the content of the current body part to the new nested multipart.
                mbp.setContent(mmpNested);
            }
            else
            {
                // We have actual content here. So create the proper data source handler for it.
                IMailData mdMailData = mpData.getMailData();
                ByteArrayDataSource bads = new ByteArrayDataSource(mdMailData.getData(),
                                                                   mdMailData.getContentType());
                mbp.setDataHandler(new DataHandler(bads));

                if (StringUtil.isSet(mdMailData.getContentDisposition()))
                {
                    mbp.addHeader("Content-Disposition", mdMailData.getContentDisposition());
                }
            }

            mmpParent.addBodyPart(mbp);
        }
    }

    /**
     * This method sets the proper recipients.
     *
     * @param   rt           The type for the list.
     * @param   aeAddresses  The list of addresses to add.
     * @param   mmMessage    The message to add the recipients to.
     *
     * @throws  MessagingException  In case of any exceptions.
     */
    private static void processRecipients(RecipientType rt, IEmailAddress[] aeAddresses,
                                          MimeMessage mmMessage)
                                   throws MessagingException
    {
        if (aeAddresses != null)
        {
            Address[] aaAddresses = new Address[aeAddresses.length];

            for (int iCount = 0; iCount < aeAddresses.length; iCount++)
            {
                aaAddresses[iCount] = aeAddresses[iCount].getInternetAddress();
            }
            mmMessage.addRecipients(rt, aaAddresses);
        }
    }

    /**
     * This method creates and returns a signed version of the given mail.
     *
     * @param   mbpToBeSigned     The message to sign.
     * @param   eicConfiguration  The configuration to use.
     * @param   sSession          The main session to use.
     * @param   sSenderAddress    The email address of the sender.
     *
     * @return  The signed message to return.
     *
     * @throws  OutboundEmailException  In case of any exceptions.
     * @throws  KeyManagerException     In case of any key manager related exceptions.
     */
    private static MimeMessage signMessage(MimeMessage mbpToBeSigned,
                                           ISMIMEConfiguration eicConfiguration, Session sSession,
                                           String sSenderAddress)
                                    throws OutboundEmailException, KeyManagerException
    {
        MimeMessage mmReturn = null;

        // Use the address to find the proper private key.
        PrivateKey pkKey = null;
        ICertificateInfo ciInfo = eicConfiguration.getCertificateInfo(sSenderAddress);

        if (ciInfo != null)
        {
            pkKey = ciInfo.getKey();
        }

        if ((pkKey == null) && !eicConfiguration.getBypassSMIME())
        {
            throw new OutboundEmailException(OutboundEmailExceptionMessages.OEE_COULD_NOT_FIND_A_PRIVATE_KEY_FOR_EMAIL_ADDRESS_0,
                                             sSenderAddress);
        }
        else
        {
            mmReturn = mbpToBeSigned;
        }

        // Create the signed message if possible. If no private key was found and bypassing S/MIME
        // is allowed the original message is returned.
        if (pkKey != null)
        {
            try
            {
                // Get the public key.
                X509Certificate xcPublic = ciInfo.getX509Certificate();

                // Create the SMIME capabilities
                SMIMECapabilityVector capabilities = new SMIMECapabilityVector();
                capabilities.addCapability(SMIMECapability.dES_EDE3_CBC);
                capabilities.addCapability(SMIMECapability.rC2_CBC, 128);
                capabilities.addCapability(SMIMECapability.dES_CBC);

                // Create the signing preferences.
                ASN1EncodableVector attributes = new ASN1EncodableVector();
                X509Name name = new X509Name(xcPublic.getIssuerDN().getName());
                IssuerAndSerialNumber issuerAndSerialNumber = new IssuerAndSerialNumber(name,
                                                                                        xcPublic
                                                                                        .getSerialNumber());
                SMIMEEncryptionKeyPreferenceAttribute encryptionKeyPreferenceAttribute = new SMIMEEncryptionKeyPreferenceAttribute(issuerAndSerialNumber);
                attributes.add(encryptionKeyPreferenceAttribute);
                attributes.add(new SMIMECapabilitiesAttribute(capabilities));

                // Create the signature generator.
                SMIMESignedGenerator signer = new SMIMESignedGenerator();
                signer.addSigner(pkKey, xcPublic,
                                 "DSA".equals(pkKey.getAlgorithm())
                                 ? SMIMESignedGenerator.DIGEST_SHA1
                                 : SMIMESignedGenerator.DIGEST_MD5, new AttributeTable(attributes),
                                 null);

                // Create the list of certificates that will be sent along with the signature. Right
                // now the CA certificate will NOT be sent along with the mail. It is expected that
                // the receiver is capable of verifying the authenticity of the certificate itself.
                List<X509Certificate> certList = new ArrayList<X509Certificate>();
                certList.add(xcPublic);

                CertStore certs = CertStore.getInstance("Collection",
                                                        new CollectionCertStoreParameters(certList),
                                                        "BC");
                signer.addCertificatesAndCRLs(certs);

                // Sign the actual message

                // The message that was created will ALWAYS have a multipart. In order to keep it
                // readable in ALL clients we will sign the content of the message, not the whole
                // message.
                MimeMultipart mm = signer.generate(mbpToBeSigned, "BC");
                mmReturn = new MimeMessage(sSession);

                // Set the content of the signed message
                mmReturn.setContent(mm);
                mmReturn.saveChanges();
            }
            catch (Exception e)
            {
                throw new OutboundEmailException(e,
                                                 OutboundEmailExceptionMessages.OEE_ERROR_SIGNING_EMAIL_MESSAGE);
            }
        }
        else if (LOG.isDebugEnabled())
        {
            LOG.debug("Bypassing S/MIME because no private key was found for " + sSenderAddress);
        }

        return mmReturn;
    }
}
