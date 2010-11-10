package com.cordys.coe.ac.emailio.keymanager;

/**
 * This interface can be used to validate if a certificate is fit for usage.
 *
 * @author  pgussow
 */
public interface ICertificateValidator
{
    /**
     * This method returns whether or not the certificate can be used.
     *
     * @return  Whether or not the certificate can be used.
     */
    boolean isValid();
}
