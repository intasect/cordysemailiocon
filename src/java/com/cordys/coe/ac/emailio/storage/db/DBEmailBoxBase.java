/*
 * This class has been generated by the Code Generator
 */



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
package com.cordys.coe.ac.emailio.storage.db;

import com.cordys.cpc.bsf.busobject.BusObjectConfig;
import com.cordys.cpc.bsf.busobject.BusObjectIterator;
import com.cordys.cpc.bsf.busobject.QueryObject;
import com.cordys.cpc.bsf.classinfo.AttributeInfo;
import com.cordys.cpc.bsf.classinfo.ClassInfo;
import com.cordys.cpc.bsf.classinfo.RelationInfo_FK;
import com.cordys.cpc.bsf.listeners.constraint.StringValidator;

/**
 * DOCUMENTME.
 *
 * @author  $author$
 */
public abstract class DBEmailBoxBase extends com.cordys.cpc.bsf.busobject.StateBusObject
{
    /**
     * tags used in the XML document.
     */
    public static final String ATTR_EMAILBOX_ID = "EMAILBOX_ID";
    /**
     * DOCUMENTME.
     */
    public static final String ATTR_BOX_NAME = "BOX_NAME";
    /**
     * DOCUMENTME.
     */
    public static final String ATTR_HOST = "HOST";
    /**
     * DOCUMENTME.
     */
    public static final String ATTR_BOX_TYPE = "BOX_TYPE";
    /**
     * DOCUMENTME.
     */
    public static final String ATTR_CONFIGURATION = "CONFIGURATION";
    /**
     * DOCUMENTME.
     */
    private static final String REL_CONTEXT_CONTAINERObjects = "FK:EMAIL_BOX[EMAILBOX_ID]:CONTEXT_CONTAINER[EMAILBOX]";
    /**
     * DOCUMENTME.
     */
    private static final String REL_TRIGGER_STOREObjects = "FK:EMAIL_BOX[EMAILBOX_ID]:TRIGGER_STORE[EMAILBOX]";
    /**
     * DOCUMENTME.
     */
    private static ClassInfo s_classInfo = null;

    /**
     * Creates a new DBEmailBoxBase object.
     *
     * @param  config  DOCUMENTME
     */
    public DBEmailBoxBase(BusObjectConfig config)
    {
        super(config);
    }

    /**
     * DOCUMENTME.
     *
     * @return  DOCUMENTME
     */
    public static ClassInfo _getClassInfo()
    {
        if (s_classInfo == null)
        {
            s_classInfo = newClassInfo(DBEmailBox.class);
            s_classInfo.setUIDElements(new String[] { ATTR_EMAILBOX_ID });

            {
                AttributeInfo ai = new AttributeInfo(ATTR_EMAILBOX_ID);
                ai.setJavaName("ID");
                ai.setAttributeClass(String.class);

                StringValidator v = new StringValidator(ATTR_EMAILBOX_ID);
                v.setMaxLength(50);
                ai.addConstraintHandler(v);
                s_classInfo.addAttributeInfo(ai);
            }

            {
                AttributeInfo ai = new AttributeInfo(ATTR_BOX_NAME);
                ai.setJavaName("Name");
                ai.setAttributeClass(String.class);

                StringValidator v = new StringValidator(ATTR_BOX_NAME);
                v.setMaxLength(50);
                ai.addConstraintHandler(v);
                s_classInfo.addAttributeInfo(ai);
            }

            {
                AttributeInfo ai = new AttributeInfo(ATTR_HOST);
                ai.setJavaName("Host");
                ai.setAttributeClass(String.class);

                StringValidator v = new StringValidator(ATTR_HOST);
                v.setMaxLength(150);
                ai.addConstraintHandler(v);
                s_classInfo.addAttributeInfo(ai);
            }

            {
                AttributeInfo ai = new AttributeInfo(ATTR_BOX_TYPE);
                ai.setJavaName("Type");
                ai.setAttributeClass(String.class);

                StringValidator v = new StringValidator(ATTR_BOX_TYPE);
                v.setMaxLength(50);
                ai.addConstraintHandler(v);
                s_classInfo.addAttributeInfo(ai);
            }

            {
                AttributeInfo ai = new AttributeInfo(ATTR_CONFIGURATION);
                ai.setJavaName("Configuration");
                ai.setAttributeClass(String.class);

                StringValidator v = new StringValidator(ATTR_CONFIGURATION);
                v.setMaxLength(1073741823);
                ai.addConstraintHandler(v);
                s_classInfo.addAttributeInfo(ai);
            }

            {
                // relation CONTEXT_CONTAINERObjects
                // (FK:EMAIL_BOX[EMAILBOX_ID]:CONTEXT_CONTAINER[EMAILBOX])
                RelationInfo_FK ri = new RelationInfo_FK(REL_CONTEXT_CONTAINERObjects);
                ri.setName("CONTEXT_CONTAINERObjects");
                ri.setLocalAttributes(new String[] { "EMAILBOX_ID" });
                ri.setLocalIsPK(true);
                ri.setRelatedClass(com.cordys.coe.ac.emailio.storage.db.DBContextContainer.class);
                ri.setRelatedAttributes(new String[] { "EMAILBOX" });
                ri.setRelatedIdentifier("FK:CONTEXT_CONTAINER[EMAILBOX]:EMAIL_BOX[EMAILBOX_ID]");
                ri.setLoadMethod("loadCONTEXT_CONTAINERObjects");
                s_classInfo.addRelationInfo(ri);
            }

            {
                // relation TRIGGER_STOREObjects (FK:EMAIL_BOX[EMAILBOX_ID]:TRIGGER_STORE[EMAILBOX])
                RelationInfo_FK ri = new RelationInfo_FK(REL_TRIGGER_STOREObjects);
                ri.setName("TRIGGER_STOREObjects");
                ri.setLocalAttributes(new String[] { "EMAILBOX_ID" });
                ri.setLocalIsPK(true);
                ri.setRelatedClass(com.cordys.coe.ac.emailio.storage.db.DBTrigger.class);
                ri.setRelatedAttributes(new String[] { "EMAILBOX" });
                ri.setRelatedIdentifier("FK:TRIGGER_STORE[EMAILBOX]:EMAIL_BOX[EMAILBOX_ID]");
                ri.setLoadMethod("loadTRIGGER_STOREObjects");
                s_classInfo.addRelationInfo(ri);
            }
        }
        return s_classInfo;
    }

