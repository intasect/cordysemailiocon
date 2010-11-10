package com.cordys.coe.ac.emailio;

import com.eibus.xml.nom.Document;

/**
 * Holds constant definitions used for this connector.
 *
 * @author  pgussow
 */
public class EmailIOConnectorConstants
{
    /**
     * Holds the namespace for the configuration of the connector.
     */
    public static final String NS_CONFIGURATION = "http://emailioconnector.coe.cordys.com/2.0/configuration";
    /**
     * Holds the namespace for the inbound focused messages for the connector.
     */
    public static final String NS_INBOUND = "http://emailioconnector.coe.cordys.com/2.0/inbound";
    /**
     * Holds the namespace for the outbound focused messages for the connector.
     */
    public static final String NS_OUTBOUND = "http://emailioconnector.coe.cordys.com/2.0/outbound";
    /**
     * Holds the namespace for the dynamic method set of the connector.
     */
    public static final String NS_DYNAMIC = "http://emailioconnector.coe.cordys.com/2.0/inbound/dynamic";
    /**
     * Holds the namespace for the implementation of the connector.
     */
    public static final String NS_IMPLEMENTATION = "http://emailioconnector.coe.cordys.com/2.0/implementation";
    /**
     * Holds the namespace for the database classes.
     */
    public static final String NS_DATABASE = "http://emailioconnector.coe.cordys.com/2.0/database";
    /**
     * Holds the namespace for the objects.
     */
    public static final String NS_OBJECTS = "http://emailioconnector.coe.cordys.com/2.0/objects";
    /**
     * Holds the namespace for the data.
     */
    public static final String NS_DATA = "http://emailioconnector.coe.cordys.com/2.0/data";
    /**
     * Holds the namespace for the default Cordys SendMail method.
     */
    public static final String NS_SEND_MAIL_COMPATIBILITY = "http://schemas.cordys.com/1.0/email";
    /**
     * Holds the deploy folder for the inbound email connector.
     */
    public static final String DEPLOY_FOLDER = "coe/emailioconnector";
    /**
     * Holds the location of the configuration XSD.
     */
    public static final String LOCATION_CONFIGURATION_XSD = DEPLOY_FOLDER +
                                                            "/docs/configuration.xsd";
    /**
     * Holds an XML document to use.
     */
    private static Document s_dDoc = new Document();

    /**
     * This method returns the XML document to use for temp XML creation.
     *
     * @return  The XML document to use for temp XML creation.
     */
    public static Document getDocument()
    {
        return s_dDoc;
    }
}
