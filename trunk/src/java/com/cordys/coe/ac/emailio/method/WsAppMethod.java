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

import com.cordys.coe.ac.emailio.EObjectAction;
import com.cordys.coe.ac.emailio.EmailIOConnectorConstants;
import com.cordys.coe.ac.emailio.exception.EmailIOException;
import com.cordys.coe.ac.emailio.exception.StorageProviderException;
import com.cordys.coe.ac.emailio.localization.EmailIOExceptionMessages;
import com.cordys.coe.ac.emailio.localization.InboundEmailExceptionMessages;
import com.cordys.coe.ac.emailio.objects.ContextContainer;
import com.cordys.coe.ac.emailio.objects.EmailBox;
import com.cordys.coe.ac.emailio.objects.EmailMessage;
import com.cordys.coe.ac.emailio.objects.IStorageProviderQueryManager;
import com.cordys.coe.ac.emailio.objects.TriggerDefinition;
import com.cordys.coe.exception.ServerLocalizableException;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.cordys.cpc.bsf.busobject.BusObject;
import com.cordys.cpc.bsf.busobject.BusObjectArray;
import com.cordys.cpc.bsf.busobject.BusObjectIterator;
import com.cordys.cpc.bsf.busobject.exception.ILocalizableException;
import com.cordys.cpc.bsf.query.Cursor;
import com.cordys.cpc.bsf.util.TupleHandling;

import com.eibus.localization.IStringResource;

import com.eibus.soap.BodyBlock;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * All methods that are WsApps-based should be handled by this class.
 *
 * <p>This class must be in the com.cordys.cpc.bsf.connector because the method BsfMethodCall.invoke
 * is package protected.</p>
 *
 * @author  pgussow
 */
public class WsAppMethod extends BaseMethod
{
    /**
     * Holds the action to execute.
     */
    private EObjectAction m_oaAction;
    /**
     * Holds the storage provider query object.
     */
    private IStorageProviderQueryManager m_spqmSPQuery;
    /**
     * Holds the Xath Meta info object to use.
     */
    private XPathMetaInfo m_xmi;

    /**
     * Constructor.
     *
     * @param  bbRequest    The request bodyblock.
     * @param  bbResponse   The response bodyblock.
     * @param  oaAction     The action to execute.
     * @param  spqmSPQuery  The query manager to use for executing the queries.
     */
    public WsAppMethod(BodyBlock bbRequest, BodyBlock bbResponse, EObjectAction oaAction,
                       IStorageProviderQueryManager spqmSPQuery)
    {
        super(bbRequest, bbResponse, null);
        m_oaAction = oaAction;
        m_spqmSPQuery = spqmSPQuery;

        m_xmi = new XPathMetaInfo();
        m_xmi.addNamespaceBinding("ns", EmailIOConnectorConstants.NS_CONFIGURATION);
    }

