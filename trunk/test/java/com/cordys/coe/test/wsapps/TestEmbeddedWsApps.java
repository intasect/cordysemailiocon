package com.cordys.coe.test.wsapps;

import com.cordys.cpc.bsf.busobject.BSF;
import com.cordys.cpc.bsf.busobject.BsfContext;
import com.cordys.cpc.bsf.busobject.Config;

import com.eibus.xml.nom.Document;

/**
 * DOCUMENTME .
 *
 * @author  pgussow
 */
public class TestEmbeddedWsApps
{
    /**
     * Holds the XML NOM document.
     */
    private static Document s_dDoc = new Document();

    /**
     * Main method.
     *
     * @param  saArguments  The commandline arguments.
     */
    @SuppressWarnings("unused")
    public static void main(String[] saArguments)
    {
        try
        {
            int iWsAppsConfig = s_dDoc.load(".\\test\\java\\com\\cordys\\coe\\test\\wsapps\\config.xml");
// iWsAppsConfig = s_dDoc.load(".\\test\\java\\com\\cordys\\coe\\test\\wsapps\\config2.xml");

            Config cWsAppConfig = new Config(null, "EmailIOConnectionPool", iWsAppsConfig,
                                             false);
// cWsAppConfig.setConfig(iWsAppsConfig);

            BsfContext bcContext = BSF.initBsfContext();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
