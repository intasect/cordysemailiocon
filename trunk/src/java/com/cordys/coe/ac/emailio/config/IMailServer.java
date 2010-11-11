

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
package com.cordys.coe.ac.emailio.config;

import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;

import com.eibus.management.IManagedComponent;

/**
 * This interface contains the common description between POP3, IMAP and SMTP servers.
 *
 * @author  pgussow
 */
public interface IMailServer extends IXMLSerializable
{
    /**
     * Holds an empty string.
     */
    String EMPTY_STRING = "";
    /**
     * Holds the name of the tag 'host'.
     */
    String TAG_HOST = "host";
    /**
     * Holds the name of the tag 'name'.
     */
    String TAG_NAME = "name";
    /**
     * Holds the name of the tag 'password'.
     */
    String TAG_PASSWORD = "password";
    /**
     * Holds the name of the tag 'port'.
     */
    String TAG_PORT = "port";
    /**
     * Holds the name of the tag 'ssl'.
     */
    String TAG_SSL = "ssl";
    /**
     * Holds the name of the tag 'username'.
     */
    String TAG_USERNAME = "username";

    /**
     * This method gets the host name where the email box resides.
     *
     * @return  The host name where the email box resides.
     */
    String getHost();

    /**
     * This method gets the managed component associated with this email box.
     *
     * @return  The managed component associated with this email box.
     */
    IManagedComponent getManagedComponent();

    /**
     * This method gets the name of the email box. This is for display only.
     *
     * @return  The name of the email box. This is for display only.
     */
    String getName();

    /**
     * This method gets the password for the email box.
     *
     * @return  The password for the email box.
     */
    String getPassword();

    /**
     * This method gets the port number.
     *
     * @return  The port number.
     */
    int getPort();

    /**
     * This method gets the username for the email box.
     *
     * @return  The username for the email box.
     */
    String getUsername();

    /**
     * This method gets whether or not SSL is enabled for the connection.
     *
     * @return  Whether or not SSL is enabled for the connection.
     */
    boolean isSSLEnabled();

    /**
     * This method sets the managed component associated with this email box.
     *
     * @param  mcManagedComponent  The managed component associated with this email box.
     */
    void setManagedComponent(IManagedComponent mcManagedComponent);

    /**
     * This method is called to validate the configuration. This method is used to i.e. check if the
     * email server is reachable or that the messages configured are actually sendable.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    void validate()
           throws EmailIOConfigurationException;
}
