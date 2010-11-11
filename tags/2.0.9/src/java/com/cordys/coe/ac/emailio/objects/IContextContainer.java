

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
package com.cordys.coe.ac.emailio.objects;

import java.util.Date;

/**
 * This interface describes the container context data. A container contains 1 or more email
 * messages that matched a certain trigger.
 *
 * @author  pgussow
 */
public interface IContextContainer
{
    /**
     * This method gets the date on which the processing was completed.
     *
     * @return  The date on which the processing was completed.
     */
    Date getCompleteDate();

    /**
     * This method gets the date on which this container was created.
     *
     * @return  The date on which this container was created.
     */
    Date getCreateDate();

    /**
     * This method gets the ID of the email box.
     *
     * @return  The ID of the email box.
     */
    String getEmailBoxID();

    /**
     * This method gets the ID for this container.
     *
     * @return  The ID for this container.
     */
    String getID();

    /**
     * This method gets the processing status for this container.
     *
     * @return  The processing status for this container.
     */
    String getProcessingStatus();

    /**
     * This method gets the date on which the container's state changed to the current state.
     *
     * @return  The date on which the container's state changed to the current state.
     */
    Date getProcessingStatusChangeDate();

    /**
     * This method gets the optional status information.
     *
     * @return  The optional status information.
     */
    String getStatusInformation();

    /**
     * This method gets the definition of the trigger this container matched.
     *
     * @return  The definition of the trigger this container matched.
     */
    String getTriggerDefinition();

    /**
     * This method sets the date on which the processing was completed.
     *
     * @param  dCompleteDate  The date on which the processing was completed.
     */
    void setCompleteDate(Date dCompleteDate);

    /**
     * This method sets the date on which this container was created.
     *
     * @param  dCreateDate  The date on which this container was created.
     */
    void setCreateDate(Date dCreateDate);

    /**
     * This method sets the ID of the email box.
     *
     * @param  sEmailBoxID  The ID of the email box.
     */
    void setEmailBoxID(String sEmailBoxID);

    /**
     * This method sets the ID for this container.
     *
     * @param  sID  The ID for this container.
     */
    void setID(String sID);

    /**
     * This method sets the processing status for this container.
     *
     * @param  sStatus  The processing status for this container.
     */
    void setProcessingStatus(String sStatus);

    /**
     * This method sets the date on which the container's state changed to the current state.
     *
     * @param  dProcessingStatusChangeDate  The date on which the container's state changed to the
     *                                      current state.
     */
    void setProcessingStatusChangeDate(Date dProcessingStatusChangeDate);

    /**
     * This method sets the optional status information.
     *
     * @param  sStatusInformation  The optional status information.
     */
    void setStatusInformation(String sStatusInformation);

    /**
     * This method sets the definition of the trigger this container matched.
     *
     * @param  sTriggerDefinition  The definition of the trigger this container matched.
     */
    void setTriggerDefinition(String sTriggerDefinition);
}
