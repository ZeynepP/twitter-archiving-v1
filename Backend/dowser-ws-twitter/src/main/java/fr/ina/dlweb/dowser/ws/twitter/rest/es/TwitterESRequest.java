package fr.ina.dlweb.dowser.ws.twitter.rest.es;

import java.io.IOException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.joda.time.DateTimeZone;

import fr.ina.dlweb.dowser.ws.twitter.model.request.TwitterSearchRequest;
import fr.ina.dlweb.dowser.ws.twitter.model.request.TwitterTrendsSearchRequest;


public class TwitterESRequest {
	


		public static String toESSearchRequest(TwitterSearchRequest request, String esVersion) {
			
			
				ObjectNode esRequest = TwitterESRequestUtils.jsonMapper.createObjectNode();
			
			  
			    ArrayNode esSorts = esRequest.putArray("sort");
				//Sort sort = request.getSort();
				ObjectNode esSort = esSorts.addObject();
				esSort.put(request.getSort_field(), request.getSort_type());
				esRequest.put("from", request.getFrom());
				esRequest.put("size", request.getSize());
				
				
				//removing source_path because of security
				ObjectNode esSourceExclude = TwitterESRequestUtils.jsonMapper.createObjectNode();
				esSourceExclude.put("exclude", "source_path");
				esRequest.put("_source", esSourceExclude);

				ObjectNode rootQuery = esRequest.putObject("query");
				ObjectNode filteredQuery = TwitterESRequestUtils.createTextQuery(request.getQuery(),true);
				filteredQuery.put("filter", TwitterESRequestUtils.createFilters(request, request.DATE_FIELD));
				rootQuery.put("filtered", filteredQuery);
				

				try {
					return TwitterESRequestUtils.jsonMapper.writeValueAsString(esRequest);
				} catch(Exception e) {
					throw new RuntimeException("Can't write json toESSearchRequest ", e);
				}
			
		}
		
		
		
		public static String toESEstimatedTimelineRequest(TwitterSearchRequest request)
		{
			
				ObjectNode esRequest = TwitterESRequestUtils.jsonMapper.createObjectNode();
				esRequest.put("from", 0);
				esRequest.put("size", 0);
				ObjectNode rootQuery = esRequest.putObject("query");
				ObjectNode filteredQuery = TwitterESRequestUtils.createTextQuery(null,true);
				filteredQuery.put("filter", TwitterESRequestUtils.createFiltersEstimation(request, request.DATE_FIELD));
				rootQuery.put("filtered", filteredQuery);
				esRequest.put("aggs",TwitterESRequestUtils.createTimelineNodeEstimation(request));
		
				try {
					return TwitterESRequestUtils.jsonMapper.writeValueAsString(esRequest);
				} catch(Exception e) {
					throw new RuntimeException("Can't write json toESEstimatedTimelineRequest ", e);
				}
			
		}
		
		
		public static String toESTimelineRequest(TwitterSearchRequest request) {
			
			
				ObjectNode esRequest = TwitterESRequestUtils.jsonMapper.createObjectNode();
				esRequest.put("from", 0);
				esRequest.put("size", 0);
				ObjectNode rootQuery = esRequest.putObject("query");
				ObjectNode filteredQuery = TwitterESRequestUtils.createTextQuery(request.getQuery(),true);
				filteredQuery.put("filter", TwitterESRequestUtils.createFilters(request,request.DATE_FIELD));
				rootQuery.put("filtered", filteredQuery);
				esRequest.put("aggs",TwitterESRequestUtils.createTimelineAggs(request.getTime_interval(), request.getTime_zone(), request.DATE_FIELD , request.getDate_start(), request.getDate_stop()));
		
				try {
					return TwitterESRequestUtils.jsonMapper.writeValueAsString(esRequest);
				} catch(Exception e) {
					throw new RuntimeException("Can't write json toESTimelineRequest ", e);
				}
		
		}
		
		public static String toESTrendsSearchRequest(TwitterTrendsSearchRequest request) {
			
			
			ObjectNode esRequest = TwitterESRequestUtils.jsonMapper.createObjectNode();
			esRequest.put("size", 15000);
			ArrayNode esSorts = esRequest.putArray("sort");
			//Sort sort = request.getSort();
			ObjectNode esSort = esSorts.addObject();
			esSort.put(request.getSort_field(), request.getSort_type());
			
			ObjectNode rootQuery = esRequest.putObject("query");
			ObjectNode filteredQuery = TwitterESRequestUtils.createTextQuery(request.getQuery(),false);
			filteredQuery.remove("fields"); // because twitter fields do not correspond to trends fields
			
			
			
			ObjectNode filter = TwitterESRequestUtils.jsonMapper.createObjectNode();
			ObjectNode boolfilter = filter.putObject("bool");
			ArrayNode mustfilters	= boolfilter.putArray("must");
			
			mustfilters.add(TwitterESRequestUtils.createRangeFilter(request.getDate_start(), request.getDate_stop(),request.DATE_FIELD));
			mustfilters.add(TwitterESRequestUtils.createRangeFilter(0,  Long.valueOf(request.getTopK()),"rank"));
			
			
			
			filteredQuery.put("filter", filter);
			rootQuery.put("filtered", filteredQuery);
			
			
		
			
			
	
			try {
				return TwitterESRequestUtils.jsonMapper.writeValueAsString(esRequest);
			} catch(Exception e) {
				throw new RuntimeException("Can't write json toESTrendsSearchRequest ", e);
			}
	
	  }
		
		
		
