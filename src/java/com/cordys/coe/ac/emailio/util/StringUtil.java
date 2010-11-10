package com.cordys.coe.ac.emailio.util;

/**
 * This method contains string utility functions.
 *
 * @author  pgussow
 */
public class StringUtil
{
    /**
     * This method returns whether or not the passed on string is set.
     *
     * @param   sString  The string to examine.
     *
     * @return  true is the string has a value. Otherwise false.
     */
    public static boolean isSet(String sString)
    {
        return ((sString != null) && (sString.length() > 0) && (sString.trim().length() > 0));
    }
}
