/**
 * Copyright 2005 Cordys R&D B.V. 
 * 
 * This file is part of the Cordys SAP Connector. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 /**
 * Copyright 2005 Cordys R&D B.V. 
 * 
 * This file is part of the Cordys SAP Connector. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package com.cordys.coe.ac.emailio.util.smime;

import com.cordys.coe.ac.emailio.exception.EmailIOException;
import com.cordys.coe.ac.emailio.keymanager.ICertificateInfo;
import com.cordys.coe.ac.emailio.localization.EmailIOExceptionMessages;
import com.cordys.coe.ac.emailio.localization.LogMessages;
import com.cordys.coe.ac.emailio.outbound.ISMIMEConfiguration;
import com.cordys.coe.ac.emailio.util.MailMessageUtil;
import com.cordys.coe.ac.emailio.util.StringUtil;

import com.eibus.util.logger.CordysLogger;

import java.security.Key;

import java.security.cert.CertStore;
import java.security.cert.X509Certificate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.mail.BodyPart;
import javax.mail.MessagingException;

import javax.mail.internet.ContentType;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;

import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.RecipientInformationStore;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;

import org.bouncycastle.mail.smime.SMIMEEnveloped;
import org.bouncycastle.mail.smime.SMIMESigned;

/**
 * This class contains utility methods around the S/MIME support.
 *
 * @author  pgussow
 */
public class SMIMEUtil
{
    /**
     * Holds the logger to use.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(SMIMEUtil.class);
    /**
     * Holds the main type that identifies a multi part as being a signed one.
     */
    private static final String SIGNED_TYPE = "multipart/signed";
    /**
     * Holds all the SMIME main content types.
     */
    private static ArrayList<String> s_alSMIMEEncryptionTypes = new ArrayList<String>();
    /**
     * Holds all the SMIME protocols used for signing.
     */
    private static ArrayList<String> s_alSMIMESignTypes = new ArrayList<String>();

    static
    {
        // These 2 types identify an encrypted message using S/MIME.
        s_alSMIMEEncryptionTypes.add("application/pkcs7-mime");
        s_alSMIMEEncryptionTypes.add("application/x-pkcs7-mime");

        // These 2 protocols identify the signature.
        s_alSMIMESignTypes.add("application/pkcs7-signature");
        s_alSMIMESignTypes.add("application/x-pkcs7-signature");
    }

    /**
     * This method will decrypt the source message and return the decrypted version of the mail.
     *
     * @param   mmSource  The message to decrypt.
     * @param   scConfig  The S/MIME configuration details.
     *
     * @return  The decrypted message.
     *
     * @throws  EmailIOException  In case of any exceptions.
     */
    public static MimeMessage decryptMessage(MimeMessage mmSource, ISMIMEConfiguration scConfig)
                                      throws EmailIOException
    {
        MimeMessage mmReturn = null;

        // First we create the wrapper around the encrypted message
        try
        {
            SMIMEEnveloped seSMIME = new SMIMEEnveloped(mmSource);

            // Now get all recipients for this mail and try to find the matching certificate with
            // the private key.
            RecipientInformationStore recipients = seSMIME.getRecipientInfos();

            ICertificateInfo ciInfo = null;
            RecipientInformation riInfo = null;

            Collection<?> cRecipients = recipients.getRecipients();

            for (Iterator<?> iRecipients = cRecipients.iterator(); iRecipients.hasNext();)
            {
                riInfo = (RecipientInformation) iRecipients.next();

                // Figure out if we have a key for this recipient ID.
                ciInfo = scConfig.getCertificateInfo(riInfo.getRID());

                if (ciInfo != null)
                {
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Found certificate info for RID " + riInfo.getRID());
                    }

                    break;
                }
            }

            // Check if we found all information.
            if (ciInfo == null)
            {
                throw new EmailIOException(EmailIOExceptionMessages.EIOE_COULD_NOT_FIND_ANY_CERTIFICATE_INFORMATION_FOR_ANY_OF_THE_RECIPIENTS);
            }

            if (riInfo == null)
            {
                throw new EmailIOException(EmailIOExceptionMessages.EIOE_COULD_NOT_DECRYPT_MESSAGE_BECAUSE_NO_RECIPIENT_INFORMATION_WAS_FOUND);
            }

            // We have all the necessary information, so now we can really decrypt the message.
            Key kPrivate = ciInfo.getKey();

            if (kPrivate == null)
            {
                throw new EmailIOException(EmailIOExceptionMessages.EIOE_COULD_NOT_FIND_A_PRIVATE_KEY_FOR_RECIPIENT_0,
                                           riInfo.getRID().getSubjectAsString());
            }

            // Do the actual decrypting of the content.
            MimeBodyPart res = org.bouncycastle.mail.smime.SMIMEUtil.toMimeBodyPart(riInfo
                                                                                    .getContent(kPrivate,
                                                                                                "BC"));

            // Now create a decrypted version of the mail.
            mmReturn = new MimeMessage(mmSource);

            MimeMultipart mmTemp = new MimeMultipart();
            mmTemp.addBodyPart(res);

            mmReturn.setContent(mmTemp);
            mmReturn.saveChanges();

            if (LOG.isDebugEnabled())
            {
                LOG.debug("The message was now decrypted. Returning mail:\n" +
                          MailMessageUtil.rawMessage(mmReturn));
            }
        }
        catch (EmailIOException eioe)
        {
            throw eioe;
        }
        catch (Exception e)
        {
            throw new EmailIOException(e,
                                       EmailIOExceptionMessages.EIOE_ERROR_DECRYPTING_EMAIL_MESSAGE);
        }

