package com.cordys.coe.ac.emailio.storage.db;

import com.cordys.coe.ac.emailio.exception.StorageProviderException;
import com.cordys.coe.ac.emailio.localization.StorageProviderExceptionMessages;
import com.cordys.coe.ac.emailio.monitor.RuleContext;
import com.cordys.coe.ac.emailio.objects.EmailMessage;
import com.cordys.coe.ac.emailio.storage.IEmailStorageProvider;
import com.cordys.coe.ac.emailio.util.QueryUtils;
import com.cordys.coe.ac.emailio.util.StringUtil;
import com.cordys.coe.util.sql.WsAppsQueryWrapper;

import com.cordys.cpc.bsf.busobject.BusObjectConfig;
import com.cordys.cpc.bsf.busobject.BusObjectIterator;
import com.cordys.cpc.bsf.busobject.QueryObject;
import com.cordys.cpc.bsf.query.Cursor;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.system.Native;

import com.eibus.xml.nom.Node;

import java.io.ByteArrayOutputStream;

import java.util.Date;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;

/**
 * This class stored actual emails.
 *
 * @author  pgussow
 */
public class DBEmail extends DBEmailBase
{
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(DBEmail.class);

    /**
     * Creates a new DBEmail object.
     */
    public DBEmail()
    {
        this((BusObjectConfig) null);
    }

    /**
     * Creates a new Email object.
     *
     * @param  config  The configuration.
     */
    public DBEmail(BusObjectConfig config)
    {
        super(config);
    }

    /**
     * This method searches for email messages that match the given criteria.
     *
     * @param   sContextContainerID  Holds the criteria for the subject.
     * @param   sFrom                Holds the criteria for the from. This criteria is interpreted
     *                               as a 'like' operator.
     * @param   sTo                  Holds the criteria for the to. This criteria is interpreted as
     *                               a 'like' operator.
     * @param   sSubject             Holds the criteria for the subject. This criteria is
     *                               interpreted as a 'like' operator.
     * @param   dFromSendDate        Holds the criteria for the from date (send date).
     * @param   dToSendDate          Holds the criteria for the to date (send date).
     * @param   dFromReceiveDate     Holds the criteria for the from date (receive date).
     * @param   dToReceiveDate       Holds the criteria for the to date (receive date).
     * @param   cCursor              Holds the current cursor.
     *
     * @return  The list of email messages matching the given criteria.
     */
    public static BusObjectIterator searchEmailMessages(String sContextContainerID, String sFrom,
                                                        String sTo, String sSubject,
                                                        Date dFromSendDate, Date dToSendDate,
                                                        Date dFromReceiveDate, Date dToReceiveDate,
                                                        Cursor cCursor)
    {
        WsAppsQueryWrapper waqwSearch = new WsAppsQueryWrapper();
        waqwSearch.select("*").from("EMAIL");

        // The ID of the context container.
        QueryUtils.addOptionalField(waqwSearch, sContextContainerID, "CONTEXT_CONTAINER", "EMAIL");

        // Any part of the from address
        QueryUtils.addOptionalFieldLike(waqwSearch, sFrom, "FROM_ADDRESS", "EMAIL");

        // Any part of the to address
        QueryUtils.addOptionalFieldLike(waqwSearch, sTo, "TO_ADDRESS", "EMAIL");

        // Any part of the subject
        QueryUtils.addOptionalFieldLike(waqwSearch, sSubject, "SUBJECT", "EMAIL");

        // The date on which the mail was sent (filled by the sending server).
        QueryUtils.addDateSearch(waqwSearch, dFromSendDate, dToSendDate, "SEND_DATE", "EMAIL");

        // The date on which the mail was received (filled by the inbound email connector).
        QueryUtils.addDateSearch(waqwSearch, dFromReceiveDate, dToReceiveDate, "RECEIVE_DATE",
                                 "EMAIL");

        waqwSearch.orderBy("SEND_DATE desc");

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
        qo.setResultClass(DBEmail.class);

        return qo.getObjects();
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
     * This method parses the current context and adds the email to the database.
     *
     * @param   rcContext  The rule context.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    public void addRuleContext(RuleContext rcContext)
                        throws StorageProviderException
    {
        Message mMessage = rcContext.getMessage();

        try
        {
            setSendDate(mMessage.getSentDate());
        }
        catch (MessagingException e)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Error getting the send date", e);
            }
        }

