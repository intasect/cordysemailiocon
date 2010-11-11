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
 package com.cordys.coe.ac.emailio.method;

import com.cordys.coe.ac.emailio.EmailIOConnectorConstants;
import com.cordys.coe.ac.emailio.archive.ArchiverFactory;
import com.cordys.coe.ac.emailio.archive.IArchiver;
import com.cordys.coe.ac.emailio.config.IEmailIOConfiguration;
import com.cordys.coe.ac.emailio.exception.ArchiverException;
import com.cordys.coe.ac.emailio.exception.EmailIOException;
import com.cordys.coe.ac.emailio.exception.StorageProviderException;
import com.cordys.coe.ac.emailio.localization.ArchiverExceptionMessages;
import com.cordys.coe.ac.emailio.localization.EmailIOExceptionMessages;
import com.cordys.coe.ac.emailio.objects.ContextContainer;
import com.cordys.coe.ac.emailio.objects.IStorageProviderQueryManager;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.cordys.cpc.bsf.query.Cursor;

import com.eibus.soap.BodyBlock;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;

/**
 * This class holds the implementation of the ArchiveContainers method.
 *
 * @author  pgussow
 */
public class ArchiveContainers extends BaseMethod
{
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(ArchiveContainers.class);
    /**
     * Holds the namespace prefix mappings to use.
     */
    private XPathMetaInfo m_xmi;

    /**
     * Constructor.
     *
     * @param  bbRequest   The request bodyblock.
     * @param  bbResponse  The response bodyblock.
     * @param  iecConfig   The configuration of the connector.
     */
    public ArchiveContainers(BodyBlock bbRequest, BodyBlock bbResponse,
                             IEmailIOConfiguration iecConfig)
    {
        super(bbRequest, bbResponse, iecConfig);

        m_xmi = new XPathMetaInfo();
        m_xmi.addNamespaceBinding("ns", EmailIOConnectorConstants.NS_DYNAMIC);
    }

    /**
     * @see  com.cordys.coe.ac.emailio.method.BaseMethod#execute()
     */
    @Override public void execute()
                           throws EmailIOException
    {
        int iRequest = getRequest().getXMLNode();

        // Make sure the information passed on in correct
        validateRequest(iRequest);

        int iArchiver = XPathHelper.selectSingleNode(iRequest, "ns:archiver", m_xmi);
        IArchiver aArchiver = ArchiverFactory.createArchiver(iArchiver, getConfiguration(),
                                                             getRequest().getSOAPTransaction()
                                                             .getIdentity().getUserOrganization(),
                                                             getConfiguration()
                                                             .getManagedComponent());

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Searching for the containers that should be archived");
        }

        List<ContextContainer> lccContainers = searchContextContainers(iRequest);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Found " + lccContainers.size() +
                      " containers to archive. Starting the actual archiving now.");
        }

        // The archiving will run in the back ground in the future. For now it'll be synchronous.
        Date dStartTime = new Date();

        if (lccContainers.size() > 0)
        {
            aArchiver.doArchive(lccContainers, getConfiguration().getStorageProviderQueryManager());
        }

        Date dEndTime = new Date();

        // Build up the response.
        int iResponse = getResponse().getXMLNode();
        int iTuple = Node.createElementWithParentNS("tuple", null, iResponse);
        int iOld = Node.createElementWithParentNS("old", null, iTuple);
        int iArchive = Node.createElementWithParentNS("archiver", null, iOld);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        Node.createElementWithParentNS("status", "done", iArchive);
        Node.createElementWithParentNS("starttime", sdf.format(dStartTime), iArchive);
        Node.createElementWithParentNS("endtime", sdf.format(dEndTime), iArchive);
        Node.createElementWithParentNS("duration",
                                       String.valueOf(dEndTime.getTime() - dStartTime.getTime()),
                                       iArchive);
    }

    /**
     * This method executes the search based on the passed on parameters.
     *
     * @param   iRequest  The request XML.
     *
     * @return  The list of containers that should be archived.
     *
     * @throws  ArchiverException  In case of any exceptions.
     */
    private List<ContextContainer> searchContextContainers(int iRequest)
                                                    throws ArchiverException
    {
        IStorageProviderQueryManager spqm = getConfiguration().getStorageProviderQueryManager();

        String sEmailBoxID = XPathHelper.getStringValue(iRequest, "ns:search/ns:EmailBoxID", m_xmi,
                                                        "");
        String sStatusInformation = "";
        String sTriggerDefinition = "";
        String sProcessingStatus = XPathHelper.getStringValue(iRequest, "ns:search/ns:Status",
                                                              m_xmi, "COMPLETED");
        Date dFromDate = XPathHelper.getDateValue(iRequest, "ns:search/ns:FromCreateDate", m_xmi,
                                                  null);
        Date dToDate = XPathHelper.getDateValue(iRequest, "ns:search/ns:ToCreateDate", m_xmi, null);
        Date dFromCompleteDate = XPathHelper.getDateValue(iRequest, "ns:search/ns:FromCompleteDate",
                                                          m_xmi, null);
        Date dToCompleteDate = XPathHelper.getDateValue(iRequest, "ns:search/ns:ToCompleteDate",
                                                        m_xmi, null);
        Date dFromStatusChangeDate = XPathHelper.getDateValue(iRequest,
                                                              "ns:search/ns:FromStatusChangeDate",
                                                              m_xmi, null);
        Date dToStatusChangeDate = XPathHelper.getDateValue(iRequest,
                                                            "ns:search/ns:ToStatusChangeDate",
                                                            m_xmi, null);
        Cursor cCursor = null;

        try
        {
            return spqm.searchContainers(sEmailBoxID, sStatusInformation, sTriggerDefinition,
                                         sProcessingStatus, dFromDate, dToDate, dFromCompleteDate,
                                         dToCompleteDate, dFromStatusChangeDate,
                                         dToStatusChangeDate, cCursor);
        }
        catch (StorageProviderException e)
        {
            throw new ArchiverException(e,
                                        ArchiverExceptionMessages.ERROR_GETTING_THE_CONTAINERS_TO_ARCHIVE,
                                        sEmailBoxID, sProcessingStatus, dFromDate, dToDate,
                                        dFromCompleteDate, dToCompleteDate, dFromStatusChangeDate,
                                        dToStatusChangeDate);
        }
    }

    /**
     * This method validates the input tyo make sure all information is there.
     *
     * @param   iRequest  The request XML.
     *
     * @throws  EmailIOException  In case the request is invalid.
     */
    private void validateRequest(int iRequest)
                          throws EmailIOException
    {
        if (XPathHelper.selectSingleNode(iRequest, "ns:search", m_xmi) == 0)
        {
            throw new EmailIOException(EmailIOExceptionMessages.EIOE_MISSING_PARAMETER_SEARCH);
        }

        int iArchiver = XPathHelper.selectSingleNode(iRequest, "ns:archiver", m_xmi);

        if (iArchiver == 0)
        {
            throw new EmailIOException(EmailIOExceptionMessages.EIOE_MISSING_PARAMETER_ARCHIVER);
        }

        if (XPathHelper.selectSingleNode(iArchiver, "ns:class", m_xmi) == 0)
        {
            throw new EmailIOException(EmailIOExceptionMessages.EIOE_MISSING_PARAMETER_ARCHIVERCLASS);
        }
    }
}
