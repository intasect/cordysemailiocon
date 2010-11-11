

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
import com.cordys.coe.ac.emailio.config.rule.IRule;
import com.cordys.coe.ac.emailio.config.rule.IRuleContext;
import com.cordys.coe.ac.emailio.config.token.IStorageToken;
import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;
import com.cordys.coe.ac.emailio.exception.TriggerEngineException;
import com.cordys.coe.ac.emailio.localization.TriggerEngineExceptionMessages;

import java.io.ByteArrayOutputStream;

import javax.mail.Message;

/**
 * This pattern will get the raw content of the mail and store it as the original string in the
 * context.
 *
 * @author  pgussow
 */
public class StoreRawMailContent extends BasePattern
{
    /**
     * Creates a new BasePattern object.
     *
     * @param   iNode  The configuration XML.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    public StoreRawMailContent(int iNode)
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

        if (ast.length != 1)
        {
            throw new TriggerEngineException(TriggerEngineExceptionMessages.TEE_SRMC_A_STORAGE_TOKEN_MUST_BE_SUPPLIED);
        }

        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            mMessage.writeTo(baos);
            pcContext.putValue(ast[0].getName(), baos.toString());
        }
        catch (Exception e)
        {
            throw new TriggerEngineException(e,
                                             TriggerEngineExceptionMessages.TEE_SRMC_ERROR_STORING_THE_FULL_MAIL_IN_THE_RULE_CONTEXT);
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
