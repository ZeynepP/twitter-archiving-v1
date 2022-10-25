package fr.ina.dlweb.dowser.ws.twitter.rest.es;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import fr.ina.dlweb.dowser.ws.twitter.model.request.TwitterSearchRequest;
import fr.ina.dlweb.dowser.ws.twitter.model.response.TwitterSearchHitsResponse;
import fr.ina.dlweb.dowser.ws.twitter.model.response.TwitterSearchResponse;
import fr.ina.dlweb.dowser.ws.twitter.model.response.TwitterTimelineBuckets;
import fr.ina.dlweb.dowser.ws.twitter.model.response.TwitterTopEntityBuckets;
import fr.ina.dlweb.dowser.ws.twitter.model.response.TwitterTrendsHitsResponse;

import info.debatty.java.stringsimilarity.Damerau;
import info.debatty.java.stringsimilarity.JaroWinkler;
import info.debatty.java.stringsimilarity.Levenshtein;
import info.debatty.java.stringsimilarity.NormalizedLevenshtein;
import it.uniroma1.dis.wsngroup.gexf4j.core.Edge;
import it.uniroma1.dis.wsngroup.gexf4j.core.EdgeType;
import it.uniroma1.dis.wsngroup.gexf4j.core.Gexf;
import it.uniroma1.dis.wsngroup.gexf4j.core.Graph;
import it.uniroma1.dis.wsngroup.gexf4j.core.Mode;
import it.uniroma1.dis.wsngroup.gexf4j.core.Node;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.Attribute;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeClass;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeList;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeType;
import it.uniroma1.dis.wsngroup.gexf4j.core.dynamic.TimeFormat;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.GexfImpl;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.StaxGraphWriter;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.data.AttributeListImpl;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.viz.ColorImpl;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.viz.PositionImpl;
import it.uniroma1.dis.wsngroup.gexf4j.core.viz.Color;
import it.uniroma1.dis.wsngroup.gexf4j.core.viz.Position;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.datastructure.SAXRecords;

import com.fastdtw.dtw.FastDTW;
import com.fastdtw.timeseries.TimeSeries;
import com.fastdtw.timeseries.TimeSeriesBase;
import com.fastdtw.util.Distances;


public class TwitterESResponse {

	
		public static TwitterSearchResponse fromESSearchResponse(String esResponse, TwitterSearchRequest request) 
		{

			TwitterSearchResponse response = new TwitterSearchResponse();
			ObjectNode esRoot = null;
			try {
				esRoot = (ObjectNode)TwitterESResponseUtils.jsonMapper.readTree(esResponse);
			} catch(Exception e) {
				throw new RuntimeException("Can't read json  fromESSearchResponse", e);
			}
			
			JsonNode esErrorNode = esRoot.get("error");
			if(esErrorNode == null) {
					response.setOk(true);
					response.setTime_interval( request.getTime_interval() );
					response.setSearch_duration(esRoot.get("took").getIntValue());
					response.setHits(TwitterESResponseUtils.getHitsFromResponse(esRoot));
					response.setTotal_count(esRoot.get("hits").get("total").asLong());

			} else {
				response.setOk(false);
				response.setError(esErrorNode.asText());
			}
			
			
			return response;
		}
		
