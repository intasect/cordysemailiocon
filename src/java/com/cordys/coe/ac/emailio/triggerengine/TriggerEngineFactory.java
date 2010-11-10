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
