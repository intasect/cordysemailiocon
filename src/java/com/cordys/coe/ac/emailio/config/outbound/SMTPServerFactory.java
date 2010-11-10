package com.cordys.coe.ac.emailio.config.outbound;

import com.cordys.coe.ac.emailio.EmailIOConnectorConstants;
import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;

import com.eibus.management.IManagedComponent;

import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * This class creates the SMTP Server object based on the given configuration XML.
 *
 * @author  pgussow
 */
public class SMTPServerFactory
{
    /**
     * This method gets the {@link ISMTPServer} object based on the given configuration.
     *
     * @param   iConfiguration      The configuration XML.
     * @param   mcManagedComponent  The parent managed component.
     *
     * @return  The {@link ISMTPServer} object based on the given configuration.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    public static ISMTPServer createSMTPServer(int iConfiguration,
                                               IManagedComponent mcManagedComponent)
                                        throws EmailIOConfigurationException
    {
        ISMTPServer ssReturn = null;

        XPathMetaInfo xmi = new XPathMetaInfo();
        xmi.addNamespaceBinding("ns", EmailIOConnectorConstants.NS_CONFIGURATION);

        // Create the actual wrapper and set the managed component.
        ssReturn = new SMTPServer(iConfiguration);
        ssReturn.setManagedComponent(mcManagedComponent);

        return ssReturn;
    }
}