		public static TwitterTimelineBuckets[] fromESTimelineResponse(String esResponse) {
			
			ObjectNode esRoot = null;
			try {
				esRoot = (ObjectNode)TwitterESResponseUtils.jsonMapper.readTree(esResponse);
			} catch(Exception e) {
				throw new RuntimeException("Can't read json es reponse", e);
			}
			JsonNode esErrorNode = esRoot.get("error");
			if(esErrorNode == null) {
				//	System.out.println(esRoot.get("aggregations").get("timeline").get("buckets").size());
				return TwitterESResponseUtils.getTimelineFromResponse(esRoot.get("aggregations").get("timeline").get("buckets"));
				

			}
			else {
				return null;
			}
				
			
		}
		
		
		public static HashMap<String,ArrayList<TwitterTrendsHitsResponse> > fromESTrendsResponse(String esResponse) {
			
			ObjectNode esRoot = null;

			try {
				esRoot = (ObjectNode)TwitterESResponseUtils.jsonMapper.readTree(esResponse);
			} catch(Exception e) {
				throw new RuntimeException("Can't read json es reponse", e);
			}
			JsonNode esErrorNode = esRoot.get("error");
			
				
			
			if(esErrorNode == null) {
				
				return TwitterESResponseUtils.getTrendsHitsFromResponse(esRoot);

			}
			else {
				return null;
			}
				
			
		}
		
		public static TwitterSearchResponse fromESAnalyzeResponse(String esResponse) {
			
			ObjectNode esRoot = null;
			try {
				esRoot = (ObjectNode)TwitterESResponseUtils.jsonMapper.readTree(esResponse);
			} catch(Exception e) {
				throw new RuntimeException("Can't read json es reponse", e);
			}
			TwitterSearchResponse response = new TwitterSearchResponse();
			JsonNode esErrorNode = esRoot.get("error");
			if(esErrorNode == null) {
				response.setOk(true);
				response.setTimeline(TwitterESResponseUtils.getTimelineFromResponse(esRoot.get("aggregations").get("analyze").get("timeline").get("buckets"))); 
			}
			else {
				response.setOk(false);
				response.setError(esErrorNode.asText());
			}
			return response;
		
		
		}
		/* Function to test clusters 
		public static TwitterTopEntityBuckets[]  fromESClusteringResponse(String esResponse) {
			TwitterTopEntityBuckets[] temp=null;
			ObjectNode esRoot = null;
			String stocompare = "";
			String h ="";
			try {
				esRoot = (ObjectNode)TwitterESResponseUtils.jsonMapper.readTree(esResponse);
			} catch(Exception e) {
				throw new RuntimeException("Can't read json es reponse", e);
			}

			
			if(!esRoot.has("error")) {
				
				temp = TwitterESResponseUtils.getDashboardFromResponse(esRoot.get("aggregations"),"clustering"); 
			}
			NormalAlphabet na = new NormalAlphabet();
			SAXProcessor sp = new SAXProcessor();
			JaroWinkler jw = new JaroWinkler();
			 Levenshtein l = new Levenshtein();
			 NormalizedLevenshtein ln = new NormalizedLevenshtein();
			Damerau d = new Damerau();
			int mindist = 10000;
			
			
			String most = "";
			// Start analyze 
			for(TwitterTopEntityBuckets t :temp )
			{
				double ts[] = new double[t.getTimelineBuckets().length];
				
				for(int i = 0; i< t.getTimelineBuckets().length;i++ )
				{
					ts[i]= t.getTimelineBuckets()[i].getDoc_count();
					
				}
				
				try {
					int dist = 0;
					SAXRecords str = sp.ts2saxByChunking(ts, 20, na.getCuts(20), 0.001);
					String s = str.getSAXString("");
					System.out.println(t.key + " ==> " + s);
				
					
					if(stocompare.isEmpty())
					{
						NeedlemanWunsch NW = new NeedlemanWunsch(stocompare, s);    
						dist = StringUtils.getLevenshteinDistance(stocompare, s);
//						System.out.println(h + " to " + t.key + "," + dist +","+ l.distance(stocompare, s)+"," +ln.distance(stocompare, s)+ "," +  d.distance(stocompare, s)+"," + jw.distance(stocompare, s) +","+NW.compare());
//						System.out.println(h + " to " + t.key + " Levenshtein2: " +);
//						System.out.println(h + " to " + t.key + " Normalised Levenshtein2: " + );
//						System.out.println(h + " to " + t.key + " DL: " +);
//						System.out.println(h + " to " + t.key + " JaroWinkler: " + );
//						
					}
					if(stocompare.isEmpty())
					{
						h = t.key ;
						stocompare = s;
						System.out.println(h + " : " + stocompare);
					}
					if(dist!=0 && dist < mindist)
					{
						mindist = dist;
						most = s;
					}
					
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			System.out.println(most + " : " + mindist);
			return temp;
		
		
class NeedlemanWunsch{
	   private int[][] E;
	   private String n, m;
	  
	   public NeedlemanWunsch(String a, String b){
	        n = a; m = b;
	        E = new int[n.length()+1][m.length()+1];
	        initialisiere();        
	   }
	   
	   public void initialisiere(){
	      // Starteintrag wird auf 0 gesetzt
	      E[0][0] = 0;
	      
	      // fülle die erste Zeile und erste Spalte mit 0-en
	      for (int i=1; i<=n.length(); i++)
	         E[i][0] = 0;            
	      for (int j=1; j<=m.length(); j++)
	         E[0][j] = 0;    
	   }
	   
	   private int cost(char a, char b){
	      if (a==b) return 1;
	      else return 0;
	   }
	   
	   public int compare(){
	      for (int i=1; i<=n.length(); i++)
	         for (int j=1; j<=m.length(); j++)
	            E[i][j] = Math.max(E[i-1][j-1] 
	                         + cost(n.charAt(i-1), m.charAt(j-1)), 
	                      Math.max(E[i-1][j], E[i][j-1]));
	      return E[n.length()][m.length()];                                           
	   }
	   
}

		}
		*/
		
