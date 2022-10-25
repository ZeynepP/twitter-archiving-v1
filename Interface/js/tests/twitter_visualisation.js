/**
 * http://usejsdoc.org/
 * zpehlivan@ina.fr
 * 
 * 
 */
 
 
function generateNetworkGraph()
{
	
	$.ajax({
						url: basews + "/ppc/file/network",
						contentType: "application/json; charset=utf-8",
					
						type: "POST",
						data: JSON.stringify(data),

						success: function (response) {
								
								var d = new Date();
								var n = d.getTime();
								//
								//loadGraph(response);
								var blob = new Blob([response], {type: "text/xml;charset=utf-8"});
                                saveAs(blob, "network_" + n +".gexf");
								
						},
						
						error: function(textStatus, errorThrown) {
								$( "#resultsbanner").html(errorThrown);
							
						}
						
					});
	return false;
	
}
function dashboardClickEx(type)
{

		data["query_type"] = 1;
		data["dashboard_type"] = type;
		console.log(sessioninfo[type]);
		if(sessioninfo[type] != 1)
		{
			console.log("Sending ajax request");
			$.ajax({
																
				url: basews+"/ppc/ws/dashboard",
				contentType: "application/json; charset=utf-8",
				dataType: "json",
				type: "POST",
			
				data: JSON.stringify(data),
				success: function (response) {
					console.log(type);
					draw_dashboard(response,type);
					
				}
				,
				 error: function(textStatus, errorThrown) {
								$( "#resultsbanner").html(errorThrown);
							
						}
			});
		}
		else
		{
			draw_dashboard(null,type);
		}
		data["query_type"] = 0;
		data["dashboard_type"] = "";
		sessioninfo[type] = 1;
	
}

function loadMore(base, actual)
{
	
		if(actual == 0) // means coming from load more
		{
			actual =  (page * max_perpage) + max_perpage;
		}
		console.log(actual );
	//	alert(actually_retrieved);
		
		
		if( actual < actually_retrieved  )
		{

			for (var i = 0; i <max_perpage; i++) {
				var id = actual + i;
				$("#" + id).show();
			}
			page ++;

		}
		else
		{
			data["from"] = actual;
			data["size"] = es_size;
			data["timelineType"] = -5;		
			$.ajax({
				url: base + "/ppc/ws/search",
				contentType: "application/json; charset=utf-8",
				dataType: "json",
				type: "POST",
				data: JSON.stringify(data),
				success: function (response) {
						actually_retrieved+=fillTweetsDatatable(response);
						var pagestate = loadMore(base,actual);
						if( response.hits.length == 0)
						{
							$("#moreresultsdivtext").hide();
						}
				},
				error: function(textStatus, errorThrown) {
								$( "#resultsbanner").html(errorThrown);
							
						}
			});

			delete data["timelineType"];
		}
		return page;
}

//I could not do all requests at the same time es was dead each time 
//thus seperated by type
function draw_dashboard(body,type)
{
	
		console.log(body);
		if(type =="hashtags")
		{
			if(body!=null)
			{
				var cat =  flatten_dashboard(body.tophashtagso);
				console.log(cat[1]);
				draw_dashboard_bar_stacked('#container_hashtags',cat[1],'Top #Hashtags'); 
			}
		    $('#container_hosts').hide();
			$('#container_mentions').hide();
			$('#container_users').hide();
			$('#container_hashtags').show();
			
			
		}
		else if(type == "urls")
		{
			if(body!=null)
			{
				var cat = flatten_dashboard(body.topurlso);
				draw_dashboard_bar_stacked('#container_hosts', cat[1],'Top Liens'); 
			}
			$('#container_hashtags').hide();
			$('#container_mentions').hide();
			$('#container_users').hide();
			$('#container_hosts').show();
		}
		else if(type == "mentions")
		{
			if(body!=null)
			{
				var cat = flatten_dashboard(body.topmentionso);
				draw_dashboard_bar_stacked('#container_mentions',cat[1],'Top @Mentions');
			}
			$('#container_hashtags').hide();
			$('#container_hosts').hide();
			$('#container_users').hide();
			$('#container_mentions').show();
		}
		else if(type == "users")
		{
			if(body!=null)
			{
				var cat = flatten_dashboard(body.topuserso);
			
				draw_dashboard_bar_stacked('#container_users',cat[1],'Top Utilisateur'); 
			}
			$('#container_hashtags').hide();
			$('#container_hosts').hide();
			$('#container_mentions').hide();
			$('#container_users').show();
			
		}
		
		
		
		
		
		
		
		
		
	
	
}


