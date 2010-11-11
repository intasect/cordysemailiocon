

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
