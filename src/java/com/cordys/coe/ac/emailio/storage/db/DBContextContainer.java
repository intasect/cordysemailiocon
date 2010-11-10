package com.cordys.coe.ac.emailio.storage.db;

import com.cordys.coe.ac.emailio.config.IEmailBox;
import com.cordys.coe.ac.emailio.config.trigger.ITrigger;
import com.cordys.coe.ac.emailio.exception.StorageProviderException;
import com.cordys.coe.ac.emailio.localization.LogMessages;
import com.cordys.coe.ac.emailio.monitor.RuleContext;
import com.cordys.coe.ac.emailio.monitor.RuleContextContainer;
import com.cordys.coe.ac.emailio.objects.ContextContainer;
import com.cordys.coe.ac.emailio.storage.EProcessingStatus;
import com.cordys.coe.ac.emailio.storage.IEmailStorageProvider;
import com.cordys.coe.ac.emailio.util.NOMUtil;
import com.cordys.coe.ac.emailio.util.QueryUtils;
import com.cordys.coe.ac.emailio.util.StringUtil;
import com.cordys.coe.util.sql.WsAppsQueryWrapper;

import com.cordys.cpc.bsf.busobject.BSF;
import com.cordys.cpc.bsf.busobject.BusObjectConfig;
import com.cordys.cpc.bsf.busobject.BusObjectIterator;
import com.cordys.cpc.bsf.busobject.QueryObject;
import com.cordys.cpc.bsf.busobject.exception.BsfApplicationRuntimeException;
import com.cordys.cpc.bsf.query.Cursor;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.system.Native;

import com.eibus.xml.nom.Node;

import java.util.Date;

/**
 * This class stores the rule context container.
 *
 * @author  pgussow
 */
public class DBContextContainer extends DBContextContainerBase
{
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(DBContextContainer.class);

    /**
     * Creates a new DBContextContainer object.
     */
    public DBContextContainer()
    {
        this((BusObjectConfig) null);
    }

    /**
     * Creates a new DBContextContainer object.
     *
     * @param  config  The configuration.
     */
    public DBContextContainer(BusObjectConfig config)
    {
        super(config);
    }

