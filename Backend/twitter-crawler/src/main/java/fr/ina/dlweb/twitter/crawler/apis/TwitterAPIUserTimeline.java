package fr.ina.dlweb.twitter.crawler.apis;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map.Entry;

import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

import fr.ina.dlweb.twitter.commons.utils.Utils;
import fr.ina.dlweb.twitter.crawler.utils.SettingsCrawler;
import fr.ina.dlweb.twitter.crawler.utils.UtilsCrawler;
import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

public class TwitterAPIUserTimeline extends ITwitterAPIs {

	String timelinetype;

	public TwitterAPIUserTimeline(String type)
	{
		super();
		twitter = getTwitterAPI();
	
		Log = LoggerFactory.getLogger(TwitterAPIUserTimeline.class);
		this.timelinetype = type;

	}

    // however it is calling just one function i need it for other classes like trends
	public boolean runAPI( )
	{
		return getTweets();

	}
	

	public long getUsertimeline(Twitter twitter, String screenName, long userId)
	{
		int total = 0; // for log information
		int i = 1;
		ResponseList<Status> statuses;
		long maxid =Long.MAX_VALUE;
		long lastid =  ( offsets.get(screenName) == null ?  1 :Long.valueOf( offsets.get(screenName)));
		int size = 10;
		long keepmaxid = lastid;
		if(lastid > 0)	//some offsets are set to zero in file
		{
			
			
					
				try{
					do
					{
						Paging page = new Paging().count(100).sinceId(lastid);
						page.setPage(i);
						if(userId > 0)
							statuses = twitter.getUserTimeline(userId, page);
						else
							statuses = twitter.getUserTimeline(screenName, page);
					    
					    size = statuses.size();
					    total += size;
						for (Status tweet : statuses) {
							
								//Log.debug(tweet.getId());
								
				            	if(tweet.getId() < maxid)
				            	{
				            		maxid = tweet.getId() - 1;
				            		keepmaxid = Math.max(maxid + 1, keepmaxid);
				            	}
				            	try
				            	{
				            		activeWriter.writeTweet(TwitterObjectFactory.getRawJSON(tweet), String.valueOf(tweet.getId()));
				            	} catch (IOException e) {
									String msg = Utils.convertToJsonMessage(SettingsCrawler.applicationName , Utils.createJsonLogMessage( "error", "error", "TwitterAPIUserTimeline : write getUsertimeline NOT ABLE TO WRITE " + Throwables.getStackTraceAsString(e),1));
									Log.error(msg);
									System.out.println(Throwables.getStackTraceAsString(e));
								}
						}
		//					System.out.println("Last Id : " + lastID);
		//					System.out.println("Max Id : " + maxid);
						page.setMaxId(maxid);
						System.out.print("!");
						i++;
						
					}while (size != 0 && maxid > lastid);
				
				}
				catch(TwitterException te)
				{
					System.out.print("$");
	        		if(te.exceededRateLimitation())
	        		{
						try {
							Thread.currentThread().sleep(te.getRateLimitStatus().getSecondsUntilReset() > 0?te.getRateLimitStatus().getSecondsUntilReset():0 * 1000);
						} catch (InterruptedException e) {}
	        		}
					else
	        		{
						try{activeWriter.writeTweet(UtilsCrawler.createJsonFromErrorMessage(te.getMessage()),"-4");} catch(Exception e){}
	        		}
						

				}
				catch(Exception ex)
				{
					System.out.println(Throwables.getStackTraceAsString(ex));
				}
			
		}
		
		String msg = Utils.convertToJsonMessage(SettingsCrawler.applicationName, Utils.createJsonLogMessage( "info", "crawled",  screenName,  total));
		Log.info(msg);
		
		return keepmaxid; 
		
		
	}
	
