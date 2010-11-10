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
