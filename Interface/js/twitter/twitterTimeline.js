

var timeline = function(div, type, title){
	this.div = div;
	this.type = type;
	this.title = title;

}

timeline.prototype =  {	

		init : function() {
			
				// rotation -45 ne marche pas dans les navigateur à l'inatheque 
				// il faut tester en mac + safari 
				rot = -45
				var nAgt = navigator.userAgent;
				
				if ((verOffset=nAgt.indexOf("Firefox"))!=-1) {
					 fullVersion = nAgt.substring(verOffset+8);
					 if(fullVersion.indexOf("3.5")!=-1)
					 {
						rot = 0
					 }
				}
				
				
				console.log(this.div);
				if(this.type == "main") {
					$(this.div).highcharts('StockChart',getMainTimelineProp(this)); }
				else if (this.type == "analyze"){
					$(this.div).highcharts(getAnalyzeTimelineProp()); }
				else if (this.type == "topdashboard"){
					$(this.div).highcharts(getTopDashboardTimelineProp(rot)); }
				else if (this.type == "topdashboard_emojis"){
					$(this.div).highcharts(getTopDashboardTimelinePropEmojis()); }
				else if (this.type == "dashboard"){
					$(this.div).highcharts(getStatsDashboardTimelineProp(this.title)); }
				else if (this.type == "mainTrends"){
					$(this.div).highcharts(getTrendsTimelineProp()); }
				
		},
			
			
		update : function(fromExtremes,dtype,onlynavigator) {
				
				if(this.type == "main")
				{
					updateTimeline(this,fromExtremes,onlynavigator);
				}
				else if(this.type == "mainTrends")
				{
					updateTrendsTimeline(this);
				}
				else {
					
					updateDashboardTimeline(this, dtype);
				}
				
			
			}

		
};


function updateTrendsTimeline(dtimeline)
{
	var state = StateManager.getState();
	var chart = $(dtimeline.div).highcharts(); 
	chart.showLoading('Chargement des données...');
	var query = state["query"];	

	var trendstimelinerequest = createPromiseRequest(basews + "/ppc/ws/trends_timeline",query,"json");

	
	trendstimelinerequest.then(
							
				function(response)
						{
							//overall timeline
							// not to add another time 
							console.log(response);
							/* // var chart = $("#container_trends_timeline").highcharts();
							// var serie = getTrendsDatafromResponse(response, "scatter", "Trends France", true, Highcharts.getOptions().colors[0])	
							// chart.addSeries(serie,true);
						    // chart.hideLoading();	 */
						},
						function(reason) {
							console.error('Handle rejected promise ', reason);
							displayError();
							
				
					});
		
		
	
	
	
}

function updateDashboardTimeline(dtimeline,dtype)
{
	var state = StateManager.getState();
	var chart = $(dtimeline.div).highcharts(); 
	chart.showLoading('Chargement des données...');
	var query = state["query"];	
	query["dashboard_type"] = dtype;
	query["dashboard_type"] = dtype;
	console.log(query)
	var dashboardrequest = createPromiseRequest(basews + "/ppc/ws/dashboard",query,"json");
	//TODO SET STATE NOT QUERY UPDATE BUT UPDATE BODY
	
	dashboardrequest.then(
							
				function(response)
				{	
						
						var dash = getPieDatafromResponse(response,dtype,true);
						console.log(dash);
						chart.addSeries(dash[0],true);
						chart.hideLoading();
						console.log(dtype,dash[1],dash[2],totalresults);
						if(dtimeline.title !=undefined) // for pies
						{
							percent = ((dash[1]/ totalresults) * 100).toFixed(2)//should not happen but sometimes 100.03 etc happens
							if(percent > 100)
							{ percent = 100.00;}
							chart.setTitle({text:   dtimeline.title },{text: '<span style="font-size: 10px"> Info trouvée dans ' + percent + '% de tweets </span>'}); 
							
						}
						else
						{
							
							chart.setTitle(null,{text: '<span style="font-size: 10px"> ' +  ((dash[2]/ totalresults) * 100).toFixed(2) + '% des tweets ne contiennent pas cette information</span>'});
						}
						
					
				},
				function(reason) {
					console.error('Handle rejected promise updateDashboardTimeline', reason);
					displayError();
					$( "#content" ).hide();
		
	});
		
		
	
	
	
}

