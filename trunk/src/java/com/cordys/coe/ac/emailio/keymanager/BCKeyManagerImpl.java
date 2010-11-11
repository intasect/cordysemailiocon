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
import com.cordys.coe.ac.emailio.exception.KeyManagerException;
import com.cordys.coe.ac.emailio.localization.KeyManagerExceptionMessages;
import com.cordys.coe.ac.emailio.util.CertificateUtil;
import com.cordys.coe.ac.emailio.util.ESubjectDNField;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.system.Native;

import com.eibus.xml.xpath.XPathMetaInfo;

import java.io.File;
import java.io.FileInputStream;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;

import java.security.cert.X509Certificate;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bouncycastle.cms.RecipientId;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * This class manages the keystores and the identities which are in there.
 *
 * @author  pgussow
 */
public class BCKeyManagerImpl extends AbstractKeyManager
{
    /**
     * Holds the logger to use.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(BCKeyManagerImpl.class);
    /**
     * Holds the identifier of the bouncy castle provider.
     */
    private static final String PROVIDER_BOUNCY_CASTLE = "BC";
    /**
     * Holds the name of the location parameter.
     */
    private static final String PARAM_LOCATION = "location";
    /**
     * Holds the name of the password parameter.
     */
    private static final String PARAM_PASSWORD = "password";
    /**
     * Holds the name of the keystore-type parameter.
     */
    private static final String PARAM_TYPE = "type";
    /**
     * Holds the name of the certificate passwords parameter.
     */
    private static final String PARAM_CERTIFICATE_PASSWORDS = "certificatepasswords";
    /**
     * Holds the list of certificates that can be used for encryption and decryption of messages.
     * The index is based on the email address that is specified in the key.
     */
    private Map<String, ICertificateInfo> m_encryptionCertificates = new LinkedHashMap<String, ICertificateInfo>();
    /**
     * Holds the JKS keystore that was loaded.
     */
    private KeyStore m_keyStore;
    /**
     * Holds the list of certificates indexed by RecipientId.
     */
    private Map<RecipientId, ICertificateInfo> m_recipientCertificates = new LinkedHashMap<RecipientId, ICertificateInfo>();
    /**
     * Holds the certificate validator to use.
     */
    private ICertificateValidator m_validator;
    /**
     * Holds the list of certificates that can be used for verifying a signature.
     */
    private Map<String, ICertificateInfo> m_verificationCertificates = new LinkedHashMap<String, ICertificateInfo>();

