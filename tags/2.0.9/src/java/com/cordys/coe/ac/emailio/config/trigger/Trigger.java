

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

import com.cordys.coe.ac.emailio.EmailIOConnectorConstants;
import com.cordys.coe.ac.emailio.config.action.ActionFactory;
import com.cordys.coe.ac.emailio.config.action.EEventType;
import com.cordys.coe.ac.emailio.config.action.IAction;
import com.cordys.coe.ac.emailio.config.action.IActionStore;
import com.cordys.coe.ac.emailio.config.message.IMessage;
import com.cordys.coe.ac.emailio.config.message.MessageFactory;
import com.cordys.coe.ac.emailio.config.rule.IRule;
import com.cordys.coe.ac.emailio.config.rule.RuleFactory;
import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;
import com.cordys.coe.ac.emailio.localization.EmailIOConfigurationExceptionMessages;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class holds the configuration of a trigger.
 *
 * @author  pgussow
 */
class Trigger
    implements ITrigger
{
    /**
     * Holds the default value for the combineemails parameter (false).
     */
    private static final boolean DEFAULT_COMBINE_EMAILS = false;

    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(Trigger.class);
    /**
     * Holds an empty string.
     */
    private static final String EMPTY_STRING = "";
    /**
     * Holds the configured rules.
     */
    private ArrayList<IRule> m_alRules = new ArrayList<IRule>();
    /**
     * Holds the list of folders this trigger should be applied to.
     */
    private String[] m_asFolders;
    /**
     * Holds whether or not multiple emails should be combined into a single SOAP call.
     */
    private boolean m_bCombineEmails;
    /**
     * Indicates whether or not this is a one-time only schedule.
     */
    private boolean m_bOneTimeOnly;
    /**
     * Holds all events defined for this trigger.
     */
    private Map<EEventType, ArrayList<IAction>> m_hmEvents = Collections.synchronizedMap(new LinkedHashMap<EEventType, ArrayList<IAction>>());
    /**
     * Holds the priority of the current trigger.
     */
    private int m_iPriority;
    /**
     * Holds the message definition of the soap message that wiull be sent.
     */
    private IMessage m_mMessage;
    /**
     * Holds the description of the trigger.
     */
    private String m_sDescription;
    /**
     * Holds the name of the trigger.
     */
    private String m_sName;

    /**
     * Creates a new Trigger object.
     *
     * @param   iNode    The configuration node.
     * @param   asStore  The action store to retrieve action references from.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    public Trigger(int iNode, IActionStore asStore)
            throws EmailIOConfigurationException
    {
        XPathMetaInfo xmi = new XPathMetaInfo();
        xmi.addNamespaceBinding("ns", EmailIOConnectorConstants.NS_CONFIGURATION);

        if (iNode == 0)
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_ERROR_CREATING_PROPERTIES);
        }

        m_sName = XPathHelper.getStringValue(iNode, "./ns:" + TAG_NAME + "/text()", xmi,
                                             EMPTY_STRING);
        m_sDescription = XPathHelper.getStringValue(iNode, "./ns:" + TAG_DESCRIPTION + "/text()",
                                                    xmi, EMPTY_STRING);
        m_bCombineEmails = XPathHelper.getBooleanValue(iNode,
                                                       "./ns:" + TAG_COMBINE_EMAILS + "/text()",
                                                       xmi, DEFAULT_COMBINE_EMAILS);

        m_bOneTimeOnly = Boolean.valueOf(Node.getAttribute(iNode, ATTR_ONETIMEONLY, "false"))
                                .booleanValue();

        m_iPriority = XPathHelper.getIntegerValue(iNode, "./ns:" + TAG_PRIORITY + "/text()", xmi,
                                                  DEFAULT_PRIORITY);

        // Check the parameters
        if (m_sName.length() == 0)
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_MISSING_TRIGGER_NAME);
        }

        String sApplyTo = Node.getAttribute(iNode, ATTR_APPLIES_TO, "");

        if (sApplyTo.length() > 0)
        {
            m_asFolders = sApplyTo.split(",");
        }

        // Parse the rules
        int[] aiRules = XPathHelper.selectNodes(iNode, "./ns:rules/ns:rule", xmi);

        if (aiRules.length == 0)
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_NO_RULES_CONFIGURED_FOR_THIS_TRIGGER);
        }

        for (int iCount = 0; iCount < aiRules.length; iCount++)
        {
            IRule rRule = RuleFactory.createRule(aiRules[iCount]);
            m_alRules.add(rRule);
        }

        // Parse the message node.
        int iMessageNode = XPathHelper.selectSingleNode(iNode, "./ns:message", xmi);

        if (iMessageNode == 0)
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_NO_MESSAGE_DEFINED_FOR_THIS_TRIGGER);
        }

        m_mMessage = MessageFactory.createMessage(iMessageNode);

        // Parse the events.
        // TODO: Add more parameter checking.
        int[] aiEvents = XPathHelper.selectNodes(iNode, "./ns:events/ns:event", xmi);

        for (int iEvent : aiEvents)
        {
            String sType = Node.getAttribute(iEvent, "type", EEventType.SUCCESS.name());
            EEventType etType = EEventType.valueOf(sType);

            ArrayList<IAction> alActions = null;

            if (m_hmEvents.containsKey(etType))
            {
                alActions = m_hmEvents.get(etType);
            }
            else
            {
                alActions = new ArrayList<IAction>();
                m_hmEvents.put(etType, alActions);
            }

            // Now we'll parse the action and actionrefs.
            int[] aiActions = XPathHelper.selectNodes(iEvent,
                                                      "./*[local-name()=\"action\" or local-name()=\"actionref\"]",
                                                      xmi);

            for (int iAction : aiActions)
            {
                if (TAG_ACTIONREF.equals(Node.getLocalName(iAction)))
                {
                    String sActionID = Node.getAttribute(iAction, "id");
                    IAction aReferenced = asStore.getAction(sActionID);

                    if (aReferenced == null)
                    {
                        // The configuration is invalid. It's using a reference to an action which
                        // is out of scope.
                        throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_COULD_NOT_FIND_THE_ACTION_WITH_ID_0_THE_INVALID_REFERENCE_IS_IN_TRIGGER_1,
                                                                sActionID, m_sName);
                    }
                    alActions.add(aReferenced);
                }
                else
                {
                    // A normal nested action.
                    alActions.add(ActionFactory.createAction(iAction));
                }
            }
        }

        // OneTimeOnly triggers cannot combine emails.
        if ((getCombineEmails() == true) && isOneTimeOnly())
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_CANNOT_CREATE_A_ONETIMEONLY_TRIGGER_WHICH_COMBINES_EMAILS);
        }
    }

    /**
     * This method returns whether or not this trigger applies to the given folder name.
     *
     * @param   sFolderName  The name of the folder.
     *
     * @return  true if this trigger applies to this folder.
     *
     * @see     com.cordys.coe.ac.emailio.config.trigger.ITrigger#appliesTo(java.lang.String)
     */
    public boolean appliesTo(String sFolderName)
    {
        boolean bReturn = false;

        String[] asTemp = getFolders();

        if ((asTemp == null) || (asTemp.length == 0))
        {
            // If no appliesTo is defined it applies to all.
            bReturn = true;
        }
        else
        {
            for (int iCount = 0; iCount < asTemp.length; iCount++)
            {
                if (sFolderName.equals(asTemp[iCount]))
                {
                    bReturn = true;
                    break;
                }
            }
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Trigger " + getName() + " applies to folder " + sFolderName + ": " +
                      bReturn);
        }

        return bReturn;
    }

    /**
     * This method returns all actions for the given event type.
     *
     * <p>A new ArrayList is returned to make sure no threading problems occur. If no actions are
     * present, an empty list will be returned, not a null value.</p>
     *
     * @param   etEvent  The event type.
     *
     * @return  All actions for the event type.
     *
     * @see     com.cordys.coe.ac.emailio.config.trigger.ITrigger#getActions(com.cordys.coe.ac.emailio.config.action.EEventType)
     */
    public ArrayList<IAction> getActions(EEventType etEvent)
    {
        ArrayList<IAction> actions = m_hmEvents.get(etEvent);

        if ((actions == null) || (actions.size() == 0))
        {
            return new ArrayList<IAction>();
        }
        else
        {
            return new ArrayList<IAction>(actions);
        }
    }

    /**
     * This method gets whether or not this trigger should combine emails into a single request.
     *
     * @return  Whether or not this trigger should combine emails into a single request.
     *
     * @see     com.cordys.coe.ac.emailio.config.trigger.ITrigger#getCombineEmails()
     */
    public boolean getCombineEmails()
    {
        return m_bCombineEmails;
    }

    /**
     * This method gets the description of the trigger.
     *
     * @return  The description of the trigger.
     *
     * @see     com.cordys.coe.ac.emailio.config.trigger.ITrigger#getDescription()
     */
    public String getDescription()
    {
        return m_sDescription;
    }

    /**
     * This method gets the name of the folders to which this trigger should apply.
     *
     * @return  The name of the folders to which this trigger should apply. If the length of the
     *          resulting array is 0 this trigger should be applied to all folders.
     *
     * @see     com.cordys.coe.ac.emailio.config.trigger.ITrigger#getFolders()
     */
    public String[] getFolders()
    {
        return m_asFolders;
    }

    /**
     * This method gets the message definition of the soap message that will be sent.
     *
     * @return  The message definition of the soap message that will be sent.
     *
     * @see     com.cordys.coe.ac.emailio.config.trigger.ITrigger#getMessage()
     */
    public IMessage getMessage()
    {
        return m_mMessage;
    }

    /**
     * This method gets the name of the trigger.
     *
     * @return  The name of the trigger.
     *
     * @see     com.cordys.coe.ac.emailio.config.trigger.ITrigger#getName()
     */
    public String getName()
    {
        return m_sName;
    }

    /**
     * This method gets the priority for this rule.
     *
     * @return  The priority for this rule.
     *
     * @see     com.cordys.coe.ac.emailio.config.trigger.ITrigger#getPriority()
     */
    @Override public int getPriority()
    {
        return m_iPriority;
    }

    /**
     * This method gets the rules for this trigger.
     *
     * @return  The rules for this trigger.
     *
     * @see     com.cordys.coe.ac.emailio.config.trigger.ITrigger#getRules()
     */
    public IRule[] getRules()
    {
        return m_alRules.toArray(new IRule[0]);
    }

    /**
     * This method gets whether or not this trigger is a one time only trigger. This means that as
     * soon as an email matches this trigger, the trigger should be removed from the current list of
     * triggers for the email box.
     *
     * @return  Whether or not this trigger is a one time only trigger.
     *
     * @see     com.cordys.coe.ac.emailio.config.trigger.ITrigger#isOneTimeOnly()
     */
    public boolean isOneTimeOnly()
    {
        return m_bOneTimeOnly;
    }

    /**
     * This method sets wether or not this trigger should combine emails into a single request.
     *
     * @param  bCombineEmails  Whether or not this trigger should combine emails into a single
     *                         request.
     */
    public void setCombineEmails(boolean bCombineEmails)
    {
        m_bCombineEmails = bCombineEmails;
    }

    /**
     * This method sets the description of the trigger.
     *
     * @param  sDescription  The description of the trigger.
     */
    public void setDescription(String sDescription)
    {
        m_sDescription = sDescription;
    }

    /**
     * This method sets the message definition of the soap message that will be sent.
     *
     * @param  mMessage  Tthe message definition of the soap message that will be sent.
     */
    public void setMessage(IMessage mMessage)
    {
        m_mMessage = mMessage;
    }

    /**
     * This method sets the name of the trigger.
     *
     * @param  sName  The name of the trigger.
     */
    public void setName(String sName)
    {
        m_sName = sName;
    }

    /**
     * This method sets wether or not this trigger is a one time only trigger. This means that as
     * soon as an email matches this trigger, the trigger should be removed from the current list of
     * triggers for the email box.
     *
     * @param  bOneTimeOnly  Whether or not this trigger is a one time only trigger.
     */
    public void setOneTimeOnly(boolean bOneTimeOnly)
    {
        m_bOneTimeOnly = bOneTimeOnly;
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

        sbReturn.append("Trigger " + getName());

        if ((m_sDescription != null) && (m_sDescription.length() > 0))
        {
            sbReturn.append("\nDescription: " + getDescription());
        }

        sbReturn.append("\nCombine emails: " + getCombineEmails());
        sbReturn.append("\nApplies to: ");

        if (m_asFolders != null)
        {
            for (int iCount = 0; iCount < m_asFolders.length; iCount++)
            {
                sbReturn.append(m_asFolders[iCount]);

                if (iCount < (m_asFolders.length - 1))
                {
                    sbReturn.append(", ");
                }
            }
        }
        else
        {
            sbReturn.append(" All folders");
        }

        if (isOneTimeOnly())
        {
            sbReturn.append("\nOne-Time only trigger!");
        }

        sbReturn.append("\nRules:");
        sbReturn.append("\n------\n");

        for (IRule rRule : m_alRules)
        {
            sbReturn.append(rRule.toString());
            sbReturn.append("\n");
        }

        sbReturn.append("\nMessage:");
        sbReturn.append("\n--------\n");

        sbReturn.append(m_mMessage.toString());

        return sbReturn.toString();
    }

    /**
     * This method dumps the configuration of this trigger to XML.
     *
     * @param   iParent  The parent element.
     *
     * @return  The created XML structure.
     *
     * @see     com.cordys.coe.ac.emailio.config.trigger.ITrigger#toXML(int)
     */
    public int toXML(int iParent)
    {
        int iReturn = 0;

        iReturn = Node.createElementNS(TAG_TRIGGER, EMPTY_STRING, EMPTY_STRING,
                                       EmailIOConnectorConstants.NS_CONFIGURATION, iParent);

        Node.createElementWithParentNS(TAG_NAME, getName(), iReturn);

        if ((m_sDescription != null) && (m_sDescription.length() > 0))
        {
            Node.createElementWithParentNS(TAG_DESCRIPTION, getDescription(), iReturn);
        }

        Node.createElementWithParentNS(TAG_COMBINE_EMAILS, String.valueOf(getCombineEmails()),
                                       iReturn);

        Node.createElementWithParentNS(TAG_PRIORITY, String.valueOf(getPriority()), iReturn);

        if ((m_asFolders != null) && (m_asFolders.length > 0))
        {
            StringBuffer sbTemp = new StringBuffer(256);

            for (int iCount = 0; iCount < m_asFolders.length; iCount++)
            {
                sbTemp.append(m_asFolders[iCount]);

                if (iCount < (m_asFolders.length - 1))
                {
                    sbTemp.append(", ");
                }
            }
            Node.setAttribute(iReturn, ATTR_APPLIES_TO, sbTemp.toString());
        }

        if (isOneTimeOnly())
        {
            Node.setAttribute(iReturn, ATTR_ONETIMEONLY, "true");
        }
        else
        {
            Node.setAttribute(iReturn, ATTR_ONETIMEONLY, "false");
        }

        if (m_alRules.size() > 0)
        {
            int iRules = Node.createElementWithParentNS("rules", EMPTY_STRING, iReturn);

            for (IRule rRule : m_alRules)
            {
                rRule.toXML(iRules);
            }
        }

        if (m_mMessage != null)
        {
            m_mMessage.toXML(iReturn);
        }

        if ((m_hmEvents != null) && (m_hmEvents.size() > 0))
        {
            int iEvents = Node.createElementWithParentNS("events", EMPTY_STRING, iReturn);

            for (EEventType etEventType : m_hmEvents.keySet())
            {
                ArrayList<IAction> alActions = m_hmEvents.get(etEventType);

                if (alActions.size() > 0)
                {
                    int iEvent = Node.createElementWithParentNS("event", EMPTY_STRING, iEvents);
                    Node.setAttribute(iEvent, ATTR_EVENT_TYPE, etEventType.name());

                    for (IAction aAction : alActions)
                    {
                        aAction.toXML(iEvent);
                    }
                }
            }
        }

        return iReturn;
    }

    /**
     * This method is called to validate the configuration. This method is used to i.e. check if the
     * email server is reachable or that the messages configured are actually sendable.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     *
     * @see     com.cordys.coe.ac.emailio.config.trigger.ITrigger#validate()
     */
    @Override public void validate()
                            throws EmailIOConfigurationException
    {
        for (ArrayList<IAction> alActions : m_hmEvents.values())
        {
            if (alActions != null)
            {
                for (IAction aAction : alActions)
                {
                    if (aAction != null)
                    {
                        aAction.validate();
                    }
                }
            }
        }

        if (m_mMessage != null)
        {
            m_mMessage.validate();
        }

        for (IRule rRule : m_alRules)
        {
            rRule.validate();
        }
    }
}