function listMedia(response)
{
	
	for (var i = 0; i < response.length; i++) { 
		var div =  '<li><figure><figcaption> Top ' + i + '<p>' + response[i].doc_count + '</p></figcaption><img src=\"' + response[i].key + '\"/></figure></li>'
	
		$('#gridsection ul').append(div);
		$('#slidesection ul').append(div);

	}
	
}

function getStatsDashboardTimelineProp(title)
{
	 var prop = {
            chart: {
                plotBackgroundColor: null,
                plotBorderWidth: null,
                plotShadow: false,
                type: 'pie'
            },
            title: {
                text:title
            },
			 credits: {
				enabled: false
			},
			exporting:{enabled:exportenabled},
            tooltip: {
                pointFormat: ' {point.y} (<b>{point.percentage:.1f}%</b>)'
            },

            plotOptions: {
                pie: {
					size: 150,
                    allowPointSelect: true,
                    cursor: 'pointer',
					dataLabels: {
						enabled: true,
						format: '{point.name} <b>{point.percentage:.1f}%</b>',
						style: {
							color: (Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black'
						}
					},
                    showInLegend: true
                }
            },
			legend: {
              enabled: false,
              layout: 'vertical',
              align: 'right',
              verticalAlign: 'middle',
			  itemMarginTop: highmargin,
			  itemMarginBottom: highmargin



          }
	 
	 };
	 
	 return prop;
}



function getTopDashboardTimelinePropEmojis()
{
	var prop = {
		
			chart: {
	 			 type: 'column',
				// height: 500
	 		  },
			title: {
	              text: null
	          },  
	        xAxis : {
	        	 type:'category',
	        		 labels: {

	                    step: 1,
						//rotation: -45,
						useHTML: true,
						formatter: function(){
				
								return '<img style="max-width:30px;max-height:30px;"  src="' + basews + '/images/emoji/72x72/' + this.value +'.png\"/> <br>' + this.value;
								//return this.value ;
										 
									 
								                        
							
						
	                 }
					 }
	          },
	          
			  exporting:{enabled:exportenabled},

	          tooltip: {
					shared: false,
					useHTML: true
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
				enabled: false,
				layout: 'vertical',
				align: 'left',
				verticalAlign: 'middle',
				itemMarginTop: highmargin,
				itemMarginBottom: highmargin


					
			}
	          
	  
	};
	
	
	return prop;
	
	
}	


function getTopDashboardTimelineProp(rot)
{
	var prop = {
		
			chart: {
	 			 type: 'column',
				// height: 500
	 		  },
			title: {
	              text: null
	          },  
	        xAxis : {
	        	 type:'category',
	        		 labels: {

	                    step: 1,
						rotation: -45,
						formatter: function(){
							
								
				
							    if(this.value.indexOf("http")!=-1)
									this.value = this.value.replace("http://","").replace("www.","")
								else if(this.value.indexOf("www.")!=-1)
									this.value = this.value.replace("www.","")
								if (this.value.length > 15){
									return this.value.substr(0,15) + "...";
								}else{
									return this.value
								}                        
							}
						
	                 }
	          },
	          
			  exporting:{enabled:exportenabled},

	          tooltip: {
					shared: false,
					useHTML: true,
					formatter: function() {
					//	console.log( this.series.name);
						if( this.series.name=="media")
							return '<img style="display:block;max-width:500px;max-height:500px; width: auto; height: auto;"  src=\"'+ this.point.name +'\"/>';
						else
							return "<b>" + this.point.name + "</b> apparait dans " + ((this.point.y/totalresults)*100).toFixed(2) + "% des tweets";

						
						
					},

					positioner: function (labelWidth, labelHeight, point) {
								var tooltipX, tooltipY;
							
								if(this.chart.series[0].name == "media")
								{
									tooltipX = 400;
									tooltipY = 50;
								}
								else
								{
									var tooltipX, tooltipY;
									if (point.plotX + labelWidth > this.chart.plotWidth) {
										tooltipX = point.plotX + this.chart.plotLeft - labelWidth +20;
									} else {
										tooltipX = point.plotX + this.chart.plotLeft -20;
									}
									tooltipY = point.plotY + this.chart.plotTop - 2*labelHeight ;
								}
								
								
								
								return {
									x: tooltipX,
									y: tooltipY
								};
							}
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
					  cursor: 'pointer',
	                  grouping: false,
	                  stacking: 'normal',
	                  pointPadding: 0.2,
	                  allowPointSelect: true,
	                  minPointLength: 3,
	                  states: {
	                      select: {
	                          color: null,
	                          borderWidth: 3,
	                          borderColor: 'white'
	                      }
	                  },
					  events: {
						click: function(event) {
							console.log(event.point.name);
						//	window.prompt("Copy to clipboard: Ctrl+C, Enter", event.point.name);
						  		 
						 
						
						}
					  }
	              }
	              
	          },
			legend: {
				enabled: false,
				layout: 'vertical',
				align: 'left',
				verticalAlign: 'middle',
				itemMarginTop: highmargin,
				itemMarginBottom: highmargin


					
			}
	          
	  
	};
	
	
	return prop;
	
	
}	


