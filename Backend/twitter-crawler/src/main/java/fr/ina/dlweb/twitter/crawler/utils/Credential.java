package fr.ina.dlweb.twitter.crawler.utils;

import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class Credential {
	
	public String CONSUMER_KEY;
	public String CONSUMER_SECRET;
	public String OAUTH_TOKEN;
	public String OAUTH_TOKEN_SECRET;
	public String OWNER;
    public boolean isActive = false;
    public int activecounter = 0;
    public boolean onlyAuthEnabled = false;
    
	public String toString() {	
		return OWNER + " - " + CONSUMER_KEY +" - " + CONSUMER_SECRET + "  - " + OAUTH_TOKEN +" - "+ OAUTH_TOKEN_SECRET;
	}
	
	
	public Configuration getConfigurationOAuth()
	{
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(false)
		  .setIncludeEntitiesEnabled(true)
		  .setPrettyDebugEnabled(false)
		  .setIncludeMyRetweetEnabled(false)
		  .setOAuthConsumerKey(this.CONSUMER_KEY)
		  .setOAuthConsumerSecret(this.CONSUMER_SECRET)
		  .setOAuthAccessToken(this.OAUTH_TOKEN)
		  .setOAuthAccessTokenSecret(this.OAUTH_TOKEN_SECRET)
		  .setApplicationOnlyAuthEnabled(onlyAuthEnabled)
		  .setJSONStoreEnabled(true)
		  .setTweetModeExtended(true);
		  
	
		 cb.setHttpProxyHost(SettingsCrawler.proxyHost);
		 cb.setHttpProxyPort(Integer.valueOf(SettingsCrawler.proxyPort));
	
		 return cb.build();
		
	}


	
	
	

}