// TODO; for users 
// TODO move ES SETTINGS TO GLOBAL VALUES
// TODO GIVE DIV 	AS PARAMETER IT IS NOT OK TO REPEAT
function analyze_oneEx(type, value, popup)
{
	
		console.log("here analyze_one");
		var prefix = "";
		var namechart = "chart_" + type + "_" + value;
	    namechart = namechart.replace(/\./g, '_');
		console.log(namechart);
		if( type == 'Hashtag')
		{
			data["analyze_request"] ={ "hashtags": value};
			prefix= "#";
		}
		else if(type == 'User')
		{
			
			data["analyze_request"] ={ "users": value};
		}
		else if(type == 'Mention')
		{
			data["analyze_request"] ={ "mentions": value};
			prefix="@";
		}
		else if(type == 'Url')
		{
			data["analyze_request"] ={ "urls": value};
		}
			

		data["query_type"] = 2; // analyze
		
		
		if(popup == false)
		{
			//create empty timeline and then we add series one by one
			var chart = $("#container_timelines_overall").highcharts();
			
			if(chart == undefined)
			{
				chart = draw_main_timeline([],"#container_timelines_overall","");
				
			}
		}
		else
		{
			namechart = "pop" + namechart;
		}
		
		$.ajax({
					url: basews + "/ppc/ws/analyze",
					contentType: "application/json; charset=utf-8",
					dataType: "json",
					type: "POST",
					data: JSON.stringify(data),
					success: function (response) {
						var index = Math.floor((Math.random() * 10) );
					    console.log(response);
						var temp =flatten_aggr_result(response.timeline);
						var rcolor = newRandomColor();
						var s= {
								  name: prefix+value,
								  data: temp[0],
								  color: Highcharts.getOptions().colors[index],
							  };
						if(popup == false)
						{
		
								$("#container_timelines_overall").highcharts().addSeries(s,true);
			
								$('<div id="' +  namechart + '">').appendTo("#container_timelines_onebyone");
						}
						else
						{
								$('<div id=' + namechart  +' title= "' +prefix+value +  ' timeline " >	<div id=container_timelines_' + namechart  +' style="width:100%;margin: 0 auto">  </div> </div>').appendTo("#container_popup");
						}
						draw_main_timeline(s,"#" + namechart,"");
						if(popup ){
							$( "#"+ namechart).dialog({width: 800,height:600});
						}
						
						
					},error: function(textStatus, errorThrown) {
								$( "#resultsbanner").html(errorThrown);
							
						}
			});
			
			
			
			
		data["query_type"] = 0;  // set back always to default value
		data["analyze_request"] = {};
	

}

