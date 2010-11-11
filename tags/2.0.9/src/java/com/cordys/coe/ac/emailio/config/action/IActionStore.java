package com.cordys.coe.ac.emailio.config.action;

/**
 * This interface describes the IActionStore that can be used to cache action definitions.
 *
 * @author  pgussow
 */
public interface IActionStore
{
    /**
     * This method gets the action with the given ID.
     *
     * @param   sActionID  The ID of the action.
     *
     * @return  The action with the given ID.
     */
    IAction getAction(String sActionID);
}
