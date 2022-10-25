package fr.ina.dlweb.twitter.indexer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ina.dlweb.twitter.commons.es.ESIndexClient;
import fr.ina.dlweb.twitter.indexer.infofilesgenerator.InfoFilesGenerator;
import fr.ina.dlweb.twitter.indexer.trends.TrendsIndexing;
import fr.ina.dlweb.twitter.indexer.tweets.TweetsIndexing;
import fr.ina.dlweb.twitter.indexer.utils.Settings;

public class TwitterIndexer {
	static Logger Log = LoggerFactory.getLogger(TwitterIndexer.class);
	
	
	public static void main(String[] args) {

		Option help = new Option( "h", "help", false, "prints the help content" );
		Option config = new Option( "config", "config", true, "path to configuration file" );
		Option multi = new Option( "multi", "multi", true, "number of threads used for indexing" );
		Option typeop = new Option( "type", "type", true, "index : only indexing \n infofiles : create/check info.json files for all \n restore : to restore \\n snapshot to snapshot index  " );
		Option application = new Option( "app", "app", true, "trends :to index trends \n tweets: to index tweets " );
		Option upsert = new Option( "upsert", "upsert", true, "true if upsert false if insert" );
		Option snapshot = new Option( "snapshot_name", "snapshot_name", true, "snapshot name it is required for restore option" );
		
		Options options = new Options();
	    options.addOption(help);
	    options.addOption(config);
	    options.addOption(multi);	    
	    options.addOption(typeop);
	    options.addOption(application);
	    options.addOption(upsert);
	    options.addOption(snapshot);

	    CommandLineParser parser =new GnuParser();
	    HelpFormatter formatter = new HelpFormatter();
	    CommandLine cmd;
	    
	    
	    
		try {
		   cmd = parser.parse( options, args);
		   if(cmd.hasOption("help")) {
		        // automatically generate the help statement
			    
		        formatter.printHelp( "Twitter Indexer", options );
		        return;
		    }
		   
		   
			String configfile  = cmd.getParsedOptionValue("config").toString();
			
			String app = (String) cmd.getParsedOptionValue("app");

			if(cmd.hasOption("upsert"))
				Settings.upsert = Boolean.valueOf((String)  cmd.getParsedOptionValue("upsert"));
			
			
			
			int multithread = 1;
			if(cmd.hasOption("multi"))
				multithread = Integer.parseInt((String)cmd.getParsedOptionValue("multi"));
			
			String snapshotName = null;
			String type = cmd.getParsedOptionValue("type").toString();
			
			if(cmd.hasOption("snapshot_name"))
				snapshotName = cmd.getParsedOptionValue("snapshot_name").toString();
			
			
			if(type.equals("index"))
			{
				Settings.InstallSettingsforIndexing(configfile);
				indexing(multithread,app);
				
			}
			else if (type.equals("infofiles") )// To create log files for existing crawls
			{
				Settings.InstallSettingsforInfoFileGenerator(configfile);
				InfoFilesGenerator  generator = new InfoFilesGenerator();
				generator.runLogger(multithread);
			}
			else if(type.equals("restore"))
			{

				Settings.InstallSettingsforRestoreSnapshot(configfile);
				int status = restore(snapshotName);
				if(status == 200)
					Log.info("Index   " + Settings.indexName + "  "  + " restored to " + Settings.restoreIndexName + " from snapshot " + snapshotName);
				else
					Log.error("Index   " + Settings.indexName + "  "  + "CANNOT BE restored to " + Settings.restoreIndexName + " from snapshot " + snapshotName);
				System.out.println(" Snapshot restored : " + snapshotName);

			}
			else if(type.equals("snapshot"))
			{
				Settings.InstallSettingsforRestoreSnapshot(configfile);
			    int status = snapshot(snapshotName);
			    if(status == 200)
			    	Log.info(" Snapshot created : " + snapshotName);
			    else
			    	Log.error(" Snapshot NOT created : " + snapshotName);
			}
			
		   
		   
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			formatter.printHelp( "Twitter Indexer", options );
			Log.error(e1.toString());
			e1.printStackTrace();
	        return;
		}

	}
	
	
	private static int restore(String snapshot)
	{
		int status = -1;
		try
		{
		
			if(snapshot != null)
			{
				Log.info("Start restore for snapshot " + snapshot);
				ESIndexClient es = new ESIndexClient(Settings.targetEsHosts);
				status = es.restore( Settings.RESTORE_JSON, Settings.restoreRepositoryName, Settings.indexName, Settings.restoreIndexName, snapshot);
				
					
				 
			}
			else 
				Log.error("Restore can not start for " + snapshot);
		
		}catch(Exception ex)
		{
			Log.error("Restore can not start for " + snapshot);
			Log.error(ex.getMessage());
			
		}
		return status;
		
	}
	
	private static int snapshot(String snapshotName) 
	{
		
		int status = -1;
		try
		{
		
			ESIndexClient esSource = new ESIndexClient(Settings.esHosts);
			boolean isSourceRepoCreated = esSource.prepareRepository(Settings.REPO_JSON, Settings.restoreRepositoryName);
			
			
			ESIndexClient esTarget = new ESIndexClient(Settings.targetEsHosts);
			boolean isTargetRepoCreated = esTarget.prepareRepository(Settings.REPO_JSON, Settings.restoreRepositoryName);
			
			if(isSourceRepoCreated) 
				Log.info("Source repository   " + Settings.restoreRepositoryName + " created");
			else {
				Log.error("ERROR  : Source repository  can not be created " + Settings.restoreRepositoryName + " created");
				System.exit(1);
			}
			
			
			if(isTargetRepoCreated) 
				Log.info("Target repository   " + Settings.restoreRepositoryName + " created");
			else {
				Log.error("ERROR  : Target repository  can not be created " + Settings.restoreRepositoryName + " created");
				System.exit(1);
			}
			
			if(isSourceRepoCreated && isTargetRepoCreated)
				status = esSource.snapshot(Settings.SNAP_JSON, Settings.restoreRepositoryName, Settings.indexName,snapshotName);
			
			
			Log.info("Snapshot ES status   " + String.valueOf(status));
			
		
		}catch(Exception ex) {
			
				Log.error("Snapshot could not be created !!!  ");
				Log.error(ex.getMessage());
				
			}
		
		return status;
			
	
	}


	private static void indexing( int multithread, String app)
	{
			TweetsIndexing tweetsindex;
			TrendsIndexing trendsindex;
			
		
			if(app.equals("tweets"))
			{
				tweetsindex = new TweetsIndexing(Settings.esHosts,Settings.indexName, Settings.indexSchema, Settings.indexType, Settings.indexSettings);
				tweetsindex.run(multithread);
			}
			else
			{
				trendsindex = new TrendsIndexing(Settings.esHosts,Settings.indexName, Settings.indexSchema, Settings.indexType, Settings.indexSettings);
				trendsindex.run(multithread);
			}
			
		}
	
	
	}



/*
 * 
 * Estimùation
else if(type == 1)
{	try{
	startTime = System.currentTimeMillis();
	Settings.windowsize = Integer.parseInt(args[3]);
	EstimatingSum limits  = new EstimatingSum(args[2]);
	limits.startEstimation();
	Settings.Log.info("Estimation took : " + (System.currentTimeMillis() - startTime));
//	limits.listIds();
	//SearchIndex.search();
}*/
