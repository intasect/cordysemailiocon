/**
 * This variable indicates whether or not the application was already started.
 */
var g_bAppStarted = false;
/**
 * Holds the context menu on the table.
 */
var g_cmContainerDetails = null;
/**
 * Holds the menu item that identifies the retryContainer item.
 */
var g_miRetryContainer = null;
/**
 * Holds the configuration namespace.
 */
var NS_DYNAMIC = "http://emailioconnector.coe.cordys.com/2.0/inbound/dynamic";
/**
 * Holds the default namespace prefix mapping for the XML.
 */
var NAMESPACE_PREFIXES = "xmlns:ns='" + NS_DYNAMIC + "'";

/**
 * This method is called when the form initializes. It will make sure that by default all datefields are empty.
 * 
 * @param eventObject The onInitDone event.
 * 
 * @return Nothing.
 */
function handleInitDone(eventObject)
{
	//Initialize the search fields
	txtFromstatuschangedate.setValue('');
	txtFromcreatedate.setValue('');
	txtTocreatedate.setValue('');
	txtFromcompletedate.setValue('');
	txtTocompletedate.setValue('');
	txtTostatuschangedate.setValue('');
	
	txtStatusInformation.disable();
	txtTriggerDefinition.disable();
	
	//Create the context menu
	loadContextMenu();
	
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
		            
					//Call this method to fire the model and update the controls with the data.
					searchContextContainers();
		       }
		    }
		}
	}
}

/**
 * This method is called when the actual search needs to be done.
 * 
 * @return Nothing.
 */
function searchContextContainers()
{
	//Get the value from the method
	var bContinue = checkFieldValues();
	if (bContinue == true)
	{
		//Fire the model.
		WebForm.sendRequest(Find_SearchContainersModelGroup);
		
		if (mdlSearchContainers.getData().selectSingleNode( "//tuple" ) != null)
	    {
			//register the menu items to the table rows.
			var aoRows = new Array();
			
			// put the table's rows in a array object.
			aoRows = tblContextContainers.getRows();
			
			//now for each row add or remove the context menu.
		    for (var iRows = 0; iRows < aoRows.length; iRows++)
		    {
		    	g_cmContainerDetails.registerHTML(aoRows[iRows]);
		    }
	    }
	}
}

/**
 * This method will check if fields values are OK for searching.
 *
 * @return bReturn:boolean value which is used as a flag.
 */
function checkFieldValues()
{
	var bReturn = true;
	
	//Compare the starttime with the end time.
	bReturn = checkDates(txtFromcreatedate, txtTocreatedate, "The from create date is greater then the to create date");
	if (bReturn == true)
	{
		bReturn = checkDates(txtFromcompletedate, txtTocompletedate, "The from complete date is greater then the to complete date");
	}
	if (bReturn == true)
	{
		bReturn = checkDates(txtFromstatuschangedate, txtTostatuschangedate, "The from status change date is greater then the to status change date");
	}
	
	return bReturn;
}

/**
 * This method checks if the given 2 times are valid (thus the start date is not after the end date.
 * 
 * @param htmlFrom The HTML object to get the start-date.
 * @param htmlTo   The HTML object to get the end-date.
 * @param sText    The text to display if the start date is greater.
 * 
 * @return true if the dates are OK. Otherwise false.
 */
function checkDates(htmlFrom, htmlTo, sText)
{
	var bReturn = true;
	
	var sFromTime = htmlFrom.getValue();
	var sToTime = htmlTo.getValue();
	if (sFromTime != "" && sToTime != "")
	{
		var dFromTime = new Date();
	 	var dToTime = new Date();
		dFromTime.setUTCFullYear(parseInt(sFromTime.substring(0, 4), 10));
		dFromTime.setUTCMonth(parseInt(sFromTime.substring(5, 7), 10) - 1);
		dFromTime.setUTCDate(parseInt(sFromTime.substring(8, 10), 10));
		dFromTime.setUTCHours(parseInt(sFromTime.substring(11, 13), 10));
		dFromTime.setUTCMinutes(parseInt(sFromTime.substring(14, 16), 10));
		dFromTime.setUTCSeconds(parseInt(sFromTime.substring(17, 19), 10));
		
		dToTime.setUTCFullYear(parseInt(sToTime.substring(0, 4), 10));
		dToTime.setUTCMonth(parseInt(sToTime.substring(5, 7), 10) - 1);
		dToTime.setUTCDate(parseInt(sToTime.substring(8, 10), 10));
		dToTime.setUTCHours(parseInt(sToTime.substring(11, 13), 10));
		dToTime.setUTCMinutes(parseInt(sToTime.substring(14, 16), 10));
		dToTime.setUTCSeconds(parseInt(sToTime.substring(17, 19), 10));
		if (dFromTime > dToTime)
		{
			application.showError(sText, "Cordys Error", htmlFrom);
			WebForm.setValidStyleClass(htmlFrom, false);
			htmlFrom.focus();
			bReturn = false;
		}
	}
	
	return bReturn;
}
 
