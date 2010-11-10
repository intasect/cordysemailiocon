package com.cordys.coe.ac.emailio.exception;

import com.eibus.localization.IStringResource;

/**
 * This class wraps configuration exceptions.
 *
 * @author  pgussow
 */
public class EmailIOConfigurationException extends EmailIOException
{
    /**
     * Creates a new InboundEmailConfigurationException object.
     *
     * @param  srMessage     The localizable message.
     * @param  aoParameters  The list of parameters for the localizable message.
     */
    public EmailIOConfigurationException(IStringResource srMessage, Object... aoParameters)
    {
        super(srMessage, aoParameters);
    }

    /**
     * Creates a new InboundEmailConfigurationException object.
     *
     * @param  sFaultActor   The actor for the current fault.
     * @param  srMessage     The localizable message.
     * @param  aoParameters  The list of parameters for the localizable message.
     */
    public EmailIOConfigurationException(String sFaultActor, IStringResource srMessage,
                                         Object... aoParameters)
    {
        super(sFaultActor, srMessage, aoParameters);
    }

    /**
     * Creates a new InboundEmailConfigurationException object.
     *
     * @param  tCause        The exception that caused this exception.
     * @param  srMessage     The localizable message.
     * @param  aoParameters  The list of parameters for the localizable message.
     */
    public EmailIOConfigurationException(Throwable tCause, IStringResource srMessage,
                                         Object... aoParameters)
    {
        super(tCause, srMessage, aoParameters);
    }

    /**
     * Creates a new InboundEmailConfigurationException object.
     *
     * @param  tCause        The exception that caused this exception.
     * @param  sFaultActor   The actor for the current fault.
     * @param  srMessage     The localizable message.
     * @param  aoParameters  The list of parameters for the localizable message.
     */
    public EmailIOConfigurationException(Throwable tCause, String sFaultActor,
                                         IStringResource srMessage, Object... aoParameters)
    {
        super(tCause, sFaultActor, srMessage, aoParameters);
    }

    /**
     * Creates a new InboundEmailConfigurationException object.
     *
     * @param  tCause             The exception that caused this exception.
     * @param  plPreferredLocale  The preferred locale for this exception. It defaults to the SOAP
     *                            locale.
     * @param  sFaultActor        The actor for the current fault.
     * @param  srMessage          The localizable message.
     * @param  aoParameters       The list of parameters for the localizable message.
     */
    public EmailIOConfigurationException(Throwable tCause, PreferredLocale plPreferredLocale,
                                         String sFaultActor, IStringResource srMessage,
                                         Object... aoParameters)
    {
        super(tCause, plPreferredLocale, sFaultActor, srMessage, aoParameters);
    }
}
