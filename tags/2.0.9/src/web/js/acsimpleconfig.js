/**
 * Indicates whether or not the page has fully loaded. Only when the page has fully loaded the
 * rowSelects are allowed to do anything.
 */
var g_bInitialized = false;
/**
 * Global XML document to be able to feed dummy data to the models.
 */
var g_xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
/**
 * Holds the lib to do the password encoding.
 */
var libPassword = null;
/**
 * Holds the currently selected key manager
 */
var g_xmlCurrentKeyManager = null;
/**
 * Holds the row object for the corresponding g_xmlCurrentKeyManager xml.
 */
var g_trCurrent = null;
/**
 * Holds the lib to do XML manipulations.
 */
var libXMLUtil = null;
/**
 * Holds the configuration namespace.
 */
var NS_CONFIGURATION = "http://emailioconnector.coe.cordys.com/2.0/configuration";
/**
 * Holds the default namespace prefix mapping for the XML.
 */
var NAMESPACE_PREFIXES = "xmlns:ns='" + NS_CONFIGURATION + "'";

/**
 * This method is called when the form is loaded. It will initialize 
 * the screen and insert a dummy XML in the current model to make sure 
 * all tags are created properly.
 *  
 * @param eventObject The event that occurred.
 */
function handleInitDone(eventObject)
{
	if (DEBUG_USERS.indexOf(system.getUser().name) > -1)
	{
		//debugger;
	}
	else
	{
		btnTestSave.hide();
	}
	
    //Create the libPassword object
    libPassword = window.document.createElement("SPAN");
    libPassword.id = "libPassword";
    
    libXMLUtil = window.document.createElement("SPAN");
    libXMLUtil.id = "libXMLUtil";
    
    //Attach the base64 library.
    application.addLibrary("/cordys/wcp/admin/library/base64encoder.htm", libPassword);
    
    //Attach the XML util lib.
    application.addLibrary("/cordys/wcp/library/util/xmlutil.htm", libXMLUtil);

	//Mandatory initialization of the applicationconnector.js
    if (parent.initialize)
    {
        parent.initialize();
        
        //In case of a new connector the XML is not filled. In that case we'll
        //fill it with a dummy XML. If no data has been put in the model getData
        //returns a document.
        if (mdlConfiguration.getData().documentElement)
        {
        	fillInPropertyScreen(xmlBaseObject.documentElement);
        }
    }
    else
    {
    	//Standalone (thus preview) mode
    	fillInPropertyScreen(xmlBaseObject.documentElement);
    }
    
    g_bInitialized = true;
}

/**
 * This method is called when the form is closed.
 */
function closeForm()
{
	application.removeLibrary("/cordys/wcp/admin/library/base64encoder.htm", libPassword);
	application.addGarbage(libPassword);
	
	application.removeLibrary("/cordys/wcp/library/util/xmlutil.htm", libXMLUtil);
	application.addGarbage(libXMLUtil);
}

/**
 * This method fills the models based on the configuration XML which is currently available.
 *
 * @param nConfigNode The current configuration node.
 */
