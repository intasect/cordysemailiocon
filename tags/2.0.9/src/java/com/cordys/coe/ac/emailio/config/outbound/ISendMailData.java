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
 package com.cordys.coe.ac.emailio.config.outbound;

import com.cordys.coe.ac.emailio.config.IXMLSerializable;

/**
 * This interface describes the data that can be passed on to the send mail method.
 *
 * @author  pgussow
 */
public interface ISendMailData extends IXMLSerializable
{
    /**
     * Holds the name of the tag 'bcc'.
     */
    String TAG_BCC = "bcc";
    /**
     * Holds the name of the tag 'cc'.
     */
    String TAG_CC = "cc";
    /**
     * Holds the name of the tag 'from'.
     */
    String TAG_FROM = "from";
    /**
     * Holds the name of the tag 'headers'.
     */
    String TAG_HEADERS = "headers";
    /**
     * Holds the name of the tag 'options'.
     */
    String TAG_OPTIONS = "options";
    /**
     * Holds the name of the tag 'replyto'.
     */
    String TAG_REPLY_TO = "replyto";
    /**
     * Holds the name of the tag 'SendMail'.
     */
    String TAG_SEND_MAIL = "SendMail";
    /**
     * Holds the name of the tag 'subject'.
     */
    String TAG_SUBJECT = "subject";
    /**
     * Holds the name of the tag 'to'.
     */
    String TAG_TO = "to";

    /**
     * This method gets the BCC addresses.
     *
     * @return  The BCC addresses.
     */
    IEmailAddress[] getBCC();

    /**
     * This method gets the CC addresses.
     *
     * @return  The CC addresses.
     */
    IEmailAddress[] getCC();

    /**
     * This method gets the from address.
     *
     * @return  The from address.
     */
    IEmailAddress getFrom();

    /**
     * This method gets the headers for the mime part.
     *
     * @return  The headers for the mime part.
     */
    IHeader[] getHeaders();

    /**
     * This method gets the main data for the mail..
     *
     * @return  The main data for the mail..
     */
    IMailData getMailData();

    /**
     * This method gets the main multipart for the mail.
     *
     * @return  The main multipart for the mail.
     */
    IMultiPart getMultiPart();

    /**
     * This method gets the reply to address.
     *
     * @return  The reply to address.
     */
    IEmailAddress getReplyTo();

    /**
     * This method gets the Send Options as set in the request to override the connector
     * configuration.
     *
     * @return  The Send Options as set in the request to override the connector configuration.
     */
    ISendOptions getSendOptions();

    /**
     * This method gets the subject.
     *
     * @return  The subject.
     */
    String getSubject();

    /**
     * This method gets the to addresses.
     *
     * @return  The to addresses.
     */
    IEmailAddress[] getTo();

    /**
     * This method returns whether or not this mime part has data.
     *
     * @return  Whether or not this mime part has data.
     */
    boolean hasData();

    /**
     * This method returns whether or not this multipart has nested multiparts.
     *
     * @return  Whether or not this multipart has nested multiparts.
     */
    boolean hasNestedParts();

    /**
     * This method sets the BCC addresses.
     *
     * @param  aeaBCC  The BCC addresses.
     */
    void setBCC(IEmailAddress[] aeaBCC);

    /**
     * This method sets the CC addresses.
     *
     * @param  aeaCC  The CC addresses.
     */
    void setCC(IEmailAddress[] aeaCC);

    /**
     * This method sets the from address.
     *
     * @param  eaFrom  The from address.
     */
    void setFrom(IEmailAddress eaFrom);

    /**
     * This method sets the headers for the mime part..
     *
     * @param  ahHeaders  The headers for the mime part..
     */
    void setHeaders(IHeader[] ahHeaders);

    /**
     * This method sets the main data for the mail..
     *
     * @param  mdMailData  The main data for the mail..
     */
    void setMailData(IMailData mdMailData);

    /**
     * This method sets the main multipart for the mail.
     *
     * @param  mpMultiPart  The main multipart for the mail.
     */
    void setMultiPart(IMultiPart mpMultiPart);

    /**
     * This method sets the reply to address..
     *
     * @param  eaReplyTo  The reply to address..
     */
    void setReplyTo(IEmailAddress eaReplyTo);

    /**
     * This method sets the Send Options as set in the request to override the connector
     * configuration.
     *
     * @param  soSendOptions  The Send Options as set in the request to override the connector
     *                        configuration.
     */
    void setSendOptions(ISendOptions soSendOptions);

    /**
     * This method sets the subject.
     *
     * @param  sSubject  The subject.
     */
    void setSubject(String sSubject);

    /**
     * This method sets the To addresses.
     *
     * @param  aeaTo  The To addresses.
     */
    void setTo(IEmailAddress[] aeaTo);
}
