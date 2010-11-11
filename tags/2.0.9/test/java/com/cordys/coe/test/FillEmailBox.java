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

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

/**
 * DOCUMENTME
 *
 * @author $author$
 */
public class FillEmailBox
{
    /**
     * Main method.
     *
     * @param saArguments The commandline arguments.
     */
    public static void main(String[] saArguments)
    {
        try
        {
            String host = "localhost";
            String to = "imap@cnd0986.vanenburg.com";
            String from = to;
            int iNrOfMessages = 2;

            Properties props = System.getProperties();

            //Setup mail server
            props.put("mail.smtp.host", host);

            //Get session
            Session session = Session.getDefaultInstance(props, null);

            //Define message
            for (int iCount = 1; iCount <= iNrOfMessages; iCount++)
            {
                MimeMessage message = createMessage(session, from, to,
                                                    "100" + iCount,
                                                    "200" + iCount);
                System.out.println("Sending mail for 100" + iCount + "/200" +
                                   iCount);
                Transport.send(message);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        System.out.println("All done...");
    }

    /**
     * DOCUMENTME
     *
     * @param session DOCUMENTME
     * @param from DOCUMENTME
     * @param to DOCUMENTME
     * @param sOrderID DOCUMENTME
     * @param sCustomerID DOCUMENTME
     *
     * @return DOCUMENTME
     */
    private static MimeMessage createMessage(Session session, String from,
                                             String to, String sOrderID,
                                             String sCustomerID)
                                      throws MessagingException, IOException
    {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

        message.setSubject("Order " + sOrderID + " from customer " +
                           sCustomerID);

        MimeMultipart mm = new MimeMultipart("mixed");

        //The plain text.
        MimeBodyPart mbpText = new MimeBodyPart();
        mbpText.addHeader("Content-Type", "text/plain");
        mbpText.setText("Hi!\n\nCustomer " + sCustomerID +
                        " has placed a new order.\nOrder " + sOrderID +
                        "\n\nPlease process this order.");
        mm.addBodyPart(mbpText);

        //The attachment
        MimeBodyPart mbpAttachment = new MimeBodyPart();
        mbpAttachment.setDataHandler(new DataHandler(createXMLDataSource(sOrderID,
                                                                         sCustomerID)));
        mbpAttachment.setFileName("salesorder.xml");
        mbpAttachment.setDisposition("attachment");
        mm.addBodyPart(mbpAttachment);

        message.setContent(mm);

        return message;
    }

    /**
     * DOCUMENTME
     *
     * @param sOrderID DOCUMENTME
     * @param sCustomerID DOCUMENTME
     *
     * @return DOCUMENTME
     *
     * @throws IOException
     */
    private static ByteArrayDataSource createXMLDataSource(String sOrderID,
                                                           String sCustomerID)
                                                    throws IOException
    {
        String sXML = "<?xml version=\"1.0\"?>" + "<Order><OrderID>" +
                      sOrderID + "</OrderID><CustomerID>" + sCustomerID +
                      "</CustomerID><OrderDate>" + new Date().toString() +
                      "</OrderDate><Lines>" + "<Line><Position>10</Position>" +
                      "<Item>Item A</Item></Line>" +
                      "<Line><Position>20</Position>" +
                      "<Item>Item B</Item></Line>" + "</Lines></Order>";

        return new ByteArrayDataSource(sXML, "text/xml");
    }
}
