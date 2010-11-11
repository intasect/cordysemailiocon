
 /**
 * Copyright 2007 Cordys R&D B.V. 
 * 
 * This file is part of the Cordys Email IO Connector. 
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
package com.cordys.coe.test.tester;

import com.cordys.coe.ac.emailio.config.EmailBoxFactory;
import com.cordys.coe.ac.emailio.config.IEmailBox;
import com.cordys.coe.ac.emailio.config.trigger.ITrigger;
import com.cordys.coe.ac.emailio.connection.IEmailConnection;
import com.cordys.coe.ac.emailio.exception.EmailConnectionException;
import com.cordys.coe.ac.emailio.exception.KeyManagerException;
import com.cordys.coe.ac.emailio.exception.StorageProviderException;
import com.cordys.coe.ac.emailio.keymanager.ICertificateInfo;
import com.cordys.coe.ac.emailio.monitor.IJMXEmailBoxPoller;
import com.cordys.coe.ac.emailio.monitor.RuleContextContainer;
import com.cordys.coe.ac.emailio.outbound.ISMIMEConfiguration;
import com.cordys.coe.ac.emailio.storage.IEmailStorageProvider;
import com.cordys.coe.ac.emailio.triggerengine.ITriggerEngine;
import com.cordys.coe.ac.emailio.triggerengine.TriggerEngineFactory;
import com.cordys.coe.util.system.SystemInfo;

import com.eibus.management.IManagedComponent;

import com.eibus.util.threadpool.Dispatcher;
import com.eibus.util.threadpool.Work;

import com.eibus.xml.nom.Document;
import com.eibus.xml.xpath.XPathMetaInfo;

import java.io.File;
import java.io.FileInputStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Session;

import javax.mail.internet.MimeMessage;

import org.bouncycastle.cms.RecipientId;

/**
 * DOCUMENTME.
 *
 * @author  $author$
 */
public class MultiLineSubjectTester
{
    /**
     * DOCUMENTME.
     */
    private static Document s_dDoc = new Document();

    /**
     * Main method.
     *
     * @param  saArguments  The commandline arguments.
     */
    public static void main(String[] saArguments)
    {
        try
        {
            System.setProperty("log.config.file", ".\\test\\Log4JConfiguration.xml");

            String sConfigFilename = ".\\test\\java\\com\\cordys\\coe\\test\\tester\\spe_feedback.xml";
            File fFolder = new File(".\\docs\\internal\\testmessages");
            String sOther = "multiline_subject.eml";
            String sEmailFile = "";

            // Test the proper type
            // EmailType et = EmailType.ADOBE;
            EmailType[] aet = EmailType.values();

            for (EmailType et : aet)
            {
                switch (et)
                {
                    case OTHER:
                        sEmailFile = new File(fFolder, sOther).getCanonicalPath();
                        break;
                }

                if (new File(sEmailFile).exists() && !new File(sEmailFile).isDirectory())
                {
                    testFile(sConfigFilename, sEmailFile);
                }
            }
        }
        catch (Exception e)
        {
            SystemInfo.getSystemInformation();
            e.printStackTrace();
        }
    }

    /**
     * DOCUMENTME.
     *
     * @param   sConfigFilename
     * @param   sEmailFile
     *
     * @throws  Exception  DOCUMENTME
     */
    private static void testFile(String sConfigFilename, String sEmailFile)
                          throws Exception
    {
        System.out.println("Parsing file: " + sEmailFile);

        // Read the trigger
        int iBox = s_dDoc.load(sConfigFilename);

        IEmailBox eb = EmailBoxFactory.createEmailBox(iBox, 0,
                                                      "cn=dummy proc, cn=dummy node,cn=soap nodes,o=dev,cn=cordys,o=cordys.com",
                                                      false, false);

        // Build up dummy mail session
        String m_sHost = "srv-nl-ces70";
        int m_iPort = 25;

        // Build up the properties
        Properties pSMTP = new Properties();
        pSMTP.put("mail.smtp.host", m_sHost);
        pSMTP.put("mail.smtp.port", String.valueOf(m_iPort));

        Authenticator aAuth = null;

        // Create the session
        Session sSession = Session.getInstance(pSMTP, aAuth);
        MimeMessage mmMail = new MimeMessage(sSession, new FileInputStream(sEmailFile));
        ArrayList<Message> alMessage = new ArrayList<Message>();
        alMessage.add(mmMail);

        ITrigger[] at = eb.getTriggers("INBOX");
        ArrayList<ITriggerEngine> alTE = new ArrayList<ITriggerEngine>();

        for (ITrigger trigger : at)
        {
            alTE.add(TriggerEngineFactory.createTriggerEngine(trigger, alMessage, null,
                                                              new DummyEmailConnection(), "INBOX",
                                                              new DummyJMX(), eb,
                                                              new DummyStorageProvider(),
                                                              new DummySMIMEConfiguration()));
        }

        // Process all triggers.
        DummyDispatcher dd = new DummyDispatcher();

        for (ITriggerEngine te : alTE)
        {
            if (te.handleTrigger(dd))
            {
                System.out.println("   MATCH: " + te.getTrigger().getName());
            }
            else
            {
                System.out.println("NO MATCH: " + te.getTrigger().getName());
            }
        }
    }

