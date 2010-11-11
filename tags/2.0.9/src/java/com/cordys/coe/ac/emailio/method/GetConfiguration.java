

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
package com.cordys.coe.ac.emailio.method;

import com.cordys.coe.ac.emailio.EmailIOConnectorConstants;
import com.cordys.coe.ac.emailio.config.IEmailBox;
import com.cordys.coe.ac.emailio.config.IEmailIOConfiguration;
import com.cordys.coe.ac.emailio.exception.EmailIOException;
import com.cordys.coe.ac.emailio.localization.EmailIOExceptionMessages;

import com.eibus.soap.BodyBlock;

import com.eibus.xml.nom.Node;

/**
 * This class handles the GetConfiguration method.
 *
 * @author  pgussow
 */
public class GetConfiguration extends BaseMethod
{
    /**
     * Constructor.
     *
     * @param  bbRequest   The request bodyblock.
     * @param  bbResponse  The response bodyblock.
     * @param  iecConfig   The application connector's configuration.
     */
    public GetConfiguration(BodyBlock bbRequest, BodyBlock bbResponse,
                            IEmailIOConfiguration iecConfig)
    {
        super(bbRequest, bbResponse, iecConfig);
    }

    /**
     * This method executed the requested SOAP method.
     *
     * @throws  EmailIOException  In case of any processing errors.
     *
     * @see     com.cordys.coe.ac.emailio.method.BaseMethod#execute()
     */
    @Override public void execute()
                           throws EmailIOException
    {
        int iResponse = getResponse().getXMLNode();
        int iResultStructure = 0;

        try
        {
            iResultStructure = Node.getDocument(iResponse).createElementNS("emailboxes", "", "",
                                                                           EmailIOConnectorConstants.NS_CONFIGURATION,
                                                                           0);

            IEmailBox[] aebBoxes = getConfiguration().getEmailBoxes();

            for (int iCount = 0; iCount < aebBoxes.length; iCount++)
            {
                IEmailBox ebBox = aebBoxes[iCount];
                ebBox.toXML(iResultStructure);
            }

            Node.appendToChildren(iResultStructure, iResponse);
        }
        catch (Exception e)
        {
            // In case of any exception the result structure has to be deleted again
            // because we will return a SOAP fault.
            if (iResultStructure != 0)
            {
                Node.delete(iResultStructure);
            }

            throw new EmailIOException(e,
                                       EmailIOExceptionMessages.EIOE_ERROR_RETURNING_THE_CURRENT_CONFIGURATION);
        }
    }
}
