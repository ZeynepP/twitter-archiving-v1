/**
 * http://usejsdoc.org/
 * 
 * zpehlivan@ina.fr
 * 
 * Contains functions to fir the es result to highcharts json format 
 * 
 */

function flatten_aggr_result_est(temp)
{
	var data = [];
	var min=5000000000000;

	//TODO key_as_string/1
	console.log(temp[0]);
	for (var i = 0; i < temp.length; i++) { 
			
		 
		  if(temp[i].key_as_string/1 < 1447457431000)
		  {
			   data.push([temp[i].key_as_string/1,null]);
		  }
		  else
		  { 
				data.push([temp[i].key_as_string/1,temp[i].doc_count]);
		  }
		  if(min >temp[i].key_as_string/1 )
		  {
			  min = temp[i].key_as_string;
		  }
	}

	return [data,min];
}

function flatten_timeline(temp)
{
	var data = [];
	for (var i = 0; i < temp.length; i++) { 
		data.push([temp[i].key_as_string/1,temp[i].doc_count]);
	}
	return data;
}




function flatten_aggr_result_timeline(temp)
{
	var categories  = new Set(["RT","Tweet"]);
	var dataretweet = [];
	var data = [];
	var total = [];
	var series = [];
	for (var i = 0; i < temp.length; i++) { 
		var buckets = temp[i].retweet.buckets;
		var date = temp[i].key;
		var t = 0;
		for (var j = 0; j < buckets.length; j++) {
			if(buckets[j].key == 'F')
			{
					data.push([date,buckets[j].doc_count]);
			}
			else
			{
				dataretweet.push([date,buckets[j].doc_count]);
			}
			 t += buckets[j].doc_count;
			 
		}
		total.push([date,t]);
	}


	series.push({"name": "RT", "data":dataretweet });
	series.push({"name": "Tweet", "data":data });

	return [series, total];

}




function flatten_aggr(temp, hashtags,series)
{

	for (var i = 0; i < temp.length; i++) { 
			var data = [];
			var n =temp[i].key;
			if(hashtags.indexOf(n)>=0)
			{
				var buckets = temp[i].time.buckets;
				
				for (var j = 0; j < buckets.length; j++) {
					data.push([buckets[j].key,buckets[j].doc_count]);
				}
				series.push({"name": n, "data":data });
				
			}
	}

	return series;
}




function flatten_dashboard(temp)
{
//TODO:do it server side
    var series = [];
	var categories  = [];
	var time  = [];
	//hashtags.buckets
	for (var i = 0; i < temp.length; i++) { 
			var buckets = temp[i].timeline.buckets;
			if(!contains(categories,temp[i].key) )
			{
				categories.push(temp[i].key);
			}
			for (var j = 0; j < buckets.length; j++) {
				if(!contains(time, buckets[j].key_as_string/1) )
				{
					time.push(buckets[j].key_as_string/1);
				}
			}
	}
	
	
	for(var k =0; k<time.length;k++)
	{
		var data = [];
		var cat = time[k];
		for (var i = 0; i < temp.length; i++) { 
			
			var tag = temp[i].key;
			if(tag != "null")
			{				
				var buckets = temp[i].timeline.buckets;
				for (var j = 0; j < buckets.length; j++) {
					if(buckets[j].key_as_string/1 == cat)
					{
						data.push({'name':tag,'y':buckets[j].doc_count});
					}
					
				
				}
			}
		
	
		}
		var date = new Date(cat);
		var date_year = date.getFullYear();
		if(date_year>2010)
		{
			series.push({"name": date_year, "data":data });

		}

	}

	return [categories,series];
}
function flatten_dashboard_bck(temp)
{

    var series = [];
	var categories  = [];
	//hashtags.buckets
	for (var i = 0; i < temp.length; i++) { 
			var buckets = temp[i].timeline.buckets;
			for (var j = 0; j < buckets.length; j++) {
				if(!contains(categories, buckets[j].key_as_string/1) )
				{
					categories.push(buckets[j].key_as_string/1);
				}
			
			
			}
	}
	
	for(var k =0; k<categories.length;k++)
	{
		var data = [];
		var cat = categories[k];
		for (var i = 0; i < temp.length; i++) { 
			
			var tag = temp[i].key;
			if(tag != "null")
			{				
				var buckets = temp[i].timeline.buckets;
				for (var j = 0; j < buckets.length; j++) {
					if(buckets[j].key_as_string/1 == cat)
					{
						data.push({'name':tag,'y':buckets[j].doc_count});
					}
				
				}
			}
		
	
		}
		var date = new Date(cat);
		var date_year = date.getFullYear();
		series.push({"name": date_year, "data":data });
		
	}



	return [categories,series];
}

function flatten_aggr_result_tags(temp, hashtags)
{

    var series = [];
	for (var i = 0; i < temp.length; i++) { 
			var data = [];
			var n =temp[i].key;
			
			if(hashtags.indexOf(n)>=0)
			{
				var buckets = temp[i].time.buckets;
				
				for (var j = 0; j < buckets.length; j++) {
					data.push([buckets[j].key,buckets[j].doc_count]);
				}
				series.push({"name": n, "data":data });
				
			}
	}

	return series;
}


function flatten_aggr_result_stacked_categories(temp)
{
	var categories  = new Set();
	for (var i = 0; i < temp.length; i++) { 
		var buckets = temp[i].yearly.buckets;
		for (var j = 0; j < buckets.length; j++) {
			categories.add(buckets[j].key);
		}
	}
	return setToArray(categories);

}


function flatten_aggr_result_stacked_categories_array(temp)
{
	var categories  = [];
	for (var i = 0; i < temp.length; i++) { 
		var buckets = temp[i].yearly.buckets;
		for (var j = 0; j < buckets.length; j++) {
			if(!contains(categories, buckets[j].key))
			{
				categories.push(buckets[j].key);
				
		
			}
		}
	}
	return categories;

}

//http://jsfiddle.net/1ktmb2d2/1/
function flatten_aggr_result_stacked(temp,categories)
{
	var serie = [];
	 var xc = 0;
	for (var i = 0; i < temp.length; i++) { 
		var buckets = temp[i].yearly.buckets;
		var data = [];
	
		var date = new Date(temp[i].key_as_string/1);
		var y = date.getFullYear();
		
		for (var j = 0; j < buckets.length; j++) {
			//alert(categories.indexOf(buckets[j].key) + "  " + buckets[j].key + "  " +buckets[j].doc_count);
			//data.push({x:categories.indexOf(buckets[j].key),'name':buckets[j].key,'y':buckets[j].doc_count});
			data.push({'name':buckets[j].key,'y':buckets[j].doc_count});
			
		}
		//data = data.sort();
		serie.push({"name":y, "data": data});
	}
   
	return serie ;
}

