package fr.ina.dlweb.twitter.commons.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.joda.time.format.ISODateTimeFormat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.ina.dlweb.utils.IOUtils;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.Configuration;

public class TwitterSourceFileUtils {

	// Thread safe
	static ObjectMapper jsonMapper = new ObjectMapper(); 
	
	public static void writetoFile(List<ObjectNode> jsonRecords, File jsonTarget) throws IOException
	{
		
		Collections.sort(jsonRecords, new Comparator<ObjectNode>() {

			@Override
			public int compare(ObjectNode o1, ObjectNode o2) {
				// sort by type then key
				int comp = o1.get("type").textValue().compareTo(o2.get("type").textValue());
				if(0 == comp) {
					comp = o1.get("key").textValue().compareTo(o2.get("key").textValue());
				}
				return comp;
			}
			 
		});
		
		
		BufferedWriter targetWriter = null;
		try {
			targetWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(jsonTarget), "UTF-8"));
			for(ObjectNode jsonRecord : jsonRecords) {
				String ser_record = jsonMapper.writeValueAsString(jsonRecord);
				targetWriter.write(ser_record);
				targetWriter.write("\n");
			}
		} finally {
			IOUtils.closeQuietly(targetWriter);			
		}
		
	}
	
	public static void convertCsvToJsons(File csvSource, String csvCharset, File jsonTarget) throws IOException {
		
		Reader csvReader = null;
		List<ObjectNode> jsonRecords = new ArrayList<ObjectNode>();
		
		try {
			String now_date_str = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC().print(System.currentTimeMillis());
			
			csvReader = new InputStreamReader(new FileInputStream(csvSource), csvCharset);
			
			
			CSVFormat csvFormat = CSVFormat.EXCEL.withDelimiter(';').withQuote('"').withHeader("type", "key", "description");
			CSVParser csvParser = csvFormat.parse(csvReader);
			
			List<CSVRecord> csvRecords =  csvParser.getRecords();
			
			
			for(CSVRecord csvRecord : csvRecords) {
				ObjectNode jsonRecord = jsonMapper.createObjectNode();
				
				String type = csvRecord.get("type");
				
				jsonRecord.put("type", type);
				jsonRecord.put("active", 1);
				if("hashtag".equals(type)) {
					String hashtag = csvRecord.get("key");
					if(!hashtag.startsWith("#")) {
						hashtag = "#"+hashtag;
					}
					jsonRecord.put("key", hashtag);
				} else {
					jsonRecord.put("key", csvRecord.get("key"));
				}
				if("timeline".equals(type)) {
					jsonRecord.put("id", (Long)null);
				}
				jsonRecord.put("description", csvRecord.get("description"));
				
				ObjectNode historyItem = jsonRecord.putArray("history").addObject();
				historyItem.put("note", "");
				historyItem.put("date", now_date_str);
				historyItem.put("action", "added");
				
				jsonRecords.add(jsonRecord);
			}
			csvParser.close();
		} finally {
			IOUtils.closeQuietly(csvReader);
		}
		
		writetoFile(jsonRecords, jsonTarget );
	}
	
	
	public static Set<Long> extractUserIds(File jsonSource) throws IOException {
		
		BufferedReader reader = null;
		
		Set<Long> ids = new HashSet<Long>();
		
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(jsonSource), "UTF-8"));
			String line = reader.readLine();
			
			while(null != line) {
				if(!line.isEmpty()) {
					ObjectNode jsonRecord = (ObjectNode)jsonMapper.readTree(line);			
					if("timeline".equals(jsonRecord.get("type").textValue())) {
						String screenname = jsonRecord.get("key").textValue(); 
						 if(!jsonRecord.hasNonNull("id")) {
							 throw new RuntimeException("No id found for screename: "+screenname);
						 }
						 long id = jsonRecord.get("id").longValue();
						 ids.add(id);
						
					}
	
	 			}
				line = reader.readLine();
			}
		} finally {
			IOUtils.closeQuietly(reader);	
		}
		
		return ids;
	}
	
	
	public static Set<String> extractHashtags(File jsonSource) throws IOException {
		
		BufferedReader reader = null;
		
		Set<String> hashtags = new HashSet<String>();
		
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(jsonSource), "UTF-8"));
			String line = reader.readLine();
			
			while(null != line) {
				if(!line.isEmpty()) {
					ObjectNode jsonRecord = (ObjectNode)jsonMapper.readTree(line);			
					if("hashtag".equals(jsonRecord.get("type").textValue())) {
						 String hashtag = jsonRecord.get("key").textValue();
						 hashtags.add(hashtag);
						
					}
	
	 			}
				line = reader.readLine();
		}
		} finally {
			IOUtils.closeQuietly(reader);	
		}
		
		return hashtags;
	}
	
	
	public static void enrichWithUserId(File jsonSource, Configuration twitter4jConf) throws IOException, TwitterException {
		
		BufferedReader reader = null;
		
		List<ObjectNode> jsonRecords = new ArrayList<ObjectNode>();
		List<String> screennames_without_id = new ArrayList<String>();
		
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(jsonSource), "UTF-8"));
			String line = reader.readLine();
			
			while(null != line) {
				if(!line.isEmpty()) {
					ObjectNode jsonRecord = (ObjectNode)jsonMapper.readTree(line);
					jsonRecords.add(jsonRecord);				
					if("timeline".equals(jsonRecord.get("type").textValue()) && !jsonRecord.hasNonNull("id")) {
						screennames_without_id.add(jsonRecord.get("key").textValue());
					}
	 			}
				line = reader.readLine();
			}
		} finally {
			reader.close();	
		}
		
		
		Map<String, Long> user_ids = collectUserIdsFromScreenname(screennames_without_id, twitter4jConf);
		
		
		for(ObjectNode jsonRecord : jsonRecords) {
			if("timeline".equals(jsonRecord.get("type").textValue()) && !jsonRecord.hasNonNull("id")) {
				
				String screenname = jsonRecord.get("key").textValue();  
				Long id = user_ids.get(screenname.toLowerCase());
				if(null == id) {
					throw new RuntimeException("No id found for screename: "+screenname);
				}
				jsonRecord.put("id", id);
			}
		}
		
		BufferedWriter writer = null;
		
		try {
		writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(jsonSource), "UTF-8"));
		for(ObjectNode jsonRecord : jsonRecords) {
			String ser_record = jsonMapper.writeValueAsString(jsonRecord);
			writer.write(ser_record);
			writer.write("\n");
		}
		} finally {
			IOUtils.closeQuietly(writer);
		}
		
	}
	
	public static Long getUserIdForScreenname(String screenname, Configuration twitter4jConf) throws TwitterException {
		
		TwitterFactory twitter4jFact = new TwitterFactory(twitter4jConf);
		Twitter twitter4j = twitter4jFact.getInstance();
		twitter4j.getOAuth2Token();
		
		ResponseList<User> users = 	twitter4j.users().lookupUsers(new String[] {screenname});
		if(users != null)
			return users.get(0).getId();
		else return 0L;
	}
	
	public static Map<String, Long> collectUserIdsFromScreenname(Iterable<String> screennames, Configuration twitter4jConf) throws IOException, TwitterException {
		
		Map<String, Long> screennameToIds = new HashMap<String, Long>();
		

		TwitterFactory twitter4jFact = new TwitterFactory(twitter4jConf);
		Twitter twitter4j = twitter4jFact.getInstance();
		twitter4j.getOAuth2Token();
	
		
		List<List<String>> batches = new ArrayList<List<String>>();
		ArrayList<String> currentBatch = new ArrayList<String>();
		batches.add(currentBatch);
		
		for(String screenname : screennames) {
			
			// max batch size for lookup
			if(currentBatch.size() == 100) {
				currentBatch = new ArrayList<String>();
				batches.add(currentBatch);
			} 	
			currentBatch.add(screenname);
		}
		
		for(List<String> batch : batches) {
			ResponseList<User> users = twitter4j.users().lookupUsers(batch.toArray(new String[0]));

			for(User user : users) {
				screennameToIds.put(user.getScreenName().toLowerCase(), user.getId());
			}
			
		}
		return screennameToIds;
	}
	

	
}
