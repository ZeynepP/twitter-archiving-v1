package fr.ina.dlweb.twitter.crawler.apis;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

import fr.ina.dlweb.twitter.commons.io.TwitterWriter;
import fr.ina.dlweb.twitter.commons.utils.Utils;
import fr.ina.dlweb.twitter.crawler.utils.SettingsCrawler;
import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;
import twitter4j.User;

public class TwitterAPIRelations  extends ITwitterAPIs {

	public TwitterAPIRelations()
	{
			super();
			twitter = getTwitterAPI();
    		Log = LoggerFactory.getLogger(TwitterAPIRelations.class);
			
	}
    
    // however it is calling just one function i need it for other classes like trends
	public boolean runAPI( )
	{
		return getTweets();

	}
	

	public long getUserFriendsList(Twitter twitter, String screenname,TwitterWriter twriter)
	{
		long cursor =  ( this.offsets.get(screenname) == null ?  -1 :Long.valueOf( this.offsets.get(screenname)));
		PagableResponseList<User> users = null;
		long keepmaxid = cursor;
		
		
		try{
				do {
					if(SettingsCrawler.archiveMethod.equals(SettingsCrawler.FRIENDS))
						users = twitter.getFriendsList(screenname, cursor,200 );
					else
						users =  twitter.getFollowersList(screenname, cursor,200);
					keepmaxid = cursor;
		            cursor = users.getNextCursor();
		            for (User tweet : users) {
		            	
		            	try
		            	{
		            		twriter.writeTweet(TwitterObjectFactory.getRawJSON(tweet), String.valueOf(tweet.getId()));
		            		
		            	} catch (IOException e) {
		            		
		            		Log.error(Utils.convertToJsonMessage(this.applicationName , Utils.createJsonLogMessage( "error", "error", "TwitterAPIFriendsList : write getUserFriendsList " + e.toString(),1)));
						
						}
		            	
		            	
		            }
		            
		          System.out.print("f");
		          
			    } while (cursor != 0);
		}
		catch(TwitterException te)
		{
			
				String msgerror = Utils.convertToJsonMessage(this.applicationName,Utils.createJsonLogMessage( "error", "exception", "TwitterAPIFriendsList : getTweets getUsertimeline " + te.toString(), 1));
				Log.error(msgerror);
				
		}
		
			
		String msg = Utils.convertToJsonMessage(this.applicationName, Utils.createJsonLogMessage( "info", "crawled",  screenname,  twriter.getSizeofTweets()));
		Log.info(msg);
		
		return keepmaxid; 
		
		
	}

	
	public boolean getTweets()
	{
		boolean isok = true;
		
		long keepmaxid;
		try{
			
			
			for(String screenname : keystoArchive)
			{
				System.out.print(screenname);		
		
				try {
					activeWriter  = new TwitterWriter(SettingsCrawler.archiveMethod, SettingsCrawler.currentFolder,SettingsCrawler.doneFolder,SettingsCrawler.fileNameTimeFormat, null, this.infoFile, SettingsCrawler.mustLock, SettingsCrawler.movetoDone, SettingsCrawler.writerMaxLines );	
					System.out.print("<");
					
				} catch (Exception e) {
					Log.error( Utils.convertToJsonMessage(SettingsCrawler.applicationName,  Utils.createJsonLogMessage("error", "error","Not able to cerate writer" + Throwables.getStackTraceAsString(e), 1)));
				}
				
				
				System.out.print("<");
				keepmaxid = getUserFriendsList(twitter, screenname,activeWriter);

				this.offsets.put(screenname,String.valueOf( keepmaxid));
			//	Utils.writeHashMap(this.offsetfile, false, this.offsets, true);
				activeWriter.closeWriter();
				System.out.print(">");
			}
			
		}
		catch(Exception ex)
		{
			
			System.out.println(ex.getMessage());
			Log.error( Utils.convertToJsonMessage(this.applicationName,Utils.createJsonLogMessage( "error", "exception", "TwitterAPIFriendsList : getTweets " + ex.toString(), 1)));
		
			isok = false;
		}
		finally{
			try {
				Utils.writeHashMap(this.offsetfile, false, this.offsets, true);
			} catch (FileNotFoundException ex) {
				Log.error(Utils.convertToJsonMessage(this.applicationName,Utils.createJsonLogMessage( "error", "error", "TwitterAPIFriendsList : writeHashMap " + ex.toString(), 1)));

			}
		}
		
		return isok;
	}
	
	



}



