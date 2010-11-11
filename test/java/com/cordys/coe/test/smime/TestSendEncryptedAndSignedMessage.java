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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertStore;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import org.bouncycastle.asn1.smime.SMIMECapabilitiesAttribute;
import org.bouncycastle.asn1.smime.SMIMECapability;
import org.bouncycastle.asn1.smime.SMIMECapabilityVector;
import org.bouncycastle.asn1.smime.SMIMEEncryptionKeyPreferenceAttribute;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.SMIMEEnvelopedGenerator;
import org.bouncycastle.mail.smime.SMIMESignedGenerator;

import com.cordys.coe.ac.emailio.keymanager.ICertificateInfo;
import com.cordys.coe.ac.emailio.keymanager.IKeyManager;
import com.cordys.coe.ac.emailio.keymanager.KeyManagerFactory;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.config.LoggerConfigurator;
import com.eibus.xml.nom.Document;

/**
 * DOCUMENTME.
 *
 * @author  $author$
 */
public class TestSendEncryptedAndSignedMessage
{
    /**
     * Holds the logger to use.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(TestSendEncryptedAndSignedMessage.class);
    /**
     * NOM Document.
     */
    private static final Document DOC = new Document();
    /**
     * Holds the key manager.
     */
    private IKeyManager m_km;

