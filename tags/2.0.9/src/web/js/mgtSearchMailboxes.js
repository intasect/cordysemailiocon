/**
 * Holds the context menu that was created.
 */
var g_cmContextContainerDetails = null;

/**
 * This method is called when the form initializes. It will make sure that by default all datefields are empty.
 * 
 * @param eventObject The onInitDone event.
 * 
 * @return Nothing.
 */
function handleInitDone(eventObject)
{
	txtConfiguration.disable();
	
	loadContextMenu();
}

/**
 * This method is called when a new search should be executed. It will make sure the context menu
 * is registered on the new data.
 * 
 * @return Nothing.
 */
function handleSearchEmailBoxes()
{
	WebForm.sendRequest(Find_EmailBoxModelGroup);
	
	if (mdlEmailBox.getData().selectSingleNode( "//tuple" ) != null)
    {
		//register the menu items to the table rows.
		var aoRows = new Array();
		
		// put the table's rows in a array object.
		aoRows = tblEmailBoxes.getRows();
		
		//now for each row add or remove the context menu.
	    for (var iRows = 0; iRows < aoRows.length; iRows++)
	    {
	    	g_cmContextContainerDetails.registerHTML(aoRows[iRows]);
	    }
    }
}

/**
 * This method will load the context menus and attach it to the table.
 */
function loadContextMenu()
{
	//create the eibus definition.
	g_cmContextContainerDetails = document.createElement('<eibus:contextmenu id="ctxContainerDetails" automaticLoad="true" style="display:none;"/>');
	   
	//Load the library
	application.addLibrary("/cordys/wcp/library/ui/contextmenu.htm", g_cmContextContainerDetails);
	
	//add the menu items SYNTAX: oMenuItem = contextMenuID.addItem(sId, fpMethod, sContent [, bEnable])
	g_cmContextContainerDetails.addItem("searchContainers", handleContextSearchContainers, "Search context containers", "true");
	g_cmContextContainerDetails.addItem("showTriggerDefinitions", handleShowTriggerDefinitions, "Show trigger definitions", "true");
}
 
/**
 * This method is called when the context item 'Seach context containers' is clicked. It will
 * start up the mgtSearchContainers screen and pass on the current email box ID.
 * 
 * @return Nothing.
 */
function handleContextSearchContainers()
{
	if (DEBUG_USERS.indexOf(system.getUser().name) > -1)
	{
		debugger;
	}
	
	var oActiveElement = g_cmContextContainerDetails.activeElement;
	if (oActiveElement != null)
	{
		var oBusinessObject = oActiveElement.businessObject;
		if (oBusinessObject != null)
		{
			var xmlApplication = xmlSearchContainers.documentElement.cloneNode(true);
			
			//The email box name
			var sEmailBoxName = "";
			
			var nNode = oBusinessObject.selectSingleNode(".//Name");
			if (nNode != null)
			{
				sEmailBoxName = nNode.text;
				xmlApplication.selectSingleNode(".//EmailBoxID").text = sEmailBoxName;
			}
			
			//Fix the Application XML
			xmlApplication.selectSingleNode(".//id").text = "sc_" + sEmailBoxName;
			var sDescription = "Search Context containers";
			if (sEmailBoxName != "")
			{
				sDescription += " for email box " + sEmailBoxName;
			}
			xmlApplication.selectSingleNode(".//description").text = sDescription;
			xmlApplication.selectSingleNode(".//caption").text = sDescription;
			
			// Now load the application.
			application.select(xmlApplication);
		}
	}
}
 
 /**
  * This method is called when the context item 'Seach context containers' is clicked. It will
  * start up the mgtSearchContainers screen and pass on the current email box ID.
  * 
  * @return Nothing.
  */
 function handleShowTriggerDefinitions()
 {
 	if (DEBUG_USERS.indexOf(system.getUser().name) > -1)
 	{
 		debugger;
 	}
 	
 	var oActiveElement = g_cmContextContainerDetails.activeElement;
 	if (oActiveElement != null)
 	{
 		var oBusinessObject = oActiveElement.businessObject;
 		if (oBusinessObject != null)
 		{
 			var xmlApplication = xmlSearchTriggerDefinitions.documentElement.cloneNode(true);
 			
 			//The email box name
 			var sEmailBoxName = "";
 			
 			var nNode = oBusinessObject.selectSingleNode(".//Name");
 			if (nNode != null)
 			{
 				sEmailBoxName = nNode.text;
 				xmlApplication.selectSingleNode(".//EmailBoxID").text = sEmailBoxName;
 			}
 			
 			//Fix the Application XML
 			xmlApplication.selectSingleNode(".//id").text = "std_" + sEmailBoxName;
 			var sDescription = "Search trigger definitions";
 			if (sEmailBoxName != "")
 			{
 				sDescription += " for email box " + sEmailBoxName;
 			}
 			xmlApplication.selectSingleNode(".//description").text = sDescription;
 			xmlApplication.selectSingleNode(".//caption").text = sDescription;
 			
 			// Now load the application.
 			application.select(xmlApplication);
 		}
 	}
}