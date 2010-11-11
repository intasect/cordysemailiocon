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
import com.cordys.coe.ac.emailio.config.trigger.TriggerFactory;
import com.cordys.coe.ac.emailio.exception.StorageProviderException;
import com.cordys.coe.ac.emailio.localization.LogMessages;
import com.cordys.coe.ac.emailio.localization.StorageProviderExceptionMessages;
import com.cordys.coe.ac.emailio.monitor.RuleContext;
import com.cordys.coe.ac.emailio.monitor.RuleContextContainer;
import com.cordys.coe.ac.emailio.util.MailMessageUtil;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.management.IManagedComponent;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.system.EIBProperties;
import com.eibus.util.system.Native;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;

import java.util.Date;

import javax.mail.Message;

import javax.mail.internet.MimeMessage;

import org.apache.commons.io.FilenameUtils;

/**
 * This class is the default implementation for the storage. The folders can be configured.<br>
 * The configuration should look like this:
 *
 * <pre>
     <storage>
      <class>com.cordys.coe.ac.emailio.storage.FileStorageProvider</class>
      <parameters>
          <logincomingmessages>true</logincomingmessages>
          <logfolder>coe/emailioconnector/archive</logfolder>
          <errorfolder>coe/emailioconnector/error</errorfolder>
          <triggerfolder>coe/emailioconnector/trigger</triggerfolder>
      </parameters>
     </storage>
 * </pre>
 *
 * @author  pgussow
 */
public class FileStorageProvider extends AbstractStorageProvider
{
    /**
     * Holds the name of the metadata file.
     */
    private static final String METADATA_FILENAME = "metadata.xml";
    /**
     * Holds the name of the metadata tag.
     */
    private static final String TAG_METADATA = "metadata";
    /**
     * Holds the name of the triggerdefinition tag.
     */
    private static final String TAG_TRIGGER_DEFINITION = "triggerdefinition";
    /**
     * Whenever a trigger is serialized, this envelope structure will be used as envelope for the
     * XML written to the file.
     *
     * <p>This envelope will be parsed using the NOM document in order to get a new document that
     * can be used for serialization.</p>
     */
    private static final String SERIALIZATION_XML_ENVELOPE = "<triggerfile xmlns=\"" +
                                                             EmailIOConnectorConstants.NS_CONFIGURATION +
                                                             "\"/>";
    /**
     * Holds the name of the status tag.
     */
    private static final String TAG_STATUS = "status";
    /**
     * Holds the name of the emailcount tag.
     */
    private static final String TAG_EMAILCOUNT = "emailcount";
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(FileStorageProvider.class);
    /**
     * Holds the document used for XML creation.
     */
    private static Document s_dDoc = new Document();
    /**
     * The file extension that will be used for the trigger files without the leading dot.
     */
    private static final String TRIGGERFILE_EXTENSION = "xml";
    /**
     * Holds whether or not all incoming messages should be archived.
     */
    private boolean m_bLogIncomingMessages;
    /**
     * Holds the location where all messages should be logged to.
     */
    private File m_fArchiveFolder;
    /**
     * Holds the location of the folder in which mails should be written that were not processed
     * correctly.
     */
    private File m_fErrorFolder;
    /**
     * Holds the location where all dynamic triggers should be logged to.
     */
    private File m_fTriggerFolder;
    /**
     * Holds the XPathMetaInfo object to use.
     */
    private XPathMetaInfo m_xmi;

    /**
     * Creates a new FileStorageProvider object.
     */
    public FileStorageProvider()
    {
        m_xmi = new XPathMetaInfo();
        m_xmi.addNamespaceBinding("ns", EmailIOConnectorConstants.NS_CONFIGURATION);
    }

