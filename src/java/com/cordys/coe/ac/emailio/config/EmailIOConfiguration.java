/**
 * (c) 2007 Cordys R&D B.V. All rights reserved.
 * The computer program(s) is the proprietary information of Cordys R&D B.V.
 * and provided under the relevant License Agreement containing restrictions
 * on use and disclosure. Use is subject to the License Agreement.
 */
package com.cordys.coe.ac.emailio.config;

import com.cordys.coe.ac.emailio.EmailIOConnectorConstants;
import com.cordys.coe.ac.emailio.config.action.IAction;
import com.cordys.coe.ac.emailio.config.outbound.ISMTPServer;
import com.cordys.coe.ac.emailio.config.outbound.SMTPServerFactory;
import com.cordys.coe.ac.emailio.config.trigger.ITrigger;
import com.cordys.coe.ac.emailio.connection.ISMTPConnectionPool;
import com.cordys.coe.ac.emailio.connection.SMTPConnectionPoolFactory;
import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;
import com.cordys.coe.ac.emailio.exception.KeyManagerException;
import com.cordys.coe.ac.emailio.exception.StorageProviderException;
import com.cordys.coe.ac.emailio.keymanager.ICertificateInfo;
import com.cordys.coe.ac.emailio.keymanager.IKeyManager;
import com.cordys.coe.ac.emailio.keymanager.KeyManagerFactory;
import com.cordys.coe.ac.emailio.localization.EmailIOConfigurationExceptionMessages;
import com.cordys.coe.ac.emailio.localization.LogMessages;
import com.cordys.coe.ac.emailio.objects.IStorageProviderQueryManager;
import com.cordys.coe.ac.emailio.storage.EmailStorageProviderFactory;
import com.cordys.coe.ac.emailio.storage.IEmailStorageProvider;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.connector.nom.Connector;

import com.eibus.management.IManagedComponent;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.system.EIBProperties;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

import java.io.ByteArrayInputStream;
import java.io.File;

import java.security.PrivateKey;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;

import javax.xml.XMLConstants;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.bouncycastle.cms.RecipientId;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This class holds the configuration details for the Email IO Connector.
 */
