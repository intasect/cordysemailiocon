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
