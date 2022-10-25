
var tweetstable = null;
var mainTimeline = null;

				

/**
	 * Generate the node required for user display length changing
	 *  @param {object} settings dataTables settings object
	 *  @returns {node} Display length feature node
	 *  @memberof DataTable#oApi
	 */
	

	
$(function() {
		
	
		
	
		
		var totalresults = 0;
        var ERROR_MESSAGE = "Une erreur s'est produite ! Veuillez réessayer ultérieurement ";
		console.log("Let's get this party started for Trends...");
	
		
		twitterTrendsDataTable.init();

		$(StateManager).on("statechange", function() {updateAll();});
		
		
		$(window).on("popstate", function(e) {
				console.log("popstate",  StateManager.getState(), window.location.hash);
				StateManager.setState(StateManager.getStateFromUrl(), true);
				console.log("popstate set state true");
		});



		$("#searchform").submit(function () {

				var query = { "query" : $("#searchform").serializeObject()};
				
				StateManager.setState(query, false);
				console.log("set state true search form")
				searchclicked = true;
				return false;

		});
	
		if(StateManager.getStateFromUrl()["query"] != undefined )
		{
			console.log("set state true page overall")
			console.log(StateManager.getStateFromUrl());
			StateManager.setState(StateManager.getStateFromUrl(), false);
			searchclicked = false;
		}
		
});


function updateAll() {
				console.log("8");
				
	         
				twitterTrendsDataTable.update();
				
				
				return false;
					
				
}