package com.cordys.coe.ac.emailio.sample;

import com.cordys.coe.ac.emailio.config.message.ICustomMapping;
import com.cordys.coe.ac.emailio.config.message.IMapping;
import com.cordys.coe.ac.emailio.config.message.IMessage;
import com.cordys.coe.ac.emailio.config.rule.IRuleContext;
import com.cordys.coe.ac.emailio.exception.TriggerEngineException;
import com.cordys.coe.ac.emailio.localization.TriggerEngineExceptionMessages;
import com.cordys.coe.util.CollectionUtils;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.xml.nom.Node;

import java.io.IOException;

import java.util.Set;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;

import javax.mail.internet.ContentType;

/**
 * Writes the email body as a string. If the email contains a text/plan part, it is used, otherwise
 * all the parts are concatenated together.
 *
 * @author  mpoyhone
 */
public class MailBodyAsStringMapping
    implements ICustomMapping
{
    /**
     * Contains mime types which specify a text content.
     */
    private static Set<String> textMimeTypes = CollectionUtils.arrayToHashSet("text/plain");

    /**
     * This method executes the mapping.
     *
     * @param   context      The rule context to get the values from.
     * @param   contextNode  The context node.
     * @param   message      The parent message.
     * @param   mapping      The mapping definition.
     *
     * @throws  TriggerEngineException  In case of any exceptions.
     *
     * @see     com.cordys.coe.ac.emailio.config.message.ICustomMapping#execute(com.cordys.coe.ac.emailio.config.rule.IRuleContext,
     *          int, com.cordys.coe.ac.emailio.config.message.IMessage,
     *          com.cordys.coe.ac.emailio.config.message.IMapping)
     */
    public void execute(IRuleContext context, int contextNode, IMessage message,
                        IMapping mapping)
                 throws TriggerEngineException
    {
        // Find the tag where we will create the mapping
        int destNode = XPathHelper.selectSingleNode(contextNode, mapping.getSourceXPath(),
                                                    message.getXPathMetaInfo());

        if (destNode == 0)
        {
            throw new TriggerEngineException(TriggerEngineExceptionMessages.TEE_COULD_NOT_FIND_THE_XPATH_IN_THE_GIVEN_XML,
                                             mapping.getSourceXPath(),
                                             Node.writeToString(contextNode, false));
        }

        try
        {
            Message mail = (Message) context.getValue(IRuleContext.SYS_MESSAGE_OBJECT);
            Part textPart = findPlainTextPart(mail);
            String text = null;

            if (textPart != null)
            {
                Object content = textPart.getContent();

                if (content != null)
                {
                    text = content.toString();
                }
            }

            if (text == null)
            {
                StringBuilder buf = new StringBuilder(4096);

                mergeTextParts(mail, buf);

                text = buf.toString();
            }

            if (text == null)
            {
                text = "";
            }

            Node.getDocument(destNode).createText(text, destNode);
        }
        catch (Exception e)
        {
            throw new TriggerEngineException(e,
                                             TriggerEngineExceptionMessages.TEE_ERROR_EXECUTING_PROPER_MAPPINGS);
        }
    }

    /**
     * Tries to find a part with mime type "plain/text".
     *
     * @param   current  Current part.
     *
     * @return  Text part or <code>null</code> if none was found.
     *
     * @throws  MessagingException  Thrown if the operation failed.
     * @throws  IOException         Thrown if the operation failed.
     */
    private Part findPlainTextPart(Part current)
                            throws MessagingException, IOException
    {
        String contentTypeStr = current.getContentType();

        if (contentTypeStr == null)
        {
            return null;
        }

        ContentType contentType = new ContentType(contentTypeStr);

        // First handle recursively any sub-parts.
        if ("multipart".equals(contentType.getPrimaryType()))
        {
            Object content = current.getContent();

            if (content != null)
            {
                if (content instanceof Multipart)
                {
                    Multipart multiPart = (Multipart) content;

                    for (int i = 0, count = multiPart.getCount(); i < count; i++)
                    {
                        BodyPart childPart = multiPart.getBodyPart(i);

                        if (childPart != null)
                        {
                            Part res = findPlainTextPart(childPart);

                            if (res != null)
                            {
                                return res;
                            }
                        }
                    }
                }
                else if (content instanceof Message)
                {
                    Part res = findPlainTextPart((Message) content);

                    if (res != null)
                    {
                        return res;
                    }
                }
            }

            return null;
        }

        // For normal parts, check if the message type is a specified text mime type.
        if (textMimeTypes.contains(contentType.getBaseType()))
        {
            return current;
        }

        return null;
    }

    /**
     * Adds all parts to the given buffer.
     *
     * @param   current  Current part.
     * @param   buffer   Text parts are added to this buffer.
     *
     * @throws  MessagingException  Thrown if the operation failed.
     * @throws  IOException         Thrown if the operation failed.
     */
    private void mergeTextParts(Part current, StringBuilder buffer)
                         throws MessagingException, IOException
    {
        Object content = current.getContent();

        if (content != null)
        {
            if (content instanceof Multipart)
            {
                Multipart multiPart = (Multipart) content;

                for (int i = 0, count = multiPart.getCount(); i < count; i++)
                {
                    BodyPart childPart = multiPart.getBodyPart(i);

                    if (childPart != null)
                    {
                        mergeTextParts(childPart, buffer);
                    }
                }
            }
            else if (content instanceof Message)
            {
                mergeTextParts((Message) content, buffer);
            }
            else
            {
                String contentTypeStr = current.getContentType();

                if (contentTypeStr != null)
                {
                    ContentType contentType = new ContentType(contentTypeStr);

                    if ("text".equals(contentType.getPrimaryType()))
                    {
                        // Only add text content types.
                        if (buffer.length() > 0)
                        {
                            buffer.append("\n");
                        }

                        buffer.append(content);
                    }
                }
            }
        }
    }
}
