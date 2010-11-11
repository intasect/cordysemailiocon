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
 package com.cordys.coe.test;

import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.xml.nom.Document;

/**
 * DOCUMENTME.
 *
 * @author  $author$
 */
public class TestXPath
{
    /**
     * Main method.
     *
     * @param  saArguments  The commandline arguments.
     */
    public static void main(String[] saArguments)
    {
        try
        {
            Document dDoc = new Document();
            int iNode = dDoc.load("D:\\data\\CustomerData\\Siemens\\IEC_problem\\testtrigger.xml");

            System.out.println(XPathHelper.getStringValue(iNode, "//pattern/value/text()"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
