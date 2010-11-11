

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
package com.cordys.coe.ac.emailio.keymanager;

import java.security.PrivateKey;

import java.security.cert.X509Certificate;

import org.bouncycastle.cms.RecipientId;

/**
 * This interface describes the information known about a certificate.
 *
 * @author  pgussow
 */
public interface ICertificateInfo
{
    /**
     * This method gets the alias of the certificate in the keystore.
     *
     * @return  The alias of the certificate in the keystore.
     */
    String getAlias();

    /**
     * This method gets the email address for the current certificate.
     *
     * @return  The email address for the current certificate.
     */
    String getEmailAddress();

    /**
     * This method gets the private key for this certificate.
     *
     * @return  The private key for this certificate.
     */
    PrivateKey getKey();

    /**
     * This method gets the recipient ID.
     *
     * @return  The recipient ID.
     */
    RecipientId getRecipientId();

    /**
     * This method gets the certificate.
     *
     * @return  The certificate.
     */
    X509Certificate getX509Certificate();

    /**
     * This method sets the alias of the certificate in the keystore.
     *
     * @param  alias  The alias of the certificate in the keystore.
     */
    void setAlias(String alias);

    /**
     * This method sets the email address for the current certificate.
     *
     * @param  emailAddress  The email address for the current certificate.
     */
    void setEmailAddress(String emailAddress);

    /**
     * This method sets the private key for this certificate.
     *
     * @param  key  The private key for this certificate.
     */
    void setKey(PrivateKey key);

    /**
     * This method sets the recipient ID.
     *
     * @param  recipientId  The recipient ID.
     */
    void setRecipientId(RecipientId recipientId);

    /**
     * This method sets the certificate.
     *
     * @param  certificate  The certificate.
     */
    void setX509Certificate(X509Certificate certificate);
}
