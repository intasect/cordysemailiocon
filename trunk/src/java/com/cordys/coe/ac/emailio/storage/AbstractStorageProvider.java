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
 package com.cordys.coe.ac.emailio.storage;

import com.cordys.coe.ac.emailio.EmailIOConnectorConstants;
import com.cordys.coe.ac.emailio.config.IEmailBox;
import com.cordys.coe.ac.emailio.config.trigger.ITrigger;
import com.cordys.coe.ac.emailio.exception.StorageProviderException;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.management.IManagedComponent;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class can be used as a base for the storage providers. It will handle the parsing of the
 * parameters.
 *
 * @author  pgussow
 */
public abstract class AbstractStorageProvider
    implements IEmailStorageProvider
{
    /**
     * Holds an empty string.
     */
    private static final String EMPTY_STRING = "";
    /**
     * Holds the logger that is used.
     */
    private CordysLogger LOG = CordysLogger.getCordysLogger(this.getClass());
    /**
     * Holds the email box.
     */
    private IEmailBox m_ebEmailBox;
    /**
     * Holds the parameters and their value.
     */
    private Map<String, Object> m_mParameters = new LinkedHashMap<String, Object>();
    /**
     * Holds all defined triggers. This has to be synchronized, because it could be possible that
     * triggers are added dynamically.
     */
    private Map<String, ITrigger> m_mTriggers = Collections.synchronizedMap(new LinkedHashMap<String, ITrigger>());

    /**
     * Holds the dn of the SOAP processor.
     */
    private String m_sSoapProcessorDN;

    /**
     * Creates a new AbstractStorageProvider object.
     */
    public AbstractStorageProvider()
    {
    }

    /**
     * Adds a trigger to the store.
     *
     * <p>This method is used for storing trigger definitions in the store. If the implementation
     * provides persistent trigger storage, the trigger should be persisted immediately. No
     * notification will be sent to the store in order to trigger explicit data storing as the
     * persistence of the triggers must be guaranteed immediately after their insertion, regardless
     * of external circumstances as power failures or system crashes.</p>
     *
     * <p>Only triggers that are defined in the global configuration of a mailbox should be stored
     * as volatile triggers, i.e. with the isPersistent argument set to <code>false</code>.</p>
     *
     * @param   tTrigger       The trigger to be stored.
     * @param   bIsPersistent  <code>True</code> if the trigger should be stored persistently,
     *                         <code>false</code> for volatile triggers.
     *
     * @throws  StorageProviderException  if something goes wrong while storing the trigger.
     *
     * @see     com.cordys.coe.ac.emailio.storage.IEmailStorageProvider#addTrigger(com.cordys.coe.ac.emailio.config.trigger.ITrigger,
     *          boolean)
     */
    @Override public void addTrigger(ITrigger tTrigger, boolean bIsPersistent)
                              throws StorageProviderException
    {
        m_mTriggers.put(tTrigger.getName(), tTrigger);

        if (bIsPersistent)
        {
            persistTrigger(tTrigger);
        }
    }

    /**
     * Checks if the trigger with the given name is contained in the store.
     *
     * @param   triggerName  The name of the trigger.
     *
     * @return  <code>True</code> if a trigger with that name exists, <code>false</code> otherwise.
     *
     * @see     com.cordys.coe.ac.emailio.storage.IEmailStorageProvider#containsTrigger(java.lang.String)
     */
    @Override public boolean containsTrigger(String triggerName)
    {
        return m_mTriggers.containsKey(triggerName);
    }

    /**
     * Returns a map of the parameters that were present in the configuration block.
     *
     * <p>This method is public primarily to make testing easier. It should not be called by
     * external code under normal conditions.</p>
     *
     * @return  The map of the parameters that were present in the configuration block.
     */
    public Map<String, Object> getParameters()
    {
        return new LinkedHashMap<String, Object>(m_mParameters);
    }

    /**
     * This method gets the DN of the SOAP processor in which this storage provider is running.
     *
     * @return  The DN of the SOAP processor in which this storage provider is running.
     *
     * @see     com.cordys.coe.ac.emailio.storage.IEmailStorageProvider#getSoapProcessorDN()
     */
    @Override public String getSoapProcessorDN()
    {
        return m_sSoapProcessorDN;
    }

    /**
     * Returns the trigger instance with the given name.
     *
     * @param   sTriggerName  The name of the trigger to return.
     *
     * @return  The trigger with the given name or <code>null</code> if no such trigger exists.
     *
     * @see     com.cordys.coe.ac.emailio.storage.IEmailStorageProvider#getTrigger(java.lang.String)
     */
    @Override public ITrigger getTrigger(String sTriggerName)
    {
        return m_mTriggers.get(sTriggerName);
    }

    /**
     * Returns a collection containing all triggers in this store.
     *
     * <p>The collection that is returned by this method should never be modified by the caller. In
     * fact it is strongly advised to implement this method in a way that creates an unmodifiable
     * collection.</p>
     *
     * <p>If no triggers are contained in the store, an empty collection is returned. The return
     * value will never be <code>null</code>.</p>
     *
     * @return  All triggers in the store.
     *
     * @see     com.cordys.coe.ac.emailio.storage.IEmailStorageProvider#getTriggers()
     */
    @Override public Collection<ITrigger> getTriggers()
    {
        return Collections.unmodifiableCollection(m_mTriggers.values());
    }

    /**
     * Returns a collection containing all triggers in this store that apply to a specific folder.
     *
     * <p>The collection that is returned by this method should never be modified by the caller. In
     * fact it is strongly advised to implement this method in a way that creates an unmodifiable
     * collection.</p>
     *
     * <p>If no appropriate triggers are contained in the store, an empty collection is returned.
     * The return value will never be <code>null</code>.</p>
     *
     * <p>The trigger list obeys the priority set for a certain trigger. The lower the number, the
     * higher the trigger will be in the list. If 2 triggers have the same priority the order is NOT
     * guaranteed.</p>
     *
     * @param   sFolderName  The name if the folder for which the triggers should be fetched.
     *
     * @return  All triggers in the store that apply to a specific folder.
     *
     * @see     com.cordys.coe.ac.emailio.storage.IEmailStorageProvider#getTriggers(java.lang.String)
     */
    @Override public Collection<ITrigger> getTriggers(String sFolderName)
    {
        List<ITrigger> lResult = new LinkedList<ITrigger>();

        for (ITrigger tTrigger : getTriggers())
        {
            if (tTrigger.appliesTo(sFolderName))
            {
                // Now we need to figure out where the trigger should be added.
                boolean bAdded = false;

                for (int iCount = 0; iCount < lResult.size(); iCount++)
                {
                    ITrigger tTemp = lResult.get(iCount);

                    if (tTemp.getPriority() > tTrigger.getPriority())
                    {
                        lResult.add(iCount, tTrigger);
                        bAdded = true;
                        break;
                    }
                }

                // If the trigger was not yet added, it will be added to the end of the list.
                if (!bAdded)
                {
                    lResult.add(tTrigger);
                }
            }
        }

        if (LOG.isDebugEnabled())
        {
            StringBuilder sbTemp = new StringBuilder(2048);
            sbTemp.append("Order of the triggers is:\n");

            for (ITrigger tTrigger : lResult)
            {
                sbTemp.append("Name: ").append(tTrigger.getName()).append("(prio ")
                      .append(tTrigger.getPriority()).append(")\n");
            }

            LOG.debug(sbTemp.toString());
        }

        return lResult;
    }

    /**
     * This method is called to initialize the the storage provider. Typically you have database
     * details or folder details in the configuration.
     *
     * <p>Extracts the parameters and makes them easily available.</p>
     *
     * <p>This method extracts the parameters from the XML configuration and puts them in a map in
     * order to make them more easily available for derived classes.</p>
     *
     * <p>After the parameters have been parsed, the template method {@link #postInit(IEmailBox,
     * int, XPathMetaInfo, IManagedComponent)} is called. Derived classes should override that one
     * to perform initialization tasks.</p>
     *
     * @param   ebEmailBox          The email box the message originated from.
     * @param   iConfigurationNode  The XML containing the configuration.
     * @param   xmi                 The XPath meta info to use. The prefix ns should be mapped to
     *                              the proper namespace.
     * @param   sSoapProcessorDN    The DN of the SOAP processor in which the storage provider is
     *                              running.
     * @param   mcParent            The parent managed component.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     *
     * @see     com.cordys.coe.ac.emailio.storage.IEmailStorageProvider#initialize(IEmailBox, int,
     *          XPathMetaInfo, String, IManagedComponent)
     */
    @Override public final void initialize(IEmailBox ebEmailBox, int iConfigurationNode,
                                           XPathMetaInfo xmi, String sSoapProcessorDN,
                                           IManagedComponent mcParent)
                                    throws StorageProviderException
    {
        m_ebEmailBox = ebEmailBox;
        m_sSoapProcessorDN = sSoapProcessorDN;

        if (iConfigurationNode > 0)
        {
            int[] aiParameters = XPathHelper.selectNodes(iConfigurationNode,
                                                         "./ns:" + ELEMENT_PARAMETER, xmi);

            for (int iParameter : aiParameters)
            {
                String sName = Node.getAttribute(iParameter, ATTRIBUTE_NAME);
                String sType = Node.getAttribute(iParameter, ATTRIBUTE_TYPE, "string");
                Object oValue = null;

                if ("string".equals(sType))
                {
                    oValue = Node.getDataWithDefault(iParameter, "");
                }
                else if ("xml".equals(sType))
                {
                    oValue = new Integer(Node.getFirstElement(iParameter));
                }

                if ((sName != null) && (sName.length() > 0))
                {
                    m_mParameters.put(sName, oValue);
                }
            }
        }

        postInit(ebEmailBox, iConfigurationNode, xmi, mcParent);
    }

    /**
     * Removes the named trigger from the store.
     *
     * <p>This method should not be used for non-persistent triggers as the results may be
     * unexpected. If a non-persistent trigger that was created from the mailbox config is removed,
     * it will automatically be re-created when the connector is restartet. This will not be
     * expected or desired in most cases, so only dynamic triggers should be removed.</p>
     *
     * <p>If no trigger with the given name exists in this store, the call will be ignored.</p>
     *
     * @param   sTriggerName  The name of the trigger to be removed.
     *
     * @throws  StorageProviderException  If the trigger couldn't be removed from storage.
     *
     * @see     com.cordys.coe.ac.emailio.storage.IEmailStorageProvider#removeTrigger(java.lang.String)
     */
    @Override public void removeTrigger(String sTriggerName)
                                 throws StorageProviderException
    {
        m_mTriggers.remove(sTriggerName);

        // Remove it from the file.
        if (isTriggerPersistent(sTriggerName))
        {
            removeTriggerFromPersistence(sTriggerName);
        }
    }

    /**
     * This method dumps the configuration of this storage provider to XML.
     *
     * @param   iParent  The parent element.
     *
     * @return  The created XML structure.
     *
     * @see     com.cordys.coe.ac.emailio.storage.IEmailStorageProvider#toXML(int)
     */
    @Override public int toXML(int iParent)
    {
        int iReturn = Node.createElementNS("storage", EMPTY_STRING, EMPTY_STRING,
                                           EmailIOConnectorConstants.NS_CONFIGURATION, iParent);

        Node.createElementWithParentNS("class", this.getClass().getName(), iReturn);

        int iParameters = Node.createElementWithParentNS("parameters", null, iReturn);

        for (Map.Entry<String, Object> eParameter : getParameters().entrySet())
        {
            int iParameter = Node.createElementWithParentNS(ELEMENT_PARAMETER, null, iParameters);
            Object oValue = eParameter.getValue();
            String sType = "string";

            if (oValue instanceof String)
            {
                Node.getDocument(iParameter).createText((String) oValue, iParameter);
            }
            else if (oValue instanceof Integer)
            {
                sType = "xml";
                Node.duplicateAndAppendToChildren((Integer) oValue, (Integer) oValue, iParameter);
            }
            Node.setAttribute(iParameter, ATTRIBUTE_TYPE, sType);
            Node.setAttribute(iParameter, ATTRIBUTE_NAME, eParameter.getKey());
        }

        return iReturn;
    }

    /**
     * This method gets the email box for this store.
     *
     * @return  The email box for this store.
     */
    protected IEmailBox getEmailBox()
    {
        return m_ebEmailBox;
    }

    /**
     * This method gets the string value for a parameter.
     *
     * @param   sName  The name of the parameter.
     *
     * @return  The string value for a parameter.
     */
    protected String getStringParameter(String sName)
    {
        return (String) m_mParameters.get(sName);
    }

    /**
     * This method gets the value of the XML parameter.
     *
     * @param   sName  The name of the parameter.
     *
     * @return  The value of the XML parameter.
     */
    protected int getXMLParameter(String sName)
    {
        int iReturn = 0;

        Object oTemp = m_mParameters.get(sName);

        if ((oTemp != null) && (oTemp instanceof Integer))
        {
            iReturn = (Integer) oTemp;
        }

        return iReturn;
    }

    /**
     * Serializes the trigger to a file.
     *
     * <p>The trigger will be serialized using it toXML method. The XML created by the trigger is
     * wrapped in the envelope defined by the constant SERIALIZATION_XML_ENVELOPE.</p>
     *
     * <p>This method must be overridden in order to actually persist triggers.</p>
     *
     * @param   tTrigger  The trigger to be persisted.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    protected void persistTrigger(ITrigger tTrigger)
                           throws StorageProviderException
    {
    }

    /**
     * Adapter method that is called after the parameters are parsed.
     *
     * @param   ebEmailBox          The corresponding email box.
     * @param   iConfigurationNode  The XML containing the configuration.
     * @param   xmi                 The XPath meta info to use. The prefix ns should be mapped to
     *                              the proper namespace.
     * @param   mcParent            The parent managed component.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    protected void postInit(IEmailBox ebEmailBox, int iConfigurationNode, XPathMetaInfo xmi,
                            IManagedComponent mcParent)
                     throws StorageProviderException
    {
    }

    /**
     * This adapter method is called when the trigger needs to be removed from the actual
     * persistence layer.
     *
     * @param   sTriggerName  The name of the trigger.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    protected void removeTriggerFromPersistence(String sTriggerName)
                                         throws StorageProviderException
    {
    }
}
