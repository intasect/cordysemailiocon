/**
 * This variable indicates whether or not the application was already started.
 */
var g_bAppStarted = false;
/**
 * Holds the lib to do the password encoding.
 */
var libPassword = null;
/**
 * Holds the configuration namespace.
 */
var NS_DYNAMIC = "http://emailioconnector.coe.cordys.com/2.0/inbound/dynamic";
/**
 * Holds the default namespace prefix mapping for the XML.
 */
var NAMESPACE_PREFIXES = "xmlns:ns='" + NS_DYNAMIC + "'";


/**
 * Prototype function to get the next month.
 */
Date.prototype.prevMonth = function ()
{
	var thisMonth = this.getMonth();
	this.setMonth(thisMonth-1);
	if(this.getMonth() != thisMonth-1 && (this.getMonth() != 11 || (thisMonth == 11 && this.getDate() == 1)))
	{
		this.setDate(0);
	}
}

/**
* Prototype function to get the previous month.
*/
Date.prototype.nextMonth = function ()
{
	var thisMonth = this.getMonth();
	this.setMonth(thisMonth+1);
	if(this.getMonth() != thisMonth+1 && this.getMonth() != 0)
	{
		this.setDate(0);
	}
}

/**
 * This method is called when the form initializes. It will make sure that by default all datefields are empty.
 * Only the LastStatusChangeDate will be filled with a date 1 month before.
 * 
 * @param eventObject The onInitDone event.
 * 
 * @return Nothing.
 */
function handleInitDone(eventObject)
{
    if (DEBUG_USERS.indexOf(system.getUser().name) > -1)
	{
    	debugger;
	}
    
	//Initialize the search fields
	txtFromstatuschangedate.setValue('');
	txtFromcreatedate.setValue('');
	txtTocreatedate.setValue('');
	txtFromcompletedate.setValue('');
	txtTocompletedate.setValue('');
	
	txtCompleted.setValue("true");
	
	var dDate = new Date();
	dDate.prevMonth();
	txtTostatuschangedate.setValue(dDate);
	
	txtZipMethod.setValue("-1");
	txtFBLocation.setValue("coe/emailioconnector/archives");
	
	//Make sure only 1 panel is shown.
	handleArchivingOptionChange(null);
	
    //Create the libPassword object
    libPassword = window.document.createElement("SPAN");
    libPassword.id = "libPassword";
    
    //Attach the base64 library.
    application.addLibrary("/cordys/wcp/admin/library/base64encoder.htm", libPassword);
    
	//If data was passed on to this screen, do the auto-search
	if (g_bAppStarted == false)
	{
		g_bAppStarted = true;
		if (window.application && window.application.event)
		{
			var nWindowData = window.application.event.applicationDefinition.selectSingleNode(".//data");
		    if (nWindowData != null)
		    {
		        if (nWindowData.text != "")
		        {
		            // Set the values taken from window.application to the input controls in the XForm.
					txtEmailBoxID.setValue(nWindowData.selectSingleNode(".//EmailBoxID").text);
		       }
		    }
		}
	}
}
 
/**
 * This method is called when the form is closed.
 */
function closeForm()
{
 	application.removeLibrary("/cordys/wcp/admin/library/base64encoder.htm", libPassword);
 	application.addGarbage(libPassword);
}

/**
 * This method is called when the value of the select has changed. It will make sure that the proper 
 * group box is shown.
 * 
 * @param eventObject The event object.
 * 
 * @return Nothing.
 */
function handleArchivingOptionChange(eventObject)
{
	var sValue = txtArchivingOption.getValue();
	
	if (sValue == "com.cordys.coe.ac.emailio.archive.DatabaseArchiver")
	{
		gbDatabaseArchiver.show();
		gbFileArchiving.hide();
		gbCustomArchiver.hide();
		
		if (!mdlDatabase.getData() || mdlDatabase.getData().firstChild == null || mdlDatabase.getData().selectSingleNode("//component") == null)
		{
			mdlDatabase.putData(xmlBaseComponent.documentElement.cloneNode(true));
			mdlDatabase.refreshAllViews(true);
		}

	}
	else if (sValue == "com.cordys.coe.ac.emailio.archive.FileArchiver")
	{
		gbDatabaseArchiver.hide();
		gbFileArchiving.show();
		gbCustomArchiver.hide();
	}
	else
	{
		gbDatabaseArchiver.hide();
		gbFileArchiving.hide();
		gbCustomArchiver.show();
	}
}

/**
 * This method builds up the actual archiving request and will kick off the archiving process.
 * Updates from the archiving process will be sent using the event service. 
 * 
 * @param eventObject The onClick event.
 * 
 * @return Nothing.
 */
