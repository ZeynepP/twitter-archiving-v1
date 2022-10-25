var twitterDataTable = (function(div){

	var thisDataTable = {

			
			init : function() {
						if ( !$.fn.dataTable.isDataTable( div ) ) {
							
							
							
							
							
							cols = [
									{ title: "Tweet",name:"url","visible": true },
									{ title: "Date de tweet",name:"created_at","visible": false },
									{ title: "Texte",name:"text","visible": false },
									{ title: "Nb de favoris",name:"favorite_count","visible": false },
									{ title: "Retweeté" ,name:"retweeted","visible": false},
									{ title: "Quoté" ,name:"quoted","visible": false},
									{ title: "Nb de retweet",name:"retweet_count","visible": false },
									{ title: "Tags" ,name:"hashtags","visible": false},
									{ title: "Mentions",name:"user_mentions.screen_name","visible": false },
									{ title: "Hôte",name:"urls.host","visible": false },
									{ title: "Utilisateur",name:"user.screen_name","visible": false },
									{ title: "Utilisateur nom",name:"user.name","visible": false },
									{ title: "Utilisateur: nb de followers",name:"user.followers_count","visible": false },
									{ title: "Utilisateur: nb de statuses" ,name:"user.statuses_count","visible": false},
									{ title: "Utilisateur: location",name:"user.location","visible": false },
									{ title: "Utilisateur: lang",name:"user.lang","visible": false },
									{ title: "Utilisateur: date d'inscription",name:"user.created_at","visible": false },
									{ title: "Id",name:"id","visible": false },
									{ title: "Lang",name:"lang","visible": false },
									{ title: "Pays",name:"place.country","visible": false },
									{ title: "Location",name:"place.name","visible": false },
									{ title: "Date d'archivage",name:"archived_at","visible": false },
									{ title: "MediaUrl",name:"media.media_urls","visible": false },
								//	{ title: "Source",name:"source_type","visible": false },
									{ title: "Url complet",name:"urls.expanded_url","visible": false },
									{ title: "Methode d'archivage",name:"method_archive","visible": false },
									{ title: "Texte enrichi",name:"full_text","visible": false },
									{ title: "Collection",name:"collection","visible": false}
					
								]
								
							
						/*if(timelinebysource)
							{}	else{
									cols = [
										{ title: "Tweet",name:"url","visible": true },
										{ title: "Date de tweet",name:"created_at","visible": false },
										{ title: "Texte",name:"text","visible": false },
										{ title: "Nb de favoris",name:"favorite_count","visible": false },
										{ title: "Retweeté" ,name:"retweeted","visible": false},
										{ title: "Quoté" ,name:"quoted","visible": false},
										{ title: "Nb de retweet",name:"retweet_count","visible": false },
										{ title: "Tags" ,name:"hashtags","visible": false},
										{ title: "Mentions",name:"user_mentions.screen_name","visible": false },
										{ title: "Hôte",name:"urls.host","visible": false },
										{ title: "Utilisateur",name:"user.screen_name","visible": false },
										{ title: "Utilisateur nom",name:"user.name","visible": false },
										{ title: "Utilisateur: nb de followers",name:"user.followers_count","visible": false },
										{ title: "Utilisateur: nb de statuses" ,name:"user.statuses_count","visible": false},
										{ title: "Utilisateur: location",name:"user.location","visible": false },
										{ title: "Utilisateur: lang",name:"user.lang","visible": false },
										{ title: "Utilisateur: date d'inscription",name:"user.created_at","visible": false },
										{ title: "Id",name:"id","visible": false },
										{ title: "Lang",name:"lang","visible": false },
										{ title: "Pays",name:"place.country","visible": false },
										{ title: "Location",name:"place.name","visible": false },
										{ title: "Date d'archivage",name:"archived_at","visible": false },
										{ title: "MediaUrl",name:"media.media_urls","visible": false }
						
									]
							}*/
						//	var tablestate = StateManager.getState()["maintable"];
							var table = $(div).DataTable({
								// colvis does not work without dom !!!
								dom: 'B<"top"l<"clear">>tr<"bottom"p<"clear">>',
								//bDestroy: true,
								searching: false,
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
								paging: true,
								processing: true,
								serverSide: true,
								pagingType: "simple",
								pageLength:10,
								ajax : function(data, callback, settings) { thisDataTable.load(data, callback, settings);},
								language: {url: "../js/French.json"},
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
								columnDefs: [ 	 {"className": "dt-body-left", "targets": "_all"},
													 {
														
														 render: function ( data, type, full, meta ) {
															var links ='';
															if(meta.col == 0)
															{
																if(timelinebysource)
																	links+='<div class="wrapper">'+twitterdiv(full[0], full[11],"@"+full[10],full[1], full[2], full[16],full[6],full[3])+   ' </div><br/>';
																else
																	links+='<div class="wrapper">'+twitterdiv(full[0], full[11],"@"+full[10],full[1], full[2], full[16],full[6],full[3],full[22] )+   ' </div><br/>';
															}
															else if(meta.col == 9)
															{   		
																links += getmainDataTableEntityColumn(data, "Url",tweetstable);
															}
															else if(meta.col == 10)
															{
																links += getmainDataTableEntityColumn(data, "User", tweetstable);	
															}
															else
															{
																for (key in data) {
														
																	var t = '';
																	if(meta.col == 7){ t = "Hashtag"; }
																	else { t = "Mention";}
																	links += getmainDataTableEntityColumn(data[key], t,tweetstable) + "</br>" ;	
																 };
															}
															  return links;
														 },
														 aTargets: [0,8,7,9,10]
													 }
												 ]
						});
							
						}
					},
					
			update : function() {
							
							$(div).DataTable().ajax.reload();
							
					},
		
			info :	function() {
							
							return $(div).DataTable().page.info().recordsTotal;
							
					},	
			load  : function(data, callback, settings)  {
					
				var datestart = new Date();
				var query  ="";
				if(StateManager.getState()!=null)
				{ query = StateManager.getState()["query"];}
	
				if(query != "")
				{
					var order =  data.order;
					
					totalresults = 0;
					query["sort_field"] = data.columns[order[0].column].name;
					query["sort_type"] = order[0].dir;
					query["from"] = data.start;
					query["size"] = data.length;
				
					

						
					//var searchrequest = createPromiseRequest(basews + "/ppc/ws/search",StateManager.getState()["query"],"json");
					var searchrequest = createPromiseRequest(basews + "/ppc/ws/search", query ,"json");
					
					
					searchrequest.then(
						
							function(response)
							{
								console.log("request fini");
								var temp = response.hits;
								console.log(temp);
								var rows = []
								totalresults  =  response.total_count;
								
								updateHitsText ( "#resultsbanner" ,  (new Date() - datestart) ,response.total_count);
								
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
											 
											 if (tempent.indexOf(ent[k].host) == -1) {
												tempent.push( ent[k].host);
											}
											  
										  }
										  hosts = tempent.join();
									  }
									  var urls ='';
									  tempent =[];
									  if(ent != undefined)
									  {
										  for (var k = 0; k <ent.length; k++) { 
											if (tempent.indexOf(ent[k].expanded_url) == -1) {
											  tempent.push( ent[k].expanded_url);
											}
											
											  
										  }
										  urls = tempent.join();
									  }

									  var media ='';
									  tempent =[];
									  ent = temp[i]._source.media;
									  if(ent != undefined)
									  {
										  for (var k = 0; k <ent.length; k++) { 
											if (tempent.indexOf(ent[k].media_url) == -1) {
											  tempent.push( ent[k].media_url);
											}
											  
										  }
										  media = tempent.join();
									  }

									  tempent =[];
									  var mentions ='';
									  ent = temp[i]._source.user_mentions;
									  if(ent != undefined)
									  {
										  for (var k = 0; k <ent.length; k++) { 
											if (tempent.indexOf(ent[k].screen_name) == -1) {
											  tempent.push( ent[k].screen_name);
											}
											  
											  
										  }
										  mentions = tempent.join();
									  }
									  
									  
									  
									  var index = "Source externe";
									  var methodarchivage = temp[i]._source.method_archive;
									  
									
									 
									  
									  if(!timelinebysource || temp[i]._source.source_type == "ina")
									  { 
										 index = "Ina";
									  }
									
									  if(index != "Ina") {methodarchivage = "Search avec les Ids";}
										
									 var archiveddate = new Date(temp[i]._source.archived_at * 1);
									// archiveddate = archiveddate.toISOString().slice(0,10).replace("T"," ") + " " + archiveddate.toLocaleTimeString();
									 archiveddate = archiveddate.getFullYear() + "-" +  ('0' + (archiveddate.getMonth()+1)).slice(-2) + "-" + ('0' + archiveddate.getDate()).slice(-2)  + " " + archiveddate.toLocaleTimeString();
										
									 var createdat = new Date(temp[i]._source.created_at * 1);
									// createdat = createdat.toISOString().slice(0,10).replace("T"," ") + " " + createdat.toLocaleTimeString();
									 createdat = createdat.getFullYear() + "-" +  ('0' + (createdat.getMonth()+1)).slice(-2) + "-" + ('0' + createdat.getDate()).slice(-2)  + " " + createdat.toLocaleTimeString();
									// var formattedDate = date.getUTCDate() + '-' + (date.getUTCMonth() + 1)+ '-' + date.getUTCFullYear()  
									
									rows.push([
											temp[i]._source.user.profile_image_url,
											createdat ,
											twemoji.parse(temp[i]._source.text),
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
											archiveddate,
											media,
									//		index,
											urls,
											methodarchivage,
											twemoji.parse(temp[i]._source.full_text),
											temp[i]._source.collection
											
											
											]);
									}
								
								
								var dataResult = {data: rows}
								dataResult.recordsTotal =  response.total_count;
								dataResult.recordsFiltered =  response.total_count;
						
								callback(dataResult);
								 
								var current_index = $("#tabs").tabs("option","active");
								if(!tabstats[current_index ] )
										loadTabs(current_index);
								tabstats[current_index ] = true;
								//totalresults =response.total_count;
							//	StateManager.setState({"totalresult" :  response.total_count}, false);
							//	document.title = "load main totalresult data set state"
						},
						function(reason) {
							console.error('Handle rejected promise load maindatatable', reason);
							displayError();
							
				
					});
			
				}
						
						
						
				return false;
						
				
			}
	
	}
	return thisDataTable;})('#tweetsTable');



