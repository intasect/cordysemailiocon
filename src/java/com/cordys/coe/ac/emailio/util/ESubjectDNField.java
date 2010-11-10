package com.cordys.coe.ac.emailio.util;

/**
 * This enum holds the fields that can be part of the DN.
 *
 * @author  $author$
 */
public enum ESubjectDNField
{
    EMAIL_ADDRESS,
    NAME,
    ORGANIZATIONAL_UNIT,
    ORGANIZATION,
    LOCATION,
    STATE,
    COUNTRY;

    /**
     * This method returns the proper field based on the passed on Subject DN key.
     *
     * @param   key  The key to map.
     *
     * @return  The proper SubjectDNField.
     */
    public static ESubjectDNField parseKey(String key)
    {
        ESubjectDNField returnValue = null;

        if ("EMAILADDRESS".equalsIgnoreCase(key) || "E".equalsIgnoreCase(key))
        {
            returnValue = EMAIL_ADDRESS;
        }
        else if ("CN".equalsIgnoreCase(key))
        {
            returnValue = NAME;
        }
        else if ("OU".equalsIgnoreCase(key))
        {
            returnValue = ORGANIZATIONAL_UNIT;
        }
        else if ("O".equalsIgnoreCase(key))
        {
            returnValue = ORGANIZATION;
        }
        else if ("L".equalsIgnoreCase(key))
        {
            returnValue = LOCATION;
        }
        else if ("ST".equalsIgnoreCase(key) || "S".equalsIgnoreCase(key))
        {
            returnValue = STATE;
        }
        else if ("C".equalsIgnoreCase(key))
        {
            returnValue = COUNTRY;
        }

        return returnValue;
    }
}