    /**
     * <p>This method will persist the contents of the Rule Context container. This allows crash
     * recovery. Based on the content of the rule context and the actual trigger definition the
     * processing could be restarted.</p>
     *
     * <p>There are several things that need to be stored before we can continue:<br>
     * 1. The content of the rule context container. We'll create an XML file containing all data.
     * <br>
     * 2. The actual emails that belong to this match.<br>
     * 3. The trigger definition that was matched. Without it we cannot restart.<br>
     * Each context will get their own folder with a unique ID. The trigger definition and other
     * metadata will be written to a metadata.xml file.</p>
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
        // Before we remove the message we write it to the the error-folder. We'll ONLY delete the
        // message when we know the file was written correctly. Also the name of the file is added
        // to the rule context. If we don't write the mail to the error folder and the processing
        // fails then the mail would be lost.

        // Create the folder in which we will store the data.
        String sGUID = Native.createGuid().replaceAll("[^a-zA-Z0-9]", "");

        // Make sure the context container knows it storage ID.
        rccContext.setStorageID(sGUID);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Created GUID for the storage container: " + sGUID);
        }

        // Create the error folder
        File fErrorFolder = new File(m_fErrorFolder, sGUID);

        if (!fErrorFolder.exists())
        {
            fErrorFolder.mkdirs();
        }

        File fMetadata = new File(fErrorFolder, METADATA_FILENAME);

        // Create the metadata XML containing the trigger definition and the amount of emails that
        // are in this context
        int iMetadata = 0;

        try
        {
            try
            {
                iMetadata = s_dDoc.createElementNS(TAG_METADATA, null, null,
                                                   EmailIOConnectorConstants.NS_CONFIGURATION, 0);

                Node.createElementWithParentNS(TAG_STATUS, EProcessingStatus.INITIAL.name(),
                                               iMetadata);
                Node.createElementWithParentNS(TAG_EMAILCOUNT, String.valueOf(rccContext.size()),
                                               iMetadata);

                int iTrigger = Node.createElementWithParentNS(TAG_TRIGGER_DEFINITION, null,
                                                              iMetadata);
                tTrigger.toXML(iTrigger);

                // Now write it to the file

                Node.writeToFile(iMetadata, iMetadata, fMetadata.getAbsolutePath(),
                                 Node.WRITE_HEADER | Node.WRITE_NORMAL);
            }
            catch (Exception e)
            {
                throw new StorageProviderException(e,
                                                   StorageProviderExceptionMessages.SPE_ERROR_CREATING_METADATA_FILE,
                                                   fMetadata.getAbsolutePath());
            }
            finally
            {
                if (iMetadata != 0)
                {
                    Node.delete(iMetadata);
                }
            }

            // Now the metadata is written. Now write all email messages.
            int iCount = 1;

            for (RuleContext rcContext : rccContext)
            {
                File fEmail = new File(fErrorFolder, "email_" + iCount + ".eml");

                try
                {
                    FileOutputStream fos = new FileOutputStream(fEmail, false);
                    rcContext.getMessage().writeTo(fos);
                    fos.flush();
                    fos.close();
                }
                catch (Exception e)
                {
                    throw new StorageProviderException(e,
                                                       StorageProviderExceptionMessages.SPE_ERROR_WRITING_EMAIL_TO_FILE_0,
                                                       fEmail.getAbsolutePath());
                }
                iCount++;
            }
        }
        catch (StorageProviderException spe)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("We're going to throw an exception, so we need to clean up the file system");
            }

            try
            {
                String[] as = fErrorFolder.list();

                for (String sFile : as)
                {
                    new File(fErrorFolder, sFile).delete();
                }
                fErrorFolder.delete();
            }
            catch (Exception e1)
            {
                LOG.error(e1, LogMessages.ERR_DELETING_FOLDER_FROM_THE_FILESYSTEM,
                          fErrorFolder.getAbsolutePath());
            }

            throw spe;
        }
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
     * This method returns the email message with the given index for the given storage ID.
     *
     * @param   sStorageID  The ID of the storage.
     * @param   iIndex      The index of the mail.
     *
     * @return  The list of messages for this storage ID.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    public Message getMessage(String sStorageID, int iIndex)
                       throws StorageProviderException
    {
        Message mReturn = null;

        File fStorage = getLocation(sStorageID);

        File fEmail = new File(fStorage, "email_" + iIndex + ".eml");

        if (!fEmail.exists())
        {
            throw new StorageProviderException(StorageProviderExceptionMessages.SPE_THE_EMAIL_WITH_INDEX_0_DOES_NOT_EXIST,
                                               iIndex, fEmail.getAbsolutePath());
        }

        try
        {
            mReturn = MailMessageUtil.createMimeMessage(fEmail);
        }
        catch (Exception e)
        {
            throw new StorageProviderException(e,
                                               StorageProviderExceptionMessages.SPE_ERROR_READING_EML_FILE,
                                               fEmail.getAbsolutePath());
        }

        return mReturn;
    }

    /**
     * This method returns all email messages for the given storage ID.
     *
     * @param   sStorageID  The ID of the storage.
     *
     * @return  The list of messages for this storage ID.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    public Message[] getMessages(String sStorageID)
                          throws StorageProviderException
    {
        Message[] amReturn = new Message[0];

        File fStorage = getLocation(sStorageID);
        File fMetadata = new File(fStorage, METADATA_FILENAME);

        if (!fMetadata.exists())
        {
            throw new StorageProviderException(StorageProviderExceptionMessages.SPE_COULD_NOT_FIND_THE_METADATA_FILE,
                                               fMetadata.getAbsolutePath());
        }

        int iMetadata = 0;
        FileInputStream fis = null;

        try
        {
            iMetadata = s_dDoc.load(fMetadata.getAbsolutePath());

            int iMailCount = XPathHelper.getIntegerValue(iMetadata, "./ns:" + TAG_EMAILCOUNT, m_xmi,
                                                         0);

            if (iMailCount > 0)
            {
                for (int iCount = 1; iCount <= iMailCount; iCount++)
                {
                    File fEmail = new File(fStorage, "email_" + iCount + ".eml");

                    if (!fEmail.exists())
                    {
                        throw new StorageProviderException(StorageProviderExceptionMessages.SPE_COULD_NOT_FIND_EMAIL_WITH_ID_0_1,
                                                           iCount, fEmail.getAbsolutePath());
                    }
                    fis = new FileInputStream(fEmail);

                    MimeMessage mTemp = MailMessageUtil.createMimeMessage(fis);
                    amReturn[iCount - 1] = mTemp;
                }
            }
            else
            {
                throw new StorageProviderException(StorageProviderExceptionMessages.SPE_THERE_ARE_NO_EMAIL_MESSAGES_FOR_STORAGE_ID_0,
                                                   sStorageID);
            }
        }
        catch (Exception e)
        {
            throw new StorageProviderException(e,
                                               StorageProviderExceptionMessages.SPE_ERROR_SETTING_THE_STATUS_OF_CONTAINER_TO_IN_PROGRESS,
                                               sStorageID);
        }
        finally
        {
            if (iMetadata != 0)
            {
                Node.delete(iMetadata);
            }

            if (fis != null)
            {
                try
                {
                    fis.close();
                }
                catch (IOException e)
                {
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Error closing stream", e);
                    }
                }
            }
        }

        return amReturn;
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
        try
        {
            return getTriggerFile(sTriggerName).exists();
        }
        catch (StorageProviderException e)
        {
            // if no file name can be created from the trigger name, it's rather
            // obvious that no such file exists.
            return false;
        }
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
        updateStatusWithInfo(rccContext, sStatusInfo, EProcessingStatus.ACTION_ERROR);
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
        File fMetadata = getMetadataFile(rccContext);
        int iMetadata = 0;

        try
        {
            iMetadata = s_dDoc.load(fMetadata.getAbsolutePath());

            int iStatus = XPathHelper.selectSingleNode(iMetadata, "./ns:status", m_xmi);

            while (Node.getFirstChild(iStatus) != 0)
            {
                Node.delete(Node.getFirstChild(iStatus));
            }
            s_dDoc.createText(EProcessingStatus.COMPLETED.name(), iStatus);

            Node.writeToFile(iMetadata, iMetadata, fMetadata.getAbsolutePath(),
                             Node.WRITE_HEADER | Node.WRITE_NORMAL);

            // Now determine whether we have to delete the folder or move it to the archive folder.
            File fFolder = fMetadata.getParentFile();

            if (m_bLogIncomingMessages)
            {
                // Move it to the archive
                fFolder.renameTo(new File(m_fArchiveFolder, fFolder.getName()));
            }
            else
            {
                // Just delete them.

                String[] as = fFolder.list();

                for (String sFile : as)
                {
                    new File(fFolder, sFile).delete();
                }
                fFolder.delete();
            }
        }
        catch (Exception e)
        {
            throw new StorageProviderException(e,
                                               StorageProviderExceptionMessages.SPE_ERROR_UPDATING_THE_STATUS_OF_CONTAINER_TO_STATUS,
                                               rccContext.getStorageID(),
                                               EProcessingStatus.COMPLETED);
        }
        finally
        {
            if (iMetadata != 0)
            {
                Node.delete(iMetadata);
            }
        }
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
     * @see     IEmailStorageProvider#setContainerStatusError(RuleContextContainer, String)
     */
    @Override public void setContainerStatusError(RuleContextContainer rccContext,
                                                  String sStatusInfo)
                                           throws StorageProviderException
    {
        updateStatusWithInfo(rccContext, sStatusInfo, EProcessingStatus.MESSAGE_ERROR);
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
        File fMetadata = getMetadataFile(rccContext);
        int iMetadata = 0;

        try
        {
            iMetadata = s_dDoc.load(fMetadata.getAbsolutePath());

            int iStatus = XPathHelper.selectSingleNode(iMetadata, "./ns:status", m_xmi);

            while (Node.getFirstChild(iStatus) != 0)
            {
                Node.delete(Node.getFirstChild(iStatus));
            }
            s_dDoc.createText(EProcessingStatus.IN_PROGRESS.name(), iStatus);

            Node.writeToFile(iMetadata, iMetadata, fMetadata.getAbsolutePath(),
                             Node.WRITE_HEADER | Node.WRITE_NORMAL);
        }
        catch (Exception e)
        {
            throw new StorageProviderException(e,
                                               StorageProviderExceptionMessages.SPE_ERROR_UPDATING_THE_STATUS_OF_CONTAINER_TO_STATUS,
                                               rccContext.getStorageID(),
                                               EProcessingStatus.IN_PROGRESS);
        }
        finally
        {
            if (iMetadata != 0)
            {
                Node.delete(iMetadata);
            }
        }
    }

