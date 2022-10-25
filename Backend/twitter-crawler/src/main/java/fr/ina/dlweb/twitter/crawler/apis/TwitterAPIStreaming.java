package fr.ina.dlweb.twitter.crawler.apis;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Throwables;

import fr.ina.dlweb.io.FileLineWriter.AlreadyLockedException;
import fr.ina.dlweb.io.FileLineWriter.CorruptedFileException;
import fr.ina.dlweb.twitter.commons.io.TwitterWriter;
import fr.ina.dlweb.twitter.commons.utils.Utils;
import fr.ina.dlweb.twitter.crawler.utils.Credential;
import fr.ina.dlweb.twitter.crawler.utils.SettingsCrawler;
import fr.ina.dlweb.twitter.crawler.utils.UtilsCrawler;
import twitter4j.FilterQuery;
import twitter4j.RawStreamListener;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

public class TwitterAPIStreaming {
	
	List<String> keystoArchive = new ArrayList<String>();
	
	//This for streaming API to handle exchange between two streams
	// it is too much noise just to keep a few more tweets during exchange
	LinkedList<TwitterStream> activeStreams = new LinkedList<TwitterStream>();
	LinkedList<TwitterWriter> activeWriters = new LinkedList<TwitterWriter>();
	Logger Log = LoggerFactory.getLogger(TwitterAPIStreaming.class);
	ObjectNode infoFile = null;	
	ObjectMapper objectMapper;
	TwitterStream twitterStream;
	String  savetoFolder;

	// not able to user twitterstream is not comprable LinkedHashMap<TwitterStream, TwitterWriter> actives = new LinkedHashMap<TwitterStream, TwitterWriter>();
	// each TwitterAPIStreaming can ha
	public TwitterAPIStreaming()
	{
		
		objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		if(SettingsCrawler.keepInfoFileforJsons)
		{
			infoFile = objectMapper.createObjectNode(); 
			
			infoFile.put("collection", SettingsCrawler.collection);
			infoFile.put("archive_method", SettingsCrawler.archiveMethod);
			infoFile.put("source_origin", SettingsCrawler.sourceOrigin);
		
		}
	

		Log = LoggerFactory.getLogger(TwitterAPIStreaming.class);
		
		try {
			keystoArchive = UtilsCrawler.readTwitterSourceJSON(SettingsCrawler.filetoArchive,"key");
		} catch(Exception e){
		
			Log.error(Utils.convertToJsonMessage(SettingsCrawler.applicationName, Utils.createJsonLogMessage("error", "error", "Not able to get keys to archive : NOTHING TO ARCHIVE : EXIT : " + Throwables.getStackTraceAsString(e), 1)));
			Runtime.getRuntime().halt(0);
		}
		
		this.infoFile.put("archive_method", "streaming");
		//if streaming api is not activated by archiveTrends to check source file regularly
		if(SettingsCrawler.archiveTrends )
		{
			this.infoFile.put("collection", "trends");
		}
		else
		{
			checkifKeysUpdate();
		}
   		
		
		if(!SettingsCrawler.archiveTrends) {
			try {
				UtilsCrawler.moveToErrorFolder(SettingsCrawler.currentFolder,SettingsCrawler.errorFolder );
			
			} catch (Exception e1) {
				Log.error( Utils.convertToJsonMessage(SettingsCrawler.applicationName,  Utils.createJsonLogMessage("error", "error","Not able to move .part files to error folder : " + Throwables.getStackTraceAsString(e1), 1)));
			}
			
		}

		else {
			try {
				UtilsCrawler.moveToErrorFolder(SettingsCrawler.currentFolder + "/streaming/",SettingsCrawler.errorFolder + "/streaming/");
			
			} catch (Exception e1) {
				Log.error( Utils.convertToJsonMessage(SettingsCrawler.applicationName,  Utils.createJsonLogMessage("error", "error","Not able to move .part files for trends streaming to error folder : " + Throwables.getStackTraceAsString(e1), 1)));
			}
			
		}
		
		
			
	}
	

