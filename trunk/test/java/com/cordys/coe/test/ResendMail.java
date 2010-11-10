package com.cordys.coe.test;

import java.io.FileInputStream;

import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * DOCUMENTME .
 *
 * @author  pgussow
 */
public class ResendMail
{
    /**
     * Main method.
     *
     * @param  saArguments  The commandline arguments.
     */
    public static void main(String[] saArguments)
    {
        try
        {
            String m_sHost = "srv-nl-ces70";
            int m_iPort = 25;
            String m_sToAddress = "reproduction@ces70.cordys.com";
            String sEmailFile = "D:\\siemens\\troubles\\inboundemailconn\\other\\RETURN_TIME_Extension_Form_Productive_data.xfdf.eml";

            // Build up the properties
            Properties pSMTP = new Properties();
            pSMTP.put("mail.smtp.host", m_sHost);
            pSMTP.put("mail.smtp.port", String.valueOf(m_iPort));

            Authenticator aAuth = null;

            // Create the session
            Session sSession = Session.getInstance(pSMTP, aAuth);

            MimeMessage mmNew = new MimeMessage(sSession, new FileInputStream(sEmailFile));
            mmNew.setRecipient(Message.RecipientType.TO, new InternetAddress(m_sToAddress));
            mmNew.setSentDate(new Date());

            // Send the mail
            Transport.send(mmNew);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
