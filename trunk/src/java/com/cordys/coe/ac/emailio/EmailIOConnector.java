


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
package com.cordys.coe.ac.emailio;

import com.cordys.coe.ac.emailio.config.EmailIOConfigurationFactory;
import com.cordys.coe.ac.emailio.config.IEmailBox;
import com.cordys.coe.ac.emailio.config.IEmailIOConfiguration;
import com.cordys.coe.ac.emailio.exception.EmailIOException;
import com.cordys.coe.ac.emailio.localization.InboundEmailExceptionMessages;
import com.cordys.coe.ac.emailio.localization.LogMessages;
import com.cordys.coe.ac.emailio.monitor.MonitorEmailBoxThread;
import com.cordys.coe.coelib.LibraryVersion;
import com.cordys.coe.util.system.SystemInfo;

import com.eibus.connector.nom.Connector;

import com.eibus.localization.ILocalizableString;

import com.eibus.management.IManagedComponent;

import com.eibus.soap.ApplicationConnector;
import com.eibus.soap.ApplicationTransaction;
import com.eibus.soap.Processor;
import com.eibus.soap.SOAPTransaction;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.threadpool.Dispatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Main application connector class for Email IO Connector.
 */
public class EmailIOConnector extends ApplicationConnector
{
    /**
     * Contains the logger instance.
     */
    private static CordysLogger LOG = CordysLogger.getCordysLogger(EmailIOConnector.class);
    /**
     * Holds the name of the connector.
     */
    private static final String CONNECTOR_NAME = "Email IO Connector Connector";
    /**
     * Holds the configuration object for this connector.
     */
    private IEmailIOConfiguration m_acConfiguration;
    /**
     * Holds the connector to use for sending messages to Cordys.
     */
    private Connector m_cConnector;
    /**
     * Holds the thread pool for executing the triggers.
     */
    private Dispatcher m_dThreadPool;
    /**
     * Holds the list of currently active poller threads.
     */
    private HashMap<String, MonitorEmailBoxThread> m_hmPollerThreads = new LinkedHashMap<String, MonitorEmailBoxThread>();
    /**
     * Holds teh sub component under which all poller threads are registered.
     */
    private IManagedComponent m_mcPollerThreads;

    /**
     * This method gets called when the processor is being stopped.
     *
     * @param  pProcessor  The processor that is being stopped.
     */
    @Override public void close(Processor pProcessor)
    {
        finishWatchers();

        if (LOG.isInfoEnabled())
        {
            LOG.info(LogMessages.CONNECTOR_STOPPED);
        }
    }

    /**
     * This method creates the transaction that will handle the requests.
     *
     * @param   stTransaction  The SOAP-transaction containing the message.
     *
     * @return  The newly created transaction.
     */
    @Override public ApplicationTransaction createTransaction(SOAPTransaction stTransaction)
    {
        return new EmailIOTransaction(m_acConfiguration);
    }

    /**
     * Returns the configuration object.
     *
     * @return  The configuration object for this application connector.
     */
    public IEmailIOConfiguration getConfigurationObject()
    {
        return m_acConfiguration;
    }

