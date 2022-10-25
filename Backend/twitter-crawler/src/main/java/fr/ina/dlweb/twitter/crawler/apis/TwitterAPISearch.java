package fr.ina.dlweb.twitter.crawler.apis;

import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

import fr.ina.dlweb.io.FileLineWriter.AlreadyLockedException;
import fr.ina.dlweb.io.FileLineWriter.CorruptedFileException;
import fr.ina.dlweb.twitter.commons.utils.Utils;
import fr.ina.dlweb.twitter.crawler.utils.SettingsCrawler;
import fr.ina.dlweb.twitter.crawler.utils.UtilsCrawler;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

public class TwitterAPISearch extends ITwitterAPIs {


    // however it is calling just one function i need it for other classes like trends
	public boolean runAPI( )
	{
		twitter = getTwitterAPI();
		Log = LoggerFactory.getLogger(TwitterAPISearch.class);
		return getTweets();
	}

	private String getSearchQueries(int from, int to)
	{
		StringBuilder sb = new StringBuilder();
		for(int i= from; i< to; i++)
		{
			sb.append(  keystoArchive.get(i)+ " OR ");
			
		}
		sb.replace(sb.lastIndexOf("OR"), sb.lastIndexOf("OR") + 2, "");
		
		return sb.toString();
		
	}
	
	@SuppressWarnings("static-access")
	public boolean getTweets()
	{

		
		try{

			Log.info(Utils.convertToJsonMessage(SettingsCrawler.applicationName, Utils.createJsonLogMessage( "info", "info", "getTweets Started getting search", 1)));
            
			boolean isOk = true;
			int from = 0;
			
			do{
				long lowestTweetId = Long.MAX_VALUE;//;
				// need this because the query size is limited for search queries, too long queries are rejected
				int to = Math.min(from + SettingsCrawler.SEARCH_NUMBEROFQUERIES , keystoArchive.size());
				String squery = getSearchQueries(from, to) ;
				
				
				String header = UtilsCrawler.getJsonHeader(keystoArchive.subList(from, to));
				
			/*	if(this.fileTweetsPath.equals(filePath)) // happens when search query is too fast and is able to do 2 searches in a second then it should create a new file 
					Thread.currentThread().sleep(2000);
					
				this.fileTweetsPath = filePath;*/

				Query searchquery = new Query(squery);
				
				if(SettingsCrawler.sinceDate != null)
					searchquery.setSince(SettingsCrawler.sinceDate);
				if(SettingsCrawler.untilDate != null)
					searchquery.setUntil(SettingsCrawler.untilDate);
				
				if(SettingsCrawler.since_id != 0)
					searchquery.setSinceId(SettingsCrawler.since_id);
				if(SettingsCrawler.max_id != 0)
					searchquery.setMaxId(SettingsCrawler.max_id);
				
				
		        searchquery.setResultType(Query.RECENT);	
		        searchquery.setCount(100);
		        int searchResultCount =10;
		        
		        
		        do {
		        	
		        	//http://stackoverflow.com/questions/33519794/readtimeouterror-twitter-streaming-api
		        	try{
		        		
		        		
			            QueryResult queryResult = twitter.search(searchquery);
			            searchResultCount = queryResult.getTweets().size();
			            System.out.print("*");
			            for (Status tweet : queryResult.getTweets()) {
			            	
			            	try {
								
			            		activeWriter.writeTweet(TwitterObjectFactory.getRawJSON(tweet),String.valueOf(tweet.getId()));
							
							} catch (AlreadyLockedException e) {
								
								System.out.print("L");
								
								Log.error(Utils.convertToJsonMessage(SettingsCrawler.applicationName,  Utils.createJsonLogMessage("debug", "exception", "AlreadyLockedException " + Throwables.getStackTraceAsString(e), 1)));
								
			
								
							} catch (CorruptedFileException e) {
								
								Log.error(Utils.convertToJsonMessage(SettingsCrawler.applicationName,  Utils.createJsonLogMessage("debug", "exception", "CorruptedFileException " + Throwables.getStackTraceAsString(e), 1)));

								System.out.print("C>"); // corrupted so closed
								
								activeWriter = handleCorruptedFileException(activeWriter);

								
							} 
			            	
		
			            	
			            	
			                if (tweet.getId() < lowestTweetId) {
			                	
			                    lowestTweetId = tweet.getId();
			                    searchquery.setMaxId(lowestTweetId-1);
			                    
				            }
				            else {
				            	// TODO: is it right way to do it??? 
				            	// each new maxid should be smaller than the other one so I decided to break here 
				            	searchResultCount = 0;
				            	break;
				            }
			            }
			            
			            isOk = true;
			            Log.info( Utils.convertToJsonMessage(SettingsCrawler.applicationName, Utils.createJsonLogMessage( "info", "crawled", "getTweets " ,  activeWriter.getSizeofTweets())));
		        	}
		        	catch(TwitterException te)
		        	{
		        		System.out.print("$");
		        		if(te.exceededRateLimitation() )
		        			Thread.currentThread().sleep(te.getRateLimitStatus().getSecondsUntilReset()>0 ? te.getRateLimitStatus().getSecondsUntilReset():0l * 1000);
		        		else
		        		{
		        			try{activeWriter.writeTweet(UtilsCrawler.createJsonFromErrorMessage(te.getMessage()),"-4");} catch(Exception e){}
		        			Log.debug( Utils.convertToJsonMessage(SettingsCrawler.applicationName , Utils.createJsonLogMessage( "debug", "exception", "getTweets for query " + squery +  " : " + te.getMessage().trim(), 1)));
		        		}
		        		
		        		isOk = false;
		        		
		        	}
		            // If we increment from before or without control we will loose the queries with the problem 
		        	// need monitoring logs it can have infinite loop maybe need counter to try a few times than break
		            
	
		         } while (searchResultCount != 0 );//& interruptedTokenId!= key )  ;
	
		         if(isOk)
	            	from += SettingsCrawler.SEARCH_NUMBEROFQUERIES;
		         
		         //header will be different in the newt loop
		         activeWriter.closeWriter(); 
				 System.out.print(">");
		    
			 } while (from < keystoArchive.size());//& interruptedTokenId!= key )  ;
	   
			
			
					
				
		}
		catch(Exception ex)
		{
 			Log.error( Utils.convertToJsonMessage(SettingsCrawler.applicationName , Utils.createJsonLogMessage( "error", "exception", "getTweets " + Throwables.getStackTraceAsString(ex), 1)));
			return false;
		}
		return true;
	}


}
