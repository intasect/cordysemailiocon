package com.cordys.coe.test.triggerstore;

import com.cordys.coe.ac.emailio.EmailIOConnectorConstants;
import com.cordys.coe.ac.emailio.config.trigger.ITrigger;
import com.cordys.coe.ac.emailio.config.trigger.TriggerFactory;
import com.cordys.coe.util.FileUtils;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * DOCUMENTME.
 *
 * @author  $author$
 */
public class TestStuff
{
    /**
     * DOCUMENTME.
     */
    static Document DOC = new Document();

    /**
     * DOCUMENTME.
     *
     * @param   args  DOCUMENTME
     *
     * @throws  Exception  DOCUMENTME
     */
    public static void main(String[] args)
                     throws Exception
    {
        int docNode = 0;
        int config = 0;
        int emailBoxConfigNode = 0;
        TestMailBox box;

        try
        {
            if (config > 0)
            {
                Node.delete(config);
            }
            config = DOC.load(FileUtils.readStreamContents(TestTriggerStores.class
                                                           .getResourceAsStream("config_miracle.xml")));

            emailBoxConfigNode = Node.getFirstElement(config);

            box = new TestMailBox(emailBoxConfigNode);
            box.getStorageProvider();

            ITrigger trigger1 = createTriggerFromResource("triggerfile_miracle.xml", box);

            docNode = DOC.load(FileUtils.readStreamContents(TestTriggerStores.class
                                                            .getResourceAsStream("testdoc.xml")));

            int resultNode = XPathHelper.selectSingleNode(docNode, ".//bpm:ETAChoice",
                                                          trigger1.getMessage().getXPathMetaInfo());

            System.out.println("Result: " + resultNode);
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
}
