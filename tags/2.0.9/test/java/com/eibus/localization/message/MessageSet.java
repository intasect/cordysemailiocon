
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
package com.eibus.localization.message;

/**
 * @author Bert Roos, Kamalakar PN
 *
 */

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.cordys.basicutil.util.CommonObjects;
import com.eibus.util.logger.CordysLogger;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;

/**
 * DOCUMENTME.
 *
 * @author  $author$
 */
public final class MessageSet
{
    /**
     * Holds the logger to use.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(MessageSet.class);
    /**
     * DOCUMENTME.
     */
    private static final Map<String, MessageSet> s_messageContexts = new HashMap<String, MessageSet>();
    /**
     * DOCUMENTME.
     */
    private static final Locale s_defaultLocale = Locale.US;

    /**
     * DOCUMENTME.
     */
    private static final String s_i18nPath = "./src/content/localization" + File.separator;
    /**
     * DOCUMENTME.
     */
    private final Map<Locale, Map<String, String>> m_localeMessageGroups = Collections.synchronizedMap(new HashMap<Locale, Map<String, String>>());
    /**
     * DOCUMENTME.
     */
    private final String m_messageContext;
    /**
     * DOCUMENTME.
     */
    private final Map<String, Message> m_messages = Collections.synchronizedMap(new HashMap<String, Message>());

