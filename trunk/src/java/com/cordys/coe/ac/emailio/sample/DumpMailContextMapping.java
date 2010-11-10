package com.cordys.coe.ac.emailio.sample;

import com.cordys.coe.ac.emailio.config.message.ICustomMapping;
import com.cordys.coe.ac.emailio.config.message.IMapping;
import com.cordys.coe.ac.emailio.config.message.IMessage;
import com.cordys.coe.ac.emailio.config.rule.IRuleContext;
import com.cordys.coe.ac.emailio.exception.TriggerEngineException;
import com.cordys.coe.ac.emailio.localization.TriggerEngineExceptionMessages;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.xml.nom.Node;

import java.util.Map;

import javax.mail.Message;

/**
 * This class will dump the values in the rule context to the as childs to the given location.
 *
 * @author  pgussow
 */
public class DumpMailContextMapping
    implements ICustomMapping
{
    /**
     * This method executes the mapping. It will execute the XPath on the given context node. When
     * it's found it will delete all the exeisting children of that node. Then based on the type of
     * the value in the context it will either create a text node (String value) or append an XML
     * structure (int).
     *
     * @param   rcContext     The rule context to get the values from.
     * @param   iContextNode  The context node.
     * @param   mMessage      The parent message.
     * @param   mMapping      The mapping definition.
     *
     * @throws  TriggerEngineException  In case of any exceptions.
     *
     * @see     com.cordys.coe.ac.emailio.config.message.ICustomMapping#execute(com.cordys.coe.ac.emailio.config.rule.IRuleContext,
     *          int, com.cordys.coe.ac.emailio.config.message.IMessage,
     *          com.cordys.coe.ac.emailio.config.message.IMapping)
     */
    public void execute(IRuleContext rcContext, int iContextNode, IMessage mMessage,
                        IMapping mMapping)
                 throws TriggerEngineException
    {
        // Find the tag were we will create the mapping
        int iNode = XPathHelper.selectSingleNode(iContextNode, mMapping.getSourceXPath(),
                                                 mMessage.getXPathMetaInfo());

        if (iNode == 0)
        {
            throw new TriggerEngineException(TriggerEngineExceptionMessages.TEE_COULD_NOT_FIND_THE_XPATH_IN_THE_GIVEN_XML,
                                             mMapping.getSourceXPath(),
                                             Node.writeToString(iContextNode, false));
        }

        try
        {
            Message mMail = (Message) rcContext.getValue(IRuleContext.SYS_MESSAGE_OBJECT);

            int iEmail = Node.createElementWithParentNS("email", null, iNode);
            Node.createElementWithParentNS("from", mMail.getFrom()[0].toString(), iEmail);
            Node.createElementWithParentNS("to", mMail.getAllRecipients()[0].toString(), iEmail);
            Node.createElementWithParentNS("senddate", mMail.getSentDate().toString(), iEmail);
            Node.createElementWithParentNS("subject", mMail.getSubject(), iEmail);

            // Now create the structure
            int iContext = Node.createElementWithParentNS("context", null, iNode);

            Map<String, Object> mTemp = rcContext.getAllValues();

            for (String sKey : mTemp.keySet())
            {
                Object oValue = mTemp.get(sKey);
                int iEntry = Node.createElementWithParentNS("entry", null, iContext);
                Node.createElementWithParentNS("key", sKey, iEntry);

                String sData = "";

                if (oValue instanceof Integer)
                {
                    // It's XML
                    sData = Node.writeToString((Integer) oValue, false);
                }
                else
                {
                    sData = oValue.toString();
                }

                if (sData.indexOf('\n') > -1)
                {
                    Node.createCDataElementWithParentNS("value", sData, iEntry);
                }
                else
                {
                    Node.createElementWithParentNS("value", sData, iEntry);
                }
            }
        }
        catch (Exception e)
        {
            throw new TriggerEngineException(e,
                                             TriggerEngineExceptionMessages.TEE_ERROR_EXECUTING_PROPER_MAPPINGS);
        }
    }
}
