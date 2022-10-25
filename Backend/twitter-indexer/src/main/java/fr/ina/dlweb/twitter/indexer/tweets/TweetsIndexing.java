
package fr.ina.dlweb.twitter.indexer.tweets;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.Charsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.io.CharStreams;

import fr.ina.dlweb.twitter.commons.io.CommonsFileReader;
import fr.ina.dlweb.twitter.commons.utils.Utils;
import fr.ina.dlweb.twitter.indexer.AbstractIndexing;
import fr.ina.dlweb.twitter.indexer.utils.Settings;
import io.searchbox.action.BulkableAction;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Update;
import io.searchbox.core.Index;


public class TweetsIndexing extends AbstractIndexing {

	static Logger Log = LoggerFactory.getLogger(TweetsIndexing.class);
	ObjectMapper objectMapper;
	


	public TweetsIndexing(String[] es_host, String indexname, String indexschema, String indextype,String indexsettingsfile) {
		super(es_host, indexname, indexschema, indextype, indexsettingsfile);
		// To Jérôme, I know it is expensive to create objectmapper for each file but unless it is not working on multi-threading 
		// This is called only once for each file
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm a z");
		objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		objectMapper.setDateFormat(df);
	}

	public boolean indexOneFile(String file)
	{
		boolean isOk = true;
		// I CAN GET file names recursively from root directory 
		// like /data/twitter/dlweb where inside there is "trends" 
		// instead of getting too many log lines with parsing error I prefer to remove them manually here
		if(!file.contains("trends_"))
		{
			
			List<BulkableAction<DocumentResult>> docBuffer = new ArrayList<BulkableAction<DocumentResult>>();

			int total = 0;
			
			// Getting offset from map if not in map add it with offset 0 
			long offset = Settings.mapFileOffset.containsKey(file) ? Long.valueOf( Settings.mapFileOffset.get(file)) : 0l;
			Settings.mapFileOffset.put(file, String.valueOf( offset));// for files that has no content etc adding here updated later if needed
			
			Log.debug( Utils.convertToJsonMessage(Settings.applicationName,  Utils.createJsonLogMessage("info", "info", "Starting for file " + file + " with offset " + String.valueOf(offset), 1)));
			
			String sourceType="",archiveMethod=""  , collection ="";
			CommonsFileReader tweetsIterator = null;
			
			try{
				
				JsonNode infoJson = getInfoFile(file);
				sourceType = infoJson.get("source_origin").asText();
				archiveMethod = infoJson.get("archive_method").asText();
				collection = infoJson.get("collection").asText();
				
				if(infoJson.get("file_length").asLong() > offset){
					tweetsIterator = new CommonsFileReader(file,offset);
				}
				else if(offset != infoJson.get("file_length").asLong()) {
					//should not happen but it happens time to time only for 1 byte !! 
					//System.out.println(file + " offset: " + offset + " file_length: " + infoJson.get("file_length").asText());
					Log.debug( Utils.convertToJsonMessage(Settings.applicationName,  Utils.createJsonLogMessage("debug", "debug", file + " offset: " + offset + " file_length: " + infoJson.get("file_length").asText() , 1)));
				}
				
			    		
			}
			catch(Exception ex)
			{
				Log.error( Utils.convertToJsonMessage(Settings.applicationName,  Utils.createJsonLogMessage("debug", "exception", "No file reader for  "+ file +  " " + ex.getMessage() , 1)));
				
				tweetsIterator = null;
			}
			String[] tmp;
			if(tweetsIterator!= null)
			{
				while(tweetsIterator.hasNext()) {
					
					String json = tweetsIterator.next();
					String t= json.substring(json.length() - 1);
					if(t.equals("}"))	json += "]"; // corrupted files happens time to time in persee files
						// [1513645537,-4,Connection refused] gives exception with objmapper
					try{
						tmp = json.split(",",3);
						if(Long.parseLong(tmp[1].trim()) > 0)
						{		
							JsonNode onetweet = objectMapper.readTree(json);
							getIndexTweet(onetweet.get(2), objectMapper, onetweet.get(0).asText(), file, sourceType, archiveMethod, docBuffer, collection, 0);		
							if(docBuffer.size() > Settings.maxRecordCount)
							{
								isOk = indexTweets(docBuffer);
								
								if(isOk)
								{
									total+=docBuffer.size();
									offset = tweetsIterator.getOffsetJsonfile();
									docBuffer.clear();
								}
								else
								{
									Log.error( Utils.convertToJsonMessage(Settings.applicationName,  Utils.createJsonLogMessage("error", "error", "BufferToES problem with ES !!! for file " + file , 1)));
								}
									
							}
							
						}
	
					}
					catch(Exception ex)
					{
						Log.error( Utils.convertToJsonMessage(Settings.applicationName,  Utils.createJsonLogMessage("error", "exception", "Error while reading tweet  "+ file +  " " + ex.getMessage() , 1)));
		
					}
					
				}
	
			}
			else
			{
				Log.info( Utils.convertToJsonMessage(Settings.applicationName,  Utils.createJsonLogMessage("info", "exception", "No tweets to iterate for "+ file +  " "  , 1)));
				
			}
	
			
			if(docBuffer.size() > 0)
			{
				isOk = indexTweets(docBuffer);
				
				if(isOk)
				{
					total+=docBuffer.size();
					offset = tweetsIterator.getOffsetJsonfile();
					docBuffer.clear();
				}
				else
				{
					Log.error( Utils.convertToJsonMessage(Settings.applicationName,  Utils.createJsonLogMessage("error", "error", "BufferToES problem with ES !!! for file " + file , 1)));
				}
					
			}
			
			tweetsIterator.close();
			Settings.mapFileOffset.put(file, String.valueOf( offset));
// It was for eestimation no need we will do it on scala			
//			if(maplimitrate.size() > 0)
//			{
//				try {
//					Utils.writeTwitterLimitErrors(Settings.limitFiles, maplimitrate,  file);
//				} catch (Exception e) {
//					Log.error( Utils.convertToJsonMessage(Settings.applicationName,  Utils.createJsonLogMessage("error", "error", "Not able to writeTwitterLimitErrors "+ e.getMessage() +  " " +Settings.limitFiles  , 1)));
//				}
//			}
			//Log.info( Utils.convertToJsonMessage(Settings.applicationName,  Utils.createJsonLogMessage("info", "info", "Over for file "+ file  , total)));
		}
			
		
		return isOk;
	}

	
	private void getIndexTweet(JsonNode jtweet,ObjectMapper objectMapper,String archivedAt, String file,String sourceType,String methodArchive, List<BulkableAction<DocumentResult>> docBuffer, String collection, int isExtended) // is fromSource added to distinct the tweets we archived and tweets we get from retweets etc. 
	{
			TweetMeta tweet = createTweettoIndex(jtweet, objectMapper, archivedAt, file, sourceType, methodArchive, collection, isExtended);
			
			if(Settings.upsert)
			{
				String script = getEsUpsertScript(tweet, objectMapper, file, sourceType, methodArchive, collection,isExtended);
					
				if(script != null) {
					docBuffer.add(new Update.Builder(script).setParameter("retry_on_conflict",100).id(tweet.getId()).build());}
			}
			else
			{
				
				try {
					docBuffer.add(new Index.Builder(objectMapper.writeValueAsString(tweet)).id(String.valueOf(tweet.getId())).build());
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			// checking for retweet 
			if(tweet.getRetweeted() == 1)
				getIndexTweet(tweet.getRetweeted_status(), objectMapper, archivedAt , file, sourceType, methodArchive, docBuffer,collection, 1);
			// checking for quote 
			if(tweet.getQuoted() == 1)
				getIndexTweet(tweet.getQuoted_status(), objectMapper,archivedAt, file, sourceType, methodArchive, docBuffer,collection, 1);
			
	 }
	
	
	private TweetMeta createTweettoIndex(JsonNode onetweet,ObjectMapper objectMapper,String archivedAt, String file,String sourceType,String methodArchive,String collection, int isExtended)
	{
		
		TweetMeta tweet = objectMapper.convertValue(onetweet,TweetMeta.class);
		tweet.setArchived_at(Long.parseLong( archivedAt) *1000);
		tweet.setMethod_archive(methodArchive);
		tweet.setIndexed_at(new Date().getTime());
		tweet.setSource_path(file);
		tweet.setSource_type(sourceType);
		tweet.setCollection(collection);
		tweet.setIs_extended(isExtended);
		tweet.update(onetweet);
		// not able to map it dricetly because we use here JsonNode recursivly and I do not want to create a objetMapper for TweetMeta class
		// TODO : this can be done better with jsonignore etc in TweetMeta class 
		if(tweet.getRetweeted() == 1)
			tweet.setRetweeted_tweet(objectMapper.convertValue(tweet.getRetweeted_status(),RetweetedQuatedStatus.class));
		if(tweet.getQuoted() == 1)
			tweet.setQuoted_tweet(objectMapper.convertValue(tweet.getQuoted_status(),RetweetedQuatedStatus.class));
		return tweet;
		
	}
	
	
	private String getEsUpsertScript(TweetMeta tweet,ObjectMapper objectMapper,String file,String sourceType,String methodArchive, String collection, int isExtended)
	{
		try {
			ObjectNode rootNode =objectMapper.createObjectNode();
			rootNode.put("script", Settings.upsertScript);
			
			ObjectNode params = rootNode.putObject("params");
			params.put("colname", collection);
			params.put("archivedate", tweet.getArchived_at());
			params.put("spath",file);
			params.put("marchive",methodArchive);
			params.put("stype",sourceType);
			params.put("sid",isExtended);
				
			rootNode.put("upsert",objectMapper.valueToTree(tweet));
	
			
			return objectMapper.writeValueAsString(rootNode);
			
		} catch (JsonProcessingException ex) {
			
			Log.error( Utils.convertToJsonMessage(Settings.applicationName,  Utils.createJsonLogMessage("debug", "exception", " getEsUpsertScript " + ex.getMessage(), 1)));
			return null;
		}
		
		
	}

	
	private JsonNode getInfoFile(String forFile) throws Exception 
	{
		String infoFilename = forFile.replace(".jsons", "_info.json");
		JsonNode node = null;
		InputStream inputStream = null;
		try
		{
			inputStream = Utils.getInputStream(infoFilename, 0);
			String s= CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8));
			return objectMapper.readTree(s);
			
	
		}
		catch(Exception ex)
		{
			Log.error( Utils.convertToJsonMessage(Settings.applicationName,  Utils.createJsonLogMessage("error", "error", " INFO FILE  " + forFile + " :" + ex.getMessage(), 1)));
			
		}
		finally {
			if(inputStream!=null)
				inputStream.close();
		}
		return node;
		
	}
}

	
	


