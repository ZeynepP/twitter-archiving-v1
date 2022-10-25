package fr.ina.dlweb.twitter.commons.utils;

import java.io.File;
import java.net.URL;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.ina.dlweb.twitter.commons.io.TwitterSourceFileUtils;
import fr.ina.dlweb.utils.FileUtils;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterSourceCsvToJsonTest
	
	public static void main(String[] args) throws Exception {
		
		ObjectMapper jsonMapper = new ObjectMapper();
		
		URL credentialUrl = TwitterSourceFileUtils.class.getResource("/credentials.json");
		String credentialJson = FileUtils.slurp(credentialUrl);
		
		ArrayNode credentials = (ArrayNode)jsonMapper.readTree(credentialJson);
		
		ObjectNode selectedCredential = null;
		for(JsonNode credential : credentials) {
			ArrayNode apps = (ArrayNode)credential.get("INA_APPS");
			for(JsonNode app : apps) {
				if("user_ids".equals(app.textValue())) {
					selectedCredential = (ObjectNode)credential;
					break;
				}	
			}
			if(null != selectedCredential) {
				break;
			}
		}
		
		if(null == selectedCredential) {
			throw new RuntimeException("No Twitter credential found for INA_APP: user_ids");
		}
		

//		teletravail 		
		File all_user_source = new File("D:/Data/svn-repo/stores/users/twitter_users.jsons");
		File all_hashtag_1_source = new File("D:/Data/svn-repo/stores/keywords/twitter.jsons");
		File all_hashtag_2_source = new File("D:/Data/svn-repo/stores/keywords/twitter2.jsons");
		
		File new_source = new File("C:/xxx.jsons");
		
		
		TwitterSourceFileUtils.convertCsvToJsons(
			new File("C:/xx_twitter_du 2018-04-09.csv"),
			"windows-1252",
			new_source
		);
		
		
		TwitterSourceFileUtils.enrichWithUserId(new_source, getCredentialTwitter4JConf(selectedCredential, "xxxx.ina.fr", yy));
		
		
		Set<Long> all_ids = TwitterSourceFileUtils.extractUserIds(all_user_source);
		Set<Long> new_ids = TwitterSourceFileUtils.extractUserIds(new_source);
		all_ids.retainAll(new_ids);
		System.out.println("ids intersection="+ all_ids);
		
		
		Set<String> all_hashtags = TwitterSourceFileUtils.extractHashtags(all_hashtag_1_source);
		all_hashtags.addAll(TwitterSourceFileUtils.extractHashtags(all_hashtag_2_source));
		Set<String> new_hashtags = TwitterSourceFileUtils.extractHashtags(new_source);
		
		all_hashtags.retainAll(new_hashtags);
		System.out.println("hashtags intersection="+ all_hashtags);
		
	}

	

	private static Configuration getCredentialTwitter4JConf(ObjectNode credentialNode, String proxyHost, int proxyPort) {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(false)
		  .setIncludeEntitiesEnabled(true)
		  .setPrettyDebugEnabled(false)
		  .setIncludeMyRetweetEnabled(false)
		  .setOAuthConsumerKey(credentialNode.get("CONSUMER_KEY").textValue())
		  .setOAuthConsumerSecret(credentialNode.get("CONSUMER_SECRET").textValue())
		  .setOAuthAccessToken(credentialNode.get("OAUTH_TOKEN").textValue())
		  .setOAuthAccessTokenSecret(credentialNode.get("OAUTH_TOKEN_SECRET").textValue())
		  .setApplicationOnlyAuthEnabled(true)
		  .setJSONStoreEnabled(true)
		  .setTweetModeExtended(true);
		  
		if(null != proxyHost) {
			cb.setHttpProxyHost(proxyHost);
			cb.setHttpProxyPort(Integer.valueOf(proxyPort));
		}
	
		 return cb.build();
	}
	
}