    /**
     * Encodes the input so it can safely be used as a file or diretcory name.
     *
     * <p>This method uses an extended version of the standard URL encoding scheme. The encoding
     * process works as follows:</p>
     *
     * <ol>
     *   <li>The input is encoded using the standard URL encoder and the UTF-8 encoding scheme.</li>
     *   <li>In the URL encoded version of the string, the characters "*", ".", "+" and "_" are
     *     replaced with their URL encoded versions, i.e. "%2a", %2e", "%2b" and "%5f".</li>
     *   <li>The character "%" is replaced with an underscore "_"</li>
     * </ol>
     *
     * <p>The result of this encoding is still more or less readable, it is guaranteed to be safe
     * for file names and it can be decoded again if necessary.</p>
     *
     * @param   sUnsafeName  Any string.
     *
     * @return  A representation of the string that is safe for use as a file or directory name.
     *
     * @throws  StorageProviderException  If the file name is null or empty.
     */
    protected String getSafeFilename(String sUnsafeName)
                              throws StorageProviderException
    {
        if ((sUnsafeName == null) || (sUnsafeName.length() == 0))
        {
            throw new StorageProviderException(StorageProviderExceptionMessages.SPE_EMPTY_STRINGS_CANT_BE_USED_AS_FILE_NAMES);
        }

        String result = null;

        try
        {
            result = URLEncoder.encode(sUnsafeName, "UTF-8");
        }
        catch (UnsupportedEncodingException ex)
        {
            // sorry, this _really_ can't happen as the specification of the Java
            // language makes UTF-8 support mandatory...
        }

        return result.replaceAll("\\*", "%2A").replaceAll("\\+", "%2B").replaceAll("\\.", "%2E")
                     .replaceAll("_", "%5F").replace('%', '_');
    }