		public static TwitterTopEntityBuckets[]  fromESDashboardResponse(String esResponse, TwitterSearchRequest request) {
			
			ObjectNode esRoot = null;
			try {
				esRoot = (ObjectNode)TwitterESResponseUtils.jsonMapper.readTree(esResponse);
			} catch(Exception e) {
				throw new RuntimeException("Can't read json es reponse", e);
			}
			JsonNode esErrorNode = esRoot.get("error");
			if(esErrorNode == null) {
				if(esRoot.get("aggregations").size() > 0)
					return TwitterESResponseUtils.getDashboardFromResponse(esRoot.get("aggregations"), request.getDashboard_type() );
			}
		
			return null;
			
			
			
		}
		
		
		
		public static TwitterTopEntityBuckets[] fromESWordCloudResponse(String esResponse, TwitterSearchRequest request) {
			
			ObjectNode esRoot = null;
			try {
				esRoot = (ObjectNode)TwitterESResponseUtils.jsonMapper.readTree(esResponse);
			} catch(Exception e) {
				throw new RuntimeException("Can't read json es reponse", e);
			}
			
			JsonNode esErrorNode = esRoot.get("error");
			if(esErrorNode == null) 
				return TwitterESResponseUtils.getDashboardFromResponse(esRoot.get("aggregations"),"tagcloud"); 
			

			return null;
	
			
		}
		
