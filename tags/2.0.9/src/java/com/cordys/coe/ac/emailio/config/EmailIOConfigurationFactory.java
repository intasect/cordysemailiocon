package com.cordys.coe.ac.emailio.config;

import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;

import com.eibus.management.IManagedComponent;

/**
 * This factory creates the configuration object to use.
 *
 * @author  pgussow
 */
public class EmailIOConfigurationFactory
{
    /**
     * Creates the constructor.This loads the configuration object and pass it to XMLProperties for
     * processing.
     *
     * @param   iConfigNode       The xml-node that contains the configuration.
     * @param   mcParent          The parent managed component for JMX.
     * @param   sSOAPProcessorDN  The DN of the SOAP processor in which the storage provider is
     *                            running.
     *
     * @return  The configuration object to use.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    public static IEmailIOConfiguration createConfiguration(int iConfigNode,
                                                            IManagedComponent mcParent,
                                                            String sSOAPProcessorDN)
                                                     throws EmailIOConfigurationException
    {
        IEmailIOConfiguration iecReturn = new EmailIOConfiguration();

        iecReturn.initialize(iConfigNode, mcParent, sSOAPProcessorDN);

        return iecReturn;
    }
}
