package fr.ina.dlweb.dowser.ws.twitter.rest.es;

import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.joda.time.DateTimeZone;

import fr.ina.dlweb.dowser.ws.twitter.model.request.TwitterSearchRequest;
import fr.ina.dlweb.dowser.ws.twitter.model.request.TwitterTrendsSearchRequest;

public class TwitterESRequestUtils {

	public static ObjectMapper jsonMapper = createJsonMapper();

	private static ObjectMapper createJsonMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(Feature.INDENT_OUTPUT);
		return mapper;
	}

	/**********************************************   TEXT QUERY ********************************************/
	
	public static ObjectNode createTextQuery(String query, boolean addfields) {
		
		ObjectNode obj = jsonMapper.createObjectNode();
		ObjectNode queryobj = obj.putObject("query");
		if(query == null || query.isEmpty() || query.trim().equals( "*:*"))
		{
			queryobj.putObject("match_all");
		    return obj;
		}
		
		ObjectNode textQuery = queryobj.putObject("query_string");
		textQuery.put("query",   query);
		String[] esFields = TwitterSettings.searchFields.split(";");
		if(addfields)
		{
			ArrayNode fields = textQuery.putArray("fields");
			for(String s : esFields) {
					fields.add(s.trim());}
			
		}
		textQuery.put("analyze_wildcard",true);

		return obj;
		
	}
	
	/**********************************************  FILTERS ********************************************/
	
	public static ObjectNode createFilters(TwitterSearchRequest request, String dateField)
	{
		
		
		ObjectNode filter = jsonMapper.createObjectNode();
		ObjectNode boolfilter = filter.putObject("bool");
		ArrayNode mustfilters	= boolfilter.putArray("must");
		ArrayNode mustnotfilters	= boolfilter.putArray("must_not");
		
		// NO NEED HERE BUT I KEEP IT 
		if( request.getDate_stop()>0 ||  request.getDate_start()>0)
		{
			ObjectNode rangefilter = createRangeFilter(request.getDate_start(), request.getDate_stop(), dateField);
			if(rangefilter !=null)
				mustfilters.add(rangefilter);
		}
		
		
		ObjectNode tags = createTweetsFilter(request.getHashtags(), TwitterSettings.mapESFields.get("hashtags"),"+");
		if(tags !=null)
			mustfilters.add(tags);
		
		tags = createTweetsFilter(request.getHashtags(), TwitterSettings.mapESFields.get("hashtags"),"-");
		if(tags !=null)
			mustnotfilters.add(tags);
		
		tags = createTweetsFilter(request.getHashtags(), TwitterSettings.mapESFields.get("hashtags"),null);
		if(tags !=null)
			mustfilters.add(tags);

		ObjectNode mentions = createTweetsFilter(request.getMentions(), TwitterSettings.mapESFields.get("mentions"),"+");
		if(mentions !=null)
			mustfilters.add(mentions);
		
		
		mentions = createTweetsFilter(request.getMentions(), TwitterSettings.mapESFields.get("mentions"),"-");
		if(mentions !=null)
			mustnotfilters.add(mentions);
		
		mentions = createTweetsFilter(request.getMentions(), TwitterSettings.mapESFields.get("mentions"),null);
		if(mentions !=null)
			mustfilters.add(mentions);
		
		ObjectNode users = createTweetsFilter(request.getUsers(), TwitterSettings.mapESFields.get("users"),null);
		if(users !=null)
			mustfilters.add(users);
		
		ObjectNode urls = createTweetsFilter(request.getUrls(), TwitterSettings.mapESFields.get("urls"),null);
		if(urls !=null)
			mustfilters.add(urls);
		
		ObjectNode langs = createTweetsFilter(request.getLang(), TwitterSettings.mapESFields.get("lang"),null);
		if(langs !=null)
			mustfilters.add(langs);
		
		if(request.getRetweet() != -1)
		{
			ObjectNode rt = createTweetsFilter( Integer.toString( request.getRetweet()), TwitterSettings.mapESFields.get("retweet"),null);
			if(rt !=null)
				mustfilters.add(rt);
		}
		
		if(request.getQuote() != -1)
		{
			ObjectNode quoted = createTweetsFilter( Integer.toString( request.getQuote()), TwitterSettings.mapESFields.get("quote"),null);
			if(quoted !=null)
				mustfilters.add(quoted);
		}

		if(request.getDashboard_type()!=null && request.getDashboard_type().equals("city"))
		{
			ObjectNode city = createTweetsFilter( "city", TwitterSettings.mapESFields.get("place_type"),null);
			if(city!=null)
				mustfilters.add(city);
		}
		
		tags = createTweetsFilter(request.getCollection(), TwitterSettings.mapESFields.get("collection"), null);
		if(tags !=null)
			mustfilters.add(tags);
	/*	WE DO NOT SEPERATE ANYMORE SOURCES 
	 * if(request.getSource() != -1)
		{
			String sourceType = "ina";
			ObjectNode source = createTweetsFilter(sourceType, TwitterSettings.SOURCE_FILTER_FIELD,null);
			if(request.getSource() == 1) // ext different ina can be whatever nick or ucla
			{
				mustnotfilters.add(source);
			}
			else
			{
				mustfilters.add(source);
			}
			
			
		} */
		String timelinetype = request.getTimeline_type();

		if(timelinetype!=null && !timelinetype.equals("all"))
		{
			String sourceType = "ina";
			ObjectNode source = createTweetsFilter(sourceType, "source_type",null);
			if(timelinetype.equals("ext"))
			{
				mustnotfilters.add(source);
			}
			else
			{
				mustfilters.add(source);
			}
		}
		return filter;
		
	}
	
	
	static ObjectNode createTweetsFilter(String query, String type, String booltype)
	{
		if(query == null|| query.isEmpty()  )
			return null;

		ObjectNode tweetfilter1 = jsonMapper.createObjectNode();
		ObjectNode tweetfilter = tweetfilter1.putObject("terms");
		ArrayNode arraynode = tweetfilter.putArray(type);
		if(booltype!=null && booltype.equals("+"))
			tweetfilter.put("execution", "and");
		// adding this to have +hastag1;-hashtag2 etc. 
		for (String s : query.split(";")) {
			if(booltype == null && (!s.startsWith("+") && !s.startsWith("-")))
				arraynode.add(s);
			else if(booltype!=null && s.startsWith(booltype))
				arraynode.add(s.replace(booltype, ""));
		}	
		if(arraynode.size() == 0)
			return null;
		return tweetfilter1;
	}
	
	
	static ObjectNode createRangeFilter(long startdate, long enddate, String dateField) {
		
		
		
		ObjectNode datefilter = jsonMapper.createObjectNode();
		ObjectNode dateRangeFilter = datefilter.putObject("range");
		ObjectNode ranges = jsonMapper.createObjectNode();

		if(enddate > 0)
			ranges.put("lte", enddate);
		
		if(startdate > 0)
			ranges.put("gte", startdate);
		
		dateRangeFilter.put(dateField, ranges);
		return datefilter;
	
	}
	
	
	public static ObjectNode createFiltersEstimation(TwitterSearchRequest request, String dateField)
	{
		ObjectNode filter = jsonMapper.createObjectNode();
		ObjectNode boolfilter = filter.putObject("bool");
		ArrayNode mustfilters	= boolfilter.putArray("must");
		if( request.getDate_stop()>0 ||  request.getDate_start()>0)
		{
			ObjectNode rangefilter = createRangeFilter(request.getDate_start(), request.getDate_stop(), dateField);
			if(rangefilter !=null)
				mustfilters.add(rangefilter);
		}
		ObjectNode tags = createTweetsFilter(request.getHashtags(), "hashtag",null);;//Stupid zp hashtag instead of hashtags
		if(tags !=null)
			mustfilters.add(tags);

		
		return filter;
		
	}
	
	
	
	
	/**********************************************  FILTERS OVER********************************************/
	
	
	
	
	/**********************************************   TIMELINES AGGREGATIONS ********************************/
	
	 static ObjectNode createTimelineNode(String field, String interval, long dateStart, long dateStop, String timeZoneStr) {

		ObjectNode esDateHistoAgg = TwitterESRequestUtils.jsonMapper.createObjectNode();
		ObjectNode esDateHistoAggParams = esDateHistoAgg.putObject("date_histogram");
		esDateHistoAggParams.put("min_doc_count", 0);
		esDateHistoAggParams.put("field", field);
		esDateHistoAggParams.put("interval", interval);

		
		esDateHistoAggParams.put("time_zone", timeZoneStr);
		
	//	if(dateStart != null || dateStop != null) {
			
			ObjectNode esDateHistoExtendedBounds = esDateHistoAggParams.putObject("extended_bounds");
			if(dateStart>0)
				esDateHistoExtendedBounds.put("min",dateStart);	
			if(dateStop>0)
				esDateHistoExtendedBounds.put("max", dateStop);
				
			
			
			
		//}
		return esDateHistoAgg;
	}
	 
	 public static ObjectNode createTimelineAggs(String interval, String timeZoneStr,  String dateField, long dateStart, long dateStop )
	{

			ObjectNode esDateHistoAgg = jsonMapper.createObjectNode();
			ObjectNode esDateHistoAggParams = esDateHistoAgg.putObject("date_histogram");
			esDateHistoAggParams.put("min_doc_count", 0);
			esDateHistoAggParams.put("field", dateField);
			esDateHistoAggParams.put("interval", interval);
			esDateHistoAggParams.put("min_doc_count" ,  0);
			
		/*	DateTimeZone timeZone = DateTimeZone.UTC;
			try {
				timeZone = DateTimeZone.forID(timeZoneStr);
			} catch(Exception e) {}*/
			
			esDateHistoAggParams.put("time_zone", timeZoneStr);
				
			ObjectNode esDateHistoExtendedBounds = esDateHistoAggParams.putObject("extended_bounds");
			if(dateStart>0)
				esDateHistoExtendedBounds.put("min",dateStart);	
			if(dateStop>0)
				esDateHistoExtendedBounds.put("max", dateStop);
				
			

			ObjectNode esAggsNode =jsonMapper.createObjectNode();
			ObjectNode es_esDateHistoAggNode = esAggsNode.putObject("timeline");
			es_esDateHistoAggNode.putAll(esDateHistoAgg);
			
			
		/*	
			String js =   " { \"timelinesum\" : { \"cardinality\" :{\"field\" : \"id\"}} }";
			try {
				JsonNode sumagg = jsonMapper.readTree(js);
				es_esDateHistoAggNode.put("aggs",sumagg);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		*/	
			
			
			return esAggsNode;
	}
	 
	 
	 
	public static ObjectNode createTimelineNodeEstimation(TwitterSearchRequest request) {
		 
		 
		   String interval = request.getTime_interval();



			ObjectNode esDateHistoAgg = jsonMapper.createObjectNode();
			ObjectNode esDateHistoAggParams = esDateHistoAgg.putObject("date_histogram");
			esDateHistoAggParams.put("min_doc_count", 0);
			esDateHistoAggParams.put("field", request.DATE_FIELD);
			esDateHistoAggParams.put("interval", interval);

			
		/*	DateTimeZone timeZone = DateTimeZone.UTC;
			try {
				timeZone = DateTimeZone.forID(timeZoneStr);
			} catch(Exception e) {}*/
			
			esDateHistoAggParams.put("time_zone", request.getTime_zone());
				


			ObjectNode esAggsNode =jsonMapper.createObjectNode();
			
			
			ObjectNode es_esDateHistoAggNode = esAggsNode.putObject("timeline");
			
			
			es_esDateHistoAggNode.putAll(esDateHistoAgg);
			
			
			
			String js =   " { \"timelinesum\" : { \"sum\" :{\"field\" : \"track_smoothed\"}} }";
			try {
				JsonNode sumagg = jsonMapper.readTree(js);
				es_esDateHistoAggNode.put("aggs",sumagg);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
			return esAggsNode;
	}
	

	
	
	
	/***************************************************** CLUSTERING AGGREGATIONS *************************/
	/**
	 * To have a recommandation  systems
	 * @param request
	 * @return
	 */
	
	static ObjectNode createClusteringAggregation(TwitterSearchRequest request)
	{
	
		ObjectNode esDateHistoAggNode = createTimelineNode(request.DATE_FIELD, request.getTime_interval(), request.getDate_start(), request.getDate_stop(), request.getTime_zone());
		
		
		ObjectNode queryAggNode = jsonMapper.createObjectNode();
		ObjectNode aggsAggNoden = queryAggNode.putObject("clustering");
		
		ObjectNode analyzeNode = jsonMapper.createObjectNode();
		ObjectNode term = aggsAggNoden.putObject("terms");
		
		term.put("field", TwitterSettings.mapESFields.get("hashtags"));
		term.put("size", TwitterSettings.TOP_ENTITIES_SIZE);
		
		ObjectNode timeline = jsonMapper.createObjectNode();
		timeline.put("timeline", esDateHistoAggNode);
		aggsAggNoden.put("aggs", timeline);
		return queryAggNode;
	}
	
	
	
	/***************************************************** ANALYZE AGGREGATIONS *************************/
	

	
	static ObjectNode createAnalyzeAggregation(TwitterSearchRequest request)
	{
	
		ObjectNode esDateHistoAggNode = createTimelineNode(request.DATE_FIELD, request.getTime_interval(), request.getDate_start(), request.getDate_stop(), request.getTime_zone());
		
		
		ObjectNode queryAggNode = jsonMapper.createObjectNode();
		ObjectNode aggsAggNoden = queryAggNode.putObject("analyze");
		ObjectNode filternode = aggsAggNoden.putObject("filter");
	
//		//TODO: check why it is here no need there is a range filter in the query !!! why why why
//		if( request.getDate_stop() > 0 || request.getDate_start() > 0)
//			filterand.add(createRangeFilter(request.getDate_start(), request.getDate_stop()));
//		
		ObjectNode analyzeNode = jsonMapper.createObjectNode();
		ObjectNode term = analyzeNode.putObject("term");
		
		
		if(request.getAnalyze_request().getHashtags() != null)
		{
			
			term.put(TwitterSettings.mapESFields.get("hashtags"), request.getAnalyze_request().getHashtags() );
		}
		else if(request.getAnalyze_request().getMentions() != null)
		{
			
			term.put(TwitterSettings.mapESFields.get("mentions"), request.getAnalyze_request().getMentions() );
		}
		else if(request.getAnalyze_request().getUrls() != null)
		{
			
			term.put(TwitterSettings.mapESFields.get("urls"), request.getAnalyze_request().getUrls() );
		}
		else if(request.getAnalyze_request().getUsers() != null)
		{
			
			term.put(TwitterSettings.mapESFields.get("users"), request.getAnalyze_request().getUsers() );
		}
		else
			term = null;

		if(term!=null)
		{
			
			ArrayNode filterand = filternode.putArray("and");
			filterand.add(analyzeNode);
		}
		
		ObjectNode timeline = jsonMapper.createObjectNode();
		timeline.put("timeline", esDateHistoAggNode);
		aggsAggNoden.put("aggs", timeline);
		return queryAggNode;
	}
	
	
	/********************************************* WORD CLOUD AGGREGATIONS ****************************************/
	
	public static ObjectNode createWordCloudAggregation(int size, List<String> stopwords)
	{	
		
		ObjectNode queryAggNode = jsonMapper.createObjectNode();
	
		ObjectNode key = queryAggNode.putObject("tagcloud");
		ObjectNode term = key.putObject("terms");
		term.put("field", TwitterSettings.TEXT_FIELD);
		term.put("size", size);
		
		if(stopwords!=null)
		{
			ArrayNode exc = term.arrayNode();
			for (String s : stopwords) {
				exc.add(s);
			}	
			term.put("exclude",exc);
		}
		
		return queryAggNode;
	}
	
	/******************* DASHBOARD AGGREGATIONS ******************************************************************/

	static ObjectNode createMissingDataAggregation(String field)
	{	

		ObjectNode missingAggNode = jsonMapper.createObjectNode();
		ObjectNode key = missingAggNode.putObject("missing_fields");
		ObjectNode term = key.putObject("missing");
		term.put("field", field);
		return missingAggNode;
	}
	
	static ObjectNode createEntityAggregation(String field, String aggname)
	{	
		
		ObjectNode queryAggNode = jsonMapper.createObjectNode();
		ObjectNode key = queryAggNode.putObject(aggname);
		ObjectNode term = key.putObject("terms");
		term.put("field", field);
		//term.put("missing","N/A"); 2.3

		term.put("size", TwitterSettings.TOP_ENTITIES_SIZE);
		ObjectNode order = jsonMapper.createObjectNode();
		order.put("_count", "desc");
		term.put("order",order);
		
		
	/*	when id was dublicated linked indexec _count should be replaced by timelinesum
	 * String js =   " { \"timelinesum\" : { \"cardinality\" :{\"field\" : \"id\"}} } ";
		try {
			JsonNode sumagg = jsonMapper.readTree(js);
			key.put("aggs", sumagg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		return queryAggNode;
	}
	
	
}