function getTrendsTimelineProp()
{
	var prop = {
			
				
				chart: {
	
					reflow: false ,
					zoomType: 'x'
				},
				
				title: {
					text: null
				},
				
				xAxis: {
						type: 'datetime',

						},
				
				yAxis: {
					labels: {
						enabled: false
					},
					tickInterval: 1,
					
					startOnTick: true,
					endOnTick: true,
					title: {
						text: null
					},
					minPadding: 0.2,
					maxPadding: 0.2
				},
				
				legend: {
					enabled: false
				},
				
				
				
				plotOptions: {
					line: {
						lineWidth: 10,
						marker: {
							enabled: false
						},
						dataLabels: {
							enabled: true,
							align: 'left',
							formatter: function() {
								return this.point.options && this.point.options.label;
							}
						}
					}
				},
				
				credits: {
					enabled: false
				},
				exporting: {
					enabled: false
				}
				

				
				
		  
		}
	
	return prop;
	

}



function getAnalyzeTimelineProp()
{
	
	var prop = {
			  chart: {
					 type:'spline',
			         reflow: true,
			         zoomType: 'x'  
			       
			     },
		      title: {
		          text: ""
		      },
		      exporting:{enabled:exportenabled},
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
		              align: 'left'
		          }
		         
		      },
		      legend: {
		            layout: 'vertical',
		            align: 'right',
		            verticalAlign: 'middle',
		            borderWidth: 0,
					itemMarginTop: highmargin,
					itemMarginBottom: highmargin

		        },
		       yAxis: {
		            title: {
		                text: null
		            }
		        },
		      tooltip: {
		    	 // xDateFormat: '%d-%m-%Y hh:MM',
		    	  shared: true,
		    	  crosshairs: true
		      },
		      plotOptions: {

				series: {
					connectNulls: true
					}
		        },

			  
		     // series: serie
		  };
	return prop;
	
}
function getMainTimelineProp(timeline)
{
	var min = StateManager.getState()["query"]["date_start"];
	if( min == undefined)
	{
		min = 1388534400000;
	}
	var prop =  {
						chart: {
							type: 'spline',
							reflow: true,
							zoomType: 'x'
							
						},


						xAxis: {
							type: 'datetime',
							labels: {
								 // format: '{value:%Y-%m-%d}',
								 // rotation: 45,
								  align: 'left'
							  },
							  ordinal: false,
							  events: {	 
								afterSetExtremes : function(e){afterSetExtremes(e,timeline);} 
						
							},
						},
						scrollbar: {
							liveRedraw: false
						},
						
						navigator : {
								adaptToUpdatedData: false,

								series:{
									includeInCSVExport: false
								},

								xAxis: {
									type: 'datetime',
									ordinal: false
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
							selected : 4,
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
									},
									{
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
							   
								stacking: 'normal',
								tooltip: {
									dateTimeLabelFormats: {
										 millisecond: '%A, %b %e, %H:%M:%S.%L',
										 second: '%A, %b %e, %H:%M:%S',
										 minute: '%A, %b %e, %H:%M',
										 hour: '%A, %b %e, %H:%M',
										 day: '%A, %b %e, %Y',
										 week: 'Semaine du %d/%m/%Y',
										 month: '%B %Y',
										 year: '%Y',
									}
								}
								
							},
								line: {
								connectNulls: false
								},
							series:{
								 events: {
										legendItemClick: function(event) {
											 var series = this.yAxis.series;
											/*if(event.target.name == "Estimation")
											 {
													 var data = []
													 for (var i = 0; i < series[0].yData.length; i++) {
														 // adding estimation to ina data 
														data.push([series[2].xData[i] ,series[2].yData[i] + series[0].yData[i]]);
														
													};
													series[2].setData( data,true);
											}*/
												
											 
										}
									},
							//	pointStart: Date.UTC(2014, 0, 1),
								// Bad idea : see column.tooltip.dateTimeLabelFormats instead
								// dataGrouping: {
									// enabled: true,
									// forced: true,
									// approximation : "sum",
									// dateTimeLabelFormats: {
										 // millisecond: ['%A, %b %e, %H:%M:%S.%L', '%A, %b %e, %H:%M:%S.%L', '-%H:%M:%S.%L'],
										 // second: ['%A, %b %e, %H:%M:%S', '%A, %b %e, %H:%M:%S', '-%H:%M:%S'],
										 // minute: ['%A, %b %e, %H:%M', '%A, %b %e, %H:%M', '-%H:%M'],
										 // hour: ['%A, %b %e, %H:%M', '%A, %b %e, %H:%M', '-%H:%M'],
										 // day: ['%A, %b %e, %Y', '%A, %b %e', '-%A, %b %e, %Y'],
										 // week: ['Semaine du %d/%m/%Y', '%A, %b %e', '-%A, %b %e, %Y'],
										 // month: ['%B %Y', '%B', '-%B %Y'],
										 // year: ['%Y', '%Y', '-%Y']
									 // }
								// }
							}
						    

						},
						exporting: {
								
								buttons: {
									contextButton: {
										enabled:exportenabled
																	
									},
									customButton: {
										enabled:true,
										text: 'Recherche dans cette période',
										x: -300,
										onclick: function () {
											updateSearchbyInterval(this);
										}
									}
								}
							}
						
	};
	return prop;
	
}	
	
