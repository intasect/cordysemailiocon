package com.cordys.coe.ac.emailio.util;

import com.eibus.xml.nom.Node;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains some NOM utility functions.
 *
 * @author  pgussow
 * @author  Gregor Ottmann [gregor.ottmann@skytecag.com]
 */
public class NOMUtil
{
    /**
     * The regex pattern which is used for extracting the XML declaration.
     */
    private static final String XML_DECLARATION_PATTERN = "[^<]*<\\?xml[^>]*encoding=(['\"])([^>]+)\\1[^>]*\\?>.*";
    /**
     * The charset used for creating a byte[] from an XML string when no declaration is found.
     */
    private static final String XML_DEFAULT_CHARSET = "UTF-8";

    /**
     * This method fixes the xmlns declaration on the given element.
     *
     * @param  iXMLNode  The XML element.
     */
    public static void fixNamespaceDeclaration(int iXMLNode)
    {
        String sNamespace = Node.getNamespaceURI(iXMLNode);
        String sPrefix = Node.getPrefix(iXMLNode);
        String sAttribute = "xmlns";

        if ((sPrefix != null) && (sPrefix.length() > 0))
        {
            sAttribute += ":" + sPrefix;
        }

        if (Node.getAttribute(iXMLNode, sAttribute, "").length() == 0)
        {
            // The attribute is not defined. So we'll define it now.
            Node.setAttribute(iXMLNode, sAttribute, sNamespace);
        }
    }

    /**
     * Extracts the encoding name from an XML string if it is present.
     *
     * <p>This method looks for an XML declaration in the document, especially for the encoding
     * name. If one is specified, it is returned. If there is no encoding specified in the document,
     * the default character set name (UTF-8) is returned.</p>
     *
     * @param   xmlString  The string that contains the XML document.
     *
     * @return  The encoding name from the XML declaration or "UTF-8" if none is given.
     */
    public static String getXmlEncodingName(String xmlString)
    {
        // The pattern could be statically compiled, but it's so simple that the
        // impact on the source's legibility would be greater than the performance
        // gain, so we do this locally. If one would want to actually improve
        // performance, it would be a better idea to truncate the input string to
        // its first n characters in order to avoid parsing of the actual xml data.
        Pattern p = Pattern.compile(XML_DECLARATION_PATTERN, Pattern.DOTALL);
        Matcher m = p.matcher(xmlString);

        if (m.matches())
        {
            if (m.groupCount() > 1)
            {
                return m.group(2);
            }
        }

        return XML_DEFAULT_CHARSET;
    }
}
