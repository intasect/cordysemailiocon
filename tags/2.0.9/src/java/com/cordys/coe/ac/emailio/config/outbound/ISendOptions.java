

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
package com.cordys.coe.ac.emailio.config.outbound;

/**
 * This interface holds the options that can be set for a specific request to override the default
 * conenctor configuration.
 *
 * @author  pgussow
 */
public interface ISendOptions
{
    /**
     * This method gets whether or not the mail should be encrypted.
     *
     * @return  Whether or not the mail should be encrypted.
     */
    boolean getEncryptMail();

    /**
     * This method gets whether or not the mail should be signed.
     *
     * @return  Whether or not the mail should be signed.
     */
    boolean getSignMail();

    /**
     * This method gets the name of SMTP server that should be used.
     *
     * @return  The name of SMTP server that should be used.
     */
    String getSMTPServer();

    /**
     * This method sets whether or not the mail should be encrypted.
     *
     * @param  bEncryptMail  Whether or not the mail should be encrypted.
     */
    void setEncryptMail(boolean bEncryptMail);

    /**
     * This method sets whether or not the mail should be signed.
     *
     * @param  bSignMail  Whether or not the mail should be signed.
     */
    void setSignMail(boolean bSignMail);

    /**
     * This method sets the name of SMTP server that should be used.
     *
     * @param  sSMTPServer  The name of SMTP server that should be used.
     */
    void setSMTPServer(String sSMTPServer);
}
