

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
import com.cordys.coe.ac.emailio.config.IEmailBox;
import com.cordys.coe.ac.emailio.config.trigger.ITrigger;
import com.cordys.coe.ac.emailio.config.trigger.TriggerFactory;
import com.cordys.coe.ac.emailio.exception.StorageProviderException;
import com.cordys.coe.ac.emailio.localization.LogMessages;
import com.cordys.coe.ac.emailio.localization.StorageProviderExceptionMessages;
import com.cordys.coe.ac.emailio.monitor.RuleContextContainer;
import com.cordys.coe.ac.emailio.objects.ContextContainer;
import com.cordys.coe.ac.emailio.objects.EmailBox;
import com.cordys.coe.ac.emailio.objects.EmailMessage;
import com.cordys.coe.ac.emailio.objects.IStorageProviderQueryManager;
import com.cordys.coe.ac.emailio.objects.TriggerDefinition;
import com.cordys.coe.ac.emailio.storage.db.ContentUtil;
import com.cordys.coe.ac.emailio.storage.db.DBContextContainer;
import com.cordys.coe.ac.emailio.storage.db.DBEmail;
import com.cordys.coe.ac.emailio.storage.db.DBEmailBox;
import com.cordys.coe.ac.emailio.storage.db.DBTrigger;
import com.cordys.coe.ac.emailio.util.WsAppUtil;

import com.cordys.cpc.bsf.busobject.BSF;
import com.cordys.cpc.bsf.busobject.BsfContext;
import com.cordys.cpc.bsf.busobject.BusObjectIterator;
import com.cordys.cpc.bsf.busobject.DMLStatement;
import com.cordys.cpc.bsf.busobject.exception.BsfNoContextException;
import com.cordys.cpc.bsf.query.Cursor;

import com.eibus.directory.soap.DN;

import com.eibus.management.IManagedComponent;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.system.Native;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This storage provider can be used to store the details in a folder structure.
 *
 * @author  pgussow
 */
