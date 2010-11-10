package com.cordys.coe.ac.emailio.triggerengine.tcb;

import com.cordys.coe.ac.emailio.config.EEmailSection;
import com.cordys.coe.ac.emailio.config.pattern.IPattern;
import com.cordys.coe.ac.emailio.config.rule.IRule;
import com.cordys.coe.ac.emailio.config.rule.IRuleContext;
import com.cordys.coe.ac.emailio.config.trigger.ITrigger;
import com.cordys.coe.ac.emailio.exception.EmailIOException;
import com.cordys.coe.ac.emailio.exception.TriggerEngineException;
import com.cordys.coe.ac.emailio.localization.TriggerEngineExceptionMessages;
import com.cordys.coe.ac.emailio.monitor.RuleContext;
import com.cordys.coe.ac.emailio.outbound.ISMIMEConfiguration;
import com.cordys.coe.ac.emailio.util.smime.SMIMEUtil;
import com.cordys.coe.util.FileUtils;

import com.eibus.util.logger.CordysLogger;

import java.io.InputStream;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.Message;

import javax.mail.Message.RecipientType;

import javax.mail.MessagingException;
import javax.mail.Multipart;

import javax.mail.internet.MimeMessage;

/**
 * This class will take a trigger definition and a set of messages and it will determine whether or
 * not the message matches the trigger definition.
 *
 * @author  pgussow
 */