function updateSeperatedTimelines(chart, fromExtremes,onlynavigator )
{
				var state = StateManager.getState();
				var query = state["query"];	
				if(fromExtremes){
		
					extremes = chart.xAxis[0].getExtremes();
					query["date_start"] = Math.round(extremes.min);	
					query["date_stop"] = Math.round(extremes.max);	
				}				
				
			//	query["max_timeline_buckets"] = 2;
			//	query["exact_interval"] =  true;
				
				if(!fromExtremes )
				{
			
					query["timeline_type"] = "all";
					var timelinerequest = createPromiseRequest(basews + "/ppc/ws/timeline",query,"json");
			//		console.log(chart.options.navigator.series[0]);
					timelinerequest.then(
							
							function(response)
							{
									
									var timeline = getTimelineDatafromResponse(response, "column", "all", false);
									for (var i = 0; i < chart.series.length; i++) {
										console.log(chart.series[i].name);
										if(chart.series[i].name == 'Navigator')
										{
											chart.series[i].setData( timeline["data"],true);
										}
									};
									
								
							},
							function(reason) {
								console.error('Handle rejected promise updateSeperatedTimelines', reason);
								displayError();
								
					
						});
			
						
				}
				
				
				
				// TODO: when(req1, req2) then
				if(!onlynavigator)
				{
					query["timeline_type"] = "ina";
					var timelinerequest = createPromiseRequest(basews + "/ppc/ws/timeline",query,"json");
					var inarequest = timelinerequest.then(
							
							function(response)
							{
									var timeline = getTimelineDatafromResponse(response, "column", "Tweets archivés", true,"#00BDD3");
									
									if(fromExtremes)
									{
										for (var i = 0; i < chart.series.length; i++) {
											if(chart.series[i].name == 'Tweets archivés')
											{
												chart.series[i].setData( timeline["data"],true);
											}
										};
										
									
									}
									else
										chart.addSeries(timeline,true);
									//TODO  I could not do the().then 
									query["timeline_type"] = "ext";
									var timelinerequest = createPromiseRequest(basews + "/ppc/ws/timeline",query,"json");
									timelinerequest.then(
											
											function(response)
											{
												    console.log(response);
													var timeline = getTimelineDatafromResponse(response, "column", "Source externe", true,"#8FCA06");
													if(fromExtremes)
													{
														for (var i = 0; i < chart.series.length; i++) {
															if(chart.series[i].name == 'Source externe')
															{
																chart.series[i].setData( timeline["data"],true);
															}
														};
													}
													else
														chart.addSeries(timeline,true);
													
													
													query["timeline_type"] = "test_estimation_multi_sum2";
													//if(query["hashtags"] != "")
													{
														var timelinerequest = createPromiseRequest(basews + "/ppc/ws/timeline",query,"json");
														timelinerequest.then(
																
																function(response)
																{
																	
																		//console.log(response);
																		var timeline = getTimelineDatafromResponse(response, "spline", "Moyenne mobile", false,"red");
																		if(fromExtremes)
																		{
																			
																			
																			
																			for (var i = 0; i < chart.series.length; i++) {
																				if(chart.series[i].name == 'Moyenne mobile')
																				{
																					chart.series[i].setData( timeline["data"],true);
																				}
																				
																			};
																			
																		}
																		else
																			chart.addSeries(timeline,true);
																		
																		
																				query["timeline_type"] = "test_estimation_savgol5min2";
																				var timelinerequest = createPromiseRequest(basews + "/ppc/ws/timeline",query,"json");
																				timelinerequest.then(
																
																					function(response)
																					{
																						
																							//console.log(response);
																							var timeline = getTimelineDatafromResponse(response, "spline", "Savitzky-Golay", false,"blue");
																							if(fromExtremes)
																							{
																								
																								
																								
																								for (var i = 0; i < chart.series.length; i++) {
																									if(chart.series[i].name == 'Savitzky-Golay')
																									{
																										chart.series[i].setData( timeline["data"],true);
																									}
																									
																								};
																								
																							}
																							else
																								chart.addSeries(timeline,true);
																								
																						
																					},
																					function(reason) {
																						console.error('Handle rejected promise estimation', reason);
																						displayError();
																						$( "#content" ).hide();
																			
																				});	
																},
																function(reason) {
																	console.error('Handle rejected promise estimation', reason);
																	displayError();
																	$( "#content" ).hide();
														
															});	
													}
												
												
											},
											function(reason) {
												console.error('Handle rejected promise ext ', reason);
												displayError();
												$( "#content" ).hide();
									
										});	
									chart.hideLoading();							
									$( "#content" ).show();	
											
							},
							function(reason) {
								console.error('Handle rejected promise ina', reason);
								displayError();
								$( "#content" ).hide();
								$("#searchform :input").prop("disabled", false);
								$("#all").removeClass("diary");
							});

					
				}
				
}

