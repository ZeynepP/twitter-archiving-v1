var analyzeDataTable = (function(div){
	var table;
	var thisDataTable = {

			init : function() {
						if ( !$.fn.dataTable.isDataTable( div ) ) {
							this.table = $(div).DataTable(
								{ 	 "paging": true,
									 "lengthChange": false,
									 "searching": false,
									 "ordering": true,
									 "info": true,
									 "autoWidth": false,
									 "sDom": 'l'
									 
								}
							);
							
							$('#tableAnalytics tbody').on( 'click', 'tr', function () {
								
								$(this).toggleClass('selected');
							} );
						}
					},
					
			check : function(key, type){
				var ret = false;
				key = key.toLowerCase();
				this.table
					.rows()
					.data()
					.each(function (value, index) {
			
					
					 console.error(key +":"+type, value,index);
					 if(value == (key +":"+type)){
						 ret = true;
					 }
					 
				 }); 

				// this.table
				// .column( 0 )
				// .data()
				// .each( function ( value, index ) {
					// console.log(value, key, type, index);
					// value = value.replace("@","").replace("#","");
					// if(value == key ) {
						// ret = true;
					// }
					
				// } );
				return ret;
				
				
			},
			addRow : function(key, type) {
				
				key = key.toLowerCase();
				var index = Math.floor((Math.random() * 10) );
				console.log("aaaaaaaaaaaaaa");
				console.log(thisDataTable.check(key, type));
				if( !thisDataTable.check(key, type))
				{
					console.log(key + ":" + type  + " drawing")
					
					this.table.row.add([key + ":" + type ]).draw();
					analyze_one(type, key,index);
				}
				$("#analyticsform")[0].reset();

				return false;
			},
			
			deleteSelecetedRow : function()
			{
				
			
				var size = this.table.rows('.selected').data().length;
			
				for(var i = 0; i<size;i++)
				{
					
					var seriename = this.table.rows('.selected').data()[i];
					removeSerie(seriename);
					var analyzestate = StateManager.getState()["analyze"];
					seriename = seriename[0].split(":");
					delete analyzestate[seriename[0]];
					StateManager.setState({"analyze" : analyzestate}, false);
					document.title = "analyze deleteSelecetedRow one set state false";
				}
			
				this.table.rows('.selected').remove().draw( false );

			}
					
			
	
	}
	return thisDataTable;})('#tableAnalytics');


function updateAnalyzes()
{
	analyzeDataTable.table.rows().remove().draw( );
	
	console.log("updateAnalyzes:", "state changed")
	var analyze = StateManager.getState()["analyze"];
			$.each(analyze, function(key, value) {
				console.log(key, value);
				analyzeDataTable.addRow(key , value );
			});
	
	
}


// TODO; for users 
// TODO move ES SETTINGS TO GLOBAL VALUES
// TODO GIVE DIV 	AS PARAMETER IT IS NOT OK TO REPEAT
function analyze_one(type, value,index)
{
	
		var analyze = StateManager.getState()["analyze"];
		console.log(analyze);
		if(analyze == undefined)
			analyze = {};
		
		analyze[value] = type;
		StateManager.setState({"analyze" : analyze}, false);
		//document.title = "analyze one set state false";
		var query = StateManager.getState()["query"];
		console.log("here analyze_one");
		var prefix = "";
		var namechart = "chart_" + type + "_" + value;
	    namechart = namechart.replace(/\./g, '_');
		console.log(namechart);
		if( type == 'Hashtag')
		{
			query["analyze_request"] ={ "hashtags": value.toLowerCase()};
	        prefix = "#";
			
		}
		else if(type == 'User')
		{
			
			query["analyze_request"] ={ "users": value.toLowerCase()};
		}
		else if(type == 'Mention')
		{
			query["analyze_request"] ={ "mentions": value.toLowerCase()};
			prefix = "@";
		}
		else if(type == 'Url')
		{
			query["analyze_request"] ={ "urls": value.toLowerCase()};
		}
		else if(type == 'Texte')
		{
			query["analyze_request"] ={ "texte": value.toLowerCase()};
		}
			
		
		
		//create empty timeline and then we add series one by one
		var chart = $("#container_timelines_overall").highcharts();
		console.log(chart);
		if(chart == undefined)
		{	
			var analyzechart  = new timeline("#container_timelines_overall","analyze");
			analyzechart.init();
			
		}
		console.log("Sending analyze request", query);
		var timelinerequest = createPromiseRequest(basews + "/ppc/ws/analyze",query,"json");
					
		timelinerequest.then(
						
						function(response)
						{
							//overall timeline
							// not to add another time 
							var chart = $("#container_timelines_overall").highcharts();
							for (var i = 0; i < chart.series.length; i++) {
										
										if(chart.series[i].name == prefix+value)
										{
											chart.series[i].remove(true);
										}
							};
							
							
							var serie = getTimelineDatafromResponse(response.timeline, "spline", prefix+value, true, Highcharts.getOptions().colors[index])	
							chart.addSeries(serie,true);
							
							// one by one timeline
							
							//$('<div id="' +  namechart + '">').appendTo("#container_timelines_onebyone");
				
							//console.log(namechart);
							//var oneanalyzechart  = new timeline(namechart,"analyze");
							//oneanalyzechart.init();
							//$(namechart).highcharts().addSeries(serie,true);
							
						},
						function(reason) {
							console.error('Handle rejected promise ', reason);
							displayError();
							
				
					});

}


function removeSerie(seriename)
{

    console.log(seriename);
	seriename = seriename[0].split(":");
	if($("#container_timelines_overall").highcharts() != undefined)
	{
			var seriesLength = $("#container_timelines_overall").highcharts().series.length;
		
		    for(var i = seriesLength - 1; i > -1; i--)
		    {
				var prefix ='';
				if(seriename[1] == 'Mention') prefix = "@";
				else if(seriename[1] == 'Hashtag') prefix = "#";
		        //chart.series[i].remove();
		        if($("#container_timelines_overall").highcharts().series[i].name == prefix + seriename[0])
		        	{
		        		$("#container_timelines_overall").highcharts().series[i].remove();
						var namechart = "chart_" + seriename[1] + "_" + seriename[0].replace("#","").replace("@","");
						namechart = namechart.replace(/\./g, '_');
						console.log(namechart);
						$("#" +namechart ).remove();
		        	}
		    }
			
			
	}

	
}

