

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
