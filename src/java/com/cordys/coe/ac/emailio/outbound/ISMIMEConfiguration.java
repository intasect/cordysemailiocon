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
 package com.cordys.coe.ac.emailio.outbound;

import com.cordys.coe.ac.emailio.exception.KeyManagerException;
import com.cordys.coe.ac.emailio.keymanager.ICertificateInfo;

import org.bouncycastle.cms.RecipientId;

/**
 * This interface describes the configuration options needed for S/MIME email handling.
 *
 * @author  pgussow
 */
public interface ISMIMEConfiguration
{
    /**
     * This method gets whether or not the S/MIME support should be used at all. When false it will
     * function as a normal SMTP service.
     *
     * @return  Whether or not the S/MIME support should be used at all. When false it will function
     *          as a normal SMTP service.
     */
    boolean getBypassSMIME();

    /**
     * This method returns the certificate info for the given email address.
     *
     * @param   sEmailAddress  The address to search for.
     *
     * @return  The information for the given emaill address. If not found, null is returned.
     *
     * @throws  KeyManagerException  In case of any exceptions.
     */
    ICertificateInfo getCertificateInfo(String sEmailAddress)
                                 throws KeyManagerException;

    /**
     * This method tries to find the certificate information based on the RecipientID information.
     *
     * @param   riRecipientID  The recipient information.
     *
     * @return  The corresponding certificate information. If no certificate information could be
     *          found null is returned.
     *
     * @throws  KeyManagerException  In case of any exceptions.
     */
    ICertificateInfo getCertificateInfo(RecipientId riRecipientID)
                                 throws KeyManagerException;

    /**
     * This method gets whether or not the CRL should be checked in case a CRL is available from the
     * certificate. If this is true then by default a certificate for which the CRL could not be
     * accessed will be considered invalid.
     *
     * @return  Whether or not the CRL should be checked in case a CRL is available from the
     *          certificate. If this is true then by default a certificate for which the CRL could
     *          not be accessed will be considered invalid.
     */
    boolean getCheckCRL();

    /**
     * This method gets whether or not to encrypt the mails that are being sent.
     *
     * @return  Whether or not to encrypt the mails that are being sent.
     */
    boolean getEncryptMails();

    /**
     * This method gets whether or not the outgoing mails should be signed.
     *
     * @return  Whether or not the outgoing mails should be signed.
     */
    boolean getSignMails();

    /**
     * This method gets whether or not S/MIME should be used.
     *
     * @return  Whether or not S/MIME should be used.
     */
    boolean getSMIMEEnabled();
}
