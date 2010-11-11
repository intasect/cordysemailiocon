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
 package com.cordys.coe.ac.emailio.config.rule;

import java.util.Map;

import javax.mail.Message;

/**
 * Interface that describes the context for a pattern.
 *
 * @author  pgussow
 */
public interface IRuleContext
{
    /**
     * Holds the default variable that holds the exception report in case of an error.
     */
    String SYS_EXCEPTION_REPORT = "sysExceptionReport";
    /**
     * Holds the default variable that holds the actual Java mail Message object.
     */
    String SYS_MESSAGE_OBJECT = "sysRawEmail";
    /**
     * Holds the default variable that holds the current XML value for the stream.
     */
    String SYS_XML_CURRENT = "sysXMLCurrent";
    /**
     * Holds the default variable that holds the email details transformed into XML.
     */
    String SYS_XML_EMAIL = "sysXMLEmail";
    /**
     * Holds the default variable that holds the details XML for the storage.
     */
    String SYS_XML_STORAGE_DETAILS = "sysXMLStorageDetails";

    /**
     * This method clears the context.
     */
    void clear();

    /**
     * This method returns if the context contains a variable with the name sName.
     *
     * @param   sName  The name of the variable.
     *
     * @return  true is there is a variable with name sName. Otherwise false.
     */
    boolean containsValue(String sName);

    /**
     * This method returns all values in the current context.
     *
     * @return  All values in the current context.
     */
    Map<String, Object> getAllValues();

    /**
     * This method gets the actual email message for this context.
     *
     * @return  The actual email message for this context.
     */
    Message getMessage();

    /**
     * This method returns the value with the given name.
     *
     * @param   sName  The name of the value.
     *
     * @return  The value for the given name. null if it's not found.
     */
    Object getValue(String sName);

    /**
     * This method puts the value with the given name into the context.
     *
     * @param  sName   The name of the value.
     * @param  oValue  The value to store.
     */
    void putValue(String sName, Object oValue);
}
