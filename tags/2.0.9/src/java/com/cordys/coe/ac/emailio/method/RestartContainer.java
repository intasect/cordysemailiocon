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

import com.cordys.coe.ac.emailio.config.EmailBoxFactory;
import com.cordys.coe.ac.emailio.config.IEmailBox;
import com.cordys.coe.ac.emailio.config.IEmailIOConfiguration;
import com.cordys.coe.ac.emailio.config.trigger.ITrigger;
import com.cordys.coe.ac.emailio.config.trigger.TriggerFactory;
import com.cordys.coe.ac.emailio.exception.EmailIOException;
import com.cordys.coe.ac.emailio.exception.InboundEmailException;
import com.cordys.coe.ac.emailio.localization.InboundEmailExceptionMessages;
import com.cordys.coe.ac.emailio.monitor.IJMXEmailBoxPoller;
import com.cordys.coe.ac.emailio.monitor.RuleContext;
import com.cordys.coe.ac.emailio.monitor.RuleContextContainer;
import com.cordys.coe.ac.emailio.objects.ContextContainer;
import com.cordys.coe.ac.emailio.objects.EmailBox;
import com.cordys.coe.ac.emailio.objects.EmailMessage;
import com.cordys.coe.ac.emailio.objects.IStorageProviderQueryManager;
import com.cordys.coe.ac.emailio.outbound.ISMIMEConfiguration;
import com.cordys.coe.ac.emailio.storage.EProcessingStatus;
import com.cordys.coe.ac.emailio.storage.IEmailStorageProvider;
import com.cordys.coe.ac.emailio.triggerengine.tcb.ITriggerContextBuilder;
import com.cordys.coe.ac.emailio.triggerengine.tcb.TriggerContextBuilderFactory;
import com.cordys.coe.ac.emailio.triggerengine.work.ITriggerWork;
import com.cordys.coe.ac.emailio.triggerengine.work.TriggerWorkFactory;
import com.cordys.coe.ac.emailio.util.MailMessageUtil;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.soap.BodyBlock;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

import java.util.List;

import javax.mail.internet.MimeMessage;

/**
 * This class handles the request to restart the processing for a certain container. A container can
 * only be restarted if the status is MESSAGE_ERROR or ACTION_ERROR.
 *
 * @author  pgussow
 */
public class RestartContainer extends BaseMethod
{
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(RestartContainer.class);
    /**
     * Indicates whether or not the reprocessing was successful.
     */
    public boolean m_bProcessingError;
    /**
     * Holds the context for the reprocessing error.
     */
    private String m_sContext;
    /**
     * Holds the exception that occurred while reprocessing.
     */
    private Throwable m_tException;

    /**
     * Creates a new RestartContainer object.
     *
     * @param  bbRequest   The request bodyblock.
     * @param  bbResponse  The response bodyblock.
     * @param  iecConfig   The configuration of the connector.
     */
    public RestartContainer(BodyBlock bbRequest, BodyBlock bbResponse,
                            IEmailIOConfiguration iecConfig)
    {
        super(bbRequest, bbResponse, iecConfig);
    }