function updateTimeline(maintimeline,fromExtremes,onlynavigator)
{
		var chart = $(maintimeline.div).highcharts();
		chart.showLoading('Chargement des données...');
		console.log(timelinebysource)
		if(timelinebysource)
			updateSeperatedTimelines(chart,fromExtremes,onlynavigator)
		else
			updateMainTimeline(chart,fromExtremes,onlynavigator)
		
}

	
function updateMainTimeline(chart,fromExtremes,onlynavigator)
{
		var state = StateManager.getState();
		var query = state["query"];	
		if(fromExtremes){

			extremes = chart.xAxis[0].getExtremes();
			console.log(extremes.min);
			query["date_start"] = Math.round(extremes.min);	
			query["date_stop"] = Math.round(extremes.max);	
		}	

		
		query["timeline_type"] = "all";	
		console.log("new timeline request")
		var timelinerequest = createPromiseRequest(basews + "/ppc/ws/timeline",query,"json");
		
		

		timelinerequest.then(
				
				function(response)
				{
					if(!onlynavigator)
					{	var timeline = getTimelineDatafromResponse(response, "spline", "Tweets archivés", true,"#00BDD3");
						if(fromExtremes)
						{
							for (var i = 0; i < chart.series.length; i++) {
								if(chart.series[i].name == 'Tweets archivés')
								{
									chart.series[i].setData( timeline["data"],true);
								}
							};
							
						
						}
						else
							chart.addSeries(timeline,true);
						
											/*		query["timeline_type"] = "test_estimation_elections_sum_00";
												//	if(query["hashtags"] != "")
													{
														var timelinerequest = createPromiseRequest(basews + "/ppc/ws/timeline",query,"json");
														timelinerequest.then(
																
																function(response)
																{
																	
																		//console.log(response);
																		var timeline = getTimelineDatafromResponse(response, "spline", "Estimation", false,"red");
																		if(fromExtremes)
																		{
																			
																			
																			
																			for (var i = 0; i < chart.series.length; i++) {
																				if(chart.series[i].name == 'Estimation')
																				{
																					chart.series[i].setData( timeline["data"],true);
																				}
																				
																			};
																			
																		}
																		else
																			chart.addSeries(timeline,true);
																		
																	
																	
																},
																function(reason) {
																	console.error('Handle rejected promise estimation', reason);
																	displayError();
																	$( "#content" ).hide();
														
															});	
													}*/
					}

					chart.hideLoading();							
					$( "#content" ).show();					
					
				},
				function(reason) {
					console.error('Handle rejected promise updateMainTimeline', reason);
					displayError();
					
		
				});
			
			
	
	
				
		
				
	
}
function getTimelineDatafromResponse(response, type, name, isvisible,color)
{

	var data = [];

	for (var i = 0; i < response.length; i++) { 
	
		if(name=="Tweets archivés" || name=="Source externe")
		{
			
			data.push([response[i].key/1,response[i].doc_count]);
		}
		else
		{
			if(response[i].doc_count == 0)
				data.push([response[i].key/1,null]);
			else if(response[i].doc_count !=null)
			{
				data.push([response[i].key/1,response[i].doc_count]);
			}
		}
			
	
	}
	
	
	
	if(response.length == undefined)
		isvisible = false;
	
	var serie = {
				  type: type,
				  name: name,
				  data: data,
				  visible: isvisible,
				  tooltip: {
					  valueDecimals: 0
				  },
				  color: color,
				  cursor: 'pointer'
			};
			
	return serie;
	
}

