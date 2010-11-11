

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

import com.cordys.coe.ac.emailio.localization.LogMessages;

import com.cordys.cpc.bsf.busobject.BusObjectConfig;
import com.cordys.cpc.bsf.busobject.exception.BsfApplicationRuntimeException;

/**
 * This class wraps the actual context container .
 *
 * @author  pgussow
 */
public class ContextContainer extends ContextContainerBase
    implements IContextContainer
{
    /**
     * Creates a new ContextContainer object.
     */
    public ContextContainer()
    {
        this((BusObjectConfig) null);
    }

    /**
     * Creates a new ContextContainer object.
     *
     * @param  config  The configuration.
     */
    public ContextContainer(BusObjectConfig config)
    {
        super(config);
    }

    /**
     * This method gets the container with the given ID.
     *
     * @param   sID  The ID of the container.
     *
     * @return  The container with the given ID.
     */
    public static ContextContainer getContextContainer(String sID)
    {
        // TODO implement body
        return null;
    }

    /**
     * @see  com.cordys.cpc.bsf.busobject.BusObject#onDelete()
     */
    @Override public void onDelete()
    {
        throw new BsfApplicationRuntimeException(LogMessages.OPERATION_NOT_SUPPORTED, "DELETE");
    }

    /**
     * @see  com.cordys.cpc.bsf.busobject.BusObject#onInsert()
     */
    @Override public void onInsert()
    {
        throw new BsfApplicationRuntimeException(LogMessages.OPERATION_NOT_SUPPORTED, "INSERT");
    }

    /**
     * @see  com.cordys.cpc.bsf.busobject.BusObject#onUpdate()
     */
    @Override public void onUpdate()
    {
        throw new BsfApplicationRuntimeException(LogMessages.OPERATION_NOT_SUPPORTED, "UPDATE");
    }
}
