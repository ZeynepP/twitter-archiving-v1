package fr.ina.dlweb.dowser.ws.twitter.rest.iface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import fr.ina.dlweb.dowser.ws.twitter.model.request.TwitterSearchRequest;
import fr.ina.dlweb.dowser.ws.twitter.model.request.TwitterTrendsSearchRequest;
import fr.ina.dlweb.dowser.ws.twitter.model.response.CacheInfoResponse;
import fr.ina.dlweb.dowser.ws.twitter.model.response.InfoResponse;
import fr.ina.dlweb.dowser.ws.twitter.model.response.TwitterSearchResponse;
import fr.ina.dlweb.dowser.ws.twitter.model.response.TwitterTimelineBuckets;
import fr.ina.dlweb.dowser.ws.twitter.model.response.TwitterTopEntityBuckets;
import fr.ina.dlweb.dowser.ws.twitter.model.response.TwitterTrendsHitsResponse;


public interface IFaceSearcher {

	public CacheInfoResponse cacheInfo(boolean includeData) throws IOException;
	
	public CacheInfoResponse resetCache(String cluster, String cacheType) throws IOException;
	
	public TwitterSearchResponse twitter_dowser(TwitterSearchRequest request) throws IOException;
	
	public InfoResponse info(String cluster) throws IOException;

	public TwitterSearchResponse twitter_analyze(TwitterSearchRequest searchRequest) throws IOException;
	
	public TwitterTopEntityBuckets[] twitter_wordcloud(TwitterSearchRequest searchRequest) throws IOException;
	
	public String twitter_iramuteq(TwitterSearchRequest searchRequest) throws IOException;
	
	public String twitter_network(TwitterSearchRequest searchRequest) throws IOException;
	
	public TwitterTimelineBuckets[] twitter_timeline(TwitterSearchRequest searchRequest) throws IOException;

	public TwitterTopEntityBuckets[] twitter_dashboard(TwitterSearchRequest searchRequest) throws IOException;
	//public TwitterTimelineBuckets[] trends_timeline(TwitterSearchRequest searchRequest) throws IOException;
	
	public HashMap<String, ArrayList<TwitterTrendsHitsResponse>> twitter_trends_timeline(TwitterTrendsSearchRequest searchRequest) throws IOException;

	String twitter_info(TwitterSearchRequest searchRequest) throws IOException;

	//public String twitter_info(TwitterSearchRequest searchRequest) throws IOException;

	

	
}
