

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
package com.cordys.coe.ac.emailio.sample;

import com.cordys.coe.ac.emailio.config.pattern.BasePattern;
import com.cordys.coe.ac.emailio.config.pattern.EPatternType;
import com.cordys.coe.ac.emailio.config.pattern.IPattern;
import com.cordys.coe.ac.emailio.config.rule.IRule;
import com.cordys.coe.ac.emailio.config.rule.IRuleContext;
import com.cordys.coe.ac.emailio.config.token.IStorageToken;
import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;
import com.cordys.coe.ac.emailio.exception.TriggerEngineException;

import com.eibus.util.logger.CordysLogger;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Map;

/**
 * This class is used to demonstrate a custom pattern.
 *
 * @author  pgussow
 */
public class CustomPattern extends BasePattern
    implements IPattern
{
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(CustomPattern.class);

    /**
     * Creates a new BasePattern object.
     *
     * @param   iNode  The configuration XML.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    public CustomPattern(int iNode)
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
     * @see     IPattern#evaluate(IRuleContext, Object, IRule)
     */
    public boolean evaluate(IRuleContext pcContext, Object oValue, IRule rRule)
                     throws TriggerEngineException
    {
        // In case of debug logging well log the whole context.
        if (LOG.isDebugEnabled())
        {
            Map<String, Object> mTemp = pcContext.getAllValues();

            for (String sKey : mTemp.keySet())
            {
                Object oTemp = mTemp.get(sKey);

                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Key: " + sKey + ", value: " + oTemp);
                }
            }
        }

        // This handle will add the current date to the context.
        Date dTemp = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        String sFormatted = sdf.format(dTemp);

        // Now we'll add that date to the context using the defines storage tokens.
        IStorageToken[] ast = getStorageTokens();

        for (IStorageToken stToken : ast)
        {
            pcContext.putValue(stToken.getName(), sFormatted);
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
