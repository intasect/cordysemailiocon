package com.cordys.coe.ac.emailio.config.token;

import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;

/**
 * This factory can be used to parse the token definitions.
 *
 * @author  pgussow
 */
public class TokenFactory
{
    /**
     * This method creates a replacement token.
     *
     * @param   iNode  The configuration of the token.
     *
     * @return  The replacement token.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    public static IReplacementToken createReplacementToken(int iNode)
                                                    throws EmailIOConfigurationException
    {
        return new ReplacementToken(iNode);
    }

    /**
     * This method creates a storage token.
     *
     * @param   iNode  The configuration of the token.
     *
     * @return  The storage token.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    public static IStorageToken createStorageToken(int iNode)
                                            throws EmailIOConfigurationException
    {
        return new StoreToken(iNode);
    }
}
