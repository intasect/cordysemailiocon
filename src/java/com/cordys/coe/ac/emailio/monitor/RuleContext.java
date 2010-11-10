package com.cordys.coe.ac.emailio.monitor;

import com.cordys.coe.ac.emailio.EmailIOConnectorConstants;
import com.cordys.coe.ac.emailio.config.rule.IRuleContext;
import com.cordys.coe.ac.emailio.util.MailMessageUtil;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Node;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;

/**
 * This class maintains the context of the rule execution.
 *
 * @author  pgussow
 */
public class RuleContext
    implements IRuleContext
{
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(RuleContext.class);
    /**
     * Holds the context information for a rule execution.
     */
    private Map<String, Object> m_hmContextInfo = Collections.synchronizedMap(new LinkedHashMap<String, Object>());
    /**
     * Holds the actual email message for this context.
     */
    private Message m_mMessage;

    /**
     * Creates a new RuleContext object.
     *
     * @param  mMessage  The context email message.
     */
    public RuleContext(Message mMessage)
    {
        m_mMessage = mMessage;

        // Dump the email message to XML.
        m_hmContextInfo.put(SYS_XML_EMAIL,
                            MailMessageUtil.messageToXML(mMessage,
                                                         EmailIOConnectorConstants.getDocument()));
    }

    /**
     * This method clears the context.
     *
     * @see  com.cordys.coe.ac.emailio.config.rule.IRuleContext#clear()
     */
    public void clear()
    {
        // All NOM nodes in this list have to be freed.
        synchronized (m_hmContextInfo)
        {
            for (String sKey : m_hmContextInfo.keySet())
            {
                Object oTemp = m_hmContextInfo.get(sKey);

                if (oTemp instanceof Integer)
                {
                    Integer iNode = (Integer) oTemp;
                    Node.delete(iNode);
                }
            }
            m_hmContextInfo.clear();
        }
    }

    /**
     * This method returns if the context contains a variable with the name sName.
     *
     * @param   sName  The name of the variable.
     *
     * @return  true is there is a variable with name sName. Otherwise false.
     *
     * @see     com.cordys.coe.ac.emailio.config.rule.IRuleContext#containsValue(java.lang.String)
     */
    public boolean containsValue(String sName)
    {
        synchronized (m_hmContextInfo)
        {
            return m_hmContextInfo.containsKey(sName);
        }
    }

    /**
     * This method returns all values in the current context.
     *
     * @return  All values in the current context.
     *
     * @see     com.cordys.coe.ac.emailio.config.rule.IRuleContext#getAllValues()
     */
    public Map<String, Object> getAllValues()
    {
        return Collections.unmodifiableMap(m_hmContextInfo);
    }

    /**
     * This method gets the actual email message for this context.
     *
     * @return  The actual email message for this context.
     */
    public Message getMessage()
    {
        return m_mMessage;
    }

    /**
     * This method returns the value with the given name.
     *
     * @param   sName  The name of the value.
     *
     * @return  The value for the given name. null if it's not found.
     *
     * @see     com.cordys.coe.ac.emailio.config.rule.IRuleContext#getValue(java.lang.String)
     */
    public Object getValue(String sName)
    {
        Object oReturn = null;

        synchronized (m_hmContextInfo)
        {
            if (m_hmContextInfo.containsKey(sName))
            {
                oReturn = m_hmContextInfo.get(sName);
            }
        }

        return oReturn;
    }

    /**
     * This method puts the value with the given name into the context.
     *
     * @param  sName   The name of the value.
     * @param  oValue  The value to store.
     *
     * @see    com.cordys.coe.ac.emailio.config.rule.IRuleContext#putValue(java.lang.String,
     *         java.lang.Object)
     */
    public void putValue(String sName, Object oValue)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Storing with name '" + sName + "':\n" + oValue);
        }

        synchronized (m_hmContextInfo)
        {
            m_hmContextInfo.put(sName, oValue);
        }
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

        sbReturn.append("Email message:\n");

        try
        {
            if (m_mMessage.isExpunged() || m_mMessage.isSet(Flags.Flag.DELETED))
            {
                if (containsValue(SYS_XML_EMAIL))
                {
                    int iNode = (Integer) getValue(SYS_XML_EMAIL);
                    sbReturn.append(Node.writeToString(iNode, true));
                }
            }
            else
            {
                sbReturn.append(MailMessageUtil.dumpMessage(m_mMessage));
            }
        }
        catch (MessagingException e)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Error checking the deleted flag.", e);
            }

            if (containsValue(SYS_XML_EMAIL))
            {
                int iNode = (Integer) getValue(SYS_XML_EMAIL);
                sbReturn.append(Node.writeToString(iNode, true));
            }
        }
        sbReturn.append("\n==========");
        sbReturn.append("\nVariables:");
        sbReturn.append("\n==========");

        synchronized (m_hmContextInfo)
        {
            for (String sKey : m_hmContextInfo.keySet())
            {
                Object oValue = m_hmContextInfo.get(sKey);
                sbReturn.append("\nKey: ");
                sbReturn.append(sKey);
                sbReturn.append(", Value: ");

                if (oValue instanceof Integer)
                {
                    Integer iTemp = (Integer) oValue;
                    sbReturn.append(Node.writeToString(iTemp, false));
                }
                else
                {
                    sbReturn.append(oValue.toString());
                }
            }
        }

        return sbReturn.toString();
    }
}
