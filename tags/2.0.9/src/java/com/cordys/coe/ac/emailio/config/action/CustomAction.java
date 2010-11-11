

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
import com.cordys.coe.ac.emailio.config.rule.IRuleContext;
import com.cordys.coe.ac.emailio.exception.ActionException;
import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;
import com.cordys.coe.ac.emailio.localization.EmailIOConfigurationExceptionMessages;
import com.cordys.coe.util.soap.SOAPWrapper;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.xpath.XPathMetaInfo;

import javax.mail.Message;

/**
 * This class handles the custom actions.
 *
 * @author  pgussow
 */
class CustomAction extends BaseAction
{
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(CustomAction.class);
    /**
     * Holds the custom action.
     */
    private ICustomAction m_caCustomAction;

    /**
     * Creates a new CustomAction object.
     *
     * @param   iNode  The configuration node.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    public CustomAction(int iNode)
                 throws EmailIOConfigurationException
    {
        super(iNode, EAction.CUSTOM);

        XPathMetaInfo xmi = new XPathMetaInfo();
        xmi.addNamespaceBinding("ns", EmailIOConnectorConstants.NS_CONFIGURATION);

        String sClass = XPathHelper.getStringValue(iNode, "./ns:class/text()", xmi, EMPTY_STRING);

        if (sClass.length() == 0)
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_CLASSNAME_MUST_BE_FILLED);
        }

        Class<?> cRealAction = null;

        try
        {
            cRealAction = Class.forName(sClass);
        }
        catch (ClassNotFoundException e)
        {
            throw new EmailIOConfigurationException(e,
                                                    EmailIOConfigurationExceptionMessages.EICE_COULD_NOT_FIND_CUSTOM_ACTION_CLASS_0,
                                                    sClass);
        }

        if (!ICustomAction.class.isAssignableFrom(cRealAction))
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_CLASS_0_DOES_NOT_IMPLEMENT_THE_ICUSTOMACTION_INTERFACE,
                                                    sClass);
        }

        // Now we can instantiate the action.
        try
        {
            m_caCustomAction = (ICustomAction) cRealAction.newInstance();
        }
        catch (Exception e)
        {
            throw new EmailIOConfigurationException(e,
                                                    EmailIOConfigurationExceptionMessages.EICE_ERROR_INSTANTIATING_THE_CUSTOM_ACTION_CLASS,
                                                    sClass);
        }

        // Configure the action
        int iConfiguration = XPathHelper.selectSingleNode(iNode, "./ns:configuration", xmi);
        m_caCustomAction.configure(iConfiguration);
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.action.IAction#execute(IRuleContext, Message,
     *       SOAPWrapper)
     */
    @Override public void execute(IRuleContext pcContext, Message mMessage, SOAPWrapper swSoap)
                           throws ActionException
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Executing action " + getID() + "\nDetails: " + toString());
        }

        m_caCustomAction.execute(pcContext, mMessage);
    }
}
