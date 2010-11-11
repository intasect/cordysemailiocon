package com.cordys.coe.ac.emailio.objects;

import java.util.Date;

/**
 * This interface describes the objects to wrap an email message.
 *
 * @author  pgussow
 */
public interface IEmailMessage
{
    /**
     * This method gets the from address of the email.
     *
     * @return  The from address of the email.
     */
    String getFromAddress();

    /**
     * This method gets the ID of the email message.
     *
     * @return  The ID of the email message.
     */
    String getID();

    /**
     * This method gets the raw content of the mail (thus the actual mail as it was received).
     *
     * @return  The raw content of the mail (thus the actual mail as it was received).
     */
    String getRawContent();

    /**
     * This method gets the date on which the mail was received.
     *
     * @return  The date on which the mail was received.
     */
    Date getReceiveDate();

    /**
     * This method gets the send date of the mail.
     *
     * @return  The send date of the mail.
     */
    Date getSendDate();

    /**
     * This method gets the sequence ID of the message within the context.
     *
     * @return  The sequence ID of the message within the context.
     */
    int getSequenceID();

    /**
     * This method gets the subject of the mail.
     *
     * @return  The subject of the mail.
     */
    String getSubject();

    /**
     * This method gets the to address of the email.
     *
     * @return  The to address of the email.
     */
    String getToAddress();

    /**
     * This method sets the from address of the email.
     *
     * @param  sFromAddress  The from address of the email.
     */
    void setFromAddress(String sFromAddress);

    /**
     * This method sets the ID of the email message.
     *
     * @param  sID  The ID of the email message.
     */
    void setID(String sID);

    /**
     * This method sets the raw content of the mail (thus the actual mail as it was received).
     *
     * @param  sRawContent  The raw content of the mail (thus the actual mail as it was received).
     */
    void setRawContent(String sRawContent);

    /**
     * This method sets the date on which the mail was received.
     *
     * @param  dReceiveDate  The date on which the mail was received.
     */
    void setReceiveDate(Date dReceiveDate);

    /**
     * This method sets the send date of the mail.
     *
     * @param  dSendDate  The send date of the mail.
     */
    void setSendDate(Date dSendDate);

    /**
     * This method sets the sequence ID of the message within the context.
     *
     * @param  iSequenceID  The sequence ID of the message within the context.
     */
    void setSequenceID(int iSequenceID);

    /**
     * This method sets the subject of the mail.
     *
     * @param  sSubject  The subject of the mail.
     */
    void setSubject(String sSubject);

    /**
     * This method sets the to address of the email.
     *
     * @param  sToAddress  The to address of the email.
     */
    void setToAddress(String sToAddress);
}
