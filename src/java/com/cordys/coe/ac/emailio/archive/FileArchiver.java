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
 package com.cordys.coe.ac.emailio.archive;

import com.cordys.coe.ac.emailio.EmailIOConnectorConstants;
import com.cordys.coe.ac.emailio.exception.ArchiverException;
import com.cordys.coe.ac.emailio.exception.StorageProviderException;
import com.cordys.coe.ac.emailio.localization.ArchiverExceptionMessages;
import com.cordys.coe.ac.emailio.objects.ContextContainer;
import com.cordys.coe.ac.emailio.objects.EmailMessage;
import com.cordys.coe.ac.emailio.objects.IStorageProviderQueryManager;
import com.cordys.coe.ac.emailio.util.StringUtil;

import com.eibus.management.IManagedComponent;

import com.eibus.util.ZipUtil;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.system.EIBProperties;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;

/**
 * This archiver will archive to the file system. It will write an XML file containing the records
 * and the file will be no bigger then a certain specified length.
 *
 * @author  pgussow
 */
public class FileArchiver extends AbstractArchiver
{
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(FileArchiver.class);
    /**
     * Holds the name of the location parameter.
     */
    private static final String PARAM_ARCHIVE_FOLDER = "location";
    /**
     * Holds the name of the max raw file size parmaeter.
     */
    private static final String PARAM_ZIP_LEVEL = "ziplevel";
    /**
     * Holds the actual archive folder.
     */
    private File m_fArchive;
    /**
     * Holds the zip level to use.
     */
    private int m_iZipLevel;

    /**
     * This method starts the actual archiving of the passed on context containers.
     *
     * @param   lccContainers  The containers to archive.
     * @param   spqmManager    The QueryManager to use.
     *
     * @throws  ArchiverException  In case of any exceptions.
     *
     * @see     com.cordys.coe.ac.emailio.archive.IArchiver#doArchive(java.util.List, com.cordys.coe.ac.emailio.objects.IStorageProviderQueryManager)
     */
    @Override public void doArchive(List<ContextContainer> lccContainers,
                                    IStorageProviderQueryManager spqmManager)
                             throws ArchiverException
    {
        File fArchive = getArchiveFolder();

        for (ContextContainer ccContainer : lccContainers)
        {
            int iWrapperXML = 0;
            ZipOutputStream zosZip = null;
            File fZipFile = null;
            List<EmailMessage> lEmails = null;

            try
            {
                lEmails = spqmManager.getEmailMessagesByContextID(ccContainer.getID());

                // Create the ZipFile
                fZipFile = new File(fArchive,
                                    ccContainer.getID().replaceAll("[^a-zA-Z0-9]", "") + ".zip");
                zosZip = new ZipOutputStream(new FileOutputStream(fZipFile, false));
                zosZip.setLevel(m_iZipLevel);

                // Now we have both the container and the emails. We will create an XML
                // to hold all data.
                int iObjectData = ccContainer._getObjectData();

                // Create the zip entry for it.
                ZipEntry zeContainer = new ZipEntry("container.xml");
                zosZip.putNextEntry(zeContainer);

                String sXML = Node.writeToString(iObjectData, true);
                zosZip.write(sXML.getBytes());
                zosZip.closeEntry();

                // Add the email messages to the zip file.
                int iCount = 0;

                for (EmailMessage emMessage : lEmails)
                {
                    iObjectData = emMessage._getObjectData();

                    ZipEntry zeEmail = new ZipEntry("email_" + iCount++ + ".xml");
                    zosZip.putNextEntry(zeEmail);

                    sXML = Node.writeToString(iObjectData, true);
                    zosZip.write(sXML.getBytes());
                    zosZip.closeEntry();
                }

                // Now all files are written into a single Zip file, so we can close it.
            }
            catch (Exception e)
            {
                throw new ArchiverException(e,
                                            ArchiverExceptionMessages.ARE_ERROR_ARCHIVING_CONTAINER_0,
                                            ccContainer.getID());
            }
            finally
            {
                if (iWrapperXML != 0)
                {
                    Node.delete(iWrapperXML);
                }

                if (zosZip != null)
                {
                    try
                    {
                        zosZip.close();
                    }
                    catch (IOException e)
                    {
                        if (LOG.isDebugEnabled())
                        {
                            LOG.debug("Error closing zip file " + fZipFile.getAbsolutePath(), e);
                        }
                    }
                }
            }

            // If we get here the zipfile was successfully created. So now we need to remove the
            // mails and the container from the current storage provider.
            try
            {
                spqmManager.removeContextContainer(ccContainer);

                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Archived the container with id " + ccContainer.getID());
                }
            }
            catch (StorageProviderException e)
            {
                throw new ArchiverException(e,
                                            ArchiverExceptionMessages.ARE_ERROR_REMOVING_CONTEXT_CONTAINER_0_FROM_THE_STORAGE,
                                            ccContainer.getID());
            }
        }

        // Now all containers have been archived, we will create a single zip file.
        try
        {
            File fFinalArchive = new File(fArchive.getParentFile(), fArchive.getName() + ".zip");

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Archiving folder " + fArchive.getCanonicalPath() + " into file " +
                          fFinalArchive.getCanonicalPath());
            }

            new ZipUtil().compress(fArchive, fFinalArchive.getAbsolutePath());
        }
        catch (Exception e)
        {
            throw new ArchiverException(e,
                                        ArchiverExceptionMessages.ARE_ERROR_ZIPPING_ALL_ARCHIVE_FILES);
        }
    }

    /**
     * @see  com.cordys.coe.ac.emailio.archive.AbstractArchiver#postInit(int, XPathMetaInfo, String,
     *       IManagedComponent)
     */
    @Override protected void postInit(int iParameters, XPathMetaInfo xmi, String sOrganization,
                                      IManagedComponent mcParent)
    {
        String sArchiveFolder = getStringParameter(PARAM_ARCHIVE_FOLDER);

        if (!StringUtil.isSet(sArchiveFolder))
        {
            sArchiveFolder = EmailIOConnectorConstants.DEPLOY_FOLDER + "/archiver";
        }

        if (FilenameUtils.getPrefix(sArchiveFolder).length() == 0)
        {
            // Relative path
            m_fArchive = new File(new File(EIBProperties.getInstallDir()), sArchiveFolder);
        }
        else
        {
            m_fArchive = new File(sArchiveFolder);
        }

        if (!m_fArchive.exists())
        {
            m_fArchive.mkdirs();
        }

        // Get the max raw filesize (in megabytes
        String sZipLevel = getStringParameter(PARAM_ZIP_LEVEL);

        if (!StringUtil.isSet(sZipLevel))
        {
            sZipLevel = "100";
        }
        m_iZipLevel = Integer.parseInt(sZipLevel);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Location: " + m_fArchive.getAbsolutePath() + "\nZip level: " + m_iZipLevel);
        }
    }

    /**
     * This method returns the folder that will be used to write the archive to.
     *
     * @return  The file to use.
     */
    private File getArchiveFolder()
    {
        File fReturn = null;

        String sBase = new SimpleDateFormat("yyyyMMss_HHmmss").format(new Date());
        String sCurrent = sBase;
        int iCount = 0;
        boolean bDone = false;

        do
        {
            fReturn = new File(m_fArchive, sCurrent);

            if (!fReturn.exists())
            {
                bDone = fReturn.mkdirs();
            }

            sCurrent = sBase + "_" + iCount++;
        }
        while (!bDone);

        return fReturn;
    }
}
