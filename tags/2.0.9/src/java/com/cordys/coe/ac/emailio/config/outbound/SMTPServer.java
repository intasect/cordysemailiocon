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
 package com.cordys.coe.ac.emailio.config.outbound;

import com.cordys.coe.ac.emailio.config.MailServer;
import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.xml.nom.Node;

/**
 * This class wraps the SMTP server configuration details.
 *
 * @author  pgussow
 */
class SMTPServer extends MailServer
    implements ISMTPServer
{
    /**
     * Holds whether or not the connection pool should be used.
     */
    private boolean m_bUseConnectionPool;
    /**
     * Holds the number of connections that should be used for the connection pool.
     */
    private int m_iMaxNrOfConnections;
    /**
     * Holds the timeout for the connections.
     */
    private long m_lConnectionTimeout;

    /**
     * Creates the SMTP server object based on the given configuration XML.
     *
     * @param   iConfiguration  The actual configuration XML.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions
     */
    public SMTPServer(int iConfiguration)
               throws EmailIOConfigurationException
    {
        super(iConfiguration);

        m_iMaxNrOfConnections = XPathHelper.getIntegerValue(iConfiguration,
                                                            "./ns:" + TAG_MAX_NR_OF_CONNECTIONS +
                                                            "/text()", getXMI(),
                                                            DEFAULT_MAX_NR_OF_CONNECTIONS);
        m_lConnectionTimeout = XPathHelper.getLongValue(iConfiguration,
                                                        "./ns:" + TAG_CONNECTION_TIMEOUT +
                                                        "/text()", getXMI(),
                                                        DEFAULT_CONNECTION_TIMEOUT);
        m_bUseConnectionPool = XPathHelper.getBooleanValue(iConfiguration,
                                                           "./ns:" + TAG_USE_CONNECTION_POOL +
                                                           "/text()", getXMI(), false);
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.ISMTPServer#getConnectionTimeout()
     */
    @Override public long getConnectionTimeout()
    {
        return m_lConnectionTimeout;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.ISMTPServer#getMaxNrOfConnections()
     */
    @Override public int getMaxNrOfConnections()
    {
        return m_iMaxNrOfConnections;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.IMailServer#toXML(int)
     */
    @Override public int toXML(int iParent)
    {
        int iReturn = super.toXML(iParent, "smtpserver");

        Node.createElementWithParentNS(TAG_USE_CONNECTION_POOL, String.valueOf(useConnectionPool()),
                                       iReturn);

        if (useConnectionPool())
        {
            Node.createElementWithParentNS(TAG_MAX_NR_OF_CONNECTIONS,
                                           String.valueOf(getMaxNrOfConnections()), iReturn);
            Node.createElementWithParentNS(TAG_CONNECTION_TIMEOUT,
                                           String.valueOf(getConnectionTimeout()), iReturn);
        }

        return iReturn;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.ISMTPServer#useConnectionPool()
     */
    @Override public boolean useConnectionPool()
    {
        return m_bUseConnectionPool;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.IMailServer#validate()
     */
    @Override public void validate()
                            throws EmailIOConfigurationException
    {
        // TODO: Check if the server is reachable.
    }
}
