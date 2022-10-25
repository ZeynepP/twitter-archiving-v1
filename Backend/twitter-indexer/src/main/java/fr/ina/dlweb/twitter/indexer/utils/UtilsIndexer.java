package fr.ina.dlweb.twitter.indexer.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.io.Closeables;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiTrie;

import fr.ina.dlweb.utils.URLUtils;

public class UtilsIndexer {


	static Logger Log = LoggerFactory.getLogger(UtilsIndexer.class);	
	//better to open connection for each url but not very performant so we will list the urls 
		
	//	
	public  static String expand(String addr) {
			
		
			String host = URLUtils.getHost(addr);
			String expandedURL = null;
			if(Arrays.binarySearch(Settings.urlshorteners, host) >= -1)
			{
				//Check cache
				String inCache = (String) Settings.urls.get(addr);
				if(inCache != null) {
					return inCache;
				}
	
				
				//Connect & check for the location field
				HttpURLConnection connection = null;
				try {
						URL address = new URL(addr);
						
						connection = (HttpURLConnection) address.openConnection(Settings.proxy);
						connection.setConnectTimeout(1000);
						connection.setInstanceFollowRedirects(false);// need to do recursive because there is ow.li => bit.ly etc
						connection.setReadTimeout(1000);
						connection.connect();
						
	
						expandedURL = connection.getHeaderField("Location");	
						addr = expand(expandedURL); // need to do recursive because there is ow.li => bit.ly etc

				} catch (Throwable e) {
					Log.debug("Problem while expanding {}", addr, e);
				} finally {
					try {
						if(connection != null) {
							Closeables.closeQuietly(connection.getInputStream());
						}
					} catch (IOException e) {
						Log.debug("Unable to close connection stream", addr);
					}
				}
			}
			if(expandedURL != null) 
				Settings.urls.put(addr, expandedURL);
			return addr;
		}
		
	


	public static ArrayNode getCoordinatesFromPlace(JsonNode bounding)
	{
		Double[] xco = new Double[4];
		Double[] yco = new Double[4];
		//{http://stackoverflow.com/questions/1203135/what-is-the-fastest-way-to-find-the-center-of-an-irregularly-shaped-polygon
		// x = min_x + (max_x - min_x)/2,
		// 	    y = min_y + (max_y - min_y)/2
		// 	}
		for (final JsonNode objNode : bounding) { // array of 
			int counter = 0;
			for (final JsonNode coo : objNode) { // array of 
				xco[counter]= coo.get(0).asDouble();
				yco[counter]= coo.get(1).asDouble();
				counter++;
			}
		}

		ArrayNode coord = JsonNodeFactory.instance.arrayNode(); // ((ObjectNode)coordinates).putArray("coordinates");
		double xmin = Collections.min(Arrays.asList(xco)); 
		double xmax = Collections.max(Arrays.asList(xco)); 
		double ymin = Collections.min(Arrays.asList(yco)); 
		double ymax = Collections.max(Arrays.asList(yco));
		
		coord.add(xmin + (xmax-xmin)/2);
		coord.add(ymin + (ymax-ymin)/2);
		
		return coord;
		
	}

	
	public static String stringJoin(String[] array, int count){
		    String joined = "";
		    for(int i = 0; i < count; i++)
		      joined += array[i];
		    return joined.substring(0,joined.length()-1); // 1f310- to remive last - 1f1eb-1f1f7-,
		  }
	
	
	public static String html2text(String html) {
	    return Jsoup.parse(html).text();
	}


	public static int getEmojiEndPos(char[] text, int startPos) {
	    int best = -1;
	    for (int j = startPos + 1; j <= text.length; j++) {
	      EmojiTrie.Matches status = EmojiManager.isEmoji(Arrays.copyOfRange(
	        text,
	        startPos,
	        j
	      ));

	      if (status.exactMatch()) {
	        best = j;
	      } else if (status.impossibleMatch()) {
	        return best;
	      }
	    }

	    return best;
	  }
	
	
}