function doActualArchiving(eventObject)
{
	if (DEBUG_USERS.indexOf(system.getUser().name) > -1)
	{
		debugger;
	}
	
	//Create the SOAP request 
	var dRequest = createXMLDocument(xmlArchiveRequest.documentElement.xml, NAMESPACE_PREFIXES);
	
	var nMethod = dRequest.documentElement.selectSingleNode("//ns:ArchiveContainers");
	
	//Create the part with the archiver configuration
	createArchiverConfiguration(nMethod);
	
	//Fill the SearchContainer variables.
	if (txtEmailBoxID.getValue() != "")
	{
		nMethod.selectSingleNode("ns:search/ns:EmailBoxID").text = txtEmailBoxID.getValue(); 
	}
	if (txtFromcreatedate.getValue() != "")
	{
		nMethod.selectSingleNode("ns:search/ns:FromCreateDate").text = txtFromcreatedate.getValue(); 
	}
	if (txtTocreatedate.getValue() != "")
	{
		nMethod.selectSingleNode("ns:search/ns:ToCreateDate").text = txtTocreatedate.getValue(); 
	}
	if (txtFromcompletedate.getValue() != "")
	{
		nMethod.selectSingleNode("ns:search/ns:FromCompleteDate").text = txtFromcompletedate.getValue(); 
	}
	if (txtTocompletedate.getValue() != "")
	{
		nMethod.selectSingleNode("ns:search/ns:ToCompleteDate").text = txtTocompletedate.getValue(); 
	}
	if (txtFromstatuschangedate.getValue() != "")
	{
		nMethod.selectSingleNode("ns:search/ns:FromStatusChangeDate").text = txtFromstatuschangedate.getValue(); 
	}
	if (txtTostatuschangedate.getValue() != "")
	{
		nMethod.selectSingleNode("ns:search/ns:ToStatusChangeDate").text = txtTostatuschangedate.getValue(); 
	}
	
	//Build up the status string
	var sStatus = "";
	if (txtCompleted.getValue() == "true")
	{
		sStatus = "COMPLETED"; 
	}
	
	if (txtActionError.getValue() == "true")
	{
		sStatus += (sStatus != "" ? ", " : "") + "ACTION_ERROR"; 
	}
	
	if (txtMessageError.getValue() == "true")
	{
		sStatus += (sStatus != "" ? ", " : "") + "MESSAGE_ERROR"; 
	}
	
	nMethod.selectSingleNode("ns:search/ns:Status").text = sStatus; 
	
	//Now the request can be sent to Cordys.
	//Passing on the document element to make sure the document is parsed into
	//XSL selection language.
	executeRequest(dRequest.documentElement);
	
	//TODO: show panel that will display the status information
}

/**
 * This method will create the archiver configuration based on the data filled in the UI.
 * 
 * @param nParent The parent node to append the XML structure to.
 *  
 * @return Nothing.
 */
function createArchiverConfiguration(nParent)
{
	var nData = mdlDatabase.getData();
	
	//Remove old data if needed or create a new tag
	var nArchiver = nParent.selectSingleNode("ns:archiver");
	if (nArchiver == null)
	{
		nArchiver = createElementNS("archiver", NS_DYNAMIC, nParent);
	}
	else
	{
		while (nArchiver.firstChild != null)
		{
			nArchiver.removeChild(nArchiver.firstChild);
		}
	}
	
	var sValue = txtArchivingOption.getValue();
	if (sValue == "")
	{
		createTextElementWithParentNS("class", txtSPCustomClass.getValue(), nArchiver);
	}
	else 
	{
		createTextElementWithParentNS("class", sValue, nArchiver);
	}
	
	var nParameters = createElementWithParentNS("parameters", nArchiver);
	
	//Now write the parameters. 
	if (sValue == "com.cordys.coe.ac.emailio.archive.DatabaseArchiver")
	{
		var nTemp = createTextElementWithParentNS("parameter", cCompressData.getValue(), nParameters);
		nTemp.setAttribute("name", "compressdata");
		nTemp.setAttribute("type", "string");
		
		nTemp = createElementWithParentNS("parameter", nParameters);
		nTemp.setAttribute("name", "dbconfig");
		nTemp.setAttribute("type", "xml");
		
		var nDBConfig = mdlDatabase.getData().selectSingleNode("//component").cloneNode(true);
		
		//Encode the password
		var anNodes = nDBConfig.selectNodes("password");
	   	for (var iCount = 0; iCount < anNodes.length; iCount++)
	   	{
	   		anNodes[iCount].text = libPassword.encode(anNodes[iCount].text);
	   	}
	   	
		nTemp.appendChild(nDBConfig);
	}
	else if (sValue == "com.cordys.coe.ac.emailio.archive.FileArchiver")
	{
		var nTemp = createTextElementWithParentNS("parameter", txtZipMethod.getValue(), nParameters);
		nTemp.setAttribute("name", "ziplevel");
		nTemp.setAttribute("type", "string");
		nTemp = createTextElementWithParentNS("parameter", txtFBLocation.getValue(), nParameters);
		nTemp.setAttribute("name", "location");
		nTemp.setAttribute("type", "string");
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
