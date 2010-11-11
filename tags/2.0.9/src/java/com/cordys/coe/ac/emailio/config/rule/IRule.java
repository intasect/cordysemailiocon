

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
package com.cordys.coe.ac.emailio.config.rule;

import com.cordys.coe.ac.emailio.config.EEmailSection;
import com.cordys.coe.ac.emailio.config.IXMLSerializable;
import com.cordys.coe.ac.emailio.config.pattern.IPattern;
import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;

import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * The interface describing a rule.
 *
 * @author  pgussow
 */
public interface IRule extends IXMLSerializable
{
    /**
     * This method gets the patterns for this rule.
     *
     * @return  The patterns for this rule.
     */
    IPattern[] getPatterns();

    /**
     * This method gets the section where to apply this rule.
     *
     * @return  The section where to apply this rule.
     */
    EEmailSection getSection();

    /**
     * This method gets the namespace binding for this rule.
     *
     * @return  The namespace binding for this rule.
     */
    XPathMetaInfo getXPathMetaInfo();

    /**
     * This method is called to validate the configuration. This method is used to i.e. check if the
     * email server is reachable or that the messages configured are actually sendable.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    void validate()
           throws EmailIOConfigurationException;
}
