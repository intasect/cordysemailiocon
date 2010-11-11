
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
package com.cordys.coe.test;

import com.cordys.coe.ac.emailio.config.EEmailBoxType;
import com.cordys.coe.ac.emailio.config.IIMAPEmailBox;
import com.cordys.coe.ac.emailio.config.action.IAction;
import com.cordys.coe.ac.emailio.config.trigger.ITrigger;
import com.cordys.coe.ac.emailio.connection.EmailConnectionFactory;
import com.cordys.coe.ac.emailio.connection.IEmailConnection;
import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;
import com.cordys.coe.ac.emailio.storage.IEmailStorageProvider;
import com.cordys.coe.ac.emailio.util.MailMessageUtil;

import com.eibus.management.IManagedComponent;

import javax.mail.Message;

/**
 * DOCUMENTME.
 *
 * @author  $author$
 */
public class TestIMAP
{
    /**
     * DOCUMENTME.
     */
    private static final String HOST = "cnd0986.vanenburg.com";
    /**
     * DOCUMENTME.
     */
    private static final int PORT = 143;
    /**
     * DOCUMENTME.
     */
    private static final String USERNAME = "imap";
    /**
     * DOCUMENTME.
     */
    private static final String PASSWORD = "imap";
    /**
     * DOCUMENTME.
     */
    private static final String NAME = "TestIMAP";
    /**
     * DOCUMENTME.
     */
    private static final String EMAIL_FOLDER = "Inbox";
    /**
     * DOCUMENTME.
     */
    private static final int POLL_INTERVAL = 5000;

    /**
     * Main method.
     *
     * @param  saArguments  The commandline arguments.
     */
    public static void main(String[] saArguments)
    {
        try
        {
            IEmailConnection ec = EmailConnectionFactory.createConnection(new IMAPEmailBox());
            Message[] am = ec.getEmailHeaders();

            for (int iCount = 0; iCount < am.length; iCount++)
            {
                Message msg = am[iCount];
                System.out.println("===============================");
                System.out.println("= Message " + iCount);
                System.out.println("===============================");

                System.out.println(MailMessageUtil.dumpMessage(msg));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * DOCUMENTME.
     *
     * @author  $author$
     */
    private static class IMAPEmailBox
        implements IIMAPEmailBox
    {
        /**
         * DOCUMENT ME!
         *
         * @param  trigger
         *
         * @see    com.cordys.coe.ac.emailio.config.IEmailBox#addTrigger(com.cordys.coe.ac.emailio.config.trigger.ITrigger)
         */
        public void addTrigger(ITrigger trigger)
        {
        }

        /**
         * DOCUMENTME.
         *
         * @param  trigger       DOCUMENTME
         * @param  isPersistent  DOCUMENTME
         */
        @Override public void addTrigger(ITrigger trigger, boolean isPersistent)
        {
        }

        /**
         * DOCUMENTME.
         *
         * @param   actionID
         *
         * @return
         *
         * @see     com.cordys.coe.ac.emailio.config.IEmailBox#getAction(java.lang.String)
         */
        public IAction getAction(String actionID)
        {
            return null;
        }

        /**
         * DOCUMENT ME!
         *
         * @return
         *
         * @see     com.cordys.coe.ac.emailio.config.IIMAPEmailBox#getEmailFolder()
         */
        public String[] getEmailFolders()
        {
            return new String[] { EMAIL_FOLDER };
        }

        /**
         * DOCUMENT ME!
         *
         * @return
         *
         * @see     com.cordys.coe.ac.emailio.config.IEmailBox#getHost()
         */
        public String getHost()
        {
            return HOST;
        }

        /**
         * @see  com.cordys.coe.ac.emailio.config.IEmailBox#getManagedComponent()
         */
        @Override public IManagedComponent getManagedComponent()
        {
            return null;
        }

        /**
         * DOCUMENT ME!
         *
         * @return
         *
         * @see     com.cordys.coe.ac.emailio.config.IEmailBox#getName()
         */
        public String getName()
        {
            return NAME;
        }

        /**
         * DOCUMENT ME!
         *
         * @return
         *
         * @see     com.cordys.coe.ac.emailio.config.IEmailBox#getPassword()
         */
        public String getPassword()
        {
            return PASSWORD;
        }

        /**
         * DOCUMENT ME!
         *
         * @return
         *
         * @see     com.cordys.coe.ac.emailio.config.IEmailBox#getPollInterval()
         */
        public int getPollInterval()
        {
            return POLL_INTERVAL;
        }

        /**
         * DOCUMENT ME!
         *
         * @return
         *
         * @see     com.cordys.coe.ac.emailio.config.IEmailBox#getPort()
         */
        public int getPort()
        {
            return PORT;
        }

        /**
         * @see  com.cordys.coe.ac.emailio.config.IEmailBox#getStorageProvider()
         */
        @Override public IEmailStorageProvider getStorageProvider()
        {
            return null;
        }

        /**
         * @see  com.cordys.coe.ac.emailio.config.IEmailBox#getStorageProviderConfiguration()
         */
        @Override public int getStorageProviderConfiguration()
        {
            return 0;
        }

        /**
         * DOCUMENT ME!
         *
         * @param   folderName
         *
         * @return
         *
         * @see     com.cordys.coe.ac.emailio.config.IEmailBox#getTriggers(java.lang.String)
         */
        public ITrigger[] getTriggers(String folderName)
        {
            return new ITrigger[0];
        }

        /**
         * DOCUMENT ME!
         *
         * @return
         *
         * @see     com.cordys.coe.ac.emailio.config.IEmailBox#getType()
         */
        public EEmailBoxType getType()
        {
            return EEmailBoxType.IMAP;
        }

        /**
         * DOCUMENT ME!
         *
         * @return
         *
         * @see     com.cordys.coe.ac.emailio.config.IEmailBox#getUsername()
         */
        public String getUsername()
        {
            return USERNAME;
        }

        /**
         * @see  com.cordys.coe.ac.emailio.config.IMailServer#isSSLEnabled()
         */
        @Override public boolean isSSLEnabled()
        {
            return false;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  trigger
         *
         * @see    com.cordys.coe.ac.emailio.config.IEmailBox#removeTrigger(com.cordys.coe.ac.emailio.config.trigger.ITrigger)
         */
        public void removeTrigger(ITrigger trigger)
        {
        }

        /**
         * DOCUMENT ME!
         *
         * @param  triggerName
         *
         * @see    com.cordys.coe.ac.emailio.config.IEmailBox#removeTrigger(com.cordys.coe.ac.emailio.config.trigger.ITrigger)
         */
        public void removeTrigger(String triggerName)
        {
        }

        /**
         * @see  com.cordys.coe.ac.emailio.config.IEmailBox#setManagedComponent(com.eibus.management.IManagedComponent)
         */
        @Override public void setManagedComponent(IManagedComponent mcManagedComponent)
        {
        }

        /**
         * DOCUMENT ME!
         *
         * @param   parent
         *
         * @return
         *
         * @see     com.cordys.coe.ac.emailio.config.IEmailBox#toXML(int)
         */
        public int toXML(int parent)
        {
            return 0;
        }

        /**
         * @see  com.cordys.coe.ac.emailio.config.IEmailBox#validate()
         */
        @Override public void validate()
                                throws EmailIOConfigurationException
        {
        }
    }
}
