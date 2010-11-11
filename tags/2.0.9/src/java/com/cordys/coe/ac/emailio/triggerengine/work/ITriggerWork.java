

 /**
 * Copyright 2007 Cordys R&D B.V. 
 * 
 * This file is part of the Cordys Email IO Connector. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
