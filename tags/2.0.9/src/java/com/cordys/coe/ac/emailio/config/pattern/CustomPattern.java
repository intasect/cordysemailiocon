package com.cordys.coe.ac.emailio.config.pattern;

import com.cordys.coe.ac.emailio.config.rule.IRule;
import com.cordys.coe.ac.emailio.config.rule.IRuleContext;
import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;
import com.cordys.coe.ac.emailio.exception.TriggerEngineException;
import com.cordys.coe.ac.emailio.localization.EmailIOConfigurationExceptionMessages;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Node;

import java.lang.reflect.Constructor;

/**
 * This pattern allows for a custom class to do custom processing.
 *
 * @author  pgussow
 */
public class CustomPattern extends BasePattern
{
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(CustomPattern.class);
    /**
     * Holds the custom pattern..
     */
    private IPattern m_pNested;

    /**
     * Creates a new CustomPattern object.
     *
     * @param   iNode  The configuration node.
     *
     * @throws  EmailIOConfigurationException  In case the pattern is invalid.
     */
    public CustomPattern(int iNode)
                  throws EmailIOConfigurationException
    {
        super(iNode);

        if ((getValue() == null) || (getValue().length() == 0))
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_CLASSNAME_COULD_NOT_BE_FOUND_FOR_THE_CUSTOM_RULE,
                                                    Node.writeToString(iNode, false));
        }

        String sRealClassName = getValue();

        // Try loading the class
        Class<?> cRealPattern = null;

        try
        {
            cRealPattern = Class.forName(sRealClassName);
        }
        catch (ClassNotFoundException e)
        {
            throw new EmailIOConfigurationException(e,
                                                    EmailIOConfigurationExceptionMessages.EICE_COULD_NOT_FIND_THE_CUSTOM_PATTERN_CLASS_0,
                                                    sRealClassName);
        }

        if (!IPattern.class.isAssignableFrom(cRealPattern))
        {
            throw new EmailIOConfigurationException(EmailIOConfigurationExceptionMessages.EICE_CLASS_DOES_NOT_IMPLEMENT_THE_IPATTERN_INTERFACE,
                                                    sRealClassName);
        }

        // Find the proper constructor.
        Constructor<?> cConstructor = null;

        try
        {
            cConstructor = cRealPattern.getConstructor(int.class);
        }
        catch (Exception e)
        {
            throw new EmailIOConfigurationException(e,
                                                    EmailIOConfigurationExceptionMessages.EICE_COULD_NOT_FIND_THE_PROPER_CONSTRUCTOR_INT_FOR_CLASS,
                                                    sRealClassName);
        }

        // Now we can instantiate the pattern.
        try
        {
            m_pNested = (IPattern) cConstructor.newInstance(iNode);
        }
        catch (Exception e)
        {
            throw new EmailIOConfigurationException(e,
                                                    EmailIOConfigurationExceptionMessages.EICE_ERROR_INSTANTIATING_THE_CUSTOM_PATTERN_CLASS,
                                                    sRealClassName);
        }
    }

    /**
     * This method evaluates an XPath expression. It assumes that the main content is an int which
     * referes to a NOM XML node.
     *
     * @param   pcContext  The context for the pattern.
     * @param   oValue     The value to operate on.
     * @param   rRule      The parent rule.
     *
     * @return  true if the passed on value matches the pattern. Otherwise false.
     *
     * @throws  TriggerEngineException  In case there was an error executing the trigger.
     *
     * @see     com.cordys.coe.ac.emailio.config.pattern.IPattern#evaluate(IRuleContext, Object,
     *          IRule)
     */
    @Override public boolean evaluate(IRuleContext pcContext, Object oValue, IRule rRule)
                               throws TriggerEngineException
    {
        boolean bReturn = m_pNested.evaluate(pcContext, oValue, rRule);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Pattern match: " + bReturn + "\nPattern details: " + this.toString());
        }

        if ((bReturn == false) && isOptional())
        {
            bReturn = true;
        }

        return bReturn;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.pattern.BasePattern#toXML(int)
     */
    @Override public int toXML(int parent)
    {
        return m_pNested.toXML(parent);
    }

    /**
     * This method returns the type of pattern.
     *
     * @return  The pattern type.
     *
     * @see     com.cordys.coe.ac.emailio.config.pattern.BasePattern#getType()
     */
    @Override protected EPatternType getType()
    {
        return EPatternType.CUSTOM;
    }
}
