package com.cordys.coe.ac.emailio.exception;

import com.eibus.localization.IStringResource;

/**
 * Exceptions for connection related material.
 *
 * @author  pgussow
 */
public class EmailConnectionException extends EmailIOException
{
    /**
     * Creates a new EmailConnectionException object.
     *
     * @param  srMessage     The localizable message.
     * @param  aoParameters  The list of parameters for the localizable message.
     */
    public EmailConnectionException(IStringResource srMessage, Object... aoParameters)
    {
        super(srMessage, aoParameters);
    }

    /**
     * Creates a new EmailConnectionException object.
     *
     * @param  sFaultActor   The actor for the current fault.
     * @param  srMessage     The localizable message.
     * @param  aoParameters  The list of parameters for the localizable message.
     */
    public EmailConnectionException(String sFaultActor, IStringResource srMessage,
                                    Object... aoParameters)
    {
        super(sFaultActor, srMessage, aoParameters);
    }

    /**
     * Creates a new EmailConnectionException object.
     *
     * @param  tCause        The exception that caused this exception.
     * @param  srMessage     The localizable message.
     * @param  aoParameters  The list of parameters for the localizable message.
     */
    public EmailConnectionException(Throwable tCause, IStringResource srMessage,
                                    Object... aoParameters)
    {
        super(tCause, srMessage, aoParameters);
    }

    /**
     * Creates a new EmailConnectionException object.
     *
     * @param  tCause        The exception that caused this exception.
     * @param  sFaultActor   The actor for the current fault.
     * @param  srMessage     The localizable message.
     * @param  aoParameters  The list of parameters for the localizable message.
     */
    public EmailConnectionException(Throwable tCause, String sFaultActor, IStringResource srMessage,
                                    Object... aoParameters)
    {
        super(tCause, sFaultActor, srMessage, aoParameters);
    }

    /**
     * Creates a new EmailConnectionException object.
     *
     * @param  tCause             The exception that caused this exception.
     * @param  plPreferredLocale  The preferred locale for this exception. It defaults to the SOAP
     *                            locale.
     * @param  sFaultActor        The actor for the current fault.
     * @param  srMessage          The localizable message.
     * @param  aoParameters       The list of parameters for the localizable message.
     */
    public EmailConnectionException(Throwable tCause, PreferredLocale plPreferredLocale,
                                    String sFaultActor, IStringResource srMessage,
                                    Object... aoParameters)
    {
        super(tCause, plPreferredLocale, sFaultActor, srMessage, aoParameters);
    }
}
