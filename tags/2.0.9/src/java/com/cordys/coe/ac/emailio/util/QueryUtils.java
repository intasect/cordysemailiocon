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
