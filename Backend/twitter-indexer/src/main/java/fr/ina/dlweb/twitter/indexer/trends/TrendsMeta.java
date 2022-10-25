package fr.ina.dlweb.twitter.indexer.trends;

import java.io.Serializable;


//TODO remove this class once everything is ok: it could be used to do mapping with gson
@SuppressWarnings("serial")
public class TrendsMeta implements Serializable {
	

	public String as_of ;
	public String date_trend;
	
	public String name;
	public String query;
	public String url;
	public String promoted_content;
	public int tweet_volume;
	public int rank;
	

}