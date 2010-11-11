

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
package com.cordys.coe.ac.emailio.config.pattern;

import com.cordys.coe.ac.emailio.config.IXMLSerializable;
import com.cordys.coe.ac.emailio.config.rule.IRule;
import com.cordys.coe.ac.emailio.config.rule.IRuleContext;
import com.cordys.coe.ac.emailio.config.token.IReplacementToken;
import com.cordys.coe.ac.emailio.config.token.IStorageToken;
import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;
import com.cordys.coe.ac.emailio.exception.TriggerEngineException;

/**
 * Interface describing the pattern.
 *
 * @author  pgussow
 */
public interface IPattern extends IXMLSerializable
{
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
     * @throws  TriggerEngineException  In case of any execeptions.
     */
    boolean evaluate(IRuleContext pcContext, Object oValue, IRule rRule)
              throws TriggerEngineException;

    /**
     * This method gets the replacement tokens.
     *
     * @return  The replacement tokens.
     */
    IReplacementToken[] getReplacementTokens();

    /**
     * This method gets the storage tokens.
     *
     * @return  The storage tokens.
     */
    IStorageToken[] getStorageTokens();

    /**
     * This method gets the value for this pattern.
     *
     * @return  The value for this pattern.
     */
    String getValue();

    /**
     * This method is called to validate the configuration. This method is used to i.e. check if the
     * email server is reachable or that the messages configured are actually sendable.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    void validate()
           throws EmailIOConfigurationException;

    /**
     * This method returns whether or not this pattern works on the headers of an email message.
     *
     * @return  true if this patterns needs the headers. Otherwise false.
     */
    boolean worksOnHeader();
}
