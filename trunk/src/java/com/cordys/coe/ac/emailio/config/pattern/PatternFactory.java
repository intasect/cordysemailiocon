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
