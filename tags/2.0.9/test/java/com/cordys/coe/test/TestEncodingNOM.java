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