    /**
     * DOCUMENTME.
     *
     * @param   BOX_NAME  DOCUMENTME
     *
     * @return  DOCUMENTME
     */
    public static com.cordys.coe.ac.emailio.storage.db.DBEmailBox getEmailBoxByName(String BOX_NAME)
    {
        String queryText = "select * from \"EMAIL_BOX\" where \"BOX_NAME\" = :BOX_NAME";
        QueryObject query = new QueryObject(queryText);
        query.addParameter("BOX_NAME", "EMAIL_BOX.BOX_NAME", QueryObject.PARAM_STRING, BOX_NAME);
        query.setResultClass(DBEmailBox.class);
        return (DBEmailBox) query.getObject();
    }

    /**
     * DOCUMENTME.
     *
     * @param   EMAILBOX_ID  DOCUMENTME
     *
     * @return  DOCUMENTME
     */
    public static com.cordys.coe.ac.emailio.storage.db.DBEmailBox getEmailBoxObject(String EMAILBOX_ID)
    {
        String queryText = "select * from \"EMAIL_BOX\" where \"EMAILBOX_ID\" = :EMAILBOX_ID";
        QueryObject query = new QueryObject(queryText);
        query.addParameter("EMAILBOX_ID", "EMAIL_BOX.EMAILBOX_ID", QueryObject.PARAM_STRING,
                           EMAILBOX_ID);
        query.setResultClass(DBEmailBox.class);
        return (DBEmailBox) query.getObject();
    }

    /**
     * DOCUMENTME.
     *
     * @param   EMAILBOX_ID  DOCUMENTME
     * @param   cursor       DOCUMENTME
     *
     * @return  DOCUMENTME
     */
    public static BusObjectIterator getNextEmailBoxObjects(String EMAILBOX_ID,
                                                           com.cordys.cpc.bsf.query.Cursor cursor)
    {
        String queryText = "select * from \"EMAIL_BOX\" where (\"EMAILBOX_ID\" > :EMAILBOX_ID) order by \"EMAILBOX_ID\" asc";
        QueryObject query = new QueryObject(queryText);
        query.addParameter("EMAILBOX_ID", "EMAIL_BOX.EMAILBOX_ID", QueryObject.PARAM_STRING,
                           EMAILBOX_ID);
        query.setResultClass(DBEmailBox.class);
        query.setCursor(cursor);
        return query.getObjects();
    }

    /**
     * DOCUMENTME.
     *
     * @param   EMAILBOX_ID  DOCUMENTME
     * @param   cursor       DOCUMENTME
     *
     * @return  DOCUMENTME
     */
    public static BusObjectIterator getPreviousEmailBoxObjects(String EMAILBOX_ID,
                                                               com.cordys.cpc.bsf.query.Cursor cursor)
    {
        String queryText = "select * from \"EMAIL_BOX\" where (\"EMAILBOX_ID\" < :EMAILBOX_ID) order by \"EMAILBOX_ID\" desc";
        QueryObject query = new QueryObject(queryText);
        query.addParameter("EMAILBOX_ID", "EMAIL_BOX.EMAILBOX_ID", QueryObject.PARAM_STRING,
                           EMAILBOX_ID);
        query.setResultClass(DBEmailBox.class);
        query.setCursor(cursor);
        return query.getObjects();
    }

