/**
 * (c) 2007 Cordys R&D B.V. All rights reserved.
 * The computer program(s) is the proprietary information of Cordys R&D B.V.
 * and provided under the relevant License Agreement containing restrictions
 * on use and disclosure. Use is subject to the License Agreement.
 */
package com.cordys.coe.ac.emailio.exception;

import com.eibus.localization.IStringResource;

/**
 * This class wraps exceptions in the inbound part.
 *
 * @author  pgussow
 */
public class InboundEmailException extends EmailIOException
{
    /**
     * Creates a new InboundEmailException object.
     *
     * @param  srMessage     The localizable message.
     * @param  aoParameters  The list of parameters for the localizable message.
     */
    public InboundEmailException(IStringResource srMessage, Object... aoParameters)
    {
        super(srMessage, aoParameters);
    }

    /**
     * Creates a new InboundEmailException object.
     *
     * @param  sFaultActor   The actor for the current fault.
     * @param  srMessage     The localizable message.
     * @param  aoParameters  The list of parameters for the localizable message.
     */
    public InboundEmailException(String sFaultActor, IStringResource srMessage,
                                 Object... aoParameters)
    {
        super(sFaultActor, srMessage, aoParameters);
    }

    /**
     * Creates a new InboundEmailException object.
     *
     * @param  tCause        The exception that caused this exception.
     * @param  srMessage     The localizable message.
     * @param  aoParameters  The list of parameters for the localizable message.
     */
    public InboundEmailException(Throwable tCause, IStringResource srMessage,
                                 Object... aoParameters)
    {
        super(tCause, srMessage, aoParameters);
    }

    /**
     * Creates a new InboundEmailException object.
     *
     * @param  tCause        The exception that caused this exception.
     * @param  sFaultActor   The actor for the current fault.
     * @param  srMessage     The localizable message.
     * @param  aoParameters  The list of parameters for the localizable message.
     */
    public InboundEmailException(Throwable tCause, String sFaultActor, IStringResource srMessage,
                                 Object... aoParameters)
    {
        super(tCause, sFaultActor, srMessage, aoParameters);
    }

    /**
     * Creates a new InboundEmailException object.
     *
     * @param  tCause             The exception that caused this exception.
     * @param  plPreferredLocale  The preferred locale for this exception. It defaults to the SOAP
     *                            locale.
     * @param  sFaultActor        The actor for the current fault.
     * @param  srMessage          The localizable message.
     * @param  aoParameters       The list of parameters for the localizable message.
     */
    public InboundEmailException(Throwable tCause, PreferredLocale plPreferredLocale,
                                 String sFaultActor, IStringResource srMessage,
                                 Object... aoParameters)
    {
        super(tCause, plPreferredLocale, sFaultActor, srMessage, aoParameters);
    }
}
