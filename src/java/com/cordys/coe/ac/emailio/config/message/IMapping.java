

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
package com.cordys.coe.ac.emailio.config.message;

import com.cordys.coe.ac.emailio.config.IXMLSerializable;
import com.cordys.coe.ac.emailio.config.rule.IRuleContext;
import com.cordys.coe.ac.emailio.exception.TriggerEngineException;

/**
 * This interface describes the mappings.
 *
 * @author  pgussow
 */
public interface IMapping extends IXMLSerializable
{
    /**
     * This method executes the mapping. It will execute the XPath on the given context node. When
     * it's found it will delete all the exeisting children of that node. Then based on the type of
     * the value in the context it will either create a text node (String value) or append an XML
     * structure (int).
     *
     * @param   rcContext     The rule context to get the values from.
     * @param   iContextNode  The context node.
     * @param   mMessage      The parent message.
     *
     * @throws  TriggerEngineException  In case of any exceptions.
     */
    void execute(IRuleContext rcContext, int iContextNode, IMessage mMessage)
          throws TriggerEngineException;

    /**
     * This method gets the class name for the custom mapping handler.
     *
     * @return  The class name for the custom mapping handler.
     */
    String getCustomClassName();

    /**
     * This method gets the lookup value for this mapping. It is the name of the variable in the
     * pattern context.
     *
     * @return  The lookup value for this mapping. It is the name of the variable in the pattern
     *          context.
     */
    String getLookupValue();

    /**
     * This method gets the mapping operation. When the source value is a non-xml value the default
     * operation is STRING_REPLACE. When the value is XML the default operation XML_APPEND_CHILD.
     *
     * @return  The mapping operation.
     */
    EMappingOperation getMappingOperation();

    /**
     * This method gets the source XPath for this mapping. Note: if the message has a repeatingxpath
     * set, then this XPath should be relative to that XPath.
     *
     * @return  The source XPath for this mapping.
     */
    String getSourceXPath();
}
