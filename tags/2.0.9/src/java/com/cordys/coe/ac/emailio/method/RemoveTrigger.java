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
 package com.cordys.coe.ac.emailio.method;

import com.cordys.coe.ac.emailio.EmailIOConnectorConstants;
import com.cordys.coe.ac.emailio.config.IEmailBox;
import com.cordys.coe.ac.emailio.config.IEmailIOConfiguration;
import com.cordys.coe.ac.emailio.exception.InboundEmailException;
import com.cordys.coe.ac.emailio.exception.StorageProviderException;
import com.cordys.coe.ac.emailio.localization.InboundEmailExceptionMessages;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.soap.BodyBlock;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * This method can remove a trigger from the currently monitored email boxes. For now it's limited
 * to onetime only triggers.
 *
 * @author  pgussow
 */
public class RemoveTrigger extends BaseMethod
{
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(RemoveTrigger.class);

    /**
     * Constructor.
     *
     * @param  bbRequest   The request bodyblock.
     * @param  bbResponse  The response bodyblock.
     * @param  iecConfig   The application connector's configuration.
     */
    public RemoveTrigger(BodyBlock bbRequest, BodyBlock bbResponse, IEmailIOConfiguration iecConfig)
    {
        super(bbRequest, bbResponse, iecConfig);
    }

    /**
     * Executes the actual trigger removal.
     *
     * <p>The argument passed to the SOAP method is quite simple. It contains only the name of the
     * EMailbox to be altered and the name of the trigger that should be removed.</p>
     *
     * <p>Please be very careful when removing triggers that are contained in the default
     * configuration. As soon as the connector is restarted and the configuration is read, those
     * will be restored. It's generally a bad idea to remove triggers you didn't create yourself.
     * </p>
     *
     * <p>The XML passed to this method is rather simple and easy to build: <code>
     * &lt;UnregisterTrigger xmlns="http://emailioconnector.coe.cordys.com/2.0/inbound/dynamic"&gt;
     * &lt;emailbox&gt;SPE_customer_feedback&lt;/emailbox&gt;
     * &lt;triggername&gt;MyTriggerName&lt;/triggername&gt; &lt;/UnregisterTrigger&gt;</code></p>
     *
     * @throws  InboundEmailException  In case of any exceptions.
     *
     * @see     com.cordys.coe.ac.emailio.method.BaseMethod#execute()
     */
    @Override public void execute()
                           throws InboundEmailException
    {
        int iRequest = getRequest().getXMLNode();

        XPathMetaInfo xmi = new XPathMetaInfo();
        xmi.addNamespaceBinding("ns", Node.getNamespaceURI(iRequest));
        xmi.addNamespaceBinding("conf", EmailIOConnectorConstants.NS_CONFIGURATION);

        String sMailboxName = XPathHelper.getStringValue(iRequest, "./ns:emailbox/text()", xmi, "")
                                         .trim();

        if (sMailboxName.length() == 0)
        {
            throw new InboundEmailException(InboundEmailExceptionMessages.IEE_PARAMETER_NOT_FILLED_EMAILBOX);
        }

        String sTriggerName = XPathHelper.getStringValue(iRequest, "./ns:triggername/text()", xmi,
                                                         "").trim();

        if (sTriggerName.length() == 0)
        {
            throw new InboundEmailException(InboundEmailExceptionMessages.IEE_PARAMETER_TRIGGERNAME_NOT_FILLED);
        }

        // Find the email box to add it to.
        IEmailBox ebBox = getConfiguration().getEmailBox(sMailboxName);

        if (ebBox == null)
        {
            throw new InboundEmailException(InboundEmailExceptionMessages.IEE_THERE_IS_NO_EMAIL_BOX_WITH_NAME,
                                            sMailboxName);
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Removing trigger " + sTriggerName + " from emailbox " + ebBox.getName());
        }

        try
        {
            ebBox.removeTrigger(sTriggerName);
        }
        catch (StorageProviderException e)
        {
            throw new InboundEmailException(e,
                                            InboundEmailExceptionMessages.IEE_COULD_NOT_REMOVE_TRIGGER_DEFINITION);
        }
    }
}
