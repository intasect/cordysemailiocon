

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
package com.cordys.coe.ac.emailio.config.outbound;

import com.cordys.coe.ac.emailio.exception.OutboundEmailException;
import com.cordys.coe.ac.emailio.localization.OutboundEmailExceptionMessages;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.xml.xpath.XPathMetaInfo;

import java.util.ArrayList;

/**
 * This class parses the internet addresses as passed on in XML.
 *
 * @author  pgussow
 */
public class AddressFactory
{
    /**
     * This method parses the XML email address.
     *
     * @param   iEmailAddress  The XML definition of the email address.
     * @param   xmi            The XPathMetaInfo with the prefix 'ns' mapped to the proper
     *                         namespace.
     *
     * @return  The parsed address.
     *
     * @throws  OutboundEmailException  In case of any exceptions.
     */
    public static IEmailAddress parseEmailAddress(int iEmailAddress, XPathMetaInfo xmi)
                                           throws OutboundEmailException
    {
        IEmailAddress eaReturn = null;

        int iNode = XPathHelper.selectSingleNode(iEmailAddress, "./ns:" + IEmailAddress.TAG_ADDRESS,
                                                 xmi);

        if (iNode == 0)
        {
            throw new OutboundEmailException(OutboundEmailExceptionMessages.OEE_COULD_NOT_FIND_THE_TAG_0_FOR_THE_EMAIL_ADDRESS,
                                             IEmailAddress.TAG_ADDRESS);
        }

        eaReturn = new EmailAddress(iNode, xmi);

        return eaReturn;
    }

    /**
     * This method parses the XML email address from a compatibility perspective.
     *
     * @param   iEmailAddress  The XML definition of the email address.
     * @param   xmi            The XPathMetaInfo with the prefix 'ns' mapped to the proper
     *                         namespace.
     *
     * @return  The parsed address.
     *
     * @throws  OutboundEmailException  In case of any exceptions.
     */
    public static IEmailAddress parseEmailAddressCompatibility(int iEmailAddress,
                                                               XPathMetaInfo xmi)
                                                        throws OutboundEmailException
    {
        IEmailAddress eaReturn = null;

        if (iEmailAddress == 0)
        {
            throw new OutboundEmailException(OutboundEmailExceptionMessages.OEE_COULD_NOT_FIND_THE_TAG_0_FOR_THE_EMAIL_ADDRESS,
                                             IEmailAddress.TAG_ADDRESS);
        }

        eaReturn = new CompatibilityEmailAddress(iEmailAddress, xmi);

        return eaReturn;
    }

    /**
     * This method parses the XML email addresses.
     *
     * @param   iEmailAddress  The XML definition of the email address.
     * @param   xmi            The XPathMetaInfo with the prefix 'ns' mapped to the proper
     *                         namespace.
     *
     * @return  The parsed address.
     *
     * @throws  OutboundEmailException  In case of any exceptions.
     */
    public static IEmailAddress[] parseEmailAddresses(int iEmailAddress, XPathMetaInfo xmi)
                                               throws OutboundEmailException
    {
        ArrayList<IEmailAddress> alTemp = new ArrayList<IEmailAddress>();

        int[] aiNode = XPathHelper.selectNodes(iEmailAddress, "./ns:" + IEmailAddress.TAG_ADDRESS,
                                               xmi);

        if (aiNode.length != 0)
        {
            for (int iAddress : aiNode)
            {
                IEmailAddress eaTemp = new EmailAddress(iAddress, xmi);
                alTemp.add(eaTemp);
            }
        }

        return alTemp.toArray(new IEmailAddress[0]);
    }

    /**
     * This method parses the XML email addresses in compatibility mode.
     *
     * @param   iEmailAddress  The XML definition of the email address.
     * @param   xmi            The XPathMetaInfo with the prefix 'ns' mapped to the proper
     *                         namespace.
     *
     * @return  The parsed address.
     *
     * @throws  OutboundEmailException  In case of any exceptions.
     */
    public static IEmailAddress[] parseEmailAddressesCompatibility(int iEmailAddress,
                                                                   XPathMetaInfo xmi)
                                                            throws OutboundEmailException
    {
        ArrayList<IEmailAddress> alTemp = new ArrayList<IEmailAddress>();

        int[] aiNode = XPathHelper.selectNodes(iEmailAddress, "./ns:" + IEmailAddress.TAG_ADDRESS,
                                               xmi);

        if (aiNode.length != 0)
        {
            for (int iAddress : aiNode)
            {
                IEmailAddress eaTemp = new CompatibilityEmailAddress(iAddress, xmi);
                alTemp.add(eaTemp);
            }
        }

        return alTemp.toArray(new IEmailAddress[0]);
    }
}
