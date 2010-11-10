package com.cordys.coe.ac.emailio.config.outbound;

import com.cordys.coe.ac.emailio.config.IXMLSerializable;

/**
 * This class wraps the details of a header.
 *
 * @author  pgussow
 */
public interface IHeader extends IXMLSerializable
{
    /**
     * Holds the name of the tag 'header'.
     */
    String TAG_HEADER = "header";
    /**
     * Holds the name of the tag 'name'.
     */
    String TAG_NAME = "name";
    /**
     * Holds the name of the tag 'value'.
     */
    String TAG_VALUE = "value";

    /**
     * This method gets the name of the header.
     *
     * @return  The name of the header.
     */
    String getName();

    /**
     * This method gets the value of the header.
     *
     * @return  The value of the header.
     */
    String getValue();
}
