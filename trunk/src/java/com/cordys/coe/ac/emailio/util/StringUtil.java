

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
