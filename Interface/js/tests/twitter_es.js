
/**
 * http://usejsdoc.org/
 * zpehlivan@ina.fr
 */




function initES(keywords,hashtags,mentions,users,retweets,start,end, type,size,from)
{


	var client = new elasticsearch.Client({
		  host: 'http://xx.ina.fr:xx',
	      log: 'trace',
	      keepAlive: true,
	      requestTimeout:60000
	  });
	
	
	var topk=15;

	return client.search({
		index: 'tweets_paris2',
		//index: 'tweets_rt_paris2',
		type: 'tweets',
		
		body :{
			  "size":size,
			  "from" : from,
			  "sort" : [ { "created_at" : {"order" : "desc"}}],
			  "_source" : [ 'created_at','text','entities','favorite_count','retweeted','retweet_count', 'user.screen_name','user.followers_count','user.statuses_count','user.location','user.lang','user.created_at','id','user.profile_image_url','user.name'],
			  "query" : getQuery(keywords,hashtags,mentions,users,retweets,start,end),
			  "aggs" : getAggragation(topk, type) 
			  // aggs
			}//body
		  
	});
  
	

}
var START_DATE;
var END_DATE;



function getQuery(keywords, hashtags, mentions, users, retweets, start_date, end_date)
{
	if(keywords=='' || typeof keywords == 'undefined') {keywords ='*';}
	if(start_date =='') 
	{
		START_DATE = 821367668000
	}
	else
	{
		var sd = new Date(start_date);
		START_DATE = sd.getTime();
		
	}
	if(end_date == '') 
	{
		var sd = new Date();
		END_DATE = sd.getTime();
	}
	else
	{
		var sd = new Date(end_date);
		END_DATE = sd.getTime();
	}


	
	var keyquery={

	        "query_string": {
	         "query": 'text:' + keywords ,
	          "analyze_wildcard": true
	        }
	      
	};
	
	var must_filters = [
			            {
				              "range": {
				                "created_at": {
				                  "gte": START_DATE,
				                  "lte": END_DATE
				                }
				              }
				            }
				          ]
	
	var should_filter = []
	
	
	if(typeof hashtags != 'undefined' &&  hashtags!='')
	{
		var hashtagstofilter;
		if(hashtags instanceof Array)
		{
			hashtagstofilter = hashtags;
		}
		else
		{
			//hashtags = hashtags.toLowerCase();
			hashtagstofilter= $.map(hashtags.split(","), $.trim);
		}
		
		
		should_filter.push({
            "term" : { "entities.hashtags.text_raw" : hashtagstofilter }
			
		});
		
	}

	if(typeof mentions != 'undefined' && mentions!='')
	{
		var mentionstofilter;
		
		if(mentions instanceof Array)	
		{
			mentionstofilter = mentions;
		}
		else
		{
			//mentions = mentions.toLowerCase();
			mentionstofilter = $.map(mentions.split(","), $.trim);
		}

		should_filter.push({
            "term" : { "entities.user_mentions.screen_name_raw" : mentionstofilter }
			
		});
	}
	
	
	if(typeof users != 'undefined' &&  users!='')
	{
		var userstofilter;
		if(users instanceof Array)	
		{
			userstofilter = users;
		}
		else
		{
			//users = users.toLowerCase();
			userstofilter = $.map(users.split(","), $.trim);
		}

		should_filter.push({
            "term" : { "user.screen_name_raw" : userstofilter }
			
		});
	}
	
	
	if(typeof retweets != 'undefined' &&  retweets!='all')
	{

		must_filters.push({
            "term" : { "retweeted" : retweets}
		
		});
		
	}
		
	should_filter.push({ "bool": {
		          "must": must_filters,
		          "must_not": []
		        }});
	
	var filters = {	

		        "bool": {
		          "must": should_filter
		         
		        }
			
	};
	

		return			{"filtered" : 
						{   "query": keyquery ,
							"filter": filters
						}
					};
	

}