class EmailIOConfiguration
    implements IEmailIOConfiguration
{
    /**
     * Holds the name of the tag ''.
     */
    private static final String TAG_INBOUND = "inbound";
    /**
     * Holds the name of the tag ''.
     */
    private static final String TAG_EMAILBOXES = "emailboxes";
    /**
     * Holds the name of the tag 'configuration'.
     */
    private static final String TAG_CONFIGURATION = "configuration";
    /**
     * Holds the name of the tag 'outbound'.
     */
    private static final String TAG_OUTBOUND = "outbound";
    /**
     * Holds the name of the tag 'keymanagers'.
     */
    private static final String TAG_KEY_MANAGERS = "keymanagers";
    /**
     * Holds the name of the tag 'smtpservers'.
     */
    private static final String TAG_SMTP_SERVERS = "smtpservers";
    /**
     * Holds the name of the tag 'smimeenabled'.
     */
    private static final String TAG_SMIME_ENABLED = "smimeenabled";
    /**
     * Holds the name of the tag 'general'.
     */
    private static final String TAG_GENERAL = "general";
    /**
     * Holds the name of the tag 'signmails'.
     */
    private static final String TAG_SIGN_MAILS = "signmails";
    /**
     * Holds the name of the tag 'encryptmails'.
     */
    private static final String TAG_ENCRYPT_MAILS = "encryptmails";
    /**
     * Holds the name of the tag 'checkcrl'.
     */
    private static final String TAG_CHECK_CRL = "checkcrl";
    /**
     * Holds the name of the tag 'bypassmime'.
     */
    private static final String TAG_BYPASS_SMIME = "bypasssmime";
    /**
     * Contains the logger instance.
     */
    private static CordysLogger LOG = CordysLogger.getCordysLogger(EmailIOConfiguration.class);
    /**
     * Holds an empty string.
     */
    private static final String EMPTY_STRING = "";
    /**
     * Holds the name of the tag 'storage'.
     */
    private static final String TAG_STORAGE = "storage";
    /**
     * Holds the name of the tag 'emailbox'.
     */
    private static final String TAG_EMAILBOX = "emailbox";
    /**
     * Holds the name of the tag 'maxworkers'.
     */
    private static final String TAG_MAX_WORKERS = "maxworkers";
    /**
     * Holds the name of the tag 'keymanager'.
     */
    private static final String TAG_KEY_MANAGER = "keymanager";
    /**
     * Holds the name of the tag 'smtpserver'.
     */
    private static final String TAG_SMTP_SERVER = "smtpserver";
    /**
     * Holds whether or not the S/MIME support should be used at all. When false it will function as
     * a normal SMTP service.
     */
    private boolean m_bBypassSMIME;
    /**
     * Holds whether or not the CRL should be checked in case a CRL is available from the
     * certificate. If this is true then by default a certificate for which the CRL could not be
     * accessed will be considered invalid.
     */
    private boolean m_bCheckCRL;
    /**
     * Holds whether or not to encrypt the mails that are being sent.
     */
    private boolean m_bEncryptMails;
    /**
     * Holds whether or not the outgoing mails should be signed.
     */
    private boolean m_bSignMails;
    /**
     * Holds whether or not S/MIME should be used.
     */
    private boolean m_bSMIMEEnabled;
    /**
     * Holds the connector to use for sending requests to the bus.
     */
    private Connector m_cConnector;
    /**
     * Holds the storage provider that should be used.
     */
    private IEmailStorageProvider m_espStorage;
    /**
     * Holds the maximum workers that can send messages to Cordys.
     */
    private int m_iMaxWorkers;
    /**
     * Holds the global storage provider configuration.
     */
    private int m_iStorageProviderConfiguration;
    /**
     * Holds all the email boxes that should be monitored.
     */
    private Map<String, IEmailBox> m_mBoxes = new LinkedHashMap<String, IEmailBox>();
    /**
     * Holds the parent managed component.
     */
    private IManagedComponent m_mcParent;
    /**
     * Holds all the Key managers configured.
     */
    private Map<String, IKeyManager> m_mKeyManagers = new LinkedHashMap<String, IKeyManager>();
    /**
     * Holds all the SMTP servers that are configured.
     */
    private Map<String, ISMTPConnectionPool> m_mSMTPServers = new LinkedHashMap<String, ISMTPConnectionPool>();
    /**
     * Holds the DN of the SOAP processor.
     */
    private String m_sSOAPProcessorDN;

    /**
     * @see  com.cordys.coe.ac.emailio.config.IEmailIOConfiguration#getBypassSMIME()
     */
    @Override public boolean getBypassSMIME()
    {
        return m_bBypassSMIME;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.IEmailIOConfiguration#getCertificateInfo(java.lang.String)
     */
    @Override public ICertificateInfo getCertificateInfo(String sEmailAddress)
                                                  throws KeyManagerException
    {
        ICertificateInfo ciReturn = null;

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Finding private key for email address " + sEmailAddress);
        }

        for (IKeyManager km : m_mKeyManagers.values())
        {
            ciReturn = km.getCertificateInfo(sEmailAddress);

            if (ciReturn != null)
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Found certificate information for address " + sEmailAddress +
                              " using key manager " + km.getName());
                }
                break;
            }
        }

        return ciReturn;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.outbound.ISMIMEConfiguration#getCertificateInfo(RecipientId)
     */
    @Override public ICertificateInfo getCertificateInfo(RecipientId riRecipientID)
                                                  throws KeyManagerException
    {
        ICertificateInfo ciReturn = null;

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Finding private key for recipient " + riRecipientID);
        }

        for (IKeyManager km : m_mKeyManagers.values())
        {
            ciReturn = km.getCertificateInfo(riRecipientID);

            if (ciReturn != null)
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Found certificate information for recipient " + riRecipientID +
                              " using key manager " + km.getName());
                }
                break;
            }
        }

        return ciReturn;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.IEmailIOConfiguration#getCheckCRL()
     */
    @Override public boolean getCheckCRL()
    {
        return m_bCheckCRL;
    }

    /**
     * This method gets the connector to use for sending requests to the bus.
     *
     * @return  The connector to use for sending requests to the bus.
     *
     * @see     com.cordys.coe.ac.emailio.config.IEmailIOConfiguration#getConnector()
     */
    public Connector getConnector()
    {
        return m_cConnector;
    }

    /**
     * This method returns the mailbox with the given name. If the mailbox is not found null is
     * returned.
     *
     * @param   sMailboxName  The name of the mail box.
     *
     * @return  The mailbox with the given name. If it's not found null is returned.
     *
     * @see     com.cordys.coe.ac.emailio.config.IEmailIOConfiguration#getEmailBox(java.lang.String)
     */
    public IEmailBox getEmailBox(String sMailboxName)
    {
        IEmailBox ebReturn = null;

        ebReturn = m_mBoxes.get(sMailboxName);

        return ebReturn;
    }

    /**
     * This method gets the email boxes that should be monitored.
     *
     * @return  The email boxes that should be monitored.
     *
     * @see     com.cordys.coe.ac.emailio.config.IEmailIOConfiguration#getEmailBoxes()
     */
    public IEmailBox[] getEmailBoxes()
    {
        return m_mBoxes.values().toArray(new IEmailBox[0]);
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.IEmailIOConfiguration#getEncryptMails()
     */
    @Override public boolean getEncryptMails()
    {
        return m_bEncryptMails;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.IEmailIOConfiguration#getKeyManagers()
     */
    @Override public IKeyManager[] getKeyManagers()
    {
        return m_mKeyManagers.values().toArray(new IKeyManager[0]);
    }

    /**
     * This mehtod returns the managed component of the parent.
     *
     * @return  The managed component of the parent.
     *
     * @see     com.cordys.coe.ac.emailio.config.IEmailIOConfiguration#getManagedComponent()
     */
    public IManagedComponent getManagedComponent()
    {
        return m_mcParent;
    }

    /**
     * This method gets the max number of workers.
     *
     * @return  The max number of workers.
     *
     * @see     com.cordys.coe.ac.emailio.config.IEmailIOConfiguration#getMaxWorkers()
     */
    public int getMaxWorkers()
    {
        return m_iMaxWorkers;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.IEmailIOConfiguration#getPrivateKey(java.lang.String)
     */
    @Override public PrivateKey getPrivateKey(String sEmailAddress)
    {
        PrivateKey pkReturn = null;

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Finding private key for email address " + sEmailAddress);
        }

        for (IKeyManager km : m_mKeyManagers.values())
        {
            pkReturn = km.getPrivateKey(sEmailAddress);

            if (pkReturn != null)
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Found private key for address " + sEmailAddress +
                              " using key manager " + km.getName());
                }
                break;
            }
        }

        return pkReturn;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.IEmailIOConfiguration#getSignMails()
     */
    @Override public boolean getSignMails()
    {
        return m_bSignMails;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.IEmailIOConfiguration#getSMIMEEnabled()
     */
    @Override public boolean getSMIMEEnabled()
    {
        return m_bSMIMEEnabled;
    }

    /**
     * This method gets the default connection pool (first one configured).
     *
     * @return  The default connection pool.
     */
    public ISMTPConnectionPool getSMTPConnectionPool()
    {
        return m_mSMTPServers.get(m_mSMTPServers.keySet().iterator().next());
    }

    /**
     * This method gets the connection pool with the given name.
     *
     * @param   sName  The name of the pool to return.
     *
     * @return  The connection pool with the given name.
     */
    public ISMTPConnectionPool getSMTPConnectionPool(String sName)
    {
        return m_mSMTPServers.get(sName);
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.IEmailIOConfiguration#getSMTPServers()
     */
    @Override public ISMTPServer[] getSMTPServers()
    {
        return m_mSMTPServers.values().toArray(new ISMTPServer[0]);
    }

    /**
     * This method gets the DN of the SOAP processor.
     *
     * @return  The DN of the SOAP processor.
     *
     * @see     com.cordys.coe.ac.emailio.config.IEmailIOConfiguration#getSOAPProcessorDN()
     */
    public String getSOAPProcessorDN()
    {
        return m_sSOAPProcessorDN;
    }

    /**
     * This method gets the storage that should be used.
     *
     * @return  The storage that should be used.
     *
     * @see     com.cordys.coe.ac.emailio.config.IEmailIOConfiguration#getStorageProvider()
     */
    public IEmailStorageProvider getStorageProvider()
    {
        return m_espStorage;
    }

    /**
     * This method gets the global storage configuration. If this method returns 0 the default store
     * is supposed to be used.
     *
     * @return  The global storage configuration.
     *
     * @see     com.cordys.coe.ac.emailio.config.IEmailIOConfiguration#getStorageProviderConfiguration()
     */
    public int getStorageProviderConfiguration()
    {
        return m_iStorageProviderConfiguration;
    }

    /**
     * This method gets the storage provider query manager. The object returned is used to support
     * the management user interfaces. If there is no global storage provider or a global storage
     * provider that does not implement the {@link IStorageProviderQueryManager} interface this
     * method will return null.
     *
     * @return  The storage provider query manager. The object returned is used to support the
     *          management user interfaces. If there is no global storage provider or a global
     *          storage provider that does not implement the {@link IStorageProviderQueryManager}
     *          interface this method will return null.
     *
     * @see     com.cordys.coe.ac.emailio.config.IEmailIOConfiguration#getStorageProviderQueryManager()
     */
    public IStorageProviderQueryManager getStorageProviderQueryManager()
    {
        IStorageProviderQueryManager spqReturn = null;

        if ((m_espStorage != null) && (m_espStorage instanceof IStorageProviderQueryManager))
        {
            spqReturn = (IStorageProviderQueryManager) m_espStorage;
        }

        return spqReturn;
    }

    /**
     * Creates the constructor.This loads the configuration object and pass it to XMLProperties for
     * processing.
     *
     * @param   iConfigNode       The xml-node that contains the configuration.
     * @param   mcParent          The parent managed component for JMX.
     * @param   sSOAPProcessorDN  The DN of the SOAP processor in which the storage provider is
     *                            running.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     *
     * @see     com.cordys.coe.ac.emailio.config.IEmailIOConfiguration#initialize(int,com.eibus.management.IManagedComponent,
     *          java.lang.String)
     */
    @Override public void initialize(int iConfigNode, IManagedComponent mcParent,
                                     String sSOAPProcessorDN)
                              throws EmailIOConfigurationException
    {
        // Check the configuration XML.
        if (iConfigNode == 0)
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_CONFIGURATION_NOT_FOUND);
        }

        m_mcParent = mcParent;
        m_sSOAPProcessorDN = sSOAPProcessorDN;

        XPathMetaInfo xmi = new XPathMetaInfo();
        xmi.addNamespaceBinding("ns", EmailIOConnectorConstants.NS_CONFIGURATION);

        iConfigNode = XPathHelper.selectSingleNode(iConfigNode, "./ns:" + TAG_CONFIGURATION, xmi);

        if (iConfigNode == 0)
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_CONFIGURATION_2ND_LEVEL_NOT_FOUND);
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Configuration XML:\n" + Node.writeToString(iConfigNode, false));
        }

        if (!Node.getName(iConfigNode).equals(TAG_CONFIGURATION))
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_ROOTTAG_OF_THE_CONFIGURATION_SHOULD_BE_CONFIGURATION);
        }

        // Now what we'll do is validate the XML against the XSD in order to be able to filter out
        // a lot of problems.
        validateAgainstXSD(iConfigNode);

        // Now parse the global storage configuration. This configuration is mandatory from 2.0
        // onwards. Individual email box configuration is still supported, but a Global one must be
        // provided.
        m_iStorageProviderConfiguration = XPathHelper.selectSingleNode(iConfigNode,
                                                                       "./ns:" + TAG_GENERAL +
                                                                       "/ns:" + TAG_STORAGE, xmi);

        if (m_iStorageProviderConfiguration == 0)
        {
            throw new EmailIOConfigurationException(LogMessages.WRN_NO_STORAGE_CONFIGURED_USING_THE_DEFAULT_STORAGE);
        }
        else
        {
            try
            {
                m_espStorage = EmailStorageProviderFactory.createStorageProvider(new GlobalEmailBox(),
                                                                                 sSOAPProcessorDN,
                                                                                 true, mcParent);
            }
            catch (StorageProviderException e)
            {
                throw new EmailIOConfigurationException(e,
                                                        EmailIOConfigurationExceptionMessages.EICE_ERROR_CREATING_GLOBAL_STORAGE_INSTANCE);
            }
        }

        // Find all email boxes to monitor.
        int[] aiEmailBoxes = XPathHelper.selectNodes(iConfigNode,
                                                     "./ns:" + TAG_INBOUND + "/ns:" +
                                                     TAG_EMAILBOXES + "/ns:" + TAG_EMAILBOX, xmi);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Found " + aiEmailBoxes.length + " email boxes to monitor.");
        }

        // It could be that there is nothing to monitor and that the connector is only used for
        // outbound traffic.
        if (aiEmailBoxes.length == 0)
        {
            LOG.warn(null,
                     EmailIOConfigurationExceptionMessages.EICE_NO_EMAIL_BOXES_FOUND_TO_MONITOR);
        }

        for (int iCount = 0; iCount < aiEmailBoxes.length; iCount++)
        {
            IEmailBox ebNew = EmailBoxFactory.createEmailBox(aiEmailBoxes[iCount],
                                                             m_iStorageProviderConfiguration,
                                                             sSOAPProcessorDN);

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Added email box:\n" + ebNew.toString());
            }

            if (m_mBoxes.containsKey(ebNew.getName()))
            {
                throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_DUPLICATE_EMAIL_BOX_NAME,
                                                        ebNew.getName());
            }

            m_mBoxes.put(ebNew.getName(), ebNew);
        }

        // Get the maximum number of worker threads.
        m_iMaxWorkers = XPathHelper.getIntegerValue(iConfigNode,
                                                    "./ns:" + TAG_INBOUND + "/ns:" +
                                                    TAG_MAX_WORKERS, xmi, 10);

        // Now we need to parse the outbound data. Both the key managers, general outbound
        // settings and actual SMTP servers.
        // Key managers
        try
        {
            int[] aiKeyManagers = XPathHelper.selectNodes(iConfigNode,
                                                          "./ns:" + TAG_OUTBOUND + "/ns:" +
                                                          TAG_KEY_MANAGERS + "/ns:" +
                                                          TAG_KEY_MANAGER, xmi);

            for (int iKeyManager : aiKeyManagers)
            {
                IKeyManager km = KeyManagerFactory.createKeyManager(this, iKeyManager);
                m_mKeyManagers.put(km.getName(), km);
            }
        }
        catch (KeyManagerException kme)
        {
            throw new EmailIOConfigurationException(kme.getMessageObject(),
                                                    kme.getMessageParameters());
        }

        // SMTP servers
        int[] aiSMTPServers = XPathHelper.selectNodes(iConfigNode,
                                                      "./ns:" + TAG_OUTBOUND + "/ns:" +
                                                      TAG_SMTP_SERVERS + "/ns:" + TAG_SMTP_SERVER,
                                                      xmi);

        for (int iSMTPServer : aiSMTPServers)
        {
            ISMTPServer ssServer = SMTPServerFactory.createSMTPServer(iSMTPServer, mcParent);

            ISMTPConnectionPool scp = SMTPConnectionPoolFactory.createConnectionPool(ssServer,
                                                                                     this);

            m_mSMTPServers.put(ssServer.getName(), scp);
        }

        // General outbound settings.
        m_bSMIMEEnabled = XPathHelper.getBooleanValue(iConfigNode,
                                                      "./ns:" + TAG_OUTBOUND + "/ns:" +
                                                      TAG_SMIME_ENABLED, xmi, false);
        m_bSignMails = XPathHelper.getBooleanValue(iConfigNode,
                                                   "./ns:" + TAG_OUTBOUND + "/ns:" + TAG_SIGN_MAILS,
                                                   xmi, false);
        m_bEncryptMails = XPathHelper.getBooleanValue(iConfigNode,
                                                      "./ns:" + TAG_OUTBOUND + "/ns:" +
                                                      TAG_ENCRYPT_MAILS, xmi, false);
        m_bCheckCRL = XPathHelper.getBooleanValue(iConfigNode,
                                                  "./ns:" + TAG_OUTBOUND + "/ns:" + TAG_CHECK_CRL,
                                                  xmi, false);
        m_bBypassSMIME = XPathHelper.getBooleanValue(iConfigNode,
                                                     "./ns:" + TAG_OUTBOUND + "/ns:" +
                                                     TAG_BYPASS_SMIME, xmi, false);

        // Register the S/MIME stuff in order to be able to at least process S/MIME content.
        MailcapCommandMap mailcap = (MailcapCommandMap) CommandMap.getDefaultCommandMap();

        mailcap.addMailcap("application/pkcs7-signature;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.pkcs7_signature");
        mailcap.addMailcap("application/pkcs7-mime;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.pkcs7_mime");
        mailcap.addMailcap("application/x-pkcs7-signature;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.x_pkcs7_signature");
        mailcap.addMailcap("application/x-pkcs7-mime;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.x_pkcs7_mime");
        mailcap.addMailcap("multipart/signed;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.multipart_signed");

        CommandMap.setDefaultCommandMap(mailcap);
    }

    /**
     * This method sets the connector to use for sending requests to the bus.
     *
     * @param  cConnector  The connector to use for sending requests to the bus.
     *
     * @see    com.cordys.coe.ac.emailio.config.IEmailIOConfiguration#setConnector(com.eibus.connector.nom.Connector)
     */
    public void setConnector(Connector cConnector)
    {
        m_cConnector = cConnector;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.IEmailIOConfiguration#toXML(int)
     */
    @Override public int toXML(int iParent)
    {
        int iReturn = 0;

        iReturn = Node.createElementNS(TAG_CONFIGURATION, EMPTY_STRING, EMPTY_STRING,
                                       EmailIOConnectorConstants.NS_CONFIGURATION, iParent);

        if (m_mBoxes.size() > 0)
        {
            int iInbound = Node.createElementWithParentNS(TAG_INBOUND, EMPTY_STRING, iReturn);
            int iEmailBoxes = Node.createElementWithParentNS(TAG_EMAILBOXES, EMPTY_STRING,
                                                             iInbound);

            for (IEmailBox ebBox : m_mBoxes.values())
            {
                ebBox.toXML(iEmailBoxes);
            }
        }

        // Now do the outbound part.
        if ((m_mKeyManagers.size() > 0) || (m_mSMTPServers.size() > 0))
        {
            int iOutbound = Node.createElementWithParentNS(TAG_OUTBOUND, EMPTY_STRING, iReturn);

            // Key managers
            if (m_mKeyManagers.size() > 0)
            {
                int iKeyManagers = Node.createElementWithParentNS(TAG_KEY_MANAGERS, EMPTY_STRING,
                                                                  iOutbound);

                for (IKeyManager km : m_mKeyManagers.values())
                {
                    km.toXML(iKeyManagers);
                }
            }

            // SMTP servers
            if (m_mSMTPServers.size() > 0)
            {
                int iSMTPServers = Node.createElementWithParentNS(TAG_SMTP_SERVERS, EMPTY_STRING,
                                                                  iOutbound);

                for (ISMTPConnectionPool scp : m_mSMTPServers.values())
                {
                    scp.getConfiguration().toXML(iSMTPServers);
                }
            }

            Node.createElementWithParentNS(TAG_SMIME_ENABLED, String.valueOf(getSMIMEEnabled()),
                                           iOutbound);
            Node.createElementWithParentNS(TAG_SIGN_MAILS, String.valueOf(getSignMails()),
                                           iOutbound);
            Node.createElementWithParentNS(TAG_ENCRYPT_MAILS, String.valueOf(getEncryptMails()),
                                           iOutbound);
            Node.createElementWithParentNS(TAG_CHECK_CRL, String.valueOf(getCheckCRL()), iOutbound);
            Node.createElementWithParentNS(TAG_BYPASS_SMIME, String.valueOf(getBypassSMIME()),
                                           iOutbound);
        }

        // Last but not least: the general properties (like storage provider.
        if (m_espStorage != null)
        {
            int iGeneral = Node.createElementWithParentNS(TAG_GENERAL, EMPTY_STRING, iReturn);
            m_espStorage.toXML(iGeneral);
        }

        return iReturn;
    }

    /**
     * This method will validate the configuration XML against the XSD. This is to make sure the XML
     * is at least XSD-valid. Some other semantics are not checked at this point (like action-refs
     * etc.).
     *
     * @param   iConfigNode
     *
     * @throws  EmailIOConfigurationException  In case the validation fails or the configuration
     *                                         validation fails.
     */
    private void validateAgainstXSD(int iConfigNode)
                             throws EmailIOConfigurationException
    {
        try
        {
            String sXML = Node.writeToString(iConfigNode, true);
            File fXSD = new File(EIBProperties.getInstallDir(),
                                 EmailIOConnectorConstants.LOCATION_CONFIGURATION_XSD);

            // Set up the schema
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(fXSD);

            // Set up the XML parser
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setIgnoringElementContentWhitespace(true);
            dbf.setIgnoringComments(true);
            dbf.setSchema(schema);

            // Create the new document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            LocalErrorHandler ehHandler = new LocalErrorHandler();
            db.setErrorHandler(ehHandler);

            db.parse(new ByteArrayInputStream(sXML.getBytes()));

            if (!ehHandler.isValid())
            {
                throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_THE_CONFIGURATION_XML_IS_INVALID,
                                                        ehHandler.toString(), sXML);
            }
        }
        catch (EmailIOConfigurationException iece)
        {
            throw iece;
        }
        catch (Exception e)
        {
            throw new EmailIOConfigurationException(e,
                                                    EmailIOConfigurationExceptionMessages.EICE_ERROR_VALIDATING_CONFIGURATION);
        }
    }

    /**
     * This class is used to create the global storage provider.
     *
     * @author  pgussow
     */
    private class GlobalEmailBox
        implements IEmailBox
    {
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
         * @see  com.cordys.coe.ac.emailio.config.IEmailBox#getManagedComponent()
         */
        @Override public IManagedComponent getManagedComponent()
        {
            return m_mcParent;
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
            return m_espStorage;
        }

        /**
         * @see  com.cordys.coe.ac.emailio.config.IEmailBox#getStorageProviderConfiguration()
         */
        @Override public int getStorageProviderConfiguration()
        {
            return m_iStorageProviderConfiguration;
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
         * @see  com.cordys.coe.ac.emailio.config.IMailServer#isSSLEnabled()
         */
        @Override public boolean isSSLEnabled()
        {
            return false;
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
         * @see  com.cordys.coe.ac.emailio.config.IEmailBox#setManagedComponent(com.eibus.management.IManagedComponent)
         */
        @Override public void setManagedComponent(IManagedComponent mcManagedComponent)
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
         * @see  com.cordys.coe.ac.emailio.config.IEmailBox#validate()
         */
        @Override public void validate()
                                throws EmailIOConfigurationException
        {
        }
    }

    /**
     * This class handles and stores the XSD validation errors that might occur.
     *
     * @author  pgussow
     */
    private class LocalErrorHandler
        implements ErrorHandler
    {
        /**
         * Holds the errors.
         */
        private ArrayList<SAXParseException> m_alErrors = new ArrayList<SAXParseException>();
        /**
         * Holds the fatal errors.
         */
        private ArrayList<SAXParseException> m_alFatals = new ArrayList<SAXParseException>();
        /**
         * Holds the warnings.
         */
        private ArrayList<SAXParseException> m_alWarnings = new ArrayList<SAXParseException>();

        /**
         * @see  org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
         */
        @Override public void error(SAXParseException exception)
                             throws SAXException
        {
            m_alErrors.add(exception);
        }

        /**
         * @see  org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
         */
        @Override public void fatalError(SAXParseException exception)
                                  throws SAXException
        {
            m_alFatals.add(exception);
        }

        /**
         * Returns true is XSD is valid.
         *
         * @return  true if valid
         */
        public boolean isValid()
        {
            return (m_alErrors.size() == 0) && (m_alFatals.size() == 0);
        }

        /**
         * This method returns the string representation of the object.
         *
         * @return  The string representation of the object.
         *
         * @see     java.lang.Object#toString()
         */
        @Override public String toString()
        {
            StringBuilder sbReturn = new StringBuilder(2048);

            if (m_alWarnings.size() > 0)
            {
                sbReturn.append("=============\n");
                sbReturn.append("Warnings:\n");
                sbReturn.append("=============\n");

                for (SAXParseException spe : m_alWarnings)
                {
                    sbReturn.append("Line (").append(spe.getLineNumber()).append(":").append(spe.getColumnNumber());
                    sbReturn.append("): ");
                    sbReturn.append(spe.getMessage()).append("\n");
                }
            }

            if (m_alErrors.size() > 0)
            {
                sbReturn.append("=============\n");
                sbReturn.append("Errors:\n");
                sbReturn.append("=============\n");

                for (SAXParseException spe : m_alErrors)
                {
                    sbReturn.append("Line (").append(spe.getLineNumber()).append(":").append(spe.getColumnNumber());
                    sbReturn.append("): ");
                    sbReturn.append(spe.getMessage()).append("\n");
                }
            }

            if (m_alFatals.size() > 0)
            {
                sbReturn.append("=============\n");
                sbReturn.append("Fatal errors:\n");
                sbReturn.append("=============\n");

                for (SAXParseException spe : m_alFatals)
                {
                    sbReturn.append("Line (").append(spe.getLineNumber()).append(":").append(spe.getColumnNumber());
                    sbReturn.append("): ");
                    sbReturn.append(spe.getMessage()).append("\n");
                }
            }

            return sbReturn.toString();
        }

        /**
         * @see  org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
         */
        @Override public void warning(SAXParseException exception)
                               throws SAXException
        {
            m_alWarnings.add(exception);
        }
    }
}
