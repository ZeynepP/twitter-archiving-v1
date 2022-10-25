package fr.ina.dlweb.twitter.commons.es;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.ina.dlweb.twitter.commons.utils.Utils;


/**
 * Basix Http elastic search client.
 * Not thread safe.
 * @author jthievre
 *
 */
public class ESIndexClient {

	private String[] hosts;
	private String[] esNodes;
	private AtomicInteger currentNode;

	
	
//	private final String REPO_JSON = "{\"type\" : \"hdfs\",\"settings\" : {\"uri\" : \"hdfs://xx:xx/\",\"path\" : \"/user/elastic/twitter/snapshots\",\"conf_location\" : \"/etc/hadoop/conf/core-site.xml,/etc/hadoop/conf/hdfs-site.xml\",\"max_restore_bytes_per_sec\" : \"20mb\",\"max_snapshot_bytes_per_sec\" : \"20mb\"    }}";
//    private final String SNAP_JSON = "{   \"indices\": \"index_name\",   \"ignore_unavailable\": true,   \"include_global_state\": false}";
//    private final String RESTORE_JSON = "{ \"ignore_unavailable\": true,   \"include_global_state\": false, \"rename_pattern\": \"index_name\",\r\n  \"rename_replacement\": \"restored\"}";		
//    		
//  	
	public ESIndexClient( String... hosts) {

		this.hosts = hosts;
		esNodes = createNodes();
		
		currentNode = new AtomicInteger(-1);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private String[] createNodes() {
		
			String[] nodes = new String[hosts.length];
			for(int i=0; i<hosts.length; i++) {
				nodes[i] = hosts[i];
			}
			return nodes;
		
	}



	private String peekESNode() {
		int nextNode = nextNode();
		return esNodes[nextNode];
		
	}

	
	private int nextNode() {
		int next = 0;
		boolean isUpdated = false;
		
		do {
			int current = currentNode.get();
			next = (current+1)%esNodes.length;
			isUpdated = currentNode.compareAndSet(current, next);
		} while(!isUpdated);
		return next;
	}
	

	
	public ESResponse rawRest(String restUrl, String httpMethod, byte[] jsonBody) throws IOException {
		
		Request request = null;

		if("GET".equalsIgnoreCase(httpMethod)) {
			
			request = Request.Get(peekESNode()+restUrl);
		} else

		if("POST".equalsIgnoreCase(httpMethod)) {
			request = Request.Post(peekESNode()+restUrl);
		} else
		
		if("PUT".equalsIgnoreCase(httpMethod)) {
			request = Request.Put(peekESNode()+restUrl);
		} else
		
		if("DELETE".equalsIgnoreCase(httpMethod)) {
			request = Request.Delete(peekESNode()+restUrl);
		} else {
			throw new IllegalArgumentException("Http method "+httpMethod+" does not exist.");
		}

		if(request != null) {
			
			if(jsonBody != null) {
				ByteArrayEntity jsonEntity = new ByteArrayEntity(jsonBody, 0, jsonBody.length, ContentType.APPLICATION_JSON);
				jsonEntity.setChunked(false);
				request.body(jsonEntity);
			}
			
			return createESResponse(request.execute().returnResponse());
			
		}
		return null;
	}
	
	
	private ESResponse createESResponse(HttpResponse httpResponse) throws IOException {
		int status = httpResponse.getStatusLine().getStatusCode();
		String json = "";
		if(httpResponse.getEntity() != null) {
			json = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");	
		}
		return new ESResponse(status, json, this);
	}
	
	
	public boolean prepareRepository(String repo_json, String repoName) throws IOException
	{
		boolean repoExists = false;


		// checking if repo exists 
		ESResponse response = rawRest("/_snapshot/" + repoName, "GET", null); 
		
		// repo not exist create a new one 
		if(response.status!=200 )
		{

			byte[] jsonBody = repo_json.getBytes(Charset.forName("UTF-8"));
			response = rawRest("/_snapshot/"+ repoName, "PUT", jsonBody);
			if(response.status==200 )
				repoExists = true;
			else
				throw new RuntimeException("Failed: \n"+response.json);
			
		}
		else 
			repoExists = true;

		return repoExists;
		
		
	}
	

	// TODO: check try catch for logging keeping it or not
	public int restore(String restore_json, String repoName, String indexName, String restoredIndexName, String snapshotName) throws IOException
	{

		ObjectMapper obj = new ObjectMapper();
		ObjectNode js = (ObjectNode) obj.readTree(restore_json);
	    js.put("rename_pattern", indexName);
	    js.put("rename_replacement", restoredIndexName);
	    
	    restore_json = obj.writeValueAsString(js);
		

		byte[] jsonBody = restore_json.getBytes(Charset.forName("UTF-8"));
		// if already exists close index to restore
		ESResponse response = rawRest("/" + restoredIndexName  + "/_close?wait_for_completion=true", "POST", null);
		response = rawRest("/_snapshot/" +  repoName +"/" + snapshotName + "/_restore?wait_for_completion=true", "POST", jsonBody);
		

		
		// open closed index
	
		response = rawRest("/" +indexName  + "/_open?wait_for_completion=true", "POST", null);
		response = rawRest("/" +restoredIndexName  + "/_open?wait_for_completion=true", "POST", null);

		return response.status;
	}
	


	public int snapshot(String snap_json, String repoName ,String indexName, String snapshotName) throws IOException
	{
	 
		ObjectMapper obj = new ObjectMapper();
		ObjectNode js = (ObjectNode) obj.readTree(snap_json);
	    js.put("indices", indexName);
	    
	    snap_json = obj.writeValueAsString(js);

		byte[] jsonBody = snap_json.getBytes(Charset.forName("UTF-8"));

	
		ESResponse response = rawRest("/_snapshot/"+ repoName + "/" + snapshotName + "?wait_for_completion=true", "PUT", jsonBody);
		
		
		return response.status;
			
		
		
	}
//	public boolean restoreSnapshot(String repoName, String indexName, String restoredIndexName) throws Exception
//	{
//		
//		boolean ok = prepareRepository(repoName);
//		if(ok)
//		{
//			String snapshotName = createSnapshot(repoName, indexName);
//			if(snapshotName !=null)
//			{
//				return restore(repoName, indexName,indexName , snapshotName);
//			}
//
//		}
//		
//		return false;
//
//	}

	@SuppressWarnings({"rawtypes"})
	static class ESResponse {

		private int status;
		private String json;
		private Map result;
		private ESIndexClient client;

		public ESResponse(int status, String json, Map result, ESIndexClient client) {
			this.status = status;
			this.json = json;
			this.result = result;
			this.client = client;
		}

		public ESResponse(int status, String json, ESIndexClient client) {
			this.status = status;
			this.json = json;
			this.result = null;
			this.client = client;
		}

		public int status() {
			return status;
		}

		public String json() {
			return json;
		}

		
	}

}
