package com.cordys.coe.ac.emailio.config.outbound;

import com.cordys.coe.ac.emailio.config.IMailServer;

/**
 * This interface describes the settings for a SMTP server.
 *
 * @author  pgussow
 */
public interface ISMTPServer extends IMailServer
{
    /**
     * Holds the default connection timeout.
     */
    long DEFAULT_CONNECTION_TIMEOUT = 60000L;
    /**
     * Holds the default number of connections in the connection pool.
     */
    int DEFAULT_MAX_NR_OF_CONNECTIONS = 5;
    /**
     * Holds the name of the tag 'connectiontimeout'.
     */
    String TAG_CONNECTION_TIMEOUT = "connectiontimeout";
    /**
     * Holds the name of the tag 'maxconnections'.
     */
    String TAG_MAX_NR_OF_CONNECTIONS = "maxconnections";
    /**
     * Holds the name of the tag 'useconnectionpool'.
     */
    String TAG_USE_CONNECTION_POOL = "useconnectionpool";

    /**
     * This method gets the connection timeout. The default timeout is 60 seconds.
     *
     * @return  The connection timeout. The default timeout is 60 seconds.
     */
    long getConnectionTimeout();

    /**
     * This method gets the maximum number of connections for the connection pool. If not specified
     * the default is 5.
     *
     * @return  The maximum number of connections for the connection pool. If not specified the
     *          default is 5.
     */
    int getMaxNrOfConnections();

    /**
     * This method gets whether or not a connection pool should be used. When set to true it means a
     * connection pool will be created with maxnrofconnections. And those connections will be
     * recycled if needed. When the timeout has expired on the connection it will disconnect and
     * reconnect. If the SMTP requires authentication the connection pooling cannot be used at all.
     *
     * @return  Whether or not a connection pool should be used.
     */
    boolean useConnectionPool();
}
