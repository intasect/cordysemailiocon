

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
package com.cordys.coe.ac.emailio.config.action;

import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;
import com.cordys.coe.ac.emailio.localization.EmailIOConfigurationExceptionMessages;

import com.eibus.xml.nom.Node;

/**
 * Factory class for creating actions.
 *
 * @author  pgussow
 */
public class ActionFactory
{
    /**
     * This method will create the proper action object based on the action configuration.
     *
     * @param   iNode  The XML configuration of the action.
     *
     * @return  The created action.
     *
     * @throws  EmailIOConfigurationException  In case of any configuration exceptions.
     */
    public static IAction createAction(int iNode)
                                throws EmailIOConfigurationException
    {
        IAction aReturn = null;

        // Determine the action type
        String sType = Node.getAttribute(iNode, "type", "");

        if (sType.length() == 0)
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_MISSING_ACTUAL_CONFIGURATION_DETAILS);
        }

        EAction aAction = EAction.CUSTOM;

        try
        {
            aAction = EAction.valueOf(sType);
        }
        catch (Exception e)
        {
            throw new EmailIOConfigurationException(e,
                                                    EmailIOConfigurationExceptionMessages.EICE_IS_NOT_A_VALID_ACTION_TYPE,
                                                    sType);
        }

        switch (aAction)
        {
            case SENDMAIL:
                aReturn = new SendMailAction(iNode);
                break;

            case SENDSOAP:
                aReturn = new SendSoapAction(iNode);
                break;

            case CUSTOM:
                aReturn = new CustomAction(iNode);
                break;
        }

        return aReturn;
    }
}
