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
 package com.cordys.coe.ac.emailio.keymanager;

import com.cordys.coe.ac.emailio.config.IEmailIOConfiguration;
import com.cordys.coe.ac.emailio.config.IXMLSerializable;
import com.cordys.coe.ac.emailio.exception.KeyManagerException;

import com.eibus.xml.xpath.XPathMetaInfo;

import java.security.PrivateKey;

import java.util.Map;

import org.bouncycastle.cms.RecipientId;

/**
 * This method describes the API for a Key manager.
 *
 * @author  pgussow
 */
public interface IKeyManager extends IXMLSerializable
{
    /**
     * Holds the name of the attribute which holds the parameter name.
     */
    String ATTRIBUTE_NAME = "name";
    /**
     * Holds the name of the attribute type.
     */
    String ATTRIBUTE_TYPE = "type";
    /**
     * Holds the name of the tag that identifies a parameter.
     */
    String ELEMENT_PARAMETER = "parameter";
    /**
     * Holds the name of the tag params which wraps the parameters.
     */
    String ELEMENT_PARAMETERS = "parameters";

    /**
     * This method returns the certificate info for the given email address.
     *
     * @param   sEmailAddress  The address to search for.
     *
     * @return  The information for the given emaill address. If not found, null is returned.
     *
     * @throws  KeyManagerException  DOCUMENTME
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
     * This method returns the list of known identities from this key manager (thus certificates
     * that include a private key).
     *
     * @return  The list of identities (thus certificates that include a private key).
     */
    Map<String, ICertificateInfo> getIdentities();

    /**
     * This method gets the name of the specific key manager.
     *
     * @return  The name of the specific key manager.
     */
    String getName();

    /**
     * This method returns the private key for the given email address.
     *
     * @param   sEmailAddress  The address to find the private key for.
     *
     * @return  The private key to use.
     */
    PrivateKey getPrivateKey(String sEmailAddress);

    /**
     * This method is called to initialize the the key manager.
     *
     * <p>This method extracts the parameters from the XML configuration and puts them in a map in
     * order to make them more easily available for derived classes.</p>
     *
     * @param   ecConfiguration     The configuration of the processor
     * @param   iConfigurationNode  The XML containing the configuration of the key manager.
     * @param   xmi                 The XPath meta info to use. The prefix ns should be mapped to
     *                              the proper namespace.
     * @param   cvValidator         The certificate validator to use.
     *
     * @throws  KeyManagerException  In case of any exceptions.
     */
    void initialize(IEmailIOConfiguration ecConfiguration, int iConfigurationNode,
                    XPathMetaInfo xmi, ICertificateValidator cvValidator)
             throws KeyManagerException;

    /**
     * This method sets the name of the specific key manager.
     *
     * @param  sName  The name of the specific key manager.
     */
    void setName(String sName);
}
