package fr.ina.dlweb.dowser.ws.twitter.rest.es;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheStats;

import fr.ina.dlweb.conf.Conf;
import fr.ina.dlweb.conf.ConfNode;
import fr.ina.dlweb.conf.Settings;
import fr.ina.dlweb.dowser.ws.commons.es.ESCachingSearchClient;
import fr.ina.dlweb.dowser.ws.commons.es.ESCachingSearchClient.CacheType;
import fr.ina.dlweb.dowser.ws.commons.es.ESSearchClient;
import fr.ina.dlweb.dowser.ws.commons.es.ESSearchClient.ESResponse;
import fr.ina.dlweb.dowser.ws.twitter.model.IFaceModel;
import fr.ina.dlweb.dowser.ws.twitter.model.request.TwitterSearchRequest;
import fr.ina.dlweb.dowser.ws.twitter.model.request.TwitterTrendsSearchRequest;
import fr.ina.dlweb.dowser.ws.twitter.model.response.CacheInfoResponse;
import fr.ina.dlweb.dowser.ws.twitter.model.response.InfoResponse;
import fr.ina.dlweb.dowser.ws.twitter.model.response.InfoResponse.IndexAvailability;
import fr.ina.dlweb.dowser.ws.twitter.model.response.TwitterSearchResponse;
import fr.ina.dlweb.dowser.ws.twitter.model.response.TwitterTimelineBuckets;
import fr.ina.dlweb.dowser.ws.twitter.model.response.TwitterTopEntityBuckets;
import fr.ina.dlweb.dowser.ws.twitter.model.response.TwitterTrendsHitsResponse;
import fr.ina.dlweb.dowser.ws.twitter.rest.iface.IFaceSearcher;

@Singleton
@Path("/es")
public class TwitterESSearcher implements IFaceSearcher {

	
	private final static Logger Log = LoggerFactory.getLogger(TwitterESSearcher.class);	
	
	private Conf conf;
	// primary index with content
	private ConfNode esHostsSpec;
	private ObjectMapper jsonMapper;

	private Map<String, List<String>> esHosts;
	private Map<String, ESCachingSearchClient> esClients;

	private String esVersion = null;
	
	public TwitterESSearcher() {
		jsonMapper = new ObjectMapper();
		jsonMapper.enable(SerializationConfig.Feature.INDENT_OUTPUT);	
		conf = Settings.getConf();
		esHostsSpec = conf.get(TwitterSettings.INDEX_HOSTS);



		initialize();
		TwitterSettings.InitSettings();
		
	}
	
	
	protected void initialize() {
		
		esHosts = new LinkedHashMap<String, List<String>>();
		esClients = new HashMap<String, ESCachingSearchClient>();			

		for(ConfNode esHost : esHostsSpec) {
			
			String cluster = esHost.get("cluster").asText();
			String host = esHost.get("host").asText();
			
			List<String> hosts = esHosts.get(cluster);
			if(hosts == null) {
				hosts = new ArrayList<String>();
				esHosts.put(cluster, hosts);
			}
			hosts.add(host);
			

		}
		
		for(String cluster : esHosts.keySet()) {
			List<String> hosts = esHosts.get(cluster);			
			esClients.put(cluster,  new ESCachingSearchClient(false, ESSearchClient.NodePeekPolicy.RoundRobin, hosts.toArray(new String[0])));
		}
	}
	
	@Override
	@GET
	@Path("/dowser/info")
	@Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
	public InfoResponse info(@QueryParam("cluster") String cluster) throws IOException {
		
		if(cluster == null) {
			cluster = esHosts.keySet().iterator().next();
		}
		
		ESSearchClient esClient = esClients.get(cluster);
		Log.info("dowser/twitterinfo request");
		ESResponse esResponse = esClient.clusterHealth(null);//esIndex removed from settings coming from request 
		Map result = esResponse.result();
		
		int live_index_count = (Integer)result.get("active_primary_shards");
		int dead_index_count = (Integer)result.get("unassigned_shards");
		int total_index_count = live_index_count + dead_index_count;
		
		InfoResponse info = new InfoResponse();
		info.setTotal_index_count(total_index_count)
		    .setLive_index_count(live_index_count)
		    .setDead_index_count(dead_index_count);
		
		if(live_index_count == 0) {
			info.setIndex_availability(IndexAvailability.none);
		} else
		if(dead_index_count > 0) {
			info.setIndex_availability(IndexAvailability.partial);
		} else {
			info.setIndex_availability(IndexAvailability.full);
		}
		
		Log.info("dowser/twitterinfo response : {}", info.getIndex_availability());
		
		return info;
	}

