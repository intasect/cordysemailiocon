package com.cordys.coe.ac.emailio.triggerengine.work;

import com.cordys.coe.ac.emailio.config.IEmailBox;
import com.cordys.coe.ac.emailio.config.message.IMessage;
import com.cordys.coe.ac.emailio.config.trigger.ITrigger;
import com.cordys.coe.ac.emailio.monitor.IJMXEmailBoxPoller;
import com.cordys.coe.ac.emailio.monitor.RuleContextContainer;

import com.eibus.connector.nom.Connector;

/**
 * This factory creates the trigger work objects.
 *
 * @author  pgussow
 */
public class TriggerWorkFactory
{
    /**
     * This method creates the trigger work object.
     *
     * @param   rccContext    All the message context that have to be sent.
     * @param   mSOAPMessage  The message template.
     * @param   tTrigger      The trigger that was matched.
     * @param   cConnector    The Cordys connector to use.
     * @param   emcCounter    The email message counter to report success or failure to.
     * @param   ebEmailBox    The email box this work originated from.
     *
     * @return  The created trigger work object.
     */
    public static ITriggerWork createTriggerWork(RuleContextContainer rccContext,
                                                 IMessage mSOAPMessage, ITrigger tTrigger,
                                                 Connector cConnector,
                                                 IJMXEmailBoxPoller emcCounter,
                                                 IEmailBox ebEmailBox)
    {
        ITriggerWork twReturn = null;

        twReturn = new TriggerWork();
        twReturn.initialize(rccContext, mSOAPMessage, tTrigger, cConnector, emcCounter, ebEmailBox);

        return twReturn;
    }
}
