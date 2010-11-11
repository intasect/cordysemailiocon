

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
package com.cordys.coe.ac.emailio.storage;

import com.cordys.coe.ac.emailio.EmailIOConnectorConstants;
import com.cordys.coe.ac.emailio.config.trigger.ITrigger;
import com.cordys.coe.ac.emailio.exception.StorageProviderException;
import com.cordys.coe.ac.emailio.monitor.RuleContextContainer;

import com.eibus.util.system.Native;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class is used if no storage configuration was found. Keep in mind that this is by no means a
 * production storage provider since it does not provide any persistence. So if the process crashes
 * all information is lost. Also no logging/archiving is provided so it is possible that mails are
 * lost when processing was not successful.
 *
 * @author  pgussow
 */
public class DefaultMemoryStore extends AbstractStorageProvider
{
    /**
     * Holds all triggers that should be persisted.
     */
    private Map<String, ITrigger> m_mTriggers = new LinkedHashMap<String, ITrigger>();

    /**
     * This method will persist the contents of the Rule Context container. This allows crash
     * recovery. Based on the content of the rule context and the actual trigger definition the
     * processing could be restarted.
     *
     * <p>The storage provider must use the rccContext.setStorageID to inform the container of it's
     * storage ID.</p>
     *
     * @param   rccContext  The context of the rule.
     * @param   tTrigger    The definition of the trigger when this context is processed.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     *
     * @see     com.cordys.coe.ac.emailio.storage.IEmailStorageProvider#addRuleContext(com.cordys.coe.ac.emailio.monitor.RuleContextContainer,
     *          com.cordys.coe.ac.emailio.config.trigger.ITrigger)
     */
    @Override public void addRuleContext(RuleContextContainer rccContext, ITrigger tTrigger)
                                  throws StorageProviderException
    {
        rccContext.setStorageID(Native.createGuid());

        // No persistence is provided for mail messages.
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
    }

    /**
     * Checks if the trigger with the given name is contained in the store.
     *
     * @param   sTriggerName  The name of the trigger.
     *
     * @return  <code>True</code> if a trigger with that name exists, <code>false</code> otherwise.
     *
     * @see     com.cordys.coe.ac.emailio.storage.IEmailStorageProvider#containsTrigger(java.lang.String)
     */
    @Override public boolean containsTrigger(String sTriggerName)
    {
        return m_mTriggers.containsKey(sTriggerName);
    }

    /**
     * @see  com.cordys.coe.ac.emailio.storage.IEmailStorageProvider#getContainerDetailXML(java.lang.String)
     */
    @Override public int getContainerDetailXML(String storageID)
                                        throws StorageProviderException
    {
        int returnValue = 0;

        // Now we can read the details and add the XML to the rule context
        Document doc = EmailIOConnectorConstants.getDocument();
        returnValue = doc.createElementNS(ELEMENT_CONTAINER, null, null,
                                          EmailIOConnectorConstants.NS_DATA, 0);

        Node.createElementWithParentNS(ELEMENT_ID, storageID, returnValue);

        return returnValue;
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
     * Returns the persistence status of a named trigger.
     *
     * <p>This method will return <code>true</code> only if the specified trigger is actually
     * contained in persistent storage. Note that this method may return <code>false</code> even if
     * persistence was requested when adding the trigger - if a store implementation doesn't support
     * persistence, the flag will be ignored during insertion.</p>
     *
     * <p>If no trigger exists with the given name, <code>false</code> is returned.</p>
     *
     * @param   sTriggerName  The name of the trigger.
     *
     * @return  <code>True</code> if the trigger is stored persistently, <code>false</code>
     *          otherwise.
     *
     * @see     com.cordys.coe.ac.emailio.storage.IEmailStorageProvider#isTriggerPersistent(java.lang.String)
     */
    @Override public boolean isTriggerPersistent(String sTriggerName)
    {
        return false;
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
    }

    /**
     * This method sets the status of all messages to error. This means that either the actions
     * failed or that the actual trigger message failed.
     *
     * @param   rccContext   The rule context container containing all messages.
     * @param   sStatusInfo  The exception details for this error.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     *
     * @see     com.cordys.coe.ac.emailio.storage.IEmailStorageProvider#setContainerStatusActionError(com.cordys.coe.ac.emailio.monitor.RuleContextContainer,
     *          java.lang.String)
     */
    @Override public void setContainerStatusActionError(RuleContextContainer rccContext,
                                                        String sStatusInfo)
                                                 throws StorageProviderException
    {
        // No persistence is provided for mail messages.
    }

    /**
     * This method sets the status of the given container to completed. This means that the messages
     * were delivered properly.
     *
     * @param   rccContext  The rule context container containing all messages.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     *
     * @see     com.cordys.coe.ac.emailio.storage.IEmailStorageProvider#setContainerStatusCompleted(com.cordys.coe.ac.emailio.monitor.RuleContextContainer)
     */
    @Override public void setContainerStatusCompleted(RuleContextContainer rccContext)
                                               throws StorageProviderException
    {
        // No persistence is provided for mail messages.
    }

    /**
     * This method sets the status of all messages to error. This means that either the actions
     * failed or that the actual trigger message failed.
     *
     * @param   rccContext         The rule context container containing all messages.
     * @param   sExceptionDetails  The exception details for this error.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     *
     * @see     com.cordys.coe.ac.emailio.storage.IEmailStorageProvider#setContainerStatusError(com.cordys.coe.ac.emailio.monitor.RuleContextContainer,
     *          java.lang.String)
     */
    @Override public void setContainerStatusError(RuleContextContainer rccContext,
                                                  String sExceptionDetails)
                                           throws StorageProviderException
    {
        // No persistence is provided for mail messages.
    }

    /**
     * This method will update the status in the storage provider to 'in progress' This means that
     * the InboundEmailConnector is in the process of sending the messages to Cordys. If the process
     * crashed during this state we will not be able to restart automatically since we cannot be
     * sure that the SOAP call was processed yes or no. So from a UI the end user will have to
     * manually decide whether or not the SOAP call can be sent again.
     *
     * @param   rccContext  The rule context container containing all messages.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     *
     * @see     com.cordys.coe.ac.emailio.storage.IEmailStorageProvider#setContainerStatusInProgress(com.cordys.coe.ac.emailio.monitor.RuleContextContainer)
     */
    @Override public void setContainerStatusInProgress(RuleContextContainer rccContext)
                                                throws StorageProviderException
    {
        // No persistence is provided for mail messages.
    }
}