    /**
     * This method executes the actual request. The idea is that even though we adjusted the
     * implementation type that the default WsApps transaction will handle the request.
     *
     * @throws  EmailIOException  In case of any exceptions.
     *
     * @see     com.cordys.coe.ac.emailio.method.BaseMethod#execute()
     */
    @Override public void execute()
                           throws EmailIOException
    {
        try
        {
            // First we need to get the parameters handled properly. In order to do that we'll use
            // the definition and based on that read the parameters from the request. Then we'll
            // form an array containing the values for these parameters
            HashMap<String, MethodParameter> hmParameters = getParameters();

            // Now we have the parameters, so we can execute the actual call.
            Object oResult = null;

            switch (m_oaAction)
            {
                case SEARCH_CONTAINERS:
                    oResult = doSeachContainers(hmParameters);
                    break;

                case SEARCH_EMAIL_BOXES:
                    oResult = doSeachEmailBoxes(hmParameters);
                    break;

                case SEARCH_EMAIL_MESSAGES:
                    oResult = doSeachEmailMessages(hmParameters);
                    break;

                case SEARCH_TRIGGER_DEFINITIONS:
                    oResult = doSeachTriggerDefinitions(hmParameters);
                    break;

                case GET_CONTEXT_CONTAINER:
                    oResult = doGetContextContainer(hmParameters);
                    break;

                case GET_EMAIL_BOX:
                    oResult = doGetEmailBox(hmParameters);
                    break;

                case GET_EMAIL_MESSAGE:
                    oResult = doGetEmailMessage(hmParameters);
                    break;

                case GET_TRIGGER_DEFINITION:
                    oResult = doGetTriggerDefinition(hmParameters);
                    break;
            }

            if (oResult != null)
            {
                // Create the response tuples.
                int[] aiTuples = null;

                if (oResult instanceof BusObjectIterator)
                {
                    BusObjectIterator boi = (BusObjectIterator) oResult;
                    aiTuples = TupleHandling.makeTupleArray(boi,
                                                            Node.getDocument(getResponse()
                                                                             .getXMLNode()),
                                                            getRequest().getMethodDefinition()
                                                            .getNamespace());
                }
                else if (oResult instanceof BusObject)
                {
                    BusObject bo = (BusObject) oResult;
                    aiTuples = TupleHandling.makeTupleArray(bo,
                                                            Node.getDocument(getResponse()
                                                                             .getXMLNode()),
                                                            getRequest().getMethodDefinition()
                                                            .getNamespace());
                }
                else
                {
                    throw new EmailIOException(EmailIOExceptionMessages.EIOE_INVALID_RETURN_TYPE_CANNOT_BUILD_UP_RESPONSE_FOR_RESULT_TYPE_0,
                                               ((oResult != null) ? oResult.getClass().getName()
                                                                  : "null"));
                }

                // Add them to the actual response.
                int iResponse = getResponse().getXMLNode();

                for (int iTuple : aiTuples)
                {
                    Node.appendToChildren(iTuple, iResponse);
                }
            }
        }
        catch (Exception e)
        {
            // It might be that in the causes there is an exception which uses a string resource.
            // If so, we'll reuse that one.
            IStringResource sr = InboundEmailExceptionMessages.IEE_ERROR_EXECUTING_WSAPPSERVER_BASED_METHOD;
            Object[] aoParams = null;
            Throwable tCause = e;

            while (tCause != null)
            {
                if (tCause instanceof ILocalizableException)
                {
                    ILocalizableException ls = (ILocalizableException) tCause;
                    sr = ls.getLocalizableMessageID();
                    aoParams = ls.getMessageParameters();
                }
                else if (tCause instanceof ServerLocalizableException)
                {
                    ServerLocalizableException sle = (ServerLocalizableException) tCause;
                    sr = sle.getMessageObject();
                    aoParams = sle.getMessageParameters();
                }

                tCause = tCause.getCause();
            }

            throw new EmailIOException(e, sr, aoParams);
        }
    }

