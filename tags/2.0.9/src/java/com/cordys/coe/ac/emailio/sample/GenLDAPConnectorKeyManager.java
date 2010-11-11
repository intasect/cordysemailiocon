

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
package com.cordys.coe.ac.emailio.sample;

import com.cordys.coe.ac.emailio.exception.KeyManagerException;
import com.cordys.coe.ac.emailio.keymanager.AbstractKeyManager;
import com.cordys.coe.ac.emailio.keymanager.ICertificateInfo;
import com.cordys.coe.ac.emailio.keymanager.MailIdentityInfo;
import com.cordys.coe.ac.emailio.localization.KeyManagerExceptionMessages;
import com.cordys.coe.ac.emailio.util.CertificateUtil;
import com.cordys.coe.ac.emailio.util.StringUtil;
import com.cordys.coe.util.general.Util;
import com.cordys.coe.util.soap.SOAPWrapper;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.soap.SOAPTransaction;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.system.Native;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.security.PrivateKey;

import java.security.cert.X509Certificate;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bouncycastle.cms.RecipientId;

/**
 * This certificate manager uses the Generic LDAP connector to retrieve the certificates for a
 * certain user. There following parameters can be set for this key manager:
 *
 * <ul>
 *   <li>filter - The actual filter to use to search for the entry. The email address will be
 *     substituted.</li>
 *   <li>certificate_attribute - The name of the LDAP attribute that contains the certificate.</li>
 *   <li>cache_certificates - Whether or not the certificates should be cached internally. The cache
 *     is NOT persisted.</li>
 *   <li>proxy_user - The user to use when calling LDAP.</li>
 * </ul>
 *
 * @author  pgussow
 */
public class GenLDAPConnectorKeyManager extends AbstractKeyManager
{
    /**
     * Holds the logger to use.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(GenLDAPConnectorKeyManager.class);
    /**
     * Holds the name of the parameter 'filter'. The parameter is the actual filter to use to search
     * for the entry. The email address will be substituted.
     */
    private static final String PARAM_FILTER = "filter";
    /**
     * Holds the name of the parameter 'certificateattribute'. The parameter is the name of the LDAP
     * attribute that contains the certificate.
     */
    private static final String PARAM_CERTIFICATE_ATTRIBUTE = "certificateattribute";
    /**
     * Holds the name of the parameter 'cache_certificates'. The parameter indicates whether or not
     * the certificates should be cached internally. The cache is NOT persisted.
     */
    private static final String PARAM_CACHE_CERTIFICATES = "cache_certificates";
    /**
     * Holds the name of the parameter 'proxy_user'. The parameter holds the optional username of
     * the user to use when sending the request.
     */
    private static final String PARAM_PROXY_USER = "proxy_user";
    /**
     * Holds the name of the SearchLDAP methodset.
     */
    private static final String MTD_SEARCH_LDAP = "SearchLDAP";
    /**
     * Holds the namespace for the GenLDAP connector methodset.
     */
    private static final String NS_GEN_LDAP_CONNECTOR = "http://genldap.coe.cordys.com/1.2/methods";
    /**
     * Holds the local cache for the certificates already retrieved from LDAP.
     */
    private Map<String, ICertificateInfo> m_mLocalCache = new LinkedHashMap<String, ICertificateInfo>();

    /**
     * This method gets whether or not the certificates already retrieved should be cached.
     *
     * @return  Whether or not the certificates already retrieved should be cached.
     */
    public boolean getCacheCertificates()
    {
        return getBooleanParameter(PARAM_CACHE_CERTIFICATES);
    }

    /**
     * This method gets the name of the attribute that contains the certificate.
     *
     * @return  The name of the attribute that contains the certificate.
     */
    public String getCertificateAttribute()
    {
        return getStringParameter(PARAM_CERTIFICATE_ATTRIBUTE);
    }

    /**
     * @see  com.cordys.coe.ac.emailio.keymanager.IKeyManager#getCertificateInfo(java.lang.String)
     */
    @Override public ICertificateInfo getCertificateInfo(String sEmailAddress)
                                                  throws KeyManagerException
    {
        ICertificateInfo ciReturn = null;

        // First see if we are caching certs and if the cert is available
        if (getCacheCertificates() && m_mLocalCache.containsKey(sEmailAddress))
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Using certificate which is already in the cache.");
            }

