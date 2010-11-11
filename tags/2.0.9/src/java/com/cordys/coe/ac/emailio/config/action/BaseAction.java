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
 package com.cordys.coe.ac.emailio.config.action;

import com.cordys.coe.ac.emailio.EmailIOConnectorConstants;
import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;
import com.cordys.coe.ac.emailio.localization.EmailIOConfigurationExceptionMessages;

import com.eibus.xml.nom.Node;

/**
 * This class can be used for a base action class.
 *
 * @author  pgussow
 */
abstract class BaseAction
    implements IAction
{
    /**
     * Holds an empty string.
     */
    protected static final String EMPTY_STRING = "";
    /**
     * Holds the name of the tag 'action'.
     */
    private static final String TAG_ACTION = "action";
    /**
     * Holds the name of the attribute 'id'.
     */
    private static final String ATTR_ID = "id";
    /**
     * Holds the name of the attribute 'id'.
     */
    private static final String ATTR_TYPE = "type";
    /**
     * Holds the type of action.
     */
    private EAction m_aActionType;
    /**
     * Holds the ID of the action.
     */
    private String m_sID;

    /**
     * Creates a new BaseAction object.
     *
     * @param   iNode        The configuration node.
     * @param   aActionType  The type of action.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    public BaseAction(int iNode, EAction aActionType)
               throws EmailIOConfigurationException
    {
        if (iNode == 0)
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_MISSING_ACTION_CONFIGURATION_NODE);
        }

        m_aActionType = aActionType;
        m_sID = Node.getAttribute(iNode, ATTR_ID, "");

        if (m_sID.length() == 0)
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_ACTION_ID_MUST_BE_SPECIFIED);
        }
    }

    /**
     * This method gets the ID of the action.
     *
     * @return  The ID of the action.
     *
     * @see     com.cordys.coe.ac.emailio.config.action.IAction#getID()
     */
    public String getID()
    {
        return m_sID;
    }

    /**
     * This method returns a string representation of the object.
     *
     * @return  A string representation of the object.
     *
     * @see     java.lang.Object#toString()
     */
    @Override public String toString()
    {
        StringBuilder sbReturn = new StringBuilder();

        sbReturn.append("Action ID: " + getID());

        return sbReturn.toString();
    }

    /**
     * This method dumps the configuration of this action to XML.
     *
     * @param   iParent  The parent element.
     *
     * @return  The created XML structure.
     *
     * @see     com.cordys.coe.ac.emailio.config.action.IAction#toXML(int)
     */
    public int toXML(int iParent)
    {
        int iReturn = 0;

        iReturn = Node.createElementNS(TAG_ACTION, EMPTY_STRING, EMPTY_STRING,
                                       EmailIOConnectorConstants.NS_CONFIGURATION, iParent);

        Node.setAttribute(iReturn, ATTR_ID, getID());
        Node.setAttribute(iReturn, ATTR_TYPE, m_aActionType.name());

        return iReturn;
    }

    /**
     * This method is called to validate the configuration. This method is used to i.e. check if the
     * email server is reachable or that the messages configured are actually sendable.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     *
     * @see     com.cordys.coe.ac.emailio.config.action.IAction#validate()
     */
    @Override public void validate()
                            throws EmailIOConfigurationException
    {
    }
}
