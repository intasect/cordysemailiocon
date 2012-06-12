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
package com.cordys.coe.ac.emailio.connection;

import com.cordys.coe.ac.emailio.config.IEmailBox;
import com.cordys.coe.ac.emailio.exception.EmailConnectionException;
import com.cordys.coe.ac.emailio.localization.EmailConnectionExceptionMessages;
import com.cordys.coe.ac.emailio.localization.LogMessages;
import com.cordys.coe.ac.emailio.util.MailMessageUtil;

import com.eibus.util.logger.CordysLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.MethodNotSupportedException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.StoreClosedException;

/**
 * This class holds the base functionality for the email connections.
 *
 * @author  pgussow
 */
abstract class BaseEmailConnection
    implements IEmailConnection
{
    /**
     * Holds the logger that is used.
     */
    protected final CordysLogger LOG = CordysLogger.getCordysLogger(this.getClass());
    /**
     * This method holds the names of the folders that have to be monitored.
     */
    private ArrayList<String> m_alMonitorFolders = new ArrayList<String>();
    /**
     * Whether or not to reconnect for each and every action to the email server. When set to true it will disconnect
     * and reconnect before every getEmailHeader() call.
     */
    private boolean m_bAlwaysReconnect = false;
    /**
     * Holds the email box configuration.
     */
    private IEmailBox m_ebBox;
    /**
     * This holds the list of folders that should be monitored.
     */
    private HashMap<String, Folder> m_hmOpenFolder = new LinkedHashMap<String, Folder>();
    /**
     * Holds the session object that is used.
     */
    private Session m_sSession;
    /**
     * Holds the store class for the connection.
     */
    private Store m_sStore;
    /**
     * Holds the store type to use.
     */
    private String m_sStoreType;

    /**
     * Creates a new BaseEmailConnection object.
     *
     * @param   sStoreType  The base store type.
     * @param   ebBox       The email box definition for this connection
     *
     * @throws  EmailConnectionException  In case of any exceptions.
     */
    public BaseEmailConnection(String sStoreType, IEmailBox ebBox)
                        throws EmailConnectionException
    {
        m_sStoreType = sStoreType;
        m_ebBox = ebBox;

        if (ebBox == null)
        {
            throw new EmailConnectionException(EmailConnectionExceptionMessages.ECE_NO_EMAIL_BOX_DEFINITION_FOUND);
        }

        if ((m_sStoreType == null) || (m_sStoreType.length() == 0))
        {
            throw new EmailConnectionException(EmailConnectionExceptionMessages.ECE_NO_STORE_TYPE_FOUND);
        }

        // Create the session object.
        createSession();

        // Add the folders.
        String[] asFolders = ebBox.getEmailFolders();

        for (int iCount = 0; iCount < asFolders.length; iCount++)
        {
            addFolder(asFolders[iCount]);
        }

        // This reconnect is basically a dummy connect. It is just used to validate the
        // actual configuration on the server. In case of 'no reconnect after each get'
        // it has to do this reconnect.
        reconnect();
    }

    /**
     * This method closes the current connection. NOTE: You MUST call this method when you're done. If you don't the
     * messages at the server won't get updated.
     *
     * @throws  EmailConnectionException  In case of any exceptions.
     *
     * @see     com.cordys.coe.ac.emailio.connection.IEmailConnection#close()
     */
    public void close()
               throws EmailConnectionException
    {
        closeCurrentConnection();
    }

    /**
     * This method makes sure that all messages marked for deletion are really deleted.
     *
     * @throws  EmailConnectionException  In case of any exceptions.
     *
     * @see     com.cordys.coe.ac.emailio.connection.IEmailConnection#expunge()
     */
    public void expunge()
                 throws EmailConnectionException
    {
        if (m_hmOpenFolder.size() > 0)
        {
            for (Folder fFolder : m_hmOpenFolder.values())
            {
                try
                {
                    if (fFolder.isOpen())
                    {
                        fFolder.expunge();
                    }
                }
                catch (MessagingException e)
                {
                    throw new EmailConnectionException(e, EmailConnectionExceptionMessages.ECE_ERROR_CLOSING_THE_FOLDER,
                                                       fFolder.getFullName());
                }
            }
        }
    }

    /**
     * This method gets whether or not to reconnect for each and every action to the email server. When set to true it
     * will disconnect and reconnect before every getEmailHeader() call.
     *
     * @return  Whether or not to reconnect for each and every action to the email server. When set to true it will
     *          disconnect and reconnect before every getEmailHeader() call.
     */
    public boolean getAlwaysReconnect()
    {
        return m_bAlwaysReconnect;
    }

    /**
     * This method gets the configuration of the email box..
     *
     * @return  The configuration of the email box..
     */
    public IEmailBox getEmailBox()
    {
        return m_ebBox;
    }

    /**
     * This method gets the headers of all messages that are in the inbox folder.
     *
     * @return  The headers of all messages that are in the inbox folder.
     *
     * @throws  EmailConnectionException  In case of any exceptions.
     *
     * @see     com.cordys.coe.ac.emailio.connection.IEmailConnection#getEmailHeaders()
     */
    public Message[] getEmailHeaders()
                              throws EmailConnectionException
    {
        if (m_alMonitorFolders.size() == 0)
        {
            throw new EmailConnectionException(EmailConnectionExceptionMessages.ECE_NO_FOLDERS_HAVE_BEEN_REGISTERED);
        }

        return getEmailHeaders(m_alMonitorFolders.get(0));
    }

    /**
     * This method gets the headers of all messages that are in the specified folder.
     *
     * @param   sFolderName  The name of the folder to get the messages for.
     *
     * @return  The headers of all messages that are in the inbox folder.
     *
     * @throws  EmailConnectionException  In case of any exceptions.
     *
     * @see     com.cordys.coe.ac.emailio.connection.IEmailConnection#getEmailHeaders(java.lang.String)
     */
    public Message[] getEmailHeaders(String sFolderName)
                              throws EmailConnectionException
    {
        if (!m_alMonitorFolders.contains(sFolderName))
        {
            throw new EmailConnectionException(EmailConnectionExceptionMessages.ECE_THERE_IS_NO_FOLDER_REGISTERED_WITH_NAME,
                                               sFolderName);
        }

        if (getAlwaysReconnect())
        {
            reconnect();
        }

        try
        {
            Folder fFolder = m_hmOpenFolder.get(sFolderName);

            if (fFolder == null)
            {
                throw new EmailConnectionException(EmailConnectionExceptionMessages.ECE_THE_FOLDER_REGISTERED_WITH_NAME_HAS_NOT_BEEN_OPENED,
                                                   sFolderName);
            }

            try
            {
                if (!fFolder.isOpen())
                {
                    fFolder.open(Folder.READ_WRITE);
                }
            }
            catch (StoreClosedException sce)
            {
                // The store appears to be closed. We'll try to reconnect.
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("The store is closed. Going to reconnect", sce);
                }

                try
                {
                    reconnect();
                    fFolder = m_hmOpenFolder.get(sFolderName);
                }
                catch (Exception e)
                {
                    LOG.error(e, LogMessages.ERROR_RECONNECTING_FOLDER, sFolderName);
                }
            }

            return fFolder.getMessages();
        }
        catch (MessagingException e)
        {
            throw new EmailConnectionException(e,
                                               EmailConnectionExceptionMessages.ECE_ERROR_GETTING_THE_MESSAGES_FOR_THE_FOLDER,
                                               sFolderName);
        }
    }

    /**
     * This method gets the open folders for this connection.
     *
     * @return  The open folders for this connection.
     */
    public HashMap<String, Folder> getOpenFolders()
    {
        return m_hmOpenFolder;
    }

    /**
     * This method gets the store type (imap or pop3).
     *
     * @return  The store type (imap or pop3).
     */
    public String getStoreType()
    {
        return m_sStoreType;
    }

    /**
     * This method removes the given message from the given folder.
     *
     * @param   sFolderName  The name of the folder.
     * @param   mMessage     The message to remove.
     *
     * @throws  EmailConnectionException  In case of any exceptions.
     *
     * @see     com.cordys.coe.ac.emailio.connection.IEmailConnection#removeMessage(java.lang.String,
     *          javax.mail.Message)
     */
    public void removeMessage(String sFolderName, Message mMessage)
                       throws EmailConnectionException
    {
        try
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Removing email message: " + MailMessageUtil.dumpMessage(mMessage));
            }

            mMessage.setFlag(Flags.Flag.DELETED, true);
        }
        catch (MessagingException e)
        {
            throw new EmailConnectionException(EmailConnectionExceptionMessages.ECE_ERROR_DELETING_THE_MESSAGE,
                                               sFolderName);
        }
    }

    /**
     * This method sets wether or not to reconnect for each and every action to the email server. When set to true it
     * will disconnect and reconnect before every getEmailHeader() call.
     *
     * @param  bAlwaysReconnect  Whether or not to reconnect for each and every action to the email server. When set to
     *                           true it will disconnect and reconnect before every getEmailHeader() call.
     */
    public void setAlwaysReconnect(boolean bAlwaysReconnect)
    {
        m_bAlwaysReconnect = bAlwaysReconnect;
    }

    /**
     * This method sets the store type (imap or pop3).
     *
     * @param  sStoreType  The store type (imap or pop3).
     */
    public void setStoreType(String sStoreType)
    {
        m_sStoreType = sStoreType;
    }

    /**
     * This method adds a new folder to the list of opened folders.
     *
     * @param   sName  The name of the folder to add.
     *
     * @throws  EmailConnectionException  In case of any exceptions.
     */
    protected void addFolder(String sName)
                      throws EmailConnectionException
    {
        if (!m_alMonitorFolders.contains(sName))
        {
            m_alMonitorFolders.add(sName);

            // Only open the folders already if there is a real connection available.
            if (m_sStore != null)
            {
                if (!m_hmOpenFolder.containsKey(sName))
                {
                    try
                    {
                        Folder fFolder = m_sStore.getFolder(sName);

                        if (!fFolder.isOpen())
                        {
                            fFolder.open(Folder.READ_WRITE);
                        }
                    }
                    catch (Exception e)
                    {
                        throw new EmailConnectionException(EmailConnectionExceptionMessages.ECE_ERROR_OPENING_FOLDER_WITH_NAME,
                                                           sName);
                    }
                }
            }
        }
    }

    /**
     * This method closes the current connections.
     *
     * @throws  EmailConnectionException  In case of any exceptions.
     */
    protected void closeCurrentConnection()
                                   throws EmailConnectionException
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Closing current connection");
        }

        if (m_hmOpenFolder.size() > 0)
        {
            for (Folder fFolder : m_hmOpenFolder.values())
            {
                try
                {
                    if (fFolder.isOpen())
                    {
                        if (LOG.isDebugEnabled())
                        {
                            // To check the message flags.
                            Message[] am = fFolder.getMessages();

                            for (Message message : am)
                            {
                                StringBuilder sbTemp = new StringBuilder(2048);
                                Flags f = message.getFlags();
                                sbTemp.append("Answered: " + f.contains(Flags.Flag.ANSWERED)).append(", ");
                                sbTemp.append("Deleted: " + f.contains(Flags.Flag.DELETED)).append(", ");
                                sbTemp.append("Draft: " + f.contains(Flags.Flag.DRAFT)).append(", ");
                                sbTemp.append("Flagged: " + f.contains(Flags.Flag.FLAGGED)).append(", ");
                                sbTemp.append("Recent: " + f.contains(Flags.Flag.RECENT)).append(", ");
                                sbTemp.append("Seen: " + f.contains(Flags.Flag.SEEN)).append(", ");
                                sbTemp.append("User: " + f.contains(Flags.Flag.USER)).append(", ");

                                if (LOG.isDebugEnabled())
                                {
                                    LOG.debug(sbTemp.toString());
                                }
                            }
                        }

                        try
                        {
                            fFolder.expunge();
                        }
                        catch (MethodNotSupportedException mnse)
                        {
                            if (LOG.isDebugEnabled())
                            {
                                LOG.debug("The expunge() method is not supported", mnse);
                            }
                        }

                        fFolder.close(true);
                    }
                }
                catch (MessagingException e)
                {
                    throw new EmailConnectionException(e, EmailConnectionExceptionMessages.ECE_ERROR_CLOSING_THE_FOLDER,
                                                       fFolder.getFullName());
                }
            }
        }

        closeStore();
    }

    /**
     * This method closes the store.
     *
     * @throws  EmailConnectionException  In case the closing fails.
     */
    protected void closeStore()
                       throws EmailConnectionException
    {
        if (m_sStore != null)
        {
            try
            {
                m_sStore.close();
                m_sStore = null;
            }
            catch (MessagingException e)
            {
                throw new EmailConnectionException(e, EmailConnectionExceptionMessages.ECE_ERROR_CLOSING_THE_STORE);
            }
        }
    }

    /**
     * This method gets the JavaMail store that should be used. It also connects to the store.
     *
     * @param   session   The JavaMail session.
     * @param   emailBox  The email box definition.
     *
     * @return  The created store.
     *
     * @throws  NoSuchProviderException  In case of exceptions.
     * @throws  MessagingException       In case of exceptions.
     */
    protected Store createAndConnectEmailStore(Session session, IEmailBox emailBox)
                                        throws NoSuchProviderException, MessagingException
    {
        Store sStore = session.getStore(getStoreType());
        sStore.connect(emailBox.getHost(), emailBox.getUsername(), emailBox.getPassword());

        return sStore;
    }

    /**
     * This method can be overloaded for a connection to fill the list of properties with connection specific
     * properties.
     *
     * @param  pJavaMailProperties  The current list of properties.
     */
    protected void fillAdditionalProperties(Properties pJavaMailProperties)
    {
    }

    /**
     * This method makes the actual connection to the email box. If the connection was already open it will be closed to
     * open a fresh connection.
     *
     * @throws  EmailConnectionException  In case of any exceptions.
     */
    protected synchronized void reconnect()
                                   throws EmailConnectionException
    {
        // Close the current connection
        closeCurrentConnection();

        // Now use that session to connected to the inbox.
        try
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Opening store of type " + getStoreType());
            }

            m_sStore = createAndConnectEmailStore(m_sSession, m_ebBox);

            if (LOG.isDebugEnabled())
            {
                // If debug is enabled we'll log all available folders.
                StringBuilder sbTemp = new StringBuilder(2048);
                Folder fTemp = m_sStore.getDefaultFolder();

                if (fTemp != null)
                {
                    sbTemp.append("Default folder: ").append(fTemp.getURLName()).append("\n");
                }

                Folder[] af = m_sStore.getPersonalNamespaces();

                for (Folder fFolder : af)
                {
                    sbTemp.append("Personal folder: ").append(fFolder.getURLName()).append("\n");
                }
                af = m_sStore.getSharedNamespaces();

                for (Folder fFolder : af)
                {
                    sbTemp.append("Shared folder: ").append(fFolder.getName()).append("\n");
                }
                LOG.debug(sbTemp.toString());
            }

            // Reopen all the folders.
            for (String sFolderName : m_alMonitorFolders)
            {
                Folder fFolder = m_sStore.getFolder(sFolderName);

                if (!fFolder.isOpen())
                {
                    fFolder.open(Folder.READ_WRITE);
                }

                m_hmOpenFolder.put(sFolderName, fFolder);

                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Opened connection to folder " + sFolderName + "\nMessage count: " +
                              fFolder.getMessageCount());
                }
            }
        }
        catch (Exception e)
        {
            throw new EmailConnectionException(e, EmailConnectionExceptionMessages.ECE_ERROR_CONNECTING_TO_THE_MAILBOX);
        }
    }

    /**
     * This method creates the session object to use.
     */
    private void createSession()
    {
        // Build up the properties for the JavaMail API and create the base session.
        Properties pJavaMailProps = new Properties();

        pJavaMailProps.setProperty("mail.store.protocol", getStoreType());

        pJavaMailProps.setProperty("mail." + getStoreType() + ".host", m_ebBox.getHost());
        pJavaMailProps.setProperty("mail." + getStoreType() + ".port", String.valueOf(m_ebBox.getPort()));

        fillAdditionalProperties(pJavaMailProps);

        // Now make the actual connection.
        if (LOG.isDebugEnabled())
        {
            StringBuffer sbTemp = new StringBuffer("Creating session to server. Properties:\n");

            for (Iterator<Object> iKeys = pJavaMailProps.keySet().iterator(); iKeys.hasNext();)
            {
                String sKey = (String) iKeys.next();
                sbTemp.append(sKey);
                sbTemp.append("=");
                sbTemp.append(pJavaMailProps.getProperty(sKey));
                sbTemp.append("\n");
            }
            LOG.debug(sbTemp.toString());
        }
        m_sSession = Session.getInstance(pJavaMailProps);
    }
}
