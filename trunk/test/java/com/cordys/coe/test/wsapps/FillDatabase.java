package com.cordys.coe.test.wsapps;

import com.cordys.coe.ac.emailio.EmailIOConnectorConstants;
import com.cordys.coe.ac.emailio.config.EEmailBoxType;
import com.cordys.coe.ac.emailio.config.IEmailBox;
import com.cordys.coe.ac.emailio.config.action.IAction;
import com.cordys.coe.ac.emailio.config.trigger.ITrigger;
import com.cordys.coe.ac.emailio.config.trigger.TriggerFactory;
import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;
import com.cordys.coe.ac.emailio.exception.StorageProviderException;
import com.cordys.coe.ac.emailio.monitor.RuleContext;
import com.cordys.coe.ac.emailio.monitor.RuleContextContainer;
import com.cordys.coe.ac.emailio.storage.EmailStorageProviderFactory;
import com.cordys.coe.ac.emailio.storage.IEmailStorageProvider;
import com.cordys.coe.util.FileUtils;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.cordys.cpc.bsf.busobject.Config;

import com.eibus.management.IManagedComponent;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

import java.io.FileInputStream;

import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Session;

import javax.mail.internet.MimeMessage;

/**
 * DOCUMENTME .
 *
 * @author  pgussow
 */
public class FillDatabase
{
    /**
     * Holds the XML document.
     */
    private static Document s_dDoc = new Document();
    /**
     * Holds the template XML for creating the WsApps server embedded configuration.
     */
    private static final byte[] WSAPPS_CONFIG = ("<configuration implementation=\"com.cordys.cpc.bsf.connector.BsfConnector\" htmfile=\"/cordys/cpc/bsf/developer/content/bsftabpage.htm\">" +
                                                 "<ruleEnabled>false</ruleEnabled>" +
                                                 "<auditEnabled>off</auditEnabled><initializeDB>true</initializeDB><initializeEIS>false</initializeEIS><bsfuser/><customInitializer/></configuration>")
                                                .getBytes();

