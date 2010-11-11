

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
package com.cordys.coe.ac.emailio.config.trigger;

import com.cordys.coe.ac.emailio.config.IXMLSerializable;
import com.cordys.coe.ac.emailio.config.action.EEventType;
import com.cordys.coe.ac.emailio.config.action.IAction;
import com.cordys.coe.ac.emailio.config.message.IMessage;
import com.cordys.coe.ac.emailio.config.rule.IRule;
import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;

import java.util.ArrayList;

/**
 * This interface describes the trigger definition.
 *
 * @author  pgussow
 */
public interface ITrigger extends IXMLSerializable
{
    /**
     * Holds the name of the attribute 'appliesTo'.
     */
    String ATTR_APPLIES_TO = "appliesTo";
    /**
     * Holds the name of the attribute 'type'.
     */
    String ATTR_EVENT_TYPE = "type";
    /**
     * Holds the name of the attribute 'onetimeonly'.
     */
    String ATTR_ONETIMEONLY = "onetimeonly";
    /**
     * Holds the default priority.
     */
    int DEFAULT_PRIORITY = 1000;
    /**
     * Holds the name of the tag 'actionref'.
     */
    String TAG_ACTIONREF = "actionref";
    /**
     * Holds the name of the tag 'combineemails'.
     */
    String TAG_COMBINE_EMAILS = "combineemails";
    /**
     * Holds the name of the tag 'description'.
     */
    String TAG_DESCRIPTION = "description";
    /**
     * Holds the name of the tag 'name'.
     */
    String TAG_NAME = "name";
    /**
     * Holds the name of the tag 'priority'.
     */
    String TAG_PRIORITY = "priority";
    /**
     * Holds the name of the tag 'trigger'.
     */
    String TAG_TRIGGER = "trigger";

    /**
     * This method returns whether or not this trigger applies to the given folder name.
     *
     * @param   sFolderName  The name of the folder.
     *
     * @return  true if this trigger applies to this folder.
     */
    boolean appliesTo(String sFolderName);

    /**
     * This method returns all actions for the given event type.
     *
     * @param   etEvent  The event type.
     *
     * @return  All actions for the event type.
     */
    ArrayList<IAction> getActions(EEventType etEvent);

    /**
     * This method gets whether or not this trigger should combine emails into a single request.
     *
     * @return  Whether or not this trigger should combine emails into a single request.
     */
    boolean getCombineEmails();

    /**
     * This method gets the description of the trigger.
     *
     * @return  The description of the trigger.
     */
    String getDescription();

    /**
     * This method gets the name of the folders to which this trigger should apply.
     *
     * @return  The name of the folders to which this trigger should apply.
     */
    String[] getFolders();

    /**
     * This method gets the message definition of the soap message that will be sent.
     *
     * @return  The message definition of the soap message that will be sent.
     */
    IMessage getMessage();

    /**
     * This method gets the name of the trigger.
     *
     * @return  The name of the trigger.
     */
    String getName();

    /**
     * This method gets the priority for this rule.
     *
     * @return  The priority for this rule.
     */
    int getPriority();

    /**
     * This method gets the rules for this trigger.
     *
     * @return  The rules for this trigger.
     */
    IRule[] getRules();

    /**
     * This method gets whether or not this trigger is a one time only trigger. This means that as
     * soon as an email matches this trigger, the trigger should be removed from the current list of
     * triggers for the email box.
     *
     * @return  Whether or not this trigger is a one time only trigger.
     */
    boolean isOneTimeOnly();

    /**
     * This method is called to validate the configuration. This method is used to i.e. check if the
     * email server is reachable or that the messages configured are actually sendable.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    void validate()
           throws EmailIOConfigurationException;
}
