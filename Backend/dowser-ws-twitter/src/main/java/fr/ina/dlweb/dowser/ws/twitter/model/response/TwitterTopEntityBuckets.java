package fr.ina.dlweb.dowser.ws.twitter.model.response;



public class TwitterTopEntityBuckets {

	public String key;
	public int doc_count;
	public Timeline timeline = new Timeline();
	private Timelinesum timelinesum;
	public TwitterTimelineBuckets[] getTimelineBuckets()
	{
		return timeline.buckets;
	}
	public  void setTimelineBuckets(TwitterTimelineBuckets[] buckets)
	{
		timeline.buckets = buckets;
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
}

class Timeline 
{
	public TwitterTimelineBuckets[] buckets;
}


