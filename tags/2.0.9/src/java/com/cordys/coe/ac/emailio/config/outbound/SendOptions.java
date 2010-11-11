package com.cordys.coe.ac.emailio.config.outbound;

import com.cordys.coe.ac.emailio.outbound.ISMIMEConfiguration;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * This class implements the send options. It is able to parse the configuration data.
 *
 * @author  pgussow
 */
class SendOptions
    implements ISendOptions
{
    /**
     * Holds the name of the tag 'encryptmail'.
     */
    private static final String TAG_ENCRYPT_MAIL = "encryptmail";
    /**
     * Holds the name of the tag 'signmail'.
     */
    private static final String TAG_SIGN_MAIL = "signmail";
    /**
     * Holds the name of the tag 'smtpserver'.
     */
    private static final String TAG_SMTP_SERVER = "smtpserver";
    /**
     * Holds whether or not the mail should be encrypted.
     */
    private boolean m_bEncryptMail = false;
    /**
     * Holds whether or not the mail should be signed.
     */
    private boolean m_bSignMail = false;
    /**
     * Holds the name of the SMTP server to use.
     */
    private String m_sSMTPServer = null;

    /**
     * Creates a new SendOptions object.
     */
    public SendOptions()
    {
    }

    /**
     * Creates a new SendOptions object.
     *
     * @param  iContent  The XML definition.
     * @param  xmi       The XPathMetaInfo with the prefix 'ns' mapped to the proper namespace.
     * @param  scConfig  The configuration for S/MIME.
     */
    public SendOptions(int iContent, XPathMetaInfo xmi, ISMIMEConfiguration scConfig)
    {
        // Get the optional configuration.
        if (iContent != 0)
        {
            m_bEncryptMail = XPathHelper.getBooleanValue(iContent, "./ns:" + TAG_ENCRYPT_MAIL, xmi,
                                                         scConfig.getEncryptMails());
            m_bSignMail = XPathHelper.getBooleanValue(iContent, "./ns:" + TAG_SIGN_MAIL, xmi,
                                                      scConfig.getSignMails());

            m_sSMTPServer = XPathHelper.getStringValue(iContent, "./ns:" + TAG_SMTP_SERVER, xmi,
                                                       null);
        }
        else
        {
            m_bEncryptMail = scConfig.getEncryptMails();
            m_bSignMail = scConfig.getSignMails();
        }
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.ISendOptions#getEncryptMail()
     */
    @Override public boolean getEncryptMail()
    {
        return m_bEncryptMail;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.ISendOptions#getSignMail()
     */
    @Override public boolean getSignMail()
    {
        return m_bSignMail;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.ISendOptions#getSMTPServer()
     */
    @Override public String getSMTPServer()
    {
        return m_sSMTPServer;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.ISendOptions#setEncryptMail(boolean)
     */
    @Override public void setEncryptMail(boolean bEncryptMail)
    {
        m_bEncryptMail = bEncryptMail;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.ISendOptions#setSignMail(boolean)
     */
    @Override public void setSignMail(boolean bSignMail)
    {
        m_bSignMail = bSignMail;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.ISendOptions#setSMTPServer(java.lang.String)
     */
    @Override public void setSMTPServer(String sSMTPServer)
    {
        m_sSMTPServer = sSMTPServer;
    }
}