public class DBStorageProvider extends AbstractStorageProvider
    implements IStorageProviderQueryManager
{
    /**
     * Holds the name of the parameter that holds whether or not compression should be used.
     */
    private static final String PARAM_COMPRESS_DATA = "compressdata";
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(DBStorageProvider.class);

    /**
     * This method will persist the contents of the Rule Context container. This allows crash
     * recovery. Based on the content of the rule context and the actual trigger definition the
     * processing could be restarted.
     *
     * <p>When the container is persisted it will also add the SYS_XML_STORAGE_DETAILS variable to
     * the rule context.</p>
     *
     * @param   rccContext  The context of the rule.
     * @param   tTrigger    The definition of the trigger when this context is processed.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     *
     * @see     IEmailStorageProvider#addRuleContext(RuleContextContainer, ITrigger)
     */
    @Override public void addRuleContext(RuleContextContainer rccContext, ITrigger tTrigger)
                                  throws StorageProviderException
    {
        registerContext();

        String sTransactionID = startTransaction();

        try
        {
            DBContextContainer ccReturn = DBContextContainer.storeRuleContextContainer(rccContext,
                                                                                       getEmailBox(),
                                                                                       tTrigger);

            rccContext.setStorageID(ccReturn.getID());

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Added context to the database with id " + rccContext.getStorageID());
            }

            BSF.commitTransaction(sTransactionID);

            int containerNode = getContainerDetailXML(ccReturn.getID());

            try
            {
                // Add it to the rule context.
                rccContext.addContainerDetailsXML(containerNode);
            }
            catch (Exception e)
            {
                if (containerNode != 0)
                {
                    Node.delete(containerNode);
                }
                throw new StorageProviderException(StorageProviderExceptionMessages.SPE_ERROR_CREATING_CONTAINER_XML_DETAILS_FOR_STORAGE_ID_0,
                                                   ccReturn.getID());
            }
        }
        catch (Exception e)
        {
            BSF.abortTransaction(sTransactionID);

            if (e instanceof StorageProviderException)
            {
                throw (StorageProviderException) e;
            }

            throw new StorageProviderException(e,
                                               StorageProviderExceptionMessages.SPE_ERROR_STORING_THE_RULE_CONTEXT_CONTAINER);
        }
    }

    /**
     * This method commits the current transaction.
     *
     * @param  sTransactionID  The ID of the transaction.
     */
    public void commitTransaction(String sTransactionID)
    {
        BSF.commitTransaction(sTransactionID);
    }

    /**
     * @see  com.cordys.coe.ac.emailio.storage.IEmailStorageProvider#getContainerDetailXML(java.lang.String)
     */
    @Override public int getContainerDetailXML(String storageID)
                                        throws StorageProviderException
    {
        int returnValue = 0;

        String sTransactionID = startTransaction();

        try
        {
            // First get the container details
            DBContextContainer ccContainer = DBContextContainer.getContextContainerObject(storageID);

            if (ccContainer == null)
            {
                throw new StorageProviderException(StorageProviderExceptionMessages.SPE_COULD_NOT_FIND_CONTEXT_CONTAINER_WITH_ID_0,
                                                   storageID);
            }

            // Now we can read the details and add the XML to the rule context
            Document doc = EmailIOConnectorConstants.getDocument();
            returnValue = doc.createElementNS(ELEMENT_CONTAINER, null, null,
                                              EmailIOConnectorConstants.NS_DATA, 0);

            // Add the details for the container to the XML structure
            ccContainer.addContainerDetailXML(returnValue);

            // Now we also need to get the emails for this container.
            BusObjectIterator boi = DBEmail.getEmailObjectsForContextContainer(ccContainer.getID());

            if (boi.hasMoreElements())
            {
                int emailsNode = Node.createElementWithParentNS(ELEMENT_EMAILS, null, returnValue);

                while (boi.hasMoreElements())
                {
                    DBEmail cc = (DBEmail) boi.nextElement();

                    int emailNode = Node.createElementWithParentNS(ELEMENT_EMAIL, null, emailsNode);

                    cc.addContainerDetailXML(emailNode);
                }
            }

            commitTransaction(sTransactionID);
        }
        catch (Exception e)
        {
            BSF.abortTransaction(sTransactionID);

            if (e instanceof StorageProviderException)
            {
                throw (StorageProviderException) e;
            }

            throw new StorageProviderException(e,
                                               StorageProviderExceptionMessages.SPE_ERROR_GETTING_CONTAINER_WITH_ID,
                                               storageID);
        }

        return returnValue;
    }

    /**
     * This method gets the container with the given ID.
     *
     * @param   sID  The ID of the container.
     *
     * @return  The container with the given ID.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     *
     * @see     IStorageProviderQueryManager#getContextContainer(String)
     */
    @Override public ContextContainer getContextContainer(String sID)
                                                   throws StorageProviderException
    {
        ContextContainer ccReturn = null;

        String sTransactionID = startTransaction();

        try
        {
            DBContextContainer ccContainer = DBContextContainer.getContextContainerObject(sID);

            if (ccContainer != null)
            {
                ccReturn = ccContainer.createGenericContextContainer();
            }

            commitTransaction(sTransactionID);
        }
        catch (Exception e)
        {
            BSF.abortTransaction(sTransactionID);

            if (e instanceof StorageProviderException)
            {
                throw (StorageProviderException) e;
            }

            throw new StorageProviderException(e,
                                               StorageProviderExceptionMessages.SPE_ERROR_GETTING_CONTAINER_WITH_ID,
                                               sID);
        }

        return ccReturn;
    }

    /**
     * This method gets the email box with the given ID.
     *
     * @param   sID  The ID (or name) of the emailbox.
     *
     * @return  The EmailBox found.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     *
     * @see     IStorageProviderQueryManager#getEmailBox(String)
     */
    @Override public EmailBox getEmailBox(String sID)
                                   throws StorageProviderException
    {
        return getEmailBox(sID, true);
    }

    /**
     * This method gets the email box with the given ID.
     *
     * @param   sID        The ID (or name) of the emailbox.
     * @param   bValidate  Whether or not the configuration should be validated.
     *
     * @return  The EmailBox found.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     *
     * @see     IStorageProviderQueryManager#getEmailBox(String, boolean)
     */
    @Override public EmailBox getEmailBox(String sID, boolean bValidate)
                                   throws StorageProviderException
    {
        EmailBox ebReturn = null;

        String sTransactionID = startTransaction();

        try
        {
            DBEmailBox ebEmailBox = DBEmailBox.getEmailBoxObject(sID);

            if (ebEmailBox == null)
            {
                ebEmailBox = DBEmailBox.getEmailBoxByName(sID);
            }

            if (ebEmailBox != null)
            {
                ebReturn = ebEmailBox.createGenericEmailBox(bValidate);
            }

            commitTransaction(sTransactionID);
        }
        catch (Exception e)
        {
            BSF.abortTransaction(sTransactionID);

            if (e instanceof StorageProviderException)
            {
                throw (StorageProviderException) e;
            }

            throw new StorageProviderException(e,
                                               StorageProviderExceptionMessages.SPE_ERROR_GETTING_CONTAINER_WITH_ID,
                                               sID);
        }

        return ebReturn;
    }

    /**
     * This method gets the email message with the given ID.
     *
     * @param   sID  The ID of the email.
     *
     * @return  The EmailMessage found.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     *
     * @see     IStorageProviderQueryManager#getEmailMessage(String)
     */
    @Override public EmailMessage getEmailMessage(String sID)
                                           throws StorageProviderException
    {
        EmailMessage ebReturn = null;

        String sTransactionID = startTransaction();

        try
        {
            DBEmail eEmail = DBEmail.getEmailObject(sID);

            if (eEmail != null)
            {
                ebReturn = eEmail.createGenericEmailMessage();
            }

            commitTransaction(sTransactionID);
        }
        catch (Exception e)
        {
            BSF.abortTransaction(sTransactionID);

            if (e instanceof StorageProviderException)
            {
                throw (StorageProviderException) e;
            }

            throw new StorageProviderException(e,
                                               StorageProviderExceptionMessages.SPE_ERROR_GETTING_CONTAINER_WITH_ID,
                                               sID);
        }

        return ebReturn;
    }

    /**
     * This method returns all email messages for the given container.
     *
     * @param   sContextContainerID  The ID of the context container.
     *
     * @return  The emails in the context.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     *
     * @see     IStorageProviderQueryManager#getEmailMessagesByContextID(String)
     */
    @Override public List<EmailMessage> getEmailMessagesByContextID(String sContextContainerID)
                                                             throws StorageProviderException
    {
        return searchEmailMessages(sContextContainerID, "", "", "", null, null, null, null, null);
    }

    /**
     * @see  IStorageProviderQueryManager#getTriggerDefinition(String)
     */
    @Override public TriggerDefinition getTriggerDefinition(String sID)
                                                     throws StorageProviderException
    {
        TriggerDefinition tdReturn = null;

        String sTransactionID = startTransaction();

        try
        {
            DBTrigger tTrigger = DBTrigger.getTriggerDefinition(sID);

            if (tTrigger != null)
            {
                tdReturn = tTrigger.createGenericTriggerDefinition();
            }

            commitTransaction(sTransactionID);
        }
        catch (Exception e)
        {
            BSF.abortTransaction(sTransactionID);

            if (e instanceof StorageProviderException)
            {
                throw (StorageProviderException) e;
            }

            throw new StorageProviderException(e,
                                               StorageProviderExceptionMessages.SPE_ERROR_GETTING_TRIGGER_DEFINITION_WITH_ID_0,
                                               sID);
        }

        return tdReturn;
    }

    /**
     * This method gets whether or not compression should be used on the data.
     *
     * @return  Whether or not compression should be used on the data.
     */
    public boolean getUseCompression()
    {
        String useCompression = (String) getParameters().get(PARAM_COMPRESS_DATA);

        boolean returnValue = "true".equalsIgnoreCase(useCompression);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("The DBStorageProvider will " + (returnValue ? "" : "NOT ") +
                      " use compression");
        }

        return returnValue;
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
     * @see     IEmailStorageProvider#isTriggerPersistent(String)
     */
    @Override public boolean isTriggerPersistent(String sTriggerName)
    {
        boolean bReturn = false;

        registerContext();

        if (DBTrigger.getTriggerByName(sTriggerName) != null)
        {
            bReturn = true;
        }

        return bReturn;
    }

    /**
     * This method will remove the given context container and corresponding email messages.
     *
     * @param   ccContainer  The container to remove.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     *
     * @see     IStorageProviderQueryManager#removeContextContainer(ContextContainer)
     */
    @Override public void removeContextContainer(ContextContainer ccContainer)
                                          throws StorageProviderException
    {
        String sTransactionID = startTransaction();

        try
        {
            DMLStatement ds = new DMLStatement("DELETE FROM EMAIL WHERE CONTEXT_CONTAINER = :ContextContainerID");
            ds.addParameter("ContextContainerID", "EMAIL.CONTEXT_CONTAINER", ccContainer.getID());
            ds.execute();

            ds = new DMLStatement("DELETE FROM CONTEXT_CONTAINER WHERE CONTEXT_CONTAINER_ID = :ContextContainerID");
            ds.addParameter("ContextContainerID", "CONTEXT_CONTAINER.CONTEXT_CONTAINER_ID",
                            ccContainer.getID());
            ds.execute();

            BSF.commitTransaction(sTransactionID);
        }
        catch (Exception e)
        {
            BSF.abortTransaction(sTransactionID);

            if (e instanceof StorageProviderException)
            {
                throw (StorageProviderException) e;
            }

            throw new StorageProviderException(e,
                                               StorageProviderExceptionMessages.SPE_ERROR_REMOVING_CONTAINER_0_FROM_THE_DATABASE,
                                               ccContainer.getID());
        }
    }

    /**
     * This method searches for container that match the given criteria. The criteria are
     * interpreted as database 'like' filters.
     *
     * @param   sEmailBoxID            Holds the criteria for the email box ID.
     * @param   sStatusInformation     Holds the criteria for the status information.
     * @param   sTriggerDefinition     Holds the criteria for the trigger definition.
     * @param   sProcessingStatus      Holds the criteria for the processing status.
     * @param   dFromCreateDate        Holds the criteria for the from date (create date).
     * @param   dToCreateDate          Holds the criteria for the to date (create date).
     * @param   dFromCompleteDate      Holds the criteria for the from date (complete date).
     * @param   dToCompleteDate        Holds the criteria for the to date (complete date).
     * @param   dFromStatusChangeDate  Holds the criteria for the from date (status change date).
     * @param   dToStatusChangeDate    Holds the criteria for the to date (status change date).
     * @param   cCursor                Holds the current cursor.
     *
     * @return  The list of matching containers.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     *
     * @see     IStorageProviderQueryManager#searchContainers(String, String, String, String, Date,
     *          Date, Date, Date, Date, Date, Cursor)
     */
    @Override public List<ContextContainer> searchContainers(String sEmailBoxID,
                                                             String sStatusInformation,
                                                             String sTriggerDefinition,
                                                             String sProcessingStatus,
                                                             Date dFromCreateDate,
                                                             Date dToCreateDate,
                                                             Date dFromCompleteDate,
                                                             Date dToCompleteDate,
                                                             Date dFromStatusChangeDate,
                                                             Date dToStatusChangeDate,
                                                             Cursor cCursor)
                                                      throws StorageProviderException
    {
        ArrayList<ContextContainer> alReturn = new ArrayList<ContextContainer>();

        String sTransactionID = startTransaction();

        try
        {
            BusObjectIterator boi = DBContextContainer.searchContainers(sEmailBoxID,
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

            while (boi.hasMoreElements())
            {
                DBContextContainer cc = (DBContextContainer) boi.nextElement();

                alReturn.add(cc.createGenericContextContainer());
            }

            BSF.commitTransaction(sTransactionID);
        }
        catch (Exception e)
        {
            BSF.abortTransaction(sTransactionID);

            if (e instanceof StorageProviderException)
            {
                throw (StorageProviderException) e;
            }

            throw new StorageProviderException(e,
                                               StorageProviderExceptionMessages.SPE_ERROR_EXECUTING_METHOD_SEARCHCONTAINERS);
        }

        return alReturn;
    }

    /**
     * This method searches for email boxes that match the given criteria.
     *
     * @param   sName           Holds the criteria for the name of the email box.
     * @param   sHost           Holds the criteria for the name of the host (server).
     * @param   sType           Holds the criteria for the type (POP3 / IMAP ).
     * @param   sConfiguration  Holds the criteria for the configuration of the email box. This
     *                          criteria is interpreted as a 'like' operator.
     * @param   cCursor         Holds the current cursor.
     *
     * @return  The list of email boxes matching the given criteria.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     *
     * @see     IStorageProviderQueryManager#searchEmailBoxes(String, String, String, String,
     *          Cursor)
     */
    @Override public List<EmailBox> searchEmailBoxes(String sName, String sHost, String sType,
                                                     String sConfiguration, Cursor cCursor)
                                              throws StorageProviderException
    {
        ArrayList<EmailBox> alReturn = new ArrayList<EmailBox>();

        String sTransactionID = startTransaction();

        try
        {
            BusObjectIterator boi = DBEmailBox.searchEmailBoxes(sName, sHost, sType, sConfiguration,
                                                                cCursor);

            while (boi.hasMoreElements())
            {
                DBEmailBox cc = (DBEmailBox) boi.nextElement();

                alReturn.add(cc.createGenericEmailBox(false));
            }

            BSF.commitTransaction(sTransactionID);
        }
        catch (Exception e)
        {
            BSF.abortTransaction(sTransactionID);

            if (e instanceof StorageProviderException)
            {
                throw (StorageProviderException) e;
            }

            throw new StorageProviderException(e,
                                               StorageProviderExceptionMessages.SPE_ERROR_EXECUTING_METHOD_SEARCHEMAILBOXES);
        }

        return alReturn;
    }

    /**
     * This method searches for email messages that match the given criteria.
     *
     * @param   sContextContainerID  Holds the criteria for the subject.
     * @param   sFrom                Holds the criteria for the from. This criteria is interpreted
     *                               as a 'like' operator.
     * @param   sTo                  Holds the criteria for the to. This criteria is interpreted as
     *                               a 'like' operator.
     * @param   sSubject             Holds the criteria for the subject. This criteria is
     *                               interpreted as a 'like' operator.
     * @param   dFromSendDate        Holds the criteria for the from date (send date).
     * @param   dToSendDate          Holds the criteria for the to date (send date).
     * @param   dFromReceiveDate     Holds the criteria for the from date (receive date).
     * @param   dToReceiveDate       Holds the criteria for the to date (receive date).
     * @param   cCursor              Holds the current cursor.
     *
     * @return  The list of email messages matching the given criteria.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     *
     * @see     IStorageProviderQueryManager#searchEmailMessages(String, String, String, String,
     *          Date, Date, Date, Date, Cursor)
     */
    @Override public List<EmailMessage> searchEmailMessages(String sContextContainerID,
                                                            String sFrom, String sTo,
                                                            String sSubject, Date dFromSendDate,
                                                            Date dToSendDate, Date dFromReceiveDate,
                                                            Date dToReceiveDate,
                                                            Cursor cCursor)
                                                     throws StorageProviderException
    {
        ArrayList<EmailMessage> alReturn = new ArrayList<EmailMessage>();

        String sTransactionID = startTransaction();

        try
        {
            BusObjectIterator boi = DBEmail.searchEmailMessages(sContextContainerID, sFrom, sTo,
                                                                sSubject, dFromSendDate,
                                                                dToSendDate, dFromReceiveDate,
                                                                dToReceiveDate, cCursor);

            while (boi.hasMoreElements())
            {
                DBEmail cc = (DBEmail) boi.nextElement();

                alReturn.add(cc.createGenericEmailMessage());
            }

            BSF.commitTransaction(sTransactionID);
        }
        catch (Exception e)
        {
            BSF.abortTransaction(sTransactionID);

            if (e instanceof StorageProviderException)
            {
                throw (StorageProviderException) e;
            }

            throw new StorageProviderException(e,
                                               StorageProviderExceptionMessages.SPE_ERROR_EXECUTING_METHOD_SEARCHEMAILBOXES);
        }

        return alReturn;
    }

    /**
     * This method searches for email boxes that match the given criteria.
     *
     * @param   sName        Holds the criteria for the name of the trigger.
     * @param   sEmailBoxID  Holds the criteria for the ID or name of the email box.
     * @param   sDefinition  Holds the criteria for the configuration of the trigger. This criteria
     *                       is interpreted as a 'like' operator.
     * @param   cCursor      The cursor to use.
     *
     * @return  The list of email boxes matching the given criteria.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     *
     * @see     IStorageProviderQueryManager#searchTriggerDefinitions(String, String, String,
     *          Cursor)
     */
    @Override public List<TriggerDefinition> searchTriggerDefinitions(String sName,
                                                                      String sEmailBoxID,
                                                                      String sDefinition,
                                                                      Cursor cCursor)
                                                               throws StorageProviderException
    {
        ArrayList<TriggerDefinition> alReturn = new ArrayList<TriggerDefinition>();

        String sTransactionID = startTransaction();

        try
        {
            BusObjectIterator boi = DBTrigger.searchTriggerDefinitions(sName, sEmailBoxID,
                                                                       sDefinition, cCursor);

            while (boi.hasMoreElements())
            {
                DBTrigger cc = (DBTrigger) boi.nextElement();

                alReturn.add(cc.createGenericTriggerDefinition());
            }

            BSF.commitTransaction(sTransactionID);
        }
        catch (Exception e)
        {
            BSF.abortTransaction(sTransactionID);

            if (e instanceof StorageProviderException)
            {
                throw (StorageProviderException) e;
            }

            throw new StorageProviderException(e,
                                               StorageProviderExceptionMessages.SPE_ERROR_EXECUTING_METHOD_SEARCHTRIGGERDEFINITIONS);
        }

        return alReturn;
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
     * @see     IEmailStorageProvider#setContainerStatusActionError(RuleContextContainer, String)
     */
    @Override public void setContainerStatusActionError(RuleContextContainer rccContext,
                                                        String sStatusInfo)
                                                 throws StorageProviderException
    {
        updateStatus(rccContext, EProcessingStatus.ACTION_ERROR, sStatusInfo);
    }

    /**
     * This method sets the status of the given container to completed. This means that the messages
     * were delivered properly.
     *
     * @param   rccContext  The rule context container containing all messages.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     *
     * @see     IEmailStorageProvider#setContainerStatusCompleted(RuleContextContainer)
     */
    @Override public void setContainerStatusCompleted(RuleContextContainer rccContext)
                                               throws StorageProviderException
    {
        updateStatus(rccContext, EProcessingStatus.COMPLETED);
    }

    /**
     * This method sets the status of all messages to error. This means that either the actions
     * failed or that the actual trigger message failed.
     *
     * @param   rccContext         The rule context container containing all messages.
     * @param   sExceptionDetails  sStatusInfo The exception details for this error.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     *
     * @see     IEmailStorageProvider#setContainerStatusError(RuleContextContainer, String)
     */
    @Override public void setContainerStatusError(RuleContextContainer rccContext,
                                                  String sExceptionDetails)
                                           throws StorageProviderException
    {
        updateStatus(rccContext, EProcessingStatus.MESSAGE_ERROR, sExceptionDetails);
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
     * @see     IEmailStorageProvider#setContainerStatusInProgress(RuleContextContainer)
     */
    @Override public void setContainerStatusInProgress(RuleContextContainer rccContext)
                                                throws StorageProviderException
    {
        updateStatus(rccContext, EProcessingStatus.IN_PROGRESS);
    }

    /**
     * This method starts the transaction. It will return the GUID for this transaction.
     *
     * @return  The transaction ID.
     */
    public String startTransaction()
    {
        registerContext();

        String sReturn = Native.createGuid();
        BSF.startTransaction(sReturn);

        return sReturn;
    }

    /**
     * Serializes the trigger to a file.
     *
     * <p>The trigger will be serialized using it toXML method. The XML created by the trigger is
     * wrapped in the envelope defined by the constant SERIALIZATION_XML_ENVELOPE.</p>
     *
     * <p>This method must be overriden in order to actually persist triggers.</p>
     *
     * @param   tTrigger  The trigger to be persisted.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     *
     * @see     com.cordys.coe.ac.emailio.storage.AbstractStorageProvider#persistTrigger(com.cordys.coe.ac.emailio.config.trigger.ITrigger)
     */
    @Override protected void persistTrigger(ITrigger tTrigger)
                                     throws StorageProviderException
    {
        String sTransactionID = startTransaction();

        try
        {
            DBTrigger tDBVersion = DBTrigger.storeTrigger(tTrigger, getEmailBox());

            BSF.commitTransaction(sTransactionID);

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Stored trigger to the database with id " + tDBVersion.getID());
            }
        }
        catch (Exception e)
        {
            BSF.abortTransaction(sTransactionID);

            if (e instanceof StorageProviderException)
            {
                throw (StorageProviderException) e;
            }

            throw new StorageProviderException(e,
                                               StorageProviderExceptionMessages.SPE_ERROR_PERSISTING_TRIGGER_WITH_NAME_0,
                                               tTrigger.getName());
        }
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
     *
     * @see     AbstractStorageProvider#postInit(IEmailBox, int, XPathMetaInfo, IManagedComponent)
     */
    @Override protected void postInit(IEmailBox ebEmailBox, int iConfigurationNode,
                                      XPathMetaInfo xmi, IManagedComponent mcParent)
                               throws StorageProviderException
    {
        // Create the embedded WsApps context
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Initializing database storage");
        }

        // Create the DB connection pool
        BsfContext bcContext = createDBConnection(mcParent);

        // Now we need to reload all persisted dynamic triggers.
        // bcContext will be filled
        assert (bcContext != null);

        // Reload the dynamic triggers from the database.
        reloadDynamicTriggers(ebEmailBox, bcContext);

        // Make sure the compression is set correctly
        ContentUtil.setUseCompression(getUseCompression());
    }

    /**
     * This adapter method is called when the trigger needs to be removed from the actual
     * persistence layer.
     *
     * @param   sTriggerName  The name of the trigger.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     *
     * @see     AbstractStorageProvider#removeTriggerFromPersistence(String)
     */
    @Override protected void removeTriggerFromPersistence(String sTriggerName)
                                                   throws StorageProviderException
    {
        String sTransactionID = startTransaction();

        try
        {
            DBTrigger tTemp = DBTrigger.getTriggerByName(sTriggerName);

            if (tTemp != null)
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Removed the trigger with name " + sTriggerName +
                              " from the database");
                }
                tTemp.delete();
            }
            else if (LOG.isDebugEnabled())
            {
                LOG.debug("The trigger with name " + sTriggerName + " cannot be found.");
            }

            BSF.commitTransaction(sTransactionID);
        }
        catch (Exception e)
        {
            BSF.abortTransaction(sTransactionID);

            if (e instanceof StorageProviderException)
            {
                throw (StorageProviderException) e;
            }

            throw new StorageProviderException(e,
                                               StorageProviderExceptionMessages.SPE_ERROR_REMOVING_THE_TRIGGER_FROM_THE_PERSISTENCE_LAYER);
        }
    }

    /**
     * This method creates the database connection and initializes WS-AppServer.
     *
     * @param   mcParent  The parent managed component.
     *
     * @return  The BSF context for the current thread.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    private BsfContext createDBConnection(IManagedComponent mcParent)
                                   throws StorageProviderException
    {
        String sOrganization = getOrganizationFromSoapProcessorDN();

        // Get the parameter values.
        int iComponent = getXMLParameter("dbconfig");

        if (iComponent == 0)
        {
            throw new StorageProviderException(StorageProviderExceptionMessages.SPE_COULD_NOT_FIND_THE_DATABASE_CONNECTION_POOL_CONFIGURATION);
        }

        // Now build up the configuration for the embedded WsApps.
        int iWsAppsConfig = 0;
        BsfContext bcContext = null;

        try
        {
            WsAppUtil.createConfiguration(iComponent, sOrganization, mcParent);

            bcContext = BSF.initBsfContext();

            if (bcContext == null)
            {
                throw new StorageProviderException(StorageProviderExceptionMessages.SPE_COULD_NOT_CREATE_THE_PROPER_WSAPPSERVER_CONTEXT);
            }

            if (LOG.isDebugEnabled())
            {
                LOG.debug("BsfContext initialized with user: " + bcContext.getUser());
            }
        }
        catch (Exception e)
        {
            if (iWsAppsConfig != 0)
            {
                Node.delete(iWsAppsConfig);
            }

            if (e instanceof StorageProviderException)
            {
                StorageProviderException iece = (StorageProviderException) e;
                throw iece;
            }

            throw new StorageProviderException(e,
                                               StorageProviderExceptionMessages.SPE_ERROR_CREATING_DATABASE_CONFIGURATION_FOR_EMBEDDED_WSAPPSERVER);
        }
        return bcContext;
    }

    /**
     * This method returns the DN of the organization. The base is the DN of the SOAP processor.
     *
     * @return  The DN of the organization.
     */
    private String getOrganizationFromSoapProcessorDN()
    {
        DN dn = DN.getDN(getSoapProcessorDN());

        return dn.getParent().getParent().getParent().toString();
    }

    /**
     * This method makes sure the proper context is registered of the current thread.
     */
    private void registerContext()
    {
        try
        {
            BSF.getMyContext();
        }
        catch (BsfNoContextException bnce)
        {
            // Register the context for the current thread.
            BSF.initBsfContext();
        }
    }

    /**
     * This method will reload the dynamic triggers from the database for the given EMail box.
     *
     * @param   ebEmailBox  The current email box for which this storage is being initialized.
     * @param   bcContext   The BSF context.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    private void reloadDynamicTriggers(IEmailBox ebEmailBox, BsfContext bcContext)
                                throws StorageProviderException
    {
        // We'll reuse the context document for parsing the XML.
        Document dDoc = bcContext.getObjectManager()._getXMLDocument();

        if (ebEmailBox != null)
        {
            String sTransactionID = startTransaction();

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Going to reload the persisted dynamic triggers from the database.");
            }

            try
            {
                // Make sure you use the ebEmailBox.getName() and not just the ebEmailBox.
                // If you do, it will try to persist the GlobalEmailBox (which is not needed.
                DBEmailBox dbEmailBox = DBEmailBox.getEmailBoxByName(ebEmailBox.getName());

                if (dbEmailBox != null)
                {
                    BusObjectIterator boi = DBTrigger.getTriggersForEmailbox(dbEmailBox.getID());

                    while (boi.hasMoreElements())
                    {
                        DBTrigger dbTrigger = (DBTrigger) boi.nextElement();

                        // Now create the ITrigger from the definition.
                        String sDefinition = dbTrigger.getContent();

                        int iTrigger = 0;

                        try
                        {
                            iTrigger = dDoc.parseString(sDefinition);

                            ITrigger tTrigger = TriggerFactory.createTrigger(iTrigger, ebEmailBox);

                            if (LOG.isDebugEnabled())
                            {
                                LOG.debug("Reloaded trigger: " + tTrigger.getName());
                            }

                            // It is important that the 'false' is passed on here. Otherwise every
                            // restart the trigger will be duplicated. We need to call our own
                            // addTrigger, and not ebEmailBox.addTrigger because the local member is
                            // not yet initialized.
                            addTrigger(tTrigger, false);

                            if (LOG.isDebugEnabled())
                            {
                                LOG.debug("Reloaded trigger " + tTrigger.getName() + "/" +
                                          tTrigger.getDescription());
                            }
                        }
                        catch (Exception e)
                        {
                            LOG.error(e, LogMessages.ERROR_LOADING_PERSISTENT_TRIGGER,
                                      dbTrigger.getName());
                        }
                        finally
                        {
                            if (iTrigger != 0)
                            {
                                Node.delete(iTrigger);
                            }
                        }
                    }
                }
                BSF.commitTransaction(sTransactionID);
            }
            catch (Exception e)
            {
                BSF.abortTransaction(sTransactionID);

                if (e instanceof StorageProviderException)
                {
                    throw (StorageProviderException) e;
                }

                throw new StorageProviderException(e,
                                                   StorageProviderExceptionMessages.SPE_ERROR_RELOADING_DYNAMIC_TRIGGERS_FROM_THE_DATABASE);
            }
        }
    }

    /**
     * This method updates the status of a context to the given status.
     *
     * @param   rccContext  The current context.
     * @param   psStatus    The new status.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    private void updateStatus(RuleContextContainer rccContext, EProcessingStatus psStatus)
                       throws StorageProviderException
    {
        updateStatus(rccContext, psStatus, null);
    }

    /**
     * This method updates the status of a context to the given status.
     *
     * @param   rccContext   The current context.
     * @param   psStatus     The new status.
     * @param   sStatusInfo  Addition information for the current state.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    private void updateStatus(RuleContextContainer rccContext, EProcessingStatus psStatus,
                              String sStatusInfo)
                       throws StorageProviderException
    {
        String sTransactionID = startTransaction();

        try
        {
            DBContextContainer cTemp = DBContextContainer.getContextContainerObject(rccContext
                                                                                    .getStorageID());
            cTemp.setProcessingStatus(psStatus.name());

            if ((sStatusInfo != null) && (sStatusInfo.length() > 0))
            {
                cTemp.setStatusInformation(sStatusInfo);
            }
            cTemp.update();

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Updated the status for context " + rccContext.getStorageID() + " to " +
                          psStatus);
            }

            BSF.commitTransaction(sTransactionID);
            BSF.clearObjectRegistration();
        }
        catch (Exception e)
        {
            BSF.abortTransaction(sTransactionID);

            if (e instanceof StorageProviderException)
            {
                throw (StorageProviderException) e;
            }

            throw new StorageProviderException(e,
                                               StorageProviderExceptionMessages.SPE_ERROR_UPDATING_THE_STATUS_OF_CONTAINER_TO_STATUS,
                                               rccContext.getStorageID(), psStatus.toString());
        }
    }
}
