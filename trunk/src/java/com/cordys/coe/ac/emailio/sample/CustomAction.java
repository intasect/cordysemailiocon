/**
 * Copyright 2005 Cordys R&D B.V. 
 * 
 * This file is part of the Cordys SAP Connector. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 /**
 * Copyright 2005 Cordys R&D B.V. 
 * 
 * This file is part of the Cordys SAP Connector. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
