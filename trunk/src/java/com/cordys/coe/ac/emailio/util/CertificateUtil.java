

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
package com.cordys.coe.ac.emailio.util;

import com.cordys.coe.ac.emailio.exception.KeyManagerException;
import com.cordys.coe.ac.emailio.localization.KeyManagerExceptionMessages;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;

import java.io.InputStream;

import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains utility methods for detailing with digital certificates.
 *
 * @author  pgussow
 */
public class CertificateUtil
{
    /**
     * Holds the logger to use.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(CertificateUtil.class);
    /**
     * Holds the regex used to split the DN fields.
     */
    private static final Pattern SUBJECT_DN_FIELDS = Pattern.compile(" *([^=]+)=(.*)");
    /**
     * Holds teh certificate type for identifying X509.
     */
    private static final String X509_CERT_TYPE = "X.509";

    /**
     * This method returns a map with the subject DN fields.
     *
     * @param   certificate  The certificate to analyze.
     *
     * @return  The map with the return values.
     */
    public static Map<ESubjectDNField, String> getSubjectFields(X509Certificate certificate)
    {
        Map<ESubjectDNField, String> returnValue = new LinkedHashMap<ESubjectDNField, String>();

        // First get the subject and split it.
        String subject = certificate.getSubjectDN().getName();
        String[] subjectEntries = subject.split(", *");

        for (String entry : subjectEntries)
        {
            Matcher m = SUBJECT_DN_FIELDS.matcher(entry);

            if (m.find())
            {
                String key = m.group(1);
                String value = m.group(2);

                ESubjectDNField sdf = ESubjectDNField.parseKey(key);

                if (sdf != null)
                {
                    returnValue.put(sdf, value);
                }
                else if (LOG.isWarningEnabled())
                {
                    LOG.log(Severity.WARN,
                            "Could not map the subject field " + key + " to a real value.");
                }
            }
        }

        return returnValue;
    }

    /**
     * This method loads a X509 certificate based on the given data.
     *
     * @param   isData  The data for the certificate.
     *
     * @return  The loaded X509 certificate.
     *
     * @throws  KeyManagerException  In case of any exceptions.
     */
    public static X509Certificate loadCertificate(InputStream isData)
                                           throws KeyManagerException
    {
        X509Certificate xcReturn = null;

        try
        {
            // Create the factory
            CertificateFactory cf = CertificateFactory.getInstance(X509_CERT_TYPE);

            Collection<? extends Certificate> coll = cf.generateCertificates(isData);
            Iterator<? extends Certificate> iter = coll.iterator();

            while (iter.hasNext())
            {
                X509Certificate cert = (X509Certificate) iter.next();

                if (cert != null)
                {
                    xcReturn = cert;

                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Found a certificate: " + cert.getSubjectDN().getName());
                    }

                    break;
                }
            }
        }
        catch (Exception e)
        {
            throw new KeyManagerException(e,
                                          KeyManagerExceptionMessages.KME_ERROR_LOADING_X509_CERTIFICATE_FROM_THE_STREAM);
        }

        return xcReturn;
    }
}
