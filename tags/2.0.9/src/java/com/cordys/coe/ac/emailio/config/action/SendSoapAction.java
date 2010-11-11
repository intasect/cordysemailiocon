

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
package com.cordys.coe.ac.emailio.config.action;

import com.cordys.coe.ac.emailio.EmailIOConnectorConstants;
import com.cordys.coe.ac.emailio.config.message.IMessage;
import com.cordys.coe.ac.emailio.config.message.MessageFactory;
import com.cordys.coe.ac.emailio.config.rule.IRuleContext;
import com.cordys.coe.ac.emailio.exception.ActionException;
import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;
import com.cordys.coe.ac.emailio.localization.ActionExceptionMessages;
import com.cordys.coe.ac.emailio.monitor.MessageExecutionHelper;
import com.cordys.coe.util.soap.SOAPWrapper;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.xpath.XPathMetaInfo;

import javax.mail.Message;

/**
 * This class handles the sendsoap action. It will send the configured SOAP call.
 *
 * @author  pgussow
 */
class SendSoapAction extends BaseAction
{
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(SendSoapAction.class);
    /**
     * Holds the action message to send.
     */
    private IMessage m_mMessage;

    /**
     * Creates a new SendSoapAction object.
     *
     * @param   iNode
     *
     * @throws  EmailIOConfigurationException
     */
    public SendSoapAction(int iNode)
                   throws EmailIOConfigurationException
    {
        super(iNode, EAction.SENDSOAP);

        XPathMetaInfo xmi = new XPathMetaInfo();
        xmi.addNamespaceBinding("ns", EmailIOConnectorConstants.NS_CONFIGURATION);

        int iMessage = XPathHelper.selectSingleNode(iNode, "./ns:sendsoap", xmi);

        m_mMessage = MessageFactory.createMessage(iMessage);
    }

    /**
     * This method is called to actually execute the action. In this case a SOAP message will be
     * sent.
     *
     * @param   pcContext  The pattern context.
     * @param   mMessage   The actual email message for which the action should be executed.
     * @param   swSoap     The soap wrapper that can be used.
     *
     * @throws  ActionException  In case of any exceptions.
     *
     * @see     com.cordys.coe.ac.emailio.config.action.IAction#execute(IRuleContext, Message,
     *          SOAPWrapper)
     */
    @Override public void execute(IRuleContext pcContext, Message mMessage, SOAPWrapper swSoap)
                           throws ActionException
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Executing action " + getID() + "\nDetails: " + toString());
        }

        try
        {
            // Fix the SOAP wrapper for the proper organizations
            if ((m_mMessage.getOrganization() != null) &&
                    (m_mMessage.getOrganization().length() > 0))
            {
                swSoap.setOrganization(m_mMessage.getOrganization());
            }

            if ((m_mMessage.getUserDN() != null) && (m_mMessage.getUserDN().length() > 0))
            {
                swSoap.setUser(m_mMessage.getUserDN());
            }
            swSoap.setTimeOut(m_mMessage.getTimeout());

            int iMethod = MessageExecutionHelper.createMethod(swSoap, m_mMessage);

            MessageExecutionHelper.fillParameters(iMethod, m_mMessage, pcContext);

            if (m_mMessage.getSynchronous())
            {
                swSoap.sendAndWait(iMethod);
            }
            else
            {
                swSoap.sendAndForget(iMethod);
            }
        }
        catch (Exception e)
        {
            throw new ActionException(e, ActionExceptionMessages.AE_ERROR_SENDING_THE_SOAP_REQUEST);
        }
        finally
        {
            swSoap.freeXMLNodes();
        }
    }

    /**
     * This method returns a string representation of the object.
     *
     * @return  A string representation of the object.
     *
     * @see     com.cordys.coe.ac.emailio.config.action.BaseAction#toString()
     */
    @Override public String toString()
    {
        StringBuilder sbReturn = new StringBuilder();

        sbReturn.append(super.toString());
        sbReturn.append("\nMessage: " + m_mMessage.toString());

        return sbReturn.toString();
    }

    /**
     * This method dumps the configuration of this action to XML.
     *
     * @param   iParent  The parent element.
     *
     * @return  The created XML structure.
     *
     * @see     com.cordys.coe.ac.emailio.config.action.BaseAction#toXML(int)
     */
    @Override public int toXML(int iParent)
    {
        int iReturn = super.toXML(iParent);

        m_mMessage.toXML(iParent);

        return iReturn;
    }
}