	@Override
	@POST
	@Path("/dowser/cache_reset")
	public CacheInfoResponse resetCache(@QueryParam("cluster") String cluster, @QueryParam("cache_type") String cacheType) throws IOException {
		if(cluster == null) {
			cluster = esHosts.keySet().iterator().next();
		}
		
		ESCachingSearchClient esClient = esClients.get(cluster);
		if(null != esClient) {
			CacheType[] cacheTypes;
			
			if("*".equals(cacheType)) {
				cacheTypes = CacheType.values();
			} else {
				try {
					cacheTypes = new CacheType[] {CacheType.valueOf(cacheType)};
				} catch(Exception e) {
					// null or bad cacheType
					cacheTypes = new CacheType[] {};
				}
			}
			
			for(CacheType cacheTypeE : cacheTypes) {
				Cache<String, ESResponse> cache = esClient.getCache(cacheTypeE);
				if(null == cache) continue;
				cache.invalidateAll();
			}
		}
		return cacheInfo(false);	
	}
	
	@Override
	@GET
	@Path("/dowser/cache_info")
	@Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")	
	public CacheInfoResponse cacheInfo(@DefaultValue("false") @QueryParam("include_data") boolean wantsCacheData) {
		
		CacheInfoResponse response = new CacheInfoResponse();
		response.setCache_infos(new ArrayList<CacheInfoResponse.CacheInfo>());
		
		for(String cluster : esClients.keySet()) {
			
			ESCachingSearchClient esClient = esClients.get(cluster);
			for(CacheType cacheType : CacheType.values()) {
				
				Cache<String, ESResponse> cache = esClient.getCache(cacheType);
				if(null != cache) {
					
					CacheInfoResponse.CacheInfo cacheInfo = new CacheInfoResponse.CacheInfo();
					cacheInfo.setCluster(cluster)
					          .setType(cacheType.name());
					cacheInfo.setCacheSize(cache.size());		
					CacheStats stats = cache.stats();
					CacheInfoResponse.Stats statsInfo = new CacheInfoResponse.Stats();
					statsInfo.setAvgLoadPenalty(stats.averageLoadPenalty())
					         .setEvictionCount(stats.evictionCount())
					         .setHitCount(stats.hitCount())
					         .setHitRate(stats.hitRate())
					         .setLoadCount(stats.loadCount())
					         .setLoadExceptionCount(stats.loadExceptionCount())
					         .setLoadExceptionRate(stats.missRate())
					         .setLoadSuccessCount(stats.loadSuccessCount())
					         .setMissCount(stats.missCount())
					         .setMissRate(stats.missRate())
					         .setRequestCount(stats.requestCount())
					         .setTotalLoadTime(stats.totalLoadTime());
					cacheInfo.setStats(statsInfo);
					if(wantsCacheData) {
						HashMap<String, String> cacheData = new LinkedHashMap<String, String>();
						for(Map.Entry<String, ESResponse> entry : cache.asMap().entrySet()) {
							cacheData.put(entry.getKey(), entry.getValue().json());
						}
						cacheInfo.setCacheData(cacheData);
					}
					response.getCache_infos().add(cacheInfo);
				}
			}			
		}
		return response;
	}


	
	@Override
	@POST
	@Path("/dowser/search")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
	public TwitterSearchResponse twitter_dowser(TwitterSearchRequest searchRequest) throws IOException {

		
		boolean useCache = searchRequest.isUse_cache();
	
		searchRequest.checkRequest();
		String index = searchRequest.getIndex();
		String type = searchRequest.getIndex_type();
		
		String esSearchRequest = TwitterESRequest.toESSearchRequest(searchRequest, getEsVersion());
		String cacheKey = "{\"index\":"+index+", \"type\":"+type+"}\n"+esSearchRequest;
		String requestInfo = index+"/"+type+": "+searchRequest.getQuery();
		
		Log.info("dowser/twitter_dowser request : {} ", requestInfo);
		mayLogDebugResponseOrRequest("dowser/twitter_dowser request : {} ", searchRequest);
		
//		String esSearchResponse = searchInternal(searchRequest.getCluster(), searchRequest.getQuery(), esSearchRequest, useCache ? CacheType.STANDARD : CacheType.NONE, index, esType);
		String esSearchResponse = searchInternal(searchRequest.getCluster(), requestInfo, index, type, esSearchRequest, cacheKey, useCache ? CacheType.STANDARD : CacheType.NONE);
		TwitterSearchResponse response = TwitterESResponse.fromESSearchResponse(esSearchResponse, searchRequest);
		
		Log.info("dowser/twitter_dowser response : {} ", searchRequest.getQuery());
		mayLogDebugResponseOrRequest("dowser/twitter_dowser response : {} ", jsonMapper.writeValueAsString(response));

		return response;
	}
	
	
	