	public boolean runAPI()
	{
		// adding here again to call streaming shutdown 
		// because of trends and archiving at the same time option 
		// actualAPI will be trends and it will only close trends file but not streaming if we use hook of TwitterAPIConnector
		Runtime.getRuntime().addShutdownHook(new Thread() {
    	    public void run() {
    	    	shutDown();
    	        
    	    }
    	});
//      if you do not do it with executor same thread will write to two seperated files 
//		pool-2-thread-1 : trends_2017-08-10T15_53_01Z.jsons
//		pool-2-thread-1 : trends_2017-08-10T15_53_44Z.jsons
//      unless, different threads will write
//		Future<Boolean> future =  this.executor.submit(new Callable<Boolean>() {

		if(keystoArchive.size() > 0)
			return getTweets();
		return false;

	
	}
	


	public boolean getTweets()
	{
		// need to do it here because of multithreading
		
		try{
		
			String header = UtilsCrawler.getJsonHeader(keystoArchive);

			TwitterWriter streamingWriter  = null;
					
			try {
				
				if(SettingsCrawler.archiveTrends) // because of trends 
				{ 
					if(UtilsCrawler.checkandCreateFolder(SettingsCrawler.currentFolder + "/streaming/") && UtilsCrawler.checkandCreateFolder(SettingsCrawler.doneFolder + "/streaming/"))
						
						streamingWriter = new TwitterWriter("streaming", SettingsCrawler.currentFolder + "/streaming/",SettingsCrawler.doneFolder+ "/streaming/",SettingsCrawler.fileNameTimeFormat, header, this.infoFile, SettingsCrawler.mustLock, SettingsCrawler.movetoDone, SettingsCrawler.writerMaxLines );
					else
						Runtime.getRuntime().halt(0);
				}
				else
					streamingWriter = new TwitterWriter(SettingsCrawler.archiveMethod, SettingsCrawler.currentFolder,SettingsCrawler.doneFolder,SettingsCrawler.fileNameTimeFormat, header, this.infoFile, SettingsCrawler.mustLock, SettingsCrawler.movetoDone, SettingsCrawler.writerMaxLines );
			
			
				
			} catch (Exception e) {
				Log.error( Utils.convertToJsonMessage(SettingsCrawler.applicationName,  Utils.createJsonLogMessage("error", "error","SHUTTING DOWN :Not able to create writer" + Throwables.getStackTraceAsString(e), 1)));
				System.exit(1);
			}	
					
			// IMPORTANT  can be null check it test it 		
			twitterStream  = getTwitterStream(streamingWriter);
			
			activeStreams.add(twitterStream);
			activeWriters.add(streamingWriter);
			
	        FilterQuery tweetFilterQuery = new FilterQuery(); // See 
			tweetFilterQuery.track(keystoArchive.toArray(new String[0]));// OR on keywords
	//		tweetFilterQuery.language(new String[]{"fr"});
			twitterStream.filter(tweetFilterQuery);
			
			
		}
		catch(Exception ex)
		{
			Log.error( Utils.convertToJsonMessage(SettingsCrawler.applicationName ,  Utils.createJsonLogMessage("error", "error", "getTweets " + Throwables.getStackTraceAsString(ex) , 1)));
			return false;
		}
		
		return true;
	}
	
