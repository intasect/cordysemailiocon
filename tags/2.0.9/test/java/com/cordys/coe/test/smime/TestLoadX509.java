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
 package com.cordys.coe.test.smime;

import java.io.FileInputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;

/**
 * DOCUMENTME
 * .
 *
 * @author  pgussow
 */
public class TestLoadX509
{
	private static final String X509_CERT_TYPE = "X.509";

    /**
     * Main method.
     *
     * @param  saArguments  Commandline arguments.
     */
    public static void main(String[] saArguments)
    {
        try
        {
        	FileInputStream fis = new FileInputStream("d:/temp/siemens/test1.cer");
        	CertificateFactory cf = CertificateFactory.getInstance(X509_CERT_TYPE);

            Collection<? extends Certificate> coll = cf.generateCertificates(fis);
            Iterator<? extends Certificate> iter = coll.iterator();

            while (iter.hasNext())
            {
                X509Certificate cert = (X509Certificate)iter.next();
                if (cert != null)
                {
                	System.out.println("Found a certificate: " + cert.getSubjectDN().getName());
                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
