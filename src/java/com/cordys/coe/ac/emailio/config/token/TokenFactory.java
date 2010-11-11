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
 package com.cordys.coe.ac.emailio.config.token;

import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;

/**
 * This factory can be used to parse the token definitions.
 *
 * @author  pgussow
 */
public class TokenFactory
{
    /**
     * This method creates a replacement token.
     *
     * @param   iNode  The configuration of the token.
     *
     * @return  The replacement token.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    public static IReplacementToken createReplacementToken(int iNode)
                                                    throws EmailIOConfigurationException
    {
        return new ReplacementToken(iNode);
    }

    /**
     * This method creates a storage token.
     *
     * @param   iNode  The configuration of the token.
     *
     * @return  The storage token.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    public static IStorageToken createStorageToken(int iNode)
                                            throws EmailIOConfigurationException
    {
        return new StoreToken(iNode);
    }
}