	private TwitterStream getTwitterStream(final TwitterWriter streamingWriter)
	{
		
		Credential cre = UtilsCrawler.getActiveCredential(SettingsCrawler.credentials);		
		
		if(cre == null)
		{
			Log.error(Utils.convertToJsonMessage(SettingsCrawler.applicationName, Utils.createJsonLogMessage("error", "error", "Can not go on no active credential : EXIT", 1)));
			Runtime.getRuntime().halt(0);
		}
		
		String msg = Utils.convertToJsonMessage(SettingsCrawler.applicationName, Utils.createJsonLogMessage( "info", "info", "Using Twitter credential for  streaming : " + cre.OWNER, 1));
		Log.info(msg);
		
		TwitterStreamFactory mainFactory = new TwitterStreamFactory(cre.getConfigurationOAuth());
		final TwitterStream twitterStream = mainFactory.getInstance();
		twitterStream.addRateLimitStatusListener(UtilsCrawler.getRateLimitListener());

		
		
		// We should be using just rawstreamlistener but onException is not fired on the original twitter4j code.
		// I added a piece of code to fire it but we should use local twitter4j not maven version thus to ensure the robustness  of the code 
		// we add two listeners 18 10 2017 (it was removed 03 2017 because onStatus was too costy)
		twitterStream.addListener(new StatusListener() {
			
			@Override
			public void onException(Exception ex) {
				System.out.print("$");
				try{activeWriters.getFirst().writeTweet(UtilsCrawler.createJsonFromErrorMessage(ex.getMessage()),"-4");} catch(Exception e){}
            	
            	Log.error(Utils.convertToJsonMessage(SettingsCrawler.applicationName , Utils.createJsonLogMessage( "error", "error",  ex.getMessage(),1)));
        		
				
			}
			
			@Override
			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStatus(Status status) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStallWarning(StallWarning warning) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onScrubGeo(long userId, long upToStatusId) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
				// TODO Auto-generated method stub
				
			}
		});
		
		
		