        return mmReturn;
    }

    /**
     * This method will return based on the given content type if the current part is a normal plain
     * text mail, a signed part or an encrypted part.
     *
     * @param   sContentType  The current content type.
     *
     * @return  The SMIME type for the given content type.
     */
    public static ESMIMEType getSMIMEType(String sContentType)
    {
        ESMIMEType stReturn = ESMIMEType.PLAIN;

        try
        {
            if (StringUtil.isSet(sContentType))
            {
                ContentType ct = new ContentType(sContentType);

                String sMainType = ct.getBaseType();

                if (SIGNED_TYPE.equals(sMainType))
                {
                    String sProtocol = ct.getParameter("protocol");

                    if (s_alSMIMESignTypes.contains(sProtocol))
                    {
                        stReturn = ESMIMEType.SIGNED;
                    }
                }
                else if (s_alSMIMEEncryptionTypes.contains(sMainType))
                {
                    stReturn = ESMIMEType.ENCRYPTED;
                }
            }
        }
        catch (Exception e)
        {
            // We'll assume the mail is plain, but log a warning.
            LOG.warn(e,
                     LogMessages.WRN_COULD_NOT_DETERMINE_THE_SMIME_TYPE_BASED_ON_THE_CONTENT_TYPE_0,
                     sContentType);
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Based on content type " + sContentType + " the SMIME type is: " +
                      stReturn.name());
        }

        return stReturn;
    }

    /**
     * This method returns whether or not the given mail is encrypted.
     *
     * @param   mm  The mail to check.
     *
     * @return  Whether or not the given mail is encrypted.
     *
     * @throws  EmailIOException  In case of any exceptions.
     */
    public static boolean isEncryped(MimeMessage mm)
                              throws EmailIOException
    {
        boolean bReturn = false;

        try
        {
            bReturn = ESMIMEType.ENCRYPTED == getSMIMEType(mm.getContentType());
        }
        catch (MessagingException e)
        {
            throw new EmailIOException(e,
                                       EmailIOExceptionMessages.EIOE_ERROR_CHECKING_IF_THE_MAIL_IS_ENCRYPTED);
        }

        return bReturn;
    }

    /**
     * This method returns whether or not the given multi part is encrypted.
     *
     * @param   mp  The multi part to check.
     *
     * @return  Whether or not the given multi part is encrypted.
     */
    public static boolean isEncryped(MimeMultipart mp)
    {
        return ESMIMEType.ENCRYPTED == getSMIMEType(mp.getContentType());
    }

    /**
     * This method returns whether or not the given mail is signed.
     *
     * @param   mm  The mail to check.
     *
     * @return  Whether or not the given mail is signed.
     *
     * @throws  EmailIOException  In case of any exceptions.
     */
    public static boolean isSigned(MimeMessage mm)
                            throws EmailIOException
    {
        boolean bReturn = false;

        try
        {
            // This statement only works if a mail is JUST signed. But if the mail was encrypted
            // before then the mime message contains a signed body part.
            bReturn = ESMIMEType.SIGNED == getSMIMEType(mm.getContentType());

            if (bReturn == false)
            {
                // Check if the bodypart is there.
                Object oContent = mm.getContent();

                if (oContent instanceof MimeMultipart)
                {
                    MimeMultipart mmp = (MimeMultipart) oContent;

                    BodyPart mbp = mmp.getBodyPart(0);
                    bReturn = ESMIMEType.SIGNED == getSMIMEType(mbp.getContentType());
                }
            }
        }
        catch (Exception e)
        {
            throw new EmailIOException(e,
                                       EmailIOExceptionMessages.EIOE_ERROR_CHECKING_IF_THE_MAIL_IS_SIGNED);
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Message is " + (bReturn ? "" : "NOT ") + "signed");
        }

        return bReturn;
    }

    /**
     * This method returns whether or not the given multi part is signed.
     *
     * @param   mp  The multi part to check.
     *
     * @return  Whether or not the given multi part is signed.
     */
    public static boolean isSigned(MimeMultipart mp)
    {
        return ESMIMEType.SIGNED == getSMIMEType(mp.getContentType());
    }

    /**
     * This method validates the signature for the mime message.
     *
     * @param   mmSigned  mmFinal The message to validate the signature.
     *
     * @throws  EmailIOException  In case of any exceptions.
     */
    public static void validateSignature(MimeMessage mmSigned)
                                  throws EmailIOException
    {
        try
        {
            // The signature is on the the first part. So we need to be the body part that is
            // actually signed.
            Object oContent = mmSigned.getContent();

            if (oContent instanceof MimePart)
            {
                MimePart mp = (MimePart) oContent;
                validateSignature(mp);
            }
            else if (oContent instanceof MimeMultipart)
            {
                // If the mail was encrypted before we need to look deeper in the multipart
                // structure.
                MimeMultipart mmp = (MimeMultipart) oContent;

                MimeBodyPart mbp = (MimeBodyPart) mmp.getBodyPart(0);

                validateSignature(mbp);
            }
            else
            {
                throw new EmailIOException(EmailIOExceptionMessages.EIOE_COULD_NOT_FIND_THE_MIMEMULTIPART_THAT_IS_SIGNED);
            }
        }
        catch (EmailIOException eioe)
        {
            throw eioe;
        }
        catch (Exception e)
        {
            throw new EmailIOException(e,
                                       EmailIOExceptionMessages.EIOE_COULD_NOT_VALIDATE_SIGNATURE);
        }
    }

    /**
     * This method validates the signature for the bodypart.
     *
     * @param   mbpSigned  mmFinal The message to validate the signature.
     *
     * @throws  EmailIOException  In case of any exceptions.
     */
    public static void validateSignature(MimePart mbpSigned)
                                  throws EmailIOException
    {
        try
        {
            if (mbpSigned.isMimeType("multipart/signed"))
            {
                SMIMESigned s = new SMIMESigned((MimeMultipart) mbpSigned.getContent());
                verify(s);
            }
            else if (mbpSigned.isMimeType("application/pkcs7-mime") ||
                         mbpSigned.isMimeType("application/x-pkcs7-mime"))
            {
                // in this case the content is wrapped in the signature block.
                SMIMESigned s = new SMIMESigned(mbpSigned);
                verify(s);
            }
        }
        catch (EmailIOException eioe)
        {
            throw eioe;
        }
        catch (Exception e)
        {
            throw new EmailIOException(e,
                                       EmailIOExceptionMessages.EIOE_COULD_NOT_VALIDATE_SIGNATURE);
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Signature is valid");
        }
    }

    /**
     * verify the signature (assuming the cert is contained in the message).
     *
     * @param   sSigned  The signed content.
     *
     * @throws  Exception  In case of any exceptions.
     */
    private static void verify(SMIMESigned sSigned)
                        throws Exception
    {
        // Get all certificates from the mail itself
        CertStore certs = sSigned.getCertificatesAndCRLs("Collection", "BC");

        // SignerInfo blocks which contain the signatures
        SignerInformationStore signers = sSigned.getSignerInfos();

        Collection<?> c = signers.getSigners();
        Iterator<?> it = c.iterator();

        // Check each signer
        while (it.hasNext())
        {
            SignerInformation signer = (SignerInformation) it.next();
            Collection<?> certCollection = certs.getCertificates(signer.getSID());

            Iterator<?> certIt = certCollection.iterator();
            X509Certificate cert = (X509Certificate) certIt.next();

            // Verify that the signature is correct and that it was generated
            // when the certificate was current
            if (signer.verify(cert, "BC"))
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Signature for " + signer.getSID().getSubjectAsString() +
                              " is valid.");
                }
            }
            else
            {
                throw new EmailIOException(EmailIOExceptionMessages.EIOE_VALIDATION_OF_THE_SIGNATURE_FOR_CERTIFICATE_0_FAILED,
                                           signer.getSID().getSubjectAsString());
            }
        }
    }
}
