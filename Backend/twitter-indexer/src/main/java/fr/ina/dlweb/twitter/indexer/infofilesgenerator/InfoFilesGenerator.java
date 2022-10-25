package fr.ina.dlweb.twitter.indexer.infofilesgenerator;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Throwables;

import fr.ina.dlweb.io.FileLineWriter;
import fr.ina.dlweb.twitter.commons.io.TwitterWriter;
import fr.ina.dlweb.twitter.commons.utils.Utils;
import fr.ina.dlweb.twitter.indexer.utils.Settings;
import fr.ina.dlweb.utils.FileUtils;


public class InfoFilesGenerator {
	
	//TODO : working only local not remote write files add it later
	Logger Log = LoggerFactory.getLogger(InfoFilesGenerator.class);
	ObjectMapper objectMapper = new ObjectMapper();
	Map<String, ObjectNode> maps ;
	
	public  boolean createInfoFile(String filePath)
	{
		
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		
		
		
	
		
		
		
		
		try {
			
			maps =  objectMapper.readValue(Settings.mapping, Map.class);
			   
			  
			String fileTweetsPath = filePath.replace("file:", "");
			String fileInfoPath = fileTweetsPath.replace(".jsons", "_info.json").replace("file:", "");
			// it is may be not in offset file for the files recently crawled so we check again if it exists
			File info = new File(fileInfoPath);
			
			//  size = 0 can happen
			
			if(!info.exists() || (info.exists() && info.length()==0) || Settings.fromScratch)
			{
				ObjectNode infoNode = getInfoFileObject( fileTweetsPath, fileTweetsPath);
				
				if(infoNode != null) {
					FileLineWriter infoWriter = new FileLineWriter(info,  StandardCharsets.UTF_8, false, false);
					infoWriter.writeLine(objectMapper.writeValueAsString(infoNode));
					System.out.println(fileInfoPath + " created");
					infoWriter.close();
					Settings.mapFileOffset.put(filePath, "1");
				}
				else 
					Settings.mapFileOffset.put(filePath, "0");
				
			}

			
		
				
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			Settings.mapFileOffset.put(filePath, "0");
			return false;
		} 
	
			
		return true;
		
	}
	
