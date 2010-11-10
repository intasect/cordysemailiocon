package com.cordys.coe.ac.emailio.config.outbound;

import com.cordys.coe.ac.emailio.config.IXMLSerializable;

import java.io.InputStream;

/**
 * This interface describes data for a mail message part.
 *
 * @author  pgussow
 */
public interface IMailData extends IXMLSerializable
{
    /**
     * Holds the name of the tag 'disposition'.
     */
    String TAG_CONTENT_DISPOSITION = "contentdisposition";
    /**
     * Holds the name of the tag 'contenttype'.
     */
    String TAG_CONTENT_TYPE = "contenttype";
    /**
     * Holds the name of the tag 'detail';
     */
    String TAG_DETAIL = "detail";
    /**
     * Holds the name of the tag 'source';
     */
    String TAG_SOURCE = "source";

    /**
     * This method gets the content disposition.
     *
     * @return  The content disposition.
     */
    String getContentDisposition();

    /**
     * This method gets the content type.
     *
     * @return  The content type.
     */
    String getContentType();

    /**
     * This method gets the data for this mime part.
     *
     * @return  The data for this mime part.
     */
    byte[] getData();

    /**
     * This method gets the type of data.
     *
     * @return  The type of data.
     */
    EDataSourceType getDataSource();

    /**
     * This method returns a stream to the data for this part. The data could be either character or
     * binary data. If the data is character data the UTF-8 encoding is used.
     *
     * @return  The data stream.
     */
    InputStream getDataStream();
}
