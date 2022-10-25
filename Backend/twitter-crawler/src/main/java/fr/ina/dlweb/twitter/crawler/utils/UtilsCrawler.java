package fr.ina.dlweb.twitter.crawler.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Throwables;

import fr.ina.dlweb.twitter.commons.io.CommonsFileReader;
import fr.ina.dlweb.twitter.commons.utils.Utils;
import twitter4j.RateLimitStatusEvent;
import twitter4j.RateLimitStatusListener;


public class UtilsCrawler {
	static Logger Log = LoggerFactory.getLogger(UtilsCrawler.class);
	public static ObjectMapper jsonMapper = new ObjectMapper();
	static DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
	static {
		jsonMapper.configure(SerializationFeature.INDENT_OUTPUT, false);
		
	}
	
	public static boolean checkandCreateFolder(String folderName)
	{
		
		
		File theDir = new File(folderName);
		boolean result = false;
		// if the directory does not exist, create it
		if (!theDir.exists()) {
		    try{
		    	result = theDir.mkdirs();//recursive
		        
		    } 
		    catch(SecurityException se){
		    	se.printStackTrace();
		    }        
		   
		}
		else
			result = true;
		
		return result;
		
	}
	
	public static String createJsonFromErrorMessage(String errorMessage) {
		JsonNode errorJson;
		try {
			JsonNode json = jsonMapper.readTree(errorMessage);
			errorJson = jsonMapper.createObjectNode().set("jsonError", json);
		} catch(Exception e) {
			errorJson = jsonMapper.createObjectNode().put("rawError", errorMessage);
		}

		try {
			return jsonMapper.writeValueAsString(errorJson);
		} catch(IOException ioe) {
			// jth
			// dead code. Must never happen
			return "{\"error\":\"impossible error\"}";
		}
	}

	public static String getJsonHeader(List<String> queries)
	{
		
		ObjectNode jsonHeader = jsonMapper.createObjectNode();
		jsonHeader.put("follow", jsonMapper.createArrayNode());
		ArrayNode lquery = jsonMapper.createArrayNode();
		for (String query : queries)
		{	
			lquery.add(query);
		}
			
		jsonHeader.put("track", lquery);
		try {
			
			return jsonMapper.writeValueAsString(jsonHeader);
		} catch (JsonProcessingException e) {
			Log.error(Utils.convertToJsonMessage(SettingsCrawler.applicationName , Utils.createJsonLogMessage( "error", "error", "getJsonHeader " +  Throwables.getStackTraceAsString(e)  , 1)));
			return null;
		}
		
		
	}
	public static Credential[] getCredentials(String pathjson, String applicationName) throws JsonParseException, JsonMappingException, JsonProcessingException, IOException
	{

		ObjectNode rootNode = (ObjectNode) jsonMapper.readTree( Files.readAllBytes(Paths.get(pathjson)));
		// TODO: stupid you need to be able to get array and map it to Credential class check it later no time now
		return jsonMapper.readValue( jsonMapper.writeValueAsString(rootNode.get(applicationName))  ,Credential[].class);

		
	}
	

	public static int countFilesinFolder(String folder)
	{
		
		File f = new File(folder);
		File[] files = f.listFiles();
		return files == null ? 0 : files.length;
		
	}
	public static void moveToErrorFolder(String currentFolder, String errorFolder) throws IOException
	{
		File f = new File(currentFolder);
		File[] files = f.listFiles();
		for(File file : files) 
		{
			if(!file.isDirectory())
				Files.move(Paths.get(file.getAbsolutePath()), Paths.get(errorFolder, file.getName()));
		}
		
		
	}
	
	public static ObjectNode parseTweet(String rawjson) throws JsonProcessingException, IOException
	{
		return (ObjectNode) jsonMapper.readTree(rawjson);
		
	}
	
	public static String stringifyTweet(ObjectNode obj) throws JsonProcessingException, IOException
	{
		return jsonMapper.writeValueAsString(obj);
	}
	

	
	public static HashMap<Long,String> readTimelineIdScreenName(String path, String key, String value) throws Exception
	{
	
		JsonNode tempNode;
		CommonsFileReader fileReader;
		HashMap<Long,String> hash = null;
	
		fileReader = new CommonsFileReader(path, 0);
		hash = new HashMap<Long, String>();

        while (fileReader.hasNext()) 
        {
        	tempNode = jsonMapper.readTree(fileReader.next());
			if(tempNode.get("active").asInt() == 1)
			{
				if(tempNode.has(value))
					hash.put(tempNode.get(key).asLong(), tempNode.get(value).asText().trim());
				else
					hash.put(tempNode.get(key).asLong(), null);
			}
			
        }
        
        fileReader.close();
	
		
		return hash;
		
		
	}
	
	
	