    /**
     * This method parses the parameters from the request based on the definition.
     *
     * @return  The list of parameter values.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    protected HashMap<String, MethodParameter> getParameters()
                                                      throws StorageProviderException
    {
        HashMap<String, MethodParameter> hmReturn = new HashMap<String, MethodParameter>();

        int[] aiParameters = XPathHelper.selectNodes(getRequest().getMethodDefinition()
                                                     .getImplementation(), "parameters/*");

        for (int iCount = 0; iCount < aiParameters.length; iCount++)
        {
            int iParameter = aiParameters[iCount];
            MethodParameter mp = new MethodParameter(iParameter, getRequest().getXMLNode());
            hmReturn.put(mp.getName(), mp);
        }

        return hmReturn;
    }

    /**
     * This method handles the execution of the getContextContainer method.
     *
     * @param   hmParameters  The parameters as received from the request.
     *
     * @return  The ContextContainer found.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    private ContextContainer doGetContextContainer(HashMap<String, MethodParameter> hmParameters)
                                            throws StorageProviderException
    {
        String sID = (String) hmParameters.get("ID").getValue();

        return m_spqmSPQuery.getContextContainer(sID);
    }

    /**
     * This method handles the execution of the getEmailBox method.
     *
     * @param   hmParameters  The parameters as received from the request.
     *
     * @return  The EmailBox found.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    private EmailBox doGetEmailBox(HashMap<String, MethodParameter> hmParameters)
                            throws StorageProviderException
    {
        String sName = (String) hmParameters.get("Name").getValue();

        return m_spqmSPQuery.getEmailBox(sName);
    }

    /**
     * This method handles the execution of the getEmailMessage method.
     *
     * @param   hmParameters  The parameters as received from the request.
     *
     * @return  The EmailMessage found.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    private EmailMessage doGetEmailMessage(HashMap<String, MethodParameter> hmParameters)
                                    throws StorageProviderException
    {
        String sID = (String) hmParameters.get("ID").getValue();

        return m_spqmSPQuery.getEmailMessage(sID);
    }

    /**
     * This method handles the execution of the getTriggerDefinition method.
     *
     * @param   hmParameters  The parameters as received from the request.
     *
     * @return  The TriggerDefinitions found.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    private TriggerDefinition doGetTriggerDefinition(HashMap<String, MethodParameter> hmParameters)
                                              throws StorageProviderException
    {
        String sID = (String) hmParameters.get("ID").getValue();

        return m_spqmSPQuery.getTriggerDefinition(sID);
    }

    /**
     * This method handles the execution of the seachContainers method.
     *
     * @param   hmParameters  The parameters as received from the request.
     *
     * @return  The iterator with containers.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    private BusObjectIterator doSeachContainers(HashMap<String, MethodParameter> hmParameters)
                                         throws StorageProviderException
    {
        String sEmailBoxID = (String) hmParameters.get("EmailBoxID").getValue();
        String sStatusInformation = (String) hmParameters.get("StatusInformation").getValue();
        String sTriggerDefinition = (String) hmParameters.get("TriggerDefinition").getValue();
        String sProcessingStatus = (String) hmParameters.get("ProcessingStatus").getValue();
        Date dFromCreateDate = (Date) hmParameters.get("FromCreateDate").getValue();
        Date dToCreateDate = (Date) hmParameters.get("ToCreateDate").getValue();
        Date dFromCompleteDate = (Date) hmParameters.get("FromCompleteDate").getValue();
        Date dToCompleteDate = (Date) hmParameters.get("ToCompleteDate").getValue();
        Date dFromStatusChangeDate = (Date) hmParameters.get("FromStatusChangeDate").getValue();
        Date dToStatusChangeDate = (Date) hmParameters.get("ToStatusChangeDate").getValue();
        Cursor cCursor = (Cursor) hmParameters.get("cursor").getValue();

        List<ContextContainer> lResult = m_spqmSPQuery.searchContainers(sEmailBoxID,
                                                                        sStatusInformation,
                                                                        sTriggerDefinition,
                                                                        sProcessingStatus,
                                                                        dFromCreateDate,
                                                                        dToCreateDate,
                                                                        dFromCompleteDate,
                                                                        dToCompleteDate,
                                                                        dFromStatusChangeDate,
                                                                        dToStatusChangeDate,
                                                                        cCursor);
        return new BusObjectArray(lResult);
    }

    /**
     * This method handles the execution of the seachEmailBoxes method.
     *
     * @param   hmParameters  The parameters as received from the request.
     *
     * @return  The iterator with email boxes.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    private BusObjectIterator doSeachEmailBoxes(HashMap<String, MethodParameter> hmParameters)
                                         throws StorageProviderException
    {
        String sName = (String) hmParameters.get("Name").getValue();
        String sHost = (String) hmParameters.get("Host").getValue();
        String sType = (String) hmParameters.get("Type").getValue();
        String sConfiguration = (String) hmParameters.get("Configuration").getValue();
        Cursor cCursor = (Cursor) hmParameters.get("cursor").getValue();

        List<EmailBox> lResult = m_spqmSPQuery.searchEmailBoxes(sName, sHost, sType, sConfiguration,
                                                                cCursor);

        return new BusObjectArray(lResult);
    }

    /**
     * This method handles the execution of the seachEmailMessages method.
     *
     * @param   hmParameters  The parameters as received from the request.
     *
     * @return  The iterator with email messages.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    private BusObjectIterator doSeachEmailMessages(HashMap<String, MethodParameter> hmParameters)
                                            throws StorageProviderException
    {
        String sContextContainerID = (String) hmParameters.get("ContextContainerID").getValue();
        String sFrom = (String) hmParameters.get("From").getValue();
        String sTo = (String) hmParameters.get("To").getValue();
        String sSubject = (String) hmParameters.get("Subject").getValue();
        Date dFromSendDate = (Date) hmParameters.get("FromSendDate").getValue();
        Date dToSendDate = (Date) hmParameters.get("ToSendDate").getValue();
        Date dFromReceiveDate = (Date) hmParameters.get("FromReceiveDate").getValue();
        Date dToReceiveDate = (Date) hmParameters.get("ToReceiveDate").getValue();
        Cursor cCursor = (Cursor) hmParameters.get("cursor").getValue();

        List<EmailMessage> lResult = m_spqmSPQuery.searchEmailMessages(sContextContainerID, sFrom,
                                                                       sTo, sSubject, dFromSendDate,
                                                                       dToSendDate,
                                                                       dFromReceiveDate,
                                                                       dToReceiveDate, cCursor);

        return new BusObjectArray(lResult);
    }

    /**
     * This method handles the execution of the seachTriggerDefinitions method.
     *
     * @param   hmParameters  The parameters as received from the request.
     *
     * @return  The iterator with trigger defintions.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    private BusObjectIterator doSeachTriggerDefinitions(HashMap<String, MethodParameter> hmParameters)
                                                 throws StorageProviderException
    {
        String sName = (String) hmParameters.get("Name").getValue();
        String sEmailBoxID = (String) hmParameters.get("EmailBoxID").getValue();
        String sDefinition = (String) hmParameters.get("Definition").getValue();
        Cursor cCursor = (Cursor) hmParameters.get("cursor").getValue();

        List<TriggerDefinition> lResult = m_spqmSPQuery.searchTriggerDefinitions(sName, sEmailBoxID,
                                                                                 sDefinition,
                                                                                 cCursor);

        return new BusObjectArray(lResult);
    }
}
