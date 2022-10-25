
var tabstats = {0:true,1:false,2:false,3:false,4:false};
var analyticstable = null;
var tweetstable = null;
var mainTimeline = null;


				

/**
	 * Generate the node required for user display length changing
	 *  @param {object} settings dataTables settings object
	 *  @returns {node} Display length feature node
	 *  @memberof DataTable#oApi
	 */
	

	
$(function() {
		
		$(".js-example-basic-multiple").select2();
		// Initialize tabs options 
		$( "#tabs" ).tabs(
		{
			 activate: function (event, ui) {
				if(!tabstats[ui.newTab.index() ] )
					loadTabs(ui.newTab.index() );
				tabstats[ui.newTab.index() ] = true;
				}
			
		});
		
	
		
		var totalresults = 0;
        var ERROR_MESSAGE = "Une erreur s'est produite ! Veuillez réessayer ultérieurement ";
		console.log("Let's get this party started...");
	
		
		mainTimeline = new timeline("#container_timeline","main");
		twitterDataTable.init();
		analyzeDataTable.init();
		
       
	
		$(StateManager).on("statechange", function() {updateAll();});
		
		
		$(window).on("popstate", function(e) {
				console.log("popstate",  StateManager.getState(), window.location.hash);
				StateManager.setState(StateManager.getStateFromUrl(), true);
				console.log("popstate set state true");
				
		});

	
		$('#analyticsform').submit(function () {
				var newanalyze = $("#analyticsform").serializeObject()
				analyzeDataTable.addRow(newanalyze["analytics_keys"],newanalyze["analytics_type"]);
				return false;
		});

		
		$('#iramuteqform').submit(function () {
				iramuteq.load();
				return false;
		});
 
		
		$('#textform').submit(function () {
			
			var totalresults = twitterDataTable.info();
			if(totalresults < 80000000)
			{
				if(wordcloudDataTable.table)
					wordcloudDataTable.update();
				else 
					wordcloudDataTable.init();
			
				
			}
			else
			{
				console.error(totalresults);
				wordcloudDataTable.clean();
			}
			return false;
		});
			
		
		$('#button_analytics_delete_selected').click( function () {
			
				analyzeDataTable.deleteSelecetedRow();
				return false;
		} );

		$("#searchform").submit(function () {
			
				var t = $('input[name=hashtags]').val();
				$('input[name=hashtags]').val(t.replace(/\#/g, ''))

				t = $('input[name=mentions]').val();
				$('input[name=mentions]').val(t.replace(/\@/g, ''))
			

				var query = { "query" : $("#searchform").serializeObject()};
				
				StateManager.setState(query, false);
				console.log("set state true search form")
				searchclicked = true;
				return false;

		});
		
		console.log("Current State", StateManager.getStateFromUrl());
	
		if(StateManager.getStateFromUrl()["query"] )
		{
			console.log("set state true page overall")
			console.log(StateManager.getStateFromUrl());
			StateManager.setState(StateManager.getStateFromUrl(), false);
			searchclicked = false;
		}
		else{
			var query = { "query" : $("#searchform").serializeObject()};
			StateManager.setState(query, true);

		}



		
});

 
function updateAll() {
			

						// fill form data
			var mergedState = StateManager.getStateFromUrl()	;
			// problem in firefox 3.5 !!!!!!!!!!!!!!!!!!!!!!!!!
			
			var nAgt = navigator.userAgent;
				console.log(nAgt);
				var highmargin = -1;
				// what is this no sense it is the same code with else
				if ((verOffset=nAgt.indexOf("Firefox"))!=-1) {
					 browserName = "Firefox";
					 fullVersion = nAgt.substring(verOffset+8);
					
					 if(fullVersion.indexOf("3.5")==-1)
					 {
						$.each(mergedState["query"], function(key, value) {
						if(key != "retweet" && key !="quote" && key!="source" && key!="collection") 
						{
							$("#searchform [name='"+key+"']").val(value);
						}
						else if(key == "collection")
						{
							$("#collections").val(value.split(";")).trigger('change');
						}
						else
						{
							$("input[name='"+key+"'][value='" + value+"']").prop("checked",true);
						}

						});
						
					 }
				}
				else{
					$.each(mergedState["query"], function(key, value) {
						if(key != "retweet" && key !="quote" && key!="source" && key!="collection") 
						{
							$("#searchform [name='"+key+"']").val(value);
						}
						else if(key == "collection")
						{
							$("#collections").val(value.split(";")).trigger('change');
						}
						else
						{
							$("input[name='"+key+"'][value='" + value+"']").prop("checked",true);
						}

						});
					
				}
			
				
				tabstats = {0:true,1:false,2:false,3:false,4:false};
			//	analyzeDataTable.table.rows().remove().draw( );
				mainTimeline.init();
	         
				mainTimeline.update(false, null, false);
				

				twitterDataTable.update(); // inside it  i handle the tabs because I need to get back result for percentage 
				if(exportenabled==false){$('#save').css("display", "none");}
				
				return false;
					
				
}