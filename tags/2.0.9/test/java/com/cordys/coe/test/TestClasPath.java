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
