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
 package com.cordys.coe.ac.emailio.method;

import com.cordys.coe.ac.emailio.exception.StorageProviderException;
import com.cordys.coe.ac.emailio.localization.StorageProviderExceptionMessages;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.cordys.cpc.bsf.busobject.BSF;
import com.cordys.cpc.bsf.busobject.BusObject;
import com.cordys.cpc.bsf.busobject.BusObjectConfig;
import com.cordys.cpc.bsf.busobject.BusObjectIterator;
import com.cordys.cpc.bsf.busobject.exception.WSAppServerException;
import com.cordys.cpc.bsf.busobject.internal.BusObjectDataSet;
import com.cordys.cpc.bsf.busobject.internal.BusObjectSetIterator;
import com.cordys.cpc.bsf.cache.CachedReflection;
import com.cordys.cpc.bsf.metadata.UnsignedInt;
import com.cordys.cpc.bsf.query.Cursor;
import com.cordys.cpc.bsf.runtime.BsfClassRegistryException;
import com.cordys.cpc.bsf.runtime.ClassRegistry;
import com.cordys.cpc.bsf.util.BSFAlertDefinitions;
import com.cordys.cpc.bsf.util.DataConverter;

import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

import java.math.BigDecimal;
import java.math.BigInteger;

import java.util.Date;

/**
 * This class wraps a parameter from the definition.
 *
 * @author  pgussow
 */
public class MethodParameter
{
    /**
     * Holds the prefix that indicates a Java BusObject.
     */
    private static final String JAVA_CLASS_PREFIX = "java:";
    /**
     * Indicates the object is a list of objects.
     */
    private static final String MULTI_OCC_SUFFIX = "[]";
    /**
     * Identifies the name of the cursor tag.
     */
    private static final String CURSOR_PARAM = "cursor";
    /**
     * Holds the Java type for this parameters.
     */
    private Class<?> m_cType;
    /**
     * Holds the actual value for this parameter.
     */
    private Object m_oValue;
    /**
     * Holds the element type (ct).
     */
    private String m_sElementType;
    /**
     * Holds the name of the parameter.
     */
    private String m_sName;
    /**
     * Holds the XML type for the parameter (dt).
     */
    private String m_sXMLType;

    /**
     * Creates a new MethodParameter object.
     *
     * @param   iParameterDefinition  The definition of the parameter.
     * @param   iMethodRequest        The actual request to get the value from.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    public MethodParameter(int iParameterDefinition, int iMethodRequest)
                    throws StorageProviderException
    {
        m_sName = Node.getLocalName(iParameterDefinition);
        m_sXMLType = Node.getAttribute(iParameterDefinition, "dt", "");
        m_sElementType = Node.getAttribute(iParameterDefinition, "ct", "");

        // Now we need to find the parameter value XML in the request.
        XPathMetaInfo xmi = new XPathMetaInfo();
        xmi.addNamespaceBinding("ns", Node.getNamespaceURI(iMethodRequest));

        m_oValue = null;

        int iParameterValue = XPathHelper.selectSingleNode(iMethodRequest, "ns:" + m_sName, xmi);

        if (iParameterValue != 0)
        {
            // It only makes sense to parse the value when value is actually passed on to the
            // request.
            readParameterValue(iParameterValue);
        }
    }

    /**
     * This method gets the Java type for this parameter.
     *
     * @return  The Java type for this parameter.
     */
    public Class<?> getJavaType()
    {
        return m_cType;
    }

    /**
     * This method gets the name of the parameter.
     *
     * @return  The name of the parameter.
     */
    public String getName()
    {
        return m_sName;
    }

    /**
     * This method gets the actual value for this parameter.
     *
     * @return  The actual value for this parameter.
     */
    public Object getValue()
    {
        return m_oValue;
    }

    /**
     * This method gets the big decimal value from the request.
     *
     * @param  iParameterValue  The value for the parameter.
     */
    private void initBigDecimal(int iParameterValue)
    {
        m_cType = BigDecimal.class;

        try
        {
            m_oValue = new BigDecimal(Node.getDataWithDefault(iParameterValue, "0"));
        }
        catch (NumberFormatException nfe)
        {
            if (iParameterValue == 0) // SC-51261
            {
                m_oValue = new BigDecimal("0");
            }
            else
            {
                throw nfe;
            }
        }
    }

