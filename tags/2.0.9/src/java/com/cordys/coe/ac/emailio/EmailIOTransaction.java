


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
package com.cordys.coe.ac.emailio;

import com.cordys.coe.ac.emailio.config.IEmailIOConfiguration;
import com.cordys.coe.ac.emailio.exception.EmailIOException;
import com.cordys.coe.ac.emailio.localization.EmailIOExceptionMessages;
import com.cordys.coe.ac.emailio.localization.InboundEmailExceptionMessages;
import com.cordys.coe.ac.emailio.localization.LogMessages;
import com.cordys.coe.ac.emailio.method.BaseMethod;
import com.cordys.coe.ac.emailio.method.SendMail;
import com.cordys.coe.ac.emailio.method.WsAppMethod;
import com.cordys.coe.ac.emailio.objects.IStorageProviderQueryManager;
import com.cordys.coe.exception.ServerLocalizableException;
import com.cordys.coe.util.general.ExceptionUtil;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.soap.ApplicationTransaction;
import com.eibus.soap.BodyBlock;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

import java.lang.reflect.Constructor;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is the Implementation of ApplicationTransaction. This class will receive the request
 * process it if it is a valid one.
 */
public class EmailIOTransaction
    implements ApplicationTransaction
{
    /**
     * Contains the logger instance.
     */
    private static CordysLogger LOG = CordysLogger.getCordysLogger(EmailIOTransaction.class);
    /**
     * The request type by which the request is to be redirected to different classes.
     */
    private static final String SERVICE_TYPE = "EMAILIO";
    /**
     * The request type which identifies queries on the storage provider.
     */
    private static final String SERVICE_TYPE_OBJECTS = "EMAILIO_OBJECTS";
    /**
     * The request type for compatibility with the the default Cordys SendMail method.
     */
    private static final String SERVICE_TYPE_COMPATIBILITY = "Email";
    /**
     * The map of request types.
     */
    private static Map<String, String> hmSeviceTypes = new HashMap<String, String>();

    static
    {
        hmSeviceTypes.put(SERVICE_TYPE, SERVICE_TYPE);
        hmSeviceTypes.put(SERVICE_TYPE_OBJECTS, SERVICE_TYPE_OBJECTS);
        hmSeviceTypes.put(SERVICE_TYPE_COMPATIBILITY, SERVICE_TYPE_COMPATIBILITY);
    }

    /**
     * Holds the configuration of the application connector.
     */
    private IEmailIOConfiguration m_acConfig;
    /**
     * Indicates that the current request is in compatibility mode (supporting default Cordys
     * methods).
     */
    private boolean m_bCompatibility = false;
    /**
     * Indicates whether or not the current request is database oriented.
     */
    private boolean m_bStorageRequest = false;

    /**
     * Creates the transactional object.
     *
     * @param  acConfig  The configuration of the application connector.
     */
    public EmailIOTransaction(IEmailIOConfiguration acConfig)
    {
        m_acConfig = acConfig;

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Transaction created.");
        }
    }

    /**
     * This will be called when a transaction is being aborted.
     */
    public void abort()
    {
        if (LOG.isInfoEnabled())
        {
            LOG.info(LogMessages.TRANSACTION_ABORT);
        }
    }

    /**
     * This method returns returns if this transaction can process requests of the given type.
     *
     * @param   sType  The type of message that needs to be processed
     *
     * @return  true if the type can be processed. Otherwise false.
     */
    public boolean canProcess(String sType)
    {
        boolean bReturn = false;

        if (hmSeviceTypes.containsKey(sType))
        {
            bReturn = true;

            if (SERVICE_TYPE_OBJECTS.equals(sType))
            {
                m_bStorageRequest = true;
            }
            else if (SERVICE_TYPE_COMPATIBILITY.equals(sType))
            {
                m_bCompatibility = true;
            }
        }
        return bReturn;
    }

    /**
     * This method is called when the transaction is committed.
     */
    public void commit()
    {
        if (LOG.isInfoEnabled())
        {
            LOG.info(LogMessages.TRANSACTION_COMMIT);
        }
    }

    /**
     * This method processes the received request.
     *
     * @param   bbRequest   The request-bodyblock.
     * @param   bbResponse  The response-bodyblock.
     *
     * @return  true if the connector has to send the response. If someone else sends the response
     *          false is returned.
     */
    public boolean process(BodyBlock bbRequest, BodyBlock bbResponse)
    {
        boolean bReturn = true;

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Incoming SOAP request:\n" +
                      Node.writeToString(bbRequest.getXMLNode(), false));
        }

        try
        {
            if (m_bStorageRequest)
            {
                // It's a WsApps based request. First check if we have such an interface.
                IStorageProviderQueryManager spqStorage = m_acConfig
                                                          .getStorageProviderQueryManager();

                if (spqStorage == null)
                {
                    throw new EmailIOException(EmailIOExceptionMessages.EIOE_THERE_IS_NO_GLOBAL_STORAGE_PROVIDER_THAT_SUPPORTS_THE_MANAGEMENT_INTERFACE);
                }

                String sAction = getActionFromImplementation(bbRequest);
                EObjectAction oaAction = EObjectAction.valueOf(sAction.toUpperCase());

                WsAppMethod wm = new WsAppMethod(bbRequest, bbResponse, oaAction, spqStorage);
                wm.execute();
            }
            else if (m_bCompatibility)
            {
                String sMethodName = Node.getLocalName(bbRequest.getXMLNode());

                if ("SendMail".equals(sMethodName))
                {
                    SendMail sm = new SendMail(bbRequest, bbResponse, m_acConfig, true);
                    sm.execute();
                }
                else
                {
                    throw new EmailIOException(EmailIOExceptionMessages.EIOE_METHOD_0_NOT_SUPPORTED_ONLY_THE_SENDMAIL_METHOD_IS_SUPPORTED,
                                               sMethodName);
                }
            }
            else
            {
                String sAction = getActionFromImplementation(bbRequest);
                EDynamicAction daAction = null;

                try
                {
                    daAction = EDynamicAction.valueOf(sAction.toUpperCase());
                }
                catch (Exception e)
                {
                    // Most likely the enum could not be parsed. So the action is invalid.
                    // Just to be sure we'll log the exception when debug is enabled. Otherwise
                    // we just return the invalid action message
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Error determining the action type", e);
                    }
                    throw new EmailIOException(EmailIOExceptionMessages.EIOE_INVALID_ACTION_TYPE_0,
                                               sAction);
                }

                // Instantiate the proper class.
                Class<? extends BaseMethod> cClass = daAction.getImplementationClass();
                Constructor<? extends BaseMethod> cConstructor = cClass.getConstructor(BodyBlock.class,
                                                                                       BodyBlock.class,
                                                                                       IEmailIOConfiguration.class);

                BaseMethod bmMethod = cConstructor.newInstance(bbRequest, bbResponse, m_acConfig);
                bmMethod.execute();
            }
        }
        catch (Throwable tException)
        {
            String sMessage = tException.getLocalizedMessage();
            LOG.error(tException, LogMessages.TRANSACTION_ERROR, sMessage);

            ServerLocalizableException sle = null;

            // What we'll do here is see which exception in the causes is a
            // ServerLocalizableException. That message we will use to send as the main message to
            // the end user. If no SLE could be found the current exception is wrapped into a new
            // SLE exception.
            int iMaxHop = 30;
            int iCurrentHop = 0;
            Throwable tCurrent = tException;

            while ((tCurrent != null) && (iCurrentHop < iMaxHop))
            {
                if (tCurrent instanceof ServerLocalizableException)
                {
                    sle = (ServerLocalizableException) tCurrent;
                }
                tCurrent = tCurrent.getCause();

                // The whole hop-thing is to make sure that if there are cyclic references in the
                // exception causes the code won't go into an endless loop.
                iCurrentHop++;
            }

            if (sle == null)
            {
                // No SLE could be found, so wrap the current exception.
                sle = new EmailIOException(tException,
                                           InboundEmailExceptionMessages.IEE_EXECUTING_REQUEST,
                                           ExceptionUtil.getSimpleErrorTrace(tException, true));
            }

            // Create the proper SOAP fault.
            sle.setPreferredLocale(ServerLocalizableException.PreferredLocale.SOAP_LOCALE);
            sle.toSOAPFault(bbResponse);

            if (bbRequest.isAsync())
            {
                bbRequest.continueTransaction();
                bReturn = false;
            }
        }

        if (LOG.isDebugEnabled() && (bReturn == true))
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Outgoing SOAP request:\n" +
                          Node.writeToString(bbResponse.getXMLNode(), false));
            }
        }

        return bReturn;
    }

    /**
     * This method gets the desired action from the method's implementation.
     *
     * @param   bbRequest  The request that was received.
     *
     * @return  The action in the implementation.
     *
     * @throws  EmailIOException  In case the action was not found.
     */
    private String getActionFromImplementation(BodyBlock bbRequest)
                                        throws EmailIOException
    {
        String sAction;

        int iImplNode = bbRequest.getMethodDefinition().getImplementation();

        XPathMetaInfo xmi = new XPathMetaInfo();
        xmi.addNamespaceBinding("ns", EmailIOConnectorConstants.NS_IMPLEMENTATION);

        sAction = XPathHelper.getStringValue(iImplNode, "./ns:action/text()", xmi, "");

        if (sAction.length() == 0)
        {
            throw new EmailIOException(EmailIOExceptionMessages.EIOE_NO_ACTION_FOUND_IN_THE_METHOD_IMPLEMENTATION);
        }

        return sAction;
    }
}