    /**
     * Returns the file for a given trigger name.
     *
     * <p>The actual filename is created from the base directory, the encoded version of the trigger
     * name and the Extension as specified by the constant TRIGGERFILE_EXTENSION.</p>
     *
     * @param   sTriggerName  The name of the trigger for which a storage file is needed.
     *
     * @return  The file using which the trigger storage should be accessed.
     *
     * @throws  StorageProviderException  If the name of the trigger is null or empty.
     */
    protected File getTriggerFile(String sTriggerName)
                           throws StorageProviderException
    {
        return new File(getWorkingDirectory(),
                        getSafeFilename(sTriggerName) + "." + TRIGGERFILE_EXTENSION);
    }

    /**
     * Tries to deserialize a trigger from the given file.
     *
     * <p>In order to deserialize, the first XML element with the name specified in the constant
     * {@link ITrigger#TAG_TRIGGER} is taken and fed to the TriggerFactory. The trigger is always
     * created as afire-once trigger, because that's what the current semantics of the
     * registerTrigger method are.</p>
     *
     * @param   triggerFile  The file from which the trigger should be read.
     *
     * @return  The trigger.
     *
     * @throws  StorageProviderException  If the deserialization fails.
     *
     * @see     ITrigger#TAG_TRIGGER
     */
    protected ITrigger getTriggerFromFile(File triggerFile)
                                   throws StorageProviderException
    {
        int docNode = 0;

        try
        {
            docNode = s_dDoc.load(triggerFile.getAbsolutePath());

            XPathMetaInfo xmi = new XPathMetaInfo();
            xmi.addNamespaceBinding("ns", EmailIOConnectorConstants.NS_CONFIGURATION);

            int triggerNode = XPathHelper.selectSingleNode(docNode, "./ns:" + ITrigger.TAG_TRIGGER,
                                                           xmi);

            return TriggerFactory.createTrigger(triggerNode, getEmailBox());
        }
        catch (Exception ex)
        {
            throw new StorageProviderException(ex,
                                               StorageProviderExceptionMessages.DPE_COULD_NOT_DESERIALIZE_TRIGGER_0,
                                               ex.getMessage());
        }
        finally
        {
            if (docNode > 0)
            {
                Node.delete(docNode);
            }
        }
    }