function getmainDataTableEntityColumn(data, type )
{
	
	return '<div class="wrapper">'+
				'<div class="content">'+
						'<ul>'+
						//'<a  onclick="analyze_one(\''+ data + '\',\'Url\',true);"><li>Ouvrir timeline</li></a>'+
							'<a    onclick="analyzeDataTable.addRow(\''+ data + '\',\'' + type + '\');"><li>Ajouter à Analyse</li></a>'+
						'</ul>'+
				   ' </div>'+
				   ' <div class="parent">' + data + '</div>'+
			   ' </div>';
	
}


function twitterlikediv(imgurl,name, tag,date,text,id, source)
{
		
		var div = "";

		
		div += '<div class="tweet-container"  id='+ id + '>';

		if( source == "Ina") {
			div+='<div class="tweet_ina">'
		}
		else{
			div+='<div class="tweet_ext">'
		}
		
		div += '<div class="tweet-avatar-wrapper"><div class="avatar"> <img onerror="this.style.display=\'none\'" src=' + imgurl + ' />';
		div+='</div> </div><div class="tweet-wrapper"><span class="name">'  ;
		div+= name ;
		div += ' </span> <span class="handle">' +tag  + '</span>';
		div += ' <span class="timestamp"> ' +date + '</span>';
		div += ' <span class="copy">' + text+'</span>';
		div += ' </div> </div></div>';


		return div;

}

