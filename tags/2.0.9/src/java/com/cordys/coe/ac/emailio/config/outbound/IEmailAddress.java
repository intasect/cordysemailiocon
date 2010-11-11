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

import javax.mail.internet.InternetAddress;

/**
 * This interface describes an email address.
 *
 * @author  pgussow
 */
public interface IEmailAddress extends IXMLSerializable
{
    /**
     * Holds the name of the tag 'address'.
     */
    String TAG_ADDRESS = "address";
    /**
     * Holds the name of the tag 'displayname'.
     */
    String TAG_DISPLAY_NAME = "displayname";
    /**
     * Holds the name of the tag 'emailaddress'.
     */
    String TAG_EMAIL_ADDRESS = "emailaddress";

    /**
     * This method gets the display name for this address.
     *
     * @return  The display name for this address.
     */
    String getDisplayName();

    /**
     * This method gets the actual email address.
     *
     * @return  The actual email address.
     */
    String getEmailAddress();

    /**
     * This method gets the internet address to use for this address defintion.
     *
     * @return  The internet address to use for this address defintion.
     */
    InternetAddress getInternetAddress();
}