    /**
     * This method gets the big integer value from the request.
     *
     * @param  iParameterValue  The value for the parameter.
     */
    private void initBigInteger(int iParameterValue)
    {
        m_cType = BigInteger.class;

        try
        {
            m_oValue = new BigInteger(Node.getDataWithDefault(iParameterValue, "0"));
        }
        catch (NumberFormatException nfe)
        {
            if (iParameterValue == 0) // SC-51261
            {
                m_oValue = new BigInteger("0");
            }
            else
            {
                throw nfe;
            }
        }
    }

    /**
     * This method gets the boolean value from the request.
     *
     * @param  iParameterValue  The value for the parameter.
     */
    private void initBoolean(int iParameterValue)
    {
        m_cType = boolean.class;
        m_oValue = new Boolean(Node.getDataWithDefault(iParameterValue, "false"));
    }

    /**
     * This method gets the byte value from the request.
     *
     * @param  iParameterValue  The value for the parameter.
     */
    private void initByte(int iParameterValue)
    {
        m_cType = byte.class;

        try
        {
            m_oValue = new Byte(Node.getDataWithDefault(iParameterValue, "0"));
        }
        catch (NumberFormatException nfe)
        {
            if (iParameterValue == 0) // SC-51261
            {
                m_oValue = new Byte((byte) 0);
            }
            else
            {
                throw nfe;
            }
        }
    }

    /**
     * This method gets the date value from the request.
     *
     * @param  iParameterValue  The value for the parameter.
     */
    private void initDate(int iParameterValue)
    {
        m_cType = Date.class;
        m_oValue = DataConverter.String2Date(Node.getDataWithDefault(iParameterValue, null));
    }

    /**
     * This method gets the double value from the request.
     *
     * @param  iParameterValue  The value for the parameter.
     */
    private void initDouble(int iParameterValue)
    {
        m_cType = double.class;

        try
        {
            m_oValue = new Double(Node.getDataWithDefault(iParameterValue, "0"));
        }
        catch (NumberFormatException nfe)
        {
            if (iParameterValue == 0) // SC-51261
            {
                m_oValue = new Double((double) 0);
            }
            else
            {
                throw nfe;
            }
        }
    }

    /**
     * This method gets the float value from the request.
     *
     * @param  iParameterValue  The value for the parameter.
     */
    private void initFloat(int iParameterValue)
    {
        m_cType = float.class;

        try
        {
            m_oValue = new Float(Node.getDataWithDefault(iParameterValue, "0"));
        }
        catch (NumberFormatException nfe)
        {
            if (iParameterValue == 0) // SC-51261
            {
                m_oValue = new Float((float) 0);
            }
            else
            {
                throw nfe;
            }
        }
    }

    /**
     * This method gets the integer value from the request.
     *
     * @param  iParameterValue  The value for the parameter.
     */
    private void initInteger(int iParameterValue)
    {
        m_cType = int.class;

        if ("elements".equals(m_sElementType))
        {
            // Interpret the value literally
            m_oValue = iParameterValue;
        }
        else
        {
            try
            {
                m_oValue = new Integer(Node.getDataWithDefault(iParameterValue, "0"));
            }
            catch (NumberFormatException nfe)
            {
                if (iParameterValue == 0) // SC-51261
                {
                    m_oValue = new Integer(0);
                }
                else
                {
                    throw nfe;
                }
            }
        }
    }

    /**
     * This method gets the long value from the request.
     *
     * @param  iParameterValue  The value for the parameter.
     */
    private void initLong(int iParameterValue)
    {
        m_cType = long.class;

        try
        {
            m_oValue = new Long(Node.getDataWithDefault(iParameterValue, "0"));
        }
        catch (NumberFormatException nfe)
        {
            if (iParameterValue == 0) // SC-51261
            {
                m_oValue = new Long((long) 0);
            }
            else
            {
                throw nfe;
            }
        }
    }

    /**
     * This method gets the short value from the request.
     *
     * @param  iParameterValue  The value for the parameter.
     */
    private void initShort(int iParameterValue)
    {
        m_cType = short.class;

        try
        {
            m_oValue = new Short(Node.getDataWithDefault(iParameterValue, "0"));
        }
        catch (NumberFormatException nfe)
        {
            if (iParameterValue == 0) // SC-51261
            {
                m_oValue = new Short((short) 0);
            }
            else
            {
                throw nfe;
            }
        }
    }