function fillInPropertyScreen(nConfigNode)
{
	if (DEBUG_USERS.indexOf(system.getUser().name) > -1)
	{
		debugger;
	}
	
	//Create a XPath version of the document.
	var xmlDoc = createXMLDocument(nConfigNode.xml, NAMESPACE_PREFIXES);
	var nRoot = xmlDoc.documentElement;
	
    var nNode = nRoot.selectSingleNode("//ns:configuration");
    
    if (nNode == null)
    {
    	//No configuration found, use the empty template.
    	nNode = xmlBaseObject.documentElement;
    }
    
    nNode = createSimpleXMLDocument(nNode.xml).documentElement;
    
    //Decode all passwords
    decodePasswords(nNode, "//emailbox/password");
    decodePasswords(nNode, "//keymanager/password");
    decodePasswords(nNode, "//smtpserver/password");
    decodePasswords(nNode, "//parameters/parameter[@name='password']");
    decodePasswords(nNode, "//certificate/password");
    
    //Set the email boxes XML.
    var nEmailBoxes = nNode.selectSingleNode("./inbound/emailboxes");
    setFormattedXML(nEmailBoxes, taEmailBoxes);
    
    //Set the SMTP servers
    var nSMTPServers = nNode.selectSingleNode("./outbound/smtpservers");
    setFormattedXML(nSMTPServers, taSMTPServers);
    
    //Set the key managers
    var nKeyManagers = nNode.selectSingleNode("./outbound/keymanagers");
    setFormattedXML(nKeyManagers, taKeyManagers);
    
    mdlConfiguration.putData(nNode);
    mdlConfiguration.refreshAllViews();
    
    //Set the properly selected storage provider
    var nStorageProvider = nRoot.selectSingleNode("//ns:configuration/ns:general/ns:storage/ns:class");
    if (nStorageProvider != null)
    {
    	var sClass = nStorageProvider.text;
    	if (sClass != "com.cordys.coe.ac.emailio.storage.DBStorageProvider" &&
    		sClass != "com.cordys.coe.ac.emailio.storage.FileStorageProvider" &&
    		sClass != "com.cordys.coe.ac.emailio.storage.DefaultMemoryStore")
    	{
    		txtSPCustomClass.setValue(sClass);
    		sClass = ""; //Custom class
    	}
    	cbStorageProvider.setValue(sClass);
    	
    	//Now we need to fill the proper panel with the proper data
    	var nParameters = nRoot.selectSingleNode("//ns:configuration/ns:general/ns:storage/ns:parameters");
    	
    	//Decode the password
    	var anNodes = nParameters.selectNodes(".//ns:component/ns:password");
    	for (var iCount = 0; iCount < anNodes.length; iCount++)
    	{
    		anNodes[iCount].text = libPassword.decode(anNodes[iCount].text);
    	}
    	
    	fillStorageProviderConfig(sClass, nParameters);
    }
    
    handleStorageProviderChange(null);
}

/**
 * This method sets the htmlControl with the formatted xml from nNode.
 *  
 * @param nNode The node to write to htmlControl.
 * @param htmlControl The HTML control to contain the data.
 * 
 * @return Nothing.
 */
function setFormattedXML(nNode, htmlControl)
{
	if (nNode != null)
    {
    	var sFormattedXML = "" + libXMLUtil.xml2nicestring(nNode, 0);
    	var regex = new RegExp("^<([^\\s/]+).*/>$");
    	var mMatch = regex.exec(sFormattedXML);
    	if (mMatch != null)
    	{
    		sFormattedXML = sFormattedXML.substring(0, sFormattedXML.length - 2) + ">\n\n</" + mMatch[1] + ">"; 
    	}
    	
    	htmlControl.setValue(sFormattedXML);
    }
}

/**
 * This method stores the configuration.
 *
 * @param nConfigNode The current configuration node.
 */
function createConnectorConfiguration(nConfigNode)
{
	if (DEBUG_USERS.indexOf(system.getUser().name) > -1)
	{
		debugger;
	}
	
    var nData = mdlConfiguration.getData();
    if (nData != null)
    {
        var nClonedData = nData.cloneNode(true);
        
        //Replace the email boxes with the one from the edit
        var nInbound = nClonedData.selectSingleNode("inbound");
        storeXMLFromTextArea(taEmailBoxes, nInbound, "emailboxes");
        
        //Do the same for the key managers and SMTP servers
        var nOutbound = nClonedData.selectSingleNode("outbound");
        storeXMLFromTextArea(taKeyManagers, nOutbound, "keymanagers");
        storeXMLFromTextArea(taSMTPServers, nOutbound, "smtpservers");
		
        //Encode all the passwords.
        encodePasswords(nClonedData, "//emailbox/password");
        encodePasswords(nClonedData, "//keymanager/password");
        encodePasswords(nClonedData, "//smtpserver/password");
        encodePasswords(nClonedData, "//parameter[@name='password']");
        encodePasswords(nClonedData, "//certificate/password");
        
        //Remove the inserted attribute
        var alNodes = nClonedData.selectNodes("//*[@inserted]");
        for (var iCount = 0; alNodes != null && iCount < alNodes.length; iCount++)
        {
            alNodes[iCount].removeAttribute("inserted");
        }
        
        //Remove the sync_id attribute
        var alNodes = nClonedData.selectNodes("//*[@sync_id]");
        for (var iCount = 0; alNodes != null && iCount < alNodes.length; iCount++)
        {
            alNodes[iCount].removeAttribute("sync_id");
        }
        var alNodes = nClonedData.selectNodes("//*[@clientattr:sync_id]");
        for (var iCount = 0; alNodes != null && iCount < alNodes.length; iCount++)
        {
            alNodes[iCount].removeAttribute("clientattr:sync_id");
        }
        
        // Now we need to store the storage configuration. This is done based on the existing tab.
        var nGeneral = nClonedData.selectSingleNode("general");
        createStorageConfiguration(nGeneral);
        
        nConfigNode.appendChild(nClonedData);
    }
    
    return true;
}

