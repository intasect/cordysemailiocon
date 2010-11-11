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
 package com.cordys.coe.test.smime;

import com.cordys.coe.ac.emailio.config.IEmailBox;
import com.cordys.coe.ac.emailio.config.POP3EmailBox;
import com.cordys.coe.ac.emailio.connection.EmailConnectionFactory;
import com.cordys.coe.ac.emailio.connection.IEmailConnection;
import com.cordys.coe.ac.emailio.keymanager.ICertificateInfo;
import com.cordys.coe.ac.emailio.keymanager.IKeyManager;
import com.cordys.coe.ac.emailio.keymanager.KeyManagerFactory;
import com.cordys.coe.ac.emailio.util.MailMessageUtil;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.util.logger.config.LoggerConfigurator;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;

import java.security.Key;

import java.security.cert.CertStore;
import java.security.cert.X509Certificate;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.mail.Message;

import javax.mail.Message.RecipientType;

import javax.mail.Multipart;

import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.RecipientInformationStore;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;

import org.bouncycastle.mail.smime.SMIMEEnveloped;
import org.bouncycastle.mail.smime.SMIMESigned;
import org.bouncycastle.mail.smime.SMIMEUtil;

/**
 * DOCUMENTME.
 *
 * @author  $author$
 */
public class TestSMIMEBouncyCastle
{
    /**
     * NOM Document.
     */
    private static final Document DOC = new Document();
    /**
     * Contains the logger instance.
     */
    private static CordysLogger LOG = CordysLogger.getCordysLogger(TestSMIMEBouncyCastle.class);

    /**
     * DOCUMENTME.
     */
    private IKeyManager m_km;

