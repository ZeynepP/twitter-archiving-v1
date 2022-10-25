package fr.ina.dlweb.twitter.crawler.main;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

import fr.ina.dlweb.twitter.commons.utils.Utils;
import fr.ina.dlweb.twitter.crawler.apis.ITwitterAPIs;
import fr.ina.dlweb.twitter.crawler.utils.SettingsCrawler;



@SuppressWarnings("deprecation")
public class TwitterCrawler {
	
	
	public static void main(String[] args) {

		Logger Log = LoggerFactory.getLogger(TwitterCrawler.class);
		
		Option help = new Option( "h", "help", false, "prints the help content" );
		Option config = new Option( "config", "configuration", true, "path to configuration file" );
		Option typeop = new Option( "type", "typeofcrawl", true, "streaming : twitter Streaming API with search for new added keys\n search: twitter Search API \n trends: to archive trends en France \n sample : twitter Sample API \n timeline: to get user timelines \n ingest: to get by ids tweets in ingest file" );
		
		Option since = new Option( "since", "sincedate", true, "Used for search to be able to search the tweets since date YYYY-MM-dd" );
		Option until = new Option( "until", "untildate", true, "Used for search to be able to search the tweets until date YYYY-MM-dd" );
		since.setRequired(false);
		until.setRequired(false);
		
		Option sinceid = new Option( "since_id", "sinceid", true, "Used for search to be able to search the tweets since id,Returns results with an ID greater than (that is, more recent than) the specified ID." );
		Option maxid = new Option( "max_id", "maxid", true, "Used for search to be able to search the tweets max id. Returns results with an ID less than (that is, older than) or equal to the specified ID." );
		sinceid.setRequired(false);
		maxid.setRequired(false);
		
		Option archiveTrends = new Option( "archiveTrends", "archiveTrends", true, "Used if we would like to archive hashtags in trends at the same time than trends archiving" );
		archiveTrends.setRequired(false);

		Options options = new Options();
	    options.addOption(help);
	    options.addOption(config);	    
	    options.addOption(typeop);
	    options.addOption(since);
	    options.addOption(until);
	    options.addOption(sinceid);
	    options.addOption(maxid);
	    options.addOption(archiveTrends);

	    CommandLineParser parser =new GnuParser();
	    HelpFormatter formatter = new HelpFormatter();
	    CommandLine cmd;
	    
	    
		try {
			
			
		   cmd = parser.parse( options, args);
		   if(cmd.hasOption("help")) { 
		        // automatically generate the help statement
			    
		        formatter.printHelp( "Twitter Crawler", options );
		        return;
		    }
		   
		    String configfile  =(String) cmd.getParsedOptionValue("config");
			SettingsCrawler.archiveMethod = ((String) cmd.getParsedOptionValue("type")).toLowerCase().trim();
			
			SettingsCrawler.sinceDate = (String) cmd.getParsedOptionValue("since");
			SettingsCrawler.untilDate = (String) cmd.getParsedOptionValue("until");
			
			if(cmd.getParsedOptionValue("since_id") != null)
				SettingsCrawler.since_id = Long.valueOf((String) cmd.getParsedOptionValue("since_id"));
			if(cmd.getParsedOptionValue("max_id") != null)
				SettingsCrawler.max_id = Long.valueOf((String)cmd.getParsedOptionValue("max_id"));
			
			if(cmd.getParsedOptionValue("archiveTrends") != null)
				SettingsCrawler.archiveTrends = Boolean.valueOf(cmd.getParsedOptionValue("archiveTrends").toString());
			
			SettingsCrawler.InstallCrawlerSettings(configfile);

			TwitterAPIConnector tc = new TwitterAPIConnector();
			boolean isOk = tc.run();
			if(!isOk)
				tc.shutDown();
			
		}
		catch(Exception ex)
		{
		
			Log.error(Utils.convertToJsonMessage("TwitterCrawler" , Utils.createJsonLogMessage( "error", "error", "TwitterCrawler : main : " + Throwables.getStackTraceAsString(ex) ,1)));
    		
			formatter.printHelp( "Twitter Crawler", options );
	        return;
		}
		   

	}

	


	

}