	@Override
	@POST
	@Path("/dowser/twitter_info")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
	public String twitter_info(TwitterSearchRequest searchRequest) throws IOException {
		
		boolean useCache = searchRequest.isUse_cache();
		searchRequest.checkRequest();
		
		if(searchRequest.getCollection()!=null && searchRequest.getCollection().isEmpty())
			searchRequest.setCollection("dlweb;attentats;elections2017");
		
		
		searchRequest.setSize(1);
		searchRequest.setFrom(0);
		searchRequest.setSort_field("created_at");
		searchRequest.setSort_type("asc");
		
		
		String index = searchRequest.getIndex();
		if(index == null )
			index = "twitter_02";
		String type = "tweets";

		if(index != null) {
			
			
			String esSearchRequest = TwitterESRequest.toESSearchRequest(searchRequest,getEsVersion());
			
			
			String cacheKey = "{\"index\":"+index+", \"type\":"+type+"}\n"+esSearchRequest;
			String requestInfo = index+"/"+type+": "+searchRequest.getQuery();
			
			Log.info("dowser/twitter_info request : {} ", searchRequest.getQuery());
	
			mayLogDebugResponseOrRequest("dowser/twitter_info request : {} ", searchRequest);
//			String esSearchResponse = searchInternal(searchRequest.getCluster(), searchRequest.getQuery(), esSearchRequest, useCache ? CacheType.STANDARD : CacheType.NONE, index, type);
			String esSearchResponseFirst = searchInternal(searchRequest.getCluster(), requestInfo, index, type, esSearchRequest, cacheKey, useCache ? CacheType.STANDARD : CacheType.NONE);

			Log.info("dowser/twitter_info response : {} ", requestInfo);

			
			
			searchRequest.setSort_type("desc");
			esSearchRequest = TwitterESRequest.toESSearchRequest(searchRequest,getEsVersion());
			cacheKey = "{\"index\":"+index+", \"type\":"+type+"}\n"+esSearchRequest;
			requestInfo = index+"/"+type+": "+searchRequest.getQuery();
			
			Log.info("dowser/twitter_info request : {} ", searchRequest.getQuery());
			
			String esSearchResponseLast = searchInternal(searchRequest.getCluster(), requestInfo, index, type, esSearchRequest, cacheKey, useCache ? CacheType.STANDARD : CacheType.NONE);
	
			String response = TwitterESResponseUtils.getTwitterInfo(esSearchResponseFirst, esSearchResponseLast);
			
			return response;
		}
		return null;
		

	}
	
	
	@Override
	@POST
	@Path("/dowser/timeline")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
	public TwitterTimelineBuckets[] twitter_timeline(TwitterSearchRequest searchRequest) throws IOException {
		
		boolean useCache = searchRequest.isUse_cache();
		searchRequest.checkRequest();
		
		String index = searchRequest.getIndex();
		String type = searchRequest.getIndex_type();

		if(index != null) {
			String esSearchRequest ;
			
			if(searchRequest.getTimeline_type().contains("estimation"))
			{
				type = searchRequest.getIndex_type();
				esSearchRequest = TwitterESRequest.toESEstimatedTimelineRequest(searchRequest);
				searchRequest.setCluster("estimation");
			}
			else {
				esSearchRequest = TwitterESRequest.toESTimelineRequest(searchRequest);
			}
			
			String cacheKey = "{\"index\":"+index+", \"type\":"+type+"}\n"+esSearchRequest;
			String requestInfo = index+"/"+type+": "+searchRequest.getQuery();
			
			Log.info("dowser/twitter_timeline request : {} ", searchRequest.getQuery());
	
			mayLogDebugResponseOrRequest("dowser/twitter_timeline request : {} ", searchRequest);
//			String esSearchResponse = searchInternal(searchRequest.getCluster(), searchRequest.getQuery(), esSearchRequest, useCache ? CacheType.STANDARD : CacheType.NONE, index, type);
			String esSearchResponse = searchInternal(searchRequest.getCluster(), requestInfo, index, type, esSearchRequest, cacheKey, useCache ? CacheType.STANDARD : CacheType.NONE);
			TwitterTimelineBuckets[] buckets = TwitterESResponse.fromESTimelineResponse(esSearchResponse);
			Log.info("dowser/twitter_timeline response : {} ", requestInfo);
			mayLogDebugResponseOrRequest("dowser/twitter_timeline response : {} ", jsonMapper.writeValueAsString(buckets));
			return buckets;
		}
		// estimation index can be null in some case
		else {
			return null;
		}

	}
	
