package com.cordys.coe.ac.emailio.exception;

import com.eibus.localization.IStringResource;

/**
 * Exception class for the trigger engine.
 *
 * @author  pgussow
 */
public class TriggerEngineException extends EmailIOException
{
    /**
     * Creates a new TriggerEngineException object.
     *
     * @param  srMessage     The localizable message.
     * @param  aoParameters  The list of parameters for the localizable message.
     */
    public TriggerEngineException(IStringResource srMessage, Object... aoParameters)
    {
        super(srMessage, aoParameters);
    }

    /**
     * Creates a new TriggerEngineException object.
     *
     * @param  sFaultActor   The actor for the current fault.
     * @param  srMessage     The localizable message.
     * @param  aoParameters  The list of parameters for the localizable message.
     */
    public TriggerEngineException(String sFaultActor, IStringResource srMessage,
                                  Object... aoParameters)
    {
        super(sFaultActor, srMessage, aoParameters);
    }

    /**
     * Creates a new TriggerEngineException object.
     *
     * @param  tCause        The exception that caused this exception.
     * @param  srMessage     The localizable message.
     * @param  aoParameters  The list of parameters for the localizable message.
     */
    public TriggerEngineException(Throwable tCause, IStringResource srMessage,
                                  Object... aoParameters)
    {
        super(tCause, srMessage, aoParameters);
    }

    /**
     * Creates a new TriggerEngineException object.
     *
     * @param  tCause        The exception that caused this exception.
     * @param  sFaultActor   The actor for the current fault.
     * @param  srMessage     The localizable message.
     * @param  aoParameters  The list of parameters for the localizable message.
     */
    public TriggerEngineException(Throwable tCause, String sFaultActor, IStringResource srMessage,
                                  Object... aoParameters)
    {
        super(tCause, sFaultActor, srMessage, aoParameters);
    }

    /**
     * Creates a new TriggerEngineException object.
     *
     * @param  tCause             The exception that caused this exception.
     * @param  plPreferredLocale  The preferred locale for this exception. It defaults to the SOAP
     *                            locale.
     * @param  sFaultActor        The actor for the current fault.
     * @param  srMessage          The localizable message.
     * @param  aoParameters       The list of parameters for the localizable message.
     */
    public TriggerEngineException(Throwable tCause, PreferredLocale plPreferredLocale,
                                  String sFaultActor, IStringResource srMessage,
                                  Object... aoParameters)
    {
        super(tCause, plPreferredLocale, sFaultActor, srMessage, aoParameters);
    }
}
