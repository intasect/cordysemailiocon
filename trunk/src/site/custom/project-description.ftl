<#-- Contains the project description that will be put in the index page. -->
<p>
	This connector succeeds the Inbound Email Connector. All functionality has been moved to this connector. 
	In addition to the inbound features it is now also capable of sending emails. 
</p>

<p>
	Incorporating the Inbound email Connector functionality had some consequences. To be consistent 
	some namespaces had to be changed. Both in the methods as in the configuration XMLs.   
</p>

<p>
	Changing namespaces automatically means breaking backwards compatibility. So the old methods have 
	been removed and you will need to recreate any Soap Processors already created.    
</p>

<p>
	This connector can be used to react to inbound mail traffic. Sometimes you need to start a 
	flow (or call any web service) based on an email that was received. You'd also like to get 
	the details of an attachment, parse it and get data from it to feed to the process. 
	This is all possible with this connector.
</p>

<p>
	The second feature for this connector is that it is also able to send mails. It is compatible with 
	the standard Cordys email web services, so it can function as a substitute for the standard Cordys email connector.
</p>

<p>
	There are 2 major differences between the standard platform email connector:
	<ul>
		<li>This connector supports S/MIME (Both sending and receiving)</li>
		<li>
			The SendMail method specifically for this connector allows more control over how the 
			actual mail structure will be
		</li>
	</ul>	
</p>

<p>
	For more information, examples and tutorials <a href="https://wiki.cordys.com/display/dsc/EmailIOConnector">go to the wiki</a>  
</p>

<p>
	Latest version is ${ant["buildinfo.latest.project.version"]}.<br/>
</p>
	
<p>
	All available versions are here: <a href="versions.html">link</a>
</p>

<p>
	The Inbound Email Connector versions are in maintenance mode. They can be found here:
	<ul>
		<li><a href="maintenance/1.1/index.html">1.1.x</a> - This version uses the storage mechanism (DB), management UIs and retry mechanism.</li>
		<li><a href="maintenance/1.0/index.html">1.0.x</a> - This version only uses filesystem storage</li>
	</ul> 
</p>