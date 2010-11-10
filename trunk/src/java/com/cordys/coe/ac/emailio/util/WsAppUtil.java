package com.cordys.coe.ac.emailio.util;

import com.cordys.coe.ac.emailio.exception.WsAppUtilException;
import com.cordys.coe.ac.emailio.localization.LogMessages;
import com.cordys.coe.ac.emailio.localization.WsAppUtilExceptionMessages;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.cordys.cpc.bsf.busobject.BusObjectManager;
import com.cordys.cpc.bsf.busobject.Config;

import com.eibus.directory.soap.LDAPDirectory;

import com.eibus.management.IManagedComponent;

import com.eibus.security.ac.ACLReader;
import com.eibus.security.ac.ACLReaderForLDAP;
import com.eibus.security.ac.AccessControlObject;
import com.eibus.security.ac.Registration;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class contains utility methods to be able to work with multiple configurations to multiple
 * databases.
 *
 * @author  pgussow
 */
public class WsAppUtil
{
    /**
     * Holds the list of active configurations.
     */
    private static Map<String, Config> s_mConfigs = Collections.synchronizedMap(new LinkedHashMap<String, Config>());
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(WsAppUtil.class);
    /**
     * Holds the template XML for creating the WsApps server embedded configuration.
     */
    private static final byte[] WSAPPS_CONFIG = ("<configuration implementation=\"com.cordys.cpc.bsf.connector.BsfConnector\" htmfile=\"/cordys/cpc/bsf/developer/content/bsftabpage.htm\">" +
                                                 "<ruleEnabled>false</ruleEnabled>" +
                                                 "<auditEnabled>off</auditEnabled><initializeDB>true</initializeDB><initializeEIS>false</initializeEIS><bsfuser/><customInitializer/></configuration>")
                                                .getBytes();
    /**
     * Holds the document that will be used.
     */
    private static Document s_dDoc = new Document();
    /**
     * Holds the ACL registration object.
     */
    private static Registration s_rACLRegistration;

    static
    {
        try
        {
            LDAPDirectory ldDirectory = new LDAPDirectory();
            ACLReader aclr = new ACLReaderForLDAP(ldDirectory);
            s_rACLRegistration = new Registration(aclr, "any", s_dDoc);
        }
        catch (Exception e)
        {
            LOG.fatal(e, LogMessages.ERROR_INITIALIZING_THE_WSAPPSERVER_UTIL_CLASS);
        }
    }

    /**
     * This method creates a new BusObjectManager for the given config.
     *
     * @param   sName  The name of the config.
     *
     * @return  The BusObjectManager to use.
     *
     * @throws  WsAppUtilException  In case of any exceptions.
     */
    public static BusObjectManager createBusObjectManager(String sName)
                                                   throws WsAppUtilException
    {
        BusObjectManager bomReturn = null;

        Config cConfig = getConfiguration(sName);

        // Get the AccessControlObject
        AccessControlObject aco = null;

        try
        {
            aco = s_rACLRegistration.getAccessControlObject(cConfig.getDefaultUser());
        }
        catch (Exception e)
        {
            throw new WsAppUtilException(e,
                                         WsAppUtilExceptionMessages.WAU_ERROR_GETTING_ACCESSCONTROLOBJECT_FOR_USER,
                                         cConfig.getDefaultUser());
        }

        // I must pass on an AccessControlObject here, otherwise it still needs a BsfContext.
        bomReturn = new BusObjectManager(cConfig, s_dDoc, aco);

        return bomReturn;
    }

    /**
     * This method creates a configuration object that can be used to create a BusObjectManager.
     *
     * <p>Note: The first time you call this method that configuration will be the default one.</p>
     *
     * @param   iDatabaseConfig  The configuration for the database.
     * @param   sOrganization    The organizational context.
     * @param   mcParent         The parent managed component.
     *
     * @return  The configuration that can be used.
     *
     * @throws  WsAppUtilException  In case of any exceptions.
     */
    public static Config createConfiguration(int iDatabaseConfig, String sOrganization,
                                             IManagedComponent mcParent)
                                      throws WsAppUtilException
    {
        Config cReturn = null;

        // Get the name of the connection pool
        String sName = Node.getAttribute(iDatabaseConfig, "name");

        if (!StringUtil.isSet(sName))
        {
            throw new WsAppUtilException(WsAppUtilExceptionMessages.WAU_THE_NAME_OF_THE_COMPONENT_MUST_BE_SET);
        }

        try
        {
            if (!s_mConfigs.containsKey(sName))
            {
                Document dDoc = Node.getDocument(iDatabaseConfig);
                int iWsAppsConfig = dDoc.load(WSAPPS_CONFIG);

                // Fix the BSF user
                int iBsfUser = XPathHelper.selectSingleNode(iWsAppsConfig, "bsfuser");
                dDoc.createText("cn=SYSTEM,cn=organizational users," + sOrganization, iBsfUser);

                // Add the component.
                Node.duplicateAndAppendToChildren(iDatabaseConfig, iDatabaseConfig, iWsAppsConfig);

                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Using this configuration:\n" +
                              Node.writeToString(iWsAppsConfig, false));
                }

                // If there is no default configuration available yet, we will create it as the
                // default.
                cReturn = new Config(mcParent, sName, iWsAppsConfig, false);
                cReturn.setConfig(iWsAppsConfig);
                s_mConfigs.put(sName, cReturn);
            }
            else
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Configuration with name " + sName + " already exists. Reusing it.");
                }
                cReturn = s_mConfigs.get(sName);
            }
        }
        catch (Exception e)
        {
            throw new WsAppUtilException(e,
                                         WsAppUtilExceptionMessages.WAU_ERROR_CREATING_CONFIGURATION_WITH_NAME,
                                         sName);
        }

        return cReturn;
    }

    /**
     * This method returns the configuration with the given name. If the configration does not exist
     * an exception is thrown.
     *
     * @param   sName  The name of the configuration.
     *
     * @return  The configuration,
     *
     * @throws  WsAppUtilException  In case the configuration is not found.
     */
    public static Config getConfiguration(String sName)
                                   throws WsAppUtilException
    {
        Config cReturn = s_mConfigs.get(sName);

        if (cReturn == null)
        {
            throw new WsAppUtilException(WsAppUtilExceptionMessages.WAU_THE_CONFIGURATION_WITH_NAME_0_COULD_NOT_BE_FOUND,
                                         sName);
        }

        return cReturn;
    }
}