function getTrendsDatafromResponse(response, type, name, isvisible,color)
{

		var data = [];

		
		for (var i = 0; i < response.length; i++) { 

				data.push([response[i]/1,1]);
				
			}
				

		
		
	    if(response.length == undefined)
		isvisible = false;
	
		var serie = {
					  type: type,
					  name: name,
					  data: data,
					  visible: isvisible,
					  tooltip: {
						  valueDecimals: 0
					  },
					  color: color,
					  cursor: 'pointer'
				};
				
		return serie;

	
}

function getPieDatafromResponse(response, dtype, isvisible)
{
	var data = [];
	var sum = 0;
	var nullsum = 0;
	//console.log(response);
	type = dtype;
	for (var i = 0; i < response.length; i++) { 
		
		if(response[i].key == 'T' || response[i].key == 'F' )
		{
			if(response[i].key == 'T'|| response[i].key =="retweet") type = "Retweet"
			else type = "Tweet"
			
			data.push({'name':type,'y':response[i].doc_count/1});
			sum += response[i].doc_count/1;
		}
		else if(response[i].key=='null' || response[i].key=='missing'  )
		{
			//console.log("nullmissing",dtype,response[i].doc_count);
			nullsum += response[i].doc_count/1;
			
		}
		else if(response[i].key=='others' )
			sum += response[i].doc_count/1;
		else 
		{
		
			data.push({'name':response[i].key,'y':response[i].doc_count/1});
			sum += response[i].doc_count/1;
		}
		
			
			
		
		
	}
	if(response.length == undefined)
		isvisible = false;
	
	var serie = {
		colorByPoint: true,

		name: type,
		data: data
	};
			
	return [serie,sum,nullsum];
	
}
	
function afterSetExtremes(e, timeline) {

	console.log(Math.round(e.min) + "-" + Math.round(e.max));
    var state =  StateManager.getState();


	//console.log(e.trigger); important zoom / navigator or undefined if set from data
	if(e.trigger != undefined)
		timeline.update(true, null, false);
		
		
}

