package fr.ina.dlweb.twitter.indexer.utils;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ina.dlweb.conf.Conf;
import fr.ina.dlweb.twitter.commons.utils.Utils;


public class Settings {

	static Logger Log = LoggerFactory.getLogger(Settings.class);
	
	public static String offsetFile;
	public static String applicationName;
	
	
	public static String[] ENTITIES_OBJECTS = {"media","urls","user_mentions","hashtags","symbols"};
	public static String[] ENTITIES_TYPES = {"entities","extended_entities"};
	

	// ES index related variables
	public static String[] esHosts;
	
	public static String indexName;  
	public static String indexType;
	public static String indexSchema;
	public static String indexSettings;
	
	
	public static Proxy proxy;
	public static boolean autoCreateIndex;
	public static String esIdField;
	public static int maxRecordCount;
	public static boolean unshortenUrls;
	public static String unshortenUrlsFileOutput;
	public static ConcurrentHashMap<String, String> urls = new ConcurrentHashMap<String, String>();

	public static ConcurrentHashMap<String, String> mapFileOffset = new ConcurrentHashMap<String, String>();
	public static String limitFiles;
	
	public static boolean upsert = true;
	public static String[] twitterData;

	
	
	public static String upsertScript = 
			"if(ctx._source.containsKey(\"archived_at\") && ctx._source.archived_at > archivedate) {ctx._source.archived_at=archivedate;} ; "
			+"if(ctx._source.containsKey(\"collection\") ) { if(!ctx._source.collection.contains(colname)) {ctx._source.collection +=colname;}} else { ctx._source.collection =[colname]} ; "
			+"if(ctx._source.containsKey(\"source_path\")) { if(!ctx._source.source_path.contains(spath))  { ctx._source.source_path +=spath;}} else { ctx._source.source_path =[spath]} ; "
			+"if(ctx._source.containsKey(\"method_archive\")) { if(!ctx._source.method_archive.contains(marchive))  { ctx._source.method_archive +=marchive;}} else { ctx._source.method_archive =[marchive]} ; "
			+"if(ctx._source.containsKey(\"source_type\")) { if(!ctx._source.source_type.contains(stype))  { ctx._source.source_type +=stype;}} else { ctx._source.source_type =[stype]} ; "
			+"if(ctx._source.containsKey(\"is_extended\")) { if(!ctx._source.is_extended.contains(sid))  { ctx._source.is_extended += sid;}} else { ctx._source.is_extended =[sid]} ; ";
	
	
	@SuppressWarnings("unchecked")
	public static void  InstallSettingsforIndexing(String path) throws Exception 
	{
		Conf tweetconf = new Conf();
		tweetconf.loadJson(new FileInputStream(path));
		
		String delim = tweetconf.get("settings").get("delim").asText();
		applicationName = tweetconf.get("settings").get("application_name").asText();
		
		
		esIdField= tweetconf.get("elastic").get("id_field").asText();
		esHosts =  tweetconf.get("elastic").get("host").asStringArray();
		indexName =  tweetconf.get("elastic").get("index_name").asText(); 
		indexType =  tweetconf.get("elastic").get("index_type_name").asText();
		indexSchema =  tweetconf.get("elastic").get("index_schema").asText();
		indexSettings =  tweetconf.get("elastic").get("index_settings").asText();
		autoCreateIndex =  tweetconf.get("elastic").get("auto_create_index").asBoolean();
		maxRecordCount =  tweetconf.get("elastic").get("max_record_count").asInt();
		
		
		proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(tweetconf.get("settings").get("proxy_host").asText(), Integer.parseInt(tweetconf.get("settings").get("proxy_port").asText())));

		
		
		offsetFile = tweetconf.get("data").get("offset_file").asText();
		try {
			mapFileOffset = Utils.readHashMapFromFile(offsetFile,";",0,1);
		} catch (FileNotFoundException e) {
			Log.warn( Utils.convertToJsonMessage(applicationName,  Utils.createJsonLogMessage("warn", "warn", offsetFile + " Not found", 1)));
		}


