package com.cordys.coe.ac.emailio.exception;

import com.cordys.coe.exception.ServerLocalizableException;

import com.eibus.localization.IStringResource;

/**
 * This class is the mail exception class for the Email IO connector.
 *
 * @author  pgussow
 */
public class EmailIOException extends ServerLocalizableException
{
    /**
     * Creates a new EmailIOException object.
     *
     * @param  srMessage     The localizable message.
     * @param  aoParameters  The list of parameters for the localizable message.
     */
    public EmailIOException(IStringResource srMessage, Object... aoParameters)
    {
        super(srMessage, aoParameters);
    }

    /**
     * Creates a new EmailIOException object.
     *
     * @param  sFaultActor   The actor for the current fault.
     * @param  srMessage     The localizable message.
     * @param  aoParameters  The list of parameters for the localizable message.
     */
    public EmailIOException(String sFaultActor, IStringResource srMessage, Object... aoParameters)
    {
        super(sFaultActor, srMessage, aoParameters);
    }

    /**
     * Creates a new EmailIOException object.
     *
     * @param  tCause        The exception that caused this exception.
     * @param  srMessage     The localizable message.
     * @param  aoParameters  The list of parameters for the localizable message.
     */
    public EmailIOException(Throwable tCause, IStringResource srMessage, Object... aoParameters)
    {
        super(tCause, srMessage, aoParameters);
    }

    /**
     * Creates a new EmailIOException object.
     *
     * @param  tCause        The exception that caused this exception.
     * @param  sFaultActor   The actor for the current fault.
     * @param  srMessage     The localizable message.
     * @param  aoParameters  The list of parameters for the localizable message.
     */
    public EmailIOException(Throwable tCause, String sFaultActor, IStringResource srMessage,
                            Object... aoParameters)
    {
        super(tCause, sFaultActor, srMessage, aoParameters);
    }

    /**
     * Creates a new EmailIOException object.
     *
     * @param  tCause             The exception that caused this exception.
     * @param  plPreferredLocale  The preferred locale for this exception. It defaults to the SOAP
     *                            locale.
     * @param  sFaultActor        The actor for the current fault.
     * @param  srMessage          The localizable message.
     * @param  aoParameters       The list of parameters for the localizable message.
     */
    public EmailIOException(Throwable tCause, PreferredLocale plPreferredLocale, String sFaultActor,
                            IStringResource srMessage, Object... aoParameters)
    {
        super(tCause, plPreferredLocale, sFaultActor, srMessage, aoParameters);
    }
}
