

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
package com.cordys.coe.ac.emailio.config.outbound;

import com.cordys.coe.ac.emailio.outbound.ISMIMEConfiguration;

import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * This method creates the send options object based on the configuration.
 *
 * @author  pgussow
 */
public class SendOptionsFactory
{
    /**
     * This method creates a new ISendOptions object based on the given configuration XML.
     *
     * @param   iContent  The actual content.
     * @param   xmi       The XPathMetaInfo with the prefix 'ns' mapped to the proper namespace.
     * @param   scConfig  The configuration for S/MIME.
     *
     * @return  The created mail data.
     */
    public static ISendOptions parseSendOptions(int iContent, XPathMetaInfo xmi,
                                                ISMIMEConfiguration scConfig)
    {
        ISendOptions soReturn = null;

        soReturn = new SendOptions(iContent, xmi, scConfig);

        return soReturn;
    }
}