	//For one shot
	public void runLogger(int numberofthreads)
	{
		try {
			startMultiThreadInfoFileGenerator(numberofthreads);
			Utils.writeHashMap(Settings.offsetFile,false, Settings.mapFileOffset,true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
		
	}
	
	

	
	public ObjectNode getInfoFileObject(String fileFullPath, String fileTweetsPath) throws JsonProcessingException, IOException
	{
		
		ObjectNode infoFile = null ;
		Path tweetJson = Paths.get(fileTweetsPath);
		for (Map.Entry<String, ObjectNode> entry : maps.entrySet()) {
			
			
			if(fileFullPath.contains(entry.getKey())) {
				infoFile =  objectMapper.valueToTree(entry.getValue());
				break;
			}
			
		}
		
		if(infoFile != null) {
			
			 File fTweets = new File(fileTweetsPath);
			 infoFile.put("file_name", tweetJson.getFileName().toString().replace(".part", ""));
			 infoFile.put("file_state",  "closed");
			 infoFile.put("file_sha256",  FileUtils.getSha256(fTweets));
			 infoFile.put("file_length",  fTweets.length());
			
		}
		
		return infoFile;
		
		
		
	}
	
	public void startMultiThreadInfoFileGenerator(int numberofthreads) throws IOException, InterruptedException, ExecutionException
	{
		
		long startTime = System.currentTimeMillis();
	
		List<Callable< Boolean>> tasks = new ArrayList<Callable<Boolean>>();
		ExecutorService executor = Executors.newFixedThreadPool(numberofthreads);
		System.out.println("Running with " + String.valueOf(numberofthreads) +  " threads " );
		for(int i= 0;i< Settings.twitterData.length ; i++)
		{
//			final String source = Settings.sourceType[i];
//			final String method_archive = Settings.methodArchive[i];
//			final String collection = Settings.collectionNames[i];
			List<String> jsons;
			try {
				jsons = Utils.getJsonsList(Settings.twitterData[i]);
				for(final String file : jsons) {
					if(!file.contains("_backup") && ( !Settings.mapFileOffset.containsKey(file) || Long.valueOf(Settings.mapFileOffset.get(file)) != 1)) // info file does not exist
					{
						tasks.add(new Callable< Boolean>(){
							 public Boolean call() {
								 
								 return createInfoFile(file);
							 }
							 
						});
					}
		
				}
			} catch (Exception e) {
				Log.error( Utils.convertToJsonMessage(Settings.applicationName,  Utils.createJsonLogMessage("error", "error", "startMultiThreadIndexing  getJsonsList : " + Throwables.getStackTraceAsString(e), 1)));
			}
			
		}
		
		
		
		List<Future<Boolean>> list = executor.invokeAll(tasks);
		for (Future<Boolean> fut :list ) 
		{
			try{
				if(!fut.get()) System.out.println("--");
			}
			catch(Exception ex)
			{
				Log.error( Utils.convertToJsonMessage(Settings.applicationName,  Utils.createJsonLogMessage("error", "error", "startMultiThreadIndexing  fut.get  "+ ex.getMessage(), 1)));
				ex.printStackTrace();
			}
			
		}
		
		Log.info( Utils.convertToJsonMessage(Settings.applicationName,  Utils.createJsonLogMessage("info", "info", "OVER", 1)));
		//error to keep this info independent of log4j level
		Log.info( Utils.convertToJsonMessage(Settings.applicationName,  Utils.createJsonLogMessage("info", "info", "Indexing took : " + ( System.currentTimeMillis() - startTime), 1)));
		
		executor.shutdown(); 

	
	}

	private  String getArchivePropertiesFromPath(String path, String type)
	{
		// "/data/twitter/collections/lab02/dataext/test.json" dataext is type + source
		// "/data/twitter/collections/lab02/streaming/test.json"
		// lab02 = collectionname streaming = archivetype  
		
		
		if(type.equals("source"))
		{
			if(path.contains("dataext"))
				return "ext";
			else if(path.contains("toptrend"))
				return "trends";
				
			return "ina";
		}
		else if(type.equals("archive_method"))
		{
			Path p = Paths.get(path.replace("file:","").replace("https:", "").replace("http:", "").replace(":",""));
			String archiveMethod= p.getName(p.getNameCount() - 2).toString();
			if(archiveMethod.equals("timelines")) archiveMethod = "timeline";
			if(archiveMethod.equals("mentions")) archiveMethod = "mention";
			if(!archiveMethod.equals("trends") && !archiveMethod.equals("ingest") && !archiveMethod.equals("timeline") && !archiveMethod.equals("streaming") && !archiveMethod.equals("search"))
				archiveMethod = "ids";
			return archiveMethod;
			
		}
		else if(type.equals("collection"))
		{
			if(path.contains("trends")) return "trends";
			
			Path p = Paths.get(path.replace("file:","").replace("https:", "").replace("http:", "").replace(":",""));// : for port 
			String colName = p.getName(p.getNameCount() - 3).toString();

			if( colName.equals("fusillade") || colName.contains("nice") ) return "attentats";
			
			return colName;
			
		}
		else return null;

		
		
	}
}


/*	
 * In main
 * 
 * else if(Settings.updateFields.length > 0) 
				{
					
					infoNode = updateFields(fileInfoPath, fileTweetsPath);
					BufferedWriter logWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileInfoPath, false),  StandardCharsets.UTF_8));
					logWriter.write(objectMapper.writeValueAsString(infoNode));//writerWithDefaultPrettyPrinter().
					logWriter.close();
					
				}*/
/*  public ObjectNode updateFields(String fileInfoPath, String fileFullPath) throws Exception

{
	
	InputStream inputStream = Utils.getInputStream(fileInfoPath, 0);
	String s= CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8));
	ObjectNode node = (ObjectNode)objectMapper.readTree(s);
	
	for (String field : Settings.updateFields)
	{
		node.remove("filde_state");
		node.remove("");
		if(field.equals("file_state") && node.get("file_state").asText().equals("active"))
		{
			System.out.println(fileInfoPath + " active");
			// close open file
			 File fTweets = new File(fileFullPath);
			 node.put("file_state",  "closed");
			 node.put("file_sha256",  FileUtils.getSha256(fTweets));
			 node.put("file_length",  fTweets.length());
		}
		else if(!field.equals("file_state"))
		{
			node.put(field, getArchivePropertiesFromPath(fileFullPath, field));
		}
		
	}
	inputStream.close();
	return node;
}*/