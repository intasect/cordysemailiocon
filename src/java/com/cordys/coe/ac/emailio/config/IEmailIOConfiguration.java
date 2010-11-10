package com.cordys.coe.ac.emailio.config;

import com.cordys.coe.ac.emailio.config.outbound.ISMTPServer;
import com.cordys.coe.ac.emailio.connection.ISMTPConnectionPool;
import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;
import com.cordys.coe.ac.emailio.exception.KeyManagerException;
import com.cordys.coe.ac.emailio.keymanager.ICertificateInfo;
import com.cordys.coe.ac.emailio.keymanager.IKeyManager;
import com.cordys.coe.ac.emailio.objects.IStorageProviderQueryManager;
import com.cordys.coe.ac.emailio.outbound.ISMIMEConfiguration;
import com.cordys.coe.ac.emailio.storage.IEmailStorageProvider;

import com.eibus.connector.nom.Connector;

import com.eibus.management.IManagedComponent;

import java.security.PrivateKey;

/**
 * This interface describes the global connector configuration data.
 *
 * @author  pgussow
 */
public interface IEmailIOConfiguration extends IXMLSerializable, ISMIMEConfiguration
{
    /**
     * This method returns the certificate info for the given email address.
     *
     * @param   sEmailAddress  The address to search for.
     *
     * @return  The information for the given email address. If not found, null is returned.
     *
     * @throws  KeyManagerException  In case of any key manager related exceptions.
     */
    ICertificateInfo getCertificateInfo(String sEmailAddress)
                                 throws KeyManagerException;

    /**
     * This method gets the connector to use for sending requests to the bus.
     *
     * @return  The connector to use for sending requests to the bus.
     */
    Connector getConnector();

    /**
     * This method returns the mailbox with the given name. If the mailbox is not found null is
     * returned.
     *
     * @param   sMailboxName  The name of the mail box.
     *
     * @return  The mailbox with the given name. If it's not found null is returned.
     */
    IEmailBox getEmailBox(String sMailboxName);

    /**
     * This method gets the email boxes that should be monitored.
     *
     * @return  The email boxes that should be monitored.
     */
    IEmailBox[] getEmailBoxes();

    /**
     * This method gets the key managers configured for this connector.
     *
     * @return  The key managers configured for this connector.
     */
    IKeyManager[] getKeyManagers();

    /**
     * This mehtod returns the managed component of the parent.
     *
     * @return  The managed component of the parent.
     */
    IManagedComponent getManagedComponent();

    /**
     * This method gets the max number of workers.
     *
     * @return  The max number of workers.
     */
    int getMaxWorkers();

    /**
     * This method returns the private key for the given email address. It will iterate over all
     * known key managers. If no private key can be found null is returned.
     *
     * @param   sEmailAddress  The address to find the private key for.
     *
     * @return  The private key to use. If no key is found null is returned.
     */
    PrivateKey getPrivateKey(String sEmailAddress);

    /**
     * This method gets the default connection pool (first one configured).
     *
     * @return  The default connection pool.
     */
    ISMTPConnectionPool getSMTPConnectionPool();

    /**
     * This method gets the connection pool with the given name.
     *
     * @param   sName  The name of the pool to return.
     *
     * @return  The connection pool with the given name.
     */
    ISMTPConnectionPool getSMTPConnectionPool(String sName);

    /**
     * This method gets the SMTP servers that are configured.
     *
     * @return  The SMTP servers that are configured.
     */
    ISMTPServer[] getSMTPServers();

    /**
     * This method gets the DN of the SOAP processor.
     *
     * @return  The DN of the SOAP processor.
     */
    String getSOAPProcessorDN();

    /**
     * This method gets the storage that should be used.
     *
     * @return  The storage that should be used.
     */
    IEmailStorageProvider getStorageProvider();

    /**
     * This method gets the global storage configuration. If this method returns 0 the default store
     * is supposed to be used.
     *
     * @return  The global storage configuration.
     */
    int getStorageProviderConfiguration();

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
     */
    IStorageProviderQueryManager getStorageProviderQueryManager();

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
     */
    void initialize(int iConfigNode, IManagedComponent mcParent, String sSOAPProcessorDN)
             throws EmailIOConfigurationException;

    /**
     * This method sets the connector to use for sending requests to the bus.
     *
     * @param  cConnector  The connector to use for sending requests to the bus.
     */
    void setConnector(Connector cConnector);
}