    /**
     * Main method.
     *
     * @param  saArguments  Commandline arguments.
     */
    public static void main(String[] saArguments)
    {
        try
        {
            TestSMIMEBouncyCastle p = new TestSMIMEBouncyCastle();

            p.setup();
            p.loadKeystore();

            p.fetchAllMessage();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Test to fetch e-mails from a POP3 mainbox.
     *
     * @throws  Exception
     */
    public void fetchAllMessage()
                         throws Exception
    {
        int config = 0;

        try
        {
            config = DOC.parseString("<emailboxes xmlns=\"http://emailioconnector.coe.cordys.com/2.0/configuration\">" +
                                     "<emailbox>" +
                                     "<name>testmailbox</name>" +
                                     "<host>srv-nl-ces70</host>" +
                                     "<port>110</port>" +
                                     "<type>POP3</type>" +
                                     "<username>cordystestuser1</username>" +
                                     "<password>Y29yZHlzdGVzdHVzZXIx</password>" +
                                     "<pollinterval>5000</pollinterval>" +
                                     "<folders>" +
                                     "<folder>Inbox</folder>" +
                                     "</folders>" +
                                     "</emailbox>" +
                                     "</emailboxes>");

            int emailBoxConfigNode = Node.getFirstElement(config);

            IEmailBox e = new POP3EmailBox(emailBoxConfigNode, 0, "", true);
            IEmailConnection ec = EmailConnectionFactory.createConnection(e);
            Message[] am = ec.getEmailHeaders();

            for (int iCount = 0; iCount < am.length; iCount++)
            {
                MimeMessage msg = (MimeMessage) am[iCount];

                if (LOG.isEnabled(Severity.DEBUG))
                {
                    LOG.log(Severity.DEBUG, "= Message " + (iCount + 1) + " =");

                    LOG.log(Severity.DEBUG,
                            msg.getSubject() + " from " + msg.getFrom()[0].toString() + " to " +
                            msg.getRecipients(RecipientType.TO)[0].toString());
                }

                // TODO: We need to detect whether or not the mail is encrypted/signed. To do this
                // we need to example the content type of the mail.

                // We need to figure out which certificate to use for receiving the mail. We'll go
                // through all known identities to see if we're there.
                Map<String, ICertificateInfo> identities = m_km.getIdentities();

                // Parse the mail message
                SMIMEEnveloped m = new SMIMEEnveloped(msg);

                // Find the details of a receipient that can decrypt the message.
                RecipientInformationStore recipients = m.getRecipientInfos();
                RecipientInformation recipient = null;
                ICertificateInfo certInfo = null;

                for (ICertificateInfo tmp : identities.values())
                {
                    recipient = recipients.get(tmp.getRecipientId());

                    if (recipient != null)
                    {
                        certInfo = tmp;
                        System.out.println("Found a recipient: " + certInfo.getAlias() + "/" +
                                           certInfo.getEmailAddress());
                        break;
                    }
                }

                // Now do the actual decryption of the message.
                Key privateKey = certInfo.getKey();

                MimeBodyPart res = SMIMEUtil.toMimeBodyPart(recipient.getContent(privateKey, "BC"));

                // Do the signature
                doSignatureValidation(res);

                // So now the mail is decrypted. If it's signed we also do the signature check. Once
                // that's done we create a NEW dummy MimeMessage which is identical to the original
                // mail, but with the Encrypted part removed.
                MimeMessage mmNew = new MimeMessage(msg);

                MimeMultipart mmTemp = new MimeMultipart();
                mmTemp.addBodyPart(res);
                mmNew.setContent(mmTemp);

                mmNew.saveChanges();

                // The part will be a stream. And the stream contains a mail message.

                if (res != null)
                {
                    LOG.log(Severity.DEBUG, "= Decrypted message " + (iCount + 1));
                    LOG.log(Severity.DEBUG, "===============================");
                    LOG.log(Severity.DEBUG, MailMessageUtil.dumpMessage(mmNew));
                    LOG.log(Severity.DEBUG, "===============================");
                    LOG.log(Severity.DEBUG,
                            "Original raw:\n" + MailMessageUtil.rawMessage(msg) +
                            "\n\n\nDecrypted raw:\n" + MailMessageUtil.rawMessage(mmNew));
                }
                else
                {
                    System.out.println("failed to decrypt message.");
                }
            }
        }
        finally
        {
            if (config > 0)
            {
                Node.delete(config);
            }
        }
    }

    /**
     * Initialize the logger.
     */
    public void setup()
    {
        LoggerConfigurator.initLogger("../InboudEmailConnector_main/test/Log4jConfiguration.xml");
    }

    /**
     * verify the signature (assuming the cert is contained in the message).
     *
     * @param   s  DOCUMENTME
     *
     * @throws  Exception  DOCUMENTME
     */
    private static void verify(SMIMESigned s)
                        throws Exception
    {
        CertStore certs = s.getCertificatesAndCRLs("Collection", "BC");
        //
        // SignerInfo blocks which contain the signatures
        //
        SignerInformationStore signers = s.getSignerInfos();

        Collection<?> c = signers.getSigners();
        Iterator<?> it = c.iterator();

        //
        // check each signer
        //
        while (it.hasNext())
        {
            SignerInformation signer = (SignerInformation) it.next();
            Collection<?> certCollection = certs.getCertificates(signer.getSID());

            Iterator<?> certIt = certCollection.iterator();
            X509Certificate cert = (X509Certificate) certIt.next();

            //
            // verify that the sig is correct and that it was generated
            // when the certificate was current
            //
            if (signer.verify(cert, "BC"))
            {
                System.out.println("signature verified");
            }
            else
            {
                System.out.println("signature failed!");
            }
        }
    }

    /**
     * DOCUMENTME.
     *
     * @param   res
     *
     * @throws  Exception  DOCUMENTME
     */
    private void doSignatureValidation(MimeBodyPart res)
                                throws Exception
    {
        if (res.isMimeType("multipart/signed"))
        {
            SMIMESigned s = new SMIMESigned((MimeMultipart) res.getContent());

            //
            // extract the content
            //
            MimeBodyPart content = s.getContent();

            System.out.println("Content:");

            Object cont = content.getContent();

            if (cont instanceof String)
            {
                System.out.println((String) cont);
            }
            else if (cont instanceof Multipart)
            {
                System.out.println(MailMessageUtil.dumpMultipart("", (Multipart) cont));
            }

            System.out.println("Status:");

            verify(s);
        }
        else if (res.isMimeType("application/pkcs7-mime") ||
                     res.isMimeType("application/x-pkcs7-mime"))
        {
            //
            // in this case the content is wrapped in the signature block.
            //
            SMIMESigned s = new SMIMESigned(res);

            //
            // extract the content
            //
            MimeBodyPart content = s.getContent();

            System.out.println("Content:");

            Object cont = content.getContent();

            if (cont instanceof String)
            {
                System.out.println((String) cont);
            }
            else if (cont instanceof Multipart)
            {
                System.out.println(MailMessageUtil.dumpMultipart("", (Multipart) cont));
            }

            System.out.println("Status:");

            verify(s);
        }
        else
        {
            System.err.println("Not a signed message!");
        }
    }

    /**
     * Loads the keystores.
     *
     * @throws  Exception  InternalSMIMEException In case of exceptions.
     */
    private void loadKeystore()
                       throws Exception
    {
        int iNode = DOC.load(".\\test\\java\\com\\cordys\\coe\\test\\smime\\keymanager.xml");

        m_km = KeyManagerFactory.createKeyManager(null, iNode);
    }
}
