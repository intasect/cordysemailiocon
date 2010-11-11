
 /**
 * Copyright 2007 Cordys R&D B.V. 
 * 
 * This file is part of the Cordys Email IO Connector. 
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
package com.cordys.coe.test;

import java.util.Locale;

import javax.mail.Message;

import com.cordys.coe.ac.emailio.config.IEmailBox;
import com.cordys.coe.ac.emailio.config.POP3EmailBox;
import com.cordys.coe.ac.emailio.connection.EmailConnectionFactory;
import com.cordys.coe.ac.emailio.connection.IEmailConnection;
import com.cordys.coe.exception.ServerLocalizableException;
import com.cordys.coe.util.FileUtils;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.util.logger.config.LoggerConfigurator;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;

/**
 * This class dumps all messages in the inbox.
 *
 * @author  pgussow
 */
public class DumpMessages
{
    /**
     * Holds the logger that is used.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(DumpMessages.class);
    /**
     * NOM Document.
     */
    private static final Document DOC = new Document();

    /**
     * Main method.
     *
     * @param  saArguments  The commandline arguments.
     */
    public static void main(String[] saArguments)
    {
        try
        {
            int config = 0;
            System.setProperty("CORDYS_INSTALL_DIR", "./src/content");

            try
            {
                LoggerConfigurator.initLogger("./test/Log4jConfiguration.xml");

                config = DOC.load(FileUtils.readStreamContents(TestPOP3.class.getResourceAsStream("DumpMessages_config.xml")));

                int emailBoxConfigNode = Node.getFirstElement(config);

                IEmailBox e = new POP3EmailBox(emailBoxConfigNode, 0, "", true);
                
                IEmailConnection ec = EmailConnectionFactory.createConnection(e);
                Message[] am = ec.getEmailHeaders();

                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Total number of messages: " + am.length);
                }

                for (int iCount = 0; iCount < am.length; iCount++)
                {
                    Message msg = am[iCount];

                    if (LOG.isEnabled(Severity.DEBUG))
                    {
                        LOG.log(Severity.DEBUG, "===============================");
                        LOG.log(Severity.DEBUG, "= Message " + (iCount + 1));
                        LOG.log(Severity.DEBUG, "===============================");

                        LOG.log(Severity.DEBUG,
                                "From: " + msg.getFrom()[0].toString() + ", Subject: " +
                                msg.getSubject());
                    }
                }
            }
            finally
            {
                if (config > 0)
                {
                    Node.delete(config);
                }
            }
        }
        catch (ServerLocalizableException sle)
        {
            sle.setLocale(Locale.getDefault());
            sle.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
