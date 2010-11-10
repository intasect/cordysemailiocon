package com.cordys.coe.ac.emailio.triggerengine.tcb;

import com.cordys.coe.ac.emailio.config.trigger.ITrigger;
import com.cordys.coe.ac.emailio.exception.TriggerEngineException;
import com.cordys.coe.ac.emailio.monitor.RuleContext;
import com.cordys.coe.ac.emailio.outbound.ISMIMEConfiguration;

import javax.mail.Message;

/**
 * This factory creates the context builder for the message.
 *
 * @author  pgussow
 */
public class TriggerContextBuilderFactory
{
    /**
     * This method creates a context builder.
     *
     * @param   rcContext  The rule context to add the information to for this message.
     * @param   mMessage   The actual email message.
     * @param   tTrigger   The definition of the trigger.
     * @param   scConfig   The S/MIME configuration details.
     *
     * @return  The created triggerContextBuilder.
     *
     * @throws  TriggerEngineException  In case of any exceptions.
     */
    public static ITriggerContextBuilder createTriggerContextBuilder(RuleContext rcContext,
                                                                     Message mMessage,
                                                                     ITrigger tTrigger,
                                                                     ISMIMEConfiguration scConfig)
                                                              throws TriggerEngineException
    {
        ITriggerContextBuilder tcbReturn = null;

        tcbReturn = new TriggerContextBuilder();
        tcbReturn.initialize(rcContext, mMessage, tTrigger, scConfig);

        return tcbReturn;
    }
}
