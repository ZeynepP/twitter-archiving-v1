package fr.ina.dlweb.dowser.ws.twitter.app;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import fr.ina.dlweb.conf.Settings;
import fr.ina.dlweb.dowser.ws.commons.utils.log.Log4jConfigurator;
import fr.ina.dlweb.tcpbridge.TcpBridges;

@WebListener
public class TwitterAppOnStartupAndShutdown  implements ServletContextListener{

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		Settings.useConf("dowser-ws-twitter");
		Log4jConfigurator.enable();
		TcpBridges.getInstance().startAll();
	}
	
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		TcpBridges.getInstance().stopAll();
		TcpBridges.getInstance().shutdownAll();
		
	}

}
