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
 package com.cordys.coe.ac.emailio.method;

import com.cordys.coe.ac.emailio.config.IEmailIOConfiguration;
import com.cordys.coe.ac.emailio.exception.EmailIOException;

import com.eibus.soap.BodyBlock;

/**
 * This class is the base class for handling specific methods.
 *
 * @author  pgussow
 */
public abstract class BaseMethod
{
    /**
     * Holds the request bodyblock.
     */
    private BodyBlock m_bbRequest;
    /**
     * Holds the response bodyblock.
     */
    private BodyBlock m_bbResponse;
    /**
     * Holds the configuration of the connector.
     */
    private IEmailIOConfiguration m_iecConfig;

    /**
     * Constructor.
     *
     * @param  bbRequest   The request bodyblock.
     * @param  bbResponse  The response bodyblock.
     * @param  iecConfig   The configuration of the connector.
     */
    public BaseMethod(BodyBlock bbRequest, BodyBlock bbResponse, IEmailIOConfiguration iecConfig)
    {
        m_bbRequest = bbRequest;
        m_bbResponse = bbResponse;
        m_iecConfig = iecConfig;
    }

    /**
     * This method executed the requested SOAP method.
     *
     * @throws  EmailIOException  In case of any processing errors.
     */
    public abstract void execute()
                          throws EmailIOException;

    /**
     * This method gets the configuration for the connector.
     *
     * @return  The configuration for the connector.
     */
    public IEmailIOConfiguration getConfiguration()
    {
        return m_iecConfig;
    }

    /**
     * This method gets the request bodyblock.
     *
     * @return  The request bodyblock.
     */
    public BodyBlock getRequest()
    {
        return m_bbRequest;
    }

    /**
     * This method gets the response bodyblock.
     *
     * @return  The response bodyblock.
     */
    public BodyBlock getResponse()
    {
        return m_bbResponse;
    }
}