    /**
     * Main method.
     *
     * @param  saArguments  The commandline arguments.
     */
    public static void main(String[] saArguments)
    {
        try
        {
            TestSendEncryptedAndSignedMessage p = new TestSendEncryptedAndSignedMessage();
            p.setup();
            p.loadKeystore();

            p.sendMessage();
        }
        catch (Exception e)
        {
            e.printStackTrace();
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

    /**
     * This method sends the message to the receiver.
     *
     * @throws  Exception  DOCUMENTME
     */
    private void sendMessage()
                      throws Exception
    {
        final InternetAddress[] RECEIVER_ADDRESS = new InternetAddress[]
                                                   {
                                                       new InternetAddress("outlook2007@ces70.cordys.com",
                                                                           "Outlook 2007 User"),
                                                       new InternetAddress("outlookexpress@ces70.cordys.com",
                                                                           "Outlook Express User"),
                                                       new InternetAddress("thunderbird@ces70.cordys.com",
                                                                           "Thunderbird User"),
                                                       new InternetAddress("cordystestuser1@ces70.cordys.com",
                                                                           "Cordys Test User 1"),
                                                       new InternetAddress("cordystestuser2@ces70.cordys.com",
                                                                           "Cordys Test User 2")
                                                   };
        final InternetAddress SENDER_ADDRESS = new InternetAddress("testprogram@ces70.cordys.com",
                                                                   "Test Program User");
        String sSubject = "From test progam V1 [S&E] No r";
        boolean bDoEncryption = true;
// String sContent = "Single line"+System.getProperty("line.separator")+"SecondLine";
        String sContent = "Single line\nSecondLine";

        // Add capabilities.
        MailcapCommandMap mailcap = (MailcapCommandMap) CommandMap.getDefaultCommandMap();

        mailcap.addMailcap("application/pkcs7-signature;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.pkcs7_signature");
        mailcap.addMailcap("application/pkcs7-mime;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.pkcs7_mime");
        mailcap.addMailcap("application/x-pkcs7-signature;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.x_pkcs7_signature");
        mailcap.addMailcap("application/x-pkcs7-mime;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.x_pkcs7_mime");
        mailcap.addMailcap("multipart/signed;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.multipart_signed");

        CommandMap.setDefaultCommandMap(mailcap);

        /* Add BC */
        Security.addProvider(new BouncyCastleProvider());

        /* Get the private key to sign the message with */
        ICertificateInfo certInfo = m_km.getCertificateInfo(SENDER_ADDRESS.getAddress());

        if (certInfo == null)
        {
            throw new Exception("cannot find private key for email address " + SENDER_ADDRESS);
        }

        /* Create the message to sign and encrypt */
        Properties props = System.getProperties();
        props.put("mail.smtp.host", "srv-nl-ces70");

        Session session = Session.getDefaultInstance(props, null);

        MimeMessage body = new MimeMessage(session);
        body.setContent(sContent, "text/plain");
        body.saveChanges();

        /* Create the SMIMESignedGenerator */
        SMIMECapabilityVector capabilities = new SMIMECapabilityVector();
        capabilities.addCapability(SMIMECapability.dES_EDE3_CBC);
        capabilities.addCapability(SMIMECapability.rC2_CBC, 128);
        capabilities.addCapability(SMIMECapability.dES_CBC);

        X509Certificate cert = certInfo.getX509Certificate();

        ASN1EncodableVector attributes = new ASN1EncodableVector();
        X509Name name = new X509Name(cert.getIssuerDN().getName());
        IssuerAndSerialNumber issuerAndSerialNumber = new IssuerAndSerialNumber(name,
                                                                                cert.getSerialNumber());
        SMIMEEncryptionKeyPreferenceAttribute encryptionKeyPreferenceAttribute = new SMIMEEncryptionKeyPreferenceAttribute(issuerAndSerialNumber);
        attributes.add(encryptionKeyPreferenceAttribute);
        attributes.add(new SMIMECapabilitiesAttribute(capabilities));

        SMIMESignedGenerator signer = new SMIMESignedGenerator();
        signer.addSigner((PrivateKey) certInfo.getKey(), cert,
                         "DSA".equals(certInfo.getKey().getAlgorithm())
                         ? SMIMESignedGenerator.DIGEST_SHA1 : SMIMESignedGenerator.DIGEST_MD5,
                         new AttributeTable(attributes), null);

        /* Add the list of certs to the generator */
        List<X509Certificate> certList = new ArrayList<X509Certificate>();
        certList.add(cert);

        CertStore certs = CertStore.getInstance("Collection",
                                                new CollectionCertStoreParameters(certList), "BC");
        signer.addCertificatesAndCRLs(certs);

        /* Sign the message */
        MimeMultipart mm = signer.generate(body, "BC");
        MimeMessage signedMessage = new MimeMessage(session);

        /* Set the content of the signed message */
        signedMessage.setContent(mm);
        signedMessage.saveChanges();

        /* Create the encrypter */
        if (bDoEncryption)
        {
            SMIMEEnvelopedGenerator encrypter = new SMIMEEnvelopedGenerator();

            for (InternetAddress ia : RECEIVER_ADDRESS)
            {
                ICertificateInfo ciTemp = m_km.getCertificateInfo(ia.getAddress());

                if (ciTemp != null)
                {
                    encrypter.addKeyTransRecipient(ciTemp.getX509Certificate());
                }
                else if (LOG.isDebugEnabled())
                {
                    LOG.debug("No certificate found for " + ia.toString());
                }
            }

            /* Encrypt the message */
            MimeBodyPart encryptedPart = encrypter.generate(signedMessage,
                                                            SMIMEEnvelopedGenerator.DES_EDE3_CBC,
                                                            "BC");

            /*
             * Create a new MimeMessage that contains the encrypted and signed content
             */
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            encryptedPart.writeTo(out);

            body = new MimeMessage(session, new ByteArrayInputStream(out.toByteArray()));
        }
        else
        {
            body = signedMessage;
        }

        body.setFrom(SENDER_ADDRESS);
        body.setRecipients(Message.RecipientType.TO, RECEIVER_ADDRESS);
        body.addRecipient(Message.RecipientType.TO,
                          new InternetAddress("intermediate@ces70.cordys.com",
                                              "Intermediate user"));

        body.setSentDate(new Date());
        body.addHeader("User-Agent", "CordysMailClient");
        body.setSubject(sSubject);

        Transport.send(body);
    }
}
