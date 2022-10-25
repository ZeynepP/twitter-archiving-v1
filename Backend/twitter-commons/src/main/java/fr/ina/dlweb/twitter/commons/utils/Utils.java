package fr.ina.dlweb.twitter.commons.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.ina.dlweb.twitter.commons.io.CommonsFileReader;


public class Utils {

	public static ObjectMapper objectMapper = new ObjectMapper();
	static String charsetName=  "UTF-8";
	
	
	public static void writeTwitterLimitErrors(String limitFile, List<String> limits,  String fileName) throws IOException
	{

		if(limits.size() > 0)
		{
			FileWriter writer;
			File t = new File(fileName);
			File directory = new File(limitFile);
			
		    if (! directory.exists())
		        directory.mkdir();
	
			writer = new FileWriter(limitFile  + "//" + t.getName(),true);
			for(String str: limits) {
				  writer.write(str);
				  writer.write("\n");
				}
			writer.close();
		}
	
	}
	
	
	/**
	 * 
	 * Writes offset for each file into a logfile : filename;offset;date
	 * 
	 * @param logfile a file that contains the filename;offset;date
	 * @param append is set to true if it is for backup, unless false
	 * @throws FileNotFoundException 
	 * 
	 */
	public static void writeHashMap(String logFile, boolean append, ConcurrentHashMap maps, boolean withDate) throws FileNotFoundException
	{
		PrintWriter saveAs;
	
		saveAs = new PrintWriter(new FileOutputStream( new File(logFile),  append)); 
		String date = new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date());
		for (Object entry : maps.entrySet()){
			if(withDate)
				saveAs.append(((Entry)entry).getKey() + ";" + ((Entry)entry).getValue() +";"+date +"\n");
			else
				saveAs.append(((Entry)entry).getKey() + ";" + ((Entry)entry).getValue() +"\n");
		}
		saveAs.close();
	
		
	}
	

	
	public static String join(List<String> strings, String joinString)
    {
        if (strings == null || strings.size() == 0) return null;
        StringBuilder b = new StringBuilder();
        for (String str : strings) {
            if (str != null) {
                b.append(str);
                if (joinString != null) b.append(joinString);
            }
        }
        return b.toString();
   }
	


	
	
	/**
	 * Reads file over https 
	 * We needed it to read data from https (alpos i think)
	 * @param url
	 * @return InputStreamReader
	 * @throws IOException
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 */
	
	// ATTENTION A NE PAS UTILISER CE CODE POUR LES SERVEURS EXTERIEURS INA
	public static  InputStream getInputStreamIgnoreSSLError(String url ) throws IOException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException{
		
		// The underlying HTTP connection is still held by the response object
		// to allow the response content to be streamed directly from the network socket.
		// In order to ensure correct deallocation of system resources
		// the user MUST call CloseableHttpResponse#close() from a finally clause.
		// Please note that if response content is not fully consumed the underlying
		// connection cannot be safely re-used and will be shut down and discarded
		// by the connection manager. 

		CloseableHttpClient httpClient = HttpClients
				.custom()
				.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
				.setSslcontext((new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy()
				        {
		
							@Override
							public boolean isTrusted(X509Certificate[] chain, String authType)
									throws CertificateException {
								// TODO Auto-generated method stub
								return true;
							}
				        }).build()))
				.build();
		
		
		HttpGet httpGet = new HttpGet(url);
		CloseableHttpResponse response1 = httpClient.execute(httpGet);
		try {
		   HttpEntity entity1 = response1.getEntity();
		   try {
		    String res =  EntityUtils.toString(entity1, "UTF-8");
		    
		    return new ByteArrayInputStream(res.getBytes());

		   } finally {
		    EntityUtils.consume(entity1);
		   }
		} finally {
		   response1.close();
		}
		
	}
		
	
	public static InputStream getInputStream(String url, long offset) throws Exception
	{
		InputStream inputStream = null;
		if(url.startsWith("http"))
		{
			try{
				URL urlRUL = new URL(url);
				
				HttpURLConnection conn  =  (HttpURLConnection) urlRUL.openConnection();
				if (offset != 0L) {
				    conn.setRequestProperty("Range", "bytes=" + offset + "-");
				}

				if(conn.getResponseCode() == HttpURLConnection.HTTP_OK || conn.getResponseCode() == HttpURLConnection.HTTP_PARTIAL )
				{
					long size = Long.parseLong( conn.getHeaderField("content-length"));
				
					
					if( size > 0) {
						InputStream httpInputStream =  conn.getInputStream();
						inputStream = new HttpInputReader(httpInputStream, conn);
					}
					
				}
			} catch(SSLHandshakeException e)
			{
				/// we should ensure that LES SERVEURS soit à l'INA in config files 
				// This class also used to read source jsons from extradev 
				inputStream = getInputStreamIgnoreSSLError(url);
			}
			
				
		}
		else
		{
			RandomAccessFile raf = new RandomAccessFile(url, "r");
			raf.seek(offset);
			inputStream =  Channels.newInputStream(raf.getChannel());
 
		}
		return inputStream;
		
	}
	

	/**
	 * Returns list of json files from twitter_data parameter of config file
	 * twitter_data can contain list of folders or urls to parse
	 * @param folder 
	 * @throws URISyntaxException 
	 */
	public static List<String> getJsonsList(String folder) throws MalformedURLException, URISyntaxException
	{
			System.out.println(folder);
			List<String> fileJsons = new ArrayList<String>();
			if(folder.endsWith("jsons"))
			{
				fileJsons.add(folder);
			}
			else
			{
					URI uri = new URI(folder);
					if(uri.getScheme().equals("file"))
					{
					
						File files = new File(uri);
						Collection<File> list = org.apache.commons.io.FileUtils.listFiles(files,  new String[]{"jsons"}, true);
						for (File f : list)
						{
							fileJsons.add(f.getAbsolutePath());
						}
					}
					else 
					{
						getRecursiveFileListFromHttp(fileJsons, folder);
					}
			}
			return fileJsons;

		
	}
	
	
	private static void getRecursiveFileListFromHttp(List<String> fileJsons, String url )
	{
		
		
		if(!url.isEmpty() && !url.endsWith(".jsons") && !url.endsWith(".json")) // empty string happens with Jsoup 
		{
			System.out.println("Getting files in " + url);
			Document doc = null;
			if(url.endsWith("/")) url = url + "*";
			else url = url + "/*";
				
			// here need to keep try catch with stdout to go on
			try {
					doc = Jsoup.connect(url ).maxBodySize(0).timeout(600000).get();
					String body = doc.body().getAllElements().text();
					String[] lines = body.split(" ");
					for(String line : lines)
					{
						if(line.endsWith(".jsons"))
								fileJsons.add(line);
						else if(!line.endsWith(".json"))
							getRecursiveFileListFromHttp(fileJsons, line);
					}
				
				
		
			} catch (Exception e) {
				System.out.println("Problem to get list of files for folder : " + url + " : " + e.getMessage());
			}
		}
			
	}
	
	public static HashMap sortByValues(HashMap map) { 
	       List list = new LinkedList(map.entrySet());
	       // Defined Custom Comparator here
	       Collections.sort(list, new Comparator() {
	            public int compare(Object o1, Object o2) {
	               return ((Comparable) ((Map.Entry) (o1)).getValue())
	                  .compareTo(((Map.Entry) (o2)).getValue());
	            }
	       });

	       // Here I am copying the sorted list in HashMap
	       // using LinkedHashMap to preserve the insertion order
	       HashMap sortedHashMap = new LinkedHashMap();
	       for (Iterator it = list.iterator(); it.hasNext();) {
	              Map.Entry entry = (Map.Entry) it.next();
	              sortedHashMap.put(entry.getKey(), entry.getValue());
	       } 
	       return sortedHashMap;
	  }
	


	/**
	 * Returns a hashmap 
	 * @param filename the log file name that keeps tracks like shorteenurl;urlexpanded etc. 
	 * @return ConcurrentHashMap<String, String> 
	 * @throws Exception 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static ConcurrentHashMap<String, String> readHashMapFromFile(String fileName, String delim, int column1, int column2) throws Exception  {
		
		ConcurrentHashMap<String, String> map = new ConcurrentHashMap<String, String>();
		CommonsFileReader scanner = new CommonsFileReader(fileName,0);
        while (scanner.hasNext()) {	  
        	
	            String[] columns =scanner.next().split(delim);
	            if(columns.length >= column2)
		            	map.put(columns[column1],  columns[column2]);
	           
        	
        }
        scanner.close();
		return map;
    }

	/**
	 * Return s string value of log message enriched by application name 
	 * @param applicationName
	 * @param node : ObjectNode containing log message created by  createJsonLogMessage
	 * @return string to log
	 */
	public static String convertToJsonMessage(String applicationName, ObjectNode node)
	{
		
		ArrayNode anode = objectMapper.createArrayNode();
		anode = anode.add((int)(new Date().getTime()  / 1000));
		
		anode = anode.add(applicationName);
		anode = anode.add(node);
		return anode.toString(); // astext
		
	}
	

	/**
	 * Creates a json object by log information
	 * @param logType : warn, exception, error etc.
	 * @param messageType :  crawled, error etc. 
	 * @param message
	 * @param count
	 * @return ObjectNode that contains json log 
	 */
	public static ObjectNode createJsonLogMessage(String logType, String messageType, String message, int count)
	{
		ObjectNode node = objectMapper.createObjectNode();
		
		node.put("log_type", logType);
		node.put("message_type", messageType);
		node.put("message", message);
		node.put("count", count);
	
		return node;

	}

	private static class HttpInputReader extends FilterInputStream {

		private HttpURLConnection connection;
		
		public HttpInputReader(InputStream in, HttpURLConnection connection) {
			super(in);
			this.connection = connection;
			// TODO Auto-generated constructor stub
		}

		@Override
		public void close() throws IOException {
			// TODO Auto-generated method stub
			super.close();
			connection.disconnect();
		}
		
		
		
		

		
		
	}
	
}



