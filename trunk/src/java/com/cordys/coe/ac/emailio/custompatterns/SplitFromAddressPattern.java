

 /**
 * Copyright 2007 Cordys R&D B.V. 
 * 
 * This file is part of the Cordys Email IO Connector. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cordys.coe.ac.emailio.custompatterns;

import com.cordys.coe.ac.emailio.config.pattern.BasePattern;
import com.cordys.coe.ac.emailio.config.pattern.EPatternType;
import com.cordys.coe.ac.emailio.config.pattern.IPattern;
import com.cordys.coe.ac.emailio.config.rule.IRule;
import com.cordys.coe.ac.emailio.config.rule.IRuleContext;
import com.cordys.coe.ac.emailio.config.token.IStorageToken;
import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;
import com.cordys.coe.ac.emailio.exception.TriggerEngineException;
import com.cordys.coe.ac.emailio.localization.TriggerEngineExceptionMessages;

import com.eibus.util.logger.CordysLogger;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;

import javax.mail.internet.InternetAddress;

/**
 * This pattern will split the from address into a display name and email address. You must specify
 * 2 storage tokens. First one must be the email address name. The second must be the display name
 * one.
 *
 * @author  pgussow
 */
public class SplitFromAddressPattern extends BasePattern
    implements IPattern
{
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(SplitFromAddressPattern.class);

    /**
     * Creates a new BasePattern object.
     *
     * @param   iNode  The configuration XML.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    public SplitFromAddressPattern(int iNode)
                            throws EmailIOConfigurationException
    {
        super(iNode);
    }

    /**
     * This method evaluates the pattern on the given data object. The data object is usually a
     * String, but can also be a JavaMail header or an XML node.
     *
     * @param   pcContext  The context information.
     * @param   oValue     The value to evaluate against.
     * @param   rRule      The parent rule.
     *
     * @return  true is the value matches the pattern. Otherwise false.
     *
     * @throws  TriggerEngineException  In case of any exceptions.
     *
     * @see     com.cordys.coe.ac.emailio.config.pattern.IPattern#evaluate(IRuleContext, Object,
     *          IRule)
     */
    public boolean evaluate(IRuleContext pcContext, Object oValue, IRule rRule)
                     throws TriggerEngineException
    {
        // Get the actual email message.
        Message mMessage = pcContext.getMessage();

        IStorageToken[] ast = getStorageTokens();

        if (ast.length != 2)
        {
            throw new TriggerEngineException(TriggerEngineExceptionMessages.TEE_SFA_INCORRECT_NUMBER_OF_STORAGE_TOKENS,
                                             2, ast.length);
        }

        try
        {
            Address[] aa = mMessage.getFrom();

            if (aa.length > 0)
            {
                Address aFrom = aa[0];

                if (aFrom instanceof InternetAddress)
                {
                    InternetAddress iaFrom = (InternetAddress) aFrom;
                    String sEmailAddress = iaFrom.getAddress();
                    String sDisplayName = iaFrom.getPersonal();

                    if ((sDisplayName == null) || (sDisplayName.length() == 0) ||
                            (sDisplayName.trim().length() == 0))
                    {
                        sDisplayName = sEmailAddress;
                    }

                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Display name: " + sDisplayName + ", Email address: " +
                                  sEmailAddress);
                    }

                    // Put the values in the context.
                    pcContext.putValue(ast[0].getName(), sEmailAddress);
                    pcContext.putValue(ast[1].getName(), sDisplayName);
                }
                else
                {
                    throw new TriggerEngineException(TriggerEngineExceptionMessages.TEE_SFA_ERROR_THE_FROM_ADDRESS_MUST_BE_AN_INTERNET_ADDRESS_THIS_ADDRESS_IS_OF_CLASS_0,
                                                     aFrom.getClass().getName());
                }
            }
            else
            {
                throw new TriggerEngineException(TriggerEngineExceptionMessages.TEE_SFA_ERROR_THERE_IS_NO_FROM_ADDRESS_AVAILABLE);
            }
        }
        catch (MessagingException e)
        {
            throw new TriggerEngineException(e,
                                             TriggerEngineExceptionMessages.TEE_SFA_ERROR_GETTING_THE_FROM_ADDRESS);
        }

        // We'll always return true, since it can be applied to every method.
        return true;
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
        return EPatternType.CUSTOM;
    }
}
