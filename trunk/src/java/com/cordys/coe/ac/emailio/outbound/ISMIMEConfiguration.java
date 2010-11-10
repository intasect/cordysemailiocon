package com.cordys.coe.ac.emailio.outbound;

import com.cordys.coe.ac.emailio.exception.KeyManagerException;
import com.cordys.coe.ac.emailio.keymanager.ICertificateInfo;

import org.bouncycastle.cms.RecipientId;

/**
 * This interface describes the configuration options needed for S/MIME email handling.
 *
 * @author  pgussow
 */
public interface ISMIMEConfiguration
{
    /**
     * This method gets whether or not the S/MIME support should be used at all. When false it will
     * function as a normal SMTP service.
     *
     * @return  Whether or not the S/MIME support should be used at all. When false it will function
     *          as a normal SMTP service.
     */
    boolean getBypassSMIME();

    /**
     * This method returns the certificate info for the given email address.
     *
     * @param   sEmailAddress  The address to search for.
     *
     * @return  The information for the given emaill address. If not found, null is returned.
     *
     * @throws  KeyManagerException  In case of any exceptions.
     */
    ICertificateInfo getCertificateInfo(String sEmailAddress)
                                 throws KeyManagerException;

    /**
     * This method tries to find the certificate information based on the RecipientID information.
     *
     * @param   riRecipientID  The recipient information.
     *
     * @return  The corresponding certificate information. If no certificate information could be
     *          found null is returned.
     *
     * @throws  KeyManagerException  In case of any exceptions.
     */
    ICertificateInfo getCertificateInfo(RecipientId riRecipientID)
                                 throws KeyManagerException;

    /**
     * This method gets whether or not the CRL should be checked in case a CRL is available from the
     * certificate. If this is true then by default a certificate for which the CRL could not be
     * accessed will be considered invalid.
     *
     * @return  Whether or not the CRL should be checked in case a CRL is available from the
     *          certificate. If this is true then by default a certificate for which the CRL could
     *          not be accessed will be considered invalid.
     */
    boolean getCheckCRL();

    /**
     * This method gets whether or not to encrypt the mails that are being sent.
     *
     * @return  Whether or not to encrypt the mails that are being sent.
     */
    boolean getEncryptMails();

    /**
     * This method gets whether or not the outgoing mails should be signed.
     *
     * @return  Whether or not the outgoing mails should be signed.
     */
    boolean getSignMails();

    /**
     * This method gets whether or not S/MIME should be used.
     *
     * @return  Whether or not S/MIME should be used.
     */
    boolean getSMIMEEnabled();
}
