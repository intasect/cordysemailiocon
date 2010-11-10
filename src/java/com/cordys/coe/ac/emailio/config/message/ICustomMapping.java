package com.cordys.coe.ac.emailio.config.message;

import com.cordys.coe.ac.emailio.config.rule.IRuleContext;
import com.cordys.coe.ac.emailio.exception.TriggerEngineException;

/**
 * This interface is used for doing custom mappings.
 *
 * @author  pgussow
 */
public interface ICustomMapping
{
    /**
     * This method executes the mapping. It will execute the XPath on the given context node. When
     * it's found it will delete all the exeisting children of that node. Then based on the type of
     * the value in the context it will either create a text node (String value) or append an XML
     * structure (int).
     *
     * @param   rcContext     The rule context to get the values from.
     * @param   iContextNode  The context node.
     * @param   mMessage      The parent message.
     * @param   mMapping      The mapping definition.
     *
     * @throws  TriggerEngineException  In case of any exceptions.
     */
    void execute(IRuleContext rcContext, int iContextNode, IMessage mMessage, IMapping mMapping)
          throws TriggerEngineException;
}
