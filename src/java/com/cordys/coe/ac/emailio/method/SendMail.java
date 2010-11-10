package com.cordys.coe.ac.emailio.method;

import com.cordys.coe.ac.emailio.config.IEmailIOConfiguration;
import com.cordys.coe.ac.emailio.config.outbound.ISendMailData;
import com.cordys.coe.ac.emailio.config.outbound.SendMailDataFactory;
import com.cordys.coe.ac.emailio.connection.ISMTPConnection;
import com.cordys.coe.ac.emailio.connection.ISMTPConnectionPool;
import com.cordys.coe.ac.emailio.exception.EmailIOException;
import com.cordys.coe.ac.emailio.outbound.EmailMessageFactory;
import com.cordys.coe.ac.emailio.util.StringUtil;

import com.eibus.soap.BodyBlock;

import com.eibus.util.logger.CordysLogger;

import java.util.List;

import javax.mail.internet.MimeMessage;

/**
 * This class handles the SendMail method for this connector. This method wraps the
 *
 * @author  pgussow
 */
public class SendMail extends BaseMethod
{
    /**
     * Holds the logger to use.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(SendMail.class);
    /**
     * Holds whether or not the method request is in compatibility data.
     */
    private boolean m_bCompatibility;

    /**
     * Creates a new SendMail object.
     *
     * @param  bbRequest   The request bodyblock.
     * @param  bbResponse  The response bodyblock.
     * @param  iecConfig   The configuration of the connector.
     */
    public SendMail(BodyBlock bbRequest, BodyBlock bbResponse, IEmailIOConfiguration iecConfig)
    {
        this(bbRequest, bbResponse, iecConfig, false);
    }

    /**
     * Creates a new SendMail object.
     *
     * @param  bbRequest       The request bodyblock.
     * @param  bbResponse      The response bodyblock.
     * @param  iecConfig       The configuration of the connector.
     * @param  bCompatibility  Whether or not the method request is in compatibility data.
     */
    public SendMail(BodyBlock bbRequest, BodyBlock bbResponse, IEmailIOConfiguration iecConfig,
                    boolean bCompatibility)
    {
        super(bbRequest, bbResponse, iecConfig);
        m_bCompatibility = bCompatibility;
    }

    /**
     * @see  com.cordys.coe.ac.emailio.method.BaseMethod#execute()
     */
    @Override public void execute()
                           throws EmailIOException
    {
        // First we need to parse the data.
        ISendMailData smd = null;

        if (m_bCompatibility)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Parsing data in compatibility mode.");
            }

            smd = SendMailDataFactory.parseSendMailDataCompatibility(getRequest().getXMLNode(),
                                                                     getConfiguration());
        }
        else
        {
            smd = SendMailDataFactory.parseSendMailData(getRequest().getXMLNode(),
                                                        getConfiguration());
        }

        ISMTPConnection scConn = null;

        try
        {
            // Get the proper connection pool.
            ISMTPConnectionPool cpPool = null;

            if (StringUtil.isSet(smd.getSendOptions().getSMTPServer()))
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Getting connection pool with name " +
                              smd.getSendOptions().getSMTPServer());
                }

                cpPool = getConfiguration().getSMTPConnectionPool(smd.getSendOptions()
                                                                  .getSMTPServer());
            }
            else
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Getting the default connection pool");
                }

                cpPool = getConfiguration().getSMTPConnectionPool();
            }

            // Wait for a free connection.
            scConn = cpPool.getConnection();

            // Do the optional signing and encryption.
            List<MimeMessage> lmMessages = EmailMessageFactory.createProperMimeMessages(smd,
                                                                                        getConfiguration(),
                                                                                        scConn
                                                                                        .getSession());

            // Actually send the message.
            scConn.sendMessage(lmMessages);

            // TODO: Build up some response if needed.

        }
        finally
        {
            if (scConn != null)
            {
                scConn.releaseConnection();
            }
        }
    }
}
