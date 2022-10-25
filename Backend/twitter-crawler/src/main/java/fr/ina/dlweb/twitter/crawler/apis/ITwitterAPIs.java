package fr.ina.dlweb.twitter.crawler.apis;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Throwables;

import fr.ina.dlweb.twitter.commons.io.TwitterWriter;
import fr.ina.dlweb.twitter.commons.utils.Utils;
import fr.ina.dlweb.twitter.crawler.utils.Credential;
import fr.ina.dlweb.twitter.crawler.utils.SettingsCrawler;
import fr.ina.dlweb.twitter.crawler.utils.UtilsCrawler;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.OAuth2Token;

public abstract class ITwitterAPIs {

	Logger Log = LoggerFactory.getLogger(ITwitterAPIs.class);
	
	Twitter twitter ;
	ObjectNode infoFile = null;
	TwitterWriter activeWriter = null;

	ConcurrentHashMap<String, String> offsets ;
	public List<String> keystoArchive = new ArrayList<String>(); // should not be null because of 
	HashMap<Long, String> timelineIdScreenname;
	
	ObjectMapper objectMapper;
	String activeToken ;
	String offsetfile, applicationName;

	boolean needOffsets = false;

	// instead of using boolean we could use void but there are cases we use the return value ex: streaming
	public abstract boolean runAPI();
	abstract boolean getTweets();
	
	public ITwitterAPIs() {


		this.offsetfile = SettingsCrawler.offsetfile;
		this.applicationName = SettingsCrawler.applicationName;
		needOffsets = !(SettingsCrawler.archiveMethod.equals(SettingsCrawler.STREAMING) || SettingsCrawler.archiveMethod.equals(SettingsCrawler.TRENDS)|| SettingsCrawler.archiveMethod.equals(SettingsCrawler.SEARCH));
				
		objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		
		
		if( UtilsCrawler.countFilesinFolder(SettingsCrawler.currentFolder) != 0)
		{
			Log.error( Utils.convertToJsonMessage(SettingsCrawler.applicationName,  Utils.createJsonLogMessage("error", "error","There are already files in current folder !!  Move them to error before moving on!!!", 1)));
			System.exit(1);
		}

		try{
			if(SettingsCrawler.archiveMethod.equals(SettingsCrawler.TIMELINE))
			{

				try{
					timelineIdScreenname = UtilsCrawler.readTimelineIdScreenName(SettingsCrawler.filetoArchive, "id", "key");
					timelineIdScreenname = Utils.sortByValues(timelineIdScreenname);
					// why I bother to sort : because for timelines it is nice to see where the crawl is based on screenname displayed on screen
				} catch(Exception e){
					
					Log.error(Utils.convertToJsonMessage(SettingsCrawler.applicationName, Utils.createJsonLogMessage("error", "error", "Timelines source file does not contain users id: GOING ON WITH SCREENNAMES " + Throwables.getStackTraceAsString(e), 1)));
					keystoArchive = UtilsCrawler.readTwitterSourceJSON(SettingsCrawler.filetoArchive,"key");
					
				}
			}
			
			else if(!SettingsCrawler.archiveMethod.equals(SettingsCrawler.INGEST) && !SettingsCrawler.archiveMethod.equals(SettingsCrawler.TRENDS))
			{
				keystoArchive = UtilsCrawler.readTwitterSourceJSON(SettingsCrawler.filetoArchive,"key");
				
			}
		} catch(Exception e){
		
			Log.error(Utils.convertToJsonMessage(SettingsCrawler.applicationName, Utils.createJsonLogMessage("error", "error", "Not able to get keys to archive : NOTHING TO ARCHIVE : EXIT : " + Throwables.getStackTraceAsString(e), 1)));
			Runtime.getRuntime().halt(0);
		}
		
		
			
		
		if(needOffsets)
		{
			try {
				offsets = Utils.readHashMapFromFile(SettingsCrawler.offsetfile,";",0,1);
				Utils.writeHashMap(SettingsCrawler.offsetfile + ".bck",false, offsets,true);
			} catch (Exception e) {
				String msgerror = Utils.convertToJsonMessage(SettingsCrawler.applicationName, Utils.createJsonLogMessage("error", "exception", "Reading offsets file : " +  Throwables.getStackTraceAsString(e) , 1));
				Log.error(msgerror);
				//For file not found offset will be 0 for all
				offsets = new ConcurrentHashMap<String, String>();
			}
		}
		

		if(SettingsCrawler.keepInfoFileforJsons)
		{
			infoFile = objectMapper.createObjectNode(); 
			
			infoFile.put("collection", SettingsCrawler.collection);
			infoFile.put("archive_method", SettingsCrawler.archiveMethod);
			infoFile.put("source_origin", SettingsCrawler.sourceOrigin);
		
		}
		
		
		
		
		try {
			activeWriter = new TwitterWriter(SettingsCrawler.archiveMethod, SettingsCrawler.currentFolder,SettingsCrawler.doneFolder,SettingsCrawler.fileNameTimeFormat, null, this.infoFile, SettingsCrawler.mustLock, SettingsCrawler.movetoDone, SettingsCrawler.writerMaxLines );		
			
		} catch (Exception e) {
			Log.error( Utils.convertToJsonMessage(SettingsCrawler.applicationName,  Utils.createJsonLogMessage("error", "error","Not able to cerate writer" + Throwables.getStackTraceAsString(e), 1)));
			shutDown();
		}
		
		
		
	}
	

