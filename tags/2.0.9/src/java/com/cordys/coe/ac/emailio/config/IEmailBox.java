

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
package com.cordys.coe.ac.emailio.config;

import com.cordys.coe.ac.emailio.config.action.IActionStore;
import com.cordys.coe.ac.emailio.config.trigger.ITrigger;
import com.cordys.coe.ac.emailio.exception.StorageProviderException;
import com.cordys.coe.ac.emailio.storage.IEmailStorageProvider;

/**
 * Interface that defines the email box.
 *
 * @author  pgussow
 */
public interface IEmailBox extends IActionStore, IMailServer
{
    /**
     * Holds the name of the global Email Box which should not be persisted.
     */
    String GLOBAL_EMAIL_BOX = "GlobalEmailBox";

    /**
     * This method adds a new trigger to this emailbox.
     *
     * <p>It is automatically assumed that the trigger should be inserted into the persistent data
     * storage if one is used. This means that this method should only be used for user-defined
     * triggers, not for the ones that come from the configuration as these may not be externally
     * persisted.</p>
     *
     * @param   tTrigger  The trigger to add.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    void addTrigger(ITrigger tTrigger)
             throws StorageProviderException;

    /**
     * Adds a trigger to the mailbox, optionally putting it into persistent storage.
     *
     * <p>If the boolean argument is set to <code>False</code>, the trigger will not be written to
     * persistent storage which means that it will be lost as soon as the connector is restarted.
     * This is required for the triggers that are defined in the global configuration as they would
     * be duplicated if they were written to and restored from some external storage mechanism.
     * User-defined triggers should always be created as persistent triggers unless there is a very
     * good reason to keep them volatile.</p>
     *
     * @param   tTrigger       The trigger that should be added.
     * @param   bIsPersistent  <code>True</code> if the trigger should be externally persisted,
     *                         <code>False</code>
     *
     * @throws  StorageProviderException  In case of any exceptions.
     *
     * @see     #addTrigger(ITrigger)
     */
    void addTrigger(ITrigger tTrigger, boolean bIsPersistent)
             throws StorageProviderException;

    /**
     * This method gets the folders in which to poll for email messages.
     *
     * @return  The folders in which to poll for email messages.
     */
    String[] getEmailFolders();

    /**
     * This method gets the poll interval for this mailbox.
     *
     * @return  The poll interval for this mailbox.
     */
    int getPollInterval();

    /**
     * This method gets the storage provider for this email box.
     *
     * @return  The storage provider for this email box.
     */
    IEmailStorageProvider getStorageProvider();

    /**
     * This method returns the configuration that should be used for the storage provider. Each
     * email box can use their own storage provider. But it's also possible to define a storage
     * provider on connector level.
     *
     * <p>Note: if there is a global storage provider configuration is will be instantiated <b>for
     * each</b> mailbox. So it is not a singleton.</p>
     *
     * @return  The storage provider configuration that should be used.
     */
    int getStorageProviderConfiguration();

    /**
     * This method returns all the triggers that should be applied to the given email folder.
     *
     * @param   sFolderName  The name of the folder.
     *
     * @return  The list of triggers that should be applied to the given folder.
     */
    ITrigger[] getTriggers(String sFolderName);

    /**
     * This method gets the type of email box (IMAP/POP3).
     *
     * @return  The type of email box (IMAP/POP3).
     */
    EEmailBoxType getType();

    /**
     * This method removes the specified trigger.
     *
     * @param   tTrigger  The trigger to remove.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    void removeTrigger(ITrigger tTrigger)
                throws StorageProviderException;

    /**
     * This method removes the trigger with the specified name.
     *
     * @param   sTriggername  The name of the trigger to remove.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    void removeTrigger(String sTriggername)
                throws StorageProviderException;
}
