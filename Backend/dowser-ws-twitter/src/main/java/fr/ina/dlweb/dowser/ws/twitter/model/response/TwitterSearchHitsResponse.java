package fr.ina.dlweb.dowser.ws.twitter.model.response;

import fr.ina.dlweb.dowser.ws.twitter.model.TweetMetaReloaded;


public class TwitterSearchHitsResponse {

	private String _index;
	private String _type;
	private String _id;
	private long _timestamp;
	private boolean _score;
	private TweetMetaReloaded _source;
	private String[] sort;
	
	
	
	public String get_type() {
		return _type;
	}
	public void set_type(String _type) {
		this._type = _type;
	}
	
	
	public String get_index() {
		return _index;
	}
	public void set_index(String _index) {
		this._index = _index;
	}
	
	public String get_id() {
		return _id;
	}
	public void set_id(String _id) {
		this._id = _id;
	}
	public boolean is_score() {
		return _score;
	}
	public void set_score(boolean _score) {
		this._score = _score;
	}
	public TweetMetaReloaded get_source() {
		return _source;
	}
	public void set_source(TweetMetaReloaded _source) {
		this._source = _source;
	}
	public String[] getSort() {
		return sort;
	}
	public void setSort(String[] sort) {
		this.sort = sort;
	}
	public long get_timestamp() {
		return _timestamp;
	}
	public void set_timestamp(long _timestamp) {
		this._timestamp = _timestamp;
	}


	




	
	
}
