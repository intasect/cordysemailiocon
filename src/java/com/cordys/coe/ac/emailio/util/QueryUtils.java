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
 package com.cordys.coe.ac.emailio.util;

import com.cordys.coe.util.sql.WsAppsQueryWrapper;

import java.util.Date;

/**
 * This class contains utility functions used to build up queries.
 *
 * @author  pgussow
 */
public class QueryUtils
{
    /**
     * This method adds the date search to the query wrapper based on the given parameters.
     *
     * @param  waqwSearch  The WsAppQuery wrapper.
     * @param  dFrom       The from date.
     * @param  dTo         The to date.
     * @param  sFieldName  The name of the field.
     * @param  sTableName  The name of the table.
     */
    public static void addDateSearch(WsAppsQueryWrapper waqwSearch, Date dFrom, Date dTo,
                                     String sFieldName, String sTableName)
    {
        if ((dFrom != null) || (dTo != null))
        {
            String sFQN = sTableName + "." + sFieldName;
            String sFromParam = "From" + sFieldName;
            String sToParam = "To" + sFieldName;

            // Either the from or to date is filled.
            if ((dFrom != null) && (dTo != null))
            {
                waqwSearch.where(sFieldName + " BETWEEN :" + sFromParam + " and :" + sToParam);
                waqwSearch.addParameter(sFromParam, sFQN, dFrom);
                waqwSearch.addParameter(sToParam, sFQN, dTo);
            }
            else if ((dFrom != null) && (dTo == null))
            {
                waqwSearch.where(sFieldName + " > :" + sFromParam);
                waqwSearch.addParameter(sFromParam, sFQN, dFrom);
            }
            else
            {
                waqwSearch.where(sFieldName + " < :" + sToParam);
                waqwSearch.addParameter(sToParam, sFQN, dTo);
            }
        }
    }

    /**
     * This method adds the search criteria if the value is not null.
     *
     * @param  waqwSearch  The WsAppQuery wrapper.
     * @param  sValue      The value to optionally add.
     * @param  sFieldName  The name of the field.
     * @param  sTableName  The name of the table.
     */
    public static void addOptionalField(WsAppsQueryWrapper waqwSearch, String sValue,
                                        String sFieldName, String sTableName)
    {
        if ((sValue != null) && (sValue.length() > 0) && (sValue.trim().length() > 0))
        {
            waqwSearch.where(sFieldName + " = :" + sFieldName);
            waqwSearch.addParameter(sFieldName, sTableName + "." + sFieldName, sValue);
        }
    }

    /**
     * This method adds the search criteria if the value is not null.
     *
     * @param  waqwSearch  The WsAppQuery wrapper.
     * @param  sValue      The value to optionally add.
     * @param  sFieldName  The name of the field.
     * @param  sTableName  The name of the table.
     */
    public static void addOptionalFieldLike(WsAppsQueryWrapper waqwSearch, String sValue,
                                            String sFieldName, String sTableName)
    {
        if ((sValue != null) && (sValue.length() > 0) && (sValue.trim().length() > 0))
        {
            waqwSearch.where(sFieldName + " like :" + sFieldName);
            waqwSearch.addParameter(sFieldName, sTableName + "." + sFieldName, "%" + sValue + "%");
        }
    }
}