    /**
     * DOCUMENTME.
     *
     * @author  $author$
     */
    private enum EmailType
    {
        OTHER
    }

    /**
     * DOCUMENTME .
     *
     * @author  pgussow
     */
    public static class DummyStorageProvider
        implements IEmailStorageProvider
    {
        /**
         * @see  com.cordys.coe.ac.emailio.storage.IEmailStorageProvider#addRuleContext(com.cordys.coe.ac.emailio.monitor.RuleContextContainer,
         *       com.cordys.coe.ac.emailio.config.trigger.ITrigger)
         */
        @Override public void addRuleContext(RuleContextContainer rccContext,
                                             ITrigger trigger)
                                      throws StorageProviderException
        {
        }

        /**
         * @see  com.cordys.coe.ac.emailio.storage.IEmailStorageProvider#addTrigger(com.cordys.coe.ac.emailio.config.trigger.ITrigger,
         *       boolean)
         */
        @Override public void addTrigger(ITrigger trigger, boolean isPersistent)
                                  throws StorageProviderException
        {
        }

        /**
         * @see  com.cordys.coe.ac.emailio.storage.IEmailStorageProvider#containsTrigger(java.lang.String)
         */
        @Override public boolean containsTrigger(String triggerName)
        {
            return false;
        }

        /**
         * @see  com.cordys.coe.ac.emailio.storage.IEmailStorageProvider#getContainerDetailXML(java.lang.String)
         */
        @Override public int getContainerDetailXML(String storageID)
                                            throws StorageProviderException
        {
            return 0;
        }

        /**
         * @see  com.cordys.coe.ac.emailio.storage.IEmailStorageProvider#getSoapProcessorDN()
         */
        @Override public String getSoapProcessorDN()
        {
            return null;
        }

        /**
         * @see  com.cordys.coe.ac.emailio.storage.IEmailStorageProvider#getTrigger(java.lang.String)
         */
        @Override public ITrigger getTrigger(String triggerName)
        {
            return null;
        }

        /**
         * @see  com.cordys.coe.ac.emailio.storage.IEmailStorageProvider#getTriggers()
         */
        @Override public Collection<ITrigger> getTriggers()
        {
            return null;
        }

        /**
         * @see  com.cordys.coe.ac.emailio.storage.IEmailStorageProvider#getTriggers(java.lang.String)
         */
        @Override public Collection<ITrigger> getTriggers(String folderName)
        {
            return null;
        }

        /**
         * @see  com.cordys.coe.ac.emailio.storage.IEmailStorageProvider#initialize(com.cordys.coe.ac.emailio.config.IEmailBox,
         *       int, com.eibus.xml.xpath.XPathMetaInfo, java.lang.String)
         */
        @Override public void initialize(IEmailBox ebEmailBox, int configurationNode,
                                         XPathMetaInfo xmi, String soapProcessorDN,
                                         IManagedComponent mcParent)
                                  throws StorageProviderException
        {
        }

        /**
         * @see  com.cordys.coe.ac.emailio.storage.IEmailStorageProvider#isTriggerPersistent(java.lang.String)
         */
        @Override public boolean isTriggerPersistent(String triggerName)
        {
            return false;
        }

        /**
         * @see  com.cordys.coe.ac.emailio.storage.IEmailStorageProvider#removeTrigger(java.lang.String)
         */
        @Override public void removeTrigger(String triggerName)
                                     throws StorageProviderException
        {
        }

        /**
         * @see  com.cordys.coe.ac.emailio.storage.IEmailStorageProvider#setContainerStatusActionError(com.cordys.coe.ac.emailio.monitor.RuleContextContainer,
         *       java.lang.String)
         */
        @Override public void setContainerStatusActionError(RuleContextContainer rccContext,
                                                            String statusInfo)
                                                     throws StorageProviderException
        {
        }

        /**
         * @see  com.cordys.coe.ac.emailio.storage.IEmailStorageProvider#setContainerStatusCompleted(com.cordys.coe.ac.emailio.monitor.RuleContextContainer)
         */
        @Override public void setContainerStatusCompleted(RuleContextContainer rccContext)
                                                   throws StorageProviderException
        {
        }

        /**
         * @see  com.cordys.coe.ac.emailio.storage.IEmailStorageProvider#setContainerStatusError(com.cordys.coe.ac.emailio.monitor.RuleContextContainer,
         *       java.lang.String)
         */
        @Override public void setContainerStatusError(RuleContextContainer rccContext,
                                                      String statusInfo)
                                               throws StorageProviderException
        {
        }

        /**
         * @see  com.cordys.coe.ac.emailio.storage.IEmailStorageProvider#setContainerStatusInProgress(com.cordys.coe.ac.emailio.monitor.RuleContextContainer)
         */
        @Override public void setContainerStatusInProgress(RuleContextContainer rccContext)
                                                    throws StorageProviderException
        {
        }

        /**
         * @see  com.cordys.coe.ac.emailio.storage.IEmailStorageProvider#toXML(int)
         */
        @Override public int toXML(int parent)
        {
            return 0;
        }
    }

