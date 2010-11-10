/**
 * (c) 2007 Cordys R&D B.V. All rights reserved.
 * The computer program(s) is the proprietary information of Cordys R&D B.V.
 * and provided under the relevant License Agreement containing restrictions
 * on use and disclosure. Use is subject to the License Agreement.
 */
package com.cordys.coe.ac.emailio.exception;

import com.eibus.localization.IStringResource;

/**
 * This class wraps exceptions in the outbound part.
 *
 * @author  pgussow
 */
public class OutboundEmailException extends EmailIOException
{
    /**
     * Creates a new OutboundEmailException object.
     *
     * @param  srMessage     The localizable message.
     * @param  aoParameters  The list of parameters for the localizable message.
     */
    public OutboundEmailException(IStringResource srMessage, Object... aoParameters)
    {
        super(srMessage, aoParameters);
    }

    /**
     * Creates a new OutboundEmailException object.
     *
     * @param  sFaultActor   The actor for the current fault.
     * @param  srMessage     The localizable message.
     * @param  aoParameters  The list of parameters for the localizable message.
     */
    public OutboundEmailException(String sFaultActor, IStringResource srMessage,
                                  Object... aoParameters)
    {
        super(sFaultActor, srMessage, aoParameters);
    }

    /**
     * Creates a new OutboundEmailException object.
     *
     * @param  tCause        The exception that caused this exception.
     * @param  srMessage     The localizable message.
     * @param  aoParameters  The list of parameters for the localizable message.
     */
    public OutboundEmailException(Throwable tCause, IStringResource srMessage,
                                  Object... aoParameters)
    {
        super(tCause, srMessage, aoParameters);
    }

    /**
     * Creates a new OutboundEmailException object.
     *
     * @param  tCause        The exception that caused this exception.
     * @param  sFaultActor   The actor for the current fault.
     * @param  srMessage     The localizable message.
     * @param  aoParameters  The list of parameters for the localizable message.
     */
    public OutboundEmailException(Throwable tCause, String sFaultActor, IStringResource srMessage,
                                  Object... aoParameters)
    {
        super(tCause, sFaultActor, srMessage, aoParameters);
    }

    /**
     * Creates a new OutboundEmailException object.
     *
     * @param  tCause             The exception that caused this exception.
     * @param  plPreferredLocale  The preferred locale for this exception. It defaults to the SOAP
     *                            locale.
     * @param  sFaultActor        The actor for the current fault.
     * @param  srMessage          The localizable message.
     * @param  aoParameters       The list of parameters for the localizable message.
     */
    public OutboundEmailException(Throwable tCause, PreferredLocale plPreferredLocale,
                                  String sFaultActor, IStringResource srMessage,
                                  Object... aoParameters)
    {
        super(tCause, plPreferredLocale, sFaultActor, srMessage, aoParameters);
    }
}