// TODO; for users 
// TODO move ES SETTINGS TO GLOBAL VALUES
// TODO GIVE DIV 	AS PARAMETER IT IS NOT OK TO REPEAT
function analyze()
{
	

	//create empty timeline and then we add series one by one
	draw_main_timeline([],"#container_timelines_overall","");


	var h = "",m = "",u ="";
	
	var i =0;
	analyticstable
    .data()
    .each( function ( value, index ) {
        var namechart = "chart_" + index;
    	if(value[1] == 'Hashtag'){
    		h+=value[0].replace("#","") + ",";
    		var hresult = initES(data["q"],[value[0].replace("#","")],data["mentions"],data["users"],data['retweet'],data["from"],data["to"] ,2,0); 
    		hresult.done(function (body) {
	    		var temp =flatten_aggr_result(body.aggregations.timeline.buckets);
	    		var s= {
				          name: value[0],
				          data: temp[0],
				          color: Highcharts.getOptions().colors[index],
				      };
	    		
	    		$("#container_timelines_overall").highcharts().addSeries(s,true);
	    
	    		$('<div id="' + value[0].replace("#","") + '">').appendTo("#container_timelines_onebyone");
	    		draw_main_timeline(s,value[0],"");
	    		 
	    		 
    		 });
    		
    	}
    	else if(value[1] == 'User'){
    		u+=value[0] + ",";
    		var hresult = initES(data["q"],data["hashtags"],data["mentions"],[value[0]],data['retweet'],data["from"],data["to"] ,2,0);
    		hresult.done(function (body) {
	    		var temp =flatten_aggr_result(body.aggregations.timeline.buckets);
	    		var s = {
				          name: value[0],
				          data: temp[0],
				          color: Highcharts.getOptions().colors[index],
				      };
	    		$("#container_timelines_overall").highcharts().addSeries(s,true);
	    		$('<div id="' + namechart + '">').appendTo("#container_timelines_onebyone");
	    		draw_main_timeline(s,"#"+namechart,"");
	    		
    		});
    		
    	}
    	else if(value[1] == 'Mention'){
    		m+=value[0].replace("@","") + ",";
    		var hresult = initES(data["q"],data["hashtags"],[value[0].replace("@","")],data["users"],data['retweet'],data["from"],data["to"] ,2,0); 
    		
    		hresult.done(function (body) {
	    		var temp =flatten_aggr_result(body.aggregations.timeline.buckets);
	    		var s = {
				          name: value[0],
				          data: temp,
				          color: Highcharts.getOptions().colors[index],
				      };
	    		$("#container_timelines_overall").highcharts().addSeries(s,true);
	    		
	    		$('<div id="' + namechart + '">').appendTo("#container_timelines_onebyone");
	    		draw_main_timeline(s,"#"+namechart,"");
    		});
    		
    	}
    	i= i +1;
    } );
	$('#container_timelines_overall').highcharts().reflow();

	var pageUrl = '?' + $('#searchform').serialize() + "&h="  + h + "&u=" + u +"&m=" + m ;
	
	//added
	//History.pushState(data, 'tweet_analyze', pageUrl);
	
	
	//draw_main_timeline(all,"#container_timelines_hashtags",'#3BBEE3',"Timelines"); 
	   /**
     * In order to synchronize tooltips and crosshairs, override the
     * built-in events with handlers defined on the parent element.
     */
     $('#container_timelines_onebyone').bind('mousemove touchmove', function (e) {
	        var chart,
	            point,
	            i;

	        for (i = 0; i < Highcharts.charts.length; i = i + 1) {
	            chart = Highcharts.charts[i];
	            e = chart.pointer.normalize(e); // Find coordinates within the chart
	            point = chart.series[0].searchPoint(e, true); // Get the hovered point

	            if (point) {
	                point.onMouseOver(); // Show the hover marker
	                chart.tooltip.refresh(point); // Show the tooltip
	                chart.xAxis[0].drawCrosshair(e, point); // Show the crosshair
	            }
	        }
	    });

    /**
     * Override the reset function, we don't need to hide the tooltips and crosshairs.
     */
    Highcharts.Pointer.prototype.reset = function () {
        return undefined;
    };

    /**
     * Synchronize zooming through the setExtremes event handler.
     */
    function syncExtremes(e) {
        var thisChart = this.chart;

        if (e.trigger !== 'syncExtremes') { // Prevent feedback loop
            Highcharts.each(Highcharts.charts, function (chart) {
                if (chart !== thisChart) {
                    if (chart.xAxis[0].setExtremes) { // It is null while updating
                        chart.xAxis[0].setExtremes(e.min, e.max, undefined, false, { trigger: 'syncExtremes' });
                    }
                }
            });
        }
    }

	
//	var result_tag;
//	var hresult = initES('http://wwwxx.ina.fr:xx',data["q"],tags,mentions,data["users"],'all',0,0 ,2,0);
//	var all = [];
//	hresult.done(function (body) {
//
//		all =flatten_aggr(body.aggregations.hashtagtimeline.buckets,tags,all);
//
//		all =flatten_aggr(body.aggregations.mentiontimeline.buckets,mentions,all);
//		
//		draw_main_timeline(all,"#container_timelines_hashtags",'#3BBEE3',"Hashtags distribution over time"); 
//		
//	});
	


	
}


