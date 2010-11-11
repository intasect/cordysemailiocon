

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
