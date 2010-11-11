

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
package com.cordys.coe.ac.emailio.monitor;

/**
 * This interface describes the methods to handle the statistic counters which are used for JMX.
 *
 * @author  pgussow
 */
public interface IJMXEmailBoxPoller
{
    /**
     * This method increases the total number of messages that failed to process.
     *
     * @param  lAmount  The amount of messages to add to the counter.
     */
    void incNrOfMessagesFailed(long lAmount);

    /**
     * This method increases the total number of messages that have been ignored because they didn't
     * match any trigger.
     *
     * @param  lAmount  The amount of messages to add to the counter.
     */
    void incNrOfMessagesIgnored(long lAmount);

    /**
     * This method increases the total number of messages that have been processed.
     *
     * @param  lAmount  The amount of messages to add to the counter.
     */
    void incNrOfMessagesProcessed(long lAmount);

    /**
     * This method increases the total number of messages that have been successfully processed.
     *
     * @param  lAmount  The amount of messages to add to the counter.
     */
    void incNrOfMessagesSuccess(long lAmount);

    /**
     * This method can be used to send out an alert that a specific message failed to process.
     *
     * @param  tException  The exception that occurred.
     * @param  sContext    The list with all context information.
     */
    void notifyProcessingError(Throwable tException, String sContext);

    /**
     * This method resets the counters to 0.
     */
    void reset();
}
