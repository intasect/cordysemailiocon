package com.cordys.coe.test;

import java.io.File;

import javax.xml.XMLConstants;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * DOCUMENTME .
 *
 * @author  pgussow
 */
public class ValidateConfig
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
            String sConfigFile = "./docs/internal/siemens_config.xml";
            String sXSD = "./docs/external/configuration.xsd";

            // Set up the schema
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new File(sXSD));

            // Set up the XML parser
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setIgnoringElementContentWhitespace(true);
            dbf.setIgnoringComments(true);
            dbf.setSchema(schema);

            // Create the new document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            db.setErrorHandler(new LocalErrorHandler());

            db.parse(new File(sConfigFile));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * DOCUMENTME .
     *
     * @author  pgussow
     */
    public static class LocalErrorHandler
        implements ErrorHandler
    {
        /**
         * @see  org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
         */
        @Override public void error(SAXParseException exception)
                             throws SAXException
        {
            System.out.println("Error: " + exception.getLineNumber() + ":" +
                               exception.getColumnNumber() + " " + exception.getMessage());
        }

        /**
         * @see  org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
         */
        @Override public void fatalError(SAXParseException exception)
                                  throws SAXException
        {
            System.out.println("Fatal: " + exception.getLineNumber() + ":" +
                               exception.getColumnNumber() + " " + exception.getMessage());
        }

        /**
         * @see  org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
         */
        @Override public void warning(SAXParseException exception)
                               throws SAXException
        {
            System.out.println(" Warn: " + exception.getLineNumber() + ":" +
                               exception.getColumnNumber() + " " + exception.getMessage());
        }
    }
}
