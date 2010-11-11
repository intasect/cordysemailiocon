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
 package com.cordys.coe.test;

import javax.mail.Message;

import com.cordys.coe.ac.emailio.config.IEmailBox;
import com.cordys.coe.ac.emailio.config.POP3EmailBox;
import com.cordys.coe.ac.emailio.connection.EmailConnectionFactory;
import com.cordys.coe.ac.emailio.connection.IEmailConnection;
import com.cordys.coe.ac.emailio.util.MailMessageUtil;
import com.cordys.coe.util.FileUtils;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.util.logger.config.LoggerConfigurator;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;

/**
 * Test classes for the POP3 inbound e-mail.
 *
 * @author  hvdvlier
 */
public class TestPOP3
{
    /**
     * NOM Document.
     */
    private static final Document DOC = new Document();
    /**
     * Contains the logger instance.
     */
    private static CordysLogger LOG = CordysLogger.getCordysLogger(TestPOP3.class);

    /**
     * Main method.
     *
     * @param  saArguments  The commandline arguments.
     */
    public static void main(String[] saArguments)
    {
        try
        {
            TestPOP3 p = new TestPOP3();
            p.setup();
            p.fetchAllMessage();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Test to fetch e-mails from a POP3 mainbox.
     *
     * @throws  Exception
     */
    public void fetchAllMessage()
                         throws Exception
    {
        int config = 0;

        try
        {
            config = DOC.load(FileUtils.readStreamContents(TestPOP3.class.getResourceAsStream("example_pop3.xml")));

            int emailBoxConfigNode = Node.getFirstElement(config);

            IEmailBox e = new POP3EmailBox(emailBoxConfigNode, 0, "", true);
            IEmailConnection ec = EmailConnectionFactory.createConnection(e);
            Message[] am = ec.getEmailHeaders();

            for (int iCount = 0; iCount < am.length; iCount++)
            {
                Message msg = am[iCount];

                if (LOG.isEnabled(Severity.DEBUG))
                {
                    LOG.log(Severity.DEBUG, "===============================");
                    LOG.log(Severity.DEBUG, "= Message " + iCount + 1);
                    LOG.log(Severity.DEBUG, "===============================");

                    LOG.log(Severity.DEBUG, MailMessageUtil.dumpMessage(msg));
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

    /**
     * Initialize the logger.
     */
    public void setup()
    {
        LoggerConfigurator.initLogger("./test/Log4jConfiguration.xml");
    }
}