function getTimelineData(body)
{
	
		var temp =flatten_aggr_result(body.timeline);
		var serienav = [];
		
		
		serienav.push({
			  type: 'bar',
			  name: 'Nombre des tweets',
			  data: temp[0],
			  tooltip: {
				  valueDecimals: 0
			  },
			  cursor: 'pointer'
		}) ;
		var serie = [];

		/*if(body.ina_timeline != undefined)
		{
			temp =flatten_aggr_result(body.ina_timeline);
			
			serie.push({
					  type: 'column',
					  name: 'Source d\'Ina',
					  data: temp[0],
					  color:"#00BDD3",
					  visible: true,
					  
					  tooltip: {
						  valueDecimals: 0 
					  },
					  cursor: 'pointer'
				}) ; 
		}*/
		
	
		if(body.ext_timeline != undefined)
		{
			temp =flatten_aggr_result(body.ext_timeline);
			
			serie.push({
					  type: 'column',
					  name: 'Tweets',
					  data: temp[0],
					  color:"#00BDD3",
					  visible: true,
					  tooltip: {
						  valueDecimals: 0
					  },
					  cursor: 'pointer'
				}) ; 
		}
		console.log(body.global_estimated_timeline);
		if(body.global_estimated_timeline != undefined)
		{
			temp =flatten_aggr_result_est(body.global_estimated_timeline);
			
			serie.push({
				  type: 'spline',
				  name: 'Estimation',
				  data: temp[0],
				  color:'red',
				  visible: false,
				  tooltip: {
					  valueDecimals: 0
				  },
				  
				  cursor: 'pointer'
			}) ; 
		}
			
		return [serie,serienav];
	/*		http://jsfiddle.net/gh/get/jquery/1.9.1/highslide-software/highcharts.com/tree/master/samples/stock/demo/candlestick-and-volume/
			http://jsfiddle.net/gh/get/jquery/1.9.1/highslide-software/highcharts.com/tree/master/samples/highcharts/demo/combo-multi-axes/
*/
}

function draw_main_all(body)
{
		//uptade hits div 
		console.log("draw_main_all");
		updateHitsText ( "#resultsbanner" ,  body.search_duration / 1000, body.total_count); 
		var serie   = getTimelineData(body);
		create_main_timeline( "#container_timeline","",serie[0], serie[1]);
		
}




function draw_dashboard_bar(container,serie,title,color)
{
	 $(container).highcharts({
		 chart: {
		    type:'column'
				
		 },
         title: {
            text: title
         },  
         xAxis : {
        	type :"category"
         },
         yAxis: {
        
        	 labels: {
        	       enabled: false
        	   },
        	   lineWidth: 0,
        	   minorGridLineWidth: 0,
        	   lineColor: 'transparent',
        	   minorTickLength: 0,
        	   tickLength: 0,
        	   gridLineColor: 'transparent'
         },
         plotOptions: {
             series: {
                 shadow:false,
                 borderWidth:0,
                 dataLabels:{
                     enabled:true
                     
                 }
             }
         },
         series: serie
         
         
 });
 
 

 
//	chart.showLoading('Getting stat data ....');
//	chart.hideLoading();
//	
//	chart.addSeries(serie, false);
//	 
//	 
//	chart.redraw();

}

function getTweetsFromES(callback, errorCallback,path)
{//
	
		$.ajax({
						url: basews + path,
						contentType: "application/json; charset=utf-8",
						dataType: "json",
						type: "POST",
						data: JSON.stringify(data),
						success: function (response) {
								callback(response);
						},
						error: function(textStatus, errorThrown) {
								errorCallback(errorThrown);
								//$( "#resultsbanner").html("Erreur pour la requete: " + errorThrown);
					
						}
						
				});
	
}


function wordcloudDataTable(datatable, dataTableCallback)
{
	console.log("START wordcloudDataTable");
	if(data["query_type"]== 3)
	{
			getTweetsFromES(
			  function(response) {
				  fillDatatable(datatable,response.wordcloud,dataTableCallback);
				
			}, function(error) {
				$( "#resultsbanner").html("Erreur pour la requete: ");
			},"/ppc/ws/wordcloud");
	} else {
		
		var dataResult = {data: []}
		dataResult.recordsTotal =  0;
		dataResult.recordsFiltered =  0;
		dataTableCallback(dataResult);
	}
	
	
	
}

