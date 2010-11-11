

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
import com.cordys.coe.ac.emailio.monitor.IJMXEmailBoxPoller;
import com.cordys.coe.ac.emailio.outbound.ISMIMEConfiguration;
import com.cordys.coe.ac.emailio.storage.IEmailStorageProvider;

import com.eibus.connector.nom.Connector;

import java.util.List;

import javax.mail.Message;

/**
 * This factory creates the trigger engine object.
 *
 * @author  pgussow
 */
public class TriggerEngineFactory
{
    /**
     * This method creates the trigger engine object that can be used.
     *
     * @param   tTrigger      The trigger to execute.
     * @param   alMessages    The list of messages to process.
     * @param   cConnector    The connector to use.
     * @param   ecConnection  The current email connection.
     * @param   sFolderName   The folder name where this message comes from.
     * @param   emcCounter    The counter component for keeping track of messages.
     * @param   ebEmailBox    The email box from which the messages are pulled.
     * @param   espStorage    The storage provider for this email box.
     * @param   scConfig      The S/MIME configuration.
     *
     * @return  The trigger engine object to use.
     */
    public static ITriggerEngine createTriggerEngine(ITrigger tTrigger, List<Message> alMessages,
                                                     Connector cConnector,
                                                     IEmailConnection ecConnection,
                                                     String sFolderName,
                                                     IJMXEmailBoxPoller emcCounter,
                                                     IEmailBox ebEmailBox,
                                                     IEmailStorageProvider espStorage,
                                                     ISMIMEConfiguration scConfig)
    {
        ITriggerEngine teReturn = new TriggerEngine();

        teReturn.initialize(tTrigger, alMessages, cConnector, ecConnection, sFolderName, emcCounter,
                            ebEmailBox, espStorage, scConfig);

        return teReturn;
    }
}
