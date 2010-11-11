
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
