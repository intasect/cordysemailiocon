package com.cordys.coe.ac.emailio.config.token;

/**
 * Interface for storage tokens.
 *
 * @author  pgussow
 */
public interface IStorageToken extends IToken
{
    /**
     * This method gets the source for the value.
     *
     * @return  The source for the value.
     */
    EStoreSource getSource();
}