            ciReturn = m_mLocalCache.get(sEmailAddress);
        }
        else
        {
            // It's not in the cache, so we need to get it from LDAP.

            byte[] baCertificate = getProperCertificate(sEmailAddress);

            if ((baCertificate != null) && (baCertificate.length > 0))
            {
                // Load the certificate
                ByteArrayInputStream bais = new ByteArrayInputStream(baCertificate);
                X509Certificate xcCertificate = CertificateUtil.loadCertificate(bais);

                // Create the certificate info object
                MailIdentityInfo mii = new MailIdentityInfo();
                mii.setAlias(sEmailAddress);
                mii.setEmailAddress(sEmailAddress);

                mii.setX509Certificate(xcCertificate);

                // Do the recipient ID
                RecipientId recipientID = new RecipientId();
                recipientID.setSerialNumber(xcCertificate.getSerialNumber());

                try
                {
                    recipientID.setIssuer(xcCertificate.getIssuerX500Principal().getEncoded());
                }
                catch (IOException e)
                {
                    throw new KeyManagerException(e,
                                                  KeyManagerExceptionMessages.KME_ERROR_SETTING_THE_ISSUER_FOR_CERTIFICATE_WITH_SUBJECT_0,
                                                  xcCertificate.getSubjectDN().toString());
                }

                mii.setRecipientId(recipientID);

                if (getCacheCertificates())
                {
                    // Cache the certificate
                    m_mLocalCache.put(sEmailAddress, mii);
                }

                ciReturn = mii;
            }
        }

        return ciReturn;
    }

    /**
     * This method will always return null since we don't do private keys.
     *
     * @param   riRecipientID  The recipient ID.
     *
     * @return  Always null.
     *
     * @throws  KeyManagerException  In case of any exceptions.
     *
     * @see     com.cordys.coe.ac.emailio.keymanager.IKeyManager#getCertificateInfo(org.bouncycastle.cms.RecipientId)
     */
    @Override public ICertificateInfo getCertificateInfo(RecipientId riRecipientID)
                                                  throws KeyManagerException
    {
        return null;
    }

    /**
     * This method gets the filter to use.
     *
     * @return  The filter to use.
     */
    public String getFilter()
    {
        return getStringParameter(PARAM_FILTER);
    }

    /**
     * This key manager will not dump all identities it has, since there might be thousands in LDAP.
     * It will only dump the ones which are in the local cache.
     *
     * @return  The list of locally cached certificates.
     *
     * @see     com.cordys.coe.ac.emailio.keymanager.IKeyManager#getIdentities()
     */
    @Override public Map<String, ICertificateInfo> getIdentities()
    {
        return m_mLocalCache;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.keymanager.IKeyManager#getPrivateKey(java.lang.String)
     */
    @Override public PrivateKey getPrivateKey(String sEmailAddress)
    {
        // No private keys can be retrieved from LDAP.
        return null;
    }

    /**
     * This method gets the name of the proxy user.
     *
     * @return  The name of the proxy user.
     */
    public String getProxyUser()
    {
        String sReturn = getStringParameter(PARAM_PROXY_USER);

        if (!StringUtil.isSet(sReturn))
        {
            // Try a fall-back scenario to use the current transaction user.
            SOAPTransaction st = SOAPTransaction.getCurrentSOAPTransaction();

            if (st != null)
            {
                sReturn = st.getIdentity().getOrgUserDN();
            }
        }
        else if ("".equals(sReturn))
        {
            sReturn = null;
        }

        return sReturn;
    }

    /**
     * This method returns the organizational context for the Generic LDAP Connector. The current
     * organization is based on either the proxy user if available or the current SOAP transaction.
     *
     * @return  The current organization.
     */
    private String getOrganization()
    {
        return Util.getOrganizationFromUser(getProxyUser());
    }

    /**
     * This method fires the request to the generic LDAP connector.
     *
     * @param   sEmailAddress  The email address to get the public key for.
     *
     * @return  The binary data for the certificate.
     *
     * @throws  KeyManagerException  In case of any exceptions.
     */
    private byte[] getProperCertificate(String sEmailAddress)
                                 throws KeyManagerException
    {
        byte[] baReturn = null;

        // Get the connector to use.
        SOAPWrapper sw = new SOAPWrapper(getConfiguration().getConnector());
        sw.setUser(getProxyUser());
        sw.setOrganization(getOrganization());

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Using user " + sw.getUser() + " and organization " + sw.getOrganization());
        }

        try
        {
            int iRequest = sw.createSoapMethod(MTD_SEARCH_LDAP, NS_GEN_LDAP_CONNECTOR);

            // Fix the filter
            String sFilter = getFilter();
            sFilter = sFilter.replaceAll("%EMAIL_ADDRESS%", sEmailAddress);
            Node.createElementWithParentNS("filter", sFilter, iRequest);

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Request sending to LDAP:\n" +
                          Node.writeToString(Node.getRoot(iRequest), false));
            }

            // Send the message
            int iResponse = sw.sendAndWait(Node.getRoot(iRequest));

            // Try to find the response.
            XPathMetaInfo xmi = new XPathMetaInfo();
            xmi.addNamespaceBinding("genldap", NS_GEN_LDAP_CONNECTOR);

            String sAttribute = getCertificateAttribute();

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Looking for the value of attribute " + sAttribute);
            }

            String sXPath = "//genldap:tuple/genldap:old/genldap:entry/genldap:" + sAttribute +
                            "/genldap:binary";

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Executing XPath:\n" + sXPath + "\nOn XML:\n" +
                          Node.writeToString(iResponse, false));
            }

            String sData = XPathHelper.getStringValue(iResponse, sXPath, xmi, "");

            if (StringUtil.isSet(sData))
            {
                // Now do the decode
                byte[] baTemp = sData.getBytes();
                baReturn = Native.decodeBinBase64(baTemp, baTemp.length);
            }
        }
        catch (Exception e)
        {
            throw new KeyManagerException(e,
                                          KeyManagerExceptionMessages.KME_ERROR_GETTING_THE_KEY_DETAILS_FROM_LDAP_FOR_EMAIL_ADDRESS_0,
                                          sEmailAddress);
        }
        finally
        {
            sw.freeXMLNodes();
        }

        return baReturn;
    }
}
