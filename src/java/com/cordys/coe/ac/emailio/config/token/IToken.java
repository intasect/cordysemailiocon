

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
package com.cordys.coe.ac.emailio.config.token;

import com.cordys.coe.ac.emailio.config.IXMLSerializable;

/**
 * General interface for replacement and storage tokens.
 *
 * @author  pgussow
 */
public interface IToken extends IXMLSerializable
{
    /**
     * This method gets the name for this token.
     *
     * @return  The name for this token.
     */
    String getName();

    /**
     * This method gets the value for this token.
     *
     * @return  The value for this token.
     */
    String getValue();
}