    /**
     * This method copies the passed on container and it's email messages as a new one.
     *
     * @param  sContextContainerID  The ID of the container.
     * @param  iCopyCount           The amount of copies to make.
     * @param  ccTemplate           The source container to copy.
     * @param  eTemplate            The email template to copy.
     */
    public static void copyContainer(String sContextContainerID, int iCopyCount,
                                     DBContextContainer ccTemplate, DBEmail eTemplate)
    {
        DBContextContainer cc = DBContextContainer.getContextContainerObject(sContextContainerID);

        if (cc != null)
        {
            for (int iCount = 0; iCount < iCopyCount; iCount++)
            {
                DBContextContainer ccNew = new DBContextContainer();
                ccNew.setID(Native.createGuid());

                if ((ccTemplate != null) && !ccTemplate.isNull(ATTR_COMPLETE_DATE))
                {
                    ccNew.setCompletionDate(ccTemplate.getCompletionDate());
                }
                else
                {
                    ccNew.setCompletionDate(cc.getCompletionDate());
                }

                if ((ccTemplate != null) && !ccTemplate.isNull(ATTR_CREATE_DATE))
                {
                    ccNew.setCreateDate(ccTemplate.getCreateDate());
                }
                else
                {
                    ccNew.setCreateDate(cc.getCreateDate());
                }

                if ((ccTemplate != null) && !ccTemplate.isNull(ATTR_EMAILBOX))
                {
                    ccNew.setEmailBoxID(ccTemplate.getEmailBoxID());
                }
                else
                {
                    ccNew.setEmailBoxID(cc.getEmailBoxID());
                }

                if ((ccTemplate != null) && !ccTemplate.isNull(ATTR_PROCESSING_STATUS))
                {
                    ccNew.setProcessingStatus(ccTemplate.getProcessingStatus());
                }
                else
                {
                    ccNew.setProcessingStatus(cc.getProcessingStatus());
                }

                if ((ccTemplate != null) && !ccTemplate.isNull(ATTR_STATUS_CHANGE_DATE))
                {
                    ccNew.setLastStatusChangeDate(ccTemplate.getLastStatusChangeDate());
                }
                else
                {
                    ccNew.setLastStatusChangeDate(cc.getLastStatusChangeDate());
                }

                if ((ccTemplate != null) && !ccTemplate.isNull(ATTR_STATUS_INFORMATION))
                {
                    ccNew.setStatusInformation(ccTemplate.getStatusInformation());
                }
                else
                {
                    ccNew.setStatusInformation(cc.getStatusInformation());
                }

                if ((ccTemplate != null) && !ccTemplate.isNull(ATTR_TRIGGER_DEFINITION))
                {
                    ccNew.setTriggerDefinition(ccTemplate.getTriggerDefinition());
                }
                else
                {
                    ccNew.setTriggerDefinition(cc.getTriggerDefinition());
                }
                ccNew.insert();

                BusObjectIterator boi = DBEmail.getEmailObjectsForContextContainer(sContextContainerID);

                while (boi.hasMoreElements())
                {
                    DBEmail e = (DBEmail) boi.nextElement();
                    DBEmail eNew = new DBEmail();
                    eNew.setContextContainerID(ccNew.getID());

                    if ((eTemplate != null) && !eTemplate.isNull(DBEmail.ATTR_CONTENT))
                    {
                        eNew.setContent(eTemplate.getContent());
                    }
                    else
                    {
                        eNew.setContent(e.getContent());
                    }

                    if ((eTemplate != null) && !eTemplate.isNull(DBEmail.ATTR_FROM_ADDRESS))
                    {
                        eNew.setFrom(eTemplate.getFrom());
                    }
                    else
                    {
                        eNew.setFrom(e.getFrom());
                    }

                    if ((eTemplate != null) && !eTemplate.isNull(DBEmail.ATTR_RECEIVE_DATE))
                    {
                        eNew.setReceiveDate(eTemplate.getReceiveDate());
                    }
                    else
                    {
                        eNew.setReceiveDate(e.getReceiveDate());
                    }

                    if ((eTemplate != null) && !eTemplate.isNull(DBEmail.ATTR_SEND_DATE))
                    {
                        eNew.setSendDate(eTemplate.getSendDate());
                    }
                    else
                    {
                        eNew.setSendDate(e.getSendDate());
                    }

                    if ((eTemplate != null) && !eTemplate.isNull(DBEmail.ATTR_SEQUENCE_ID))
                    {
                        eNew.setSequenceID(eTemplate.getSequenceID());
                    }
                    else
                    {
                        eNew.setSequenceID(e.getSequenceID());
                    }

                    if ((eTemplate != null) && !eTemplate.isNull(DBEmail.ATTR_SUBJECT))
                    {
                        eNew.setSubject(eTemplate.getSubject());
                    }
                    else
                    {
                        eNew.setSubject(e.getSubject());
                    }

                    if ((eTemplate != null) && !eTemplate.isNull(DBEmail.ATTR_TO_ADDRESS))
                    {
                        eNew.setTo(eTemplate.getTo());
                    }
                    else
                    {
                        eNew.setTo(e.getTo());
                    }
                    eNew.insert();
                }
            }
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
     * @param   cCursor                The browsing cursor.
     *
     * @return  The list of matching containers.
     */
    public static BusObjectIterator searchContainers(String sEmailBoxID, String sStatusInformation,
                                                     String sTriggerDefinition,
                                                     String sProcessingStatus, Date dFromCreateDate,
                                                     Date dToCreateDate, Date dFromCompleteDate,
                                                     Date dToCompleteDate,
                                                     Date dFromStatusChangeDate,
                                                     Date dToStatusChangeDate, Cursor cCursor)
    {
        // If compression is being used the LIKE filters won't work.
        if (ContentUtil.usingCompression() &&
                (StringUtil.isSet(sTriggerDefinition) || StringUtil.isSet(sStatusInformation)))
        {
            throw new BsfApplicationRuntimeException(LogMessages.ERR_THE_DB_LAYER_IS_USING_COMPRESSION_SO_THE_LIKE_FILTERS_ON_STATUS_INFORMATION_AND_TRIGGER_DEFINITION_DO_NOT_WORK);
        }

        WsAppsQueryWrapper waqwSearch = new WsAppsQueryWrapper();
        waqwSearch.select("*").from("CONTEXT_CONTAINER");

        // Email box ID. The EmailBox ID can either be a GUID or the name.
        if ((sEmailBoxID != null) && (sEmailBoxID.length() > 0) &&
                (sEmailBoxID.trim().length() > 0))
        {
            DBEmailBox dbeb = DBEmailBox.getEmailBoxByName(sEmailBoxID);

            if (dbeb == null)
            {
                // Try it by ID
                dbeb = DBEmailBox.getEmailBoxObject(sEmailBoxID);
            }

            if (dbeb != null)
            {
                sEmailBoxID = dbeb.getID();
            }

            QueryUtils.addOptionalField(waqwSearch, sEmailBoxID, "EMAILBOX", "CONTEXT_CONTAINER");
        }
        // Status information will always be a like.
        QueryUtils.addOptionalFieldLike(waqwSearch, sStatusInformation, "STATUS_INFORMATION",
                                        "CONTEXT_CONTAINER");
        // Trigger definition will always be a like.
        QueryUtils.addOptionalFieldLike(waqwSearch, sTriggerDefinition, "TRIGGER_DEFINITION",
                                        "CONTEXT_CONTAINER");
        // Processing status
        QueryUtils.addOptionalField(waqwSearch, sProcessingStatus, "PROCESSING_STATUS",
                                    "CONTEXT_CONTAINER");
        // Do the from date
        QueryUtils.addDateSearch(waqwSearch, dFromCreateDate, dToCreateDate, "CREATE_DATE",
                                 "CONTEXT_CONTAINER");
        // Do the completion date.
        QueryUtils.addDateSearch(waqwSearch, dFromCompleteDate, dToCompleteDate, "COMPLETE_DATE",
                                 "CONTEXT_CONTAINER");
        // Do the last status change date.
        QueryUtils.addDateSearch(waqwSearch, dFromStatusChangeDate, dToStatusChangeDate,
                                 "STATUS_CHANGE_DATE", "CONTEXT_CONTAINER");

        waqwSearch.orderBy("STATUS_CHANGE_DATE desc");

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Going to execute this query:\n" + waqwSearch.toDebugString(true));
        }

        // Do the actual query
        QueryObject qo = waqwSearch.createQueryObject();

        if (cCursor != null)
        {
            qo.setCursor(cCursor);
        }
        qo.setResultClass(DBContextContainer.class);

        return qo.getObjects();
    }

    /**
     * This method stores the rule context container in the database.
     *
     * @param   rccContext  The rule context container containing all messages that need to be
     *                      stored.
     * @param   ebEmailBox  The email box for this context.
     * @param   tTrigger    The details of the corresponding trigger.
     *
     * @return  The inserted container.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    public static DBContextContainer storeRuleContextContainer(RuleContextContainer rccContext,
                                                               IEmailBox ebEmailBox,
                                                               ITrigger tTrigger)
                                                        throws StorageProviderException
    {
        DBContextContainer ccReturn = null;

        // Get the proper email box
        DBEmailBox ebMail = DBEmailBox.getEmailBoxByName(ebEmailBox);

        // Create the container.
        ccReturn = new DBContextContainer();
        ccReturn.setID(Native.createGuid());
        ccReturn.setEmailBoxID(ebMail.getID());
        ccReturn.setProcessingStatus(EProcessingStatus.INITIAL.name());

        int iTemp = 0;

        try
        {
            iTemp = BSF.getXMLDocument().createElement("dummy");
            tTrigger.toXML(iTemp);

            int iTriggerDefinition = Node.getFirstElement(iTemp);

            // NOM.writeToString does not write the proper XML. The xmlns declaration is on a
            // higher level,
            // but NOM fails to write the proper XML. So we'll compensate.
            NOMUtil.fixNamespaceDeclaration(iTriggerDefinition);

            ccReturn.setTriggerDefinition(Node.writeToString(iTriggerDefinition, false));
        }
        finally
        {
            if (iTemp != 0)
            {
                Node.delete(iTemp);
            }
        }

        Date dNow = ContentUtil.getNow();
        ccReturn.setCreateDate(dNow);
        ccReturn.setLastStatusChangeDate(dNow);

        ccReturn.insert();

        // Now do all the individual messages.
        int iIndex = 1;

        for (RuleContext rcContext : rccContext)
        {
            DBEmail eEmail = new DBEmail();
            eEmail.setID(Native.createGuid());
            eEmail.setContextContainerID(ccReturn.getID());
            eEmail.setSequenceID(iIndex);
            eEmail.addRuleContext(rcContext);
            eEmail.insert();
        }

        return ccReturn;
    }

    /**
     * This method adds the details for this container to the container detail XML.
     *
     * @param  parentNode  The parent node.
     */
    public void addContainerDetailXML(int parentNode)
    {
        Node.createElementWithParentNS(IEmailStorageProvider.ELEMENT_ID, getID(), parentNode);
    }

    /**
     * This method will return a new ContextContainer which is used in the UIs.
     *
     * @return  A new ContextContainer which is used in the UIs.
     */
    public ContextContainer createGenericContextContainer()
    {
        ContextContainer ccReturn = new ContextContainer();
        ccReturn.makeTransient();

        ccReturn.setID(getID());
        ccReturn.setName(loadEMAILBOXObject().getName());
        ccReturn.setEmailBoxID(getEmailBoxID());
        ccReturn.setEmailBoxName(ccReturn.getName());
        ccReturn.setCompleteDate(getCompletionDate());
        ccReturn.setCreateDate(getCreateDate());
        ccReturn.setProcessingStatus(getProcessingStatus());
        ccReturn.setProcessingStatusChangeDate(getLastStatusChangeDate());
        ccReturn.setStatusInformation(getStatusInformation());
        ccReturn.setTriggerDefinition(getTriggerDefinition());

        return ccReturn;
    }

    /**
     * @see  DBContextContainerBase#getStatusInformation()
     */
    @Override public String getStatusInformation()
    {
        String sReturn = super.getStatusInformation();

        sReturn = ContentUtil.decompressData(sReturn);

        return sReturn;
    }

    /**
     * @see  DBContextContainerBase#getTriggerDefinition()
     */
    @Override public String getTriggerDefinition()
    {
        String sReturn = super.getTriggerDefinition();

        sReturn = ContentUtil.decompressData(sReturn);

        return sReturn;
    }

    /**
     * @see  DBContextContainerBase#setProcessingStatus(String)
     */
    @Override public void setProcessingStatus(String value)
    {
        super.setProcessingStatus(value);

        Date dNow = ContentUtil.getNow();

        setLastStatusChangeDate(dNow);

        if (EProcessingStatus.COMPLETED.name().equals(value))
        {
            setCompletionDate(dNow);
        }
    }

    /**
     * @see  DBContextContainerBase#setStatusInformation(String)
     */
    @Override public void setStatusInformation(String sValue)
    {
        sValue = ContentUtil.compressData(sValue);

        super.setStatusInformation(sValue);
    }

    /**
     * @see  DBContextContainerBase#setTriggerDefinition(String)
     */
    @Override public void setTriggerDefinition(String sValue)
    {
        sValue = ContentUtil.compressData(sValue);

        super.setTriggerDefinition(sValue);
    }

    /**
     * @see  com.cordys.cpc.bsf.busobject.BusObject#onBeforeInsert()
     */
    @Override protected void onBeforeInsert()
    {
        if (isNull(ATTR_CONTEXT_CONTAINER_ID))
        {
            setID(Native.createGuid());
        }
    }
}
