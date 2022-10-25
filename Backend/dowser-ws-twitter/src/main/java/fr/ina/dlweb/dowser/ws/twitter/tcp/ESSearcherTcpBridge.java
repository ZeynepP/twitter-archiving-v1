package fr.ina.dlweb.dowser.ws.twitter.tcp;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import fr.ina.dlweb.conf.Conf;
import fr.ina.dlweb.conf.Settings;
import fr.ina.dlweb.dowser.ws.twitter.rest.es.TwitterESSearcher;
import fr.ina.dlweb.tcpbridge.JsonLineTcpToSimpleHttpBridge;

public class ESSearcherTcpBridge extends JsonLineTcpToSimpleHttpBridge {

		public ESSearcherTcpBridge(String server) {
			super(server);
		}

		@Override
		protected URI createRestURI() {
			Conf conf = Settings.getConf();
			String restHostPort = conf.get("dowser.ws.twitter.host").asText();
			String restBase = conf.get("dowser.ws.twitter.base").asText();
			UriBuilder restUriBuilder = UriBuilder.fromUri("http://"+restHostPort+restBase);
			URI restUri = restUriBuilder.path(TwitterESSearcher.class).path(TwitterESSearcher.class, "dowser").build();
			return restUri;	
		}
		
}