		twitterData = tweetconf.get("data").get("twitter_data").asText().split(delim);
		limitFiles = tweetconf.get("data").get("limit_file").asText();
		
		
		unshortenUrlsFileOutput = tweetconf.get("data").get("urls_file").asText();
		unshortenUrls = tweetconf.get("settings").get("url_unshorten").asBoolean();
		if(unshortenUrls)
		{
			Arrays.sort(urlshorteners);
			try {
				urls = Utils.readHashMapFromFile(unshortenUrlsFileOutput,";",0,1);
			} catch (FileNotFoundException e) {
				Log.warn( Utils.convertToJsonMessage(applicationName,  Utils.createJsonLogMessage("warn", "warn", unshortenUrlsFileOutput + " Not found", 1)));
			}
		}
		
			
	}	
	
	
	
	
	public static boolean fromScratch;
	public static String mapping ;
	@SuppressWarnings("unchecked")
	public static void  InstallSettingsforInfoFileGenerator(String path) throws Exception 
	{
		Conf tweetconf = new Conf();
		tweetconf.loadJson(new FileInputStream(path));
		String delim = ";";
		offsetFile = tweetconf.get("offset_file").asText();
		twitterData = tweetconf.get("twitter_data").asText().split(delim);
		fromScratch = tweetconf.get("from_scratch").asBoolean();
		mapping = tweetconf.get("mapping").dump();
		
	}
	
	public static String REPO_JSON = "{\"type\" : \"hdfs\",\"settings\" : {\"uri\" : \"hdfs://xxxx01.ina.fr:8020/\",\"path\" : \"/user/elastic/twitter/snapshots\",\"conf_location\" : \"/etc/hadoop/conf/core-site.xml,/etc/hadoop/conf/hdfs-site.xml\",\"max_restore_bytes_per_sec\" : \"20mb\",\"max_snapshot_bytes_per_sec\" : \"20mb\"    }}";
	public static String SNAP_JSON = "{   \"indices\": \"index_name\",   \"ignore_unavailable\": true,   \"include_global_state\": false}";
    public static String RESTORE_JSON = "{ \"ignore_unavailable\": true,   \"include_global_state\": false, \"rename_pattern\": \"index_name\",\r\n  \"rename_replacement\": \"restored\"}";		

	
	public static String restoreIndexName;  
	public static String restoreRepositoryName;
	public static String[] targetEsHosts;
	

	
	@SuppressWarnings("unchecked")
	public static void  InstallSettingsforRestoreSnapshot(String path) throws FileNotFoundException, IOException 
	{
		Conf tweetconf = new Conf();
		tweetconf.loadJson(new FileInputStream(path));
	
		esHosts =  tweetconf.get("elastic").get("source_hosts").asStringArray();
		indexName =  tweetconf.get("elastic").get("source_index").asText(); 
		targetEsHosts = tweetconf.get("elastic").get("target_hosts").asStringArray();


		REPO_JSON =  tweetconf.get("repository").dump(false);
		
	
		SNAP_JSON =  tweetconf.get("snapshot").dump(false);
		

		RESTORE_JSON =  tweetconf.get("restore").dump(false);
		
		

		restoreIndexName = tweetconf.get("elastic").get("target_index").asText(); 
		restoreRepositoryName = tweetconf.get("elastic").get("restore_repository_name").asText(); 

			
	}
	
	
	
	
	// TODO: Add them to a file and read from file
	
	public static String[] urlshorteners = //{"bit.ly","goo.gl","ow.ly", "dlvr.it","ift.tt", "j.mp",  "buff.ly","huff.to","ln.is","binged.it","wp.me", "tinyurl.com","trib.al","shar.es","is.gd","owl.li"};
	{
		"buff.ly","ift.tt",
		"t.co"	,"ln.is","binged.it",
		"g.co"	,
		"j.mp"	,
		"q.gs"	,
		"is.gd"	,
		"tl.gd"	,
		"es.pn"	,
		"fb.me"	,
		"tr.im"	,
		"ow.ly"	,
		"cl.ly"	,
		"kl.am"	,
		"wp.me"	,
		"ht.ly"	,
		"su.pr"	,
		"ds.io"	,
		"gu.com"	,
		"goo.gl"	,
		"owl.li"	,
		"cli.gs"	,
		"tpm.ly"	,
		"htn.to"	,
		"bbc.in"	,
		"kcy.me"	,
		"bit.ly"	,
		"ur1.ca"	,
		"dld.bz"	,
		"rww.to"	,
		"adf.ly"	,
		"cos.as"	,
		"ebz.by"	,
		"cdb.io"	,
		"tnw.to"	,
		"adf.ly"	,
		"sta.mn"	,
		"vsb.ly"	,
		"cot.ag"	,
		"tpt.to"	,
		"soc.li"	,
		"smf.is"	,
		"see.sc"	,
		"mbl.mx"	,
		"vsb.li"	,
		"ind.pn"	,
		"awe.sm"	,
		"htl.li"	,
		"mun.do"	,
		"mzl.la"	,
		"tgr.ph"	,
		"nblo.gs"	,
		"tiny.cc"	,
		"cort.as"	,
		"dlvr.it"	,
		"noti.ca"	,
		"nyti.ms"	,
		"flic.kr"	,
		"lnkd.in"	,
		"tcrn.ch"	,
		"zite.to"	,
		"shar.es"	,
		"ppfr.it"	,
		"post.ly"	,
		"yhoo.it"	,
		"vrge.co"	,
		"read.bi"	,
		"seod.co"	,
		"6sen.se"	,
		"neow.in"	,
		"econ.st"	,
		"pear.ly"	,
		"huff.to"	,
		"hint.fm"	,
		"amba.to"	,
		"trib.al"	,
		"ymlp.com"	,
		"twurl.nl"	,
		"short.to"	,
		"pulse.me"	,
		"tmblr.co"	,
		"oreil.ly"	,
		"short.ie"	,
		"menea.me"	,
		"flpbd.it"	,
		"refer.ly"	,
		"egent.me"	,
		"mcmgz.in"	,
		"nokia.ly"	,
		"s.vfs.ro"	,
		"on.fb.me"	,
		"youtu.be"	,
		"keruff.it"	,
		"on.rt.com"	,
		"on.mash.to"	,
		"feedly.com"	,
		"on.wsj.com"	,
		"tinyurl.com"	,
		"www.tumblr.com"	,
		"feeds.gawker.com"	,
		"feeds.feedburner.com"	,
		"feedproxy.google.com"	};
	

	
	
	
}
