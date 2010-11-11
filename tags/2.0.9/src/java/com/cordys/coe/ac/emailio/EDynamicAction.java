

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
package com.cordys.coe.ac.emailio;

import com.cordys.coe.ac.emailio.method.ArchiveContainers;
import com.cordys.coe.ac.emailio.method.BaseMethod;
import com.cordys.coe.ac.emailio.method.GetConfiguration;
import com.cordys.coe.ac.emailio.method.RegisterTrigger;
import com.cordys.coe.ac.emailio.method.RemoveTrigger;
import com.cordys.coe.ac.emailio.method.RestartContainer;
import com.cordys.coe.ac.emailio.method.SendMail;

/**
 * This enum identifies the different methods that can be executed.
 *
 * @author  pgussow
 */
public enum EDynamicAction
{
    ARCHIVE_CONTAINERS(ArchiveContainers.class),
    GET_CONFIGURATION(GetConfiguration.class),
    REGISTER_TRIGGER(RegisterTrigger.class),
    REMOVE_TRIGGER(RemoveTrigger.class),
    SEND_MAIL(SendMail.class),
    RESTART_CONTAINER(RestartContainer.class);

    /**
     * Holds the implementation class for the method.
     */
    private Class<? extends BaseMethod> m_cImplClass;

    /**
     * Constructor. Creates the action definition.
     *
     * @param  cImplClass  The implementation class for this method.
     */
    EDynamicAction(Class<? extends BaseMethod> cImplClass)
    {
        m_cImplClass = cImplClass;
    }

    /**
     * This method gets the implementation class to use.
     *
     * @return  The implementation class to use.
     */
    public Class<? extends BaseMethod> getImplementationClass()
    {
        return m_cImplClass;
    }
}
