package com.cordys.coe.ac.emailio.storage;

import com.cordys.coe.ac.emailio.config.IEmailBox;
import com.cordys.coe.ac.emailio.config.IXMLSerializable;
import com.cordys.coe.ac.emailio.config.trigger.ITrigger;
import com.cordys.coe.ac.emailio.exception.StorageProviderException;
import com.cordys.coe.ac.emailio.monitor.RuleContextContainer;

import com.eibus.management.IManagedComponent;

import com.eibus.xml.xpath.XPathMetaInfo;

import java.util.Collection;

/**
 * This interface holds the methods that can be used to store emails and their processing status.
 * This storage is also used for crash recovery.
 *
 * @author  pgussow
 */
public interface IEmailStorageProvider extends IXMLSerializable
{
    /**
     * Holds the name of the attribute which holds the parameter name.
     */
    String ATTRIBUTE_NAME = "name";
    /**
     * Holds the name of the attribute type.
     */
    String ATTRIBUTE_TYPE = "type";
    /**
     * Holds the name of the tag 'container'.
     */
    String ELEMENT_CONTAINER = "container";
    /**
     * Holds the name of the tag 'email'.
     */
    String ELEMENT_EMAIL = "email";
    /**
     * Holds the name of the tag 'emails'.
     */
    String ELEMENT_EMAILS = "emails";
    /**
     * Holds the name of the tag 'id'.
     */
    String ELEMENT_ID = "id";
    /**
     * Holds the name of the tag that identifies a parameter.
     */
    String ELEMENT_PARAMETER = "parameter";
    /**
     * Holds the name of the tag params which wraps the parameters.
     */
    String ELEMENT_PARAMETERS = "parameters";

    /**
     * This method will persist the contents of the Rule Context container. This allows crash
     * recovery. Based on the content of the rule context and the actual trigger definition the
     * processing could be restarted.
     *
     * <p>The storage provider must use the rccContext.setStorageID to inform the container of it's
     * storage ID.</p>
     *
     * <p>When the container is persisted it will also add the SYS_XML_STORAGE_DETAILS variable to
     * the rule context.</p>
     *
     * @param   rccContext  The context of the rule.
     * @param   tTrigger    The definition of the trigger when this context is processed.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    void addRuleContext(RuleContextContainer rccContext, ITrigger tTrigger)
                 throws StorageProviderException;

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
     */
    void addTrigger(ITrigger tTrigger, boolean bIsPersistent)
             throws StorageProviderException;

    /**
     * Checks if the trigger with the given name is contained in the store.
     *
     * @param   sTriggerName  The name of the trigger.
     *
     * @return  <code>True</code> if a trigger with that name exists, <code>false</code> otherwise.
     */
    boolean containsTrigger(String sTriggerName);

    /**
     * This method gets the XML structure containing the information about the given container.
     *
     * <p>The XML structure is defined in the configuration.xsd</p>
     *
     * @param   storageID  The ID of the storage.
     *
     * @return  The created container XML.
     *
     * @throws  StorageProviderException  In case of any exceptions
     */
    int getContainerDetailXML(String storageID)
                       throws StorageProviderException;

    /**
     * This method gets the DN of the SOAP processor in which this storage provider is running.
     *
     * @return  The DN of the SOAP processor in which this storage provider is running.
     */
    String getSoapProcessorDN();

    /**
     * Returns the trigger instance with the given name.
     *
     * @param   sTriggerName  The name of the trigger to return.
     *
     * @return  The trigger with the given name or <code>null</code> if no such trigger exists.
     */
    ITrigger getTrigger(String sTriggerName);

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
     */
    Collection<ITrigger> getTriggers();

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
     */
    Collection<ITrigger> getTriggers(String sFolderName);

    /**
     * This method is called to initialize the the storage provider. Typically you have database
     * details or folder details in the configuration.
     *
     * <p>Extracts the parameters and makes them easily available.</p>
     *
     * <p>This method extracts the parameters from the XML configuration and puts them in a map in
     * order to make them more easily available for derived classes.</p>
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
     */
    void initialize(IEmailBox ebEmailBox, int iConfigurationNode, XPathMetaInfo xmi,
                    String sSoapProcessorDN, IManagedComponent mcParent)
             throws StorageProviderException;

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
     */
    boolean isTriggerPersistent(String sTriggerName);

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
     */
    void removeTrigger(String sTriggerName)
                throws StorageProviderException;

    /**
     * This method sets the status of all messages to error. This means that either the actions
     * failed or that the actual trigger message failed.
     *
     * @param   rccContext   The rule context container containing all messages.
     * @param   sStatusInfo  The exception details for this error.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    void setContainerStatusActionError(RuleContextContainer rccContext, String sStatusInfo)
                                throws StorageProviderException;

    /**
     * This method sets the status of the given container to completed. This means that the messages
     * were delivered properly.
     *
     * @param   rccContext  The rule context container containing all messages.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    void setContainerStatusCompleted(RuleContextContainer rccContext)
                              throws StorageProviderException;

    /**
     * This method sets the status of all messages to error. This means that either the actions
     * failed or that the actual trigger message failed.
     *
     * @param   rccContext   The rule context container containing all messages.
     * @param   sStatusInfo  The exception details for this error.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    void setContainerStatusError(RuleContextContainer rccContext, String sStatusInfo)
                          throws StorageProviderException;

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
     */
    void setContainerStatusInProgress(RuleContextContainer rccContext)
                               throws StorageProviderException;
}
