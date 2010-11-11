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
 package com.cordys.coe.ac.emailio.storage.db;

import com.cordys.coe.ac.emailio.EmailIOConnectorConstants;
import com.cordys.coe.ac.emailio.config.EmailBoxFactory;
import com.cordys.coe.ac.emailio.config.IEmailBox;
import com.cordys.coe.ac.emailio.exception.StorageProviderException;
import com.cordys.coe.ac.emailio.localization.LogMessages;
import com.cordys.coe.ac.emailio.localization.StorageProviderExceptionMessages;
import com.cordys.coe.ac.emailio.objects.EmailBox;
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

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;

/**
 * This class wraps the definition of an email box.
 *
 * @author  pgussow
 */
public class DBEmailBox extends DBEmailBoxBase
{
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(DBEmailBox.class);
    /**
     * Holds a static document to use for temporary XML creation.
     */
    private static Document s_dDoc = new Document();

    /**
     * Creates a new DBEmailBox object.
     */
    public DBEmailBox()
    {
        this((BusObjectConfig) null);
    }

    /**
     * Creates a new DBEmailBox object.
     *
     * @param  config  The configuration.
     */
    public DBEmailBox(BusObjectConfig config)
    {
        super(config);
    }

    /**
     * This method returns the EmailBox object which is stored in the database corresponding the
     * passed on email box.
     *
     * @param   ebEmailBox  The email box to find.
     *
     * @return  The emailbox to use.
     */
    public static DBEmailBox getEmailBoxByName(IEmailBox ebEmailBox)
    {
        DBEmailBox ebReturn = null;

        ebReturn = getEmailBoxByName(ebEmailBox.getName());

// if (ebReturn != null) { //We need to check the configuration. If the configuration is different
// we still need to add //a new entry to the database. String sConfiguration =
// getEmailboxConfiguration(ebEmailBox); if (!sConfiguration.equals(ebReturn.getConfiguration())) {
// //Different mailbox, so it needs to be inserted. if (LOG.isDebugEnabled()) { LOG.debug("The
// mailbox was found, but the configuration is different. In order to ensure restarting of a
// container works, "); } } }

        if (ebReturn == null)
        {
            ebReturn = new DBEmailBox();
            ebReturn.setName(ebEmailBox.getName());
            ebReturn.setHost(ebEmailBox.getHost());
            ebReturn.setType(ebEmailBox.getType().name());

            int iTemp = 0;

            try
            {
                iTemp = BSF.getXMLDocument().createElement("dummy");
                ebEmailBox.toXML(iTemp);

                int iEmailBox = Node.getFirstElement(iTemp);

                // NOM.writeToString does not write the proper XML. The xmlns declaration is on a
                // higher level, but NOM fails to write the proper XML. So we'll compensate.
                NOMUtil.fixNamespaceDeclaration(iEmailBox);

                ebReturn.setConfiguration(Node.writeToString(iEmailBox, false));
            }
            finally
            {
                if (iTemp != 0)
                {
                    Node.delete(iTemp);
                }
            }
            ebReturn.insert();
        }

        return ebReturn;
    }

// private static String getEmailboxConfiguration(IEmailBox ebEmailBox)
// {
//
// }

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
     */
    public static BusObjectIterator searchEmailBoxes(String sName, String sHost, String sType,
                                                     String sConfiguration, Cursor cCursor)
    {
        // If compression is being used the LIKE filters won't work.
        if (ContentUtil.usingCompression() && StringUtil.isSet(sConfiguration))
        {
            throw new BsfApplicationRuntimeException(LogMessages.ERR_THE_DB_LAYER_IS_USING_COMPRESSION_SO_THE_LIKE_FILTER_ON_CONFIGURATION_WILL_NOT_WORK);
        }

        WsAppsQueryWrapper waqwSearch = new WsAppsQueryWrapper();
        waqwSearch.select("*").from("EMAIL_BOX");

        // The name of the email box.
        QueryUtils.addOptionalFieldLike(waqwSearch, sName, "BOX_NAME", "EMAIL_BOX");

        // The name of the host
        QueryUtils.addOptionalField(waqwSearch, sHost, "HOST", "EMAIL_BOX");

        // The type of email
        QueryUtils.addOptionalField(waqwSearch, sType, "BOX_TYPE", "EMAIL_BOX");

        // Any part of the configuration
        QueryUtils.addOptionalFieldLike(waqwSearch, sConfiguration, "CONFIGURATION", "EMAIL_BOX");

        waqwSearch.orderBy("BOX_NAME asc");

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
        qo.setResultClass(DBEmailBox.class);

        return qo.getObjects();
    }

    /**
     * This method will create the generic email box wrapper object.
     *
     * @param   bValidate  Whether or not the configuration should be validated.
     *
     * @return  The generic mail box.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    public EmailBox createGenericEmailBox(boolean bValidate)
                                   throws StorageProviderException
    {
        EmailBox ebReturn = new EmailBox();
        ebReturn.makeTransient();

        ebReturn.setName(getName());

        int iNode = 0;

        try
        {
            String sConfiguration = getConfiguration();

            if (sConfiguration.startsWith("<emailbox>"))
            {
                // The xmlns is missing. We'll fix it.
                sConfiguration = "<emailbox xmlns=\"" + EmailIOConnectorConstants.NS_CONFIGURATION +
                                 "\">" + sConfiguration.substring("<emailbox>".length());
            }

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Parsing the configuration:\n" + sConfiguration);
            }
            iNode = s_dDoc.parseString(sConfiguration);

            IEmailBox eb = EmailBoxFactory.createEmailBox(iNode, bValidate);

            ebReturn.setHost(eb.getHost());
            ebReturn.setPort(eb.getPort());
            ebReturn.setUsername(eb.getUsername());
            ebReturn.setType(eb.getType().toString());
            ebReturn.setPollInterval(eb.getPollInterval());

            ebReturn.setConfiguration(sConfiguration);
        }
        catch (Exception e)
        {
            throw new StorageProviderException(e,
                                               StorageProviderExceptionMessages.SPE_ERROR_CREATING_EMAILBOX_FOR_ID_01,
                                               getID(), getName());
        }
        finally
        {
            if (iNode != 0)
            {
                Node.delete(iNode);
            }
        }

        return ebReturn;
    }

    /**
     * This method will make sure that it is decompressed when someone needs it.
     *
     * @return  The configuration of the email box.
     *
     * @see     DBEmailBoxBase#getConfiguration()
     */
    @Override public String getConfiguration()
    {
        String sReturn = super.getConfiguration();

        sReturn = ContentUtil.decompressData(sReturn);

        return sReturn;
    }

    /**
     * This method makes sure the internal data is compressed.
     *
     * @param  sValue  The configuration for the email box.
     *
     * @see    DBEmailBoxBase#setConfiguration(String)
     */
    @Override public void setConfiguration(String sValue)
    {
        sValue = ContentUtil.compressData(sValue);

        super.setConfiguration(sValue);
    }

    /**
     * @see  DBEmailBoxBase#setHost(String)
     */
    @Override public void setHost(String value)
    {
        if (StringUtil.isSet(value) && (value.length() > 150))
        {
            value = value.substring(0, 150);
        }

        super.setHost(value);
    }

    /**
     * @see  com.cordys.cpc.bsf.busobject.BusObject#onBeforeInsert()
     */
    @Override protected void onBeforeInsert()
    {
        if (isNull(ATTR_EMAILBOX_ID))
        {
            setID(Native.createGuid());
        }
    }
}
