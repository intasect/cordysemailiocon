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