    /**
     * This method executed the requested SOAP method.
     *
     * @throws  EmailIOException  In case of any processing errors.
     *
     * @see     com.cordys.coe.ac.emailio.method.BaseMethod#execute()
     */
    @Override public void execute()
                           throws EmailIOException
    {
        int iRequest = getRequest().getXMLNode();

        XPathMetaInfo xmi = new XPathMetaInfo();
        xmi.addNamespaceBinding("ns", Node.getNamespaceURI(iRequest));

        String sContextContainerID = XPathHelper.getStringValue(iRequest,
                                                                "./ns:contextcontainerid/text()",
                                                                xmi, "");

        if (sContextContainerID.length() == 0)
        {
            throw new InboundEmailException(InboundEmailExceptionMessages.IEE_PARAMETER_NOT_FILLED_CONTEXT_CONTAINER_ID);
        }

        // Get the query manager
        IStorageProviderQueryManager spqm = getConfiguration().getStorageProviderQueryManager();

        if (spqm == null)
        {
            throw new InboundEmailException(InboundEmailExceptionMessages.IEE_THIS_STORAGE_PROVIDER_DOES_NOT_SUPPORT_QUERIES);
        }

        ContextContainer cc = spqm.getContextContainer(sContextContainerID);

        if (cc == null)
        {
            throw new InboundEmailException(InboundEmailExceptionMessages.IEE_COULD_NOT_FIND_THE_CONTAINER_WITH_ID,
                                            sContextContainerID);
        }

        EProcessingStatus ps = EProcessingStatus.valueOf(cc.getProcessingStatus());

        if (ps != EProcessingStatus.MESSAGE_ERROR)
        {
            throw new InboundEmailException(InboundEmailExceptionMessages.IEE_CONTEXT_CONTAINERS_CANNOT_BE_RESTARTED,
                                            cc.getID(), ps);
        }

        List<EmailMessage> lEmails = spqm.getEmailMessagesByContextID(cc.getID());

        if ((lEmails == null) || (lEmails.size() == 0))
        {
            throw new InboundEmailException(InboundEmailExceptionMessages.IEE_THERE_ARE_NO_EMAILS_IN_CONTEXT_CONTAINER,
                                            sContextContainerID);
        }

        EmailBox eb = spqm.getEmailBox(cc.getEmailBoxID(), false);

        if (eb == null)
        {
            throw new InboundEmailException(InboundEmailExceptionMessages.IEE_COULD_NOT_FIND_THE_EMAILBOX_WITH_ID,
                                            cc.getEmailBoxID());
        }

        // Recreate and get the proper objects. First we'll get the proper email box.
        IEmailBox ebEmailBox = getConfiguration().getEmailBox(eb.getName());
        Document dDoc = Node.getDocument(getRequest().getXMLNode());

        if (ebEmailBox == null)
        {
            // This means that the actual email box is no longer used. But we still need to restart
            // the currently selected container. So we'll recreate it from the configuration.
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Recreating EmailBox from database");
            }

            int iNode = 0;

            try
            {
                iNode = dDoc.parseString(eb.getConfiguration());
                ebEmailBox = EmailBoxFactory.createEmailBox(iNode,
                                                            getConfiguration()
                                                            .getStorageProviderConfiguration(),
                                                            getConfiguration().getSOAPProcessorDN(),
                                                            false, false);
            }
            catch (Exception e)
            {
                throw new InboundEmailException(e,
                                                InboundEmailExceptionMessages.IEE_ERROR_RECREATING_EMAILBOX_0,
                                                eb.getName());
            }
            finally
            {
                if (iNode != 0)
                {
                    Node.delete(iNode);
                }
            }
        }

        // Now recreate the trigger definition.
        int iNode = 0;
        ITrigger tTrigger = null;

        try
        {
            iNode = dDoc.parseString(cc.getTriggerDefinition());

            tTrigger = TriggerFactory.createTrigger(iNode, ebEmailBox);
        }
        catch (Exception e)
        {
            throw new InboundEmailException(e,
                                            InboundEmailExceptionMessages.IEE_ERROR_CREATING_TRIGGER_OBJECT_FROM_DEFINITION,
                                            cc.getTriggerDefinition());
        }
        finally
        {
            if (iNode != 0)
            {
                Node.delete(iNode);
            }
        }

