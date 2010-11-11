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
package com.cordys.coe.ac.emailio.objects;

import com.cordys.cpc.bsf.busobject.BusObjectConfig;
import com.cordys.cpc.bsf.classinfo.AttributeInfo;
import com.cordys.cpc.bsf.classinfo.ClassInfo;

/**
 * DOCUMENTME.
 *
 * @author  $author$
 */
public abstract class ContextContainerBase extends com.cordys.cpc.bsf.busobject.CustomBusObject
{
    /**
     * tags used in the XML document.
     */
    public static final String ATTR_ID = "ID";
    /**
     * DOCUMENTME.
     */
    public static final String ATTR_Name = "Name";
    /**
     * DOCUMENTME.
     */
    public static final String ATTR_EmailBoxID = "EmailBoxID";
    /**
     * DOCUMENTME.
     */
    public static final String ATTR_EmailBoxName = "EmailBoxName";
    /**
     * DOCUMENTME.
     */
    public static final String ATTR_ProcessingStatus = "ProcessingStatus";
    /**
     * DOCUMENTME.
     */
    public static final String ATTR_StatusInformation = "StatusInformation";
    /**
     * DOCUMENTME.
     */
    public static final String ATTR_TriggerDefinition = "TriggerDefinition";
    /**
     * DOCUMENTME.
     */
    public static final String ATTR_CreateDate = "CreateDate";
    /**
     * DOCUMENTME.
     */
    public static final String ATTR_ProcessingStatusChangeDate = "ProcessingStatusChangeDate";
    /**
     * DOCUMENTME.
     */
    public static final String ATTR_CompleteDate = "CompleteDate";
    /**
     * DOCUMENTME.
     */
    private static ClassInfo s_classInfo = null;

    /**
     * Creates a new ContextContainerBase object.
     *
     * @param  config  DOCUMENTME
     */
    public ContextContainerBase(BusObjectConfig config)
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
            s_classInfo = newClassInfo(ContextContainer.class);
            s_classInfo.setUIDElements(new String[] { ATTR_ID });

            {
                AttributeInfo ai = new AttributeInfo(ATTR_ID);
                ai.setJavaName("ID");
                ai.setAttributeClass(String.class);
                ai.setChangeability(AttributeInfo.CHANGEABILITY_FROZEN);
                s_classInfo.addAttributeInfo(ai);
            }

            {
                AttributeInfo ai = new AttributeInfo(ATTR_Name);
                ai.setJavaName("Name");
                ai.setAttributeClass(String.class);
                ai.setChangeability(AttributeInfo.CHANGEABILITY_FROZEN);
                s_classInfo.addAttributeInfo(ai);
            }

            {
                AttributeInfo ai = new AttributeInfo(ATTR_EmailBoxID);
                ai.setJavaName("EmailBoxID");
                ai.setAttributeClass(String.class);
                ai.setChangeability(AttributeInfo.CHANGEABILITY_FROZEN);
                s_classInfo.addAttributeInfo(ai);
            }

            {
                AttributeInfo ai = new AttributeInfo(ATTR_EmailBoxName);
                ai.setJavaName("EmailBoxName");
                ai.setAttributeClass(String.class);
                ai.setChangeability(AttributeInfo.CHANGEABILITY_FROZEN);
                s_classInfo.addAttributeInfo(ai);
            }

            {
                AttributeInfo ai = new AttributeInfo(ATTR_ProcessingStatus);
                ai.setJavaName("ProcessingStatus");
                ai.setAttributeClass(String.class);
                ai.setChangeability(AttributeInfo.CHANGEABILITY_FROZEN);
                s_classInfo.addAttributeInfo(ai);
            }

            {
                AttributeInfo ai = new AttributeInfo(ATTR_StatusInformation);
                ai.setJavaName("StatusInformation");
                ai.setAttributeClass(String.class);
                ai.setChangeability(AttributeInfo.CHANGEABILITY_FROZEN);
                s_classInfo.addAttributeInfo(ai);
            }

            {
                AttributeInfo ai = new AttributeInfo(ATTR_TriggerDefinition);
                ai.setJavaName("TriggerDefinition");
                ai.setAttributeClass(String.class);
                ai.setChangeability(AttributeInfo.CHANGEABILITY_FROZEN);
                s_classInfo.addAttributeInfo(ai);
            }

            {
                AttributeInfo ai = new AttributeInfo(ATTR_CreateDate);
                ai.setJavaName("CreateDate");
                ai.setAttributeClass(java.util.Date.class);
                ai.setChangeability(AttributeInfo.CHANGEABILITY_FROZEN);
                s_classInfo.addAttributeInfo(ai);
            }

