package com.cordys.coe.ac.emailio.storage.db;

import com.cordys.coe.ac.emailio.config.IEmailBox;
import com.cordys.coe.ac.emailio.config.trigger.ITrigger;
import com.cordys.coe.ac.emailio.exception.StorageProviderException;
import com.cordys.coe.ac.emailio.localization.LogMessages;
import com.cordys.coe.ac.emailio.localization.StorageProviderExceptionMessages;
import com.cordys.coe.ac.emailio.objects.TriggerDefinition;
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

/**
 * This class stores the one-time only triggers.
 *
 * @author  pgussow
 */
public class DBTrigger extends DBTriggerBase
{
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(DBTrigger.class);

    /**
     * Creates a new DBTrigger object.
     */
    public DBTrigger()
    {
        this((BusObjectConfig) null);
    }

    /**
     * Creates a new DBTrigger object.
     *
     * @param  config  The configuration.
     */
    public DBTrigger(BusObjectConfig config)
    {
        super(config);
    }

    /**
     * This method returns the trigger which matches the given criteria.
     *
     * @param   sName        Holds the criteria for the name of the trigger.
     * @param   sEmailBoxID  Holds the criteria for the ID or name of the email box.
     * @param   sDefinition  Holds the criteria for the configuration of the trigger. This criteria
     *                       is interpreted as a 'like' operator.
     * @param   cCursor      The cursor to use.
     *
     * @return  The list of triggers that match the given criteria.
     */
    public static BusObjectIterator searchTriggerDefinitions(String sName, String sEmailBoxID,
                                                             String sDefinition, Cursor cCursor)
    {
        // If compression is being used the LIKE filters won't work.
        if (ContentUtil.usingCompression() && StringUtil.isSet(sDefinition))
        {
            throw new BsfApplicationRuntimeException(LogMessages.ERR_THE_DB_LAYER_IS_USING_COMPRESSION_SO_THE_LIKE_FILTER_ON_THE_DEFINITION_WILL_NOT_WORK);
        }

        WsAppsQueryWrapper waqwSearch = new WsAppsQueryWrapper();
        waqwSearch.select("*").from("TRIGGER_STORE");

        // The name of the trigger
        QueryUtils.addOptionalField(waqwSearch, sName, "TRIGGER_NAME", "TRIGGER_STORE");

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

            QueryUtils.addOptionalField(waqwSearch, sEmailBoxID, "EMAILBOX", "TRIGGER_STORE");
        }

        // The actual definition of the trigger.
        QueryUtils.addOptionalFieldLike(waqwSearch, sDefinition, "CONTENT", "TRIGGER_STORE");

        waqwSearch.orderBy("EMAILBOX, TRIGGER_NAME");

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
        qo.setResultClass(DBTrigger.class);

        return qo.getObjects();
    }

    /**
     * This method stores the current trigger.
     *
     * @param   tTrigger    The trigger to store.
     * @param   ebEmailBox  The parent email box.
     *
     * @return  The inserted trigger object.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    public static DBTrigger storeTrigger(ITrigger tTrigger, IEmailBox ebEmailBox)
                                  throws StorageProviderException
    {
        DBTrigger tReturn = null;

        // TODO: What if the trigger is already persisted?
        DBEmailBox eb = DBEmailBox.getEmailBoxByName(ebEmailBox);

        if (eb == null)
        {
            throw new StorageProviderException(StorageProviderExceptionMessages.SPE_COULD_NOT_FIND_THE_EMAILBOX_FOR_NAME,
                                               ebEmailBox.getName());
        }
        tReturn = new DBTrigger();
        tReturn.setID(Native.createGuid());
        tReturn.setEmailBoxID(eb.getID());
        tReturn.setName(tTrigger.getName());

        int iTemp = 0;

        try
        {
            iTemp = BSF.getXMLDocument().createElement("dummy");
            tTrigger.toXML(iTemp);
            tReturn.setContent(Node.writeToString(Node.getFirstElement(iTemp), false));
        }
        finally
        {
            if (iTemp != 0)
            {
                Node.delete(iTemp);
            }
        }

        tReturn.insert();

        return tReturn;
    }

    /**
     * This method creates the generic trigger definition from the DB trigger.
     *
     * @return  The trigger definition.
     */
    public TriggerDefinition createGenericTriggerDefinition()
    {
        TriggerDefinition tdReturn = new TriggerDefinition();
        tdReturn.makeTransient();

        tdReturn.setID(getID());
        tdReturn.setEmailBoxID(getEmailBoxID());
        tdReturn.setName(getName());
        tdReturn.setDefinition(getContent());

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Created trigger from database with this configuration:\n" + getContent());
        }

        return tdReturn;
    }

    /**
     * @see  DBTriggerBase#getContent()
     */
    @Override public String getContent()
    {
        String sReturn = super.getContent();

        sReturn = ContentUtil.decompressData(sReturn);

        return sReturn;
    }

    /**
     * @see  DBTriggerBase#setContent(String)
     */
    @Override public void setContent(String sValue)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Setting trigger:\n" + sValue);
        }

        sValue = ContentUtil.compressData(sValue);

        super.setContent(sValue);
    }

    /**
     * @see  com.cordys.cpc.bsf.busobject.BusObject#onBeforeInsert()
     */
    @Override protected void onBeforeInsert()
    {
        if (isNull(ATTR_TRIGGER_ID))
        {
            setID(Native.createGuid());
        }
    }
}