    /**
     * Main method.
     *
     * @param  saArguments  The commandline arguments.
     */
    public static void main(String[] saArguments)
    {
        try
        {
            // Create the dummy data.
            IEmailStorageProvider sp = createStorageProvider(FileUtils.readTextStreamContents(FillDatabase
                                                                                              .class
                                                                                              .getResourceAsStream("storageprovider.xml")));
            ITrigger tTrigger = createTrigger(FileUtils.readTextStreamContents(FillDatabase.class
                                                                               .getResourceAsStream("trigger.xml")));
            RuleContextContainer rccContext = createRuleContext();

            // Add it to the database.
            sp.addRuleContext(rccContext, tTrigger);
            sp.setContainerStatusCompleted(rccContext);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * This method creates a dummy rule context container.
     *
     * @return  The rule context.
     *
     * @throws  Exception
     */
    private static RuleContextContainer createRuleContext()
                                                                throws Exception
    {
        RuleContextContainer rccReturn = new RuleContextContainer();
        // Build up the properties
        Properties pSMTP = new Properties();
        pSMTP.put("mail.smtp.host", "dummt");
        pSMTP.put("mail.smtp.port", 25);

        Authenticator aAuth = null;

        // Create the session
        Session sSession = Session.getInstance(pSMTP, aAuth);

        MimeMessage mmNew = new MimeMessage(sSession,
                                            new FileInputStream(".\\docs\\internal\\testmessages\\viaAdobe.eml"));
        mmNew.setSentDate(new Date());

        RuleContext rc = new RuleContext(mmNew);

        rccReturn.add(rc);

        return rccReturn;
    }

    /**
     * DOCUMENTME.
     *
     * @param   sXML  readTextStreamContents
     *
     * @return
     *
     * @throws  Exception  DOCUMENTME
     */
    private static IEmailStorageProvider createStorageProvider(String sXML)
                                                        throws Exception
    {
        IEmailStorageProvider espReturn = null;

        XPathMetaInfo xmi = new XPathMetaInfo();
        xmi.addNamespaceBinding("ns", EmailIOConnectorConstants.NS_CONFIGURATION);

        int iSP = s_dDoc.parseString(sXML);
        int iWsAppsConfig = s_dDoc.load(WSAPPS_CONFIG);

        try
        {
            int iComponent = XPathHelper.selectSingleNode(iSP, "//ns:component", xmi);

            // Fix the BSF user
            int iBsfUser = XPathHelper.selectSingleNode(iWsAppsConfig, "bsfuser");
            s_dDoc.createText("cn=SYSTEM,cn=organizational users,o=system,cn=cordys,o=vanenburg.com",
                              iBsfUser);

            // Add the component.
            Node.duplicateAndAppendToChildren(iComponent, iComponent, iWsAppsConfig);

            // Create the WsAppS config before we go in.
            Config cWsAppsConfig = new Config(null, "EmailIOConnectionPool", iWsAppsConfig,
                                              false);
            cWsAppsConfig.setConfig(iWsAppsConfig);

            EmailStorageProviderFactory.createStorageProvider(new DummyEmailBox(iSP),
                                                              "cn=dummy,cn=dummy,cn=soap nodes,o=system,cn=cordys,o=vanenburg.com",
                                                              true, null);
        }
        finally
        {
            Node.delete(iSP);
            Node.delete(iWsAppsConfig);
        }

        return espReturn;
    }

    /**
     * DOCUMENTME.
     *
     * @param   sXML  DOCUMENTME
     *
     * @return
     *
     * @throws  Exception  DOCUMENTME
     */
    private static ITrigger createTrigger(String sXML)
                                   throws Exception
    {
        ITrigger tReturn = null;

        int iSP = s_dDoc.parseString(sXML);

        try
        {
            tReturn = TriggerFactory.createTrigger(iSP, null);
        }
        finally
        {
            Node.delete(iSP);
        }

        return tReturn;
    }

    /**
     * This class is used to create the global storage provider.
     *
     * @author  pgussow
     */
    private static class DummyEmailBox
        implements IEmailBox
    {
        /**
         * DOCUMENTME.
         */
        private int m_iStorageProvider;

        /**
         * Creates a new DummyEmailBox object.
         *
         * @param  iStorageProvider  isp
         */
        public DummyEmailBox(int iStorageProvider)
        {
            m_iStorageProvider = iStorageProvider;
        }

        /**
         * @see  com.cordys.coe.ac.emailio.config.IEmailBox#addTrigger(com.cordys.coe.ac.emailio.config.trigger.ITrigger)
         */
        @Override public void addTrigger(ITrigger trigger)
                                  throws StorageProviderException
        {
        }

        /**
         * @see  com.cordys.coe.ac.emailio.config.IEmailBox#addTrigger(com.cordys.coe.ac.emailio.config.trigger.ITrigger,
         *       boolean)
         */
        @Override public void addTrigger(ITrigger trigger, boolean isPersistent)
                                  throws StorageProviderException
        {
        }

        /**
         * @see  com.cordys.coe.ac.emailio.config.action.IActionStore#getAction(java.lang.String)
         */
        @Override public IAction getAction(String actionID)
        {
            return null;
        }

        /**
         * @see  com.cordys.coe.ac.emailio.config.IEmailBox#getEmailFolders()
         */
        @Override public String[] getEmailFolders()
        {
            return null;
        }

        /**
         * @see  com.cordys.coe.ac.emailio.config.IEmailBox#getHost()
         */
        @Override public String getHost()
        {
            return null;
        }

        /**
         * @see  com.cordys.coe.ac.emailio.config.IEmailBox#getName()
         */
        @Override public String getName()
        {
            return GLOBAL_EMAIL_BOX;
        }

        /**
         * @see  com.cordys.coe.ac.emailio.config.IEmailBox#getPassword()
         */
        @Override public String getPassword()
        {
            return null;
        }

        /**
         * @see  com.cordys.coe.ac.emailio.config.IEmailBox#getPollInterval()
         */
        @Override public int getPollInterval()
        {
            return 0;
        }

        /**
         * @see  com.cordys.coe.ac.emailio.config.IEmailBox#getPort()
         */
        @Override public int getPort()
        {
            return 0;
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
            return m_iStorageProvider;
        }

        /**
         * @see  com.cordys.coe.ac.emailio.config.IEmailBox#getTriggers(java.lang.String)
         */
        @Override public ITrigger[] getTriggers(String folderName)
        {
            return null;
        }

        /**
         * @see  com.cordys.coe.ac.emailio.config.IEmailBox#getType()
         */
        @Override public EEmailBoxType getType()
        {
            return null;
        }

        /**
         * @see  com.cordys.coe.ac.emailio.config.IEmailBox#getUsername()
         */
        @Override public String getUsername()
        {
            return null;
        }

        /**
         * @see  com.cordys.coe.ac.emailio.config.IEmailBox#removeTrigger(com.cordys.coe.ac.emailio.config.trigger.ITrigger)
         */
        @Override public void removeTrigger(ITrigger trigger)
                                     throws StorageProviderException
        {
        }

        /**
         * @see  com.cordys.coe.ac.emailio.config.IEmailBox#removeTrigger(java.lang.String)
         */
        @Override public void removeTrigger(String triggername)
                                     throws StorageProviderException
        {
        }

        /**
         * @see  com.cordys.coe.ac.emailio.config.IEmailBox#toXML(int)
         */
        @Override public int toXML(int parent)
        {
            return 0;
        }

		/**
		 * @see com.cordys.coe.ac.emailio.config.IEmailBox#getManagedComponent()
		 */
		@Override
		public IManagedComponent getManagedComponent()
		{
			return null;
		}

		/**
		 * @see com.cordys.coe.ac.emailio.config.IEmailBox#setManagedComponent(com.eibus.management.IManagedComponent)
		 */
		@Override
		public void setManagedComponent(IManagedComponent mcManagedComponent)
		{
		}

		/**
		 * @see com.cordys.coe.ac.emailio.config.IEmailBox#validate()
		 */
		@Override
		public void validate() throws EmailIOConfigurationException
		{
		}

		@Override
		public boolean isSSLEnabled()
		{
			return false;
		}
    }
}
