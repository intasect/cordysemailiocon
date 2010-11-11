package com.cordys.coe.ac.emailio.exception;

import com.eibus.localization.IStringResource;

/**
 * This class is used for exceptions that are related to the storage provider.
 *
 * @author  pgussow
 */
public class StorageProviderException extends EmailIOException
{
    /**
     * Creates a new StorageProviderException object.
     *
     * @param  srMessage     The localizable message.
     * @param  aoParameters  The list of parameters for the localizable message.
     */
    public StorageProviderException(IStringResource srMessage, Object... aoParameters)
    {
        super(srMessage, aoParameters);
    }

    /**
     * Creates a new StorageProviderException object.
     *
     * @param  sFaultActor   The actor for the current fault.
     * @param  srMessage     The localizable message.
     * @param  aoParameters  The list of parameters for the localizable message.
     */
    public StorageProviderException(String sFaultActor, IStringResource srMessage,
                                    Object... aoParameters)
    {
        super(sFaultActor, srMessage, aoParameters);
    }

    /**
     * Creates a new StorageProviderException object.
     *
     * @param  tCause        The exception that caused this exception.
     * @param  srMessage     The localizable message.
     * @param  aoParameters  The list of parameters for the localizable message.
     */
    public StorageProviderException(Throwable tCause, IStringResource srMessage,
                                    Object... aoParameters)
    {
        super(tCause, srMessage, aoParameters);
    }

    /**
     * Creates a new StorageProviderException object.
     *
     * @param  tCause        The exception that caused this exception.
     * @param  sFaultActor   The actor for the current fault.
     * @param  srMessage     The localizable message.
     * @param  aoParameters  The list of parameters for the localizable message.
     */
    public StorageProviderException(Throwable tCause, String sFaultActor, IStringResource srMessage,
                                    Object... aoParameters)
    {
        super(tCause, sFaultActor, srMessage, aoParameters);
    }

    /**
     * Creates a new StorageProviderException object.
     *
     * @param  tCause             The exception that caused this exception.
     * @param  plPreferredLocale  The preferred locale for this exception. It defaults to the SOAP
     *                            locale.
     * @param  sFaultActor        The actor for the current fault.
     * @param  srMessage          The localizable message.
     * @param  aoParameters       The list of parameters for the localizable message.
     */
    public StorageProviderException(Throwable tCause, PreferredLocale plPreferredLocale,
                                    String sFaultActor, IStringResource srMessage,
                                    Object... aoParameters)
    {
        super(tCause, plPreferredLocale, sFaultActor, srMessage, aoParameters);
    }
}
