package com.cordys.coe.ac.emailio.config.rule;

import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;

/**
 * This factory allows the creation of rules.
 *
 * @author  pgussow
 */
public class RuleFactory
{
    /**
     * This method creates a rule object.
     *
     * @param   iNode  The configuration XML.
     *
     * @return  The rule object.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    public static IRule createRule(int iNode)
                            throws EmailIOConfigurationException
    {
        return new Rule(iNode);
    }
}
