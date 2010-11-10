package com.cordys.coe.ac.emailio.config.outbound;

import com.cordys.coe.ac.emailio.exception.OutboundEmailException;
import com.cordys.coe.ac.emailio.localization.OutboundEmailExceptionMessages;
import com.cordys.coe.ac.emailio.util.StringUtil;
import com.cordys.coe.util.FileUtils;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.util.system.Native;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class contains the actual data that should be set in the mail.
 *
 * @author  pgussow
 */
public class MailData
    implements IMailData
{
    /**
     * Holds the data for this data block.
     */
    private byte[] m_baData;
    /**
     * Holds the type for this data.
     */
    private EDataSourceType m_dstDataType;

    /**
     * Holds the content disposition for this data block.
     */
    private String m_sContentDisposition;
    /**
     * Holds the content type for this data block.
     */
    private String m_sContentType;

    /**
     * Creates a new MailData object.
     */
    public MailData()
    {
    }

    /**
     * Creates a new MailData object.
     *
     * @param   iContent  The XML definition.
     * @param   xmi       The XPathMetaInfo with the prefix 'ns' mapped to the proper namespace.
     *
     * @throws  OutboundEmailException  In case of any exceptions.
     */
    public MailData(int iContent, XPathMetaInfo xmi)
             throws OutboundEmailException
    {
        // Get the optional content type.
        m_sContentType = XPathHelper.getStringValue(iContent, "./ns:" + TAG_CONTENT_TYPE, xmi, "");

        if (!StringUtil.isSet(m_sContentType))
        {
            throw new OutboundEmailException(OutboundEmailExceptionMessages.OEE_THE_CONTENT_TYPE_MUST_BE_SET_FOR_MAIL_DATA);
        }

        // Get the optional content disposition.
        m_sContentDisposition = XPathHelper.getStringValue(iContent,
                                                           "./ns:" + TAG_CONTENT_DISPOSITION, xmi,
                                                           null);

        // Get the source type.
        String sDataSourceType = XPathHelper.getStringValue(iContent, "./ns:" + TAG_SOURCE, xmi,
                                                            "");

        if (!StringUtil.isSet(sDataSourceType))
        {
            throw new OutboundEmailException(OutboundEmailExceptionMessages.OEE_THE_TAG_SOURCE_MUST_BE_SET_FOR_THE_MAIL_DATA);
        }
        m_dstDataType = EDataSourceType.valueOf(sDataSourceType);

        int iDetail = XPathHelper.selectSingleNode(iContent, "./ns:" + TAG_DETAIL, xmi);

        if (iDetail == 0)
        {
            throw new OutboundEmailException(OutboundEmailExceptionMessages.OEE_THE_TAG_DETAIL_MUST_BE_SPECIFIED_FOR_THE_MAIL_DATA);
        }

        String sData = Node.getDataWithDefault(iDetail, "");

        // Because of the S/MIME specification newlines MUST me <CR><LF>. If we don't do this
        // Outlook, Outlook Express and Windows Live Mail cannot verify the signature properly.
        switch (m_dstDataType)
        {
            case XML:
                sData = writeData(iDetail);

            case PLAIN:
                sData = sData.replaceAll("(?<!\r)\n", "\r\n");
                m_baData = sData.getBytes();
                break;

            case BASE64:

                byte[] baBASE64Source = sData.getBytes();
                m_baData = Native.decodeBinBase64(baBASE64Source, baBASE64Source.length);
                break;

            case LOCAL_FILE_BIN:
                m_baData = loadFile(sData, true);
                break;

            case LOCAL_FILE_TEXT:
                m_baData = loadFile(sData, false);
                break;
        }
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.IMailData#getContentDisposition()
     */
    @Override public String getContentDisposition()
    {
        return m_sContentDisposition;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.IMailData#getContentType()
     */
    @Override public String getContentType()
    {
        return m_sContentType;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.IMailData#getData()
     */
    @Override public byte[] getData()
    {
        return m_baData;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.IMailData#getDataSource()
     */
    @Override public EDataSourceType getDataSource()
    {
        return m_dstDataType;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.outbound.IMailData#getDataStream()
     */
    @Override public InputStream getDataStream()
    {
        return new ByteArrayInputStream(getData());
    }

    /**
     * This method sets the content disposition.
     *
     * @param  sContentDisposition  The disposition to set.
     */
    public void setContentDisposition(String sContentDisposition)
    {
        m_sContentDisposition = sContentDisposition;
    }

    /**
     * This method sets the content type.
     *
     * @param  sContentType  The new content type.
     */
    public void setContentType(String sContentType)
    {
        m_sContentType = sContentType;
    }

    /**
     * This method sets the actual data.
     *
     * @param  baData  The data to set.
     */
    public void setData(byte[] baData)
    {
        m_baData = baData;
    }

    /**
     * This method sets the data type for this piece of data.
     *
     * @param  dstDataType  The data type.
     */
    public void setDataType(EDataSourceType dstDataType)
    {
        m_dstDataType = dstDataType;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.config.IXMLSerializable#toXML(int)
     */
    @Override public int toXML(int iParent)
    {
        int iReturn = Node.createElementWithParentNS(IMultiPart.TAG_DATA, null, iParent);

        // Do the content type.
        if (StringUtil.isSet(m_sContentType))
        {
            Node.createElementWithParentNS(TAG_CONTENT_TYPE, m_sContentType, iReturn);
        }

        if (StringUtil.isSet(m_sContentDisposition))
        {
            Node.createElementWithParentNS(TAG_CONTENT_DISPOSITION, m_sContentDisposition, iReturn);
        }

        // TODO: Do the data. The data is a bit more complicated, because the original source XML is
        // not stored. So based on the current data we need to write it to XML.
        Node.createElementWithParentNS(TAG_SOURCE, m_dstDataType.name(), iReturn);
        Node.createElementWithParentNS(TAG_DETAIL, "", iReturn);

        return iReturn;
    }

    /**
     * This method loads the local file and encodes it to a BASE64 stream.
     *
     * @param   sFilename  The name of the file.
     * @param   bBinary    Whether or not the data should considered as binary. If false the
     *                     character encoding will be converted to UTF-8.
     *
     * @return  The data of the file.
     *
     * @throws  OutboundEmailException  In case of any exceptions.
     */
    private byte[] loadFile(String sFilename, boolean bBinary)
                     throws OutboundEmailException
    {
        byte[] baReturn = null;

        File fFile = new File(sFilename);

        if (!fFile.exists())
        {
            throw new OutboundEmailException(OutboundEmailExceptionMessages.OEE_COULD_NOT_FIND_FILE_0,
                                             fFile.getAbsolutePath());
        }

        if (!fFile.canRead())
        {
            throw new OutboundEmailException(OutboundEmailExceptionMessages.OEE_COULD_NOT_READ_FILE_0,
                                             fFile.getAbsolutePath());
        }

        try
        {
            if (bBinary)
            {
                baReturn = FileUtils.readFileContents(fFile);
            }
            else
            {
                String sTemp = FileUtils.readTextFileContents(fFile);

                if (StringUtil.isSet(sTemp))
                {
                    // For text is means that is MUST be \r\n. So we'll have to replace them
                    sTemp = sTemp.replaceAll("(?<!\r)\n", "\r\n");
                    baReturn = sTemp.getBytes("UTF-8");
                }
                else
                {
                    baReturn = new byte[0];
                }
            }
        }
        catch (IOException e)
        {
            throw new OutboundEmailException(e,
                                             OutboundEmailExceptionMessages.OEE_ERROR_READING_FILE_0,
                                             fFile.getAbsolutePath());
        }

        return baReturn;
    }

    /**
     * This method writes the XML data to a string so that it can be incorporated.
     *
     * <p>Note that there could be multiple children for the data like processing instructions.</p>
     *
     * @param   iData  The data XML.
     *
     * @return  The data written to string.
     */
    private String writeData(int iData)
    {
        StringBuilder sbReturn = new StringBuilder(1024);

        int iCurrent = Node.getFirstChild(iData);

        while (iCurrent != 0)
        {
            sbReturn.append(Node.writeToString(iCurrent, false));
            iCurrent = Node.getNextSibling(iCurrent);
        }

        return sbReturn.toString();
    }
}