    /**
     * This method gets the String value from the request.
     *
     * @param  iParameterValue  The value for the parameter.
     */
    private void initString(int iParameterValue)
    {
        m_cType = String.class;

        if ((iParameterValue == 0) || "true".equals(Node.getAttribute(iParameterValue, "null")))
        {
            m_oValue = null;
        }
        else
        {
            m_oValue = Node.getDataWithDefault(iParameterValue, "");
        }
    }

    /**
     * This method gets the unsigned integer from the request.
     *
     * @param  iParameterValue  The value for the parameter.
     */
    private void initUI1(int iParameterValue)
    {
        m_cType = UnsignedInt.class;

        try
        {
            m_oValue = new UnsignedInt(Node.getDataWithDefault(iParameterValue, "0"));
        }
        catch (NumberFormatException nfe)
        {
            if (iParameterValue == 0) // SC-51261
            {
                m_oValue = new UnsignedInt("0");
            }
            else
            {
                throw nfe;
            }
        }
    }

    /**
     * This method reads the parameter value if the parameter is a java object.
     *
     * @param   iParameterValue  The actual value XML for the parameter.
     *
     * @return  true if the type was found. Otherwise false.
     *
     * @throws  StorageProviderException  In case of any exceptions
     */
    private boolean readJavaParameterValue(int iParameterValue)
                                    throws StorageProviderException
    {
        boolean bReturn = false;

        try
        {
            // Get the actual FQN for the class
            String sClassName = m_sXMLType.substring(JAVA_CLASS_PREFIX.length());
            boolean bMultiOcc = false;

            if (m_sXMLType.endsWith(MULTI_OCC_SUFFIX))
            {
                // strip the MULTI_OCC_SUFFIX
                sClassName = sClassName.substring(0,
                                                  sClassName.length() - MULTI_OCC_SUFFIX.length());
                bMultiOcc = true;
            }

            // Get the action definition class for this type. We reuse the default WS-AppServer
            // reflection cache.
            CachedReflection cachedReflection = CachedReflection.getInstance();
            cachedReflection.setAlertDefinition(BSFAlertDefinitions.CORDYS_ORC_2003);

            Class<?> definedClass = cachedReflection.getClassObject(sClassName).getKlas();

            if (BusObject.class.isAssignableFrom(definedClass))
            {
                // The class definition is a bus object. So we need to parse the XML into a real
                // instance of the bus object.
                if (bMultiOcc)
                {
                    // There could be multiple bus objects so we'll use a BusObjectIterator
                    int[] tuples = Find.match(iParameterValue, "fChild<tuple><old>fChild");

                    if ((tuples != null) && (tuples.length == 0))
                    {
                        tuples = Find.match(iParameterValue, "fChild<item>fChild");
                    }

                    int[] objectData = new int[tuples.length];

                    for (int i = 0; i < tuples.length; i++)
                    {
                        objectData[i] = Node.clone(tuples[i], true);
                    }

                    BusObjectDataSet objectDataSet = new BusObjectDataSet(objectData);
                    m_cType = BusObjectIterator.class;
                    m_oValue = new BusObjectSetIterator(definedClass, objectDataSet,
                                                        BSF.getObjectManager(),
                                                        BusObjectConfig.TRANSIENT_OBJECT);
                    bReturn = true;
                }
                else
                {
                    // it is single, use the specified BusObject type
                    int iTuple = Find.firstMatch(iParameterValue, "fChild<tuple><old>fChild");

                    if (iTuple == 0)
                    {
                        iTuple = Node.getFirstElement(iParameterValue);
                    }

                    String sNamespace = Node.getNamespaceURI(iTuple);
                    String sTag = Node.getLocalName(iTuple);
                    Class<?> cActual;

                    try
                    {
                        cActual = ClassRegistry.getClassForTagAndNamespace(sTag, sNamespace);
                    }
                    catch (BsfClassRegistryException e)
                    {
                        throw new StorageProviderException(e,
                                                           StorageProviderExceptionMessages.SPE_COULD_NOT_FIND_THE_CLASS_FOR_01,
                                                           sNamespace, sTag);
                    }

                    if (definedClass.isAssignableFrom(cActual))
                    {
                        // create the object
                        // BasicProfile-001.SN -- Revisit
                        int iClonedTuple = Node.clone(iTuple, true);
                        // Node.setAttribute(ituple, "xmlns", Node.getNamespaceURI(ituple));
                        String sPrefix = Node.getPrefix(iClonedTuple);
                        Node.removeAttribute(iClonedTuple, "xmlns:" + sPrefix);
                        Node.setName(iClonedTuple, Node.getLocalName(iClonedTuple));
                        removePrefixes(iClonedTuple);

                        // BasicProfile-001.EN -- Revisit
                        BusObjectConfig boc = new BusObjectConfig(BSF.getObjectManager(),
                                                                  iClonedTuple,
                                                                  BusObjectConfig.TRANSIENT_OBJECT);
                        m_oValue = BusObject.sys_createBusObject(cActual, boc);
                        m_cType = BusObject.class;
                        bReturn = true;
                    }
                }
            }
        }
        catch (WSAppServerException cnfe)
        {
            throw new StorageProviderException(cnfe,
                                               StorageProviderExceptionMessages.SPE_COULD_NOT_READ_THE_JAVA_PARAMETER_VALUE_FOR_THIS_PARAMETER,
                                               Node.writeToString(iParameterValue, false));
        }

        return bReturn;
    }

