package fr.ina.dlweb.twitter.indexer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

import fr.ina.dlweb.twitter.commons.es.ESJestClient;
import fr.ina.dlweb.twitter.commons.utils.Utils;
import fr.ina.dlweb.twitter.indexer.utils.Settings;
import io.searchbox.action.BulkableAction;
import io.searchbox.core.BulkResult;
import io.searchbox.core.DocumentResult;

public abstract class AbstractIndexing {
	
	ESJestClient es ;
	Logger Log = LoggerFactory.getLogger(AbstractIndexing.class);
	//SimpleDateFormat is not thread safe
	//SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm a z");

	
	public abstract boolean indexOneFile(String file);

	
	public AbstractIndexing(String[] es_host, String indexname, String indexschema, String indextype,  String indexsettingsfile)
	{
		try {
			es = new ESJestClient(es_host, indexname, indexschema, indextype, indexsettingsfile,true);
			Runtime.getRuntime().addShutdownHook(new Thread() {
	    	    public void run() {
	    	    	shutDown();
	    	   	        
	    	    }
	    	});
			
		} catch (Exception e) {
			Log.error( Utils.convertToJsonMessage(Settings.applicationName,  Utils.createJsonLogMessage("fatal", "error", "EXITTT !!!!! "  + Throwables.getStackTraceAsString(e), 1)));
			System.exit(1); // If we can not get an index, we can not archive
		}
		
	}

	public  ESJestClient getES()
	{
		return es;
		
	}
	
	

	
	
	
	/**
	 * Reads json file starting from an offset, creates jsonobjects for each line of a file, updates and mapes them to TweetMeta before adding to buffer
	 * @param maxBatchDocCount 
	 * 
	 */
	public void run(int numberofthreads)
	{
		try {
			Utils.writeHashMap(Settings.offsetFile + ".bck",true, Settings.mapFileOffset,true);
		}
		catch (FileNotFoundException e1) {
			Log.warn( Utils.convertToJsonMessage(Settings.applicationName,  Utils.createJsonLogMessage("warn", "warn", "writeHashMap to  " + Settings.offsetFile +  Throwables.getStackTraceAsString(e1), 1)));
		}

		startMultiThreadIndexing(numberofthreads);
		

		
	}
	public void startMultiThreadIndexing(int numberofthreads) 
	{
		
		long startTime = System.currentTimeMillis();
	
		List<Callable< Boolean>> tasks = new ArrayList<Callable<Boolean>>();
		ExecutorService executor = Executors.newFixedThreadPool(numberofthreads);
		System.out.println("Running with " + String.valueOf(numberofthreads) +  " threads " );
		
		
		
		
		for(int i= 0;i< Settings.twitterData.length ; i++)
		{
			List<String> jsons;
			try {
				jsons = Utils.getJsonsList(Settings.twitterData[i]);
				for(final String file : jsons) {
				    
					
					
						tasks.add(new Callable< Boolean>(){
							 public Boolean call() {
								 return indexOneFile(file);
							 }
							 
						});
					
				}
			} catch (Exception e1) {
				Log.error( Utils.convertToJsonMessage(Settings.applicationName,  Utils.createJsonLogMessage("error", "error", "startMultiThreadIndexing  getJsonsList  "+ Settings.twitterData[i] + " : " +e1.getMessage(), 1)));
			}
			
		}
		
		
		
		List<Future<Boolean>> list = null;
		try {
			list = executor.invokeAll(tasks);
		} catch (InterruptedException e) {
			Log.error( Utils.convertToJsonMessage(Settings.applicationName,  Utils.createJsonLogMessage("error", "error", "startMultiThreadIndexing  invokeAll  "+ Throwables.getStackTraceAsString(e), 1)));
		}
		
		
		
		for (Future<Boolean> fut : list ) 
		{
			try { 
				if(!fut.get()) System.out.println("--"); 
			} catch (Exception e) {}
		}
		
		//error to keep this info independent of log4j level
		Log.error( Utils.convertToJsonMessage(Settings.applicationName,  Utils.createJsonLogMessage("info", "info", "Indexing took (ms) : " + ( System.currentTimeMillis() - startTime), 1)));
		System.out.println("OVER Indexing took (ms) : " + ( System.currentTimeMillis() - startTime));
		
		executor.shutdown(); 
		
		es.shutDownESClient();
		
		
	
	}
	
	
	private int logBulkResults(BulkResult result)
	{
		int globalStatus = result.getResponseCode();
		List<Integer> failedStatus = new ArrayList<Integer>();
		List<String> failedErrors = new ArrayList<String>();

		if(result.getFailedItems().size() > 0)
		{
			for(int i=0;i<result.getFailedItems().size();i++)
			{
				failedStatus.add( result.getFailedItems().get(i).status);
				failedErrors.add( result.getFailedItems().get(i).error);
		
			}
		}
		
		for (String error : failedErrors) {
			Log.error( Utils.convertToJsonMessage(Settings.applicationName,  Utils.createJsonLogMessage("error", "stats", error, Collections.frequency(failedErrors, error))));	
		}
		
		for (int status : failedStatus) {
			Log.error( Utils.convertToJsonMessage(Settings.applicationName,  Utils.createJsonLogMessage("error", "stats", Integer.toString(status) , Collections.frequency(failedStatus, status))));
		}

		return globalStatus;

		
	}
	protected boolean indexTweets(List<BulkableAction<DocumentResult>> docBuffer)
	{
			try {
				BulkResult result = getES().bufferToEs(docBuffer);
				int status = logBulkResults(result);
				
				if( status==200 || status==409 ) 
				{
					System.out.print(".");
					return true;
				}
				
				
			} catch (IOException e) {
				Log.error( Utils.convertToJsonMessage(Settings.applicationName,  Utils.createJsonLogMessage("error", "error", "indexTweets :" + Throwables.getStackTraceAsString(e), 1)));
				System.out.print("!");
				return false;
			}
			System.out.print("-");
			return false;
		
	}

	protected void shutDown() {
		
		Log.warn( Utils.convertToJsonMessage(Settings.applicationName,  Utils.createJsonLogMessage("warn", "warn", "Shutdown called", 1)));
	
		
		try {
			Utils.writeHashMap(Settings.offsetFile,false, Settings.mapFileOffset,true);
			if(Settings.unshortenUrls)
				Utils.writeHashMap(Settings.unshortenUrlsFileOutput ,true, Settings.urls, false);
		} catch (FileNotFoundException e1) {
			Log.warn( Utils.convertToJsonMessage(Settings.applicationName,  Utils.createJsonLogMessage("warn", "warn", "writeHashMap to  " + Settings.offsetFile + ".bck or " + Settings.unshortenUrls + " "  + e1.getMessage(), 1)));
		}
		
		es.shutDownESClient();
	}
	
	
}
