var twitterTrendsDataTable = (function(div){

	var thisDataTable = {

			
			init : function() {
						if ( !$.fn.dataTable.isDataTable( div ) ) {
							
						
						
								cols = [
										{ title: "as_of",name:"as_of","visible": true },
										{ title: "date_trend",name:"date_trend","visible": true },
										{ title: "name",name:"name","visible": true },
										{ title: "query",name:"query","visible": true },
										{ title: "url" ,name:"url","visible": true},
										{ title: "promoted_content" ,name:"promoted_content","visible": true},
										{ title: "tweet_volume",name:"tweet_volume","visible": true },
										{ title: "rank" ,name:"rank","visible": true}
										
						
									]
								
							
							
						//	var tablestate = StateManager.getState()["maintable"];
							var table = $(div).DataTable({
								// colvis does not work without dom !!!
								dom: 'B<"top"l<"clear">>tr<"bottom"p<"clear">>',
								//bDestroy: true,
								searching: true,
								stateSave: true,
							/*	stateSaveParams: function (settings, data) {
									var tablestate ={};
									console.log(data.columns);
									for(var i=0; i<data.columns.length;i++)
									{
										tablestate[i] = data.columns[i].visible
									}
									console.log("Setting maintable to state");
							//		StateManager.setState({"maintable" : tablestate},false);
									
								 },*/
								paging: false,
								processing: true,
								serverSide: true,
								pagingType: "simple",
								pageLength:50,
								ajax : function(data, callback, settings) { thisDataTable.load(data, callback, settings);},
								language: {url: "./js/French.json"},
								order: [[ 1, "asc" ]],
								
								buttons: [
									{
										extend: 'colvis',
										text: 'Afficher / Masquer les colonnes',
										collectionLayout: 'fixed three-column',
										postfixButtons: [ 'colvisRestore' ]

									}
								],
						
						
								columns: cols,
								columnDefs: [ 	 {"className": "dt-center", "targets": "_all"}]
						});
							
						}
					},
					
			update : function() {
							$(div).DataTable().ajax.reload();
							
					},
					
			load  : function(data, callback, settings)  {
					
				var datestart = new Date();
				var query  ="";
				if(StateManager.getState()!=null)
				{ query = StateManager.getState()["query"];}
	

				var searchrequest = createPromiseRequest(basews + "/ppc/ws/trends_timeline",query,"json");

	
	

					
					searchrequest.then(
						
							function(response)
							{
								console.log(response);
								var temp = response;
								var rows = []
								totalresults  = temp.length;
								
								
								for (var i = 0; i <temp.length; i++) {
									  
									  
									 
										
									 var date_trend = new Date(temp[i]._source.date_trend );
									
									 date_trend = date_trend.getFullYear() + "-" +  ('0' + (date_trend.getMonth()+1)).slice(-2) + "-" + ('0' + date_trend.getDate()).slice(-2)  + " " + date_trend.toLocaleTimeString();
										
									
									
									rows.push([
											temp[i]._source.as_of,
											date_trend ,
											temp[i]._source.name,
											temp[i]._source.query,
											temp[i]._source.url,
											temp[i]._source.promoted_content,
											temp[i]._source.tweet_volume,
											temp[i]._source.rank
											
											]);
									}
								
								
								var dataResult = {data: rows}
								dataResult.recordsTotal =  response.total_count;
								dataResult.recordsFiltered =  response.total_count;
						
								callback(dataResult);
								 
						},
						function(reason) {
							console.error('Handle rejected promise load maindatatable', reason);
							displayError();
							
				
					});
			
				
						
						
						
				return false;
						
				
			}
	
	}
	return thisDataTable;})('#tweetsTrendsTable');



// function twitterlikediv(imgurl,name, tag,date,text,id, source)
// {
		
		// var div = "";
		// var style = "";
			
		// div += '<div class="tweet-container"' + style  +  ' id='+ id + '>';

		// if( source == "Ina") {
			// div+='<div class="tweet_ina">'
		// }
		// else{
			// div+='<div class="tweet_ext">'
		// }
		
		// div += '<div class="tweet-avatar-wrapper"><div class="avatar"> <img onerror="this.style.display=\'none\'" src=' + imgurl + ' />';
		// div+='</div> </div><div class="tweet-wrapper"><span class="name">'  ;
		// div+= name ;
		// div += ' </span> <span class="handle">' +tag  + '</span>';
		// div += ' <span class="timestamp"> ' +date + '</span>';
		// div += ' <span class="copy">' + text+'</span>';
		// div += ' </div> </div></div>';


		// return div;

// }
