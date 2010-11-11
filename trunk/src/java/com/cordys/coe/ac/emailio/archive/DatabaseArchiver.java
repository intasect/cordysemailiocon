

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
package com.cordys.coe.ac.emailio.archive;

import com.cordys.coe.ac.emailio.exception.ArchiverException;
import com.cordys.coe.ac.emailio.exception.WsAppUtilException;
import com.cordys.coe.ac.emailio.localization.ArchiverExceptionMessages;
import com.cordys.coe.ac.emailio.localization.LogMessages;
import com.cordys.coe.ac.emailio.objects.ContextContainer;
import com.cordys.coe.ac.emailio.objects.EmailBox;
import com.cordys.coe.ac.emailio.objects.EmailMessage;
import com.cordys.coe.ac.emailio.objects.IStorageProviderQueryManager;
import com.cordys.coe.ac.emailio.storage.db.DBContextContainer;
import com.cordys.coe.ac.emailio.storage.db.DBEmail;
import com.cordys.coe.ac.emailio.storage.db.DBEmailBox;
import com.cordys.coe.ac.emailio.util.WsAppUtil;

import com.cordys.cpc.bsf.busobject.BSF;
import com.cordys.cpc.bsf.busobject.BsfContext;
import com.cordys.cpc.bsf.busobject.BusObjectConfig;
import com.cordys.cpc.bsf.busobject.BusObjectManager;
import com.cordys.cpc.bsf.busobject.DMLStatement;
import com.cordys.cpc.bsf.busobject.QueryObject;

import com.eibus.management.IManagedComponent;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.system.Native;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

import java.util.List;

/**
 * This method will archive the data to the target database. The tables must be called
 * ARC_CONTEXT_CONTAINER and ARC_EMAIL
 *
 * @author  pgussow
 */
public class DatabaseArchiver extends AbstractArchiver
{
    /**
     * Holds the name of the archiver connection pool.
     */
    private static final String ARCHIVE_DATABASE = "ArchiveDatabase";
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(DatabaseArchiver.class);

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
        BsfContext bcContext = null;