/**
 * This method encodes the text of all elements that match the XPath into Base64.
 * 
 * @param nXML The XML to search.
 * @param sXPath The XPath to use.
 * 
 * @return Nothing.
 */
function encodePasswords(nXML, sXPath)
{
	var anNodes = nXML.selectNodes(sXPath);
	for (var iCount = 0; iCount < anNodes.length; iCount++)
	{
		if (anNodes[iCount].text != "")
		{
			anNodes[iCount].text = libPassword.encode(anNodes[iCount].text);
		}
	}
}

/**
 * This method decodes the text of all elements that match the XPath into Base64.
 * 
 * @param nXML The XML to search.
 * @param sXPath The XPath to use.
 * 
 * @return Nothing.
 */
function decodePasswords(nXML, sXPath)
{
	var anNodes = nXML.selectNodes(sXPath);
	for (var iCount = 0; iCount < anNodes.length; iCount++)
	{
		if (anNodes[iCount].text != "")
		{
			anNodes[iCount].text = libPassword.decode(anNodes[iCount].text);
		}
	}
}

/**
 * This method stores the XML from the text area as a child of the nParent node.
 * 
 * @param taData The text area containing the data.
 * @param nParent The parent XML node.
 * 
 * @return Nothing.
 */
function storeXMLFromTextArea(taData, nParent, sTagName)
{
	var nDestination = nParent.selectSingleNode(sTagName);
    if (nDestination == null)
    {
    	nDestination = nClonedData.ownerDocument.createNode(1, sTagName, NS_CONFIGURATION);
    	nParent.appendChild(nDestination);
    }

    while(nDestination.firstChild != null)
	{
		nDestination.removeChild(nDestination.firstChild);
	}

	//If someone uses xml:space="preserve" XMLDOM will add a xmlns:xml to the XML.
	//This needs to be removed.
    var sNewXMLData = "" + taData.getValue();
    var reRegEx = /\sxmlns:xml=".+?"/gm;
    sNewXMLData = sNewXMLData.replace(reRegEx, "");

    //Load the edited XML
    var xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
	xmlDoc.loadXML(sNewXMLData);

	if (libXMLUtil.checkAndReportError(xmlDoc) == null)
	{
		//XML ok
		var nDocElem = xmlDoc.documentElement;
		while (nDocElem.firstChild != null)
		{
			nDestination.appendChild(nDocElem.firstChild);
		}
	}
}

/**
 * This method will create the storage provider configuration based on the data filled in the UI.
 * 
 * @param nParent The parent node to append the XML structure to.
 *  
 * @return Nothing.
 */
