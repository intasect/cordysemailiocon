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
 package com.cordys.coe.ac.emailio.config;

import com.cordys.coe.ac.emailio.EmailIOConnectorConstants;
import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;
import com.cordys.coe.ac.emailio.localization.EmailIOConfigurationExceptionMessages;
import com.cordys.coe.ac.emailio.util.StringUtil;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.management.IManagedComponent;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.system.Native;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * This base class implements the details for the generic mail server tags. All mail servers (POP3,
 * IMAP and SMTP extend from this class.
 *
 * @author  pgussow
 */
public abstract class MailServer
    implements IMailServer
{
    /**
     * Holds the logger to use.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(MailServer.class);
    /**
     * Holds whether or not SSL is enabled for.
     */
    private boolean m_bSSL;
    /**
     * Holds the port number to connect to.
     */
    private int m_iPort;
    /**
     * Holds the managed component which is associated with this mailbox.
     */
    private IManagedComponent m_mcManagedComponent = null;
    /**
     * Holds the host name.
     */
    private String m_sHost;
    /**
     * Holds the name of the box.
     */
    private String m_sName;
    /**
     * Holds the password for the account.
     */
    private String m_sPassword;
    /**
     * Holds the user name for the email box.
     */
    private String m_sUsername;
    /**
     * Holds the XPathMetaInfo object.
     */
    private XPathMetaInfo m_xmi;

    /**
     * Creates the new MailServer object based on the given configuration XML.
     *
     * @param   iConfiguration  The actual configuration XML.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions
     */
    public MailServer(int iConfiguration)
               throws EmailIOConfigurationException
    {
        this(iConfiguration, null);
    }

    /**
     * Creates the new MailServer object based on the given configuration XML.
     *
     * @param   iConfiguration      The actual configuration XML.
     * @param   mcManagedComponent  The parent managed component.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions
     */
    public MailServer(int iConfiguration, IManagedComponent mcManagedComponent)
               throws EmailIOConfigurationException
    {
        if (iConfiguration == 0)
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_MISSING_SMTP_SERVER_CONFIGURATION);
        }

        m_xmi = new XPathMetaInfo();
        m_xmi.addNamespaceBinding("ns", EmailIOConnectorConstants.NS_CONFIGURATION);

        m_mcManagedComponent = mcManagedComponent;

        m_sName = XPathHelper.getStringValue(iConfiguration, "./ns:" + TAG_NAME + "/text()", m_xmi,
                                             EMPTY_STRING);
        m_sHost = XPathHelper.getStringValue(iConfiguration, "./ns:" + TAG_HOST + "/text()", m_xmi,
                                             EMPTY_STRING);
        m_iPort = XPathHelper.getIntegerValue(iConfiguration, "./ns:" + TAG_PORT + "/text()", m_xmi,
                                              0);
        m_sUsername = XPathHelper.getStringValue(iConfiguration, "./ns:" + TAG_USERNAME + "/text()",
                                                 m_xmi, EMPTY_STRING);
        m_sPassword = XPathHelper.getStringValue(iConfiguration, "./ns:" + TAG_PASSWORD + "/text()",
                                                 m_xmi, EMPTY_STRING);
        m_bSSL = XPathHelper.getBooleanValue(iConfiguration, "./ns:" + TAG_SSL + "/text()", m_xmi,
                                             false);

        // Check the configuration.
        if (m_sName.length() == 0)
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_MISSING_EMAIL_BOX_NAME);
        }

        if (m_sHost.length() == 0)
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_MISSING_HOST_NAME);
        }

        if (StringUtil.isSet(m_sPassword))
        {
            // Decode the password. When restarting the password is not encoded and not available.
            try
            {
                byte[] baPassword = m_sPassword.getBytes();
                m_sPassword = new String(Native.decodeBinBase64(baPassword, baPassword.length));
            }
            catch (Exception e)
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Error decoding password " + m_sPassword, e);
                }
            }
        }
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.IMailServer#getHost()
     */
    @Override public String getHost()
    {
        return m_sHost;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.IMailServer#getManagedComponent()
     */
    @Override public IManagedComponent getManagedComponent()
    {
        return m_mcManagedComponent;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.IMailServer#getName()
     */
    @Override public String getName()
    {
        return m_sName;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.IMailServer#getPassword()
     */
    @Override public String getPassword()
    {
        return m_sPassword;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.IMailServer#getPort()
     */
    @Override public int getPort()
    {
        return m_iPort;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.IMailServer#getUsername()
     */
    @Override public String getUsername()
    {
        return m_sUsername;
    }

    /**
     * This method gets the XPathMataInfo object.
     *
     * @return  The XPathMataInfo object.
     */
    public XPathMetaInfo getXMI()
    {
        return m_xmi;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.IMailServer#isSSLEnabled()
     */
    @Override public boolean isSSLEnabled()
    {
        return m_bSSL;
    }

    /**
     * This method sets the host name where the email box resides.
     *
     * @param  sHost  The host name where the email box resides.
     */
    public void setHost(String sHost)
    {
        m_sHost = sHost;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.IMailServer#setManagedComponent(com.eibus.management.IManagedComponent)
     */
    @Override public void setManagedComponent(IManagedComponent mcManagedComponent)
    {
        m_mcManagedComponent = mcManagedComponent;
    }

    /**
     * This method sets the name of the email box. This is for display only.
     *
     * @param  sName  The name of the email box. This is for display only.
     */
    public void setName(String sName)
    {
        m_sName = sName;
    }

    /**
     * This method sets the password for the email box.
     *
     * @param  sPassword  The password for the email box.
     */
    public void setPassword(String sPassword)
    {
        m_sPassword = sPassword;
    }

    /**
     * This method sets the port number.
     *
     * @param  iPort  The port number.
     */
    public void setPort(int iPort)
    {
        m_iPort = iPort;
    }

    /**
     * This method sets the username for the email box.
     *
     * @param  sUsername  The username for the email box.
     */
    public void setUsername(String sUsername)
    {
        m_sUsername = sUsername;
    }

    /**
     * This method writes the generic fields to XML.
     *
     * @param   iParent   The parent XML node.
     * @param   sRootTag  The root tag for this mail server.
     *
     * @return  The created XML element.
     */
    protected int toXML(int iParent, String sRootTag)
    {
        int iReturn = 0;

        iReturn = Node.createElementNS(sRootTag, EMPTY_STRING, EMPTY_STRING,
                                       EmailIOConnectorConstants.NS_CONFIGURATION, iParent);

        Node.createElementWithParentNS(TAG_NAME, getName(), iReturn);
        Node.createElementWithParentNS(TAG_HOST, getHost(), iReturn);
        Node.createElementWithParentNS(TAG_PORT, String.valueOf(getPort()), iReturn);
        Node.createElementWithParentNS(TAG_USERNAME, getUsername(), iReturn);
        Node.createElementWithParentNS(TAG_PASSWORD, "******", iReturn);
        Node.createElementWithParentNS(TAG_SSL, String.valueOf(isSSLEnabled()), iReturn);

        return iReturn;
    }
}
