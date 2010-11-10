package com.cordys.coe.ac.emailio.config;

/**
 * This interface identifies that an object can serialize itself to XML.
 *
 * @author  pgussow
 */
public interface IXMLSerializable
{
    /**
     * This method dumps the configuration of this action to XML.
     *
     * @param   iParent  The parent element.
     *
     * @return  The created XML structure.
     */
    int toXML(int iParent);
}
