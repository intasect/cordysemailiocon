package com.cordys.coe.ac.emailio.config.action;

import com.cordys.coe.ac.emailio.config.rule.IRuleContext;
import com.cordys.coe.ac.emailio.exception.ActionException;

import javax.mail.Message;

/**
 * All custom actions should implement this interface.
 *
 * @author  pgussow
 */
public interface ICustomAction
{
    /**
     * This method is called to configure the custom action with the XML.
     *
     * @param  iNode  The XML configuration.
     */
    void configure(int iNode);

    /**
     * This method is called to actually execute the action.
     *
     * @param   pcContext  The pattern context.
     * @param   mMessage   The actual email message for which the action should be executed.
     *
     * @throws  ActionException  In case of any exception.
     */
    void execute(IRuleContext pcContext, Message mMessage)
          throws ActionException;
}
