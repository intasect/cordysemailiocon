package com.cordys.coe.ac.emailio.triggerengine.tcb;

import com.cordys.coe.ac.emailio.config.trigger.ITrigger;
import com.cordys.coe.ac.emailio.exception.TriggerEngineException;
import com.cordys.coe.ac.emailio.monitor.RuleContext;
import com.cordys.coe.ac.emailio.outbound.ISMIMEConfiguration;

import javax.mail.Message;

/**
 * This interface describes the context builder.
 *
 * @author  pgussow
 */
public interface ITriggerContextBuilder
{
    /**
     * This method initializes the context builder.
     *
     * @param   rcContext  The rule context to add the information to for this message.
     * @param   mMessage   The actual email message.
     * @param   tTrigger   The definition of the trigger.
     * @param   scConfig   The S/MIME configuration details.
     *
     * @throws  TriggerEngineException  In case of any exceptions.
     */
    void initialize(RuleContext rcContext, Message mMessage, ITrigger tTrigger,
                    ISMIMEConfiguration scConfig)
             throws TriggerEngineException;

    /**
     * This method actually processes all the rules that are defined for this trigger. It will walk
     * through all defined rules and patterns and evalutate them. The result of that evaluation it
     * will store in the RuleContext object. If this message actually matches the patterns as
     * described it will return true.
     *
     * @return  true if the message will be processed. Otherwise false.
     *
     * @throws  TriggerEngineException  In case of any exceptions.
     */
    boolean processMessage()
                    throws TriggerEngineException;
}