    /**
     * DOCUMENTME.
     *
     * @author  $author$
     */
    private static class DummyDispatcher extends Dispatcher
    {
        /**
         * Creates a new DummyDispatcher object.
         */
        public DummyDispatcher()
        {
            super(1, 1, 1, 60000);
        }

        /**
         * @see  com.eibus.util.threadpool.Dispatcher#addWork(com.eibus.util.threadpool.Work)
         */
        @Override public void addWork(Work work)
        {
            // Ignore
        }
    }

    /**
     * DOCUMENTME.
     *
     * @author  $author$
     */
    private static class DummyEmailConnection
        implements IEmailConnection
    {
        /**
         * @see  com.cordys.coe.ac.emailio.connection.IEmailConnection#close()
         */
        @Override public void close()
                             throws EmailConnectionException
        {
        }

        /**
         * @see  com.cordys.coe.ac.emailio.connection.IEmailConnection#expunge()
         */
        @Override public void expunge()
                               throws EmailConnectionException
        {
        }

        /**
         * @see  com.cordys.coe.ac.emailio.connection.IEmailConnection#getEmailHeaders()
         */
        @Override public Message[] getEmailHeaders()
                                            throws EmailConnectionException
        {
            return null;
        }

        /**
         * @see  com.cordys.coe.ac.emailio.connection.IEmailConnection#getEmailHeaders(java.lang.String)
         */
        @Override public Message[] getEmailHeaders(String folder)
                                            throws EmailConnectionException
        {
            return null;
        }

        /**
         * @see  com.cordys.coe.ac.emailio.connection.IEmailConnection#removeMessage(java.lang.String,
         *       javax.mail.Message)
         */
        @Override public void removeMessage(String folderName, Message message)
                                     throws EmailConnectionException
        {
        }
    }

    /**
     * DOCUMENTME.
     *
     * @author  $author$
     */
    private static class DummyJMX
        implements IJMXEmailBoxPoller
    {
        /**
         * @see  com.cordys.coe.ac.emailio.monitor.IJMXEmailBoxPoller#incNrOfMessagesFailed(long)
         */
        @Override public void incNrOfMessagesFailed(long amount)
        {
        }

        /**
         * @see  com.cordys.coe.ac.emailio.monitor.IJMXEmailBoxPoller#incNrOfMessagesIgnored(long)
         */
        @Override public void incNrOfMessagesIgnored(long amount)
        {
        }

        /**
         * @see  com.cordys.coe.ac.emailio.monitor.IJMXEmailBoxPoller#incNrOfMessagesProcessed(long)
         */
        @Override public void incNrOfMessagesProcessed(long amount)
        {
        }

        /**
         * @see  com.cordys.coe.ac.emailio.monitor.IJMXEmailBoxPoller#incNrOfMessagesSuccess(long)
         */
        @Override public void incNrOfMessagesSuccess(long amount)
        {
        }

        /**
         * @see  com.cordys.coe.ac.emailio.monitor.IJMXEmailBoxPoller#notifyProcessingError(java.lang.Throwable,
         *       java.lang.String)
         */
        @Override public void notifyProcessingError(Throwable exception, String context)
        {
        }

        /**
         * @see  com.cordys.coe.ac.emailio.monitor.IJMXEmailBoxPoller#reset()
         */
        @Override public void reset()
        {
        }
    }

    /**
     * DOCUMENTME .
     *
     * @author  pgussow
     */
    private static class DummySMIMEConfiguration
        implements ISMIMEConfiguration
    {
        /**
         * @see  com.cordys.coe.ac.emailio.outbound.ISMIMEConfiguration#getBypassSMIME()
         */
        @Override public boolean getBypassSMIME()
        {
            return false;
        }

        /**
         * @see  com.cordys.coe.ac.emailio.outbound.ISMIMEConfiguration#getCertificateInfo(org.bouncycastle.cms.RecipientId)
         */
        @Override public ICertificateInfo getCertificateInfo(RecipientId riRecipientID)
                                                      throws KeyManagerException
        {
            return null;
        }

        /**
         * @see  com.cordys.coe.ac.emailio.outbound.ISMIMEConfiguration#getCertificateInfo(java.lang.String)
         */
        @Override public ICertificateInfo getCertificateInfo(String sEmailAddress)
                                                      throws KeyManagerException
        {
            return null;
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
            return false;
        }

        /**
         * @see  com.cordys.coe.ac.emailio.outbound.ISMIMEConfiguration#getSignMails()
         */
        @Override public boolean getSignMails()
        {
            return false;
        }

        /**
         * @see  com.cordys.coe.ac.emailio.outbound.ISMIMEConfiguration#getSMIMEEnabled()
         */
        @Override public boolean getSMIMEEnabled()
        {
            return false;
        }
    }
}
