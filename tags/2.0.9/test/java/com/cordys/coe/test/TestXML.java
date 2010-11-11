package com.cordys.coe.test;

import com.cordys.coe.ac.emailio.EmailIOConnectorConstants;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;

/**
 * @author pgussow
 *
 */
public class TestXML
{
	private static Document s_dDoc = new Document();
	
	/**
	 * Main method.
	 *
	 * @param saArguments The commandline arguments.
	 */
	public static void main(String[] saArguments)
	{
		try
		{
			int iMetadata = 0;
	        try
	        {
	        	iMetadata = s_dDoc.createElementNS("metadata", null, "ns", EmailIOConnectorConstants.NS_CONFIGURATION, 0);
	        	
	        	Node.createElementWithParentNS("emailcount", String.valueOf(2), iMetadata);
	        	Node.createElementWithParentNS("triggerdefinition", null, iMetadata);
	        	
	        	System.out.println(Node.writeToString(iMetadata, true));
	        }
	        finally
	        {
	        	if (iMetadata != 0)
	        	{
	        		Node.delete(iMetadata);
	        	}
	        }
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
