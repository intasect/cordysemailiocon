

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

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;

/**
 * This class contains utility functions to work with the XQY layer.
 *
 * @author  pgussow
 */
public class XQYUtil
{
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(XQYUtil.class);
    /**
     * Holds the document to use to create XML.
     */
    private static Document s_dDoc = new Document();

    /**
     * This mehtod creates the XQY query that can be passed on to the XQY layer.
     *
     * @param   sSQL  The SQL statement.
     *
     * @return  The XQY query to execute.
     */
    public static int createQuery(String sSQL)
    {
        return createQuery(sSQL, -1);
    }

    /**
     * This method creates a query with a cursor of iNumRows. If iNumRows is -1 the cursor is
     * omitted.
     *
     * @param   sSQL      The SQL statement to execute
     * @param   iNumRows  The number of rows to return.
     *
     * @return  The query definition.
     */
    public static int createQuery(String sSQL, int iNumRows)
    {
        int iReturn = s_dDoc.createElement("dataset");

        try
        {
            int iConstructor = s_dDoc.createElementWithParentNS("constructor", null, iReturn);
            Node.setAttribute(iConstructor, "language", "DBSQL");

            s_dDoc.createCDataElementWithParentNS("query", sSQL, iReturn);

            // There are no parameters for this query.
            if (iNumRows > 0)
            {
                int iCursor = s_dDoc.createElementWithParentNS("cursor", null, iConstructor);
                Node.setAttribute(iCursor, "numRows", String.valueOf(iNumRows));
            }
        }
        catch (Exception e)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Error creating query", e);
            }

            if (iReturn != 0)
            {
                Node.delete(iReturn);
                iReturn = 0;
            }
        }

        return iReturn;
    }
}
