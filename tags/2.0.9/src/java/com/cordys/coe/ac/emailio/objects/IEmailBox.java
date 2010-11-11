

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

import com.cordys.coe.ac.emailio.config.EEmailBoxType;

/**
 * This interface describes the data for an EmailBox as it should be retrieved from the persistence
 * layer.
 *
 * @author  pgussow
 */
public interface IEmailBox
{
    /**
     * This method gets the full configuration for this email box.
     *
     * @return  The full configuration for this email box.
     */
    String getFullConfiguration();

    /**
     * This method gets the host name where the email box resides.
     *
     * @return  The host name where the email box resides.
     */
    String getHost();

    /**
     * This method gets the name of the email box. This is for display only.
     *
     * @return  The name of the email box. This is for display only.
     */
    String getName();

    /**
     * This method gets the poll interval for this mailbox.
     *
     * @return  The poll interval for this mailbox.
     */
    int getPollInterval();

    /**
     * This method gets the port number.
     *
     * @return  The port number.
     */
    int getPort();

    /**
     * This method gets the type of email box (IMAP/POP3).
     *
     * @return  The type of email box (IMAP/POP3).
     */
    EEmailBoxType getType();

    /**
     * This method gets the username for the email box.
     *
     * @return  The username for the email box.
     */
    String getUsername();

    /**
     * This method sets the full configuration for this email box.
     *
     * @param  sFullConfiguration  The full configuration for this email box.
     */
    void setFullConfiguration(String sFullConfiguration);
}