function twitterDatatableAjax(datatable, dataTableCallback)
{
	console.log("START twitterDatatableAjax");
	var order = datatable.order;
	data["timelineType"] = -5;
	data["sort_field"] = datatable.columns[order[0].column].name;
	data["sort_type"] = order[0].dir;
	data["from"] = datatable.start;
	data["size"] = datatable.length;
	

		getTweetsFromES(
		  function(response) {
			  actually_retrieved+=fillTweetsDatatable(response, dataTableCallback);
			
		}, function(error) {
			
			$( "#resultsbanner").html("Recherche de tweets, veuillez préciser vos critéres de recherche.");
							
		},"/ppc/ws/search");
	/*} else {
		
		var dataResult = {data: []}
		dataResult.recordsTotal =  0;
		dataResult.recordsFiltered =  0;
		dataTableCallback(dataResult);
	}*/
	delete data["sort_field"];
	delete data["sort_type"];
	delete data["timelineType"];
	
	
}

function draw_dashboard_bar_stacked(container,serie,title)
{
	
	 	 $(container).highcharts({
	 		  chart: {
	 			 type: 'column',
	 		       height: 1000
	 		  },
              title: {
	              text: null
	          },  
	          xAxis : {
	        	 type:'category',
	        		 labels: {
	                     step: 1
	                 }
	          },
	          
	          tooltip: {
	              pointFormat: '{series.name}: {point.y}<br/>Total: {point.stackTotal} ({point.percentage:.0f}%) '
	          },
	      yAxis: {
            min: 0,
            title: {
                text: null
            },
            stackLabels: {
                enabled: true,
                style: {
                    fontWeight: 'bold',
                    color: (Highcharts.theme && Highcharts.theme.textColor) || 'gray'
                }
            }
        },  
	         
	          plotOptions: {
	              series: {
	                  grouping: false,
	                  stacking: 'normal',
	                  pointPadding: 0.2,
	                  allowPointSelect: true,
	                  minPointLength: 3,
	                  states: {
	                      select: {
	                          color: null,
	                          borderWidth: 5,
	                          borderColor: 'Blue'
	                      }
	                  }
	              }
	              
	          },
			 legend: {
					enabled: true,
					layout: 'vertical',
					align: 'left',
					verticalAlign: 'middle'
					
				},
	          series: serie
	          
	  });
	 


}

function create_main_timeline(div, title,serie, serienav)
{
	
	var c = $(div).highcharts('StockChart', {
			chart: {
				type: 'column',
				reflow: true,
				zoomType: 'x'
			},

			xAxis: {
				type: 'datetime',
				labels: {
		             // format: '{value:%Y-%m-%d}',
		              rotation: 45,
		              align: 'left'
		          },
				  ordinal: false,
				  events: {	 
					afterSetExtremes : afterSetExtremes
			
				},
			},
			scrollbar: {
				liveRedraw: false
			},
			
			navigator : {
                adaptToUpdatedData: false,
                series : {
                    data : serienav["data"]
                }
            },
			legend: {
					enabled: true,
					layout: 'horizontal',
					align: 'center',
					verticalAlign: 'bottom'
					
				},
			 rangeSelector: {
                enabled: true,
				buttonSpacing: 16,
				buttons: [
					
						{
							type: 'day',
							count: 1,
							text: 'Jour'
						}, {
							type: 'week',
							count: 1,
							text: 'Semaine'
						}, {
							type: 'month',
							count: 1,
							text: 'Mois'
						},{
							type: 'year',
							count: 1,
							text: 'Année'
						},{
							type: 'all',
							text: 'Totalité'
						}]
           
			},
			

			  yAxis: {
		            title: {
		                text: null
		            },
		            labels: {
		                enabled: true,
						align: 'left',
						x: 15
						
		            }
		        },
			plotOptions: {
		        column: {
		           
					stacking: 'normal'
					
		        },
					line: {
					connectNulls: false
					},
		        series:{
					
		        //	pointStart: Date.UTC(2014, 0, 1),
		            dataGrouping: {
			            dateTimeLabelFormats: {
			            	 millisecond: ['%A, %b %e, %H:%M:%S.%L', '%A, %b %e, %H:%M:%S.%L', '-%H:%M:%S.%L'],
			                 second: ['%A, %b %e, %H:%M:%S', '%A, %b %e, %H:%M:%S', '-%H:%M:%S'],
			                 minute: ['%A, %b %e, %H:%M', '%A, %b %e, %H:%M', '-%H:%M'],
			                 hour: ['%A, %b %e, %H:%M', '%A, %b %e, %H:%M', '-%H:%M'],
			                 day: ['%A, %b %e, %Y', '%A, %b %e', '-%A, %b %e, %Y'],
			                 week: ['Semaine du %d/%m/%Y', '%A, %b %e', '-%A, %b %e, %Y'],
			                 month: ['%B %Y', '%B', '-%B %Y'],
			                 year: ['%Y', '%Y', '-%Y']
			             },
						 approximation : "sum"
						 
						 
		            }
		        }
		    },
		   
			series : serie
     });
	 
	 return c;

}




