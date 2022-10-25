package fr.ina.dlweb.dowser.ws.twitter.model.response;

import fr.ina.dlweb.utils.DateUtil;

public class TwitterTimelineBuckets implements Comparable<TwitterTimelineBuckets>{
	
	
	private String key_as_string;
	private long key;
	private double  doc_count;
	private Timelinesum timelinesum;


	public String getKey_as_string() {
		return key_as_string;
	}

	public void setKey_as_string(String key_as_string) {
		try{
			
			this.key_as_string = String.valueOf( DateUtil.parseISO_8601_T(key_as_string).getTime());
		}
		catch(Exception e)
		{
			this.key_as_string = key_as_string; // for dashboard 
		}
		//2.2 this.key_as_string = key_as_string;
	}

	public long getKey() {
		return key;
	}

	public void setKey(long key) {
		 
// String date = TwitterSettings.d.format(new Date(key));
//		  try {
		this.key = key;//TwitterSettings.d.parse(date).getTime();
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	public double getDoc_count() {
		return doc_count;
	}

	public void setDoc_count(double doc_count) {
		this.doc_count = doc_count;
	}


	@Override
	public int compareTo(TwitterTimelineBuckets o) {
		// TODO Auto-generated method stub
		return Long.compare(this.key, o.getKey());
	}

	public Timelinesum getTimelinesum() {
		return timelinesum;
	}

	public void setTimelinesum(Timelinesum timelinesum) {
		this.timelinesum = timelinesum;
		if(timelinesum !=null)
		{
			this.doc_count = timelinesum.getValue();
			/* if(this.doc_count > 0)
					this.doc_count+= Math.round( this.doc_count * 0.20);
			*/
		}
	}


/*
	public Timelinesum getTimelinesum() {
		return getTimelinesum();
	}

	public void setTimelinesum(Timelinesum limitsum) {
		this.setTimelinesum(limitsum);
		if(limitsum !=null)
			this.doc_count = limitsum.getValue();
	}

*/
//	public double getPercentage() {
//		return percentage;
//	}
//	@JsonIgnore
//	public void setPercentage(double percentage) {
//		this.percentage = percentage;
//	}
	


	
	
}

class Timelinesum
{
	private int value;

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	
}

class Trendsmax
{
	private int value;

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	
}