	@Override
	@POST
	@Path("/dowser/trends_timeline")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
	public HashMap<String,ArrayList<TwitterTrendsHitsResponse> > twitter_trends_timeline(TwitterTrendsSearchRequest searchRequest) throws IOException {
		
		boolean useCache = searchRequest.isUse_cache();
		
		
		String index = searchRequest.getIndex();
		String type = searchRequest.getIndex_type();

		
		
		String esSearchRequest = TwitterESRequest.toESTrendsSearchRequest(searchRequest);
		String cacheKey = "{\"index\":"+index+", \"type\":"+type+"}\n"+esSearchRequest;
		String requestInfo = index+"/"+type+": "+searchRequest.getQuery();
		
		Log.info("dowser/twitter_trends_timeline request : {} ", requestInfo);

		mayLogDebugResponseOrRequest("dowser/twitter_trends_timeline twitter_trends_timeline : {} ", searchRequest);
//		String esSearchResponse = searchInternal(searchRequest.getCluster(), searchRequest.getQuery(), esSearchRequest, useCache ? CacheType.STANDARD : CacheType.NONE, index, type);
		String esSearchResponse = searchInternal(searchRequest.getCluster(), requestInfo, index, type, esSearchRequest, cacheKey, useCache ? CacheType.STANDARD : CacheType.NONE);
		HashMap<String,ArrayList<TwitterTrendsHitsResponse> > hits = TwitterESResponse.fromESTrendsResponse(esSearchResponse);
		Log.info("dowser/twitter_trends_timeline response : {} ", requestInfo);
		mayLogDebugResponseOrRequest("dowser/twitter_trends_timeline response : {} ", jsonMapper.writeValueAsString(hits));
		return hits;
	}
	
	
	@Override
	@POST
	@Path("/dowser/dashboard")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
	public TwitterTopEntityBuckets[] twitter_dashboard(TwitterSearchRequest searchRequest) throws IOException {
		boolean useCache = searchRequest.isUse_cache();
		searchRequest.checkRequest();
		
		String index = searchRequest.getIndex();
		String type = searchRequest.getIndex_type();
		
		String esSearchRequest = TwitterESRequest.toESDashboardRequest(searchRequest);
		String cacheKey = "{\"index\":"+index+", \"type\":"+type+"}\n"+esSearchRequest;
		String requestInfo = index+"/"+type+": "+searchRequest.getQuery();
		
		Log.info("dowser/twitter_dashboard request : {} ", requestInfo);
		mayLogDebugResponseOrRequest("dowser/twitter_dashboard request : {} ", searchRequest);
//		String esSearchResponse = searchInternal(searchRequest.getCluster(), searchRequest.getQuery(), esSearchRequest, useCache ? CacheType.STANDARD : CacheType.NONE, index, esType);
		String esSearchResponse = searchInternal(searchRequest.getCluster(), requestInfo, index, type, esSearchRequest, cacheKey, useCache ? CacheType.STANDARD : CacheType.NONE);
		TwitterTopEntityBuckets[] buckets =  TwitterESResponse.fromESDashboardResponse(esSearchResponse, searchRequest);
		Log.info("dowser/twitter_dashboard response : {} ", requestInfo);
		mayLogDebugResponseOrRequest("dowser/twitter_dashboard response : {} ", jsonMapper.writeValueAsString(buckets));
		return buckets;
	}
	

	
	