function twitterdiv(imgurl,name, screenname,date,text,id, nb_retweet, nb_favorite, media )
{	
		var div = "";
		div += '<br/><br/><div class="tweet-container"  id='+ id + '>';

		text = text.replace(/(^|\s)(#[a-z\d-]+)/ig, "$1<span class='hash_tag'>$2</span>");
		text = text.replace(/(^|\s)(@[a-z\d-]+)/ig, "$1<span class='hash_tag'>$2</span>");

		div += ' <div class="tweet-avatar-wrapper"><div class="avatar"> <img onerror="this.style.display=\'none\'" src=' + imgurl + ' />';
		div += ' </div> </div><div class="tweet-wrapper">';
		div += ' <span class="name">' + twemoji.parse(name) + '</span>';
		div += ' <span class="handle">' + screenname + '</span>';
		div += ' <span class="timestamp"> ' + date + '</span>';
		div += ' <span class="copy">' + text +'</span>';
			
		if(media!=undefined)
		{
				m = media.split(",");
				div += '<img class="center-cropped" onerror="this.style.display=\'none\'" vspace="20" src=' + m[0] + ' /><br/>';	
		}

		div += ' <span class="fa fa-retweet" style="color:grey;""> ' + nb_retweet +'</span>';
		div += ' <span class="fa fa-heart" style="color:grey;margin-left:10px""> ' + nb_favorite +'</span>';
		div += ' </div></div>';

		return div;
}
