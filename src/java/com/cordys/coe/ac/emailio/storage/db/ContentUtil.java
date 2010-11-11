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
 package com.cordys.coe.ac.emailio.storage.db;

import com.cordys.coe.ac.emailio.localization.LogMessages;
import com.cordys.coe.ac.emailio.util.StringUtil;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.system.EIBProperties;
import com.eibus.util.system.Native;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.util.Date;

import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * This method contains utility methods to store and retrieve large data. Based on a setting the
 * large data will be stored 'as-is' or compressed to save data
 *
 * @author  pgussow
 */
public class ContentUtil
{
    /**
     * Holds the name of the global property to set the nocompression flag.
     */
    private static final String PROPERTY_GLOBAL_NO_COMPRESSION = "com.cordys.coe.ac.emailio.nocompression";
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(ContentUtil.class);
    /**
     * Holds whether or not compression should be used. The default is true.
     */
    private static boolean s_useCompression = true;

    static
    {
        if (StringUtil.isSet(EIBProperties.getProperty(PROPERTY_GLOBAL_NO_COMPRESSION, "")))
        {
            // The property is set, so this means we will always use this value. Note: the
            // property name is nocompression, so when that is set to true no compression should be
            // used and thus the value of s_useCompression should be false.
            s_useCompression = !EIBProperties.getBoolean(PROPERTY_GLOBAL_NO_COMPRESSION);
        }
    }

    /**
     * This method compresses the passed on data.
     *
     * @param   sData  The data to compress.
     *
     * @return  The compressed data.
     */
    public static String compressData(String sData)
    {
        String sReturn = sData;

        if (s_useCompression && (sData != null))
        {
            try
            {
                // Create the streams.
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                GZIPOutputStream gzip = new GZIPOutputStream(baos);

                // Write the data
                gzip.write(sData.getBytes());
                gzip.finish();

                // Encode the data
                byte[] baData = baos.toByteArray();
                sReturn = new String(Native.encodeBinBase64(baData, baData.length));

                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Compressed data. Original length: " + sData.length() +
                              ". Compressed length: " + sReturn.length() + "\nData:\n" + sReturn);
                }
            }
            catch (Exception e)
            {
                LOG.error(e,
                          LogMessages.ERROR_COMPRESSING_THE_ORIGINAL_DATA_WILL_STORE_THE_DATA_UNCOMPRESSED);
                sReturn = sData;
            }
        }

        return sReturn;
    }

    /**
     * This method decompresses the passed on data.
     *
     * @param   sData  The data to decompress.
     *
     * @return  The decompressed data.
     */
    public static String decompressData(String sData)
    {
        String sReturn = sData;

        if (s_useCompression && (sData != null))
        {
            try
            {
                // Decode the base 64 content.
                byte[] baDecoded = Native.decodeBinBase64(sData.getBytes(), sData.length());

                // Create the streams.
                ByteArrayInputStream bais = new ByteArrayInputStream(baDecoded);
                GZIPInputStream zis = new GZIPInputStream(bais);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                // Read the stream
                byte[] baBuffer = new byte[1024 * 64];

                int iRead = zis.read(baBuffer);

                while (iRead > -1)
                {
                    baos.write(baBuffer, 0, iRead);
                    iRead = zis.read(baBuffer);
                }

                sReturn = baos.toString();

                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Decompressed data. Compressed size: " + sData.length() +
                              ". Original size: " + sReturn.length() + "\nData:\n" + sReturn);
                }
            }
            catch (Exception e)
            {
                LOG.error(e, LogMessages.ERROR_DECOMPRESSING_THE_DATA_TO_ORIGINAL_FORMAT);
                sReturn = sData;
            }
        }

        return sReturn;
    }

    /**
     * This method returns the current date, but makes sure it's rounded to whole seconds to avoid
     * fraction problems in WsApps.
     *
     * @return  The current date.
     */
    public static Date getNow()
    {
        Date dReturn = new Date();

        long lTime = dReturn.getTime();
        lTime = (lTime / 1000) * 1000;
        dReturn.setTime(lTime);

        return dReturn;
    }

    /**
     * This method will set whether or not compression should be used. This method will only work if
     * the system property com.cordys.coe.ac.emailio.nocompression has not been set.
     *
     * @param  value  The new value for compression.
     */
    public static void setUseCompression(boolean value)
    {
        if (StringUtil.isSet(EIBProperties.getProperty(PROPERTY_GLOBAL_NO_COMPRESSION, "")))
        {
            LOG.warn(null,
                     LogMessages.WRN_CANNOT_SET_USECOMPRESSION_TO_0_BECAUSE_IT_IS_ALREADY_SET_GLOBALLY,
                     value, PROPERTY_GLOBAL_NO_COMPRESSION, s_useCompression);
        }
        else
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Set compression usage to: " + value);
            }

            s_useCompression = value;
        }
    }

    /**
     * Returns whether or not the DB layer is using compression.
     *
     * @return  true if compression is being used. Otherwise false.
     */
    public static boolean usingCompression()
    {
        return s_useCompression;
    }
}
