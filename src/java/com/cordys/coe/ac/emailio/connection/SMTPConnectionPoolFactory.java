package com.cordys.coe.ac.emailio.connection;

import com.cordys.coe.ac.emailio.config.IEmailIOConfiguration;
import com.cordys.coe.ac.emailio.config.outbound.ISMTPServer;

/**
 * This class creates the connection pool for the given configuration.
 *
 * @author  pgussow
 */
public class SMTPConnectionPoolFactory
{
    /**
     * This method creates the SMTP server connection pool based on the given configuration.
     *
     * @param   ssServer   The configuration for the connection pool.
     * @param   eicConfig  The configuration of the connector.
     *
     * @return  The created connection pool.
     */
    public static ISMTPConnectionPool createConnectionPool(ISMTPServer ssServer,
                                                           IEmailIOConfiguration eicConfig)
    {
        ISMTPConnectionPool scpReturn = null;

        scpReturn = new SMTPConnectionPool(ssServer, eicConfig.getManagedComponent());

        return scpReturn;
    }
}
