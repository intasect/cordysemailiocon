

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
package com.cordys.coe.ac.emailio.objects;

import com.cordys.coe.ac.emailio.exception.StorageProviderException;

import com.cordys.cpc.bsf.query.Cursor;

import java.util.Date;
import java.util.List;

/**
 * This interface describes all methods that a persistence layer should implement in order to be
 * able to support the management UIs.
 *
 * @author  pgussow
 */
public interface IStorageProviderQueryManager
{
    /**
     * This method gets the container with the given ID.
     *
     * @param   sID  The ID of the container.
     *
     * @return  The container with the given ID.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    ContextContainer getContextContainer(String sID)
                                  throws StorageProviderException;

    /**
     * This method gets the email box with the given ID.
     *
     * @param   sID  The ID (or name) of the emailbox.
     *
     * @return  The EmailBox found.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    EmailBox getEmailBox(String sID)
                  throws StorageProviderException;

    /**
     * This method gets the email box with the given ID.
     *
     * @param   sID        The ID (or name) of the emailbox.
     * @param   bValidate  Whether or not the configuration should be validated.
     *
     * @return  The EmailBox found.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    EmailBox getEmailBox(String sID, boolean bValidate)
                  throws StorageProviderException;

    /**
     * This method gets the email message with the given ID.
     *
     * @param   sID  The ID of the email.
     *
     * @return  The EmailMessage found.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    EmailMessage getEmailMessage(String sID)
                          throws StorageProviderException;

    /**
     * This method returns all email messages for the given container.
     *
     * @param   sContextContainerID  The ID of the context container.
     *
     * @return  The list of emails within the current cotnext.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    List<EmailMessage> getEmailMessagesByContextID(String sContextContainerID)
                                            throws StorageProviderException;

    /**
     * This method gets the trigger definition with the given ID.
     *
     * @param   sID  The ID of the trigger.
     *
     * @return  The Trigger found.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    TriggerDefinition getTriggerDefinition(String sID)
                                    throws StorageProviderException;

    /**
     * This method will remove the given context container and corresponding email messages.
     *
     * @param   ccContainer  The container to remove.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    void removeContextContainer(ContextContainer ccContainer)
                         throws StorageProviderException;

    /**
     * This method searches for container that match the given criteria.
     *
     * @param   sEmailBoxID            Holds the criteria for the email box ID.
     * @param   sStatusInformation     Holds the criteria for the status information. This criteria
     *                                 is interpreted as a 'like' operator.
     * @param   sTriggerDefinition     Holds the criteria for the trigger definition. This criteria
     *                                 is interpreted as a 'like' operator.
     * @param   sProcessingStatus      Holds the criteria for the processing status.
     * @param   dFromDate              Holds the criteria for the from date (create date).
     * @param   dToDate                Holds the criteria for the to date (create date).
     * @param   dFromCompleteDate      Holds the criteria for the from date (complete date).
     * @param   dToCompleteDate        Holds the criteria for the to date (complete date).
     * @param   dFromStatusChangeDate  Holds the criteria for the from date (status change date).
     * @param   dToStatusChangeDate    Holds the criteria for the to date (status change date).
     * @param   cCursor                Holds the current cursor.
     *
     * @return  The list of matching containers.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    List<ContextContainer> searchContainers(String sEmailBoxID, String sStatusInformation,
                                            String sTriggerDefinition, String sProcessingStatus,
                                            Date dFromDate, Date dToDate, Date dFromCompleteDate,
                                            Date dToCompleteDate, Date dFromStatusChangeDate,
                                            Date dToStatusChangeDate, Cursor cCursor)
                                     throws StorageProviderException;

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
     */
    List<EmailBox> searchEmailBoxes(String sName, String sHost, String sType, String sConfiguration,
                                    Cursor cCursor)
                             throws StorageProviderException;

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
     */
    List<EmailMessage> searchEmailMessages(String sContextContainerID, String sFrom, String sTo,
                                           String sSubject, Date dFromSendDate, Date dToSendDate,
                                           Date dFromReceiveDate, Date dToReceiveDate,
                                           Cursor cCursor)
                                    throws StorageProviderException;

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
     */
    List<TriggerDefinition> searchTriggerDefinitions(String sName, String sEmailBoxID,
                                                     String sDefinition, Cursor cCursor)
                                              throws StorageProviderException;
}