    /**
     * Returns the directory in which this store saves the serialized triggers.
     *
     * @return  The directory in which this store saves the serialized triggers.
     */
    protected final File getWorkingDirectory()
    {
        return m_fTriggerFolder;
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
     * @throws  StorageProviderException  If the name of the trigger is null or empty.
     *
     * @see     AbstractStorageProvider#persistTrigger(ITrigger)
     */
    @Override protected void persistTrigger(ITrigger tTrigger)
                                     throws StorageProviderException
    {
        int iDocNode = 0;

        try
        {
            iDocNode = s_dDoc.parseString(SERIALIZATION_XML_ENVELOPE);

            tTrigger.toXML(iDocNode);

            Node.writeToFile(0, iDocNode, getTriggerFile(tTrigger.getName()).getAbsolutePath(),
                             Node.WRITE_HEADER | Node.WRITE_PRETTY);
        }
        catch (Exception ex)
        {
            // this also shouldn't happen, but at least it's possible.
            throw new StorageProviderException(ex,
                                               StorageProviderExceptionMessages.SPE_COULD_NOT_DESERIALIZE_TRIGGER,
                                               ex.getMessage());
        }
        finally
        {
            // we must free the node or we'll have a memory leak on our hands. if you
            // think NOM, you have to think C - and not the sharp kind.
            if (iDocNode > 0)
            {
                Node.delete(iDocNode);
            }
        }
    }

    /**
     * Adapter method that is called after the parameters are parsed.
     *
     * @param   ebEmailBox          The corresponding email box.
     * @param   iConfigurationNode  The XML containing the configuration.
     * @param   xmi                 The XPath meta info to use. The prefix ns should be mapped to
     *                              the proper namespace.
     * @param   mcParent            The parent managed component.*
     *
     * @throws  StorageProviderException  In case of any exception.
     *
     * @see     AbstractStorageProvider#postInit(IEmailBox, int, XPathMetaInfo, IManagedComponent)
     */
    @Override protected void postInit(IEmailBox ebEmailBox, int iConfigurationNode,
                                      XPathMetaInfo xmi, IManagedComponent mcParent)
                               throws StorageProviderException
    {
        m_bLogIncomingMessages = "true".equals(getStringParameter("logincomingmessages"));

        String sLogFolder = getStringParameter("logfolder");

        if ((sLogFolder == null) || (sLogFolder.length() == 0))
        {
            sLogFolder = "coe/emailioconnector/archive";
        }
        m_fArchiveFolder = getProperFolder(sLogFolder, ebEmailBox);

        String sErrorFolder = getStringParameter("errorfolder");

        if ((sErrorFolder == null) || (sErrorFolder.length() == 0))
        {
            sErrorFolder = "coe/emailioconnector/error";
        }
        m_fErrorFolder = getProperFolder(sErrorFolder, ebEmailBox);

        String sTriggerFolder = getStringParameter("triggerfolder");

        if ((sTriggerFolder == null) || (sTriggerFolder.length() == 0))
        {
            sTriggerFolder = "coe/emailioconnector/trigger";
        }
        m_fTriggerFolder = getProperFolder(sTriggerFolder, ebEmailBox);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("configured File-based storage using:\nLog folder: " + m_fArchiveFolder +
                      "\nError folder: " + m_fErrorFolder + "\nLogging incoming messages: " +
                      m_bLogIncomingMessages + "\nTrigger folder: " + m_fTriggerFolder);
        }

        restoreTriggers();
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
        getTriggerFile(sTriggerName).delete();
    }

