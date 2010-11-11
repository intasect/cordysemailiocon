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

import com.cordys.coe.ac.emailio.exception.OutboundEmailException;
import com.cordys.coe.ac.emailio.localization.OutboundEmailExceptionMessages;
import com.cordys.coe.ac.emailio.util.StringUtil;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * This class wraps the email address as sent using the standard Cordys SendMail method.
 *
 * @author  pgussow
 */
public class CompatibilityEmailAddress extends EmailAddress
{
    /**
     * Holds the name of the email address.
     */
    private static final String TAG_COMP_EMAIL_ADDRESS = "emailAddress";
    /**
     * Holds the name of the display name.
     */
    private static final String TAG_COMP_DISPLAY_NAME = "displayName";

    /**
     * Constructor.
     *
     * @param   iEmailAddress  The XML definition of the address.
     * @param   xmi            The XPathMetaInfo object. The prefix 'ns' must be mapped to the
     *                         proper namespace.
     *
     * @throws  OutboundEmailException  In case of any exceptions.
     */
    public CompatibilityEmailAddress(int iEmailAddress, XPathMetaInfo xmi)
                              throws OutboundEmailException
    {
        super();

        // Get the email address
        String sEmailAddress = XPathHelper.getStringValue(iEmailAddress,
                                                          "./ns:" + TAG_COMP_EMAIL_ADDRESS, xmi,
                                                          "");

        if (!StringUtil.isSet(sEmailAddress))
        {
            throw new OutboundEmailException(OutboundEmailExceptionMessages.OEE_THE_TAG_0_MUST_BE_FILLED,
                                             TAG_COMP_EMAIL_ADDRESS);
        }

        setEmailAddress(sEmailAddress);

        // Now do the display name
        String sDisplayName = XPathHelper.getStringValue(iEmailAddress,
                                                         "./ns:" + TAG_COMP_DISPLAY_NAME, xmi, "");
        setDisplayName(sDisplayName);

        parseInternetAddress();
    }

    /**
     * Creates a new CompatibilityEmailAddress object.
     *
     * @param   sEmailAddress  The actual email address.
     * @param   sDisplayName   The display name for the email address.
     *
     * @throws  OutboundEmailException  In case of any exceptions.
     */
    public CompatibilityEmailAddress(String sEmailAddress, String sDisplayName)
                              throws OutboundEmailException
    {
        super(sEmailAddress, sDisplayName);
    }
}