        // Now we have all the information, so let's build up the environment.
        restartContainer(cc, lEmails, ebEmailBox, tTrigger, getConfiguration());
    }

    /**
     * This method will actually reprocess the given container.
     *
     * @param   cc          The context container to restart.
     * @param   emails      The list of actual email messages.
     * @param   ebEmailBox  The corresponding email box.
     * @param   tTrigger    The definition of the trigger to execute.
     * @param   scConfig    The configuration for the S/MIME details
     *
     * @throws  InboundEmailException  In case the processing fails.
     */
    private void restartContainer(ContextContainer cc, List<EmailMessage> emails,
                                  IEmailBox ebEmailBox, ITrigger tTrigger,
                                  ISMIMEConfiguration scConfig)
                           throws InboundEmailException
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Restarting container " + cc.getID() + " with status " +
                      cc.getProcessingStatus());
        }

        try
        {
            // Build up the Rule Context container.
            RuleContextContainer rccContext = new RuleContextContainer();
            rccContext.setStorageID(cc.getID());

            for (EmailMessage emMessage : emails)
            {
                MimeMessage mm = MailMessageUtil.createMimeMessage(emMessage.getRawContent());
                RuleContext rcContext = new RuleContext(mm);
                ITriggerContextBuilder tcb = TriggerContextBuilderFactory
                                             .createTriggerContextBuilder(rcContext, mm, tTrigger,
                                                                          scConfig);

                // Build up the actual context.
                if (tcb.processMessage())
                {
                    rccContext.add(rcContext);
                }
                else
                {
                    throw new InboundEmailException(InboundEmailExceptionMessages.IEE_THERE_WAS_A_PROBLEM_REPROCESSING_THE_MESSAGE_FOR_AN_UNKNOWN_REASON_THE_MAIL_DID_NOT_MATCH_THE_ORIGINAL_TRIGGER);
                }
            }

            // Now we need to add the storage details to the context container.
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Getting container detail XML");
            }

            IEmailStorageProvider esp = getConfiguration().getStorageProvider();
            int containerDetailXML = esp.getContainerDetailXML(cc.getID());

            rccContext.addContainerDetailsXML(containerDetailXML);

            // Create the actual work object.
            ITriggerWork tw = TriggerWorkFactory.createTriggerWork(rccContext,
                                                                   tTrigger.getMessage(), tTrigger,
                                                                   getConfiguration()
                                                                   .getConnector(),
                                                                   new DummyJMXCounter(),
                                                                   ebEmailBox);

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Re-executing the work");
            }

            // Resend the original message.
            tw.execute();

            if (m_bProcessingError == true)
            {
                throw new InboundEmailException(m_tException,
                                                InboundEmailExceptionMessages.IEE_ERROR_REPROCESSING_THE_CONTAINER_WITH_ID,
                                                cc.getID(), m_sContext);
            }
        }
        catch (InboundEmailException iee)
        {
            throw iee;
        }
        catch (Exception e)
        {
            throw new InboundEmailException(e,
                                            InboundEmailExceptionMessages.IEE_GENERAL_ERROR_REPROCESSING_CONTAINER_WITH_ID,
                                            cc.getID());
        }
    }

    /**
     * This implementation just catches the process errors.
     *
     * @author  pgussow
     */
    public class DummyJMXCounter
        implements IJMXEmailBoxPoller
    {
        /**
         * @see  com.cordys.coe.ac.emailio.monitor.IJMXEmailBoxPoller#incNrOfMessagesFailed(long)
         */
        @Override public void incNrOfMessagesFailed(long amount)
        {
        }

        /**
         * @see  com.cordys.coe.ac.emailio.monitor.IJMXEmailBoxPoller#incNrOfMessagesIgnored(long)
         */
        @Override public void incNrOfMessagesIgnored(long amount)
        {
        }

        /**
         * @see  com.cordys.coe.ac.emailio.monitor.IJMXEmailBoxPoller#incNrOfMessagesProcessed(long)
         */
        @Override public void incNrOfMessagesProcessed(long amount)
        {
        }

        /**
         * @see  com.cordys.coe.ac.emailio.monitor.IJMXEmailBoxPoller#incNrOfMessagesSuccess(long)
         */
        @Override public void incNrOfMessagesSuccess(long amount)
        {
        }

        /**
         * This method can be used to send out an alert that a specific message failed to process.
         *
         * @param  tException  The exception that occurred.
         * @param  sContext    The list with all context information.
         *
         * @see    com.cordys.coe.ac.emailio.monitor.IJMXEmailBoxPoller#notifyProcessingError(java.lang.Throwable,
         *         java.lang.String)
         */
        @Override public void notifyProcessingError(Throwable tException, String sContext)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("The trigger work reported an error again. Context:\n" + sContext,
                          tException);
            }

            m_bProcessingError = true;
            m_tException = tException;
            m_sContext = sContext;
        }

        /**
         * @see  com.cordys.coe.ac.emailio.monitor.IJMXEmailBoxPoller#reset()
         */
        @Override public void reset()
        {
        }
    }
}