    /**
     * Restores the persistent triggers that are in the working directory.
     *
     * <p>This method iterates over the working directory and tries to deserialize a trigger from
     * every file it finds. Note that the directory isn't scanned recursively, so no subdirectories
     * should be used.</p>
     *
     * @throws  StorageProviderException  If the deserialization of the triggers fails.
     */
    protected void restoreTriggers()
                            throws StorageProviderException
    {
        File[] triggerFiles = getWorkingDirectory().listFiles();

        if (triggerFiles != null)
        {
            for (File triggerFile : triggerFiles)
            {
                ITrigger trigger = getTriggerFromFile(triggerFile);

                // obviously we have to add the triggers as "non persistent" as new
                // files would be created otherwise.
                addTrigger(trigger, false);
            }
        }
    }

    /**
     * This method returns the location where the details for this storage ID are stored.
     *
     * @param   sStorageID  The ID of the storage.
     *
     * @return  The location (either the error folder or the archive folder) where the data is
     *          stored for this storage ID.
     *
     * @throws  StorageProviderException  In case the ID was nowhere to be found.
     */
    private File getLocation(String sStorageID)
                      throws StorageProviderException
    {
        File fReturn = null;

        fReturn = new File(m_fErrorFolder, sStorageID);

        if (!fReturn.exists())
        {
            fReturn = new File(m_fArchiveFolder, sStorageID);

            if (!fReturn.exists())
            {
                throw new StorageProviderException(StorageProviderExceptionMessages.SPE_COULD_NOT_FIND_THE_STORAGE_ID_0_IN_THE_FOLDER_1_OR_IN_THE_FOLDER_2,
                                                   sStorageID, m_fErrorFolder.getAbsolutePath(),
                                                   m_fArchiveFolder.getAbsolutePath());
            }
        }
        return fReturn;
    }