        try
        {
            BusObjectManager bom = WsAppUtil.createBusObjectManager(ARCHIVE_DATABASE);

            bcContext = new BsfContext(bom);
            BSF.registerContext(bcContext);

            for (ContextContainer ccContainer : lccContainers)
            {
                String sTransactionID = Native.createGuid();

                try
                {
                    // Start the transaction on the Archive dabase.
                    bom.startTransaction(sTransactionID);

                    QueryObject query = new QueryObject("select * from \"EMAIL_BOX\" where \"EMAILBOX_ID\" = '" +
                                                        ccContainer.getEmailBoxName() +
                                                        "' or BOX_NAME = '" +
                                                        ccContainer.getEmailBoxName() + "'");
                    query.setResultClass(DBEmailBox.class);

                    DBEmailBox eb = (DBEmailBox) query.getObject(bom);
                    String sEmailBoxID = null;

                    if (eb == null)
                    {
                        // It's not there, let's insert it. First we need to close the current
                        // transaction and unregister our current context Otherwise the other
                        // database will fail.
                        bom.commitTransaction(sTransactionID, true);
                        BSF.unregisterContext(bcContext);

                        EmailBox ebSource = spqmManager.getEmailBox(ccContainer.getEmailBoxName());

                        if (ebSource == null)
                        {
                            throw new ArchiverException(ArchiverExceptionMessages.ARE_COULD_NOT_GET_THE_DETAILS_OF_EMAIL_BOX_0_FROM_THE_STORAGE_PROVIDER,
                                                        ccContainer.getEmailBoxName(),
                                                        spqmManager.toString());
                        }

                        // Start a transaction again for the current context.
                        bom.startTransaction(sTransactionID);
                        BSF.registerContext(bcContext);

                        DBEmailBox dbNew = new DBEmailBox(new BusObjectConfig(bom, 0,
                                                                              BusObjectConfig.NEW_OBJECT));
                        dbNew.setID(Native.createGuid());
                        dbNew.setName(ebSource.getName());
                        dbNew.setConfiguration(ebSource.getConfiguration());
                        dbNew.setHost(ebSource.getHost());
                        dbNew.setType(ebSource.getType());
                        dbNew.insert();

                        sEmailBoxID = dbNew.getID();
                    }
                    else
                    {
                        sEmailBoxID = eb.getID();
                    }

                    // Get all email messages that should be archived. It is on the other database
                    // again, so close the current transaction and when it's done start a new one.
                    bom.commitTransaction(sTransactionID, true);
                    BSF.unregisterContext(bcContext);

                    List<EmailMessage> lEmails = spqmManager.getEmailMessagesByContextID(ccContainer
                                                                                         .getID());

                    bom.startTransaction(sTransactionID);
                    BSF.registerContext(bcContext);

                    // Note: since we're using GUIDs it can never be that the same message is
                    // archived twice under the dame GUID. So if a GUID already exists in the
                    // archive database it is most likely because of an exception. This means it is
                    // safe to update it.
                    query = new QueryObject("select * from \"CONTEXT_CONTAINER\" where \"CONTEXT_CONTAINER_ID\" = '" +
                                            ccContainer.getID() + "'");
                    query.setResultClass(DBContextContainer.class);

                    DBContextContainer ccNew = (DBContextContainer) query.getObject(bom);

                    if (ccNew == null)
                    {
                        // Do the context container
                        ccNew = new DBContextContainer(new BusObjectConfig(bom, 0,
                                                                           BusObjectConfig.NEW_OBJECT));
                    }
                    ccNew.setID(ccContainer.getID());
                    ccNew.setCompletionDate(ccContainer.getCompleteDate());
                    ccNew.setCreateDate(ccContainer.getCreateDate());
                    ccNew.setEmailBoxID(sEmailBoxID);
                    ccNew.setLastStatusChangeDate(ccContainer.getProcessingStatusChangeDate());
                    ccNew.setProcessingStatus(ccContainer.getProcessingStatus());
                    ccNew.setStatusInformation(ccContainer.getStatusInformation());
                    ccNew.setTriggerDefinition(ccContainer.getTriggerDefinition());

                    // Now do the content of the container
                    for (EmailMessage emMessage : lEmails)
                    {
                        query = new QueryObject("select * from \"EMAIL\" where \"EMAIL_ID\" = '" +
                                                emMessage.getID() + "'");
                        query.setResultClass(DBEmail.class);

                        DBEmail deEmail = (DBEmail) query.getObject(bom);

                        if (deEmail == null)
                        {
                            deEmail = new DBEmail(new BusObjectConfig(bom, 0,
                                                                      BusObjectConfig.NEW_OBJECT));
                        }

                        deEmail.setID(emMessage.getID());
                        deEmail.setContextContainerID(ccNew.getID());
                        deEmail.setContent(emMessage.getRawContent());
                        deEmail.setFrom(emMessage.getFrom());
                        deEmail.setReceiveDate(emMessage.getReceiveDate());
                        deEmail.setSequenceID(emMessage.getSequenceID());
                        deEmail.setSendDate(emMessage.getSendDate());
                        deEmail.setSubject(emMessage.getSubject());
                        deEmail.setTo(emMessage.getTo());
                    }

                    bom.commitTransaction(sTransactionID, true);
                    BSF.unregisterContext(bcContext);

                    // Now that everything is committed we can remove it from the actual database.
                    try
                    {
                        spqmManager.removeContextContainer(ccContainer);
                    }
                    catch (Exception e)
                    {
                        // Basically we have the 2-phase commit problem. So we need to
                        // roll back the changes in the archive database.
                        try
                        {
                            bom.startTransaction(sTransactionID);
                            BSF.registerContext(bcContext);

                            DMLStatement ds = new DMLStatement("DELETE FROM EMAIL WHERE CONTEXT_CONTAINER = '" +
                                                               ccContainer.getID() + "'");
                            ds.execute(bom);

                            ds = new DMLStatement("DELETE FROM CONTEXT_CONTAINER WHERE CONTEXT_CONTAINER_ID = '" +
                                                  ccContainer.getID() + "'");
                            ds.execute(bom);

                            bom.commitTransaction(sTransactionID, true);
                        }
                        catch (Exception e2)
                        {
                            LOG.error(e2,
                                      LogMessages.ERR_ERROR_ROLING_BACK_THE_ARCHIVING_OF_CONTAINER_WITH_ID_0,
                                      ccContainer.getID());
                            bom.abortTransaction(sTransactionID, e2);
                        }
                        LOG.error(e, ArchiverExceptionMessages.ARE_ERROR_ARCHIVING_CONTAINER_0,
                                  ccContainer.getID());
                    }
                }
                catch (Exception e)
                {
                    bom.abortTransaction(sTransactionID, e);

                    throw new ArchiverException(e,
                                                ArchiverExceptionMessages.ARE_ERROR_ARCHIVING_CONTAINER_0,
                                                ccContainer.getID());
                }
            }
        }
        catch (ArchiverException ae)
        {
            throw ae;
        }
        catch (Exception e)
        {
            throw new ArchiverException(e, ArchiverExceptionMessages.ARE_ERROR_ARCHIVING_MESSAGES);
        }
        finally
        {
            if (bcContext != null)
            {
                BSF.unregisterContext(bcContext);
            }
        }
    }

    /**
     * @see  com.cordys.coe.ac.emailio.archive.AbstractArchiver#postInit(int, XPathMetaInfo, String,
     *       IManagedComponent)
     */
    @Override protected void postInit(int iParameters, XPathMetaInfo xmi, String sOrganization,
                                      IManagedComponent mcParent)
                               throws ArchiverException
    {
        // Get the parameter values.
        int iComponent = getXMLParameter("dbconfig");

        if (iComponent == 0)
        {
            throw new ArchiverException(ArchiverExceptionMessages.ARE_COULD_NOT_FIND_THE_DATABASE_CONNECTION_POOL_CONFIGURATION);
        }

        // Fix the name
        Node.setAttribute(iComponent, "name", ARCHIVE_DATABASE);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Creating WsApps configuration");
        }

        try
        {
            WsAppUtil.createConfiguration(iComponent, sOrganization, mcParent);
        }
        catch (WsAppUtilException e)
        {
            throw new ArchiverException(e, e.getMessageObject(), e.getMessageParameters());
        }

        // Check the DB to make sure all tables are there
        checkDatabase();
    }

    /**
     * This method checks the database to make sure all tables are there.
     *
     * @throws  ArchiverException  In case of any exceptions.
     */
    private void checkDatabase()
                        throws ArchiverException
    {
        try
        {
            BusObjectManager bom = WsAppUtil.createBusObjectManager(ARCHIVE_DATABASE);

            bom.startTransaction("dummy");

            BsfContext bcContext = new BsfContext(bom);
            BSF.registerContext(bcContext);

            try
            {
                // Check that the EmailBox table is present.
                QueryObject query = new QueryObject("select * from \"EMAIL_BOX\" where \"EMAILBOX_ID\" = 'dummy'");
                query.setResultClass(DBEmailBox.class);
                query.getObject(bom);

                // Check that the ContextContainer table is present.
                query = new QueryObject("select * from \"CONTEXT_CONTAINER\" where \"CONTEXT_CONTAINER_ID\" = 'dummy'");
                query.setResultClass(DBContextContainer.class);
                query.getObject(bom);

                // Check that the Email table is present.
                query = new QueryObject("select * from \"EMAIL\" where \"EMAIL_ID\" = 'dummy'");
                query.setResultClass(DBEmail.class);
                query.getObject(bom);

                bom.commitTransaction("dummy", true);
            }
            finally
            {
                BSF.unregisterContext(bcContext);
            }
        }
        catch (Exception e)
        {
            throw new ArchiverException(e, ArchiverExceptionMessages.ARE_INVALID_ARCHIVE_DATABASE);
        }
    }
}
