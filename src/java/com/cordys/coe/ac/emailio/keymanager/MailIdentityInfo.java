package com.cordys.coe.ac.emailio.keymanager;

import java.security.PrivateKey;

import java.security.cert.X509Certificate;

import org.bouncycastle.cms.RecipientId;

/**
 * This class contains all information for the specific user.
 *
 * @author  pgussow
 */
public class MailIdentityInfo
    implements ICertificateInfo
{
    /**
     * Holds the alias of the certificate in the keystore.
     */
    private String m_alias;
    /**
     * Holds the certificate.
     */
    private X509Certificate m_certificate;
    /**
     * Holds the email address for the current certificate.
     */
    private String m_emailAddress;
    /**
     * Holds the private key for this certificate.
     */
    private PrivateKey m_key;
    /**
     * Holds the recipient ID.
     */
    private RecipientId m_recipientId;

    /**
     * @see  ICertificateInfo#getAlias()
     */
    public String getAlias()
    {
        return m_alias;
    }

    /**
     * @see  ICertificateInfo#getEmailAddress()
     */
    public String getEmailAddress()
    {
        return m_emailAddress;
    }

    /**
     * @see  ICertificateInfo#getKey()
     */
    public PrivateKey getKey()
    {
        return m_key;
    }

    /**
     * @see  ICertificateInfo#getRecipientId()
     */
    public RecipientId getRecipientId()
    {
        return m_recipientId;
    }

    /**
     * @see  ICertificateInfo#getX509Certificate()
     */
    public X509Certificate getX509Certificate()
    {
        return m_certificate;
    }

    /**
     * @see  ICertificateInfo#setAlias(java.lang.String)
     */
    public void setAlias(String alias)
    {
        m_alias = alias;
    }

    /**
     * @see  ICertificateInfo#setEmailAddress(java.lang.String)
     */
    public void setEmailAddress(String emailAddress)
    {
        m_emailAddress = emailAddress;
    }

    /**
     * @see  ICertificateInfo#setKey(PrivateKey)
     */
    public void setKey(PrivateKey key)
    {
        m_key = key;
    }

    /**
     * @see  ICertificateInfo#setRecipientId(org.bouncycastle.cms.RecipientId)
     */
    public void setRecipientId(RecipientId recipientId)
    {
        m_recipientId = recipientId;
    }

    /**
     * @see  ICertificateInfo#setX509Certificate(java.security.cert.X509Certificate)
     */
    public void setX509Certificate(X509Certificate certificate)
    {
        m_certificate = certificate;
    }
}