	@Override
	@POST
	@Path("/dowser/wordcloud")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
	public TwitterTopEntityBuckets[] twitter_wordcloud(TwitterSearchRequest searchRequest) throws IOException {

		
		boolean useCache = searchRequest.isUse_cache();
		String index = searchRequest.getIndex();
		String type = searchRequest.getIndex_type();

		searchRequest.checkRequest();
//		searchRequest.setTimelineType(-1);
		
		String esSearchRequest = TwitterESRequest.toESWordCountRequest(searchRequest);
		String cacheKey = "{\"index\":"+index+", \"type\":"+type+"}\n"+esSearchRequest;
		String requestInfo = index+"/"+type+": "+searchRequest.getQuery();		
		
		
		
		Log.info("dowser/twitter_wordcloud request : {} ", requestInfo);
		mayLogDebugResponseOrRequest("dowser/twitter_wordcloud request : {} ", searchRequest);
//		String esSearchResponse = searchInternal(searchRequest.getCluster(), searchRequest.getQuery(), esSearchRequest, useCache ? CacheType.STANDARD : CacheType.NONE, index, esType);
		String esSearchResponse = searchInternal(searchRequest.getCluster(), requestInfo, index, type, esSearchRequest, cacheKey, useCache ? CacheType.STANDARD : CacheType.NONE);
		TwitterTopEntityBuckets[] buckets =  TwitterESResponse.fromESWordCloudResponse(esSearchResponse, searchRequest);
		
		Log.info("dowser/twitter_iramuteq response : {} ", requestInfo);
		mayLogDebugResponseOrRequest("dowser/twitter_iramuteq response : {} ", jsonMapper.writeValueAsString(buckets));
		
		return buckets;
	}
	
