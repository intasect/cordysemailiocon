package com.cordys.coe.ac.emailio.keymanager;

import java.security.PrivateKey;

import java.security.cert.X509Certificate;

import org.bouncycastle.cms.RecipientId;

/**
 * This interface describes the information known about a certificate.
 *
 * @author  pgussow
 */
public interface ICertificateInfo
{
    /**
     * This method gets the alias of the certificate in the keystore.
     *
     * @return  The alias of the certificate in the keystore.
     */
    String getAlias();

    /**
     * This method gets the email address for the current certificate.
     *
     * @return  The email address for the current certificate.
     */
    String getEmailAddress();

    /**
     * This method gets the private key for this certificate.
     *
     * @return  The private key for this certificate.
     */
    PrivateKey getKey();

    /**
     * This method gets the recipient ID.
     *
     * @return  The recipient ID.
     */
    RecipientId getRecipientId();

    /**
     * This method gets the certificate.
     *
     * @return  The certificate.
     */
    X509Certificate getX509Certificate();

    /**
     * This method sets the alias of the certificate in the keystore.
     *
     * @param  alias  The alias of the certificate in the keystore.
     */
    void setAlias(String alias);

    /**
     * This method sets the email address for the current certificate.
     *
     * @param  emailAddress  The email address for the current certificate.
     */
    void setEmailAddress(String emailAddress);

    /**
     * This method sets the private key for this certificate.
     *
     * @param  key  The private key for this certificate.
     */
    void setKey(PrivateKey key);

    /**
     * This method sets the recipient ID.
     *
     * @param  recipientId  The recipient ID.
     */
    void setRecipientId(RecipientId recipientId);

    /**
     * This method sets the certificate.
     *
     * @param  certificate  The certificate.
     */
    void setX509Certificate(X509Certificate certificate);
}
