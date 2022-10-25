package fr.ina.dlweb.twitter.crawler.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ina.dlweb.conf.Conf;
import fr.ina.dlweb.twitter.commons.utils.Utils;


public class SettingsCrawler {
	
	static Logger Log = LoggerFactory.getLogger(SettingsCrawler.class);
	
	public static String TIMELINE = "timeline";
	public static String MENTIONTIMELINE = "mention";
	public static String SEARCH = "search";
	public static String STREAMING = "streaming";
	public static String SAMPLE = "sample";
	public static String TRENDS = "trends";
	public static String INGEST = "ingest";
	public static String FRIENDS = "friends";
	public static String FOLLOWERS = "followers";
	
	// TODO: get from config 
	public static int TRENDS_WHEREID = 23424819;
	
	public static int SEARCH_NUMBEROFQUERIES = 15;
	
	public static Credential[] credentials;
	public static String filetoArchive;

	public static String sinceDate;
	public static String untilDate;
	public static long since_id;
	public static long max_id;
	public static boolean archiveTrends = false;
	
	public static int writerMaxLines;
	public static String currentFolder;
	public static String doneFolder;
	public static String errorFolder;
	
	public static String proxyHost;
	public static String proxyPort;
	public static int fileCheckInterval;
	public static String offsetfile;

	public static boolean mustLock = false;
	public static boolean movetoDone = false;
	
	public static String fileNameTimeFormat = "yyyy-MM-dd'T'HH_mm_ss'Z'"; // fixed will be send to writer
	
	
	
	//Adding for daffisation log files 
	public static String archiveMethod;
	public static String sourceOrigin; //ina ext etc.
	public static String collection;
	public static String applicationName = "twitter:";
	// zpehlivan.jsons and zpehlivan_info.json for the information related to jsons file for daffisation
	public static boolean keepInfoFileforJsons = false;


	public static ExecutorService executor = Executors.newFixedThreadPool(10);
	
	public static void  InstallCrawlerSettings(String path) throws FileNotFoundException, IOException 
	{
		
		Conf  tweetconf= new Conf();
		tweetconf.loadJson(new FileInputStream(path));
		
		
		//"source" 
		filetoArchive = tweetconf.get("source").get("file").asText();
		if(tweetconf.get("source").has("check_interval_second"))
			fileCheckInterval = tweetconf.get("source").get("check_interval_second").asInt();
		
		
		
		
		//"save" :
		currentFolder = tweetconf.get("save").get("current_folder").asText();
		doneFolder = tweetconf.get("save").get("done_folder").asText();
		errorFolder = tweetconf.get("save").get("error_folder").asText();
		
		

		boolean fileCreated = UtilsCrawler.checkandCreateFolder(SettingsCrawler.errorFolder);
		if(fileCreated) System.out.println(SettingsCrawler.errorFolder + " created or already exists"); 
		else System.out.println(SettingsCrawler.errorFolder + " NOT CREATED "); 
		
		
		fileCreated = UtilsCrawler.checkandCreateFolder(currentFolder);
		if(fileCreated)  System.out.println(SettingsCrawler.currentFolder + " created or already exists");
		else System.out.println(SettingsCrawler.currentFolder + " NOT CREATED "); 
		
		
		fileCreated = UtilsCrawler.checkandCreateFolder(doneFolder);
		if(fileCreated)  System.out.println(SettingsCrawler.doneFolder + " created or already exists");
		else System.out.println(SettingsCrawler.doneFolder + " NOT CREATED "); 
		
		
		
		writerMaxLines = tweetconf.get("save").get("max_record_by_file").asInt();
		movetoDone = !doneFolder.isEmpty(); // if done folder is not an empty string it means move to done
		
		
		offsetfile = tweetconf.get("save").get("offset_file").asText();
		
		
		//"crawlerinfo" :		
		sourceOrigin =tweetconf.get("crawler_info").get("source_origin").asText();
		collection =tweetconf.get("crawler_info").get("collection").asText();
		applicationName= tweetconf.get("crawler_info").get("application_name").asText();
		
		
		
		credentials = UtilsCrawler.getCredentials(tweetconf.get("twitter_credentials").asText(), tweetconf.get("twitter_credentials_name").asText()); // for twitter api keys
		
		

		
		 //"settings": 
		if(tweetconf.get("settings").has("keep_info_file_for_jsons"))
			keepInfoFileforJsons = tweetconf.get("settings").get("keep_info_file_for_jsons").asBoolean();
		
		proxyHost = tweetconf.get("settings").get("proxy_host").asText();
		proxyPort = tweetconf.get("settings").get("proxy_port").asText();
		
		if(tweetconf.get("settings").has("log4jproperties"))
				org.apache.log4j.PropertyConfigurator.configure(tweetconf.get("settings").get("log4jproperties").asText());

		
	}	
	

	
}
