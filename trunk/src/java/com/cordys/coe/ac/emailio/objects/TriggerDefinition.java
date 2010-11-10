package com.cordys.coe.ac.emailio.objects;

import com.cordys.coe.ac.emailio.localization.LogMessages;

import com.cordys.cpc.bsf.busobject.BusObjectConfig;
import com.cordys.cpc.bsf.busobject.exception.BsfApplicationRuntimeException;

/**
 * This class wraps the stored trigger definitions.
 *
 * @author  pgussow
 */
public class TriggerDefinition extends TriggerDefinitionBase
{
    /**
     * Creates a new TriggerDefinition object.
     */
    public TriggerDefinition()
    {
        this((BusObjectConfig) null);
    }

    /**
     * Creates a new TriggerDefinition object.
     *
     * @param  config  The configuration.
     */
    public TriggerDefinition(BusObjectConfig config)
    {
        super(config);
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
