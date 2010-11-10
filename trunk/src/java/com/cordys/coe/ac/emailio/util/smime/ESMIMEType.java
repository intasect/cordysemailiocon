package com.cordys.coe.ac.emailio.util.smime;

/**
 * This enum holds the different types from a multipart perspective. The multipart is either plain
 * text, encrypted or signed. When a mail is both signed and encrypted the current multipart will be
 * either the encrypted multipart OR the signed multipart.
 *
 * @author  pgussow
 */
public enum ESMIMEType
{
    PLAIN,
    SIGNED,
    ENCRYPTED
}
