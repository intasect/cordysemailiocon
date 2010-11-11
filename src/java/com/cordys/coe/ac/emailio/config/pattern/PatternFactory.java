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
 package com.cordys.coe.ac.emailio.config.pattern;

import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;
import com.cordys.coe.ac.emailio.localization.EmailIOConfigurationExceptionMessages;

import com.eibus.xml.nom.Node;

/**
 * This factory can be used to create patterns.
 *
 * @author  pgussow
 */
public class PatternFactory
{
    /**
     * This method creates a pattern based on the pattern definition.
     *
     * @param   iNode  The configuration XML.
     *
     * @return  The created pattern.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    public static IPattern createPattern(int iNode)
                                  throws EmailIOConfigurationException
    {
        IPattern pReturn = null;

        String sType = Node.getAttribute(iNode, "type", "").toUpperCase();
        EPatternType ptType = null;

        try
        {
            ptType = EPatternType.valueOf(sType);
        }
        catch (IllegalArgumentException iae)
        {
            throw new EmailIOConfigurationException(iae,
                                                    EmailIOConfigurationExceptionMessages.EICE_INVALID_PATTERN_TYPE,
                                                    sType);
        }

        switch (ptType)
        {
            case REGEX:
                pReturn = new RegExPattern(iNode);
                break;

            case XPATH:
                pReturn = new XPathPattern(iNode);
                break;

            case HEADER:
                pReturn = new HeaderPattern(iNode);
                break;

            case CUSTOM:
                pReturn = new CustomPattern(iNode);
                break;
        }

        return pReturn;
    }
}
