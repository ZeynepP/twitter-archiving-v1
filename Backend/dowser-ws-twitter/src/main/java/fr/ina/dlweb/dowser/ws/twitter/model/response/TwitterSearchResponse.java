package fr.ina.dlweb.dowser.ws.twitter.model.response;

import fr.ina.dlweb.dowser.ws.twitter.model.IFaceModel;



public class TwitterSearchResponse implements IFaceModel {

	private boolean ok;
	private String error;
	private InfoResponse info;
	private int search_duration;
	private int total_duration;
	private int from_cache;
	private long total_count;
	private long minArchivedDate;
	private TwitterSearchHitsResponse[] hits;
	private String time_interval;
	private TwitterTimelineBuckets[] timeline;
	private TwitterTopEntityBuckets[] dashboard;

	private TwitterTopEntityBuckets[] wordcloud;
	
	
	public boolean isOk() {
		return ok;
	}

	public TwitterSearchResponse setOk(boolean ok) {
		this.ok = ok;
		return this;
	}

	public String getError() {
		return error;
	}

	public TwitterSearchResponse setError(String error) {
		this.error = error;
		return this;
	}

	public InfoResponse getInfo() {
		return info;
	}

	public TwitterSearchResponse setInfo(InfoResponse info) {
		this.info = info;
		return this;
	}

	public int getSearch_duration() {
		return search_duration;
	}

	public TwitterSearchResponse setSearch_duration(int search_duration) {
		this.search_duration = search_duration;
		return this;
	}

	public int getTotal_duration() {
		return total_duration;
	}

	public TwitterSearchResponse setTotal_duration(int total_duration) {
		this.total_duration = total_duration;
		return this;
	}

	public int getFrom_cache() {
		return from_cache;
	}

	public void setFrom_cache(int from_cache) {
		this.from_cache = from_cache;
	
	}

	public TwitterSearchHitsResponse[] getHits() {
		return hits;
	}

	public void setHits(TwitterSearchHitsResponse[] hits) {
		this.hits = hits;
		
	}

	public TwitterTimelineBuckets[] getTimeline() {
		return timeline;
	}

	public void setTimeline(TwitterTimelineBuckets[] timeline) {
		this.timeline = timeline;
		
	}

	public long getTotal_count() {
		return total_count;
	}

	public void setTotal_count(long total_count) {
		this.total_count = total_count;
	}

	
	public long getMinArchivedDate() {
		return minArchivedDate;
	}

	public void setMinArchivedDate(long minArchivedDate) {
		this.minArchivedDate = minArchivedDate;
	}
	
	

	public String getTime_interval() {
		return time_interval;
	}

	public void setTime_interval(String time_interval) {
		this.time_interval = time_interval;
	}

	public TwitterTopEntityBuckets[] getWordcloud() {
		return wordcloud;
	}

	public void setWordcloud(TwitterTopEntityBuckets[] wordcloud) {
		this.wordcloud = wordcloud;
	}

	public TwitterTopEntityBuckets[] getDashboard() {
		return dashboard;
	}

	public void setDashboard(TwitterTopEntityBuckets[] dashboard) {
		this.dashboard = dashboard;
	}


	
}