class TriggerContextBuilder
    implements ITriggerContextBuilder
{
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(TriggerContextBuilder.class);
    /**
     * Holds the actual email message.
     */
    private Message m_mMessage;
    /**
     * Holds the rule context for this message.
     */
    private RuleContext m_rcContext;
    /**
     * Holds the definition of the trigger.
     */
    private ITrigger m_tTrigger;

    /**
     * This method initializes the context builder.
     *
     * <p>If a mail is encrypted the mail will be decrypted first. If the mail is signed the
     * signature will be validated. The rules will be fired on the decrypted message.</p>
     *
     * @param   rcContext  The rule context to add the information to for this message.
     * @param   mMessage   The actual email message.
     * @param   tTrigger   The definition of the trigger.
     * @param   scConfig   The S/MIME configuration.
     *
     * @throws  TriggerEngineException  In case of any exceptions.
     *
     * @see     ITriggerContextBuilder#initialize(RuleContext, Message, ITrigger,
     *          ISMIMEConfiguration)
     */
    @Override public void initialize(RuleContext rcContext, Message mMessage, ITrigger tTrigger,
                                     ISMIMEConfiguration scConfig)
                              throws TriggerEngineException
    {
        m_rcContext = rcContext;
        m_tTrigger = tTrigger;

        try
        {
            MimeMessage mmFinal = (MimeMessage) mMessage;

            // Check if the mail is encrypted. If it is, decrypt it
            if (SMIMEUtil.isEncryped((MimeMessage) mMessage))
            {
                mmFinal = SMIMEUtil.decryptMessage(mmFinal, scConfig);
            }

            if (SMIMEUtil.isSigned(mmFinal))
            {
                SMIMEUtil.validateSignature(mmFinal);
            }

            // The decrypted message will be used for rule processing.
            m_mMessage = mmFinal;
        }
        catch (EmailIOException e)
        {
            throw new TriggerEngineException(e.getMessageObject(), e.getMessageParameters());
        }
    }

    /**
     * This method actually processes all the rules that are defined for this trigger. It will walk
     * through all defined rules and patterns and evalutate them. The result of that evaluation it
     * will store in the RuleContext object. If this message actually matches the patterns as
     * described it will return true.
     *
     * @return  true if the message will be processed. Otherwise false.
     *
     * @throws  TriggerEngineException  In case of any exceptions.
     */
    public boolean processMessage()
                           throws TriggerEngineException
    {
        boolean bReturn = true;

        IRule[] arRules = m_tTrigger.getRules();

        for (int iCount = 0; iCount < arRules.length; iCount++)
        {
            IRule rRule = arRules[iCount];

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Processing rule:\n" + rRule);
            }

            boolean bMatch = evaluateRule(m_rcContext, m_mMessage, rRule);

            if (bMatch == false)
            {
                // ALL patterns have to match.
                bReturn = false;
                break;
            }
        }

        // If the message is matched we will add the full message object to the rule context.
        if (bReturn == true)
        {
            m_rcContext.putValue(RuleContext.SYS_MESSAGE_OBJECT, m_mMessage);
        }

        return bReturn;
    }

    /**
     * This method handles the patterns on the header section. Basically only patterns of type
     * "header" are allowed in this section.
     *
     * @param   rcContext  The rule context.
     * @param   mMessage   The current message.
     * @param   rRule      The actual rule to execute.
     *
     * @return  true if the field matches the given pattern. Otherwise false.
     *
     * @throws  TriggerEngineException  In case of any exception.
     */
    private boolean evaluateHeaderRule(IRuleContext rcContext, Message mMessage,
                                       IRule rRule)
                                throws TriggerEngineException
    {
        boolean bReturn = true;

        try
        {
            Enumeration<?> oTemp = mMessage.getAllHeaders();
            HashMap<String, String> hmHeaders = new LinkedHashMap<String, String>();

            while (oTemp.hasMoreElements())
            {
                Header hHeader = (Header) oTemp.nextElement();
                hmHeaders.put(hHeader.getName(), hHeader.getValue());
            }

            // Now we have the value on which we can work.
            IPattern[] apPatterns = rRule.getPatterns();

            for (int iCount = 0; iCount < apPatterns.length; iCount++)
            {
                IPattern pPattern = apPatterns[iCount];

                if (pPattern.evaluate(rcContext, hmHeaders, rRule) == false)
                {
                    bReturn = false;
                    break;
                }
            }
        }
        catch (MessagingException me)
        {
            throw new TriggerEngineException(me,
                                             TriggerEngineExceptionMessages.TEEERROR_GETTING_THE_VALUE_FOR_SECTION,
                                             rRule.getSection());
        }

        return bReturn;
    }

    /**
     * This is the most complex handling of the trigger. This part will handle rules that are bound
     * to the multipart of a message. NOTE: This has to be recursive, because you never know which
     * body part matches the pattern defined.
     *
     * @param   rcContext  The rule context.
     * @param   mMessage   The current message.
     * @param   rRule      The actual rule to execute.
     *
     * @return  true if the field matches the given pattern. Otherwise false.
     *
     * @throws  TriggerEngineException  In case of any exceptions.
     */
    private boolean evaluateMultipartRule(IRuleContext rcContext, Message mMessage,
                                          IRule rRule)
                                   throws TriggerEngineException
    {
        boolean bReturn = true;

        // There are a couple of options:
        // 1. The mMessage.getContent() returns a string.
        // 2. mMessage.getContent() returns a multipart
        // 3. mMessage.getContent() returns a nested message.
        // 4. mMessage might be a MimePart
        try
        {
            String sContentType = mMessage.getContentType();

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Content type for the message is: " + sContentType);
            }

            // When you have a message the content is either a multipart or normal data. Multipart
            // rules will also be evaluated against the normal messages since it's also possible to
            // have normal data.
            Object oContent = mMessage.getContent();

            // If the content is a stream we'll read the full stream and assume that
            // the file is a text file.
            if (oContent instanceof InputStream)
            {
                byte[] baContent = FileUtils.readStreamContents((InputStream) oContent);
                oContent = new String(baContent);

                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Content was a stream. The content of the stream was:\n" + oContent);
                }
            }

            if (oContent instanceof String)
            {
                String sContent = (String) oContent;

                // Now we have the value on which we can work.
                IPattern[] apPatterns = rRule.getPatterns();

                // Create the hashmap that will facilitate all headers. For performance
                // reasons it will only be filled when an actual header-pattern was found.
                LinkedHashMap<String, String> lhmHeader = new LinkedHashMap<String, String>();

                for (int iCount = 0; iCount < apPatterns.length; iCount++)
                {
                    IPattern pPattern = apPatterns[iCount];

                    boolean bPatternOK = true;

                    if (pPattern.worksOnHeader())
                    {
                        // Fill the headers if they are not filled yet.
                        if (lhmHeader.size() == 0)
                        {
                            Enumeration<?> oTemp = mMessage.getAllHeaders();

                            while (oTemp.hasMoreElements())
                            {
                                Header hHeader = (Header) oTemp.nextElement();
                                lhmHeader.put(hHeader.getName(), hHeader.getValue());
                            }
                        }

                        bPatternOK = pPattern.evaluate(rcContext, lhmHeader, rRule);
                    }
                    else
                    {
                        bPatternOK = pPattern.evaluate(rcContext, sContent, rRule);
                    }

                    if (bPatternOK == false)
                    {
                        bReturn = false;
                        break;
                    }
                }
            }
            else if (oContent instanceof Multipart)
            {
                Multipart mpContent = (Multipart) oContent;
                bReturn = parseAndEvaluateMultipart(rcContext, mpContent, rRule);
            }
            else if (oContent instanceof Message)
            {
                Message mContent = (Message) oContent;
                bReturn = evaluateMultipartRule(rcContext, mContent, rRule);
            }
            else
            {
                throw new TriggerEngineException(TriggerEngineExceptionMessages.TEE_UNSUPPORTED_EMAIL_CONTENT,
                                                 oContent.getClass().getName());
            }
        }
        catch (TriggerEngineException tee)
        {
            throw tee;
        }
        catch (Exception e)
        {
            throw new TriggerEngineException(e,
                                             TriggerEngineExceptionMessages.TEE_ERROR_GETTING_THE_VALUE_FOR_SECTION,
                                             rRule.getSection());
        }

        return bReturn;
    }

    /**
     * This method evaluates the rule. A rule is defined on a certain section. All sections are
     * pretty straight forward. The only special section is the multipart, since that can also be a
     * nested structure. So there we need to go recursive.
     *
     * @param   rcContext  The current context of the rules.
     * @param   mMessage   The actual email message.
     * @param   rRule      The definition of the rule.
     *
     * @return  true if the current message matches the rule. Otherwise false.
     *
     * @throws  TriggerEngineException  In case of any exceptions.
     */
    private boolean evaluateRule(RuleContext rcContext, Message mMessage, IRule rRule)
                          throws TriggerEngineException
    {
        boolean bReturn = false;

        switch (rRule.getSection())
        {
            case CC:
            case FROM:
            case TO:
            case SUBJECT:
                bReturn = evaluateSimpleRule(rcContext, mMessage, rRule);
                break;

            case HEADER:
                bReturn = evaluateHeaderRule(rcContext, mMessage, rRule);
                break;

            case MULTIPART:
                bReturn = evaluateMultipartRule(rcContext, mMessage, rRule);
                break;
        }

        return bReturn;
    }

    /**
     * This handles a simple rule. A simple rule is basically only reg ex rules on a string value.
     * This is used for the sections CC, FROM, TO and SUBJECT.
     *
     * @param   rcContext  The rule context.
     * @param   mMessage   The current message.
     * @param   rRule      The actual rule to execute.
     *
     * @return  true if the field matches the given pattern. Otherwise false.
     *
     * @throws  TriggerEngineException  In case of any exceptions.
     */
    private boolean evaluateSimpleRule(RuleContext rcContext, Message mMessage,
                                       IRule rRule)
                                throws TriggerEngineException
    {
        boolean bReturn = true;

        // First get the value on which the rule needs to be evaluated.
        String sValue = "";

        try
        {
            if (rRule.getSection() == EEmailSection.SUBJECT)
            {
                sValue = mMessage.getSubject();

                // It could be that the mail has no subject and thus this value *could* be null.
                // If we'd return null then a regex would fail because of a null pointer. So that's
                // why in case of NULL the subject is returned as an empty string.
                if (sValue == null)
                {
                    sValue = "";
                }
            }
            else
            {
                StringBuffer sbValue = new StringBuffer(512);
                Address[] aTemp = null;

                switch (rRule.getSection())
                {
                    case TO:
                        aTemp = mMessage.getRecipients(RecipientType.TO);
                        break;

                    case CC:
                        aTemp = mMessage.getRecipients(RecipientType.CC);
                        break;

                    case FROM:
                        aTemp = mMessage.getFrom();
                        break;

                    default:
                        throw new TriggerEngineException(TriggerEngineExceptionMessages.TEE_INVALID_SIMPLE_TYPE);
                }

                for (int iCount = 0; iCount < aTemp.length; iCount++)
                {
                    sbValue.append(aTemp[iCount].toString());

                    if (iCount < (aTemp.length - 1))
                    {
                        sbValue.append(";");
                    }
                }

                sValue = sbValue.toString();
            }
        }
        catch (MessagingException me)
        {
            throw new TriggerEngineException(me,
                                             TriggerEngineExceptionMessages.TEE_ERROR_GETTING_THE_VALUE_FOR_SECTION,
                                             rRule.getSection());
        }

        // Now we have the value on which we can work.
        IPattern[] apPatterns = rRule.getPatterns();

        for (int iCount = 0; iCount < apPatterns.length; iCount++)
        {
            IPattern pPattern = apPatterns[iCount];

            if (pPattern.evaluate(rcContext, sValue, rRule) == false)
            {
                bReturn = false;
                break;
            }
        }

        return bReturn;
    }

    /**
     * This method does the actual parsing of the multipart message. This method is called
     * recursively for each and every multipart that is encounterd. For a multipart we can have
     * header, xpath or regex patterns. A multipart has it's own set of headers, so that's what
     * makes this special. Also a multipart can contain a multipart.
     *
     * @param   rcContext  The pattern context.
     * @param   mpContent  The multipart to handle.
     * @param   rRule      The rule to evaluate.
     *
     * @return  true if this multipart matches the given pattern.
     *
     * @throws  TriggerEngineException  In case of any exceptions.
     */
    private boolean parseAndEvaluateMultipart(IRuleContext rcContext, Multipart mpContent,
                                              IRule rRule)
                                       throws TriggerEngineException
    {
        boolean bReturn = true;

        // There are a couple of options:
        // 1. The mMessage.getContent() returns a string.
        // 2. mMessage.getContent() returns a multipart
        // 3. mMessage.getContent() returns a nested message.
        try
        {
            for (int iCount = 0; iCount < mpContent.getCount(); iCount++)
            {
                bReturn = true;

                BodyPart bpPart = mpContent.getBodyPart(iCount);

                // Get the headers for this body part.
                Enumeration<?> oTemp = bpPart.getAllHeaders();
                HashMap<String, String> hmHeaders = new LinkedHashMap<String, String>();

                while (oTemp.hasMoreElements())
                {
                    Header hHeader = (Header) oTemp.nextElement();
                    hmHeaders.put(hHeader.getName(), hHeader.getValue());
                }

                Object oContent = bpPart.getContent();

                // If the content is a multipart we need to go recursive again.
                if (oContent instanceof Multipart)
                {
                    Multipart mpNestedContent = (Multipart) oContent;
                    bReturn = parseAndEvaluateMultipart(rcContext, mpNestedContent, rRule);
                }
                else
                {
                    // Now we have the value on which we can work.
                    IPattern[] apPatterns = rRule.getPatterns();

                    for (int iPatternCount = 0; iPatternCount < apPatterns.length; iPatternCount++)
                    {
                        IPattern pPattern = apPatterns[iPatternCount];

                        if (pPattern.worksOnHeader())
                        {
                            bReturn = pPattern.evaluate(rcContext, hmHeaders, rRule);

                            if (bReturn == false)
                            {
                                break;
                            }
                        }
                        else
                        {
                            // If the content is a stream we'll read the full stream and assume that
                            // the file is a text file.
                            if (oContent instanceof InputStream)
                            {
                                byte[] baContent = FileUtils.readStreamContents((InputStream)
                                                                                oContent);
                                oContent = new String(baContent);

                                if (LOG.isDebugEnabled())
                                {
                                    LOG.debug("Content was a stream. The content of the stream was:\n" +
                                              oContent);
                                }
                            }

                            // It works on the body content.
                            if (oContent instanceof String)
                            {
                                String sContent = (String) oContent;
                                bReturn = pPattern.evaluate(rcContext, sContent, rRule);

                                if (bReturn == false)
                                {
                                    break;
                                }
                            }
                            else if (oContent instanceof Multipart)
                            {
                                Multipart mpNestedContent = (Multipart) oContent;
                                bReturn = parseAndEvaluateMultipart(rcContext, mpNestedContent,
                                                                    rRule);
                            }
                            else if (oContent instanceof Message)
                            {
                                Message mContent = (Message) oContent;
                                bReturn = evaluateMultipartRule(rcContext, mContent, rRule);
                            }
                            else
                            {
                                throw new TriggerEngineException(TriggerEngineExceptionMessages.TEE_UNSUPPORTED_EMAIL_CONTENT,
                                                                 oContent.getClass().getName());
                            }
                        }
                    }
                }

                // The multiparts work a little different then the normal sections. There needs to
                // be just 1 multipart that matches the patterns. Not all multiparts need to match
                // all patterns. So as soon as a multipart is found that matches the pattern this
                // method will return.
                if (bReturn == true)
                {
                    break;
                }
            }
        }
        catch (TriggerEngineException tee)
        {
            throw tee;
        }
        catch (Exception e)
        {
            throw new TriggerEngineException(e,
                                             TriggerEngineExceptionMessages.TEE_ERROR_GETTING_THE_VALUE_FOR_SECTION,
                                             rRule.getSection());
        }

        return bReturn;
    }
}
