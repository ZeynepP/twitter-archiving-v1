package fr.ina.dlweb.dowser.ws.twitter.rest.es;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringEscapeUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.codehaus.jackson.node.ObjectNode;

import fr.ina.dlweb.dowser.ws.twitter.model.response.TwitterSearchHitsResponse;
import fr.ina.dlweb.dowser.ws.twitter.model.response.TwitterTimelineBuckets;
import fr.ina.dlweb.dowser.ws.twitter.model.response.TwitterTopEntityBuckets;
import fr.ina.dlweb.dowser.ws.twitter.model.response.TwitterTrendsHitsResponse;



public class TwitterESResponseUtils {

	public static ObjectMapper jsonMapper = createJsonMapper();
	private static SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");


	private static ObjectMapper createJsonMapper() {
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(Feature.INDENT_OUTPUT);
		return mapper;
	}
	
	
	/************************************** SEARCH QUERY RESPONSE FUNCTIONS *************************************/
	public static TwitterSearchHitsResponse[] getHitsFromResponse(ObjectNode response)
	{
		JsonNode esHits = response.get("hits").get("hits");
		TwitterSearchHitsResponse[] hits = null;
		try {
			hits = jsonMapper.readValue(esHits,TwitterSearchHitsResponse[].class);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return hits;	
	}
	
	
	public static String getTwitterInfo(String firstResponse, String lastResponse)
	{
		String results = "";
		JsonNode first;
		try {
			first = jsonMapper.readTree(firstResponse);
		}  catch(Exception e) {
			throw new RuntimeException("Can't read json  fromESSearchResponse", e);
		}
		
		
		
		
		JsonNode last;
		try {
			last = jsonMapper.readTree(lastResponse);
		} catch(Exception e) {
			throw new RuntimeException("Can't read json  fromESSearchResponse", e);
		}
		
		if(first!= null && last!= null)
		{
		
			long total = Long.parseLong( first.get("hits").get("total").asText());
					
			ObjectNode info =  jsonMapper.createObjectNode();
			info.put("first_date", Long.parseLong( first.get("hits").get("hits").get(0).get("_source").get("created_at").asText()));
			info.put("first_id", first.get("hits").get("hits").get(0).get("_source").get("id").asText());
			info.put("total", total);
			info.put("last_date", Long.parseLong( last.get("hits").get("hits").get(0).get("_source").get("created_at").asText()));
			info.put("last_id", last.get("hits").get("hits").get(0).get("_source").get("id").asText());
			
			
			
			
			try {
				results = jsonMapper.writeValueAsString(info);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return 	results;
	}
	
	

	
	
	public static HashMap<String,ArrayList<TwitterTrendsHitsResponse> > getTrendsHitsFromResponse(ObjectNode response)
	{
		JsonNode esHits = response.get("hits").get("hits");
		
		HashMap<String,ArrayList<TwitterTrendsHitsResponse> >  hits = new HashMap<String,ArrayList<TwitterTrendsHitsResponse>>();
		
		
		
		try {
			for(JsonNode h : esHits) {
			
				if(hits.containsKey(h.get("_source").get("name").asText())) {
					hits.get(h.get("_source").get("name").asText()).add(jsonMapper.readValue(h.get("_source"),TwitterTrendsHitsResponse.class));
				}	
				else {
					ArrayList<TwitterTrendsHitsResponse> t = new ArrayList<TwitterTrendsHitsResponse>();
					t.add(jsonMapper.readValue(h.get("_source"),TwitterTrendsHitsResponse.class));
					hits.put(h.get("_source").get("name").asText(), t);
				}
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//return  hits.toArray( new TwitterTrendsHitsResponse[hits.size()] );
		return hits;
	}
	


	
	/************************************** TIMELINE QUERY RESPONSE FUNCTIONS *************************************/
	

	public static TwitterTimelineBuckets[] getTimelineFromResponse(JsonNode response)
	{
		TwitterTimelineBuckets[] timeline = null;
		if(response!=null)
		{
			try {
				timeline = TwitterESResponseUtils.jsonMapper.readValue(response, TwitterTimelineBuckets[].class);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return timeline;
	}
	
	
	
	/************************************** DASHBOARD QUERY RESPONSE FUNCTIONS *************************************/
	
	public static TwitterTopEntityBuckets[] getDashboardFromResponse(JsonNode response, String aggName)
	{
		ArrayList<TwitterTopEntityBuckets> temp = new ArrayList<TwitterTopEntityBuckets>();
		if(response!=null)
		{
			try {
				
				
				if(response.has("missing_fields"))
				{
					TwitterTopEntityBuckets missing = new TwitterTopEntityBuckets();
					missing.key ="missing";
					missing.doc_count = response.get("missing_fields").get("doc_count").asInt();
					temp.add(missing);
					
				}
                
                temp.addAll(Arrays.asList( jsonMapper.readValue(response.get(aggName).get("buckets"), TwitterTopEntityBuckets[].class)));
	               
                TwitterTopEntityBuckets others = new TwitterTopEntityBuckets();
				others.key ="others";
				others.doc_count = response.get(aggName).get("sum_other_doc_count").asInt();
				temp.add(others);
                
				
				

				
			} catch (Exception e1) {
		
				e1.printStackTrace();
			}
			
		}

		return temp.toArray(new TwitterTopEntityBuckets[temp.size()]);
	}
	
	
	// Getting all results iteration
	public static TwitterTopEntityBuckets[] getDashboardFromResponse(JsonNode response)
	{
		ArrayList<TwitterTopEntityBuckets> temp = new ArrayList<TwitterTopEntityBuckets>();
		
		if(response!=null)
		{
			try {
				 Iterator<String> itr = response.getFieldNames();
	             while (itr.hasNext()) {  //to get the key fields
	                
	            	 String key_field = itr.next();
	              
	                
	                if(key_field.equals("missing_fields"))
					{
						TwitterTopEntityBuckets missing = new TwitterTopEntityBuckets();
						missing.key ="missing";
						missing.doc_count = response.get("missing_fields").get("doc_count").asInt();
						temp.add(missing);
						
					}
	                else
	                {
		                temp.addAll(Arrays.asList( jsonMapper.readValue(response.get(key_field).get("buckets"), TwitterTopEntityBuckets[].class)));
		               
		                TwitterTopEntityBuckets others = new TwitterTopEntityBuckets();
						others.key ="others_" + key_field;
						others.doc_count = response.get(key_field).get("sum_other_doc_count").asInt();
						temp.add(others);
	                }
					
				}
			

			} catch (Exception e1) {
		
				e1.printStackTrace();
			}
			
		}

		return temp.toArray(new TwitterTopEntityBuckets[temp.size()]);
	}
	
/*	
	private static String updateDashboardTimelines(TwitterTopEntityTimelineBuckets[] dashboard)
	{
		HashSet<String> categories = new HashSet<String>();
		
		for(TwitterTopEntityTimelineBuckets onedash : dashboard)
		{
			for(TwitterTimelineBuckets onebucket :  onedash.getTimelineBuckets())
			{
				categories.add(onebucket.getKey_as_string());
				//new Date(Long.parseLong(onebucket.getKey_as_string())).getYear()
			}
		}

		ArrayNode jsondashboard = jsonMapper.createArrayNode();
		
		for(String date : categories)
		{
			ObjectNode oneyear = jsonMapper.createObjectNode();
			oneyear.put("name", date);
			ArrayNode entities = jsonMapper.createArrayNode();
		
			TwitterTopEntityTimelineBuckets one = new TwitterTopEntityTimelineBuckets();
			one.key = String.valueOf(date);

			for(TwitterTopEntityTimelineBuckets onedash : dashboard)
			{
				
				for(TwitterTimelineBuckets onebucket :  onedash.getTimelineBuckets())
				{
					if(date == onebucket.getKey_as_string())
					{
						ObjectNode h = jsonMapper.createObjectNode();
						h.put("name",onedash.key);
						h.put("y", onebucket.getDoc_count());
						entities.add(h);
					}
					//
				}
			}
			oneyear.put("data", entities);
			jsondashboard.add(oneyear);
		}
		
		try {
			return jsonMapper.writeValueAsString(jsondashboard);
		} catch(Exception e) {
			throw new RuntimeException("Can't write json es request", e);
		}
		
		
	}
	
	*/
	/********************************************* ANALYZE RESPONSE FUNCTIONS **************************************/
	
	/********************************************* IRAMUTEQ RESPONSE FUNCTION *******************************/
	
	public static Entry<String, String> getIramuteqTextfromHit( TwitterSearchHitsResponse response,  String[] fields, String irasettings)
	{
		 
		String id = "****";

		String text = "";
		Date date = null;
		Calendar calendar1 = Calendar.getInstance();
	    
		try {
			//System.out.println(response.get_source().getCreated_at());
			date = new Date(Long.valueOf(  response.get_source().getCreated_at()));
			calendar1.setTime(date);
			text = response.get_source().text;
			if(irasettings.contains("nohashtag"))
			{
				for(String h : response.get_source().getHashtags())
				{
					text = text.replace(h, "");
				}
			}
			if(irasettings.contains("nomention"))
			{
				for(String m : response.get_source().mentions)
				{
					text = text.replace(m, "");
				}
			}
			if(irasettings.contains("nourl"))
			{
				text = text.replaceAll("(\\A|\\s)((http|https|ftp|mailto):\\S+)(\\s|\\z)", "");
			}
			
			text = text.toLowerCase();
			text = text.replace("#", "").replace("@", "").replace("rt ", "").replace("qt:", "");
			
			byte[] utf8Bytes;
			try {
				utf8Bytes = text.getBytes("UTF-8");
				text = new String(utf8Bytes, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
            text = StringEscapeUtils.escapeJson(text);

			for(String s : fields)
			{
				if(s.equals("id"))
				{
					id +=response.get_id();
				}
				else if(s.equals("date_yyyy"))
				{
					
					id = id + " *" + (calendar1.get(Calendar.YEAR));
					
				}
				else if(s.equals("date_yyyy_mm"))
				{
					
					id = id + " *" + (calendar1.get(Calendar.YEAR)) + "_" +  (calendar1.get(Calendar.MONTH));
					
				}
				else if(s.equals( "date_yyyy_mm_dd"))
				{
					
					id = id + " *" +(calendar1.get(Calendar.YEAR))+ "_" +  (calendar1.get(Calendar.MONTH))+ "_" + (calendar1.get(Calendar.DAY_OF_MONTH));
					
				}
				else if(s.equals( "date_yyyy_mm_dd_hh"))
				{
					
					id = id + " *" +(calendar1.get(Calendar.YEAR))+ "_" +  (calendar1.get(Calendar.MONTH))+ "_" + (calendar1.get(Calendar.DAY_OF_MONTH))+"_" + (calendar1.get(Calendar.HOUR_OF_DAY));
					
				}
				else if(s.equals( "date_yyyy_mm_dd_hh_mm"))
				{
					
					id = id + " *" +(calendar1.get(Calendar.YEAR))+ "_" +  (calendar1.get(Calendar.MONTH))+ "_" + (calendar1.get(Calendar.DAY_OF_MONTH))+"_" + (calendar1.get(Calendar.HOUR_OF_DAY))+"_" + (calendar1.get(Calendar.MINUTE));
					
				}
				else if(s.equals("user.id"))
				{
					
					id = id + " *" + response.get_source().getUserScreenName();
					
				}
				
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return new AbstractMap.SimpleEntry<String, String>(id, text);
		
		
	}
}
