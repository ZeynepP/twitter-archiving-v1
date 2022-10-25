package fr.ina.dlweb.twitter.crawler.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ina.dlweb.twitter.crawler.apis.ITwitterAPIs;
import fr.ina.dlweb.twitter.crawler.apis.TwitterAPIGetByIngest;
import fr.ina.dlweb.twitter.crawler.apis.TwitterAPIRelations;
import fr.ina.dlweb.twitter.crawler.apis.TwitterAPISearch;
import fr.ina.dlweb.twitter.crawler.apis.TwitterAPIStreaming;
import fr.ina.dlweb.twitter.crawler.apis.TwitterAPITrends;
import fr.ina.dlweb.twitter.crawler.apis.TwitterAPIUserTimeline;
import fr.ina.dlweb.twitter.crawler.utils.SettingsCrawler;
import fr.ina.dlweb.twitter.crawler.utils.UtilsCrawler;



public class TwitterAPIConnector {

	Logger Log = LoggerFactory.getLogger(TwitterAPIConnector.class);
	ITwitterAPIs actualAPI;
	TwitterAPIStreaming streamingAPI; // need to remove from ITwitterAPIs because of trends
    public TwitterAPIConnector()
	{
  
		
    	Runtime.getRuntime().addShutdownHook(new Thread() {
    	    public void run() {
    	    	if(!SettingsCrawler.archiveMethod.equals(SettingsCrawler.STREAMING)) // streaming handles it itself because of trends streaming 
    	    		actualAPI.shutDown();
    	        
    	    }
    	});
    	
	}

    
    public boolean run()
    {
    	
    	
    	if(SettingsCrawler.archiveMethod.equals(SettingsCrawler.STREAMING)) {
    		
    		streamingAPI = new TwitterAPIStreaming();
    		return streamingAPI.runAPI();
    	}
    		
    	else if(SettingsCrawler.archiveMethod.equals(SettingsCrawler.TRENDS))
    		actualAPI = new TwitterAPITrends();
    	else if(SettingsCrawler.archiveMethod.equals(SettingsCrawler.SEARCH))
    		actualAPI = new TwitterAPISearch();
    	else if(SettingsCrawler.archiveMethod.equals(SettingsCrawler.INGEST))
    		actualAPI = new TwitterAPIGetByIngest();
    	else if(SettingsCrawler.archiveMethod.equals(SettingsCrawler.TIMELINE) || SettingsCrawler.archiveMethod.equals(SettingsCrawler.MENTIONTIMELINE) )
    		actualAPI = new TwitterAPIUserTimeline(SettingsCrawler.archiveMethod);
    	else if(SettingsCrawler.archiveMethod.equals(SettingsCrawler.FRIENDS) || SettingsCrawler.archiveMethod.equals(SettingsCrawler.FOLLOWERS))
    		actualAPI = new TwitterAPIRelations();


		return actualAPI.runAPI();
				
		

    }
    
    public void shutDown()
    {
    	if(!SettingsCrawler.archiveMethod.equals(SettingsCrawler.STREAMING)) // streaming handles it itself because of trends streaming 
    		actualAPI.shutDown();
    	else streamingAPI.shutDown();
    	
    }
    



	
	



}
