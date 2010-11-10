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