    /**
     * This method returns the location of the metadata file for the specific context.
     *
     * @param   rccContext  The RuleContextContainer.
     *
     * @return  The location of the metadata file.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    private File getMetadataFile(RuleContextContainer rccContext)
                          throws StorageProviderException
    {
        File fReturn = null;

        // Create the error folder
        fReturn = new File(m_fErrorFolder, rccContext.getStorageID());
        fReturn = new File(fReturn, METADATA_FILENAME);

        if (!fReturn.exists())
        {
            throw new StorageProviderException(StorageProviderExceptionMessages.SPE_COULD_NOT_FIND_THE_METADATA_FILE,
                                               fReturn.getAbsolutePath());
        }

        return fReturn;
    }

    /**
     * This method returns the log folder. It will check whether the folder is relative or not and
     * then it will create the folder if it is not there on the filesystem.
     *
     * @param   sLogFolder  The folder to create.
     * @param   ebEmailBox  The corresponding email box.
     *
     * @return  The folder.
     */
    private File getProperFolder(String sLogFolder, IEmailBox ebEmailBox)
    {
        File fReturn = null;

        if (FilenameUtils.getPrefix(sLogFolder).length() == 0)
        {
            // Relative file
            fReturn = new File(EIBProperties.getInstallDir(), sLogFolder);
        }
        else
        {
            fReturn = new File(sLogFolder);
        }

        if (!fReturn.exists())
        {
            fReturn.mkdirs();
        }

        // Now create the folder specific for this emailbox.
        fReturn = new File(fReturn, ebEmailBox.getName().replaceAll("[^a-zA-Z0-9]", ""));

        if (!fReturn.exists())
        {
            fReturn.mkdirs();
        }

        return fReturn;
    }

    /**
     * This method updates the status for this for this context.
     *
     * @param   rccContext   The rule context container containing all messages.
     * @param   sStatusInfo  The additional status information.
     * @param   psStatus     The actual status.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    private void updateStatusWithInfo(RuleContextContainer rccContext, String sStatusInfo,
                                      EProcessingStatus psStatus)
                               throws StorageProviderException
    {
        File fMetadata = getMetadataFile(rccContext);
        int iMetadata = 0;

        try
        {
            iMetadata = s_dDoc.load(fMetadata.getAbsolutePath());

            int iStatus = XPathHelper.selectSingleNode(iMetadata, "./ns:status", m_xmi);

            while (Node.getFirstChild(iStatus) != 0)
            {
                Node.delete(Node.getFirstChild(iStatus));
            }
            s_dDoc.createText(psStatus.name(), iStatus);

            // Add the exception report
            int iErrors = XPathHelper.selectSingleNode(iMetadata, "./ns:errors", m_xmi);

            if (iErrors == 0)
            {
                iErrors = s_dDoc.createElementWithParentNS("errors", null, iMetadata);
            }

            int iError = s_dDoc.createElementWithParentNS("error", sStatusInfo, iErrors);
            Node.setAttribute(iError, "timestamp", String.valueOf(new Date().getTime()));

            // Write it back to the file.
            Node.writeToFile(iMetadata, iMetadata, fMetadata.getAbsolutePath(),
                             Node.WRITE_HEADER | Node.WRITE_NORMAL);
        }
        catch (Exception e)
        {
            throw new StorageProviderException(e,
                                               StorageProviderExceptionMessages.SPE_ERROR_UPDATING_THE_STATUS_OF_CONTAINER_TO_STATUS,
                                               rccContext.getStorageID(), psStatus);
        }
        finally
        {
            if (iMetadata != 0)
            {
                Node.delete(iMetadata);
            }
        }
    }
}