function createStorageConfiguration(nParent)
{
	var nData = mdlDatabase.getData();
	
	var nStorage = nParent.selectSingleNode("storage");
	if (nStorage == null)
	{
		nStorage = createElementNS("storage", NS_CONFIGURATION, nParent);
	}
	else
	{
		while (nStorage.firstChild != null)
		{
			nStorage.removeChild(nStorage.firstChild);
		}
	}
	
	var sValue = cbStorageProvider.getValue();
	if (sValue == "")
	{
		createTextElementWithParentNS("class", txtSPCustomClass.getValue(), nStorage);
	}
	else 
	{
		createTextElementWithParentNS("class", sValue, nStorage);
	}
	
	var nParameters = createElementWithParentNS("parameters", nStorage);
	
	//Now write the parameters. 
	if (sValue == "com.cordys.coe.ac.emailio.storage.DBStorageProvider")
	{
		var nTemp = createTextElementWithParentNS("parameter", cCompressData.getValue(), nParameters);
		nTemp.setAttribute("name", "compressdata");
		nTemp.setAttribute("type", "string");
		
		nTemp = createElementWithParentNS("parameter", nParameters);
		nTemp.setAttribute("name", "dbconfig");
		nTemp.setAttribute("type", "xml");
		
		nTemp.appendChild(mdlDatabase.getData().selectSingleNode("//component").cloneNode(true));
		
		//Encode the password
		var anNodes = nTemp.selectNodes(".//component/password");
    	for (var iCount = 0; iCount < anNodes.length; iCount++)
    	{
    		anNodes[iCount].text = libPassword.encode(anNodes[iCount].text);
    	}
	}
	else if (sValue == "com.cordys.coe.ac.emailio.storage.FileStorageProvider")
	{
		var nTemp = createTextElementWithParentNS("parameter", txtFSErrorFolder.getValue(), nParameters);
		nTemp.setAttribute("name", "errorfolder");
		nTemp.setAttribute("type", "string");
		nTemp = createTextElementWithParentNS("parameter", cFSLogIncomming.getValue(), nParameters);
		nTemp.setAttribute("name", "logincomingmessages");
		nTemp.setAttribute("type", "string");
		nTemp = createTextElementWithParentNS("parameter", cFSLogFolder.getValue(), nParameters);
		nTemp.setAttribute("name", "logfolder");
		nTemp.setAttribute("type", "string");
		nTemp = createTextElementWithParentNS("parameter", txtFSTriggerFolder.getValue(), nParameters);
		nTemp.setAttribute("name", "triggerfolder");
		nTemp.setAttribute("type", "string");
	}
	else if (sValue == "com.cordys.coe.ac.emailio.storage.DefaultMemoryStore")
	{
		//No parameters to write.
	}
	else
	{
		var anNodes = mdlCustom.getData().selectNodes("//parameter");
		
		for (var iCount = 0; iCount < anNodes.length; iCount++) 
		{
			var sName = anNodes[iCount].selectSingleNode("./name").text;
			var sType = anNodes[iCount].selectSingleNode("./type").text;
			var sValue = anNodes[iCount].selectSingleNode("./value").text;
			
			var nTemp = null;
			if (sType == "xml")
			{
				nTemp = createElementWithParentNS("parameter", nParameters);
				var xmlTemp = createSimpleXMLDocument(sValue);
				nTemp.appendChild(xmlTemp.documentElement.cloneNode(true));
			}
			else
			{
				nTemp = createTextElementWithParentNS("parameter", sValue, nParameters);
			}
			
			nTemp.setAttribute("name", sName);
			nTemp.setAttribute("type", sType);
		}
	}
}

/**
 * This method returns the XML root for the XML parameter.
 * 
 * @param nParameter The parameter node.
 * 
 * @return The actual XML root.
 */
function getXMLRoot(nParameter)
{
	var nRealXML = nParameter.firstChild;
	while (nRealXML != null && nRealXML.nodeType != 1)
	{
		nRealXML = nRealXML.nextSibling;
	}
	
	return nRealXML;
}

/**
 * This function will parse the parameters and fill the proper UI panel.
 * 
 * @param nParameters The parameters node.
 * 
 * @return Nothing.
 */