        try
        {
            if (mMessage.getReceivedDate() != null)
            {
                setReceiveDate(mMessage.getReceivedDate());
            }
            else
            {
                setReceiveDate(new Date());
            }
        }
        catch (MessagingException e)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Error getting the received date", e);
            }
        }

        try
        {
            // It could be that the mail has no subject and thus this value *could* be null.
            // If we'd return null then a regex would fail because of a null pointer. So that's
            // why in case of NULL the subject is returned as an empty string.
            setSubject((mMessage.getSubject() != null) ? mMessage.getSubject() : "");
        }
        catch (MessagingException e)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Error getting the subject", e);
            }
        }

        try
        {
            StringBuilder sbTemp = new StringBuilder(2048);
            Address[] ae = mMessage.getFrom();

            for (Address a : ae)
            {
                sbTemp.append(a.toString());
                sbTemp.append(";");
            }
            setFrom(sbTemp.toString());
        }
        catch (MessagingException e)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Error getting the from addresses", e);
            }
        }

        try
        {
            StringBuilder sbTemp = new StringBuilder(2048);
            Address[] ae = mMessage.getAllRecipients();

            for (Address a : ae)
            {
                sbTemp.append(a.toString());
                sbTemp.append(";");
            }
            setTo(sbTemp.toString());
        }
        catch (MessagingException e)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Error getting the to addresses", e);
            }
        }

        // Now do the content of the amil itself.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try
        {
            mMessage.writeTo(baos);
            setContent(baos.toString());
        }
        catch (Exception e)
        {
            throw new StorageProviderException(e,
                                               StorageProviderExceptionMessages.SPE_ERROR_STORING_THE_EMAIL_CONTENT);
        }
    }

    /**
     * This method will create the EmailMessage instance based on the message read from the
     * database.
     *
     * @return  The created EmailMessage object.
     */
    public EmailMessage createGenericEmailMessage()
    {
        EmailMessage emReturn = new EmailMessage();
        emReturn.makeTransient();

        emReturn.setID(getID());
        emReturn.setSequenceID(getSequenceID());
        emReturn.setContextContainerID(getContextContainerID());
        emReturn.setFrom(getFrom());
        emReturn.setTo(getTo());
        emReturn.setSubject(getSubject());
        emReturn.setSendDate(getSendDate());
        emReturn.setReceiveDate(getReceiveDate());
        emReturn.setRawContent(getContent());

        return emReturn;
    }

    /**
     * @see  DBEmailBase#getContent()
     */
    @Override public String getContent()
    {
        String sReturn = super.getContent();

        sReturn = ContentUtil.decompressData(sReturn);

        return sReturn;
    }

    /**
     * @see  DBEmailBase#setContent(String)
     */
    @Override public void setContent(String sValue)
    {
        sValue = ContentUtil.compressData(sValue);

        super.setContent(sValue);
    }

    /**
     * @see  DBEmailBase#setFrom(String)
     */
    @Override public void setFrom(String value)
    {
        if (StringUtil.isSet(value) && (value.length() > 200))
        {
            value = value.substring(0, 200);
        }

        super.setFrom(value);
    }

    /**
     * @see  DBEmailBase#setSubject(String)
     */
    @Override public void setSubject(String value)
    {
        if (StringUtil.isSet(value) && (value.length() > 200))
        {
            value = value.substring(0, 200);
        }
        super.setSubject(value);
    }

    /**
     * @see  DBEmailBase#setTo(String)
     */
    @Override public void setTo(String value)
    {
        if (StringUtil.isSet(value) && (value.length() > 200))
        {
            value = value.substring(0, 200);
        }

        super.setTo(value);
    }

    /**
     * @see  com.cordys.cpc.bsf.busobject.BusObject#onBeforeInsert()
     */
    @Override protected void onBeforeInsert()
    {
        if (isNull(ATTR_EMAIL_ID))
        {
            setID(Native.createGuid());
        }
    }
}
