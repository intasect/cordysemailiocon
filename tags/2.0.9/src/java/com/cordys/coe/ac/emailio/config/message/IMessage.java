

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
package com.cordys.coe.ac.emailio.config.message;

import com.cordys.coe.ac.emailio.config.IXMLSerializable;
import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;

import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * This interface describes the message configuration.
 *
 * @author  pgussow
 */
public interface IMessage extends IXMLSerializable
{
    /**
     * This method gets the XML template to use for creating the trigger message.
     *
     * @return  The XML template to use for creating the trigger message.
     */
    String getInputXML();

    /**
     * This method gets the mappings for this message.
     *
     * @return  The mappings for this message.
     */
    IMapping[] getMappings();

    /**
     * This method gets the name of the SOAP method that needs to be executed.
     *
     * @return  The name of the SOAP method that needs to be executed.
     */
    String getMethod();

    /**
     * This method gets the namespace of the SOAP method that needs to be executed.
     *
     * @return  The namespace of the SOAP method that needs to be executed.
     */
    String getNamespace();

    /**
     * This method gets the organizational context for the SOAP method that needs to be executed.
     *
     * @return  The organizational context for the SOAP method that needs to be executed.
     */
    String getOrganization();

    /**
     * This method gets the XPath of the repeating structure within the message request. If this
     * message does not support any repeating group it holds an empty string.
     *
     * @return  The XPath of the repeating structure within the message request. If this message
     *          does not support any repeating group it holds an empty string.
     */
    String getRepeatingXPath();

    /**
     * This method gets the DN of the SOAP node/processor to which the SOAP message should be send.
     *
     * @return  The DN of the SOAP node/processor to which the SOAP message should be send.
     */
    String getSoapDN();

    /**
     * This method gets whether or not the SOAP message should be sent in a synchronous way.
     *
     * @return  Whether or not the SOAP message should be sent in a synchronous way.
     */
    boolean getSynchronous();

    /**
     * This method gets the timeout for waiting for a response. Note: This value is only looked at
     * when the call is synchronous.
     *
     * @return  The timeout for waiting for a response. Note: This value is only looked at when the
     *          call is synchronous.
     */
    long getTimeout();

    /**
     * This method gets the user context for the SOAP method that needs to be executed.
     *
     * @return  The user context for the SOAP method that needs to be executed.
     */
    String getUserDN();

    /**
     * This method gets the namespace binding for this rule.
     *
     * @return  The namespace binding for this rule.
     */
    XPathMetaInfo getXPathMetaInfo();

    /**
     * This method gets whether or not this message is capable of handling multiple emails in a
     * single request.
     *
     * @return  Whether or not this message is capable of handling multiple emails in a single
     *          request.
     */
    boolean supportsMultipleEmails();

    /**
     * This method is called to validate the configuration. This method is used to i.e. check if the
     * email server is reachable or that the messages configured are actually sendable.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    void validate()
           throws EmailIOConfigurationException;
}