// normal timeline
function draw_main_timeline(s,div, title)
{
		console.log(div);
		var chart1 =  $(div).highcharts({
			  chart: {
			         reflow: true,
			         renderTo:div,
			         zoomType: 'x',
					 width: $(div).width(),
			         events: {
						 load: function(event) {
							//When is chart ready?
							$(document).resize(); 
							}
					}     
			       
			     },
		      title: {
		          text: title
		      },
		     
		      xAxis: {
		    	  type: 'datetime',
				   xAxis: {
						dateTimeLabelFormats: {
							day: '%Y',
							week: '%Y',
							month: '%Y',
							year: '%Y'
						}
					},
					labels: {
		             // format: '{value:%Y-%m-%d}',
		              rotation: 45,
		              align: 'left'
		          }
		         
		      },
		      legend: {
		            layout: 'vertical',
		            align: 'right',
		            verticalAlign: 'middle',
		            borderWidth: 0
		        },
		       yAxis: {
		            title: {
		                text: null
		            }
		        },
//		      yAxis: {
//		          title: {
//		              text: '# Tweets'
//		          }
//		      },
		      tooltip: {
		    	 // xDateFormat: '%d-%m-%Y hh:MM',
		    	  shared: true,
		    	  crosshairs: true
		      },
		      plotOptions: {
		            column: {
		                pointPadding: 0.2,
		                borderWidth: 0
		            },
					line: {
					connectNulls: false
					}
		        },
			  
		     // series: serie
		  });
		if(s.length != 0)
		{
			$(div).highcharts().addSeries(s,true);
		}
		
		return chart1;
}




function fillTweetsDatatable(response,datatableCallback)
{
	  var data = [];

	  var temp = response.hits;
	  console.log("fill datatable");
	  console.log(temp);
	  var rows = [];
	  
	  for (var i = 0; i <temp.length; i++) {
		  var hashtags = '';
		  var tempent =[];
		  var user = temp[i]._source.user.name;
		  
		  var ent = temp[i]._source.urls;
		  tempent =[];
		  var hosts ='';
		  if(ent != undefined)
		  {
			  for (var k = 0; k <ent.length; k++) { 
				  tempent.push( ent[k].host);
				  hosts = tempent.join();
			  }
		  }
		  
		  tempent =[];
		  var mentions ='';
		  ent = temp[i]._source.user_mentions;
		  if(ent != undefined)
		  {
			  for (var k = 0; k <ent.length; k++) { 
				  tempent.push(ent[k].screen_name);
				  mentions = tempent;
			  }
		  }
		  
		  rows.push([
				temp[i]._source.user.profile_image_url,
				temp[i]._source.created_at.replace("T"," ").replace("Z"," "),
				temp[i]._source.text,
				temp[i]._source.favorite_count,
				temp[i]._source.retweeted,
				temp[i]._source.quoted,
				temp[i]._source.retweet_count,
				temp[i]._source.hashtags,
				mentions,
				hosts,
				temp[i]._source.user.screen_name,
				temp[i]._source.user.name,
				temp[i]._source.user.followers_count,
				temp[i]._source.user.statuses_count,
				temp[i]._source.user.location,
				temp[i]._source.user.lang,
				temp[i]._source.user.created_at,
				temp[i]._source.id,
				temp[i]._source.lang,
				temp[i]._source.place.country,
				temp[i]._source.place.name,
					
				]);
		}

		var dataResult = {data: rows}
		dataResult.recordsTotal =  response.total_count;
		dataResult.recordsFiltered =  response.total_count;
		
		//console.log("fillTweetsDatatable ", dataResult);
		
		datatableCallback(dataResult);
	
		console.log("over datatable");
	    return temp.length;
}
