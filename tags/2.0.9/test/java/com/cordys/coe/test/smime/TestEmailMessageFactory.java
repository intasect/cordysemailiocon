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

import com.cordys.coe.ac.emailio.config.outbound.ISendMailData;
import com.cordys.coe.ac.emailio.config.outbound.SendMailDataFactory;
import com.cordys.coe.ac.emailio.exception.KeyManagerException;
import com.cordys.coe.ac.emailio.keymanager.ICertificateInfo;
import com.cordys.coe.ac.emailio.keymanager.IKeyManager;
import com.cordys.coe.ac.emailio.keymanager.KeyManagerFactory;
import com.cordys.coe.ac.emailio.outbound.EmailMessageFactory;
import com.cordys.coe.ac.emailio.outbound.ISMIMEConfiguration;
import com.cordys.coe.util.system.SystemInfo;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.config.LoggerConfigurator;

import com.eibus.xml.nom.Document;

import java.io.File;
import java.io.FilenameFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.Transport;

import javax.mail.internet.MimeMessage;

import org.bouncycastle.cms.RecipientId;

/**
 * DOCUMENTME.
 *
 * @author  $author$
 */
public class TestEmailMessageFactory
{
    /**
     * Holds the logger to use.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(TestEmailMessageFactory.class);
    /**
     * NOM Document.
     */
    private static final Document DOC = new Document();
    /**
     * Holds the key manager.
     */
    private IKeyManager m_km;
    /**
     * Holds the data for the mail to send.
     */
    private List<ISendMailData> m_lSendMailData = new ArrayList<ISendMailData>();
    /**
     * DOCUMENTME.
     */
    private LocalSMIMEConfig m_sc;

    /**
     * Main method.
     *
     * @param  saArguments  The commandline arguments.
     */
    public static void main(String[] saArguments)
    {
        try
        {
            TestEmailMessageFactory p = new TestEmailMessageFactory();
            p.setup();
            p.loadKeystore();

            if (LOG.isDebugEnabled())
            {
                LOG.debug(SystemInfo.getSystemInformation());
            }

            p.loadMessageData("send_encsign_simple.xml");
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

        // Create the configuration
        m_sc = new LocalSMIMEConfig(true, true);
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
     * This method loads the data for sending an email.
     *
     * @param   sFilter  The file filter to apply to the test files.
     *
     * @throws  Exception  In case of any exceptions.
     */
    private void loadMessageData(final String sFilter)
                          throws Exception
    {
        File fFolder = new File(".\\test\\java\\com\\cordys\\coe\\test\\smime\\testfiles");

        // Load the ones according to the new syntax.
        File[] afFiles = fFolder.listFiles(new FilenameFilter()
            {
                @Override public boolean accept(File dir, String name)
                {
                    return name.startsWith(sFilter);
                }
            });

        for (File fFile : afFiles)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Loading mail data file: " + fFile);
            }

            int iNode = DOC.load(fFile.getCanonicalPath());

            if (fFile.getName().indexOf("_comp_") > -1)
            {
                m_lSendMailData.add(SendMailDataFactory.parseSendMailDataCompatibility(iNode,
                                                                                       m_sc));
            }
            else
            {
                m_lSendMailData.add(SendMailDataFactory.parseSendMailData(iNode, m_sc));
            }
        }
    }

    /**
     * This method actually sends the message.
     *
     * @throws  Exception  DOCUMENTME
     */
    private void sendMessage()
                      throws Exception
    {
        // Set up the JavaMail API
        Properties props = System.getProperties();
        props.put("mail.smtp.host", "srv-nl-ces70");

        Session sSession = Session.getDefaultInstance(props, null);

        // Create the actual message
        for (ISendMailData smd : m_lSendMailData)
        {
            // Change the subject.
            String sSubject = smd.getSubject();

            sSubject += "[S:" + m_sc.getSignMails() + "|E:" + m_sc.getEncryptMails() + "]";

            smd.setSubject(sSubject);

            // Build up the mails and send them
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Sending mail with subject: " + sSubject);
            }

            List<MimeMessage> l = EmailMessageFactory.createProperMimeMessages(smd, m_sc, sSession);

            for (MimeMessage mm : l)
            {
                Transport.send(mm);
            }
        }
    }

    /**
     * Local class holding the configuration.
     *
     * @author  pgussow
     */
    private class LocalSMIMEConfig
        implements ISMIMEConfiguration
    {
        /**
         * DOCUMENTME.
         */
        private boolean m_bEncrypt;
        /**
         * DOCUMENTME.
         */
        private boolean m_bSign;

        /**
         * Creates a new LocalSMIMEConfig object.
         *
         * @param  bEncrypt  DOCUMENTME
         * @param  bSign     DOCUMENTME
         */
        public LocalSMIMEConfig(boolean bEncrypt, boolean bSign)
        {
            m_bEncrypt = bEncrypt;
            m_bSign = bSign;
        }

        /**
         * @see  com.cordys.coe.ac.emailio.outbound.ISMIMEConfiguration#getBypassSMIME()
         */
        @Override public boolean getBypassSMIME()
        {
            return false;
        }

        /**
         * @see  com.cordys.coe.ac.emailio.outbound.ISMIMEConfiguration#getCertificateInfo(java.lang.String)
         */
        @Override public ICertificateInfo getCertificateInfo(String sEmailAddress)
                                                      throws KeyManagerException
        {
            return m_km.getCertificateInfo(sEmailAddress);
        }

        /**
         * @see  com.cordys.coe.ac.emailio.outbound.ISMIMEConfiguration#getCertificateInfo(org.bouncycastle.cms.RecipientId)
         */
        @Override public ICertificateInfo getCertificateInfo(RecipientId riRecipientID)
                                                      throws KeyManagerException
        {
            return m_km.getCertificateInfo(riRecipientID);
        }

        /**
         * @see  com.cordys.coe.ac.emailio.outbound.ISMIMEConfiguration#getCheckCRL()
         */
        @Override public boolean getCheckCRL()
        {
            return false;
        }

        /**
         * @see  com.cordys.coe.ac.emailio.outbound.ISMIMEConfiguration#getEncryptMails()
         */
        @Override public boolean getEncryptMails()
        {
            return m_bEncrypt;
        }

        /**
         * @see  com.cordys.coe.ac.emailio.outbound.ISMIMEConfiguration#getSignMails()
         */
        @Override public boolean getSignMails()
        {
            return m_bSign;
        }

        /**
         * @see  com.cordys.coe.ac.emailio.outbound.ISMIMEConfiguration#getSMIMEEnabled()
         */
        @Override public boolean getSMIMEEnabled()
        {
            return true;
        }
    }
}