		public static String toESDashboardRequest(TwitterSearchRequest request) {
			
			
			ObjectNode esRequest = TwitterESRequestUtils.jsonMapper.createObjectNode();
			esRequest.put("from", 0);
			esRequest.put("size", 0);

			ObjectNode rootQuery = esRequest.putObject("query");
			ObjectNode filteredQuery = TwitterESRequestUtils.createTextQuery(request.getQuery(),true);
			filteredQuery.put("filter", TwitterESRequestUtils.createFilters(request,request.DATE_FIELD));
	
			rootQuery.put("filtered", filteredQuery);
			
			String fieldname = TwitterSettings.mapESAggregationFields.get(request.getDashboard_type());

			ObjectNode aggsObj = esRequest.putObject("aggs");
			aggsObj.putAll(TwitterESRequestUtils.createEntityAggregation(fieldname,request.getDashboard_type() ));
			aggsObj.putAll(TwitterESRequestUtils.createMissingDataAggregation(fieldname ));
		
			try {
				return TwitterESRequestUtils.jsonMapper.writeValueAsString(esRequest);
			} catch(Exception e) {
				throw new RuntimeException("Can't write json toESSearchRequest ", e);
			}
			
	
		}
		
		
		
		public static String toESClusteringRequest(TwitterSearchRequest request) {
			
			
			ObjectNode esRequest = TwitterESRequestUtils.jsonMapper.createObjectNode();
			esRequest.put("from", 0);
			esRequest.put("size", 0);

			ObjectNode rootQuery = esRequest.putObject("query");
			ObjectNode filteredQuery = TwitterESRequestUtils.createTextQuery(request.getQuery(),true);
			filteredQuery.put("filter", TwitterESRequestUtils.createFilters(request,request.DATE_FIELD));
			rootQuery.put("filtered", filteredQuery);
			esRequest.put("aggs",TwitterESRequestUtils.createClusteringAggregation(request));
			try {
				return TwitterESRequestUtils.jsonMapper.writeValueAsString(esRequest);
			} catch(Exception e) {
				throw new RuntimeException("Can't write json toESSearchRequest ", e);
			}
			
	
		}
		
		public static String toESAnalyzeRequest(TwitterSearchRequest request) {
			
			
			ObjectNode esRequest = TwitterESRequestUtils.jsonMapper.createObjectNode();
			esRequest.put("from", 0);
			esRequest.put("size", 0);

			ObjectNode rootQuery = esRequest.putObject("query");
			ObjectNode filteredQuery = TwitterESRequestUtils.createTextQuery(request.getQuery(),true);
			ObjectNode filters = TwitterESRequestUtils.createFilters(request, request.DATE_FIELD);
			
			if(request.getAnalyze_request().getTexte() != null)
			{
				ArrayNode updatedfilters = (ArrayNode)filters.get("bool").get("must");
				updatedfilters.add(TwitterESRequestUtils.createTextQuery(request.getAnalyze_request().getTexte(),true));
			}
			
			filteredQuery.put("filter", filters);
			
			
			rootQuery.put("filtered", filteredQuery);
			esRequest.put("aggs",TwitterESRequestUtils.createAnalyzeAggregation(request));
			
			try {
				return TwitterESRequestUtils.jsonMapper.writeValueAsString(esRequest);
			} catch(Exception e) {
				throw new RuntimeException("Can't write json toESSearchRequest ", e);
			}
			
	
		}
		
	
		
		
		public static String toESWordCountRequest(TwitterSearchRequest request) {
			
			
			ObjectNode esRequest = TwitterESRequestUtils.jsonMapper.createObjectNode();
			esRequest.put("from", 0);
			esRequest.put("size", 0);

			ObjectNode rootQuery = esRequest.putObject("query");
			ObjectNode filteredQuery = TwitterESRequestUtils.createTextQuery(request.getQuery(),true);
			filteredQuery.put("filter", TwitterESRequestUtils.createFilters(request, request.DATE_FIELD));
			rootQuery.put("filtered", filteredQuery);
			esRequest.put("aggs",TwitterESRequestUtils.createWordCloudAggregation(request.getWordcloudsize(),request.wordcloudstopwords));
			try {
				return TwitterESRequestUtils.jsonMapper.writeValueAsString(esRequest);
			} catch(Exception e) {
				throw new RuntimeException("Can't write json toESSearchRequest ", e);
			}
			
	
		}	
		
		
		
		
		

		
	

		
		

		

		

		
		
		
		
		
		

		


	
}
