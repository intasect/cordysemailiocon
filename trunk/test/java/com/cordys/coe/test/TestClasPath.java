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

import java.util.ArrayList;

import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * DOCUMENTME.
 *
 * @author  $author$
 */
public class TestClasPath
{
    /**
     * Main method.
     *
     * @param  saArguments  Commandline arguments.
     */
    public static void main(String[] saArguments)
    {
        try
        {
            ArrayList<String> al = new ArrayList<String>();

            // Build up the array list
            String bootClasspath = System.getProperty("sun.boot.class.path");
            parseClasspath(al, bootClasspath);

            String jvmClasspath = System.getProperty("java.class.path");
            parseClasspath(al, jvmClasspath);

            String mailJarLocation = "";

            for (String entry : al)
            {
                if (entry.matches("^(.+[\\\\/]){0,1}mail.jar$"))
                {
                    mailJarLocation = entry;
                    break;
                }
            }

            if (mailJarLocation.length() > 0)
            {
                System.out.println(mailJarLocation);

                JarFile jf = new JarFile(mailJarLocation);
                Manifest mf = jf.getManifest();
                Attributes a = mf.getMainAttributes();

                for (Object key : a.keySet())
                {
                    Object value = a.get(key);

                    System.out.println(key.toString() + ": " + value.toString());
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * DOCUMENTME.
     *
     * @param  al         DOCUMENTME
     * @param  classpath  DOCUMENTME
     */
    private static void parseClasspath(ArrayList<String> al, String classpath)
    {
        String[] saEntries = classpath.split(System.getProperty("path.separator"));

        for (int iCount = 0; iCount < saEntries.length; iCount++)
        {
            al.add(saEntries[iCount]);
        }
    }
}
