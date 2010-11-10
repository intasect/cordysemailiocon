package com.cordys.coe.ac.emailio.triggerengine.work;

import com.cordys.coe.ac.emailio.config.IEmailBox;
import com.cordys.coe.ac.emailio.config.message.IMessage;
import com.cordys.coe.ac.emailio.config.trigger.ITrigger;
import com.cordys.coe.ac.emailio.monitor.IJMXEmailBoxPoller;
import com.cordys.coe.ac.emailio.monitor.RuleContextContainer;

import com.eibus.connector.nom.Connector;

import com.eibus.util.threadpool.Work;

/**
 * This interface describes the work that needs to be done for a trigger.
 *
 * @author  pgussow
 */
public interface ITriggerWork extends Work
{
    /**
     * This method initializes the work object.
     *
     * @param  rccContext  All the message context that have to be sent.
     * @param  mMessage    The message template.
     * @param  tTrigger    The trigger that was matched.
     * @param  cConnector  The Cordys connector to use.
     * @param  emcCounter  The email message counter to report success or failure to.
     * @param  ebEmailBox  The email box this work originated from.
     */
    void initialize(RuleContextContainer rccContext, IMessage mMessage, ITrigger tTrigger,
                    Connector cConnector, IJMXEmailBoxPoller emcCounter, IEmailBox ebEmailBox);
}
