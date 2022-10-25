package fr.ina.dlweb.dowser.ws.twitter.rest.iface;

import java.util.List;

public interface IFaceSearchTester {

	
	public String search(boolean useCache, String query, int start, int size,
							 List<String> fields, List<String> hl_fields,
							 String dateStart, String dateStop, String timeZone, int maxTimelineBuckets,
							 String domain, String url);



	public String search(boolean use_cache, String query, int start, int size, String date_start, String date_stop,
			String time_zone, int max_timeline_buckets, String hashtags, String mentions, String users,
			int retweet_state, String urls, boolean timeline);
	
}