	public static List<String> readTwitterSourceJSON(String path, String key) throws Exception
	{
		JsonNode tempNode;
		CommonsFileReader fileReader;
		Set<String> keys = new HashSet<String>();
		DateTime start;
		DateTime stop;

		fileReader = new CommonsFileReader(path, 0);
		
		String line;
        while (fileReader.hasNext()) 
        {
        	line = fileReader.next();
        	try {
	        	tempNode = jsonMapper.readTree(line);
	        	// added several controls to be sure it works with all date combinations 
	        	if(tempNode.has("start_date") && tempNode.has("stop_date") )
	        	{
	        	
	        		start = dtf.parseDateTime(tempNode.get("start_date").asText());
	        		stop = dtf.parseDateTime(tempNode.get("stop_date").asText());
	        		
	        		if( start.isBeforeNow() && stop.isAfterNow())
	        				keys.add(tempNode.get(key).asText().trim());
	        				
	        	}
	        	else if(tempNode.has("start_date"))
	        	{
	        		start = dtf.parseDateTime(tempNode.get("start_date").asText());
	        		if( start.isBeforeNow())
	    				keys.add(tempNode.get(key).asText().trim());
	        	}
	        	else if(tempNode.has("stop_date"))
	        	{
	        		stop = dtf.parseDateTime(tempNode.get("stop_date").asText());
	        		if( stop.isAfterNow())
	    				keys.add(tempNode.get(key).asText().trim());
	        	}
	        	else if(tempNode.get("active").asInt() == 1)
					keys.add(tempNode.get(key).asText().trim());
        	}catch(Exception ex)
        	{
        		Log.error( Utils.convertToJsonMessage(SettingsCrawler.applicationName ,  Utils.createJsonLogMessage("error", "error", "Utils crawler : readTwitterSourceJSON not able to read " + line , 1)));
        	}
			
        }
        
        fileReader.close();

		
		return new ArrayList<String>(keys);

	}

	
   public static RateLimitStatusListener getRateLimitListener()
   {

	   return new RateLimitStatusListener() {
			
						@SuppressWarnings("static-access")
						@Override
						//Called when the response contains rate limit status.
						public void onRateLimitStatus(RateLimitStatusEvent event) {
							int remainingquery = event.getRateLimitStatus().getRemaining();
							if(remainingquery == 0)
							{
		
								if(event.isAccountRateLimitStatus())
									System.out.print("0");
								else if(event.isIPRateLimitStatus())
									System.out.println("URGENT-IP");
								try {
									if(event.getRateLimitStatus().getSecondsUntilReset() > 0)
										Thread.currentThread().sleep(event.getRateLimitStatus().getSecondsUntilReset()*1000);
								} catch (InterruptedException e) {
									Thread.currentThread().interrupt();
								}
							}	
							
							
						}
						
						@SuppressWarnings("static-access")
						@Override
						public void onRateLimitReached(RateLimitStatusEvent event) {
							// TODO ZP : what to do there?Auto-generated method stub
							//onRateLimitReached(RateLimitStatusEvent event)
							//Called when the account or IP address is hitting the rate limit.
							//onRateLimitStatus will be also called before this event.
							// seconduntil reset not ok  {remaining=0, limit=900, resetTimeInSeconds=1489582008, secondsUntilReset=0}
							try {
								if(event.getRateLimitStatus().getSecondsUntilReset() > 0)
									Thread.currentThread().sleep( event.getRateLimitStatus().getSecondsUntilReset()*1000);
							} catch (InterruptedException e) {
								Thread.currentThread().interrupt();
							}
							
						}
			};
	   
   }
	
	@SuppressWarnings("unused")
	public static boolean equalLists(List<String> a, List<String> b){     
	    // Check for sizes and nulls
		if(a == null || b ==null)
			return false;
		if (a == null && b == null) 
	    	return true;
	    if ((a.size() != b.size()) || (a == null && b!= null) || (a != null && b== null)){
	        return false;
	    }

	    

	    // Sort and compare the two lists          
	    Collections.sort(a);
	    Collections.sort(b);      
	    return a.equals(b);
	}
	
	

	
	// get one credential from list and desactive the others
	public static Credential getActiveCredential( Credential[] credentials)
	{
		Credential active = null;
		for(Credential c : credentials)
		{
			if(c.isActive == false)
			{
				c.isActive = true;
				c.activecounter++;
				active = c;
				break;
			}

		}
		
		if(active == null) // second pass to get an active cre but used only once
		{
			for(Credential c : credentials)
			{
				if(c.activecounter <2 )
				{
					c.isActive = true;
					c.activecounter++;
					active = c;
					
				}

			}
			
			
		}
		return active;
		
		
	}
	

	
	public static void releaseCredential(String token)
	{
		for(Credential c : SettingsCrawler.credentials)
		{
			if(c.OAUTH_TOKEN.equals(token ))
			{
					c.activecounter--;
					if(c.activecounter == 0)
						c.isActive = false;
				
					break;
			}

		}

	
	}
	

	
}