		public static String fromESIramuteqResponse(String esResponse, TwitterSearchRequest request) {
			
			ObjectNode esRoot = null;
			String filename = "iramuteque_" + String.valueOf( new Date().getTime());
			try {
				esRoot = (ObjectNode)TwitterESResponseUtils.jsonMapper.readTree(esResponse);
			} catch(Exception e) {
				throw new RuntimeException("Can't read json es reponse", e);
			}
			
			HashMap<String,String> irainput = new HashMap<String,String>();
			JsonNode esErrorNode = esRoot.get("error");
			long totalhits = esRoot.get("hits").get("total").asLong();
			StringBuilder sb = new StringBuilder();
			
			if(esErrorNode == null) {
				
				
				TwitterSearchHitsResponse[] hits =  TwitterESResponseUtils.getHitsFromResponse(esRoot);
				for(TwitterSearchHitsResponse hit : hits)
				{
					 Entry<String, String> ent = TwitterESResponseUtils.getIramuteqTextfromHit(hit, request.getIravarfields(), request.getIrasettings() );
					 String exval = irainput.get(ent.getKey());
					 if(ent.getValue().isEmpty())
					 {
						 if(exval!=null)
							 irainput.put(ent.getKey(), exval + " " + ent.getValue());	 
						 else
							 irainput.put(ent.getKey(), ent.getValue());
					 }
					
				}
				
				
				for(Entry<String, String> ent : irainput.entrySet() )
				{
					sb.append(ent.getKey());
					sb.append("\\n");
					sb.append(ent.getValue());
					sb.append("\\n");
					sb.append("\\n");
					
				}
				
				
//				BufferedWriter writer;
//				try {
//					writer = new BufferedWriter(new FileWriter("C://Users//zpehlivan//" + filename));
//					writer.write(sb.toString());
//					writer.flush();
//					
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			   
			}
			
		//	return "{ \"content\":\"" + sb.toString() + "\"}";
			return "{\"name\":\"" + filename +  "\",  \"content\":\"" + sb.toString() +  "\"}";

			
		}
		
		
public static String fromESNetworkResponse(String esResponse, TwitterSearchRequest request) {
			
			ObjectNode esRoot = null;
			String filename = "network_" + String.valueOf( new Date().getTime()) + ".gexf";
			try {
				esRoot = (ObjectNode)TwitterESResponseUtils.jsonMapper.readTree(esResponse);
			} catch(Exception e) {
				throw new RuntimeException("Can't read json es reponse", e);
			}
			StaxGraphWriter graphWriter = new StaxGraphWriter();
	        StringWriter stringWriter = new StringWriter();
	
			JsonNode esErrorNode = esRoot.get("error");
			
			if(esErrorNode == null ) {
				Gexf gexf = new GexfImpl();
				gexf.setVisualization(true);
				gexf.getMetadata()
	                .setCreator("Dlweb@ina")
	                .setDescription("A Twitter network");
				
				Graph graph = gexf.getGraph();
		        graph.
		                setDefaultEdgeType(EdgeType.DIRECTED)
		                
		                .setMode(Mode.STATIC)
		                .setTimeType(TimeFormat.XSDDATETIME);
		        
		        
		        AttributeList attrList = new AttributeListImpl(AttributeClass.NODE);
		        AttributeList edgeattrList = new AttributeListImpl(AttributeClass.EDGE);
		        graph.getAttributeLists().add(attrList);
		        graph.getAttributeLists().add(edgeattrList);

		        Attribute attType = attrList.createAttribute("0", AttributeType.STRING, "type");

		        Attribute attEdgeType = edgeattrList.createAttribute("0", AttributeType.STRING, "edgetype");
		        
				TwitterSearchHitsResponse[] hits =  TwitterESResponseUtils.getHitsFromResponse(esRoot);
				
				Position pos = new PositionImpl().setX(0).setY(0).setZ(0);
				Color	colorutilisateur = new ColorImpl().setB(0).setG(50).setR(0);
				Color	colorhashtag = new ColorImpl().setB(0).setG(0).setR(50);
				Color	colorurl = new ColorImpl().setB(50).setG(0).setR(0);
			

				
				for(TwitterSearchHitsResponse hit : hits)
				{
					 
						 Node temp = graph.createNode(hit.get_source().getUserScreenName().toLowerCase());
						 Node temptarget;  
						 Edge tempedge;
						 temp.setLabel(hit.get_source().getUserScreenName().toLowerCase())
						 	.getAttributeValues()
						 	.addValue(attType, "utilisateur");
						 temp.setColor(colorutilisateur).setSize(2).setPosition(pos);
						 System.out.println(hit.get_source().getUserScreenName().toLowerCase());
						 if(hit.get_source().retweeted)
						 {
							 System.out.println(" => " + hit.get_source().getRetweetScreenName().toLowerCase());
							 Node retweet = graph.createNode(hit.get_source().getRetweetScreenName().toLowerCase());
							 retweet.setColor(colorutilisateur);
							 retweet.setLabel(hit.get_source().getRetweetScreenName().toLowerCase())
							 	.getAttributeValues()
							 	.addValue(attType, "utilisateur");
							 retweet.setColor(colorutilisateur).setSize(2).setPosition(pos);
							 try{
								 tempedge = temp.connectTo(retweet);
								 tempedge.getAttributeValues().addValue(attEdgeType, "RT");
							 }
							 catch(Exception e)
							 {
								 System.out.println("retweeted");
								 
							 }
						 }
						 
						 if(hit.get_source().quoted)
						 {
							 System.out.println(" => " + hit.get_source().getRetweetScreenName().toLowerCase());
							 temptarget = graph.createNode(hit.get_source().getRetweetScreenName().toLowerCase());
							 temptarget.setLabel(hit.get_source().getRetweetScreenName().toLowerCase())
							 	.getAttributeValues()
							 	.addValue(attType, "utilisateur");
							 temptarget.setColor(colorutilisateur).setSize(2).setPosition(pos);
							 try{
								 tempedge = temp.connectTo(temptarget);
								 tempedge.getAttributeValues().addValue(attEdgeType, "QT");
							 }
							 catch(Exception e)
							 {
								 System.out.println("quoted");
								 
							 }
						 }
						 for (String h : hit.get_source().getHashtags())
						 {
							 System.out.println(" => " + h.toLowerCase());
							 temptarget = graph.createNode("#"+h.toLowerCase());
							 temptarget.setLabel("#"+h.toLowerCase())
							 	.getAttributeValues()
							 	.addValue(attType, "hashtag");
							 temptarget.setColor(colorhashtag).setSize(2).setPosition(pos);
							 try{
								 tempedge = temp.connectTo(temptarget);
								 tempedge.getAttributeValues().addValue(attEdgeType, "hashtaglink");
							 }
							 catch(Exception e)
							 {
								 System.out.println("hashtag");
								
							 }
						 }
						 
						 for (String h : hit.get_source().mentions)
						 {
							 System.out.println(" => " + h.toLowerCase());
							 temptarget = graph.createNode(h.toLowerCase());
							 temptarget.setLabel(h.toLowerCase())
							 	.getAttributeValues()
							 	.addValue(attType, "utilisateur");
							 temptarget.setColor(colorutilisateur).setSize(2).setPosition(pos);
							 try{	 
								 tempedge = temp.connectTo(temptarget);
								 tempedge.getAttributeValues().addValue(attEdgeType, "mention");
							 }
							 catch(Exception e)
							 {
								 System.out.println("mentions");
								 
							 }
						 }
						 
//						 for (String h : hit.get_source().urlslist)
//						 {
//					
//							 temptarget = graph.createNode(h);
//							 temptarget.setLabel(h)
//							 	.getAttributeValues()
//							 	.addValue(attType, "host");
//							 temptarget.setColor(colorurl).setSize(2).setPosition(pos);
//							 try{	 
//								 tempedge = temp.connectTo(temptarget);
//								 tempedge.getAttributeValues().addValue(attEdgeType, "linkurl");
//							 }
//							 catch(Exception e)
//							 {
//								 System.out.println("urlslist");
//							 
//							 }
//						 }
//						 
//						 
						 
					
				}
				
				
				
		        try {
					graphWriter.writeToStream(gexf, stringWriter, "UTF-8");
//					FileWriter fw = new FileWriter(filename);
//			        fw.write(stringWriter.toString());
//			        fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			   
			}
			return "{\"name\":\"" + filename +  "\",  \"content\":\"" + StringEscapeUtils.escapeJson( stringWriter.toString()) +  "\"}";
	
			
			
		}
}
