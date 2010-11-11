

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
package com.cordys.coe.ac.emailio.triggerengine;

import com.cordys.coe.ac.emailio.config.IEmailBox;
import com.cordys.coe.ac.emailio.config.trigger.ITrigger;
import com.cordys.coe.ac.emailio.connection.IEmailConnection;
import com.cordys.coe.ac.emailio.exception.TriggerEngineException;
import com.cordys.coe.ac.emailio.monitor.IJMXEmailBoxPoller;
import com.cordys.coe.ac.emailio.outbound.ISMIMEConfiguration;
import com.cordys.coe.ac.emailio.storage.IEmailStorageProvider;

import com.eibus.connector.nom.Connector;

import com.eibus.util.threadpool.Dispatcher;

import java.util.List;

import javax.mail.Message;

/**
 * This interface describes what the trigger engine can do.
 *
 * @author  pgussow
 */
public interface ITriggerEngine
{
    /**
     * This method gets the trigger for the current engine.
     *
     * @return  The trigger for the current engine.
     */
    ITrigger getTrigger();

    /**
     * This method will handle the trigger to see if this message matches the current trigger. If
     * something goes wrong validating the trigger an exception will be thrown. If the email message
     * did not match the trigger it will return false. If the message did match the trigger and
     * needs to be handled it will return true.<br>
     * TODO: We need to be aware of timing issues. What to do with a message which is put into
     * processing mode (into the dispatcher) and then it is picked up again in the next polling
     * interval?
     *
     * @param   dDispatcher  The dispatcher to assign the work if the messages match the trigger.
     *
     * @return  true if at least 1 of the messages in the list was processed.
     *
     * @throws  TriggerEngineException  In case of any exception.
     */
    boolean handleTrigger(Dispatcher dDispatcher)
                   throws TriggerEngineException;

    /**
     * This method initializes the trigger engine object.
     *
     * @param  tTrigger      The trigger to execute.
     * @param  alMessages    The list of messages to process.
     * @param  cConnector    The connector to use.
     * @param  ecConnection  The current email connection.
     * @param  sFolderName   The folder name where this message comes from.
     * @param  emcCounter    The counter component for keeping track of messages.
     * @param  ebEmailBox    The email box from which the messages are pulled.
     * @param  espStorage    The storage provider for this email box.
     * @param  scConfig      The S/MIME configuration.
     */
    void initialize(ITrigger tTrigger, List<Message> alMessages, Connector cConnector,
                    IEmailConnection ecConnection, String sFolderName,
                    IJMXEmailBoxPoller emcCounter, IEmailBox ebEmailBox,
                    IEmailStorageProvider espStorage, ISMIMEConfiguration scConfig);
}
