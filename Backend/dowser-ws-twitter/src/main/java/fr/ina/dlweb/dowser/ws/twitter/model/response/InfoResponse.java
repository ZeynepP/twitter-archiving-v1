package fr.ina.dlweb.dowser.ws.twitter.model.response;

public class InfoResponse {
	
	public enum IndexAvailability {full, partial, none};
	
	private int total_index_count;
	private int dead_index_count;
	private int live_index_count;
	private IndexAvailability index_availability;
	
	public InfoResponse() {
		
	}
	
	
	public int getTotal_index_count() {
		return total_index_count;
	}
	public InfoResponse setTotal_index_count(int total_index_count) {
		this.total_index_count = total_index_count;
		return this;
	}
	public int getDead_index_count() {
		return dead_index_count;
	}
	public InfoResponse setDead_index_count(int dead_index_count) {
		this.dead_index_count = dead_index_count;
		return this;
	}
	public int getLive_index_count() {
		return live_index_count;
	}
	public InfoResponse setLive_index_count(int live_index_count) {
		this.live_index_count = live_index_count;
		return this;
	}
	public IndexAvailability getIndex_availability() {
		return index_availability;
	}
	public InfoResponse setIndex_availability(IndexAvailability index_availability) {
		this.index_availability = index_availability;
		return this;
	}

	
}
