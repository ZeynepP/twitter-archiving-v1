package fr.ina.dlweb.twitter.crawler.apis;

import java.util.HashSet;

import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Throwables;
import com.google.common.primitives.Longs;

import fr.ina.dlweb.bloom.BloomFilter;
import fr.ina.dlweb.bloom.BloomFilters;
import fr.ina.dlweb.io.FileLineWriter.AlreadyLockedException;
import fr.ina.dlweb.io.FileLineWriter.CorruptedFileException;
import fr.ina.dlweb.twitter.commons.io.CommonsFileReader;
import fr.ina.dlweb.twitter.commons.utils.Utils;
import fr.ina.dlweb.twitter.crawler.utils.SettingsCrawler;
import fr.ina.dlweb.twitter.crawler.utils.UtilsCrawler;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

public class TwitterAPIGetByIngest extends ITwitterAPIs {
	

	BloomFilter bloom;
	final long EXPECTED = 1000000;
	final int BLOOM_WINDOW_SIZE = 500000;
	final double FPP = 0.000000001;
	int counter=0;

	
	
	public TwitterAPIGetByIngest() 
	{
		super();

		bloom = BloomFilters.createLongArrayBloomFilter(EXPECTED, FPP);
		Log = LoggerFactory.getLogger(TwitterAPIGetByIngest.class);
		twitter = getTwitterAPI();

		
	}

	public boolean runAPI( )
	{
		
		boolean isover = false;
		HashSet<Long> ids = new HashSet<Long>(100);
		JsonNode onetweet ;
		JsonNode jsonObject;
	
		String[] seekerfiles = SettingsCrawler.filetoArchive.split(";");
		long offset = 0;
		for( String file : seekerfiles)
		{
			
			long id;
			CommonsFileReader ingestids = null;
			try {
				ingestids = new CommonsFileReader(file, offsets.get(file) == null ? 0 : Long.valueOf(offsets.get(file) ));
			} catch (Exception e) {
				String msg = Utils.convertToJsonMessage(SettingsCrawler.applicationName , Utils.createJsonLogMessage( "error", "error", "Commons : TweetsIdsFromFile " + Throwables.getStackTraceAsString(e) ,1));
				Log.error(msg);
			}
			
			
		
			
			if(ingestids!= null)
			{
					
				
					while (ingestids.hasNext())
					{
						try {
							
							offset= ingestids.getOffsetJsonfile();
							onetweet = objectMapper.readTree(ingestids.next());
						
						
							jsonObject= objectMapper.readValue(onetweet.get(1).toString(),JsonNode.class);	
							
							id = jsonObject.get("id").asLong();
							
							
							if(!bloom.contains(Longs.toByteArray(id))) // oR String.valueOf(longVar).getBytes()
							{
								ids.add(id);
								bloom.put(Longs.toByteArray(id));
								counter ++;
							}
							// not 99 because of rate limit error 
							if(ids.size() > 99)
							{
								
								isover = getTweets(Longs.toArray(ids));
								if(!isover)
									ingestids = new CommonsFileReader(file,offset); // go back because of limit errors to get bak related ids 
								ids.clear();
							}
							
							// bloom window slice reinitilize bloom 
							if(counter > BLOOM_WINDOW_SIZE )
							{
								counter = 0;
								bloom = BloomFilters.createLongArrayBloomFilter(EXPECTED, FPP);
							}
							
						} catch (Exception e) {
							Log.error(Utils.convertToJsonMessage(SettingsCrawler.applicationName , Utils.createJsonLogMessage( "error", "error",  Throwables.getStackTraceAsString(e) , 1)));
							isover = false;
						}
					}
					
					
					if(ids.size() > 0)
					{
						isover = getTweets(Longs.toArray(ids));
						ids.clear();
					}
					
					if(isover) // if something went wrong no need to update offsets next time it should start with the same offset
						offsets.put(file, String.valueOf(ingestids.getOffsetJsonfile()));
			}
			
			if(ingestids != null) ingestids.close();
		}
		

		shutDown();
	
		return isover;

	}


	public boolean getTweets(long[] idslong)
	{
		ResponseList<Status> status = null;
		long id;
		try{
				if(idslong.length <= 100){	
					status = twitter.lookup(idslong);
				}
				else{//should not happen just checking 
					System.out.print(idslong.length + " ???" );
				}
				
				
				System.out.print(idslong.length + "-" + status.size() + "!" ); 
	
				for (Status st : status)
				{
					
					id = st.getId();
					
					try {
						
						activeWriter.writeTweet(TwitterObjectFactory.getRawJSON(st),String.valueOf( id));
					
					} catch (AlreadyLockedException e) {
						
						System.out.print("L");
						
						Log.error(Utils.convertToJsonMessage(SettingsCrawler.applicationName,  Utils.createJsonLogMessage("debug", "exception", "AlreadyLockedException " + Throwables.getStackTraceAsString(e), 1)));
						
	
						
					} catch (CorruptedFileException e) {
						
						Log.error(Utils.convertToJsonMessage(SettingsCrawler.applicationName,  Utils.createJsonLogMessage("debug", "exception", "CorruptedFileException " + Throwables.getStackTraceAsString(e), 1)));

						System.out.print("C>"); // corrupted so closed
						
						activeWriter = handleCorruptedFileException(activeWriter);

						
					} 
					
				
				}
				//activeWriter will be closed 
				Log.info( Utils.convertToJsonMessage(SettingsCrawler.applicationName , Utils.createJsonLogMessage( "info", "crawled", "Total ids crawled", status.size())));

		}
		catch(TwitterException te)
		{
			System.out.print("$");
			try{activeWriter.writeTweet(UtilsCrawler.createJsonFromErrorMessage(te.getMessage()),"-4");} catch(Exception e){}
			return false;
			
		} catch (Exception e) {
			
			System.out.print("-");
			
			Log.error(Utils.convertToJsonMessage(SettingsCrawler.applicationName,  Utils.createJsonLogMessage("debug", "exception", "getTweets " + Throwables.getStackTraceAsString(e), 1)));
			return false;
			
		}
	
		
		return true;
	}

	@Override
	boolean getTweets() {
		return false;
	}





}
