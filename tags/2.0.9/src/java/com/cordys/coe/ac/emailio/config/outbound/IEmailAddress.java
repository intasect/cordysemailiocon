package com.cordys.coe.ac.emailio.config.outbound;

import com.cordys.coe.ac.emailio.config.IXMLSerializable;

import javax.mail.internet.InternetAddress;

/**
 * This interface describes an email address.
 *
 * @author  pgussow
 */
public interface IEmailAddress extends IXMLSerializable
{
    /**
     * Holds the name of the tag 'address'.
     */
    String TAG_ADDRESS = "address";
    /**
     * Holds the name of the tag 'displayname'.
     */
    String TAG_DISPLAY_NAME = "displayname";
    /**
     * Holds the name of the tag 'emailaddress'.
     */
    String TAG_EMAIL_ADDRESS = "emailaddress";

    /**
     * This method gets the display name for this address.
     *
     * @return  The display name for this address.
     */
    String getDisplayName();

    /**
     * This method gets the actual email address.
     *
     * @return  The actual email address.
     */
    String getEmailAddress();

    /**
     * This method gets the internet address to use for this address defintion.
     *
     * @return  The internet address to use for this address defintion.
     */
    InternetAddress getInternetAddress();
}