	@Override
	@POST
	@Path("/dowser/iramuteq")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
	public String twitter_iramuteq(TwitterSearchRequest searchRequest) throws IOException {

		boolean useCache = searchRequest.isUse_cache();
		searchRequest.checkRequest();
		searchRequest.setSize(10000);
		String index = searchRequest.getIndex();
		String type = searchRequest.getIndex_type();
		
		String esSearchRequest = TwitterESRequest.toESSearchRequest(searchRequest, getEsVersion());
		String cacheKey = "{\"index\":"+index+", \"type\":"+type+"}\n"+esSearchRequest;
		String requestInfo = index+"/"+type+": "+searchRequest.getQuery();
		
		Log.info("dowser/twitter_iramuteq request : {} ", requestInfo);
		mayLogDebugResponseOrRequest("dowser/search twitter_iramuteq : {} ", searchRequest);
//		String esSearchResponse = searchInternal(searchRequest.getCluster(), searchRequest.getQuery(), esSearchRequest, useCache ? CacheType.STANDARD : CacheType.NONE, index, esType);
		String esSearchResponse = searchInternal(searchRequest.getCluster(), requestInfo, index, type, esSearchRequest, cacheKey, useCache ? CacheType.STANDARD : CacheType.NONE);
		String response = TwitterESResponse.fromESIramuteqResponse(esSearchResponse, searchRequest);
		Log.info("dowser/twitter_iramuteq response : {} ", requestInfo);
		mayLogDebugResponseOrRequest("dowser/twitter_iramuteq response : {} ", response);
		
		return response;
	}
	@Override
	@POST
	@Path("/dowser/network")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
	public String twitter_network(TwitterSearchRequest searchRequest) throws IOException {

		
		boolean useCache = searchRequest.isUse_cache();
		searchRequest.checkRequest();
		searchRequest.setSize(200000);
		String index = searchRequest.getIndex();
		String type = searchRequest.getIndex_type();
		
		String esSearchRequest = TwitterESRequest.toESSearchRequest(searchRequest, getEsVersion());
		String cacheKey = "{\"index\":"+index+", \"type\":"+type+"}\n"+esSearchRequest;
		String requestInfo = index+"/"+type+": "+searchRequest.getQuery();
		
		
		Log.info("dowser/twitter_network request : {} ", requestInfo);
		mayLogDebugResponseOrRequest("dowser/twitter_network request : {} ", searchRequest);
		//String esSearchResponse = searchInternal(searchRequest.getCluster(), searchRequest.getQuery(), esSearchRequest, useCache ? CacheType.STANDARD : CacheType.NONE, index, esType);		
		String esSearchResponse = searchInternal(searchRequest.getCluster(), requestInfo, index, type, esSearchRequest, cacheKey, useCache ? CacheType.STANDARD : CacheType.NONE);
		String response = TwitterESResponse.fromESNetworkResponse(esSearchResponse, searchRequest);
		Log.info("dowser/twitter_network response : {} ", requestInfo);
		mayLogDebugResponseOrRequest("dowser/twitter_network response : {} ", response);
		
		return response;
	}
	
	@Override
	@POST
	@Path("/dowser/analyze")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
	public TwitterSearchResponse twitter_analyze(TwitterSearchRequest searchRequest) throws IOException {

		
		boolean useCache = searchRequest.isUse_cache();
		searchRequest.checkRequest();
		String index = searchRequest.getIndex();
		String type = searchRequest.getIndex_type();
		
		//// TEST CLUSTERING ALGO
	//	searchRequest.setTimelineType(TwitterSettings.REAL_TIMELINE);
		//String esSearchRequest = TwitterESRequest.toESClusteringRequest(searchRequest);
		//String esSearchResponse = searchInternal(searchRequest.getCluster(), searchRequest.getQuery(), esSearchRequest, useCache ? CacheType.STANDARD : CacheType.NONE, index,esType);
	//	TwitterESResponse.fromESClusteringResponse( esSearchResponse  );
		
		String esSearchRequest = TwitterESRequest.toESAnalyzeRequest(searchRequest);
		String cacheKey = "{\"index\":"+index+", \"type\":"+type+"}\n"+esSearchRequest;
		String requestInfo = index+"/"+type+": "+searchRequest.getQuery();
		
		Log.info("dowser/twitter_analyze request : {} ", requestInfo);
		mayLogDebugResponseOrRequest("dowser/twitter_analyze request : {} ", searchRequest);
		String esSearchResponse = searchInternal(searchRequest.getCluster(), requestInfo, index, type, esSearchRequest, cacheKey, useCache ? CacheType.STANDARD : CacheType.NONE);
	    TwitterSearchResponse response = TwitterESResponse.fromESAnalyzeResponse(esSearchResponse);
		Log.info("dowser/twitter_analyze response : {} ", requestInfo);
		mayLogDebugResponseOrRequest("dowser/twitter_analyze response : {} ", jsonMapper.writeValueAsString(response));
		return response;
	}
	
	

	

