package com.cordys.coe.ac.emailio.config.token;

import com.cordys.coe.ac.emailio.EmailIOConnectorConstants;
import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;
import com.cordys.coe.ac.emailio.localization.EmailIOConfigurationExceptionMessages;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * This class wraps around replacement tokens.
 *
 * @author  pgussow
 */
class Token
    implements IToken
{
    /**
     * Holds the name of the tag 'value'.
     */
    private static final String TAG_VALUE = "value";
    /**
     * Holds the name of the tag 'name'.
     */
    private static final String TAG_NAME = "name";
    /**
     * Holds the name of the tag 'token'.
     */
    private static final String TAG_TOKEN = "token";
    /**
     * Holds an empty string.
     */
    private static final String EMPTY_STRING = "";
    /**
     * Holds the name for this token.
     */
    private String m_sName;
    /**
     * Holds the value for this token.
     */
    private String m_sValue;
    /**
     * Holds the XPathMetaInfo object.
     */
    private XPathMetaInfo m_xmi;

    /**
     * Creates a new ReplacementToken object.
     *
     * @param   iNode  The configuration node.
     *
     * @throws  EmailIOConfigurationException  In case of any exception.
     */
    public Token(int iNode)
          throws EmailIOConfigurationException
    {
        m_xmi = new XPathMetaInfo();
        m_xmi.addNamespaceBinding("ns", EmailIOConnectorConstants.NS_CONFIGURATION);

        if (iNode == 0)
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_ERROR_CREATING_PROPERTIES);
        }

        m_sName = XPathHelper.getStringValue(iNode, "./ns:name/text()", m_xmi, EMPTY_STRING);
        m_sValue = XPathHelper.getStringValue(iNode, "./ns:value/text()", m_xmi, EMPTY_STRING);

        if (m_sName.length() == 0)
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_MISSING_TOKEN_NAME);
        }

        if (m_sValue.length() == 0)
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_MISSING_TOKEN_VALUE);
        }
    }

    /**
     * This method gets the name for this token.
     *
     * @return  The name for this token.
     *
     * @see     com.cordys.coe.ac.emailio.config.token.IToken#getName()
     */
    public String getName()
    {
        return m_sName;
    }

    /**
     * This method gets the value for this token.
     *
     * @return  The value for this token.
     *
     * @see     com.cordys.coe.ac.emailio.config.token.IToken#getValue()
     */
    public String getValue()
    {
        return m_sValue;
    }

    /**
     * This method gets the XPathMetaInfo object.
     *
     * @return  The XPathMetaInfo object.
     */
    public XPathMetaInfo getXPathMetaInfo()
    {
        return m_xmi;
    }

    /**
     * This method sets the name for this token.
     *
     * @param  sName  The name for this token.
     */
    public void setName(String sName)
    {
        m_sName = sName;
    }

    /**
     * This method sets the value for this token.
     *
     * @param  sValue  The value for this token.
     */
    public void setValue(String sValue)
    {
        m_sValue = sValue;
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

        sbReturn.append("Name: " + getName());
        sbReturn.append(", Value: " + getValue());

        return sbReturn.toString();
    }

    /**
     * This method dumps the configuration of this token to XML.
     *
     * @param   iParent  The parent element.
     *
     * @return  The created XML structure.
     *
     * @see     com.cordys.coe.ac.emailio.config.token.IToken#toXML(int)
     */
    public int toXML(int iParent)
    {
        int iReturn = 0;

        iReturn = Node.createElementNS(TAG_TOKEN, EMPTY_STRING, EMPTY_STRING,
                                       EmailIOConnectorConstants.NS_CONFIGURATION, iParent);

        Node.createElementWithParentNS(TAG_NAME, getName(), iReturn);
        Node.createElementWithParentNS(TAG_VALUE, getValue(), iReturn);

        return iReturn;
    }
}
