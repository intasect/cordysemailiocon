/**
 * This variable indicates whether or not the application was already started.
 */
var g_bAppStarted = false;

/**
 * This method is called when the form initializes. It will make sure that by default all datefields are empty.
 * 
 * @param eventObject The onInitDone event.
 * 
 * @return Nothing.
 */
function handleInitDone(eventObject)
{
	txtTriggerDefinition.disable();
	
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
					searchTriggerDefinitions();
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
function searchTriggerDefinitions()
{
	WebForm.sendRequest(Find_TriggerDefinitionModelGroup);
}