	private String getEsVersion() throws IOException {
		if(esVersion == null) {
			
			String cluster = esHosts.keySet().iterator().next();
			ESSearchClient esClient = esClients.get(cluster);
			esVersion = esClient.version();
			
		}
		return esVersion;
	}
	
	private void mayLogDebugResponseOrRequest(String format, Object... args) throws IOException {

		if(Log.isDebugEnabled()) {
			for(int i=0; i<args.length; i++) {
				if(args[i] instanceof IFaceModel) {
					args[i] = jsonMapper.writeValueAsString(args[i]); 
				}
			}
			Log.debug(format, args);
		}
	}
	

	protected String searchInternal(String cluster,String requestInfo,  String index, String type, String esRequest, String cacheKey, CacheType cacheType) throws IOException { 
		
		// if no cluster set, take the first one
		if(cluster == null) {
			cluster = esHosts.keySet().iterator().next();
		}
		
		ESCachingSearchClient esClient = esClients.get(cluster);
		
		Log.info("searchInternal ask request {}", requestInfo);
		ESResponse esResponse = esClient.cachingSearch(index, type, esRequest, cacheKey, cacheType);
		Log.info("searchInternal get response {}", requestInfo);
		return esResponse.json();
	}
	
//	protected String searchInternal2(String cluster, String requestInfo, String esRequest, CacheType cacheType, String index, String indextype) throws IOException {
//
//		// if no cluster set, take the first one
//		if(cluster == null) {
//			cluster = esHosts.keySet().iterator().next();
//		}
//		Cache<String, String> cache = getCache(cluster, cacheType);
//		
//		ESSearchClient esClient = esClients.get(cluster);
//		
//		String cacheKey = "{\"index\":"+index+", \"type\":"+indextype+"}\n"+esRequest;
//		
//		
//		String esResponseJson = null;
//		if(cache != null) {
//			esResponseJson = cache.getIfPresent(cacheKey);
//		}
//
//		//	TODO REMOVE LATER JUST TO TESTE ESTIMATION?
//		//esResponseJson = null;
//		if(esResponseJson == null) {
//			
//			Cache<String, String> pendingRequestCache = getCache(cluster, CacheType.PENDING_REQ);
//			Cache<String, String> pendingResponseCache = getCache(cluster, CacheType.PENDING_RESP);
//			boolean isPending = (pendingRequestCache.getIfPresent(cacheKey) != null);
//			
//			if(!isPending) {
//				Log.info("searchInternal ask request {}", requestInfo);
//				pendingRequestCache.put(cacheKey, "yes");
//				ESResponse esResponse = esClient.search(index, indextype, esRequest);
//				Log.info("searchInternal get response {}", requestInfo);
//				if(cache != null) {
//					// cache only success
//					if(esResponse.status() >= 200 && esResponse.status() < 300) {
//						cache.put(cacheKey, esResponse.json());
//					}
//				}
//				pendingResponseCache.put(cacheKey, esResponse.json());
//				pendingRequestCache.invalidate(cacheKey);
//				esResponseJson = esResponse.json();
//			} else {
//				
//				Log.info("searchInternal wait pending request {}", requestInfo);
//				while(isPending) {
//					try {Thread.sleep(200);} catch(InterruptedException ie) {}
//					isPending = (pendingRequestCache.getIfPresent(cacheKey) != null);
//				}
//				Log.info("searchInternal get pending response {}", requestInfo);
//				esResponseJson = pendingResponseCache.getIfPresent(cacheKey);
//			}
//		} else {
//			Log.info("searchInternal ask request from "+cacheType.toString()+" cache {}", requestInfo);
//			Log.info("searchInternal get response from "+cacheType.toString()+" cache {}", requestInfo);
//		}
//		return esResponseJson;
//	}
	
	protected Cache<String, ESSearchClient.ESResponse> getCache(String cluster, CacheType cacheType) {		
		return esClients.get(cluster).getCache(cacheType);
	}


}