    /**
     * DOCUMENTME.
     *
     * @param  a_CONTEXT_CONTAINER  DOCUMENTME
     */
    public void addCONTEXT_CONTAINERObject(DBContextContainer a_CONTEXT_CONTAINER)
    {
        a_CONTEXT_CONTAINER.setEmailBoxID(this.getID());
    }

    /**
     * DOCUMENTME.
     *
     * @param  a_TRIGGER_STORE  DOCUMENTME
     */
    public void addTRIGGER_STOREObject(DBTrigger a_TRIGGER_STORE)
    {
        a_TRIGGER_STORE.setEmailBoxID(this.getID());
    }

    /**
     * DOCUMENTME.
     *
     * @return  DOCUMENTME
     */
    public String getConfiguration()
    {
        return getStringProperty(ATTR_CONFIGURATION);
    }

    /**
     * DOCUMENTME.
     *
     * @return  DOCUMENTME
     */
    public BusObjectIterator getCONTEXT_CONTAINERObjects()
    {
        return getMultiRelationObjects(REL_CONTEXT_CONTAINERObjects);
    }

    /**
     * DOCUMENTME.
     *
     * @return  DOCUMENTME
     */
    public String getHost()
    {
        return getStringProperty(ATTR_HOST);
    }

    /**
     * DOCUMENTME.
     *
     * @return  DOCUMENTME
     */
    public String getID()
    {
        return getStringProperty(ATTR_EMAILBOX_ID);
    }

    /**
     * DOCUMENTME.
     *
     * @return  DOCUMENTME
     */
    public String getName()
    {
        return getStringProperty(ATTR_BOX_NAME);
    }

    /**
     * DOCUMENTME.
     *
     * @return  DOCUMENTME
     */
    public BusObjectIterator getTRIGGER_STOREObjects()
    {
        return getMultiRelationObjects(REL_TRIGGER_STOREObjects);
    }

    /**
     * DOCUMENTME.
     *
     * @return  DOCUMENTME
     */
    public String getType()
    {
        return getStringProperty(ATTR_BOX_TYPE);
    }

    /**
     * DOCUMENTME.
     *
     * @return  DOCUMENTME
     */
    public BusObjectIterator loadCONTEXT_CONTAINERObjects()
    {
        String queryText = "select * from \"CONTEXT_CONTAINER\" where \"EMAILBOX\" = :EMAILBOX_ID";
        QueryObject query = new QueryObject(queryText);
        query.addParameter("EMAILBOX_ID", "CONTEXT_CONTAINER.EMAILBOX", QueryObject.PARAM_STRING,
                           getID());
        query.setResultClass(DBContextContainer.class);
        return query.getObjects();
    }

    /**
     * DOCUMENTME.
     *
     * @return  DOCUMENTME
     */
    public BusObjectIterator loadTRIGGER_STOREObjects()
    {
        String queryText = "select * from \"TRIGGER_STORE\" where \"EMAILBOX\" = :EMAILBOX_ID";
        QueryObject query = new QueryObject(queryText);
        query.addParameter("EMAILBOX_ID", "TRIGGER_STORE.EMAILBOX", QueryObject.PARAM_STRING,
                           getID());
        query.setResultClass(DBTrigger.class);
        return query.getObjects();
    }

    /**
     * DOCUMENTME.
     *
     * @param  a_CONTEXT_CONTAINER  DOCUMENTME
     */
    public void removeCONTEXT_CONTAINERObject(DBContextContainer a_CONTEXT_CONTAINER)
    {
        a_CONTEXT_CONTAINER.setNull("EMAILBOX");
    }

    /**
     * DOCUMENTME.
     *
     * @param  a_TRIGGER_STORE  DOCUMENTME
     */
    public void removeTRIGGER_STOREObject(DBTrigger a_TRIGGER_STORE)
    {
        a_TRIGGER_STORE.setNull("EMAILBOX");
    }

    /**
     * DOCUMENTME.
     *
     * @param  value  DOCUMENTME
     */
    public void setConfiguration(String value)
    {
        setProperty(ATTR_CONFIGURATION, value, 0);
    }

    /**
     * DOCUMENTME.
     *
     * @param  value  DOCUMENTME
     */
    public void setHost(String value)
    {
        setProperty(ATTR_HOST, value, 0);
    }

    /**
     * DOCUMENTME.
     *
     * @param  value  DOCUMENTME
     */
    public void setID(String value)
    {
        setProperty(ATTR_EMAILBOX_ID, value, 0);
    }

    /**
     * DOCUMENTME.
     *
     * @param  value  DOCUMENTME
     */
    public void setName(String value)
    {
        setProperty(ATTR_BOX_NAME, value, 0);
    }

    /**
     * DOCUMENTME.
     *
     * @param  value  DOCUMENTME
     */
    public void setType(String value)
    {
        setProperty(ATTR_BOX_TYPE, value, 0);
    }
}