function fillStorageProviderConfig(sClass, nParameters)
{
	if (sClass == "com.cordys.coe.ac.emailio.storage.DBStorageProvider")
	{
		var sTemp = nParameters.selectSingleNode("ns:parameter[@name='compressdata']").text;
		cCompressData.setValue(sTemp);
		
		var nTemp = nParameters.selectSingleNode("ns:parameter[@name='dbconfig']");

		//The parameter is XML, so let's get it.
		var nActual = getXMLRoot(nTemp);
		var sXMLData = "";
		if (nActual.nodeType == 1)
		{
			sXMLData = nActual.xml;
		}
		xmlDoc = createSimpleXMLDocument("<dbconfig xmlns=\"http://emailioconnector.coe.cordys.com/2.0/configuration\">" + nActual.xml + "</dbconfig>");
		
		mdlDatabase.putData(xmlDoc.documentElement);
		mdlDatabase.refreshAllViews();
	}
	else if (sClass == "com.cordys.coe.ac.emailio.storage.FileStorageProvider")
	{
		var sTemp = nParameters.selectSingleNode("ns:parameter[@name='errorfolder']").text;
		txtFSErrorFolder.setValue(sTemp);
		sTemp = nParameters.selectSingleNode("ns:parameter[@name='logincomingmessages']").text;
		cFSLogIncomming.setValue(sTemp);
		sTemp = nParameters.selectSingleNode("ns:parameter[@name='logfolder']").text;
		cFSLogFolder.setValue(sTemp);
		sTemp = nParameters.selectSingleNode("ns:parameter[@name='triggerfolder']").text;
		txtFSTriggerFolder.setValue(sTemp);
	}
	else if (sValue == "com.cordys.coe.ac.emailio.storage.DefaultMemoryStore")
	{
		//No parameters to write.
	}
	else
	{
		var anNodes = nParameters.selectNodes("//ns:parameter");
		var xmlDoc = createSimpleXMLDocument("<parameters/>");
		var nRoot = xmlDoc.documentElement;
		
		for (var iCount = 0; iCount < anNodes.length; iCount++) 
		{
			var sName = anNodes[iCount].getAttribute("name");
			var sType = anNodes[iCount].getAttribute("type");
			var sValue = anNodes[iCount].text;
			
			var nParam = createElementWithParentNS("parameter", nRoot);
			createTextElementWithParentNS("name", sName, nParam);
			createTextElementWithParentNS("type", sType, nParam);
			createTextElementWithParentNS("value", sValue, nParam);
		}
		
		mdlCustom.putData(nRoot);
		mdlCustom.refreshAllViews();
	}
}

/**
 * This method is used to test the save functionality.
 * 
 * @param eventObject The event object.
 * 
 * @return Nothing.
 */
function handleTestSave(eventObject)
{
	xmlDoc = createSimpleXMLDocument("<config/>");
	createConnectorConfiguration(xmlDoc.documentElement);
	alert(xmlDoc.xml);
}

/**
 * This method is called when the value of the select has changed. It will make sure that the proper 
 * group box is shown.
 * 
 * @param eventObject The event object.
 * 
 * @return Nothing.
 */
function handleStorageProviderChange(eventObject)
{
	var sValue = cbStorageProvider.getValue();
	
	if (sValue == "com.cordys.coe.ac.emailio.storage.DBStorageProvider")
	{
		gbSPDatabase.show();
		gbSPFileSystem.hide();
		gbSPMemory.hide();
		gbSPCustom.hide();
		
		if (!mdlDatabase.getData() || mdlDatabase.getData().firstChild == null || mdlDatabase.getData().selectSingleNode("//component") == null)
		{
			mdlDatabase.putData(xmlBaseComponent.documentElement.cloneNode(true));
			mdlDatabase.refreshAllViews(true);
		}

	}
	else if (sValue == "com.cordys.coe.ac.emailio.storage.FileStorageProvider")
	{
		gbSPDatabase.hide();
		gbSPFileSystem.show();
		gbSPMemory.hide();
		gbSPCustom.hide();
	}
	else if (sValue == "com.cordys.coe.ac.emailio.storage.DefaultMemoryStore")
	{
		gbSPDatabase.hide();
		gbSPFileSystem.hide();
		gbSPMemory.show();
		gbSPCustom.hide();
	}
	else
	{
		gbSPDatabase.hide();
		gbSPFileSystem.hide();
		gbSPMemory.hide();
		gbSPCustom.show();
	}
}