	protected Twitter getTwitterAPI()
	{
		
		Credential cre = UtilsCrawler.getActiveCredential(SettingsCrawler.credentials);
		
		if(cre == null)
		{
			Log.error(Utils.convertToJsonMessage(SettingsCrawler.applicationName, Utils.createJsonLogMessage("error", "error", "TwitterAPISearch : initEnv Can not go on no active credential : EXIT", 1)));
			System.exit(0);
		}
		
		String msg = Utils.convertToJsonMessage(SettingsCrawler.applicationName,Utils.createJsonLogMessage( "info", "info", "Using Twitter credential for  " + SettingsCrawler.archiveMethod + " : " + cre.OWNER, 1));
		Log.info(msg);
		
		
		activeToken = cre.OAUTH_TOKEN;
		// OAuth2Token is used because
		// Requests / 15-min window (user auth)	180
		// Requests / 15-min window (app auth)	450
		
		OAuth2Token token = null;
		cre.onlyAuthEnabled = true;
		TwitterFactory tFactory =  new TwitterFactory(cre.getConfigurationOAuth());
		Twitter twitter = tFactory.getInstance();
		
		twitter.addRateLimitStatusListener(UtilsCrawler.getRateLimitListener());
		
		try {
			token = new TwitterFactory(cre.getConfigurationOAuth()).getInstance().getOAuth2Token();
		    twitter.setOAuth2Token(token);

		} catch (TwitterException e) {
			Log.debug(Utils.convertToJsonMessage(SettingsCrawler.applicationName, Utils.createJsonLogMessage("debug", "exception", "TwitterAPISearch : user auth will be used " + e.getErrorMessage() , 1)));

		}
		return twitter;
		
	}


	public void shutDown() {
		
		
		Log.debug( Utils.convertToJsonMessage(SettingsCrawler.applicationName,  Utils.createJsonLogMessage("debug", "shutDown", "shutDown called writing tweets to file and closing writer ", 1)));
		
		try {
			if(needOffsets)
				Utils.writeHashMap(SettingsCrawler.offsetfile,false, this.offsets,true);
		} catch (Exception e) {
			Log.error(Utils.convertToJsonMessage(SettingsCrawler.applicationName , Utils.createJsonLogMessage( "error", "error", "Commons : writeHashMap " + Throwables.getStackTraceAsString(e)  , 1)));
			
		}
		
		
		try {
			if(activeWriter!= null && activeWriter.getWriter() != null)
			{
				activeWriter.closeWriter();
			
			}
		} catch (IOException e) {
			Log.error(Utils.convertToJsonMessage(SettingsCrawler.applicationName , Utils.createJsonLogMessage( "error", "error", "Commons : closeWriter " + Throwables.getStackTraceAsString(e)  , 1)));
			
		}
		Runtime.getRuntime().halt(0);
	}
	

	public TwitterWriter handleCorruptedFileException(TwitterWriter activeWriter)
	{
		System.out.print("C>"); // corrupted so closed
		try {

		    Path source = Files.move(Paths.get(activeWriter.fileTweetsCurrentPath), Paths.get(activeWriter.fileTweetsCurrentPath + ".corrupted"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
		    activeWriter.closeWriter();
		    activeWriter = null;
		    if(source!= null)
		    	activeWriter = new TwitterWriter(SettingsCrawler.archiveMethod, SettingsCrawler.currentFolder,SettingsCrawler.doneFolder,SettingsCrawler.fileNameTimeFormat, null, this.infoFile, SettingsCrawler.mustLock, SettingsCrawler.movetoDone, SettingsCrawler.writerMaxLines );	
		   
		    
		    
		} catch (IOException ex) {
			Log.error(Utils.convertToJsonMessage(SettingsCrawler.applicationName , Utils.createJsonLogMessage( "error", "error",  Throwables.getStackTraceAsString(ex)  , 1)));
			
		}
		
		return activeWriter;
		
	}
	
	
	
}

