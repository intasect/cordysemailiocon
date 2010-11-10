package com.cordys.coe.ac.emailio.config.rule;

import com.cordys.coe.ac.emailio.config.EEmailSection;
import com.cordys.coe.ac.emailio.config.IXMLSerializable;
import com.cordys.coe.ac.emailio.config.pattern.IPattern;
import com.cordys.coe.ac.emailio.exception.EmailIOConfigurationException;

import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * The interface describing a rule.
 *
 * @author  pgussow
 */
public interface IRule extends IXMLSerializable
{
    /**
     * This method gets the patterns for this rule.
     *
     * @return  The patterns for this rule.
     */
    IPattern[] getPatterns();

    /**
     * This method gets the section where to apply this rule.
     *
     * @return  The section where to apply this rule.
     */
    EEmailSection getSection();

    /**
     * This method gets the namespace binding for this rule.
     *
     * @return  The namespace binding for this rule.
     */
    XPathMetaInfo getXPathMetaInfo();

    /**
     * This method is called to validate the configuration. This method is used to i.e. check if the
     * email server is reachable or that the messages configured are actually sendable.
     *
     * @throws  EmailIOConfigurationException  In case of any exceptions.
     */
    void validate()
           throws EmailIOConfigurationException;
}
