

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
import com.cordys.coe.ac.emailio.config.trigger.ITrigger;
import com.cordys.coe.ac.emailio.config.trigger.TriggerFactory;
import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;
import com.cordys.coe.ac.emailio.exception.EmailIOException;
import com.cordys.coe.ac.emailio.exception.InboundEmailException;
import com.cordys.coe.ac.emailio.exception.StorageProviderException;
import com.cordys.coe.ac.emailio.localization.InboundEmailExceptionMessages;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.soap.BodyBlock;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * This class handles the RegisterTrigger SOAP method.
 *
 * <p>The trigger created by this class will have its name modified by a prepended GUID as it is
 * done by the {@link TriggerFactory#createUniqueTrigger(int,
 * com.cordys.coe.ac.emailio.config.action.IActionStore)} method. This ensures unique trigger names
 * without the need to create them in the porcess itself, but it also creates the necessity to store
 * the trigger name returned by the service method for later deletion of the trigger.</p>
 *
 * <p>Earlier versions of this method automatically created one-time triggers, this isn't the case
 * anymore. Since version 1.0.7, the triggers are created exactly as specified in the XML
 * configuration, i.e. the attribute "onetimeonly" of the "trigger" element must be set to <code>
 * true</code> in order to create a one-time trigger.</p>
 *
 * @author  pgussow, gottmann
 */
public class RegisterTrigger extends BaseMethod
{
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(RegisterTrigger.class);

    /**
     * Constructor.
     *
     * @param  bbRequest   The request bodyblock.
     * @param  bbResponse  The response bodyblock.
     * @param  iecConfig   The application connector's configuration.
     */
    public RegisterTrigger(BodyBlock bbRequest, BodyBlock bbResponse,
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
        int iRequest = getRequest().getXMLNode();

        XPathMetaInfo xmi = new XPathMetaInfo();
        xmi.addNamespaceBinding("ns", Node.getNamespaceURI(iRequest));
        xmi.addNamespaceBinding("conf", EmailIOConnectorConstants.NS_CONFIGURATION);

        String sMailboxName = XPathHelper.getStringValue(iRequest, "./ns:emailbox/text()", xmi, "");

        if (sMailboxName.length() == 0)
        {
            throw new InboundEmailException(InboundEmailExceptionMessages.IEE_PARAMETER_NOT_FILLED_EMAILBOX);
        }

        int iTrigger = XPathHelper.selectSingleNode(iRequest, "./conf:trigger", xmi);

        if (iTrigger == 0)
        {
            throw new InboundEmailException(InboundEmailExceptionMessages.IEE_MISSING_PARAMETER_TRIGGER);
        }

        // Find the email box to add it to.
        IEmailBox ebBox = getConfiguration().getEmailBox(sMailboxName);

        if (ebBox == null)
        {
            throw new InboundEmailException(InboundEmailExceptionMessages.IEE_THERE_IS_NO_EMAIL_BOX_WITH_NAME,
                                            sMailboxName);
        }

        // Create the trigger
        try
        {
            ITrigger tTrigger = TriggerFactory.createUniqueTrigger(iTrigger, ebBox);

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Adding trigger to mailbox " + ebBox.getName() + "\n" + tTrigger);
            }
            ebBox.addTrigger(tTrigger);

            if (LOG.isDebugEnabled())
            {
                LOG.debug("The name for the trigger is " + tTrigger.getName());
            }

            // Build up the response with the new trigger name
            int iResponse = getResponse().getXMLNode();
            Node.createElementWithParentNS("triggername", tTrigger.getName(), iResponse);
        }
        catch (EmailIOConfigurationException e)
        {
            throw new InboundEmailException(e,
                                            InboundEmailExceptionMessages.IEE_COULD_NOT_CREATE_TRIGGER_DEFINITION);
        }
        catch (StorageProviderException e)
        {
            throw new InboundEmailException(e,
                                            InboundEmailExceptionMessages.IEE_COULD_NOT_CREATE_TRIGGER_DEFINITION);
        }
    }
}
