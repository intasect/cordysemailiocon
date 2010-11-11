package com.cordys.coe.ac.emailio.config.pattern;

import com.cordys.coe.ac.emailio.config.rule.IRule;
import com.cordys.coe.ac.emailio.config.rule.IRuleContext;
import com.cordys.coe.ac.emailio.config.token.EStoreSource;
import com.cordys.coe.ac.emailio.config.token.IStorageToken;
import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;
import com.cordys.coe.ac.emailio.exception.TriggerEngineException;
import com.cordys.coe.ac.emailio.localization.TriggerEngineExceptionMessages;

import com.eibus.util.logger.CordysLogger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class implements a regex based pattern.
 *
 * @author  pgussow
 */
class RegExPattern extends BasePattern
{
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(RegExPattern.class);

    /**
     * Creates a new RegExPattern object.
     *
     * @param   iNode  The configuration node.
     *
     * @throws  EmailIOConfigurationException  In case the pattern is invalid.
     */
    public RegExPattern(int iNode)
                 throws EmailIOConfigurationException
    {
        super(iNode);
    }

    /**
     * This method evaluates an RegEx expression. It assumes that the main content is a String.
     *
     * @param   pcContext  The context for the pattern.
     * @param   oValue     The value to operate on.
     * @param   rRule      The paretn rule.
     *
     * @return  true if the passed on value matches the pattern. Otherwise false.
     *
     * @throws  TriggerEngineException  In case there was an error executing the trigger.
     *
     * @see     com.cordys.coe.ac.emailio.config.pattern.IPattern#evaluate(IRuleContext, Object,
     *          IRule)
     */
    @Override public boolean evaluate(IRuleContext pcContext, Object oValue, IRule rRule)
                               throws TriggerEngineException
    {
        boolean bReturn = false;

        if (oValue == null)
        {
            throw new TriggerEngineException(TriggerEngineExceptionMessages.TEE_THE_VALUE_CAN_NOT_BE_NULL);
        }

        if (oValue instanceof String)
        {
            String sString = (String) oValue;

            String sRealRegEx = replaceValue(pcContext);

            Matcher mMatcher = Pattern.compile(sRealRegEx, Pattern.DOTALL | Pattern.MULTILINE)
                                      .matcher(sString);

            if (mMatcher.find())
            {
                // The pattern was found, so we'll return true
                bReturn = true;

                // Now do the storage.
                IStorageToken[] astTokens = getStorageTokens();

                for (int iCount = 0; iCount < astTokens.length; iCount++)
                {
                    IStorageToken stToken = astTokens[iCount];
                    String sName = stToken.getName();
                    String sValue = stToken.getValue();

                    if (stToken.getSource() != EStoreSource.FIXED)
                    {
                        // In case of a regex the value is the number of the group.
                        int iGroup = 0;

                        try
                        {
                            iGroup = Integer.parseInt(sValue);
                        }
                        catch (NumberFormatException nfe)
                        {
                            throw new TriggerEngineException(nfe,
                                                             TriggerEngineExceptionMessages.TEE_THE_VALUE_OF_A_STORE_TOKEN_FOR_A_REGEX_MUST_BE_AN_INTEGER,
                                                             sValue);
                        }
                        sValue = mMatcher.group(iGroup);
                    }

                    // Store the value in the context.
                    pcContext.putValue(sName, sValue);
                }
            }
        }
        else
        {
            throw new TriggerEngineException(TriggerEngineExceptionMessages.TEE_THE_TYPE_GIVEN_IS_NOT_SUPPORTED,
                                             oValue.getClass().getName());
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Pattern match: " + bReturn + "\nPattern details: " + this.toString());
        }

        if ((bReturn == false) && isOptional())
        {
            bReturn = true;
        }

        return bReturn;
    }

    /**
     * This method returns the type of pattern.
     *
     * @return  The pattern type.
     *
     * @see     com.cordys.coe.ac.emailio.config.pattern.BasePattern#getType()
     */
    @Override protected EPatternType getType()
    {
        return EPatternType.REGEX;
    }
}
