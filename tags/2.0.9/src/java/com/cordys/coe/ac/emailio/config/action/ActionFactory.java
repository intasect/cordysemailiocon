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