function getAggragation(topk, type)
{
	if(type == 0)
	{
			var aggs = {
			    "timeline": {
			      "date_histogram": {
			        "field": "created_at",
			        "interval": "1H",
			        "pre_zone": "-01:00",
			        "pre_zone_adjust_large_interval": true,
			        "min_doc_count": 1
			      },
			     
			    
			    },
			    "tophashtags": {
			          "date_histogram": {
			            "field": "created_at",
			            "interval": "1y",
			            "pre_zone": "-01:00",
			            "pre_zone_adjust_large_interval": true,
			            "min_doc_count": 1,
			            "extended_bounds": {
			              "min": START_DATE,
			              "max": END_DATE
			            }
			          }
			        ,
				    "aggs": {
				    	"yearly": {
			                "terms": {
			                  "field": "entities.hashtags.text",
			                  "size": topk,
			                  "order": {
			                    "_count": "desc"
			                  }
			                }
				      }
		          }
			    },
		          "topmentions": {
		        	  "date_histogram": {
				            "field": "created_at",
				            "interval": "1y",
				            "pre_zone": "-01:00",
				            "pre_zone_adjust_large_interval": true,
				            "min_doc_count": 1,
				            "extended_bounds": {
				              "min": START_DATE,
				              "max": END_DATE
				            }
				          }
				        ,
					    "aggs": {
					    	"yearly": {
				                "terms": {
				                  "field": "entities.user_mentions.screen_name",
				                  "size": topk,
				                  "order": {
				                    "_count": "desc"
				                  }
				                }
					    
					      }
		              }
		          }  ,
			    "tophost": {
		          "date_histogram": {
			            "field": "created_at",
			            "interval": "1y",
			            "pre_zone": "-01:00",
			            "pre_zone_adjust_large_interval": true,
			            "min_doc_count": 1,
			            "extended_bounds": {
			              "min": START_DATE,
			              "max": END_DATE
			            }
			          }
			        ,
				    "aggs": {
				    	"yearly": {
			                "terms": {
			                 "field": "entities.urls.host",
			                 // "field": "entities.urls.expanded_urls.host",
			                  "size": topk,
			                  "order": {
			                    "_count": "desc"
			                  }
			                }
				    
				      }
				    }
			      },
			      
			      "topusers": {
			    	  "date_histogram": {
				            "field": "created_at",
				            "interval": "1y",
				            "pre_zone": "-01:00",
				            "pre_zone_adjust_large_interval": true,
				            "min_doc_count": 1,
				            "extended_bounds": {
				              "min": START_DATE,
				              "max": END_DATE
				            }
				          }
				        ,
					    "aggs": {
					    	"yearly": {
				                "terms": {
				                  "field": "user.screen_name",
				                  "size": topk,
				                  "order": {
				                    "_count": "desc"
				                  }
				                }
					    
					      }
					    }
			        }
			  	}
	}
	else if(type == 1) {
		
		var aggs =  {
		    "hashtagtimeline": {
		        "terms": {
		            "field": "entities.hashtags.text",
		            "size": topk,
		            "order": {
		              "_count": "desc"
		            }
		          },
		          "aggs": {
		            "time": {
		              "date_histogram": {
		                "field": "created_at",
		                "interval": "1H",
		                "pre_zone": "-01:00",
		                "pre_zone_adjust_large_interval": true,
		                "min_doc_count": 1,
		                "extended_bounds": {
		                  "min": START_DATE,
		                  "max": END_DATE
		                }
		              }
		            }
		          }
		    
		    },// for hashtag timelines
		    "mentiontimeline": {
		        "terms": {
		            "field": "entities.user_mentions.screen_name",
		            "size": topk,
		            "order": {
		              "_count": "desc"
		            }
		          },
		          "aggs": {
		            "time": {
		              "date_histogram": {
		                "field": "created_at",
		                "interval": "1H",
		                "pre_zone": "-01:00",
		                "pre_zone_adjust_large_interval": true,
		                "min_doc_count": 1,
		                "extended_bounds": {
		                  "min": START_DATE,
		                  "max": END_DATE
		                }
		              }
		            }
		          }
		    
		    }
		}	
	}
	else
	{
		var aggs =  {
				  "timeline": {
				      "date_histogram": {
				        "field": "created_at",
				        "interval": "1H",
				        "pre_zone": "-01:00",
				        "pre_zone_adjust_large_interval": true,
				        "min_doc_count": 1
				      }
				      
				    }
		};
		
		
	}
	return aggs; 
}






function init_hashtags_timeline(container)
{
	$(container).bind('mousemove touchmove', function (e) {
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

	 Highcharts.Pointer.prototype.reset = function () {
	        return undefined;
	    };
	    
	    
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
}