		twitterStream.addListener(new RawStreamListener() {

            @Override
            public void onMessage(String rawString) {

            	// I do not know why it happens but i get empty tweets time to time
            	if(!rawString.isEmpty())
            	{
            		
            		try{

						// need to parse and stringifyTweet again because of encoding errors 
						  ObjectNode tweet  = UtilsCrawler.parseTweet(rawString);
						  if(tweet.has("id"))
						  {
							  
					        	try {
					        		 System.out.print(".");
					        		 streamingWriter.writeTweet( UtilsCrawler.stringifyTweet(tweet) , tweet.get("id").asText());
								
								} catch (AlreadyLockedException e) {
									
									System.out.print("L");
									
									Log.error(Utils.convertToJsonMessage(SettingsCrawler.applicationName,  Utils.createJsonLogMessage("debug", "exception", "AlreadyLockedException " + Throwables.getStackTraceAsString(e), 1)));
									
				
									
								} catch (CorruptedFileException e) {
									
									Log.error(Utils.convertToJsonMessage(SettingsCrawler.applicationName,  Utils.createJsonLogMessage("debug", "exception", "CorruptedFileException " + Throwables.getStackTraceAsString(e), 1)));

									System.out.print("C>"); // corrupted so closed
									
									//streamingWriter = handleCorruptedFileException(activeWriter);
									maintenanceAPI() ; // nothing to do I can not reinitialize final variable 

									
								} catch (Exception ex) {
					        		 
									Log.error(Utils.convertToJsonMessage(SettingsCrawler.applicationName,  Utils.createJsonLogMessage("debug", "exception",  Throwables.getStackTraceAsString(ex), 1)));

					        	}
		                	 
						  }
						  else
						  {
							   System.out.print("-");
							   streamingWriter.writeTweet(rawString,"0");
						  }
					
						  
						  
						  
	            	} catch(Exception e)	{
	            		e.printStackTrace();
	            		Log.error(Utils.convertToJsonMessage(SettingsCrawler.applicationName , Utils.createJsonLogMessage( "error", "error", "actualwriter, not able to write / create file " + Throwables.getStackTraceAsString(e) ,1)));
	            		
	            	}
	            
            	}
            }

            @Override
            public void onException(Exception e) {
            	
            }
            
            
        }); 

		
		return twitterStream;
		
	}

	public void maintenanceAPI() 
	{
		if(activeStreams.size() > 0) // can happen at the beginning if googlespreadsheet is empty
		{
	    	
		
			TwitterStream  tempStream = activeStreams.getFirst();
			TwitterWriter activeWriter = activeWriters.getFirst();

			// all these because of trends archiving 
			// we would like the new streaming to keep the initial ones settings 
			// unless it will take values from settingscrawler if it is for trends it should not
	    	boolean newRunning = runAPI(); // launch first the other stream
	    
	    	if(newRunning)
	    	{
	    		
	    		System.out.print("Changed at " + new Date().toGMTString());
	    		
	    		try {
					Thread.currentThread().sleep(5000); // to run two streams together for a while
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	    		
	    		
	    		tempStream.cleanUp(); //to close twitter streams properly
	    		activeStreams.removeFirst();
		    	try {
		    			Log.info(Utils.convertToJsonMessage(SettingsCrawler.applicationName , Utils.createJsonLogMessage( "info", "crawled", "Total crawled" , activeWriter.getSizeofTweets())));
		    			activeWriter.closeWriter();
		    			
		    			activeWriters.removeFirst();
		    			System.out.print(">");
						String token =	tempStream.getOAuthAccessToken().getToken();
						UtilsCrawler.releaseCredential(token);
						
				} catch (Exception ex) {
					Log.error( Utils.convertToJsonMessage(SettingsCrawler.applicationName ,  Utils.createJsonLogMessage("error", "exception", "maintenanceAPI credential  " +  Throwables.getStackTraceAsString(ex) , 1)));

				}
		    	
		    	
		
		    	
	    	}
		}
		else
			runAPI();
			

	}
	
    private void updateAPIKeys() 
    {
    	List<String> temp = null;
    	try
    	{
    		temp =   UtilsCrawler.readTwitterSourceJSON(SettingsCrawler.filetoArchive, "key");
    	}
    	catch(Exception e)
    	{
    		Log.error(Utils.convertToJsonMessage(SettingsCrawler.applicationName, Utils.createJsonLogMessage("error", "exception", "this.keysfile " + Throwables.getStackTraceAsString(e), 1)));
    	}
    	
    	if(temp != null)
    	{
    		
	        	boolean updated = !UtilsCrawler.equalLists(this.keystoArchive,temp);
		        if(updated)
		        {
		        		this.keystoArchive.clear();
		        		this.keystoArchive.addAll(temp);
		        		this.maintenanceAPI();
		        		
	    			
		        }
    	}
  
    	
    }
    /**
     * This function checks if any new source(hashtag etc) is added to a file specified in config (SettingsCrawler.filetoarchive)
     * 
     */
    private void checkifKeysUpdate()
	{
	
		Runnable runnable = new Runnable() {
			
			@Override
			public void run() {
				
					System.out.print("G");
					updateAPIKeys();
					
					
					Log.debug( Utils.convertToJsonMessage(SettingsCrawler.applicationName, Utils.createJsonLogMessage("debug", "readFile", "checkifSpreadSheetUpdate Checking updates from file " , 1)));
					
					
				}    
			
		};
          
		
		ScheduledExecutorService  executor = new ScheduledThreadPoolExecutor(1);
		executor.scheduleAtFixedRate(runnable, 1, SettingsCrawler.fileCheckInterval, TimeUnit.MINUTES);
		

		
		
	}
    

	public void shutDown() {
			
			if(activeStreams != null)
			{
				for(TwitterStream t : activeStreams)
				{
					t.shutdown();
				}
			}

			if(activeWriters.size() >0) // checking if it is not already closed 
			{
				
				for(TwitterWriter t : activeWriters)
				{
					
					if(t.getWriter() != null ) 
					{
							System.out.print(">");
							// it can be already closed properly it is just to be sure we close again here but we do not log
							try {	t.closeWriter(); }catch(IOException e) {} 
							
							
					}
				}
			}
				
			Runtime.getRuntime().halt(0);
		
	}
				

}