            {
                AttributeInfo ai = new AttributeInfo(ATTR_ProcessingStatusChangeDate);
                ai.setJavaName("ProcessingStatusChangeDate");
                ai.setAttributeClass(java.util.Date.class);
                ai.setChangeability(AttributeInfo.CHANGEABILITY_FROZEN);
                s_classInfo.addAttributeInfo(ai);
            }

            {
                AttributeInfo ai = new AttributeInfo(ATTR_CompleteDate);
                ai.setJavaName("CompleteDate");
                ai.setAttributeClass(java.util.Date.class);
                ai.setChangeability(AttributeInfo.CHANGEABILITY_FROZEN);
                s_classInfo.addAttributeInfo(ai);
            }
        }
        return s_classInfo;
    }

    /**
     * DOCUMENTME.
     *
     * @return  DOCUMENTME
     */
    public java.util.Date getCompleteDate()
    {
        return getDateTimestampProperty(ATTR_CompleteDate);
    }

    /**
     * DOCUMENTME.
     *
     * @return  DOCUMENTME
     */
    public java.util.Date getCreateDate()
    {
        return getDateTimestampProperty(ATTR_CreateDate);
    }

    /**
     * DOCUMENTME.
     *
     * @return  DOCUMENTME
     */
    public String getEmailBoxID()
    {
        return getStringProperty(ATTR_EmailBoxID);
    }

    /**
     * DOCUMENTME.
     *
     * @return  DOCUMENTME
     */
    public String getEmailBoxName()
    {
        return getStringProperty(ATTR_EmailBoxName);
    }

    /**
     * DOCUMENTME.
     *
     * @return  DOCUMENTME
     */
    public String getID()
    {
        return getStringProperty(ATTR_ID);
    }

    /**
     * DOCUMENTME.
     *
     * @return  DOCUMENTME
     */
    public String getName()
    {
        return getStringProperty(ATTR_Name);
    }

    /**
     * DOCUMENTME.
     *
     * @return  DOCUMENTME
     */
    public String getProcessingStatus()
    {
        return getStringProperty(ATTR_ProcessingStatus);
    }

    /**
     * DOCUMENTME.
     *
     * @return  DOCUMENTME
     */
    public java.util.Date getProcessingStatusChangeDate()
    {
        return getDateTimestampProperty(ATTR_ProcessingStatusChangeDate);
    }

    /**
     * DOCUMENTME.
     *
     * @return  DOCUMENTME
     */
    public String getStatusInformation()
    {
        return getStringProperty(ATTR_StatusInformation);
    }

    /**
     * DOCUMENTME.
     *
     * @return  DOCUMENTME
     */
    public String getTriggerDefinition()
    {
        return getStringProperty(ATTR_TriggerDefinition);
    }

    /**
     * DOCUMENTME.
     *
     * @param  value  DOCUMENTME
     */
    public void setCompleteDate(java.util.Date value)
    {
        setProperty(ATTR_CompleteDate, value, 0);
    }

    /**
     * DOCUMENTME.
     *
     * @param  value  DOCUMENTME
     */
    public void setCreateDate(java.util.Date value)
    {
        setProperty(ATTR_CreateDate, value, 0);
    }

    /**
     * DOCUMENTME.
     *
     * @param  value  DOCUMENTME
     */
    public void setEmailBoxID(String value)
    {
        setProperty(ATTR_EmailBoxID, value, 0);
    }

    /**
     * DOCUMENTME.
     *
     * @param  value  DOCUMENTME
     */
    public void setEmailBoxName(String value)
    {
        setProperty(ATTR_EmailBoxName, value, 0);
    }

    /**
     * DOCUMENTME.
     *
     * @param  value  DOCUMENTME
     */
    public void setID(String value)
    {
        setProperty(ATTR_ID, value, 0);
    }

    /**
     * DOCUMENTME.
     *
     * @param  value  DOCUMENTME
     */
    public void setName(String value)
    {
        setProperty(ATTR_Name, value, 0);
    }

    /**
     * DOCUMENTME.
     *
     * @param  value  DOCUMENTME
     */
    public void setProcessingStatus(String value)
    {
        setProperty(ATTR_ProcessingStatus, value, 0);
    }

    /**
     * DOCUMENTME.
     *
     * @param  value  DOCUMENTME
     */
    public void setProcessingStatusChangeDate(java.util.Date value)
    {
        setProperty(ATTR_ProcessingStatusChangeDate, value, 0);
    }

    /**
     * DOCUMENTME.
     *
     * @param  value  DOCUMENTME
     */
    public void setStatusInformation(String value)
    {
        setProperty(ATTR_StatusInformation, value, 0);
    }

    /**
     * DOCUMENTME.
     *
     * @param  value  DOCUMENTME
     */
    public void setTriggerDefinition(String value)
    {
        setProperty(ATTR_TriggerDefinition, value, 0);
    }
}