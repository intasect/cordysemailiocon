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
 package com.cordys.coe.ac.emailio.config.trigger;

import com.cordys.coe.ac.emailio.config.action.IActionStore;
import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;

import com.eibus.util.system.Native;

/**
 * This factory allows the creation of triggers.
 *
 * @author  pgussow
 */
public class TriggerFactory
{
    /**
     * This method creates a trigger object.
     *
     * @param   iNode    The configuration XML.
     * @param   asStore  Holds the store for action references.
     *
     * @return  The trigger object.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions
     */
    public static ITrigger createTrigger(int iNode, IActionStore asStore)
                                  throws EmailIOConfigurationException
    {
        return new Trigger(iNode, asStore);
    }

    /**
     * This method creates a trigger object and renames it so it is unique.
     *
     * <p>This method generates a GUID and prepends it to the name of the trigger that was
     * originally specified. If the configuration contains the name "foo", the name of the trigger
     * that's actually created will be something like "{1234-1234-1234-1234}-foo".</p>
     * <b>Note that the original XML node conatining the config will not be changed by this, only
     * the trigger object itself is affected.</b> <b>This method is used by the webservice for
     * registering dynamic triggers as unique names are very important and hard to create in that
     * context.</b>
     *
     * @param   iNode    The configuration XML.
     * @param   asStore  Holds the store for action references.
     *
     * @return  The uniquely-named trigger object.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions
     */
    public static ITrigger createUniqueTrigger(int iNode, IActionStore asStore)
                                        throws EmailIOConfigurationException
    {
        Trigger tReturn = new Trigger(iNode, asStore);

        tReturn.setName(Native.createGuid() + "-" + tReturn.getName());

        return tReturn;
    }
}
