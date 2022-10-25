package fr.ina.dlweb.dowser.ws.twitter.model.request;

import java.util.List;

import fr.ina.dlweb.dowser.ws.twitter.model.IFaceModel;

	
public class SearchRequest implements IFaceModel {


	public static enum Sort {score, date_desc, date_asc};
	
	public static enum Interval {year, month, day};
	
	private String cluster;
	private Boolean use_cache;	
	private Boolean need_info;
	
	private Integer start;
	private Integer size;
	private String query;
	
	private List<String> fields;
	private List<String> hl_fields;
	private Sort sort;
	
	
	private String domain_filter;
	private String url_filter;
	
	private String locale;
	
	private String time_zone;
	private String date_start;
	private String date_stop;
	
	private Boolean need_timeline;
	private Boolean need_relative_timeline;
	private Interval timeline_interval;
	private Integer max_timeline_buckets;
	
	public SearchRequest() {	
	}
	public String getCluster() {
		return cluster;
	}
	public SearchRequest setCluster(String cluster) {
		this.cluster = cluster;
		return this;
	}
	public boolean isUse_cache() {
		return use_cache == null ? false : use_cache;
	}
	public SearchRequest setUse_cache(boolean use_cache) {
		this.use_cache = use_cache;
		return this;
	}
	public boolean isNeed_info() {
		return need_info == null ? false : need_info;
	}
	public SearchRequest setNeed_info(Boolean need_info) {
		this.need_info = need_info;
		return this;
	}
	public Integer getStart() {
		return start;
	}
	public SearchRequest setStart(int start) {
		this.start = start;
		return this;
	}
	public Integer getSize() {
		return size;
	}
	public SearchRequest setSize(int size) {
		this.size = size;
		return this;
	}
	public String getQuery() {
		return query;
	}
	public SearchRequest setQuery(String query) {
		this.query = query;
		return this;
	}
	public List<String> getFields() {
		return fields;
	}
	public SearchRequest setFields(List<String> fields) {
		this.fields = fields;
		return this;
	}
	public List<String> getHl_fields() {
		return hl_fields;
	}
	public SearchRequest setHl_fields(List<String> hl_fields) {
		this.hl_fields = hl_fields;
		return this;
	}
	public Sort getSort() {
		return sort;
	}
	public SearchRequest setSort(Sort sort) {
		this.sort = sort;
		return this;
	}
	public String getDomain_filter() {
		return domain_filter;
	}
	public SearchRequest setDomain_filter(String domain_filter) {
		this.domain_filter = domain_filter;
		return this;
	}
	public String getUrl_filter() {
		return url_filter;
	}
	public SearchRequest setUrl_filter(String url_filter) {
		this.url_filter = url_filter;
		return this;
	}
	public String getLocale() {
		return locale;
	}
	public SearchRequest setLocale(String locale) {
		this.locale = locale;
		return this;
	}
	public String getTime_zone() {
		return time_zone;
	}
	public SearchRequest setTime_zone(String time_zone) {
		this.time_zone = time_zone;
		return this;
	}
	public String getDate_start() {
		return date_start;
	}
	public SearchRequest setDate_start(String date_start) {
		this.date_start = date_start;
		return this;
	}
	public String getDate_stop() {
		return date_stop;
	}
	public SearchRequest setDate_stop(String date_stop) {
		this.date_stop = date_stop;
		return this;
	}
	public boolean isNeed_timeline() {
		return need_timeline == null ? false : need_timeline;
	}
	public SearchRequest setNeed_timeline(boolean need_timeline) {
		this.need_timeline = need_timeline;
		return this;
	}
	public boolean isNeed_relative_timeline() {
		return need_relative_timeline == null ? false : need_relative_timeline;
	}
	public SearchRequest setNeed_relative_timeline(boolean need_relative_timeline) {
		this.need_relative_timeline = need_relative_timeline;
		return this;
	}
	public Interval getTimeline_interval() {
		return timeline_interval;
	}
	public SearchRequest setTimeline_interval(Interval timeline_interval) {
		this.timeline_interval = timeline_interval;
		return this;
	}
	public Integer getMax_timeline_buckets() {
		return max_timeline_buckets;
	}
	public SearchRequest setMax_timeline_buckets(int max_timeline_buckets) {
		this.max_timeline_buckets = max_timeline_buckets;
		return this;
	}
	
	
	
	
	
	
	
}
