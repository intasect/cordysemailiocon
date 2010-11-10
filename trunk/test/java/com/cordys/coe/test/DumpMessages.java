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
