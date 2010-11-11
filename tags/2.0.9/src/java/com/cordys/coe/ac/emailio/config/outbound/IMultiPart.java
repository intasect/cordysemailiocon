package com.cordys.coe.ac.emailio.config.outbound;

import com.cordys.coe.ac.emailio.config.IXMLSerializable;

/**
 * This interface holds the data for a mime part.
 *
 * @author  pgussow
 */
public interface IMultiPart extends IXMLSerializable
{
    /**
     * Holds the name of the tag 'data'.
     */
    String TAG_DATA = "data";
    /**
     * Holds the name of the tag 'mimepart'.
     */
    String TAG_MULTI_PART = "multipart";
    /**
     * Holds the name of the tag 'subtype'.
     */
    String TAG_SUB_TYPE = "subtype";

    /**
     * This method gets the mail data for this part.
     *
     * @return  The mail data for this part.
     */
    IMailData getMailData();

    /**
     * This method gets the nested multi parts.
     *
     * @return  The nested multi parts.
     */
    IMultiPart[] getMultiParts();

    /**
     * This method gets the sub type.
     *
     * @return  The sub type.
     */
    String getSubType();

    /**
     * This method returns whether or not this mime part has data.
     *
     * @return  Whether or not this mime part has data.
     */
    boolean hasData();

    /**
     * This method returns whether or not this multipart has nested multiparts.
     *
     * @return  Whether or not this multipart has nested multiparts.
     */
    boolean hasNestedParts();
}
