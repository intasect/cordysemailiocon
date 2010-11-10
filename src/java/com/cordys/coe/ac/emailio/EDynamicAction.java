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
