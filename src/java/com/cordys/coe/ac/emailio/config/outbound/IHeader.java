/**
 * Copyright 2005 Cordys R&D B.V. 
 * 
 * This file is part of the Cordys SAP Connector. 
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
 /**
 * Copyright 2005 Cordys R&D B.V. 
 * 
 * This file is part of the Cordys SAP Connector. 
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
 package com.cordys.coe.ac.emailio.config.outbound;

import com.cordys.coe.ac.emailio.config.IXMLSerializable;

/**
 * This class wraps the details of a header.
 *
 * @author  pgussow
 */
public interface IHeader extends IXMLSerializable
{
    /**
     * Holds the name of the tag 'header'.
     */
    String TAG_HEADER = "header";
    /**
     * Holds the name of the tag 'name'.
     */
    String TAG_NAME = "name";
    /**
     * Holds the name of the tag 'value'.
     */
    String TAG_VALUE = "value";

    /**
     * This method gets the name of the header.
     *
     * @return  The name of the header.
     */
    String getName();

    /**
     * This method gets the value of the header.
     *
     * @return  The value of the header.
     */
    String getValue();
}
