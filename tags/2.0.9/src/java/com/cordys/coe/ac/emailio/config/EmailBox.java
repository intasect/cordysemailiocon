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
 package com.cordys.coe.ac.emailio.config;

import com.cordys.coe.ac.emailio.config.action.ActionFactory;
import com.cordys.coe.ac.emailio.config.action.IAction;
import com.cordys.coe.ac.emailio.config.trigger.ITrigger;
import com.cordys.coe.ac.emailio.config.trigger.TriggerFactory;
import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;
import com.cordys.coe.ac.emailio.exception.StorageProviderException;
import com.cordys.coe.ac.emailio.localization.EmailIOConfigurationExceptionMessages;
import com.cordys.coe.ac.emailio.storage.EmailStorageProviderFactory;
import com.cordys.coe.ac.emailio.storage.IEmailStorageProvider;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.system.Native;

import com.eibus.xml.nom.Node;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class wraps the configuration of an email box.
 *
 * @author  pgussow
 */
public abstract class EmailBox extends MailServer
    implements IEmailBox
{
    /**
     * Holds the name of the tag 'triggers'.
     */
    private static final String TAG_TRIGGERS = "triggers";
    /**
     * Holds the name of the tag 'folder'.
     */
    private static final String TAG_FOLDER = "folder";
    /**
     * Holds the name of the tag 'folders'.
     */
    private static final String TAG_FOLDERS = "folders";
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(EmailBox.class);
    /**
     * Holds the name of the tag 'type'.
     */
    private static final String TAG_TYPE = "type";
    /**
     * Holds the name of the tag 'pollinterval'.
     */
    private static final String TAG_POLLINTERVAL = "pollinterval";
    /**
     * Holds the default poll interval.
     */
    private static final int DEFAULT_POLL_INTERVAL = 5000;
    /**
     * Holds an empty string.
     */
    private static final String EMPTY_STRING = "";
    /**
     * Holds the name of the tag 'actions'.
     */
    private static final String TAG_ACTIONS = "actions";
    /**
     * Holds the storage provider for this email box.
     */
    private IEmailStorageProvider m_espStorage;
    /**
     * Holds the list of actions defined for this mailbox.
     */
    private Map<String, IAction> m_hmActions = Collections.synchronizedMap(new LinkedHashMap<String, IAction>());
    /**
     * Holds the poll interval to use.
     */
    private int m_iPollInterval;
    /**
     * Holds the configuration of the storage provider that should be used.
     */
    private int m_iStorageProviderConfiguration;
    /**
     * Holds the type of email box.
     */
    private EEmailBoxType m_sType;

    /**
     * Creates a new EmailBox object.
     *
     * @param   iNode                       The configuration node.
     * @param   iGlobalStorage              The global storage configuration to use.
     * @param   sSoapProcessorDN            The DN of the SOAP processor in which the storage
     *                                      provider is running.
     * @param   bInitializeStorageProvider  Indicates whether or not the storage provider should be
     *                                      created.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    public EmailBox(int iNode, int iGlobalStorage, String sSoapProcessorDN,
                    boolean bInitializeStorageProvider)
             throws EmailIOConfigurationException
    {
        super(iNode);

        m_iPollInterval = XPathHelper.getIntegerValue(iNode, "./ns:" + TAG_POLLINTERVAL + "/text()",
                                                      getXMI(), DEFAULT_POLL_INTERVAL);

        String sBoxType = XPathHelper.getStringValue(iNode, "./ns:" + TAG_TYPE + "/text()",
                                                     getXMI(), EEmailBoxType.POP3.name());

        try
        {
            m_sType = EEmailBoxType.valueOf(sBoxType);
        }
        catch (IllegalArgumentException iae)
        {
            throw new EmailIOConfigurationException(iae,
                                                    EmailIOConfigurationExceptionMessages.EICE_INVALID_MAILBOX_TYPE,
                                                    sBoxType);
        }

        if (getUsername().length() == 0)
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_MISSING_USER_NAME);
        }

        // Decode the password.
        String sPassword = getPassword();

        if (sPassword.length() > 0)
        {
            byte[] baTemp = sPassword.getBytes();

            byte[] baDecoded = Native.decodeBinBase64(baTemp, baTemp.length);

            if (baDecoded != null)
            {
                sPassword = new String(baDecoded);
            }
        }

        // create the trigger store.
        int iStorage = XPathHelper.selectSingleNode(iNode, "./ns:storage", getXMI());

        if (iStorage == 0)
        {
            iStorage = iGlobalStorage;
        }
        m_iStorageProviderConfiguration = iStorage;

        try
        {
            m_espStorage = EmailStorageProviderFactory.createStorageProvider(this, sSoapProcessorDN,
                                                                             bInitializeStorageProvider,
                                                                             getManagedComponent());
        }
        catch (StorageProviderException e)
        {
            throw new EmailIOConfigurationException(e,
                                                    EmailIOConfigurationExceptionMessages.EICE_ERROR_CREATING_STORAGE_PROVIDER);
        }

        // Now parse the trigger.
        int[] aiTriggers = XPathHelper.selectNodes(iNode, "./ns:triggers/ns:trigger", getXMI());

        if (aiTriggers.length == 0)
        {
            LOG.warn(null,
                     EmailIOConfigurationExceptionMessages.EICE_NO_TRIGGERS_CONFIGURED_FOR_THIS_EMAIL_BOX);
        }

        // Now get all the action definitions Note: this has to be done BEFORE
        // parsing the triggers,
        // because the triggers might need the actions defined here.
        int[] aiActions = XPathHelper.selectNodes(iNode, "./ns:actions/ns:action", getXMI());

        for (int iCount = 0; iCount < aiActions.length; iCount++)
        {
            IAction aAction = ActionFactory.createAction(aiActions[iCount]);
            m_hmActions.put(aAction.getID(), aAction);
        }

        for (int iCount = 0; iCount < aiTriggers.length; iCount++)
        {
            ITrigger tTrigger = TriggerFactory.createTrigger(aiTriggers[iCount], this);

            try
            {
                addTrigger(tTrigger, false);
            }
            catch (StorageProviderException ex)
            {
                throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.ERROR_WHEN_ADDING_TRIGGERS,
                                                        ex.getMessage());
            }
        }
    }

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
     *
     * @see     com.cordys.coe.ac.emailio.config.IEmailBox#addTrigger(com.cordys.coe.ac.emailio.config.trigger.ITrigger)
     */
    public void addTrigger(ITrigger tTrigger)
                    throws StorageProviderException
    {
        addTrigger(tTrigger, true);
    }

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
    public void addTrigger(ITrigger tTrigger, boolean bIsPersistent)
                    throws StorageProviderException
    {
        m_espStorage.addTrigger(tTrigger, bIsPersistent);
    }

    /**
     * This method gets the action with the given ID.
     *
     * @param   sActionID  The ID of the action.
     *
     * @return  The action with the given ID.
     *
     * @see     com.cordys.coe.ac.emailio.config.IEmailBox#getAction(java.lang.String)
     */
    public IAction getAction(String sActionID)
    {
        return m_hmActions.get(sActionID);
    }

    /**
     * This method gets the poll interval for this mailbox.
     *
     * @return  The poll interval for this mailbox.
     *
     * @see     com.cordys.coe.ac.emailio.config.IEmailBox#getPollInterval()
     */
    public int getPollInterval()
    {
        return m_iPollInterval;
    }

    /**
     * This method gets the storage provider for this email box.
     *
     * @return  The storage provider for this email box.
     *
     * @see     com.cordys.coe.ac.emailio.config.IEmailBox#getStorageProvider()
     */
    @Override public IEmailStorageProvider getStorageProvider()
    {
        return m_espStorage;
    }

    /**
     * This method returns the configuration that should be used for the storage provider. Each
     * email box can use their own storage provider. But it's also possible to define a storage
     * provider on connector level.
     *
     * @return  The storage provider configuration that should be used.
     *
     * @see     com.cordys.coe.ac.emailio.config.IEmailBox#getStorageProviderConfiguration()
     */
    @Override public int getStorageProviderConfiguration()
    {
        return m_iStorageProviderConfiguration;
    }

    /**
     * This method returns all the triggers that should be applied to the given email folder.
     *
     * <p>The trigger list obeys the priority set for a certain trigger. The lower the number, the
     * higher the trigger will be in the list. If 2 triggers have the same priority the order is NOT
     * guaranteed.</p>
     *
     * @param   sFolderName  The name of the folder.
     *
     * @return  The list of triggers that should be applied to the given folder.
     *
     * @see     com.cordys.coe.ac.emailio.config.IEmailBox#getTriggers(java.lang.String)
     */
    public ITrigger[] getTriggers(String sFolderName)
    {
        return m_espStorage.getTriggers(sFolderName).toArray(new ITrigger[0]);
    }

    /**
     * This method gets the type of email box (IMAP/POP3).
     *
     * @return  The type of email box (IMAP/POP3).
     *
     * @see     com.cordys.coe.ac.emailio.config.IEmailBox#getType()
     */
    public EEmailBoxType getType()
    {
        return m_sType;
    }

    /**
     * This method removes the trigger passed to it.
     *
     * @param   tTrigger  The trigger to remove.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     *
     * @see     IEmailBox#removeTrigger(ITrigger)
     */
    public void removeTrigger(ITrigger tTrigger)
                       throws StorageProviderException
    {
        if (tTrigger == null)
        {
            return;
        }

        removeTrigger(tTrigger.getName());
    }

    /**
     * This method removes the trigger with the given name.
     *
     * @param   sTriggerName  The trigger to remove.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     *
     * @see     com.cordys.coe.ac.emailio.config.IEmailBox#removeTrigger(com.cordys.coe.ac.emailio.config.trigger.ITrigger)
     */
    public void removeTrigger(String sTriggerName)
                       throws StorageProviderException
    {
        m_espStorage.removeTrigger(sTriggerName);
    }

    /**
     * This method sets the poll interval for this mailbox.
     *
     * @param  iPollInterval  The poll interval for this mailbox.
     */
    public void setPollInterval(int iPollInterval)
    {
        m_iPollInterval = iPollInterval;
    }

    /**
     * This method sets the type of email box (IMAP/POP3).
     *
     * @param  sType  The type of email box (IMAP/POP3).
     */
    public void setType(EEmailBoxType sType)
    {
        m_sType = sType;
    }

    /**
     * This method returns a string representation of the object.
     *
     * @return  A string representation of the object.
     *
     * @see     java.lang.Object#toString()
     */
    @Override public String toString()
    {
        StringBuilder sbReturn = new StringBuilder();

        sbReturn.append("Email box " + getName());
        sbReturn.append("\nHost: " + getHost());
        sbReturn.append("\nPort: " + getPort());
        sbReturn.append("\nUsername: " + getUsername());
        sbReturn.append("\nPassword: *******");
        sbReturn.append("\nType: " + getType());
        sbReturn.append("\nPoll interval: " + getPollInterval());
        sbReturn.append("\nTriggers:");
        sbReturn.append("\n---------");

        for (ITrigger tTrigger : m_espStorage.getTriggers())
        {
            sbReturn.append(tTrigger.toString());
            sbReturn.append("\n");
        }

        if (m_hmActions.size() > 0)
        {
            sbReturn.append("\nActions:");
            sbReturn.append("\n---------");

            for (IAction aAction : m_hmActions.values())
            {
                sbReturn.append(aAction.toString());
                sbReturn.append("\n");
            }
        }

        return sbReturn.toString();
    }

    /**
     * This method dumps the configuration of this email box to XML.
     *
     * @param   iParent  The parent element.
     *
     * @return  The XML node that was created.
     *
     * @see     com.cordys.coe.ac.emailio.config.IEmailBox#toXML(int)
     */
    @Override public int toXML(int iParent)
    {
        int iReturn = super.toXML(iParent, "emailbox");

        Node.createElementWithParentNS(TAG_TYPE, getType().toString(), iReturn);
        Node.createElementWithParentNS(TAG_POLLINTERVAL, String.valueOf(getPollInterval()),
                                       iReturn);

        String[] asFolders = getEmailFolders();

        if ((asFolders != null) && (asFolders.length > 0))
        {
            int iFolders = Node.createElementWithParentNS(TAG_FOLDERS, EMPTY_STRING, iReturn);

            for (int iCount = 0; iCount < asFolders.length; iCount++)
            {
                Node.createElementWithParentNS(TAG_FOLDER, asFolders[iCount], iFolders);
            }
        }

        int iActions = Node.createElementWithParentNS(TAG_ACTIONS, EMPTY_STRING, iReturn);

        for (IAction aAction : m_hmActions.values())
        {
            aAction.toXML(iActions);
        }

        // Dump the trigger store configuration
        if (m_espStorage != null)
        {
            m_espStorage.toXML(iReturn);
        }

        int iTriggers = Node.createElementWithParentNS(TAG_TRIGGERS, EMPTY_STRING, iReturn);

        for (ITrigger tTrigger : m_espStorage.getTriggers())
        {
            tTrigger.toXML(iTriggers);
        }

        return iReturn;
    }

    /**
     * This method is called to validate the configuration. This method is used to i.e. check if the
     * email server is reachable or that the messages configured are actually sendable.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     *
     * @see     com.cordys.coe.ac.emailio.config.IEmailBox#validate()
     */
    @Override public void validate()
                            throws EmailIOConfigurationException
    {
        Collection<ITrigger> cTriggers = m_espStorage.getTriggers();

        for (ITrigger tTrigger : cTriggers)
        {
            tTrigger.validate();
        }

        for (IAction aAction : m_hmActions.values())
        {
            aAction.validate();
        }
    }
}
