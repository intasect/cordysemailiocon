

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

import com.cordys.coe.ac.emailio.EmailIOConnectorConstants;
import com.cordys.coe.ac.emailio.config.NamespaceBinding;
import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;
import com.cordys.coe.ac.emailio.localization.EmailIOConfigurationExceptionMessages;
import com.cordys.coe.ac.emailio.util.StringUtil;
import com.cordys.coe.util.general.Util;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.directory.soap.DN;
import com.eibus.directory.soap.DirectoryException;
import com.eibus.directory.soap.LDAPDirectory;
import com.eibus.directory.soap.LDAPUtil;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

import com.novell.ldap.LDAPEntry;

import java.util.ArrayList;

/**
 * This class wraps around the message configuration.
 *
 * @author  pgussow
 */
class Message
    implements IMessage
{
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(Message.class);
    /**
     * Holds the name of the tag 'repeatingxpath'.
     */
    private static final String TAG_REPEATINGXPATH = "repeatingxpath";
    /**
     * Holds the name of the tag 'timeout'.
     */
    private static final String TAG_TIMEOUT = "timeout";
    /**
     * Holds the name of the tag 'soapdn'.
     */
    private static final String TAG_SOAPDN = "soapdn";
    /**
     * Holds the name of the tag 'organization'.
     */
    private static final String TAG_ORGANIZATION = "organization";
    /**
     * Holds the name of the tag 'sync'.
     */
    private static final String TAG_SYNC = "sync";
    /**
     * Holds the name of the tag 'user'.
     */
    private static final String TAG_USER = "user";
    /**
     * Holds the name of the tag 'method'.
     */
    private static final String TAG_METHOD = "method";
    /**
     * Holds the name of the tag 'namespace'.
     */
    private static final String TAG_NAMESPACE = "namespace";
    /**
     * Holds an empty string.
     */
    private static final String EMPTY_STRING = "";
    /**
     * Holds the default timeout to use.
     */
    private static final long DEFAULT_TIMEOUT = 30000L;
    /**
     * Holds the name of the tag 'message'.
     */
    private static final String TAG_MESSAGE = "message";
    /**
     * Holds the list of mappings to apply before sending the message.
     */
    private ArrayList<IMapping> m_alMappings = new ArrayList<IMapping>();
    /**
     * Indicates whether or not the SOAP message should be sent in a synchronous or asynchronous
     * way.
     */
    private boolean m_bSyncCall;
    /**
     * Holds the timeout for waiting for a response. Note: This value is only looked at when the
     * call is synchronous.
     */
    private long m_lTimeout;
    /**
     * Holds the namespace binding configured for this rule.
     */
    private NamespaceBinding m_nbBinding = null;
    /**
     * Holds the XML template to use for creating the trigger message.
     */
    private String m_sInputXML;
    /**
     * Holds the name of the SOAP method that needs to be executed.
     */
    private String m_sMethod;
    /**
     * Holds the namespace of the SOAP method that needs to be executed.
     */
    private String m_sNamespace;
    /**
     * Holds the organizational context for the SOAP method that needs to be executed.
     */
    private String m_sOrganization;
    /**
     * Holds the XPath of the repeating structure within the message request. If this message does
     * not support any repeating group it holds an empty string.
     */
    private String m_sRepeatingXPath;
    /**
     * Holds the DN of the SOAP node/processor to which the SOAP message should be send.
     */
    private String m_sSoapDN;
    /**
     * Holds the user context for the SOAP method that needs to be executed.
     */
    private String m_sUserDN;

    /**
     * Creates a new Mapping object.
     *
     * @param   iNode  The configuration node.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    public Message(int iNode)
            throws EmailIOConfigurationException
    {
        XPathMetaInfo xmi = new XPathMetaInfo();
        xmi.addNamespaceBinding("ns", EmailIOConnectorConstants.NS_CONFIGURATION);

        if (iNode == 0)
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_ERROR_CREATING_MAPPING_CONFIGURATION);
        }

        // First get and check the mandatory fields.
        m_sMethod = XPathHelper.getStringValue(iNode, "./ns:method/text()", xmi, EMPTY_STRING);
        m_sNamespace = XPathHelper.getStringValue(iNode, "./ns:namespace/text()", xmi,
                                                  EMPTY_STRING);

        if (m_sMethod.length() == 0)
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_THE_METHOD_NAME_MUST_BE_SPECIFIED);
        }

        if (m_sNamespace.length() == 0)
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_THE_NAMESPACE_MUST_BE_SPECIFIED);
        }

        m_sUserDN = XPathHelper.getStringValue(iNode, "./ns:user/text()", xmi, EMPTY_STRING);
        m_sOrganization = XPathHelper.getStringValue(iNode, "./ns:organization/text()", xmi,
                                                     EMPTY_STRING);
        m_sSoapDN = XPathHelper.getStringValue(iNode, "./ns:soapdn/text()", xmi, EMPTY_STRING);
        m_sRepeatingXPath = XPathHelper.getStringValue(iNode, "./ns:repeatingxpath/text()", xmi,
                                                       EMPTY_STRING);

        m_lTimeout = XPathHelper.getLongValue(iNode, "./ns:timeout/text()", xmi, DEFAULT_TIMEOUT);
        m_bSyncCall = XPathHelper.getBooleanValue(iNode, "./ns:sync/text()", xmi, true);

        // The input will be stored as a string and parsed when needed. This is done to
        // avoid the need to somehow free the XML when the object is garbage-collected.
        int iInput = XPathHelper.selectSingleNode(iNode, "./ns:input", xmi);

        if (iInput != 0)
        {
            m_sInputXML = Node.writeToString(iInput, false);
        }

        // Now parse the mappings.
        int[] aiMappings = XPathHelper.selectNodes(iNode, "./ns:mappings/ns:mapping", xmi);

        for (int iCount = 0; iCount < aiMappings.length; iCount++)
        {
            int iMappingNode = aiMappings[iCount];
            IMapping mMapping = new Mapping(iMappingNode);

            m_alMappings.add(mMapping);
        }

        // Parse the namespace prefix bindings.
        int iBinding = XPathHelper.selectSingleNode(iNode, "./ns:namespacemappings", xmi);

        if (iBinding != 0)
        {
            m_nbBinding = new NamespaceBinding(iBinding);
        }
        else
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("No namespace mappings were found in the message: " +
                          Node.writeToString(iNode, false));
            }
        }
    }

    /**
     * This method gets the XML template to use for creating the trigger message.
     *
     * @return  The XML template to use for creating the trigger message.
     *
     * @see     com.cordys.coe.ac.emailio.config.message.IMessage#getInputXML()
     */
    public String getInputXML()
    {
        return m_sInputXML;
    }

    /**
     * This method gets the mappings for this message.
     *
     * @return  The mappings for this message.
     *
     * @see     com.cordys.coe.ac.emailio.config.message.IMessage#getMappings()
     */
    public IMapping[] getMappings()
    {
        return m_alMappings.toArray(new IMapping[0]);
    }

    /**
     * This method gets the name of the SOAP method that needs to be executed.
     *
     * @return  The name of the SOAP method that needs to be executed.
     *
     * @see     com.cordys.coe.ac.emailio.config.message.IMessage#getMethod()
     */
    public String getMethod()
    {
        return m_sMethod;
    }

    /**
     * This method gets the namespace of the SOAP method that needs to be executed.
     *
     * @return  The namespace of the SOAP method that needs to be executed.
     *
     * @see     com.cordys.coe.ac.emailio.config.message.IMessage#getNamespace()
     */
    public String getNamespace()
    {
        return m_sNamespace;
    }

    /**
     * This method gets the organizational context for the SOAP method that needs to be executed.
     *
     * @return  The organizational context for the SOAP method that needs to be executed.
     *
     * @see     com.cordys.coe.ac.emailio.config.message.IMessage#getOrganization()
     */
    public String getOrganization()
    {
        return m_sOrganization;
    }

    /**
     * This method gets the XPath of the repeating structure within the message request. If this
     * message does not support any repeating group it holds an empty string.
     *
     * @return  The XPath of the repeating structure within the message request. If this message
     *          does not support any repeating group it holds an empty string.
     *
     * @see     com.cordys.coe.ac.emailio.config.message.IMessage#getRepeatingXPath()
     */
    public String getRepeatingXPath()
    {
        return m_sRepeatingXPath;
    }

    /**
     * This method gets the DN of the SOAP node/processor to which the SOAP message should be send.
     *
     * @return  The DN of the SOAP node/processor to which the SOAP message should be send.
     *
     * @see     com.cordys.coe.ac.emailio.config.message.IMessage#getSoapDN()
     */
    public String getSoapDN()
    {
        return m_sSoapDN;
    }

    /**
     * This method gets whether or not the SOAP message should be sent in a synchronous way.
     *
     * @return  Whether or not the SOAP message should be sent in a synchronous way.
     *
     * @see     com.cordys.coe.ac.emailio.config.message.IMessage#getSynchronous()
     */
    public boolean getSynchronous()
    {
        return m_bSyncCall;
    }

    /**
     * This method gets the timeout for waiting for a response. Note: This value is only looked at
     * when the call is synchronous.
     *
     * @return  The timeout for waiting for a response. Note: This value is only looked at when the
     *          call is synchronous.
     *
     * @see     com.cordys.coe.ac.emailio.config.message.IMessage#getTimeout()
     */
    public long getTimeout()
    {
        return m_lTimeout;
    }

    /**
     * This method gets the user context for the SOAP method that needs to be executed.
     *
     * @return  The user context for the SOAP method that needs to be executed.
     *
     * @see     com.cordys.coe.ac.emailio.config.message.IMessage#getUserDN()
     */
    public String getUserDN()
    {
        return m_sUserDN;
    }

    /**
     * This method gets the namespace binding for this rule.
     *
     * @return  The namespace binding for this rule.
     *
     * @see     com.cordys.coe.ac.emailio.config.message.IMessage#getXPathMetaInfo()
     */
    public XPathMetaInfo getXPathMetaInfo()
    {
        if (m_nbBinding != null)
        {
            return m_nbBinding.getXPathMetaInfo();
        }
        else
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("The namespace binding was null, returning blank namespace meta info object.");
            }

            return new XPathMetaInfo();
        }
    }

    /**
     * This method sets the XML template to use for creating the trigger message.
     *
     * @param  sInputXML  The XML template to use for creating the trigger message.
     */
    public void setInputXML(String sInputXML)
    {
        m_sInputXML = sInputXML;
    }

    /**
     * This method sets the name of the SOAP method that needs to be executed.
     *
     * @param  sMethod  The name of the SOAP method that needs to be executed.
     */
    public void setMethod(String sMethod)
    {
        m_sMethod = sMethod;
    }

    /**
     * This method sets the namespace of the SOAP method that needs to be executed.
     *
     * @param  sNamespace  The namespace of the SOAP method that needs to be executed.
     */
    public void setNamespace(String sNamespace)
    {
        m_sNamespace = sNamespace;
    }

    /**
     * This method sets the organizational context for the SOAP method that needs to be executed.
     *
     * @param  sOrganization  The organizational context for the SOAP method that needs to be
     *                        executed.
     */
    public void setOrganization(String sOrganization)
    {
        m_sOrganization = sOrganization;
    }

    /**
     * This method sets the XPath of the repeating structure within the message request. If this
     * message does not support any repeating group it holds an empty string.
     *
     * @param  sRepeatingXPath  The XPath of the repeating structure within the message request. If
     *                          this message does not support any repeating group it holds an empty
     *                          string.
     */
    public void setRepeatingXPath(String sRepeatingXPath)
    {
        m_sRepeatingXPath = sRepeatingXPath;
    }

    /**
     * This method sets the DN of the SOAP node/processor to which the SOAP message should be send..
     *
     * @param  sSoapDN  The DN of the SOAP node/processor to which the SOAP message should be send..
     */
    public void setSoapDN(String sSoapDN)
    {
        m_sSoapDN = sSoapDN;
    }

    /**
     * This method sets wether or not the SOAP message should be sent in a synchronous way.
     *
     * @param  bSyncCall  Whether or not the SOAP message should be sent in a synchronous way.
     */
    public void setSynchronous(boolean bSyncCall)
    {
        m_bSyncCall = bSyncCall;
    }

    /**
     * This method sets the timeout for waiting for a response. Note: This value is only looked at
     * when the call is synchronous.
     *
     * @param  lTimeout  The timeout for waiting for a response. Note: This value is only looked at
     *                   when the call is synchronous.
     */
    public void setTimeout(long lTimeout)
    {
        m_lTimeout = lTimeout;
    }

    /**
     * This method sets the user context for the SOAP method that needs to be executed.
     *
     * @param  sUserDN  The user context for the SOAP method that needs to be executed.
     */
    public void setUserDN(String sUserDN)
    {
        m_sUserDN = sUserDN;
    }

    /**
     * This method gets whether or not this message is capable of handling multiple emails in a
     * single request.
     *
     * @return  Whether or not this message is capable of handling multiple emails in a single
     *          request.
     *
     * @see     com.cordys.coe.ac.emailio.config.message.IMessage#supportsMultipleEmails()
     */
    public boolean supportsMultipleEmails()
    {
        return ((m_sRepeatingXPath != null) && (m_sRepeatingXPath.length() > 0));
    }

    /**
     * This method returns a string representation of the object.
     *
     * @return  A string representation of the object.
     *
     * @see     java.lang.Object#toString()
     */
    @Override public String toString()
    {
        StringBuilder sbReturn = new StringBuilder();

        sbReturn.append("Method: " + getMethod());
        sbReturn.append("\nNamespace: " + getNamespace());
        sbReturn.append("\nOrganization: " + getOrganization());
        sbReturn.append("\nUserDN: " + getUserDN());
        sbReturn.append("\nSoapDN: " + getSoapDN());
        sbReturn.append("\nSynchronous: " + getSynchronous());
        sbReturn.append("\nTimeout: " + getTimeout());
        sbReturn.append("\nRepeating XPath: " + getRepeatingXPath());
        sbReturn.append("\nInput XML: " + getInputXML());

        sbReturn.append("\nMappings:");
        sbReturn.append("\n---------\n");

        for (IMapping mMapping : m_alMappings)
        {
            sbReturn.append(mMapping.toString());
            sbReturn.append("\n");
        }

        return sbReturn.toString();
    }

    /**
     * This method dumps the configuration of this message to XML.
     *
     * @param   iParent  The parent element.
     *
     * @return  The created XML structure.
     *
     * @see     com.cordys.coe.ac.emailio.config.message.IMessage#toXML(int)
     */
    public int toXML(int iParent)
    {
        int iReturn = 0;

        iReturn = Node.createElementNS(TAG_MESSAGE, EMPTY_STRING, EMPTY_STRING,
                                       EmailIOConnectorConstants.NS_CONFIGURATION, iParent);

        Node.createElementWithParentNS(TAG_METHOD, getMethod(), iReturn);
        Node.createElementWithParentNS(TAG_NAMESPACE, getNamespace(), iReturn);

        if ((m_sOrganization != null) && (m_sOrganization.length() > 0))
        {
            Node.createElementWithParentNS(TAG_ORGANIZATION, getOrganization(), iReturn);
        }

        if ((m_sUserDN != null) && (m_sUserDN.length() > 0))
        {
            Node.createElementWithParentNS(TAG_USER, getUserDN(), iReturn);
        }

        if ((m_sSoapDN != null) && (m_sSoapDN.length() > 0))
        {
            Node.createElementWithParentNS(TAG_SOAPDN, getSoapDN(), iReturn);
        }

        Node.createElementWithParentNS(TAG_SYNC, String.valueOf(getSynchronous()), iReturn);
        Node.createElementWithParentNS(TAG_TIMEOUT, String.valueOf(getTimeout()), iReturn);

        if ((m_sRepeatingXPath != null) && (m_sRepeatingXPath.length() > 0))
        {
            Node.createElementWithParentNS(TAG_REPEATINGXPATH, getRepeatingXPath(), iReturn);
        }

        // Dump the namespace mappings
        if (m_nbBinding != null)
        {
            m_nbBinding.toXML(iReturn);
        }

        if ((m_sInputXML != null) && (m_sInputXML.length() > 0))
        {
            try
            {
                Node.appendToChildren(Node.getDocument(iReturn).parseString(m_sInputXML), iReturn);
            }
            catch (Exception e)
            {
                // Ignore it, since it can't happen.
            }
        }

        if ((m_alMappings != null) && (m_alMappings.size() > 0))
        {
            int iMappings = Node.createElementWithParentNS("mappings", "", iReturn);

            for (IMapping mMapping : m_alMappings)
            {
                mMapping.toXML(iMappings);
            }
        }

        return iReturn;
    }

    /**
     * This method validates the given message configuration. The things it will validate are:
     *
     * <p>1. If the organization DN exists.<br>
     * 2. If the user DN exists.<br>
     * 3. If there is a SOAP node actaully implementing the given method.<br>
     * 4. If the Soap processor/node exists.<br>
     * </p>
     *
     * @throws  EmailIOConfigurationException  In case of any excpetions.
     */
    public void validate()
                  throws EmailIOConfigurationException
    {
        try
        {
            LDAPDirectory lDir = new LDAPDirectory();

            if (StringUtil.isSet(getOrganization()))
            {
                try
                {
                    lDir.read(getOrganization());
                }
                catch (DirectoryException de)
                {
                    if (de.getResultCode() == DirectoryException.NO_SUCH_OBJECT)
                    {
                        throw new EmailIOConfigurationException(de,
                                                                EmailIOConfigurationExceptionMessages.EICE_ORGANIZATION_DOES_NOT_EXIST,
                                                                getOrganization());
                    }
                }
            }

            if (StringUtil.isSet(getUserDN()))
            {
                try
                {
                    lDir.read(getUserDN());
                }
                catch (DirectoryException de)
                {
                    if (de.getResultCode() == DirectoryException.NO_SUCH_OBJECT)
                    {
                        throw new EmailIOConfigurationException(de,
                                                                EmailIOConfigurationExceptionMessages.EICE_USER_DN_DOES_NOT_EXIST,
                                                                getUserDN());
                    }
                }
            }

            if (StringUtil.isSet(getSoapDN()))
            {
                try
                {
                    lDir.read(getSoapDN());
                }
                catch (DirectoryException de)
                {
                    if (de.getResultCode() == DirectoryException.NO_SUCH_OBJECT)
                    {
                        throw new EmailIOConfigurationException(de,
                                                                EmailIOConfigurationExceptionMessages.EICE_SOAP_NODE__PROCESSOR_DOES_NOT_EXIST,
                                                                getSoapDN());
                    }
                }
            }

            // Now we need to validate that there is actually a SOAP node implementing the given
            // method name and namespace.
            String sOrganization = getOrganization();
            String sUsername = getUserDN();

            if (!StringUtil.isSet(sUsername))
            {
                sUsername = lDir.getOrganizationalUser();
            }

            if (!StringUtil.isSet(sOrganization))
            {
                if (StringUtil.isSet(sUsername))
                {
                    sOrganization = Util.getOrganizationFromUser(sUsername);
                }
                else
                {
                    // TODO: It should be the organization in which
                    // the processor is running. But we need to figure out how to get that
                    // information at this level.
                    sOrganization = lDir.getOrganization();
                }
            }

            if (StringUtil.isSet(getSoapDN()))
            {
                // A destination has been set. It can either be a SOAP node or a SOAP processor.
                // So we need to figure out if they support the current namespace.
                LDAPEntry leSOAPNode = lDir.read(getSoapDN());
                String[] as = LDAPUtil.getStringValues(leSOAPNode, "objectclass");
                boolean bDone = false;

                do
                {
                    for (String sValue : as)
                    {
                        if (StringUtil.isSet(sValue) && sValue.equals("bussoapprocessor"))
                        {
                            // We have a SOAP processor, so we need the SOAP node
                            leSOAPNode = lDir.read(DN.getDN(leSOAPNode.getDN()).getParent()
                                                   .toString());
                        }
                        else if (StringUtil.isSet(sValue) && sValue.equals("bussoapnode"))
                        {
                            bDone = true;
                        }
                    }
                }
                while (bDone == false);

                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Entry of the SOAP node: " + leSOAPNode.getDN());
                }

                String[] asURIs = LDAPUtil.getStringValues(leSOAPNode, "namespace");
                boolean bFound = false;

                for (String sURI : asURIs)
                {
                    if (getNamespace().equals(sURI))
                    {
                        bFound = true;
                        break;
                    }
                }

                if (!bFound)
                {
                    throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_THE_SOAP_NODE_0_DOES_NOT_HAVE_NAMESPACE_1_ATTACHED,
                                                            leSOAPNode.getDN(), getNamespace());
                }

                // TODO: we should also check the method name. It is possible nowadays to have
                // multiple methodsets with the same namespace and different methods. So we should
                // check the
            }
            else
            {
                // No specific receiver was given, so we'll use the default mechanism.
                try
                {
                    lDir.findSOAPNode(sOrganization, getNamespace(), getMethod());
                }
                catch (DirectoryException de)
                {
                    if (de.getResultCode() == DirectoryException.NO_SUCH_OBJECT)
                    {
                        throw new EmailIOConfigurationException(de,
                                                                EmailIOConfigurationExceptionMessages.EICE_COULD_NOT_FIND_A_SOAP_NODE_IMPLEMENTING_01_IN_ORGANIZATION_2,
                                                                getNamespace(), getMethod(),
                                                                sOrganization);
                    }
                }
            }
        }
        catch (EmailIOConfigurationException iece)
        {
            throw iece;
        }
        catch (Exception e)
        {
            throw new EmailIOConfigurationException(e,
                                                    EmailIOConfigurationExceptionMessages.EICE_ERROR_INITIALIZING_LDAP_CONNECTION);
        }
    }
}
