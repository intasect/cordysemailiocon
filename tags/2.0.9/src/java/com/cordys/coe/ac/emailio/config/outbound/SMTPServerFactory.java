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

import com.cordys.coe.ac.emailio.EmailIOConnectorConstants;
import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;

import com.eibus.management.IManagedComponent;

import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * This class creates the SMTP Server object based on the given configuration XML.
 *
 * @author  pgussow
 */
public class SMTPServerFactory
{
    /**
     * This method gets the {@link ISMTPServer} object based on the given configuration.
     *
     * @param   iConfiguration      The configuration XML.
     * @param   mcManagedComponent  The parent managed component.
     *
     * @return  The {@link ISMTPServer} object based on the given configuration.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    public static ISMTPServer createSMTPServer(int iConfiguration,
                                               IManagedComponent mcManagedComponent)
                                        throws EmailIOConfigurationException
    {
        ISMTPServer ssReturn = null;

        XPathMetaInfo xmi = new XPathMetaInfo();
        xmi.addNamespaceBinding("ns", EmailIOConnectorConstants.NS_CONFIGURATION);

        // Create the actual wrapper and set the managed component.
        ssReturn = new SMTPServer(iConfiguration);
        ssReturn.setManagedComponent(mcManagedComponent);

        return ssReturn;
    }
}
