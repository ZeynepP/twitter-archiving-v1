package fr.ina.dlweb.dowser.ws.twitter.model.request;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import fr.ina.dlweb.dowser.ws.twitter.rest.es.TwitterSettings;

// TODO: encapsulation pour l'instant public to test
public class TwitterTrendsSearchRequest {

	
	public static String DATE_FIELD = "date_trend";
	public static enum Sort {score, date_desc, date_asc};
	SimpleDateFormat dtf  = new SimpleDateFormat("dd-MM-yyyy HH:mm");
	
	//public static enum Interval {second,minute, hour,day,week,month,year};
	//private List<String> fields;
	//architecture
	private String cluster;
	private boolean use_cache = true;	
	private boolean need_info = false;
	private String to_date;
	private String from_date;
	private int  topK;
	//variables from interface
	private String query;
	private Long date_start_time = null;
	private Long date_stop_time = null;
	private String sort_field ="date_trend";
	private String sort_type ="asc";

	private String index = null;
	private String index_type = "trends";

	
	public void checkRequest()
	{
		
		
		if(this.date_start_time == null) {
			try {
				date_start_time = dtf.parse(this.from_date).getTime();
			} catch(Exception e) {
				
			}
		}
		
		if(this.date_stop_time == null) {
			try {
				date_stop_time = dtf.parse(this.to_date).getTime() + 86400000;
			} catch(Exception e) {
				// bound stop date with 00:00 tomorrow
				
				DateTime now = new DateTime();
				DateTime tomorrowFirstMoment = now.plusDays(1).withTimeAtStartOfDay();
				
				date_stop_time = tomorrowFirstMoment.getMillis();
			}
		}

			
	}
	
	
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
	
	

	public String getFrom_date() {
		return from_date;
	}
	public void setFrom_date(String from_date) {
		if(from_date.isEmpty() || from_date == null)
			from_date = dtf.format(new Date());
		if(!from_date.contains(":"))
			from_date = from_date + " 00:00";
		this.from_date = from_date;// + " 00:00";
	}
	public String getTo_date() {
		return to_date;
	}
	
	public void setTo_date(String end) {
		this.to_date = end ;//+ " 00:00:00";
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
	
	public String getIndex() {
		return index;
	}
	public void setIndex(String index) {
		this.index = index;
	
	}
	public String getIndex_type() {
		return index_type;
	}
	public void setIndex_type(String index_type) {
		this.index_type = index_type;
	}


	public int getTopK() {
		return topK;
	}


	public void setTopK(int topK) {
		this.topK = topK;
	}


}
