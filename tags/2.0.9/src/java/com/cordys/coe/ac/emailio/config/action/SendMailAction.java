package com.cordys.coe.ac.emailio.config.action;

import com.cordys.coe.ac.emailio.EmailIOConnectorConstants;
import com.cordys.coe.ac.emailio.config.rule.IRuleContext;
import com.cordys.coe.ac.emailio.exception.ActionException;
import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;
import com.cordys.coe.ac.emailio.localization.ActionExceptionMessages;
import com.cordys.coe.ac.emailio.localization.EmailIOConfigurationExceptionMessages;
import com.cordys.coe.util.soap.SOAPWrapper;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * This class implements the SendMail action. It will send a new mail to the configured mailbox.
 *
 * @author  pgussow
 */
class SendMailAction extends BaseAction
{
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(SendMailAction.class);
    /**
     * Holds the name of the tag 'password'.
     */
    private static final String TAG_PASSWORD = "password";
    /**
     * Holds the name of the tag 'username'.
     */
    private static final String TAG_USERNAME = "username";
    /**
     * Holds the name of the tag 'port'.
     */
    private static final String TAG_PORT = "port";
    /**
     * Holds the name of the tag 'host'.
     */
    private static final String TAG_HOST = "host";
    /**
     * Holds the default SMTP port.
     */
    private static final int DEFAULT_SMTP_PORT = 25;
    /**
     * Holds the name of the tag 'smtp'.
     */
    private static final String TAG_SENDMAIL = "sendmail";
    /**
     * Holds the name of the tag 'fromaddress'.
     */
    private static final String TAG_FROMADDRESS = "fromaddress";
    /**
     * Holds the name of the tag 'toaddress'.
     */
    private static final String TAG_TOADDRESS = "toaddress";
    /**
     * Holds the name of the tag 'subject'.
     */
    private static final String TAG_SUBJECT = "subject";
    /**
     * Holds the port number for the SMTP server.
     */
    private int m_iPort;
    /**
     * Holds the from address for the mail.
     */
    private String m_sFromAddress;
    /**
     * Holds the host name.
     */
    private String m_sHost;
    /**
     * Holds the password.
     */
    private String m_sPassword;
    /**
     * Holds the new subject for the mail.
     */
    private String m_sSubject;
    /**
     * Holds the to address.
     */
    private String m_sToAddress;
    /**
     * Holds the username.
     */
    private String m_sUsername;

