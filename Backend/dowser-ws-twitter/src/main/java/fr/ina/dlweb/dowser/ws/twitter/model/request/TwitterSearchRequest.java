package fr.ina.dlweb.dowser.ws.twitter.model.request;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import fr.ina.dlweb.dowser.ws.twitter.rest.es.TwitterSettings;

// TODO: encapsulation pour l'instant public to test
public class TwitterSearchRequest {

	public static enum Sort {score, date_desc, date_asc};

	SimpleDateFormat dtf  = new SimpleDateFormat("dd-MM-yyyy");
	public static String DATE_FIELD = "created_at";
	
	
	
	//public static enum Interval {second,minute, hour,day,week,month,year};
	//private List<String> fields;
	//architecture
	private String cluster;
	private boolean use_cache = true;	
	private boolean need_info = false;
	private String to_date;
	private String from_date;
	//variables from interface
	private String query;
	private String interval;
	private String hashtags;
	private String collection;
	private String mentions;
	private String symbols;
	private String lang;
	private int source;
	private String urls;
	private String users;
	private int retweet;
	private int quote;
	private Long date_start_time = null;
	private Long date_stop_time = null;
	private String dashboard_type;

	private String sort_field ="created_at";
	private String sort_type ="asc";
	//Time related 
	private String time_zone = "Europe/Paris";
	private String locale;
//	private int time_interval;
	private String stopwords = "";
	private int wordcloudsize = 20;
	private String index = null;
	
	
	private TwitterAnalyzeSearchRequest analyze_request;
	//query specific es

	private int topsize = 15;
	private String time_interval = "day";
	private double max_timeline_buckets = 5000;
	private int size = 100;
	private int from = 0;
	private boolean exact_interval = false;
	// for dashboard timeline global time is default and will be returned for all queries
	private String timeline_type = "all";
	public List<String> wordcloudstopwords;
	private String index_type = "tweets";
	
