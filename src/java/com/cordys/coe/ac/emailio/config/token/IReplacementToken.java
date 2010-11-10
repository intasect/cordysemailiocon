package com.cordys.coe.ac.emailio.config.token;

import com.cordys.coe.ac.emailio.config.rule.IRuleContext;

/**
 * Interface for replacement tokens.
 *
 * @author  pgussow
 */
public interface IReplacementToken extends IToken
{
    /**
     * This method gets the value for this replacement token based on the current context.
     *
     * @param   pcContext  The current context.
     *
     * @return  The value for this replacement token based on the current context.
     */
    Object getValue(IRuleContext pcContext);
}
