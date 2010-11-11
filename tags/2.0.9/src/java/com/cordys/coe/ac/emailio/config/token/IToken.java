package com.cordys.coe.ac.emailio.config.token;

import com.cordys.coe.ac.emailio.config.IXMLSerializable;

/**
 * General interface for replacement and storage tokens.
 *
 * @author  pgussow
 */
public interface IToken extends IXMLSerializable
{
    /**
     * This method gets the name for this token.
     *
     * @return  The name for this token.
     */
    String getName();

    /**
     * This method gets the value for this token.
     *
     * @return  The value for this token.
     */
    String getValue();
}