    /**
     * This method parses the parameter value. This can be anything: simple types or Java classes.
     *
     * @param   iParameterValue  The actual value for the parameter.
     *
     * @throws  StorageProviderException  In case of any exceptions.
     */
    private void readParameterValue(int iParameterValue)
                             throws StorageProviderException
    {
        boolean bFallBack = true;

        if (m_sXMLType.startsWith(JAVA_CLASS_PREFIX))
        {
            bFallBack = !readJavaParameterValue(iParameterValue);
        }
        else if ("i4".equals(m_sXMLType) && CURSOR_PARAM.equals(m_sElementType))
        {
            // Remove any prefixes
            Node.setName(iParameterValue, Node.getLocalName(iParameterValue));
            m_oValue = new Cursor(iParameterValue);
            m_cType = Cursor.class;
            bFallBack = false;
        }

        // If either no type was found or the fallback was set, try to read it the old-fashioned
        // way.
        if (bFallBack)
        {
            if ("string".equals(m_sXMLType) || "String".equals(m_sXMLType))
            {
                initString(iParameterValue);
            }
            else if ("int".equals(m_sXMLType) || "i4".equals(m_sXMLType) ||
                         "ui2".equals(m_sXMLType))
            {
                initInteger(iParameterValue);
            }
            else if ("boolean".equals(m_sXMLType))
            {
                initBoolean(iParameterValue);
            }
            else if ("number".equals(m_sXMLType))
            {
                initBigDecimal(iParameterValue);
            }
            else if ("ui8".equals(m_sXMLType))
            {
                initBigInteger(iParameterValue);
            }
            else if ("byte".equals(m_sXMLType) || "i1".equals(m_sXMLType))
            {
                initByte(iParameterValue);
            }
            else if ("dateTime".equals(m_sXMLType) || "java.util.Date".equals(m_sXMLType))
            {
                initDate(iParameterValue);
            }
            else if ("double".equals(m_sXMLType) || "r8".equals(m_sXMLType))
            {
                initDouble(iParameterValue);
            }
            else if ("float".equals(m_sXMLType) || "r4".equals(m_sXMLType))
            {
                initFloat(iParameterValue);
            }
            else if ("short".equals(m_sXMLType) || "i2".equals(m_sXMLType))
            {
                initShort(iParameterValue);
            }
            else if ("long".equals(m_sXMLType) || "i8".equals(m_sXMLType) ||
                         "ui4".equals(m_sXMLType))
            {
                initLong(iParameterValue);
            }
            else if ("ui1".equals(m_sXMLType) || UnsignedInt.CLASS.equals(m_sXMLType))
            {
                initUI1(iParameterValue);
            }
            else
            {
                initString(iParameterValue);
            }
        }
    }

    /**
     * This method removes all the prefixes set to tags. It is done recursively.
     *
     * @param  iParent  The parent node.
     */
    private void removePrefixes(int iParent)
    {
        for (int node = Node.getFirstElement(iParent); node != 0; node = Node.getNextElement(node))
        {
            Node.setName(node, Node.getLocalName(node));
            removePrefixes(node);
        }
    }
}
