package fr.ina.dlweb.twitter.crawler.apis;


import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

import fr.ina.dlweb.io.FileLineWriter.AlreadyLockedException;
import fr.ina.dlweb.io.FileLineWriter.CorruptedFileException;
import fr.ina.dlweb.twitter.commons.io.TwitterWriter;
import fr.ina.dlweb.twitter.commons.utils.Utils;
import fr.ina.dlweb.twitter.crawler.utils.SettingsCrawler;
import fr.ina.dlweb.twitter.crawler.utils.UtilsCrawler;
import twitter4j.Trend;
import twitter4j.Trends;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

public class TwitterAPITrends extends ITwitterAPIs {

	TwitterAPIStreaming streaming ;
	
    
	public TwitterAPITrends() {
		
		
		super();
		twitter = getTwitterAPI();
		Log = LoggerFactory.getLogger(TwitterAPITrends.class);
	
		
		
		if(SettingsCrawler.archiveTrends)
		{
			  streaming = new TwitterAPIStreaming();
			  
		}
		
	}
	

	@Override
	public boolean runAPI() {
			Runnable runnable = new Runnable() {
			
			@Override
			public void run() {
				getTweets();
			}		  
            	
		};
		
		ScheduledExecutorService  executor = new ScheduledThreadPoolExecutor(1);

		executor.scheduleAtFixedRate(runnable, 0, SettingsCrawler.fileCheckInterval, TimeUnit.SECONDS);
	
		
		
		return true;
	}

	@Override
	public boolean getTweets() {
		
		try {
			  
			  Trends trends = twitter.getPlaceTrends(SettingsCrawler.TRENDS_WHEREID);

			  String sTrend =  TwitterObjectFactory.getRawJSON(trends);
			  try {
				  		System.out.print("x");
				  		activeWriter.writeTweet(sTrend.trim(),"-2");
				
				} catch (AlreadyLockedException e) {
					
					System.out.print("L");
					
					Log.error(Utils.convertToJsonMessage(SettingsCrawler.applicationName,  Utils.createJsonLogMessage("debug", "exception", "AlreadyLockedException " + Throwables.getStackTraceAsString(e), 1)));
					

					
				} catch (CorruptedFileException e) {
					
					Log.error(Utils.convertToJsonMessage(SettingsCrawler.applicationName,  Utils.createJsonLogMessage("debug", "exception", "CorruptedFileException " + Throwables.getStackTraceAsString(e), 1)));

					System.out.print("C>"); // corrupted so closed
					
					activeWriter = handleCorruptedFileException(activeWriter);
					
					
				} 

			  if(SettingsCrawler.archiveTrends)
			  {
				  runStreamingforTrends(trends);
			  }
  
		} catch (TwitterException te) {
			
			System.out.print("$");
			try{activeWriter.writeTweet(UtilsCrawler.createJsonFromErrorMessage(te.getMessage()),"-4");} catch(Exception e){}
		
		} catch (IOException e) {
			Log.error(Utils.convertToJsonMessage(SettingsCrawler.applicationName, Utils.createJsonLogMessage("error", "exception", "TwitterAPITrends : getTweets IOException" + Throwables.getStackTraceAsString(e), 1)));
		}
		
		
		return true;
	}


   public void runStreamingforTrends(Trends trends)
   {

		  ArrayList<String> temp = new ArrayList<String>();
		  for(Trend t :  trends.getTrends())
		  {
			  temp.add(t.getName());  
		  }
		  
		  boolean updated = !UtilsCrawler.equalLists(streaming.keystoArchive,temp);
		  if(updated)
          {
			  	
        		streaming.keystoArchive.clear();
        		streaming.keystoArchive.addAll(temp);
        		streaming.maintenanceAPI();
		
          }
		
		  
	
	   
	   
   }



}