	// irmuteq variables 
	private String irafields;
	private String[] iravarfields;
	private String irasettings ; // 0;1;2;// remove hashtags remove mentions remove urls 
	//architecture
	public String getCluster() {
		return cluster;
	}
	public void setCluster(String cluster) {
		this.cluster = cluster;
	}
	
	
	public boolean isUse_cache() {
		return use_cache;
	}
	public void setUse_cache(boolean use_cache) {
		this.use_cache = use_cache;
	
	}
	public boolean isNeed_info() {
		return  need_info;
	}
	public void setNeed_info(Boolean need_info) {
		this.need_info = need_info;
		
	}
	
	
	//query specific es
	

	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;

	}
	
	//variables from interface
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
//removed because we use now simple_string_query
//		if(query == null || query =="")
//			query = "*";
		this.query = query;
	}
	
	public String getHashtags() {
		return hashtags;
	}
	public void setHashtags(String h) {
		this.hashtags = h.toLowerCase();
	}
	
	public String getMentions() {
		return mentions;
	}
	public void setMentions(String h) {
		this.mentions = h.toLowerCase();
	}
	
	public String getUsers() {
		return users;
	}
	public void setUsers(String h) {
		this.users = h.toLowerCase();
	}
	
	public String getUrls() {
		return urls;
	}
	public void setUrls(String h) {
		this.urls = h.toLowerCase();
	}
	
	public String getLocale() {
		return locale;
	}
	public void setLocale(String locale) {
		this.locale = locale;
	}
	public String getTime_zone() {
		return time_zone;
	}
	public void setTime_zone(String time_zone) {
		this.time_zone = time_zone;
		
	}

	public void setMax_timeline_buckets(double maxbuckets) {
		max_timeline_buckets = maxbuckets;
	}
	
	public double getMax_timeline_buckets() {
		return max_timeline_buckets  ;
	}
	
	public void setTime_interval(String timeline_interval) {
		this.time_interval = timeline_interval ;
	}
	
	public String getTime_interval() {
		return this.time_interval  ;
	}
	
	private String maxTimelineBucketsCheck(String interval, long duration, double time)
	{
		
			if(duration == 0) duration += 1;
		    double prefix = (duration/(double)max_timeline_buckets) * time ;
		    if(prefix != 0) return prefix +  interval;
		    else return interval;
		
	}
	

	private String getOptimalInterval(boolean isExact_interval)
	{
	
		//max_timeline_buckets -=1;
		
		
		// Bad use of Period.getXXX()
		// Period.getMinutes() do not return the total number of minutes in this Period
		// ex: Period(1 year, 2 minutes).getMinutes() == 2
//		int years = new Period(this.date_start_time,this.date_stop_time, PeriodType.years()).getYears();
//		int months = new Period(this.date_start_time,this.date_stop_time, PeriodType.months()).getMonths() ;
//		int weeks = new Period(this.date_start_time,this.date_stop_time, PeriodType.weeks()).getWeeks() ;
//		int days = new Period(this.date_start_time,this.date_stop_time, PeriodType.days()).getDays() ;
//		int hours = new Period(this.date_start_time,this.date_stop_time, PeriodType.hours()).getHours() ;
//		int minutes = new Period(this.date_start_time,this.date_stop_time, PeriodType.minutes()).getMinutes();
//		int seconds = new Period(this.date_start_time,this.date_stop_time, PeriodType.seconds()).getSeconds();
		
		long millis = this.date_stop_time - this.date_start_time;
		int seconds = (int)Math.ceil(millis / 1000.0);
		int minutes = (int)Math.ceil(millis / 60000.0);
		int hours = (int)Math.ceil(millis / 3600000.0);
		int days = (int)Math.ceil(millis / 86400000.0);
		int weeks = (int)Math.ceil(millis / 604800000.0);
		int months = (int)Math.ceil(millis / 2628000000.0);
		int years = (int)Math.ceil(millis /  31557600000.0);
		

		
	  /*  if(seconds <= max_timeline_buckets) {
	    	if(isExact_interval)
	    		return maxTimelieBucketsCheck("ms",seconds,1000);
	    	else return "second";
			
		}*/
	    if(minutes <= max_timeline_buckets) {
			if(isExact_interval)
				return maxTimelineBucketsCheck("s",minutes, 60 );
			else return "minute";
		} 
		else if(hours <= max_timeline_buckets) {
			if(isExact_interval)
				return maxTimelineBucketsCheck("m",hours, 60 );
			else return "hour";
		} 
		else if(days <= max_timeline_buckets) {
			if(isExact_interval)
				return maxTimelineBucketsCheck("h",days,24 );
			else return "day";
		}
		else if(weeks <= max_timeline_buckets) {
			if(isExact_interval)
				return maxTimelineBucketsCheck("d",weeks ,7);// 7.75 ??
			else return "week";
		}
		else if(months <= max_timeline_buckets) {
			if(isExact_interval)
				return maxTimelineBucketsCheck("d",months,30 );
			else return "month";
		}
		else if(years <= max_timeline_buckets) {
			if(isExact_interval)
				return maxTimelineBucketsCheck("d",years,365 );
			else return "year";
		}
	
	    
	    return "day";
	
		
	}
	
	
	public void checkRequest()
	{
		
		
		if(null == this.date_start_time) {
			try {
				date_start_time = dtf.parse(this.from_date).getTime();
			} catch(Exception e) {
				// bound start date with 20 mars 2006 00:00 first tweet date 21 march 2006
	
				try {
					date_start_time = dtf.parse("20-03-2006").getTime();
				} catch(Exception ee) {}
			}
		}
		
		if(null == this.date_stop_time) {
			try {
				date_stop_time = dtf.parse(this.to_date).getTime() + 86400000;
			} catch(Exception e) {
				// bound stop date with 00:00 tomorrow
				
				DateTime now = new DateTime();
				DateTime tomorrowFirstMoment = now.plusDays(1).withTimeAtStartOfDay();
				
				date_stop_time = tomorrowFirstMoment.getMillis();
			}
		}
		String optinterval = "day";
		if(this.date_start_time> 0 || this.date_stop_time> 0)
		{
			optinterval = getOptimalInterval(this.isExact_interval());
		}
		this.setTime_interval(optinterval);
		
			
	}
	
	
	public int getRetweet() {
		return retweet;
	}
	public void setRetweet(int retweet) {
		this.retweet = retweet;
	}


	public String getFrom_date() {
		return from_date;
	}
	public void setFrom_date(String from_date) {
		this.from_date = from_date;// + " 00:00:00";
	}
	public String getTo_date() {
		return to_date;
	}
	
	public void setTo_date(String end) {
		this.to_date = end ;//+ " 00:00:00";
	}
	public int getFrom() {
		return from;
	}
	public void setFrom(int from) {
		this.from = from;
	}
	public TwitterAnalyzeSearchRequest getAnalyze_request() {
		return analyze_request;
	}
	public void setAnalyze_request(TwitterAnalyzeSearchRequest analyze_request) {
		this.analyze_request = analyze_request;
	}
	public String getDashboard_type() {
		return dashboard_type;
	}
	public void setDashboard_type(String dashboard_type) {
		this.dashboard_type = dashboard_type;
	}
	public String getInterval() {
		return interval;
	}
	public void setInterval(String interval) {
		this.interval = interval;
	}
	public int getQuote() {
		return quote;
	}
	public void setQuote(int quote) {
		this.quote = quote;
	}

	public String getSort_field() {
		return sort_field;
	}
	public void setSort_field(String sort_field) {
		this.sort_field = sort_field;
	}
	public String getSort_type() {
		return sort_type;
	}
	public void setSort_type(String sort_type) {
		this.sort_type = sort_type;
	}
	
	public String getStopwords() {
		return stopwords;
	}
	public void setStopwords(String stopwords) {
		this.stopwords = stopwords.toLowerCase();
		Set<String> setstops =  new HashSet<String>(TwitterSettings.stopwords);
		if(!stopwords.isEmpty())
		{
			String[] swords = stopwords.split(";");
			for(String s: swords)
			{
				setstops.add(s);
			}
		}
		wordcloudstopwords = new ArrayList<String>(setstops);
		Collections.sort(wordcloudstopwords);
	}
	public int getWordcloudsize() {
		return wordcloudsize;
	}
	public void setWordcloudsize(int wordcloudesize) {
		this.wordcloudsize = wordcloudesize;
	}
	public String getIrafields() {
		return irafields;
	}
	public void setIrafields(String irafields) {
		this.irafields = irafields;
		String [] varset = irafields.split("&");
		if(varset[0] != null)
			this.iravarfields = varset[0].split(";");
		if(varset.length > 1 && varset[1] != null)
			this.setIrasettings(varset[1]);
		else
			this.setIrasettings("");
	}
	public String[] getIravarfields() {
		return iravarfields;
	}
	public void setIravarfields(String[] iravarfields) {
		this.iravarfields = iravarfields;
	}
	public String getIrasettings() {
		return irasettings;
	}
	public void setIrasettings(String irasettings) {
		this.irasettings = irasettings;
	}
	
	public long getDate_start() {
		return date_start_time;
	}
	public void setDate_start(long start) {
		 this.date_start_time = start;
	}

	public long getDate_stop() {
		return date_stop_time;
	}
	public void setDate_stop(long end) {
		this.date_stop_time = end;
	}

	
	public String getSymbols() {
		return symbols;
	}
	public void setSymbols(String symbols) {
		this.symbols = symbols;
	}
	public boolean isExact_interval() {
		return exact_interval;
	}
	public void setExact_interval(boolean exact_interval) {
		this.exact_interval = exact_interval;
	}
	public String getLang() {
		return lang;
	}
	public void setLang(String lang) {
		this.lang = lang;
	}
	public int getSource() {
		return source;
	}
	public void setSource(int source) {
		this.source = source;
	}
	public int getTopsize() {
		return topsize;
	}
	public void setTopsize(int topsize) {
		TwitterSettings.TOP_ENTITIES_SIZE = topsize;
		this.topsize = topsize;
	}
	public String getTimeline_type() {
		return timeline_type;
	}
	public void setTimeline_type(String timeline_type) {
		this.timeline_type = timeline_type;
	}
	public String getIndex() {
		return index;
	}
	public void setIndex(String index) {
		this.index = index;
	
	}
	public String getCollection() {
		return collection;
	}
	public void setCollection(String collection) {
		this.collection = collection;
	}
	public String getIndex_type() {
		return index_type;
	}
	public void setIndex_type(String index_type) {
		this.index_type = index_type;
	}


}
