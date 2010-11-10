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