	/* This is just a search function for timeline's mentions 
	 * It is better maybe to remove this option and launch mentions with streaming api or search api in seperated procedure
	 * 
	 * */
	public long getMentionsofUser(Twitter twitter, String screenName)
	{
		
		long maxid =Long.MAX_VALUE;
		long lastid =  ( offsets.get(screenName) == null ?  1 :Long.valueOf(offsets.get(screenName))); 
		long keepmaxid = lastid;
		int total = 0;
		//System.out.println(lastid);
		if(lastid > 0)	
		{
			Query q = new Query( "@" + screenName);
			q.setSinceId(lastid);
			q.setResultType(Query.RECENT);	
	        q.setCount(100);
	        int searchResultCount = 0 ;
	        do{
				try{
					
					
					QueryResult queryResult = twitter.search(q);
					searchResultCount = queryResult.getTweets().size();
					total+=searchResultCount;
	
					for (Status tweet : queryResult.getTweets()) {
			
						try
		            	{
							activeWriter.writeTweet(TwitterObjectFactory.getRawJSON(tweet), String.valueOf(tweet.getId()));
		            	} catch (IOException e) {
							String msg = Utils.convertToJsonMessage(SettingsCrawler.applicationName , Utils.createJsonLogMessage( "error", "exception", "TwitterAPIUserTimeline : write getMentionsofUser " + Throwables.getStackTraceAsString(e) ,1));
							Log.error(msg);
						}
						
			            if (tweet.getId() < maxid) {
			            	
			            	maxid = tweet.getId();
			            	keepmaxid = Math.max(maxid, keepmaxid);
			                q.setMaxId(maxid-1);
			               
			            }
			            else 
			            {
			            	// is it right way to do it??? 
			            	// each new maxid should be smaller than the other one so I decided to break here 
			            	searchResultCount = 0;
			            	break;
			            }
			            	
			        }
					
					System.out.print("@");
				}
				catch(TwitterException te)
				{
				
					System.out.print("$");
	        		if(te.exceededRateLimitation())
	        		{
						try {
							Thread.currentThread().sleep(te.getRateLimitStatus().getSecondsUntilReset() > 0?te.getRateLimitStatus().getSecondsUntilReset():0 * 1000);
						} catch (InterruptedException e) {}
	        		}
					else
	        		{
						try{activeWriter.writeTweet(UtilsCrawler.createJsonFromErrorMessage(te.getMessage()),"-4");} catch(Exception e){}
	        		}
					String msgerror = Utils.convertToJsonMessage(SettingsCrawler.applicationName,Utils.createJsonLogMessage( "error", "exception", "TwitterAPIUserTimeline : getTweets getMentionsofUser " + te.toString(), 1));
					Log.error(msgerror);
					
				}
				
				
			} while (searchResultCount != 0 )  ;
		}
		
		
		String msg = Utils.convertToJsonMessage(SettingsCrawler.applicationName, Utils.createJsonLogMessage( "info", "crawled", "@" + screenName, total));
		Log.info(msg);
		
		return keepmaxid;
		
	}
	
	public boolean getTweets()
	{
		boolean isok = true;
		long keepmaxid = 0;
		String screenName;
		long userId;
		int size = 0;
		try{
			
			//TODO : ZP Find a more elegant way if timelineIdScreenname is null instead of repeating whole code block 
			// solution 1 : reverse hashmap thus it will be screenname to Id not very good as different users can have same screen names at different times 
			// solution 2 : use guava bimap to search by value (screenname) to get userid
			// solution 3 : lookup here for userid => too costy for twitter account and will not update source file with found id 
			// solution 4 : exit if sourcefile does not contain Ids => thus archive only by id 
			if(timelineIdScreenname != null)
			{
				
				for( Entry<Long, String> entry : timelineIdScreenname.entrySet())
				{
					userId = entry.getKey();
					screenName = entry.getValue();
					System.out.print(screenName);		
					
	
					if(timelinetype.equals(SettingsCrawler.TIMELINE))
						keepmaxid = getUsertimeline(twitter, screenName, userId);
					else if(timelinetype.equals(SettingsCrawler.MENTIONTIMELINE))
						keepmaxid = getMentionsofUser(twitter, screenName);
					
					offsets.put(screenName,String.valueOf( keepmaxid));
					// we can go on keeping offsets by screenname because it is the screenname we keep in source json files / same as file name 
					
					
					
				}
			}
			else
			{
				for( String name : keystoArchive)
				{
					userId = 0;
					screenName = name;
					System.out.print(screenName);		
					
			
			
					if(timelinetype.equals(SettingsCrawler.TIMELINE))
						keepmaxid = getUsertimeline(twitter, screenName, userId);
					else if(timelinetype.equals(SettingsCrawler.MENTIONTIMELINE))
						keepmaxid = getMentionsofUser(twitter, screenName);
					
					offsets.put(screenName,String.valueOf(keepmaxid));
					// we can go on keeping offsets by screenname because it is the screenname we keep in source json files / same as file name 
					
				
					
				}
				
			}
			
				
		}
		catch(Exception ex)
		{
			
			System.out.println(ex.getMessage());
			Log.error(Utils.convertToJsonMessage(SettingsCrawler.applicationName,Utils.createJsonLogMessage( "error", "exception", "TwitterAPIUserTimeline : getTweets " + Throwables.getStackTraceAsString(ex), 1)));
	
			isok = false;
		}
		finally{
			try {
				Utils.writeHashMap(SettingsCrawler.offsetfile, false, offsets, true);
			} catch (FileNotFoundException ex) {
				
				Log.error(Utils.convertToJsonMessage(SettingsCrawler.applicationName,Utils.createJsonLogMessage( "error", "error", "TwitterAPIUserTimeline : writeHashMap " + Throwables.getStackTraceAsString(ex),1)));

			}
		}
		
		return isok;
	}
	


}
