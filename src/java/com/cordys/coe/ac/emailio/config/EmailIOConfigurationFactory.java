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

import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;

import com.eibus.management.IManagedComponent;

/**
 * This factory creates the configuration object to use.
 *
 * @author  pgussow
 */
public class EmailIOConfigurationFactory
{
    /**
     * Creates the constructor.This loads the configuration object and pass it to XMLProperties for
     * processing.
     *
     * @param   iConfigNode       The xml-node that contains the configuration.
     * @param   mcParent          The parent managed component for JMX.
     * @param   sSOAPProcessorDN  The DN of the SOAP processor in which the storage provider is
     *                            running.
     *
     * @return  The configuration object to use.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    public static IEmailIOConfiguration createConfiguration(int iConfigNode,
                                                            IManagedComponent mcParent,
                                                            String sSOAPProcessorDN)
                                                     throws EmailIOConfigurationException
    {
        IEmailIOConfiguration iecReturn = new EmailIOConfiguration();

        iecReturn.initialize(iConfigNode, mcParent, sSOAPProcessorDN);

        return iecReturn;
    }
}
