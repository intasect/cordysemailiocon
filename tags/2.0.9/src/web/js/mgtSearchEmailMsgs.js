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
	txtFromsenddate.setValue('');
	txtTosenddate.setValue('');
	txtFromreceivedate.setValue('');
	txtToreceivedate.setValue('');
	
	txtRawcontent.disable();
	
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
					txtContextContainerID.setValue(nWindowData.selectSingleNode(".//ContextContainerID").text);
		            
					//Call this method to fire the model and update the controls with the data.
					searchEmailMessages();
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
function searchEmailMessages()
{
	//Get the value from the method
	var bContinue = checkFieldValues();
	if (bContinue == true)
	{
		//Fire the model.
		WebForm.sendRequest(Find_EmailMessageModelGroup);
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
	bReturn = checkDates(txtFromsenddate, txtTosenddate, "The from send date is greater then the to send date");
	if (bReturn == true)
	{
		bReturn = checkDates(txtFromreceivedate, txtToreceivedate, "The from receive date is greater then the to receive date");
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
