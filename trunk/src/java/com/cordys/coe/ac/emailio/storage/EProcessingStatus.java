/**
 * Copyright 2005 Cordys R&D B.V. 
 * 
 * This file is part of the Cordys SAP Connector. 
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
 /**
 * Copyright 2005 Cordys R&D B.V. 
 * 
 * This file is part of the Cordys SAP Connector. 
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
 package com.cordys.coe.ac.emailio.storage;

/**
 * This enum describes the different statuses that an email can have.
 *
 * @author  pgussow
 */
public enum EProcessingStatus
{
    /**
     * The email has matched a trigger and now needs to be executed.
     */
    INITIAL,
    /**
     * The trigger is in the process of being executed.
     */
    IN_PROGRESS,
    /**
     * The trigger has been executed and the trigger is finished.
     */
    COMPLETED,
    /**
     * The trigger execution resulted in an error.
     */
    MESSAGE_ERROR,
    /**
     * The trigger executed ok, but the actions failed.
     */
    ACTION_ERROR
}
