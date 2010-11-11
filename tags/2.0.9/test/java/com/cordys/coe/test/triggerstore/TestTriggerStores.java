
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
package com.cordys.coe.test.triggerstore;

import com.cordys.coe.ac.emailio.EmailIOConnectorConstants;
import com.cordys.coe.ac.emailio.config.trigger.ITrigger;
import com.cordys.coe.ac.emailio.config.trigger.TriggerFactory;
import com.cordys.coe.ac.emailio.storage.AbstractStorageProvider;
import com.cordys.coe.ac.emailio.storage.IEmailStorageProvider;
import com.cordys.coe.util.FileUtils;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

import java.util.Map;

/**
 * DOCUMENTME.
 *
 * @author  $author$
 */
public class TestTriggerStores
{
    /**
     * NOM Document.
     */
    private static final Document DOC = new Document();

    /**
     * DOCUMENTME.
     *
     * @param   args  DOCUMENTME
     *
     * @throws  Throwable  DOCUMENTME
     */
    public static void main(String[] args)
                     throws Throwable
    {
        // ========================================================================
        // Make sure that the config in config_filestore.xml points to a directory
        // where you can actually write files or this won't work at all. Open that
        // dir somewhere to see how files get written and removed - it's the point
        // of this test. :)
        // ========================================================================

        int config = 0;
        int emailBoxConfigNode = 0;
        TestMailBox box = null;
        AbstractStorageProvider store = null;

        try
        {
            // test explicit config: memory store
            System.out.println("Using explicit memory store config");

            if (config > 0)
            {
                Node.delete(config);
            }
            config = DOC.load(FileUtils.readStreamContents(TestTriggerStores.class
                                                           .getResourceAsStream("config_memorystore.xml")));

            emailBoxConfigNode = Node.getFirstElement(config);

            box = new TestMailBox(emailBoxConfigNode);

            store = (AbstractStorageProvider) box.getStorageProvider();

            System.out.println("Store class: " + store.getClass().getName());

            System.out.println("Parameters:");

            for (Map.Entry<String, Object> entry : store.getParameters().entrySet())
            {
                System.out.println("  " + entry.getKey() + ": " + entry.getValue());
            }

            // ========================================================================
            // Make sure that the config in config_filestore.xml points to a directory
            // where you can actually write files or this won't work at all. Open that
            // dir somewhere to see how files get written and removed - it's the point
            // of this test. :)
            // ========================================================================

            // test explicit config: file store
            System.out.println("Using explicit file store config");

            if (config > 0)
            {
                // better delete and garbagecollect whatever one can... NOM.
                box = null;
                store = null;
                Node.delete(config);
            }
            config = DOC.load(FileUtils.readStreamContents(TestTriggerStores.class
                                                           .getResourceAsStream("config_filestore.xml")));

            emailBoxConfigNode = Node.getFirstElement(config);

            box = new TestMailBox(emailBoxConfigNode);

            store = (AbstractStorageProvider) box.getStorageProvider();

            System.out.println("Store class: " + store.getClass().getName());

            System.out.println("Parameters:");

            for (Map.Entry<String, Object> entry : store.getParameters().entrySet())
            {
                System.out.println("  " + entry.getKey() + ": " + entry.getValue());
            }

            dumpTriggers("Triggers before add:", store);

            ITrigger trigger1 = createTriggerFromResource("triggerfile_testtrigger.xml", box);
            ITrigger trigger2 = createTriggerFromResource("triggerfile_onetime.xml", box);

            store.addTrigger(trigger1, true);
            store.addTrigger(trigger2, true);

            dumpTriggers("Triggers after add:", store);

            System.out.println("### Check if trigger files for " + trigger1.getName() + " and " +
                               trigger2.getName() + " were created properly and press RETURN.");
            System.in.read();

            store.removeTrigger(trigger1.getName());
            store.removeTrigger(trigger2.getName());

            dumpTriggers("Triggers after remove:", store);

            System.out.println("### Check if trigger files for " + trigger1.getName() + " and " +
                               trigger2.getName() + " were removed properly.");

            // now let's have a look at trigger store serialization.
            System.out.println("### Serializing trigger store:");

            int tempNode = DOC.createElement("Test");
            System.out.println(Node.writeToString(store.toXML(tempNode), true));

            if (tempNode > 0)
            {
                Node.delete(tempNode);
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
     * DOCUMENTME.
     *
     * @param   resourceName  DOCUMENTME
     * @param   box           DOCUMENTME
     *
     * @return  DOCUMENTME
     */
    private static ITrigger createTriggerFromResource(String resourceName, TestMailBox box)
    {
        int docNode = 0;

        try
        {
            docNode = DOC.load(FileUtils.readStreamContents(TestTriggerStores.class
                                                            .getResourceAsStream(resourceName)));

            XPathMetaInfo xmi = new XPathMetaInfo();
            xmi.addNamespaceBinding("ns", EmailIOConnectorConstants.NS_CONFIGURATION);

            int triggerNode = XPathHelper.selectSingleNode(docNode, "./ns:trigger", xmi);

            return TriggerFactory.createTrigger(triggerNode, box);
        }
        catch (Exception ex)
        {
            throw new RuntimeException("Could not deserialize trigger: " + ex.getMessage(), ex);
        }
        finally
        {
            if (docNode > 0)
            {
                Node.delete(docNode);
            }
        }
    }

    /**
     * DOCUMENTME.
     *
     * @param  headline  DOCUMENTME
     * @param  store     DOCUMENTME
     */
    private static void dumpTriggers(String headline, IEmailStorageProvider store)
    {
        System.out.println(headline);

        for (ITrigger tigger : store.getTriggers())
        {
            System.out.println("  " +
                               (store.isTriggerPersistent(tigger.getName()) ? "Persistent"
                                                                            : "Transient ") + "/" +
                               (tigger.isOneTimeOnly() ? "Onetime" : "Durable") + ": " +
                               tigger.getName() + " (" + tigger.getDescription() + ")");
        }
    }
}
