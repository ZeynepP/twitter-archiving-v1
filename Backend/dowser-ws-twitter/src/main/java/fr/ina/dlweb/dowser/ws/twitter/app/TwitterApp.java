package fr.ina.dlweb.dowser.ws.twitter.app;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import fr.ina.dlweb.conf.Conf;
import fr.ina.dlweb.conf.Settings;
import fr.ina.dlweb.dowser.ws.commons.jersey.provider.IndentingJacksonProvider;


public class TwitterApp extends ResourceConfig {

	public TwitterApp() {
		
		packages("fr.ina.dlweb.dowser.ws.commons.rest.log4j");
		packages("fr.ina.dlweb.dowser.ws.commons.rest.settings");
		packages("fr.ina.dlweb.dowser.ws.twitter.rest");
		register(JacksonFeature.class);
		register(IndentingJacksonProvider.class);
		
		Conf conf = Settings.getConf();
		if(conf.path("dowser.ws.twitter.log.jersey").asBoolean(false)) {
			register(LoggingFilter.class);
		}
	}
	
}
