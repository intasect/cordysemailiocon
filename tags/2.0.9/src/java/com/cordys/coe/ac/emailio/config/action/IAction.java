package com.cordys.coe.ac.emailio.config.action;

import com.cordys.coe.ac.emailio.config.IXMLSerializable;
import com.cordys.coe.ac.emailio.config.rule.IRuleContext;
import com.cordys.coe.ac.emailio.exception.ActionException;
import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;
import com.cordys.coe.util.soap.SOAPWrapper;

import javax.mail.Message;

/**
 * This interface describes the actions that can be defined for any event.
 *
 * @author  pgussow
 */
public interface IAction extends IXMLSerializable
{
    /**
     * This method is called to actually execute the action.
     *
     * @param   pcContext  The pattern context.
     * @param   mMessage   The actual email message for which the action should be executed.
     * @param   swSoap     The SOAP wrapper that could be used by this action if needed.
     *
     * @throws  ActionException  In case of any exceptions
     */
    void execute(IRuleContext pcContext, Message mMessage, SOAPWrapper swSoap)
          throws ActionException;

    /**
     * This method gets the ID of the action.
     *
     * @return  The ID of the action.
     */
    String getID();

    /**
     * This method is called to validate the configuration. This method is used to i.e. check if the
     * email server is reachable or that the messages configured are actually sendable.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    void validate()
           throws EmailIOConfigurationException;
}
