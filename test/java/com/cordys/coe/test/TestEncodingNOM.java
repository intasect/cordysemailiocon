
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
package com.cordys.coe.test;

import com.cordys.coe.ac.emailio.util.NOMUtil;
import com.cordys.coe.util.FileUtils;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;

import java.io.File;

/**
 * Test calss for testing encoding/NOM parsing.
 *
 * @author  pgussow
 */
public class TestEncodingNOM
{
    /**
     * The document to use.
     */
    private static Document s_dDoc = new Document();

    /**
     * Main method.
     *
     * @param  saArguments  The commandline arguments.
     */
    public static void main(String[] saArguments)
    {
        try
        {
            String sContent = FileUtils.readTextFileContents(new File("test\\xml\\Sonderzeichen+20090407.wise"));

            System.out.println("After reading:\n" + sContent);

            String sEncoding = NOMUtil.getXmlEncodingName(sContent);
            int iNode = s_dDoc.load(sContent.getBytes(sEncoding));

            System.out.println("After parsing:\n" + Node.writeToString(iNode, true));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
