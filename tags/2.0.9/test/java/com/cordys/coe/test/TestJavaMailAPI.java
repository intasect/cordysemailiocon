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

import java.io.FileInputStream;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Session;

import javax.mail.internet.MimeMessage;

/**
 * DOCUMENTME.
 *
 * @author  $author$
 */
public class TestJavaMailAPI
{
    /**
     * Main method.
     *
     * @param  saArguments  Commandline arguments.
     */
    public static void main(String[] saArguments)
    {
        try
        {
            String m_sHost = "srv-nl-ces70";
            int m_iPort = 25;
            String sEmailFile = "D:/temp/test.eml";

            // Build up the properties
            Properties pSMTP = new Properties();
            pSMTP.put("mail.smtp.host", m_sHost);
            pSMTP.put("mail.smtp.port", String.valueOf(m_iPort));

            Authenticator aAuth = null;

            // Create the session
            Session sSession = Session.getInstance(pSMTP, aAuth);

            MimeMessage mmNew = new MimeMessage(sSession, new FileInputStream(sEmailFile));

            System.out.println(mmNew.getSubject());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
