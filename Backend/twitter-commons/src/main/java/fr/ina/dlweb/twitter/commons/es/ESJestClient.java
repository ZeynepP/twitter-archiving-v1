package fr.ina.dlweb.twitter.commons.es;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import fr.ina.dlweb.utils.FileUtils;
import io.searchbox.action.BulkableAction;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Bulk;
import io.searchbox.core.BulkResult;
import io.searchbox.core.BulkResult.BulkResultItem;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.DeleteIndex;
import io.searchbox.indices.IndicesExists;
import io.searchbox.indices.mapping.PutMapping;



public class ESJestClient {
	

	JestClient jestClient;
	String indexName;
	String indexType;
	int maxRetrySize = 5;
	boolean autoCreateIndex ;


	public ESJestClient(String[] es_host, String indexName, String indexschema, String indexType,  String indexsettingsfile, boolean autoCreateIndex) throws Exception
	{
	
		
			this.indexName = indexName;
			this.indexType =indexType;
			this.autoCreateIndex = autoCreateIndex;
			
			jestClient = jestClient(es_host);
			initializeIndex(indexName, indexschema, indexType,  indexsettingsfile);
 
		
		
	}
	
	private JestClient jestClient(String[]  es_host)
	{
		
		JestClientFactory jestFactory = new JestClientFactory();
		jestFactory.setHttpClientConfig(
				new HttpClientConfig.Builder(Arrays.asList(es_host))
									.discoveryEnabled(false)
									.discoveryFrequency(3l, TimeUnit.HOURS)
				                    .multiThreaded(true)
				                    .readTimeout(300000)
				                    .build()
		);
		JestClient  jestClient = jestFactory.getObject();
	
		return jestClient;
		
		
	}
	


	private void initializeIndex(String index_name, String index_schema,String index_type_name, String index_settings) throws Exception
	{
		
		
		IndicesExists indexExists = new IndicesExists.Builder(index_name).build();
		JestResult indexExistsResult = jestClient.execute(indexExists);

	
		if(!indexExistsResult.isSucceeded()) {
			if(!autoCreateIndex) {
				throw new Exception("InitializeIndex Index does not exist. Use autoCreateIndex parameter for force creation of an index. Quiting...");
				
			} else {
				
				String settingsSource = FileUtils.slurp(new File(index_settings));
				CreateIndex createIndex = new CreateIndex.Builder(index_name).settings(settingsSource).build();
				JestResult result = jestClient.execute(createIndex);
				
				if(!result.isSucceeded()) {
					throw new Exception("InitializeIndex Error during create index  " + result.getErrorMessage());
					
				}
				
				String mappingSource = FileUtils.slurp(new File(index_schema));
				PutMapping putMapping = new PutMapping.Builder(index_name,index_type_name,mappingSource).build();
				result =jestClient.execute(putMapping);
				if(!result.isSucceeded()) {

					DeleteIndex deleteIndex = new DeleteIndex.Builder(index_name).build();
					result = jestClient.execute(deleteIndex);
					
					throw new Exception("InitializeIndex: Error during mapping: " + result.getErrorMessage());
					
				}
				
				
			}
		}	
		

	}

	public BulkResult bufferToEs(List<BulkableAction<DocumentResult>> docBuffer) throws IOException 
	{
			int retry = 0; 

			BulkResult result = null;
		
			//http://stackoverflow.com/questions/30222880/elasticsearch-jest-update-a-whole-document
		   Bulk bulk = new Bulk.Builder()
								.defaultIndex(indexName)
								.defaultType(indexType)
								.addAction(docBuffer)
								.setParameter("consistency", "all")
								.setParameter("timeout", "5m")
								.build();
			
			
			result = jestClient.execute(bulk);

			while (retry < 3 && result.getFailedItems().size() !=0 ) //, 200 etc. index is closed etc.
			{
				boolean isConflict = true;
				
				for(int i=0;i<result.getFailedItems().size();i++)
				{
					if(result.getFailedItems().get(i).status != 409 )
					{
						isConflict = false;
						break;
					}
				}
			
				//ignore version conflicts
				if(!isConflict)
				{
					try {
						Thread.sleep((retry + 1) * 180000 );
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}

					Set<String> failedItemIds = new HashSet<String>();
					for(BulkResultItem item : result.getFailedItems()) {
						// version conflict is an acceptable error
						if(item.status != 409) {
							failedItemIds.add(item.id);
						}
					}
					
					List<BulkableAction<DocumentResult>> retryDocBuffer = new ArrayList<BulkableAction<DocumentResult>>();
					

					for(BulkableAction<DocumentResult> item : docBuffer) {

						if(failedItemIds.contains(item.getId())) {
							retryDocBuffer.add(item);
						}
						
					}
					
					bulk = new Bulk.Builder()
								.defaultIndex(indexName)
								.defaultType(indexType)
								.addAction(docBuffer)
								.setParameter("consistency", "all")
								.setParameter("timeout", "5m")
								.build();
					
					result = jestClient.execute(bulk);
					
				}
				
					
				
				retry++;
			}
				
			
	
			
			return result;

		
	}

	public SearchResult searhIndex(String query) throws IOException
	{
		
        Search.Builder searchBuilder = new Search.Builder(query).addIndex(indexName).addType(indexType);
		SearchResult result = (SearchResult) jestClient.execute(searchBuilder.build());
	    return result;
		
	}
	

	
	public void shutDownESClient()
	{
		
		jestClient.shutdownClient();
		
	}
	

	
}