    /**
     * Creates a new SendMailAction object.
     *
     * @param   iNode  The configuration node.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    public SendMailAction(int iNode)
                   throws EmailIOConfigurationException
    {
        super(iNode, EAction.SENDMAIL);

        XPathMetaInfo xmi = new XPathMetaInfo();
        xmi.addNamespaceBinding("ns", EmailIOConnectorConstants.NS_CONFIGURATION);

        m_sHost = XPathHelper.getStringValue(iNode,
                                             "./ns:" + TAG_SENDMAIL + "/ns:" + TAG_HOST + "/text()",
                                             xmi, EMPTY_STRING);
        m_iPort = XPathHelper.getIntegerValue(iNode,
                                              "./ns:" + TAG_SENDMAIL + "/ns:" + TAG_PORT +
                                              "/text()", xmi, 0);
        m_sUsername = XPathHelper.getStringValue(iNode,
                                                 "./ns:" + TAG_SENDMAIL + "/ns:" + TAG_USERNAME +
                                                 "/text()", xmi, EMPTY_STRING);
        m_sPassword = XPathHelper.getStringValue(iNode,
                                                 "./ns:" + TAG_SENDMAIL + "/ns:" + TAG_PASSWORD +
                                                 "/text()", xmi, EMPTY_STRING);

        if (m_sHost.length() == 0)
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_MISSING_HOST_NAME);
        }

        if (m_iPort == 0)
        {
            m_iPort = DEFAULT_SMTP_PORT;
        }

        if (m_sUsername.length() == 0)
        {
            m_sUsername = null;
            m_sPassword = null;
        }

        // Read the from, to and subject.
        m_sFromAddress = XPathHelper.getStringValue(iNode,
                                                    "./ns:" + TAG_SENDMAIL + "/ns:" +
                                                    TAG_FROMADDRESS + "/text()", xmi, EMPTY_STRING);
        m_sToAddress = XPathHelper.getStringValue(iNode,
                                                  "./ns:" + TAG_SENDMAIL + "/ns:" + TAG_TOADDRESS +
                                                  "/text()", xmi, EMPTY_STRING);

        if (m_sFromAddress.length() == 0)
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_MISSING_FROM_ADDRESS_FOR_ACTION_0,
                                                    getID());
        }

        if (m_sToAddress.length() == 0)
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_MISSING_TO_ADDRESS_FOR_ACTION,
                                                    getID());
        }

        m_sSubject = XPathHelper.getStringValue(iNode,
                                                "./ns:" + TAG_SENDMAIL + "/ns:" + TAG_SUBJECT +
                                                "/text()", xmi, EMPTY_STRING);
    }

    /**
     * This method is called to actually execute the action. In this case a mail will be sent to the
     * configured email box.
     *
     * @param   pcContext  The pattern context.
     * @param   mMessage   The actual email message for which the action should be executed.
     * @param   swSoap     The soap wrapper to use.
     *
     * @throws  ActionException  InboundEmailConfigurationException In case of any exceptions.
     *
     * @see     com.cordys.coe.ac.emailio.config.action.IAction#execute(IRuleContext, Message,
     *          SOAPWrapper)
     */
    @Override public void execute(IRuleContext pcContext, Message mMessage, SOAPWrapper swSoap)
                           throws ActionException
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Executing action " + getID() + "\nDetails: " + toString());
        }

        // Build up the properties
        Properties pSMTP = new Properties();
        pSMTP.put("mail.smtp.host", m_sHost);
        pSMTP.put("mail.smtp.port", String.valueOf(m_iPort));

        Authenticator aAuth = null;

        if (m_sUsername != null)
        {
            aAuth = new LocalAuthenticator();
        }

        // Create the session
        Session sSession = Session.getInstance(pSMTP, aAuth);

        try
        {
            MimeMessage mmNew = new MimeMessage(sSession, mMessage.getInputStream());
            mmNew.setFrom(new InternetAddress(m_sFromAddress));
            mmNew.setRecipient(Message.RecipientType.TO, new InternetAddress(m_sToAddress));
            mmNew.setSentDate(new Date());

            if ((m_sSubject != null) & (m_sSubject.length() > 0))
            {
                mmNew.setSubject(m_sSubject);
            }
            else
            {
                mmNew.setSubject(mMessage.getSubject());
            }

            // Build up the mail that will be sent.
            Multipart mMulti = new MimeMultipart();

            String sExceptionDetails = (String) pcContext.getValue(IRuleContext.SYS_EXCEPTION_REPORT);

            if ((sExceptionDetails != null) && (sExceptionDetails.length() > 0))
            {
                // Now add the report to the mail as text.
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Adding exception details to the mail body:\n" + sExceptionDetails);
                }

                MimeBodyPart mbpBody = new MimeBodyPart();
                DataHandler dhPlain = new DataHandler(sExceptionDetails, "text/plain");
                mbpBody.setDataHandler(dhPlain);
                mMulti.addBodyPart(mbpBody);
            }

            // Incorporate the original mail
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            DataHandler dh = new DataHandler(mMessage, "message/rfc822");
            messageBodyPart.setDataHandler(dh);
            mMulti.addBodyPart(messageBodyPart);
            mmNew.setContent(mMulti);

            // Send the mail
            Transport.send(mmNew);
        }
        catch (Exception e)
        {
            // In case of any exceptions we'll throw them back.
            throw new ActionException(e, ActionExceptionMessages.AE_ERROR_SEND_MAIL_ACTION,
                                      getID());
        }
    }

    /**
     * This method returns a string representation of the object.
     *
     * @return  A string representation of the object.
     *
     * @see     com.cordys.coe.ac.emailio.config.action.BaseAction#toString()
     */
    @Override public String toString()
    {
        StringBuilder sbReturn = new StringBuilder();

        sbReturn.append(super.toString());
        sbReturn.append("\nHost: " + m_sHost);
        sbReturn.append("\nPort: " + m_iPort);
        sbReturn.append("\nUsername: " + m_sUsername);
        sbReturn.append("\nPassword: *******");

        return sbReturn.toString();
    }

    /**
     * This method dumps the configuration of this action to XML.
     *
     * @param   parent  The parent element.
     *
     * @return  The created XML structure.
     *
     * @see     com.cordys.coe.ac.emailio.config.action.BaseAction#toXML(int)
     */
    @Override public int toXML(int parent)
    {
        int iReturn = super.toXML(parent);

        int iSMTP = Node.createElementWithParentNS(TAG_SENDMAIL, EMPTY_STRING, iReturn);

        Node.createElementWithParentNS(TAG_HOST, m_sHost, iSMTP);
        Node.createElementWithParentNS(TAG_PORT, String.valueOf(m_iPort), iSMTP);

        if ((m_sUsername != null) && (m_sUsername.length() > 0))
        {
            Node.createElementWithParentNS(TAG_USERNAME, m_sUsername, iSMTP);
            Node.createElementWithParentNS(TAG_PASSWORD, "******", iSMTP);
        }

        if ((m_sFromAddress != null) && (m_sFromAddress.length() > 0))
        {
            Node.createElementWithParentNS(TAG_FROMADDRESS, m_sFromAddress, iSMTP);
        }

        if ((m_sToAddress != null) && (m_sToAddress.length() > 0))
        {
            Node.createElementWithParentNS(TAG_TOADDRESS, m_sToAddress, iSMTP);
        }

        if ((m_sSubject != null) && (m_sSubject.length() > 0))
        {
            Node.createElementWithParentNS(TAG_SUBJECT, m_sSubject, iSMTP);
        }

        return iReturn;
    }

    /**
     * Local class to do SMTP authentication.
     *
     * @author  pgussow
     */
    public class LocalAuthenticator extends Authenticator
    {
        /**
         * This method returns the PasswordAuthentication object to use.
         *
         * @return  The password authentication object.
         *
         * @see     javax.mail.Authenticator#getPasswordAuthentication()
         */
        @Override protected PasswordAuthentication getPasswordAuthentication()
        {
            return new PasswordAuthentication(m_sUsername, m_sPassword);
        }
    }
}
