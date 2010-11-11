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
 package com.cordys.coe.ac.emailio.connection;

import com.cordys.coe.ac.emailio.config.IEmailIOConfiguration;
import com.cordys.coe.ac.emailio.config.outbound.ISMTPServer;

/**
 * This class creates the connection pool for the given configuration.
 *
 * @author  pgussow
 */
public class SMTPConnectionPoolFactory
{
    /**
     * This method creates the SMTP server connection pool based on the given configuration.
     *
     * @param   ssServer   The configuration for the connection pool.
     * @param   eicConfig  The configuration of the connector.
     *
     * @return  The created connection pool.
     */
    public static ISMTPConnectionPool createConnectionPool(ISMTPServer ssServer,
                                                           IEmailIOConfiguration eicConfig)
    {
        ISMTPConnectionPool scpReturn = null;

        scpReturn = new SMTPConnectionPool(ssServer, eicConfig.getManagedComponent());

        return scpReturn;
    }
}
