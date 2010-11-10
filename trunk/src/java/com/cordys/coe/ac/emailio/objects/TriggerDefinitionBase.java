/*
 * This class has been generated by the Code Generator
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
public abstract class TriggerDefinitionBase extends com.cordys.cpc.bsf.busobject.CustomBusObject
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
    public static final String ATTR_Definition = "Definition";
    /**
     * DOCUMENTME.
     */
    private static ClassInfo s_classInfo = null;

    /**
     * Creates a new TriggerDefinitionBase object.
     *
     * @param  config  DOCUMENTME
     */
    public TriggerDefinitionBase(BusObjectConfig config)
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
            s_classInfo = newClassInfo(TriggerDefinition.class);
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
                AttributeInfo ai = new AttributeInfo(ATTR_Definition);
                ai.setJavaName("Definition");
                ai.setAttributeClass(String.class);
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
    public String getDefinition()
    {
        return getStringProperty(ATTR_Definition);
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
     * @param  value  DOCUMENTME
     */
    public void setDefinition(String value)
    {
        setProperty(ATTR_Definition, value, 0);
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
}