    /**
     * Creates a new BCKeyManagerImpl object.
     */
    public BCKeyManagerImpl()
    {
        if (Security.getProvider(PROVIDER_BOUNCY_CASTLE) == null)
        {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * @see  com.cordys.coe.ac.emailio.keymanager.IKeyManager#getCertificateInfo(java.lang.String)
     */
    @Override public ICertificateInfo getCertificateInfo(String emailAddress)
    {
        ICertificateInfo returnValue = m_encryptionCertificates.get(emailAddress);

        if (returnValue == null)
        {
            // It might be that we only have public information.
            returnValue = m_verificationCertificates.get(emailAddress);
        }

        return returnValue;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.keymanager.IKeyManager#getCertificateInfo(org.bouncycastle.cms.RecipientId)
     */
    @Override public ICertificateInfo getCertificateInfo(RecipientId riRecipientID)
                                                  throws KeyManagerException
    {
        ICertificateInfo returnValue = m_recipientCertificates.get(riRecipientID);

        if (returnValue == null)
        {
            // It might be that we only have public information.
            returnValue = m_recipientCertificates.get(riRecipientID);
        }

        return returnValue;
    }

    /**
     * This method gets the certificate passwords that are in the configuration.
     *
     * @param   xmi  The XpathMetaInfo details.
     *
     * @return  The certificate passwords that are in the configuration.
     */
    public Map<String, String> getCertificatePasswords(XPathMetaInfo xmi)
    {
        Map<String, String> returnValue = new LinkedHashMap<String, String>();

        int passwords = getXMLParameter(PARAM_CERTIFICATE_PASSWORDS);

        int[] pws = XPathHelper.selectNodes(passwords, "./ns:certificate", xmi);

        for (int password : pws)
        {
            String alias = XPathHelper.getStringValue(password, "./ns:alias", xmi, "");
            String realPassword = XPathHelper.getStringValue(password, "./ns:password", xmi, "");

            // Decode the password
            byte[] bytes = realPassword.getBytes();
            realPassword = new String(Native.decodeBinBase64(bytes, bytes.length));

            returnValue.put(alias, realPassword);
        }

        return returnValue;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.keymanager.IKeyManager#getIdentities()
     */
    @Override public Map<String, ICertificateInfo> getIdentities()
    {
        return Collections.unmodifiableMap(m_encryptionCertificates);
    }

    /**
     * This method gets the location of the keystore file.
     *
     * @return  The location of the keystore file.
     */
    public String getLocation()
    {
        return getStringParameter(PARAM_LOCATION);
    }

    /**
     * This method gets the password for the given keystore. In the XML this parameter is Base64
     * encoded.
     *
     * @return  The password for the given keystore.
     */
    public String getPassword()
    {
        String returnValue = getStringParameter(PARAM_PASSWORD);

        try
        {
            byte[] password = returnValue.getBytes();
            returnValue = new String(Native.decodeBinBase64(password, password.length));
        }
        catch (Exception e)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Error Base64-decoding password", e);
            }
        }

        return returnValue;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.keymanager.IKeyManager#getPrivateKey(java.lang.String)
     */
    @Override public PrivateKey getPrivateKey(String emailAddress)
    {
        PrivateKey returnValue = null;

        ICertificateInfo certInfo = m_encryptionCertificates.get(emailAddress);

        if (certInfo != null)
        {
            returnValue = (PrivateKey) certInfo.getKey();
        }
        return returnValue;
    }

    /**
     * This method gets the type of the keystore (JKS/PKCS12.
     *
     * @return  The type of the keystore (JKS/PKCS12.
     */
    public String getType()
    {
        return getStringParameter(PARAM_TYPE);
    }

    /**
     * Adapter method that is called after the parameters are parsed.
     *
     * @param   ecConfiguration     The Email IO Connector configuration
     * @param   iConfigurationNode  The XML containing the configuration of the key manager.
     * @param   xmi                 The XPath meta info to use. The prefix ns should be mapped to
     *                              the proper namespace.
     * @param   cvValidator         The certificate validator to use.
     *
     * @throws  KeyManagerException  In case of any exceptions.
     *
     * @see     com.cordys.coe.ac.emailio.keymanager.AbstractKeyManager#postInit(com.cordys.coe.ac.emailio.config.IEmailIOConfiguration,
     *          int, com.eibus.xml.xpath.XPathMetaInfo,
     *          com.cordys.coe.ac.emailio.keymanager.ICertificateValidator)
     */
    @Override protected void postInit(IEmailIOConfiguration ecConfiguration, int iConfigurationNode,
                                      XPathMetaInfo xmi, ICertificateValidator cvValidator)
                               throws KeyManagerException
    {
        m_keyStore = null;

        File keyStoreFile = new File(getLocation());

        // Check the parameters.
        if (keyStoreFile == null)
        {
            throw new KeyManagerException(KeyManagerExceptionMessages.KME_KEYSTORE_MUST_BE_PRESENT);
        }

        if (!keyStoreFile.exists())
        {
            throw new KeyManagerException(KeyManagerExceptionMessages.KME_KEYSTORE_0_COULD_NOT_BE_FOUND,
                                          keyStoreFile);
        }

        if (Security.getProvider(PROVIDER_BOUNCY_CASTLE) == null)
        {
            throw new KeyManagerException(KeyManagerExceptionMessages.KME_THE_BOUNCY_CASTLE_PROVIDER_WAS_NOT_FOUND);
        }

        // Load the Keystore
        try
        {
            if ("PKCS12".equals(getType()))
            {
                m_keyStore = KeyStore.getInstance(getType(), PROVIDER_BOUNCY_CASTLE);
            }
            else
            {
                m_keyStore = KeyStore.getInstance(getType());
            }
        }
        catch (Exception e)
        {
            throw new KeyManagerException(e,
                                          KeyManagerExceptionMessages.KME_ERROR_CREATING_KEYSTORE_OF_TYPE_0_FOR_PROVIDER_1,
                                          getType(), PROVIDER_BOUNCY_CASTLE);
        }

        // Now load the actual keystore.
        try
        {
            char[] password = new char[0];

            if (getPassword() != null)
            {
                password = getPassword().toCharArray();
            }
            m_keyStore.load(new FileInputStream(keyStoreFile), password);
        }
        catch (Exception e)
        {
            throw new KeyManagerException(e,
                                          KeyManagerExceptionMessages.KME_ERROR_LOADING_THE_KEYSTORE_0,
                                          keyStoreFile.getPath());
        }

        // Load the optional individual certificate passwords
        Map<String, String> certificatePasswords = getCertificatePasswords(xmi);

        // Now load all certificates in this keystore.
        try
        {
            Enumeration<String> e = m_keyStore.aliases();

            while (e.hasMoreElements())
            {
                String alias = e.nextElement();

                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Processing key with alias: " + alias);
                }

                MailIdentityInfo mii = new MailIdentityInfo();
                mii.setAlias(alias);

                // This is a key pair, so this can be used for encryption.
                X509Certificate cert = (X509Certificate) m_keyStore.getCertificate(alias);
                mii.setX509Certificate(cert);

                // Do the recipient ID
                RecipientId recipientID = new RecipientId();
                recipientID.setSerialNumber(cert.getSerialNumber());
                recipientID.setIssuer(cert.getIssuerX500Principal().getEncoded());

                mii.setRecipientId(recipientID);

                Map<String, ICertificateInfo> storage = null;

                if (m_keyStore.isKeyEntry(alias))
                {
                    // It could be that this key has another password.
                    char[] certPassword = null;

                    if (certificatePasswords.containsKey(alias))
                    {
                        certPassword = certificatePasswords.get(alias).toCharArray();
                    }

                    mii.setKey((PrivateKey) m_keyStore.getKey(alias, certPassword));

                    storage = m_encryptionCertificates;
                }
                else
                {
                    storage = m_verificationCertificates;
                }

                String storageKey = alias;

                // Get the email address from the certificate. For now we'll assume it has to be in
                // the subject.
                Map<ESubjectDNField, String> fields = CertificateUtil.getSubjectFields(cert);
                String emailAddress = fields.get(ESubjectDNField.EMAIL_ADDRESS);

                if (emailAddress != null)
                {
                    mii.setEmailAddress(emailAddress);
                    storageKey = emailAddress;
                }

                // Validate the certificate for the current usage
                if (((m_validator != null) && m_validator.isValid()) || (m_validator == null))
                {
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Storing certificate with alias " + alias +
                                  " under email address " + storageKey);
                    }

                    storage.put(storageKey, mii);
                    m_recipientCertificates.put(recipientID, mii);
                }
                else if (LOG.isDebugEnabled())
                {
                    LOG.debug("Certificate " + alias + " is invalid.");
                }
            }
        }
        catch (Exception e)
        {
            throw new KeyManagerException(e,
                                          KeyManagerExceptionMessages.KME_ERROR_GETTING_ALL_CERTIFICATES_IN_THE_KEYSTORE);
        }
    }
}
