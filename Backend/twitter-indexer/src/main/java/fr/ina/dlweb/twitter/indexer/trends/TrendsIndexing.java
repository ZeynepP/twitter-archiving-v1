
package fr.ina.dlweb.twitter.indexer.trends;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.ina.dlweb.twitter.commons.io.CommonsFileReader;
import fr.ina.dlweb.twitter.commons.utils.Utils;
import fr.ina.dlweb.twitter.indexer.AbstractIndexing;
import fr.ina.dlweb.twitter.indexer.utils.Settings;
import fr.ina.dlweb.twitter.indexer.utils.UtilsIndexer;
import io.searchbox.action.BulkableAction;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;


public class TrendsIndexing extends AbstractIndexing{

	// TODO THIS WAS A TEST CODE; DO NOT USE IT ON PROD

	public TrendsIndexing(String[] es_host, String indexname, String indexschema, String indextype,	String indexsettingsfile) {
		super(es_host, indexname, indexschema, indextype, indexsettingsfile);

	}



	Logger Log = LoggerFactory.getLogger(TrendsIndexing.class);

	

	public boolean indexOneFile(String file)
	{
		int total =0;
		boolean isOk = false;
		if(file.contains("trends_"))
		{
				
				List<BulkableAction<DocumentResult>> docBuffer = new ArrayList<BulkableAction<DocumentResult>>();
				String sourceType= null;String methodArchive = null; String collection= null;
				Log.info( Utils.convertToJsonMessage(Settings.applicationName,  Utils.createJsonLogMessage("info", "info", "Starting for file " + file, 1)));
			    
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
				
				
				
				// Getting offset from map if not in map add it with offset 0 
				long offset = (Settings.mapFileOffset.containsKey(file) ? Long.valueOf(Settings.mapFileOffset.get(file)) : 0l);
				Settings.mapFileOffset.put(file,String.valueOf( offset));// for files that has no content etc adding here updated later if needed
				
				
				if(offset == 0)
					Log.info( Utils.convertToJsonMessage(Settings.applicationName,  Utils.createJsonLogMessage("info", "info", file + " not found on the offset map, offset will be 0", 1)));
		
		
				
				CommonsFileReader jsonIterator = null;
				try {
					jsonIterator = new CommonsFileReader(file,offset);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				long indexed_at = new Date().getTime();
				
				
				while(jsonIterator!=null && jsonIterator.hasNext()) {
					
					
					String json = jsonIterator.next();
				
					String t= json.substring(json.length() - 1);
					if(t.equals("}"))	json += "]";
					JsonNode onetweet ;
					JsonNode jsonObject;
					List<Index> indexes = new ArrayList<Index>(2) ;
					try{
						onetweet = objectMapper.readTree(json);
						jsonObject= objectMapper.readValue(onetweet.get(2).toString(),JsonNode.class);	
						
						indexes = getIndexBuilderforJson(jsonObject.get(0), objectMapper,indexes,Long.parseLong(onetweet.get(0).toString()),sourceType,methodArchive,file,indexed_at);
						docBuffer.addAll(indexes);
						
						
						if(docBuffer.size() > Settings.maxRecordCount)
						{
							isOk = indexTweets(docBuffer);
							
							if(isOk)
							{
								total+=docBuffer.size();
								offset = jsonIterator.getOffsetJsonfile();
								docBuffer.clear();
							}
							else
							{
								Log.error( Utils.convertToJsonMessage(Settings.applicationName,  Utils.createJsonLogMessage("error", "error", "BufferToES problem with ES !!! for file " + file , 1)));
							}
								
						}
					}
					catch(Exception ex)
					{
						//errors -4
						Log.debug( Utils.convertToJsonMessage(Settings.applicationName,  Utils.createJsonLogMessage("error", "error", "Error while adding docs to buffer for file  "+ file +  " " + ex.getMessage() , 1)));
						//break;
					}
					
				}
				
				if(docBuffer.size() > 0)
				{
					isOk = indexTweets(docBuffer);
					
					if(isOk)
					{
						total+=docBuffer.size();
						offset = jsonIterator.getOffsetJsonfile();
						docBuffer.clear();
					}
					else
					{
						Log.error( Utils.convertToJsonMessage(Settings.applicationName,  Utils.createJsonLogMessage("error", "error", "BufferToES problem with ES !!! for file " + file , 1)));
					}
						
				}
				Settings.mapFileOffset.put(file, String.valueOf( offset));
				 //Updating logs to tell the indexer where to start next time
				
				
				Log.info( Utils.convertToJsonMessage(Settings.applicationName,  Utils.createJsonLogMessage("info", "info", "Over for file  "+ file +  " "  , total)));
		}
		return true;
		//TODO : work cases to return false

		
	}
	
	
	private List<Index> getIndexBuilderforJson(JsonNode jObject,ObjectMapper objectMapper,List<Index> indexes, long archived_at, String sourceType, String methodType, String sourcePath, long indexed_at)
    {
		String oneLinedJson = null;
 
		try {
			TrendsMeta bean;
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			String as_of = jObject.get("as_of").textValue();
			JsonNode trends = jObject.get("trends");
			long id = df.parse(as_of).getTime();
			int counter = 1;
			for (JsonNode objNode : trends) {
			    
				((ObjectNode)objNode).put("archived_at",archived_at);
				((ObjectNode)objNode).put("indexed_at",indexed_at);
				((ObjectNode)objNode).put("source_path",sourcePath);
				((ObjectNode)objNode).put("as_of",as_of);
				((ObjectNode)objNode).put("rank",counter);
				((ObjectNode)objNode).put("date_trend",id);
				oneLinedJson = objectMapper.writeValueAsString(objNode);

				bean = objectMapper.readValue(oneLinedJson, TrendsMeta.class);
				indexes.add(new Index.Builder(objectMapper.writeValueAsString(bean)).id(String.valueOf(id)+"_"+counter).build());
				counter++;
			}

		} catch(Exception ex)
		{
			
			Log.error( Utils.convertToJsonMessage(Settings.applicationName,  Utils.createJsonLogMessage("error", "error","Json parser error in getIndexBuilderforJson " + ex + jObject.asText(), 1)));
			
		}
		
		
		return indexes;

    }
}
	

	
