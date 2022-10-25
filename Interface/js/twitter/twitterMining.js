var wordcloudDataTable = (function(div){
	var table;
	
	var thisDataTable = {

			init : function() {
						if ( !$.fn.dataTable.isDataTable( div ) ) {
							this.table = $(div).DataTable(
								{ 
										dom: 'Bfrtip',
										buttons: [
											 {
												extend: 'csv',
												text: 'Exporter en CSV',
												title:'Ina_DLWEB_wordcloud'
												
											}
										],
										searching: false,
										paging: false,
										info:false,
										processing: true,
										ordering:false,
										serverSide: true,
										ajax : thisDataTable.load,
										language: {url: "./js/French.json"},
										order: []
								 
								}
							);
							

						}
				},
					
			update : function()			{
			
					$( "#cloud" ).empty();
					$('#tableWordcount tbody').empty();
					$(div).DataTable().ajax.reload();
				
					
				},
			clean : function()			{
			
					$( "#cloud" ).empty();
					$('#tableWordcount tbody').empty();
					
				
					
				},
			
			load  : function(data, callback, settings){
				console.error("load wordcloud");
				var query = StateManager.getState()["query"];
				query["wordcloudsize"] =  $("#wordcloudsize").val();
				query["stopwords"] =  $("#stopwords").val();
				var request = createPromiseRequest(basews + "/ppc/ws/wordcloud",query,"json");
				request.then(
						
						function(response)
						{
							var rows = []
							var words =[]
				
							
							
							for (var i = 0; i <response.length; i++) {
								 rows.push([response[i].key,response[i].doc_count]); 
								 words.push({text:response[i].key,size:response[i].doc_count});
							}
							
							
							var dataResult = {data: rows}
							dataResult.recordsTotal =  response.total_count;
							dataResult.recordsFiltered =  response.total_count;
							callback(dataResult);

							$( "#cloud" ).empty();
							
							
								var fill = d3.scale.category20();
								scale = function(x) {
								  if (!arguments.length) return scale;
								  scale = x == null ? 'sqrt' : x;
								  return wordcloud;
								};
								
							  var w =800;
							  var h = 400;
					
							  		  
							  var fontSize = d3.scale.log().base(10).domain([response[response.length-1].doc_count, response[0].doc_count+1000]).range([5, 95]); 
							  // // 95 because 100 was causing stuff to be missing
							  // http://stackoverflow.com/questions/26471497/d3-js-word-missing-from-word-cloud
							  
							  d3.layout.cloud().size([w, h])
								  .words(words)
								  .rotate(function() { return ~~(Math.random() * 2) * 90; })
								  .font("Impact")
								  .fontSize(function(d) {return fontSize(d.size); })
								  .on("end", draw)
								  .start();
								  
								  

							  function draw(words, bounds) {
								 
								  
								  scaling = bounds ? Math.min(
											w / Math.abs(bounds[1].x - w / 2),
											w / Math.abs(bounds[0].x - w / 2),
											h / Math.abs(bounds[1].y - h / 2),
											h / Math.abs(bounds[0].y - h / 2)) / 2 : 1;
											
								
								d3.select("#cloud").append("svg")
									.attr("width", w)
									.attr("height", h)
									.attr("id","svgcloud")
									
								  .append("g").attr("transform", "translate(" + [w >> 1, h >> 1] + ")scale(" + scaling + ")")
								
								  .selectAll("text")
									.data(words)
									.enter().append("text")
										.style("font-size", function(d) { return d.size + "px"; })
										.style("font-family", "Impact")
										.style("fill", function(d, i) { return fill(i); })
										.attr("text-anchor", "middle")
										.attr("transform", function(d) {
										  return "translate(" + [d.x, d.y] + ")rotate(" + d.rotate + ")";
										})
								  
									.text(function(d) { return d.text; });
							  }	

						},
						function(reason) {
							console.error('Handle rejected promise load wordcloudDataTable ', reason);
							displayError();
							
				
					});
			
				}

	}
	return thisDataTable;
})('#tableWordcount');



function svgToImage() {
			// Works in firefox 
			// https://gist.github.com/Caged/4649511
			console.log("svg to image");
			var svg = document.getElementById('svgcloud');
			
			var xml = new XMLSerializer().serializeToString(svg);
			
		   // var data = 'data:image/svg+xml;base64,' + btoa(xml);
			var data = 'data:image/svg+xml;base64,' + btoa(unescape(encodeURIComponent(xml)));
			var image = new Image(); 
			image.setAttribute('src', data); 

		  //  document.body.appendChild(image);
			
			var a = document.createElement('a');
			a.setAttribute("download", "Ina_.svg");
			a.setAttribute("href", data);
			a.appendChild(image);

			a.click();
}



var iramuteq = (function(){
	
	var thisiramuteq = {
		
		load: function(){
				var query = StateManager.getState()["query"];
				query["irafields"] =  "";
				$('#checkboxes input:checked').each(function() {
					query["irafields"] = query["irafields"]+ ";" + $(this).attr('name')
				});
				query["irafields"] = query["irafields"] + "&";

				$('#irasettings input:checked').each(function() {
					query["irafields"] += $(this).attr('name') + ";"
				});
				
				var request = createPromiseRequest(basews + "/ppc/ws/iramuteq",query,null);
				request.then(
						
						function(response)
						{
							console.log(response.name);
							var blob = new Blob([response.content], {type: "text/plain;charset=utf-8"});
							console.log("creating " + response.name);
							saveAs(blob, response.name);
							
						},
						function(reason) {
							console.error('Handle rejected promise iramuteq load', reason);
							displayError();
							
				
						});
			
				
		}
		
		
	}
	return thisiramuteq;
})();


var network = (function(){
	
	var thisnetwork = {
		
		load: function(){
				var query = StateManager.getState()["query"];
				query["irafields"] =  "";
				$('#checkboxes input:checked').each(function() {
					query["irafields"] = query["irafields"]+ ";" + $(this).attr('name')
				});
				query["irafields"] = query["irafields"] + "&";

				$('#irasettings input:checked').each(function() {
					query["irafields"] += $(this).attr('name') + ";"
				});
				
				var request = createPromiseRequest(basews + "/ppc/ws/iramuteq",query,null);
				request.then(
						
						function(response)
						{
							console.log(response.name);
							var blob = new Blob([response.content], {type: "text/plain;charset=utf-8"});
							console.log("creating " + response.name);
							saveAs(blob, response.name);
							
						},
						function(reason) {
							console.error('Handle rejected promise network load ', reason);
							displayError();
							
				
						});
			
				
		}
		
		
	}
	return thisnetwork;
})();



function generateNetworkGraph()
{
	
	var query = StateManager.getState()["query"];
	var request = createPromiseRequest(basews + "/ppc/ws/network",query,null);
	request.then(
						
				function(response)
				{
					console.log(response.name);
					var blob = new Blob([response.content], {type: "text/plain;charset=utf-8"});
					console.log("creating " + response.name);
					saveAs(blob, response.name);
					
				},
				function(reason) {
					console.error('Handle rejected promise generateNetworkGraph', reason);
					displayError();
					
		
				});
			
}



function getWordsList(div)
{
	var words = [];
	var data = $(div).DataTable().rows().data();
	var n = data.length
	var words =[];
	for (var i = 0; i<n;i++) {
		var rows = $(div).DataTable().rows( i ).data();
		
		words.push({"text":rows[0][0],"size":rows[0][1]});
		
	}
	return words;
}

