package com.cordys.coe.ac.emailio.config.pattern;

import com.cordys.coe.ac.emailio.config.rule.IRule;
import com.cordys.coe.ac.emailio.config.rule.IRuleContext;
import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;
import com.cordys.coe.ac.emailio.exception.TriggerEngineException;
import com.cordys.coe.ac.emailio.localization.EmailIOConfigurationExceptionMessages;
import com.cordys.coe.ac.emailio.localization.TriggerEngineExceptionMessages;
import com.cordys.coe.exception.GeneralException;
import com.cordys.coe.util.XMLProperties;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Node;

import java.util.HashMap;

/**
 * This class wraps around header patterns. Header patterns are some sort of regex patterns, but
 * only apply to the multipart sections.
 *
 * @author  pgussow
 */
class HeaderPattern extends RegExPattern
{
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(HeaderPattern.class);
    /**
     * Holds the name of the tag 'name'.
     */
    private static final String TAG_NAME = "name";
    /**
     * Holds the name of the header to match.
     */
    private String m_sName;

    /**
     * Creates a new HeaderPattern object.
     *
     * @param   iNode  The configuration XML.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    public HeaderPattern(int iNode)
                  throws EmailIOConfigurationException
    {
        super(iNode);

        XMLProperties xpBase = null;

        try
        {
            xpBase = new XMLProperties(iNode);
        }
        catch (GeneralException e)
        {
            throw new EmailIOConfigurationException(e,
                                                    EmailIOConfigurationExceptionMessages.EICE_ERROR_CREATING_PATTERN);
        }

        m_sName = xpBase.getStringValue(TAG_NAME, "");

        if (m_sName.length() == 0)
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_ERROR_CREATING_PATTERN_MISSING_NAME);
        }
    }

    /**
     * This method evaluates a regular expression on a header. It assumes that the main content is a
     * hashmap. It basically works the same as a RegEx pattern, only it works on a specific header.
     *
     * @param   pcContext  The context for the pattern.
     * @param   oValue     The value to operate on.
     * @param   rRule      The parent rule.
     *
     * @return  true if the passed on value matches the pattern. Otherwise false.
     *
     * @throws  TriggerEngineException  In case of any exception.
     *
     * @see     com.cordys.coe.ac.emailio.config.pattern.IPattern#evaluate(IRuleContext, Object,
     *          IRule)
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean evaluate(IRuleContext pcContext, Object oValue, IRule rRule)
                     throws TriggerEngineException
    {
        boolean bReturn = false;
        boolean bLog = true;

        if (oValue == null)
        {
            throw new TriggerEngineException(TriggerEngineExceptionMessages.TEE_THE_VALUE_CAN_NOT_BE_NULL);
        }

        if (oValue instanceof HashMap)
        {
            HashMap<String, String> hmHeaders = (HashMap<String, String>) oValue;

            if (hmHeaders.containsKey(m_sName))
            {
                // Check if the header name exists.
                String sValue = hmHeaders.get(m_sName);

                // Now that we have the value we can evaluate the pattern as a normal regex.
                bReturn = super.evaluate(pcContext, sValue, rRule);
                bLog = false;
            }
        }
        else
        {
            throw new TriggerEngineException(TriggerEngineExceptionMessages.TEE_THE_TYPE_GIVEN_IS_NOT_SUPPORTED,
                                             oValue.getClass().getName());
        }

        if (LOG.isDebugEnabled() && bLog)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Pattern match: " + bReturn + "\nPattern details: " + this.toString());
            }
        }

        if ((bReturn == false) && isOptional())
        {
            bReturn = true;
        }

        return bReturn;
    }

    /**
     * This method dumps the configuration of this pattern to XML.
     *
     * @param   parent  The parent element.
     *
     * @return  The created XML structure.
     *
     * @see     com.cordys.coe.ac.emailio.config.pattern.BasePattern#toXML(int)
     */
    @Override public int toXML(int parent)
    {
        int iReturn = super.toXML(parent);

        Node.createElementWithParentNS(TAG_NAME, m_sName, iReturn);

        return iReturn;
    }

    /**
     * This method returns whether or not this pattern works on the headers of an email message.
     *
     * @return  true if this patterns needs the headers. Otherwise false.
     *
     * @see     com.cordys.coe.ac.emailio.config.pattern.IPattern#worksOnHeader()
     */
    @Override public boolean worksOnHeader()
    {
        return true;
    }

    /**
     * This method returns the type of pattern.
     *
     * @return  The pattern type.
     *
     * @see     com.cordys.coe.ac.emailio.config.pattern.RegExPattern#getType()
     */
    @Override protected EPatternType getType()
    {
        return EPatternType.HEADER;
    }
}