    /**
     * Creates a new MessageSet object.
     *
     * @param  messageContext  DOCUMENTME
     */
    private MessageSet(String messageContext)
    {
        m_messageContext = messageContext;
        getMessageBundle(s_defaultLocale, messageContext);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Custom MessageSet");
        }
    }

    /**
     * DOCUMENTME.
     *
     * @return  Returns default locale
     */
    public static Locale get_DefaultLocale()
    {
        return s_defaultLocale;
    }

    /**
     * DOCUMENTME.
     *
     * @param   messageContext
     *
     * @return
     */
    public static synchronized MessageSet getMessageSet(String messageContext)
    {
        MessageSet messageSet = s_messageContexts.get(messageContext);

        if (messageSet == null)
        {
            messageSet = new MessageSet(messageContext);
            s_messageContexts.put(messageContext, messageSet);
        }
        return messageSet;
    }

    /**
     * DOCUMENTME.
     *
     * @return  Locale[] Array of available locales
     *
     * @author  achaitnya Determine the available locales the MessageSet
     */
    public Locale[] getAvailableLocales()
    {
        // TODO: This is not 100% clean yet.
        // Example: Alerts_int_en_uk.xml will be mis-interpreted for messagecontext 'Alerts'
        // And the actual message context for this is 'Alerts_int'
        // For now, we recommend not use '_' in message bundle id / filenames.

        FilenameFilter contextFilter = new FilenameFilter()
        {
            public final boolean accept(File dir, String name)
            {
                return (name.startsWith(m_messageContext) && name.endsWith(".xml"));
            }
        };

        ArrayList<Locale> availableLocales = new ArrayList<Locale>();
        availableLocales.add(s_defaultLocale);

        File i18nFolder = new File(s_i18nPath);
        String[] filelist = i18nFolder.list(contextFilter);

        for (int index = 0; index < filelist.length; index++)
        {
            int starting = filelist[index].indexOf(m_messageContext + '_') +
                           m_messageContext.length() + 1;

            if (starting < (m_messageContext.length() + 1))
            {
                continue;
            }

            int ending = filelist[index].lastIndexOf(".xml");
            String[] tokens = filelist[index].substring(starting, ending).split("[_]", 3);

            String language = "";
            String country = "";
            String variant = "";

            switch (tokens.length)
            {
                case 1:
                    if (tokens[0].length() == 0)
                    {
                        continue;
                    }
                    language = tokens[0];
                    break;

                case 2:
                    if ((tokens[0].length() == 0) || (tokens[1].length() == 0))
                    {
                        continue;
                    }
                    language = tokens[0];
                    country = tokens[1];
                    break;

                case 3:
                    if ((tokens[0].length() == 0) || (tokens[1].length() == 0) ||
                            (tokens[2].length() == 0))
                    {
                        continue;
                    }
                    language = tokens[0];
                    country = tokens[1];
                    variant = tokens[2];
                    break;

                default:
                    continue;
            }

            Locale locale = new Locale(language, country, variant);

            if (!availableLocales.contains(locale))
            {
                availableLocales.add(locale);
            }
        }
        return (availableLocales.toArray(new Locale[availableLocales.size()]));
    }

    /**
     * DOCUMENTME.
     *
     * @param   messageID
     *
     * @return
     */
    public Message getMessage(String messageID)
    {
        Object oObject = m_messages.get(messageID);

        if (oObject == null)
        {
            synchronized (m_messages)
            {
                if (m_messages.get(messageID) == null)
                {
                    Message oMessage = new Message(this, messageID);
                    m_messages.put(messageID, oMessage);
                    return oMessage;
                }
            }
        }
        return (Message) oObject;
    }

    /**
     * DOCUMENTME.
     *
     * @return
     */
    public String getMessageContext()
    {
        return m_messageContext;
    }

    /**
     * Lazily loads the message definitions and return the requested message text.<BR/>
     * Messages are loaded for the specific messageContext upon first usage of this method for any
     * Message object.
     *
     * @param   locale
     * @param   messageID
     *
     * @return  String Locale specific message text for the specified messageID
     */
    public String getMessageText(Locale locale, String messageID)
    {
        Map<String, String> messages = m_localeMessageGroups.get(locale);

        if (messages == null)
        {
            synchronized (m_localeMessageGroups)
            {
                if (m_localeMessageGroups.get(locale) == null)
                {
                    if (isLocaleAvialable(locale))
                    {
                        File file = getMessageBundle(locale, m_messageContext);
                        int mesgRoot = 0;

                        try
                        {
                            mesgRoot = CommonObjects._getNOMDocument().load(file.getAbsolutePath());

                            int[] textNodes = Find.match(mesgRoot, "?<Message>");
                            Map<String, String> messageTexts = new HashMap<String, String>();

                            for (int numNodes = 0; numNodes < textNodes.length; numNodes++)
                            {
                                String mesgId = Node.getAttribute(textNodes[numNodes], "id");
                                String mesgText = Node.getDataElement(textNodes[numNodes],
                                                                      "MessageText", null);

                                assert mesgId != null;
                                assert mesgText != null;

                                messageTexts.put(mesgId, mesgText);
                            }
                            messages = messageTexts;
                            m_localeMessageGroups.put(locale, messageTexts);
                        }
                        catch (XMLException xe)
                        {
                            throw new MissingMessageBundleException(file.getAbsolutePath(), xe);
                        }
                        finally
                        {
                            if (mesgRoot > 0)
                            {
                                Node.delete(mesgRoot);
                            }
                        }
                    }
                    else
                    {
                        String language = locale.getLanguage();
                        String country = locale.getCountry();
                        String variant = locale.getVariant();
                        String messageTxt = "with specified Locale ";

                        if (!"".equals(language))
                        {
                            messageTxt += "Language: " + language;
                        }

                        if (!"".equals(country))
                        {
                            messageTxt += " Country: " + country;
                        }

                        if (!"".equals(variant))
                        {
                            messageTxt += " Variant: " + variant;
                        }
                        throw new MissingMessageBundleException(messageTxt);
                    }
                }
            }
        }
        return (messages.get(messageID));
    }

    /**
     * Check whether the message bundle file for specified locale exists or not. Algorithm: Check
     * for locale specific file at &ltCORYDS_INSTALL_DIR&gt\localization folder.
     *
     * @param      locale
     * @param      messageContext
     *
     * @return     File, reference to message bundle file if exists and accessible, null otherwise
     *
     * @exception  MissingMessageBundleException  if the message bundle file does not exist
     */
    private static File getMessageBundle(Locale locale, String messageContext)
                                  throws MissingMessageBundleException
    {
        StringBuffer baseFileName = new StringBuffer(s_i18nPath).append(messageContext);

        if (!locale.equals(s_defaultLocale))
        {
            baseFileName.append("_").append(locale.toString());
        }
        baseFileName.append(".xml");

        File file = new File(baseFileName.toString());

        if (!file.exists() || !file.canRead())
        {
            throw new MissingMessageBundleException(file.getAbsolutePath());
        }
        return file;
    }

    /**
     * DOCUMENTME.
     *
     * @param   locale  DOCUMENTME
     *
     * @return  DOCUMENTME
     */
    private Boolean isLocaleAvialable(Locale locale)
    {
        Locale[] avialableLocales = getAvailableLocales();
        Boolean isLocaleFound = false;

        for (Locale availableLocale : avialableLocales)
        {
            if (availableLocale.equals(locale))
            {
                isLocaleFound = true;
                break;
            }
        }
        return isLocaleFound;
    }
}