    /**
     * This method gets called when the processor is started. It reads the configuration of the
     * processor and creates the connector with the proper parameters. It will also create a client
     * connection to Cordys.
     *
     * @param  pProcessor  The processor that is started.
     */
    @Override public void open(Processor pProcessor)
    {
        // Check the CoELib version.
        try
        {
            LibraryVersion.loadAndCheckLibraryVersionFromResource(this.getClass(), true);
        }
        catch (Exception e)
        {
            LOG.fatal(e, LogMessages.COELIB_VERSION_MISMATCH);
            throw new IllegalStateException(e.toString());
        }

        try
        {
            if (LOG.isInfoEnabled())
            {
                LOG.info(LogMessages.CONNECTOR_STARTING, SystemInfo.getSystemInformation());
            }

            // Check if the coe.connector.startup.delay is set to an int bigger then 0.
            // If so we will sleep to allow attaching of the debugger.
            String sTemp = System.getProperty("coe.connector.startup.delay");

            if ((sTemp != null) && (sTemp.length() > 0))
            {
                try
                {
                    long lTime = Long.parseLong(sTemp);

                    if (lTime > 0)
                    {
                        if (LOG.isDebugEnabled())
                        {
                            LOG.debug("Going to pause for " + lTime +
                                      " ms to allow debugger attachment.");
                        }
                        Thread.sleep(lTime);
                    }
                }
                catch (Exception e)
                {
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Error checking for debugger delay", e);
                    }
                }
            }

            // Get the configuration
            m_acConfiguration = EmailIOConfigurationFactory.createConfiguration(getConfiguration(),
                                                                                getManagedComponent(),
                                                                                pProcessor
                                                                                .getSOAPProcessorEntry()
                                                                                .getDN());

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Creating thread pool with a maximum of " +
                          m_acConfiguration.getMaxWorkers() + " workers.");
            }

            // Create the thread pool and make sure it's monitored via JMX.
            m_dThreadPool = new Dispatcher("TriggerThreadPool", 1,
                                           m_acConfiguration.getMaxWorkers(), 250, 5 * 60 * 1000);
            m_dThreadPool.createManagedComponent(getManagedComponent());

            // Open the client connector
            m_cConnector = Connector.getInstance(CONNECTOR_NAME);

            if (!m_cConnector.isOpen())
            {
                m_cConnector.open();
            }

            m_acConfiguration.setConnector(m_cConnector);

            m_mcPollerThreads = getManagedComponent().createSubComponent("MailBoxPollers",
                                                                         "MailBoxPollers",
                                                                         LogMessages.JMX_MAILBOX_POLLERS,
                                                                         this);

            // Start up all poller threads
            startPollerThreads();

            // Figure out and log the version of the Java Mail API
            logJavaMailAPIVersion();

            if (LOG.isInfoEnabled())
            {
                LOG.info(LogMessages.CONNECTOR_STARTED);
            }
        }
        catch (Exception e)
        {
            LOG.fatal(e, LogMessages.CONNECTOR_START_EXCEPTION);
            throw new IllegalStateException(e);
        }
    }

    /**
     * This method gets called when the processor is ordered to rest.
     *
     * @param  processor  The processor that is to be in reset state
     */
    @Override public void reset(Processor processor)
    {
        // First stop all watchers in a nice way.
        finishWatchers();

        // Now create them all over again.
        try
        {
            startPollerThreads();
        }
        catch (EmailIOException e)
        {
            LOG.fatal(e, LogMessages.FAT_ERROR_DURING_RESET);
            throw new IllegalStateException("Error resetting the processor", e);
        }

        if (LOG.isInfoEnabled())
        {
            LOG.info(LogMessages.CONNECTOR_RESET);
        }
    }

    /**
     * Standard management method. Allows adding custom coumters, alert definitions and problems
     * definitions to this connector. Note that this method is called by the SOAP processor.
     *
     * @return  The JMX managed component created by the super class.
     */
    @Override protected IManagedComponent createManagedComponent()
    {
        IManagedComponent mc = super.createManagedComponent();

        // TODO: Add your counters, alert definitions and problem definitions here.
        return mc;
    }

    /**
     * Standard management method.
     *
     * @return  JMX type for this application connector.
     */
    @Override protected String getManagedComponentType()
    {
        return "AppConnector";
    }

    /**
     * Standard management method.
     *
     * @return  JMX description for this application connector.
     */
    @Override protected ILocalizableString getManagementDescription()
    {
        return LogMessages.CONNECTOR_MANAGEMENT_DESCRIPTION;
    }

    /**
     * Standard management method.
     *
     * @return  JMX name for this application connector.
     */
    @Override protected String getManagementName()
    {
        return "Email IO Connector";
    }

    /**
     * This method stops all email box pollers.
     */
    private void finishWatchers()
    {
        for (MonitorEmailBoxThread mebt : m_hmPollerThreads.values())
        {
            mebt.setShouldStop(true);
            mebt.interrupt();

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Waiting for thread " + mebt.getName() + " to finish.");
            }

            try
            {
                mebt.join();
            }
            catch (InterruptedException e)
            {
                if (LOG.isWarningEnabled())
                {
                    LOG.warn(e, LogMessages.WRN_WAITING_FOR_THREAD_TO_FINISH, mebt.getName());
                }
            }
        }

        m_hmPollerThreads.clear();

        // Make sure all sub components are removed.
        if (m_mcPollerThreads != null)
        {
            m_mcPollerThreads.unregisterComponentTree();
        }
    }

    /**
     * This method will try to log the javamail manifest information.
     */
    private void logJavaMailAPIVersion()
    {
        try
        {
            ArrayList<String> al = new ArrayList<String>();

            // Build up the array list
            String bootClasspath = System.getProperty("sun.boot.class.path");
            parseClasspath(al, bootClasspath);

            String jvmClasspath = System.getProperty("java.class.path");
            parseClasspath(al, jvmClasspath);

            String mailJarLocation = "";

            if (LOG.isDebugEnabled())
            {
                StringBuilder sb = new StringBuilder(2048);

                for (String entry : al)
                {
                    sb.append(entry).append("\n");
                }
                LOG.debug("Classpath:\n" + sb.toString());
            }

            for (String entry : al)
            {
                if (entry.matches("^(.+[\\\\/]){0,1}mail.jar$"))
                {
                    mailJarLocation = entry;
                    break;
                }
            }

            if (mailJarLocation.length() > 0)
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("The mail.jar which is being used: " + mailJarLocation);
                }

                JarFile jf = new JarFile(mailJarLocation);
                Manifest mf = jf.getManifest();
                Attributes a = mf.getMainAttributes();

                StringBuilder sb = new StringBuilder(2048);

                for (Object key : a.keySet())
                {
                    Object value = a.get(key);

                    sb.append(key.toString()).append(": ").append(value.toString()).append("\n");
                }

                LOG.info(LogMessages.INF_MAILJAR_LOCATION_0NJAR_DETAILSN1, mailJarLocation,
                         sb.toString());
            }
        }
        catch (Exception e)
        {
            LOG.warn(e,
                     LogMessages.WRN_COULD_NOT_DETERMINE_THE_VERSION_OF_THE_JAVAMAILAPI_BEING_USED);
        }
    }

    /**
     * This method parses the entries in the classpath and adds them to the array list.
     *
     * @param  al         The list to add the path entries to.
     * @param  classpath  The classpath to parse.
     */
    private void parseClasspath(ArrayList<String> al, String classpath)
    {
        String[] saEntries = classpath.split(System.getProperty("path.separator"));

        for (int iCount = 0; iCount < saEntries.length; iCount++)
        {
            al.add(saEntries[iCount]);
        }
    }

    /**
     * This method starts up all poller threads.
     *
     * @throws  EmailIOException  In case of any exceptions.
     */
    private void startPollerThreads()
                             throws EmailIOException
    {
        IEmailBox[] aebBoxes = m_acConfiguration.getEmailBoxes();

        for (int iCount = 0; iCount < aebBoxes.length; iCount++)
        {
            IEmailBox ebBox = aebBoxes[iCount];
            MonitorEmailBoxThread mebt = new MonitorEmailBoxThread(ebBox, m_dThreadPool,
                                                                   m_cConnector, m_mcPollerThreads,
                                                                   m_acConfiguration);

            if (m_hmPollerThreads.containsKey(ebBox.getName()))
            {
                throw new EmailIOException(InboundEmailExceptionMessages.IEE_THERE_IS_ALREADY_AN_EMAILBOX_WITH_NAME_0_CONFIGURED,
                                           ebBox.getName());
            }
            m_hmPollerThreads.put(ebBox.getName(), mebt);
        }

        // Start all threads.
        for (String sKey : m_hmPollerThreads.keySet())
        {
            MonitorEmailBoxThread mebt = m_hmPollerThreads.get(sKey);

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Starting poller thread for mailbox " + sKey);
            }
            mebt.start();
        }

        // Register all sub components (the pollers).
        m_mcPollerThreads.registerComponentTree();
    }
}
