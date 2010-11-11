package com.cordys.coe.ac.emailio.sample;

import com.cordys.coe.ac.emailio.config.action.ICustomAction;
import com.cordys.coe.ac.emailio.config.rule.IRuleContext;
import com.cordys.coe.ac.emailio.exception.ActionException;
import com.cordys.coe.ac.emailio.localization.ActionExceptionMessages;
import com.cordys.coe.util.xml.nom.XPathHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.mail.Message;

/**
 * This demo action writes the email message to the configured folder.
 *
 * @author  pgussow
 */
public class CustomAction
    implements ICustomAction
{
    /**
     * Holds the folder to log to.
     */
    private String m_sFolder;

    /**
     * This method is called to configure the custom action with the XML.
     *
     * @param  iNode  The XML configuration.
     *
     * @see    com.cordys.coe.ac.emailio.config.action.ICustomAction#configure(int)
     */
    public void configure(int iNode)
    {
        m_sFolder = XPathHelper.getStringValue(iNode, "./folder/text()");
    }

    /**
     * This method is called to actually execute the action.
     *
     * @param   pcContext  The pattern context.
     * @param   mMessage   The actual email message for which the action should be executed.
     *
     * @throws  ActionException  In case of any exception.
     *
     * @see     com.cordys.coe.ac.emailio.config.action.ICustomAction#execute(com.cordys.coe.ac.emailio.config.rule.IRuleContext,
     *          javax.mail.Message)
     */
    public void execute(IRuleContext pcContext, Message mMessage)
                 throws ActionException
    {
        File fTemp = new File(m_sFolder, String.valueOf(System.currentTimeMillis()) + ".eml");

        FileOutputStream fos = null;

        try
        {
            fos = new FileOutputStream(fTemp, false);
        }
        catch (FileNotFoundException e1)
        {
            throw new ActionException(e1, ActionExceptionMessages.AE_ERROR_EXECUTING_CUSTOMACTION);
        }

        try
        {
            mMessage.writeTo(fos);
        }
        catch (Exception e)
        {
            throw new ActionException(e, ActionExceptionMessages.AE_ERROR_EXECUTING_CUSTOMACTION);
        }
        finally
        {
            try
            {
                fos.close();
            }
            catch (IOException e)
            {
                // Ignore it.
            }
        }
    }
}