/**
 * This method will load the context menus and attach it to the table.
 */
function loadContextMenu()
{
 	//create the eibus definition.
 	g_cmContainerDetails = document.createElement('<eibus:contextmenu id="ctxContainerDetails" automaticLoad="true" style="display:none;" oncontext="onContextHandler4ContainerDetails();"/>');
   
 	//Load the library
 	application.addLibrary("/cordys/wcp/library/ui/contextmenu.htm", g_cmContainerDetails);

 	//add the menu items SYNTAX: oMenuItem = contextMenuID.addItem(sId, fpMethod, sContent [, bEnable])
 	g_cmContainerDetails.addItem("searchEmailMessages", handleContextSearchEmailMessages, "View all emails", "true");
 	g_miRetryContainer = g_cmContainerDetails.addItem("retryContainer", handleContextReprocessContainer, "Reprocess container", "false");
}
 
/**
 * This method is called when the context menu is shown. Based on the status of the current container 
 * the reprocessing option is enabled (only when the processing status is MESSAGE_ERROR).
 * 
 * @return Nothing.
 */
function onContextHandler4ContainerDetails()
{
	g_miRetryContainer.enabled = "false";
	var oActiveElement = g_cmContainerDetails.activeElement;
	if (oActiveElement != null)
	{
		var oBusinessObject = oActiveElement.businessObject;
		if (oBusinessObject != null)
		{
			//AgentID
			var sStatus = oBusinessObject.selectSingleNode(".//ProcessingStatus").text;
			if (sStatus.toUpperCase() == "MESSAGE_ERROR")
			{
				g_miRetryContainer.enabled = "true";
			}
		}
	}
}
 
/**
 * This method is called when the context item 'View all emails' is clicked. It will
 * start up the mgtSearchEmailMsgs screen and pass on the current context container ID.
 * 
 * @return Nothing.
 */
function handleContextSearchEmailMessages()
{
	if (DEBUG_USERS.indexOf(system.getUser().name) > -1)
	{
		debugger;
	}
	
	var oActiveElement = g_cmContainerDetails.activeElement;
	if (oActiveElement != null)
	{
		var oBusinessObject = oActiveElement.businessObject;
		if (oBusinessObject != null)
		{
			var xmlApplication = xmlSearchEmailMessages.documentElement.cloneNode(true);
			
			//The email box ID
			var sContextContainerID = "";
			
			var nNode = oBusinessObject.selectSingleNode(".//ID");
			if (nNode != null)
			{
				sContextContainerID = nNode.text;
				xmlApplication.selectSingleNode(".//ContextContainerID").text = sContextContainerID;
			}
			
			//Fix the Application XML
			xmlApplication.selectSingleNode(".//id").text = "sc_" + sContextContainerID;
			var sDescription = "Search emails";
			if (sContextContainerID != "")
			{
				sDescription += " for context container " + sContextContainerID;
			}
			xmlApplication.selectSingleNode(".//description").text = sDescription;
			xmlApplication.selectSingleNode(".//caption").text = sDescription;
			
			// Now load the application.
			application.select(xmlApplication);
		}
	}
}

/**
 * This method is called when the context item is clicked to reprocess the currently selected container.
 * 
 * @return Nothing.
 */
function handleContextReprocessContainer()
{
	//Fix the request to fire off the restart of the context container.
	if (DEBUG_USERS.indexOf(system.getUser().name) > -1)
	{
		debugger;
	}

	var oActiveElement = g_cmContainerDetails.activeElement;
	if (oActiveElement != null)
	{
		var oBusinessObject = oActiveElement.businessObject;
		if (oBusinessObject != null)
		{
			//The email box ID
			var sContextContainerID = "";
	
			var nNode = oBusinessObject.selectSingleNode(".//ID");
			if (nNode != null)
			{
				sContextContainerID = nNode.text;
				
				//Create the SOAP request 
				var dRequest = createXMLDocument(xmlRestartContainer.documentElement.xml, NAMESPACE_PREFIXES);
				var nContextID = dRequest.documentElement.selectSingleNode("//ns:RestartContainer/ns:contextcontainerid");
				nContextID.text = sContextContainerID;
				
				//Now the request can be sent to Cordys.
				//Passing on the document element to make sure the document is parsed into
				//XSL selection language.
				executeRequest(dRequest.documentElement);
			}
		}
	}
}