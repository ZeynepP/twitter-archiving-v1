package fr.ina.dlweb.dowser.ws.twitter.model;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jackson.JsonNode;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;



@SuppressWarnings("serial")
public class TweetMetaReloaded implements Serializable {
	
	//SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

	// INA related data fields
	public long archived_at;
	public long indexed_at;
	public String source_type[];
	public String method_archive[];
	public String source_path[];
	public String  collection[];
	
	public int[] is_extended;
	public int possibly_sensitive = 0;


	public  String created_at;
	public String id;
	
	public  String text;
	public  String full_text; // Twitter updates data 23 03 2017, we also use for enriched text qt etc.
	
	
	public  long  timestamp_ms;
	public  String  filter_level;
	public  boolean retweeted;
	public  boolean quoted;
	public  boolean favorited;
	public  String lang;

	
	public RetweetedQuatedStatus retweeted_status = null;

	public RetweetedQuatedStatus quoted_status = null;	
	

	
	public  String in_reply_to_user_id;
	public  String in_reply_to_screen_name;
	public  String in_reply_to_status_id;
	public int retweet_count;
	public int favorite_count;
	public String source; 

	
	public Coordinates coordinates;
	public Place place;
	public User user;
	
	private  User_mentions user_mentions[];
	private  Urls urls[];
	private String hashtags[];
	public String urlslist[];
	public String mentions[];
	public Media media[];
	public String symbols[];
	public String emojis[];
	
    public String getUserScreenName()
    {
    	return user.screen_name;
    }
    
    public String getRetweetScreenName()
    {
    	
    	if(retweeted_status!=null)
    		return retweeted_status.user.screen_name;
    	else return id;
    }
	

	public String getCreated_at() {
		return created_at;
	}




	public void setCreated_at(String created_at) {
	   
		//this.created_at  = TwitterSettings.d.format(new Date(Long.parseLong(created_at)));
		this.created_at = created_at;
	}

	public User_mentions[] getUser_mentions() {
		return user_mentions;
	}

	public void setUser_mentions(User_mentions user_mentions[]) {
		
		if(user_mentions==null)
		{
			this.user_mentions = new User_mentions[0];
		}
		else
		{
			this.user_mentions = user_mentions;
		}
		// need it for iramuteq to celan the text
		
		this.mentions = new String[this.user_mentions.length];
		for(int i= 0;i<this.user_mentions.length;i++)
		{
			mentions[i] = this.user_mentions[i].screen_name;
		}
		
	}

	public String[] getHashtags() {
		return hashtags;
	}

	public void setHashtags(String hashtags[]) {
		if(hashtags==null)
		{
			this.hashtags = new String[0];
		}
		else
		{
			this.hashtags = hashtags;
		}
	
	}

	public Urls[] getUrls() {
		return urls;
	}

	public void setUrls(Urls urls[]) {
		this.urls = urls;
		if(urls==null)
		{
			this.urlslist = new String[0];
		}
		else
		{
			this.urlslist = new String[this.urls.length];
			for(int i= 0;i<this.urls.length;i++)
			{
				urlslist[i] = this.urls[i].host_url;
			}
		}
	}

	public Place getPlace() {
		return place;
	}

	public void setPlace(Place place) {
		if(place != null)
			this.place = place;
		else
		{
			Place p = new Place();
			p.country = "non définie";
			p.name = "non définie";
			p.place_type = "non définie";
			this.place = p;
		}
	}

}
class RetweetedQuatedStatus
{
	

	public  String created_at;
	public String id;
	public User user;
	public String text;
	public String full_text;
	public int possibly_sensitive = 0;
	
	
}



class Score{
	public int retweet_count;
	public int favorite_count;
}

class Place{
	
	public String country;
	public String country_code;
	public String name;
	public String place_type;
	public String id;
	public JsonNode bounding_box;

}

class Urls{
	public String url;
	public  String expanded_url;
//	public  String domain;
//	public  String suffix;
	public  String host_expanded_url;
	public  String host_url;

//	public  String path;
	
	@Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
            // if deriving: appendSuper(super.hashCode()).
            append(url).
            toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
       if (!(obj instanceof Urls))
            return false;
        if (obj == this)
            return true;

        Urls rhs = (Urls) obj;
        return new EqualsBuilder().
            // if deriving: appendSuper(super.equals(obj)).
            append(url, rhs.url).
            isEquals();
    }
}




class User {
	
	
	public  int statuses_count;
	public  int followers_count;
	public  int listed_count;
	public  int friends_count;
	public  int favourites_count;
	public  String description;
	public  String location;
	public  String id;
	public  String lang;
	public String created_at;
	public String name;
	public  String screen_name;
	//	public String name_raw;
	//	public  String screen_name_raw;
	public  String profile_image_url;
	public  String url;
	public int verified = 0;
	
}


class In_reply_to
{
	public  String screen_name;
	//	public  String screen_name_raw;
	public  String id;
	public  String user_id;
}


class User_mentions
{
	public  String name;
	public  String screen_name;
	//public  String screen_name_raw;
	public  String id;
	 @Override
	    public int hashCode() {
	        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
	            // if deriving: appendSuper(super.hashCode()).
	            append(screen_name).
	            toHashCode();
	    }
	
	    @Override
	    public boolean equals(Object obj) {
	       if (!(obj instanceof User_mentions))
	            return false;
	        if (obj == this)
	            return true;
	
	        User_mentions rhs = (User_mentions) obj;
	        return new EqualsBuilder().
	            // if deriving: appendSuper(super.equals(obj)).
	            append(screen_name, rhs.screen_name).
	            isEquals();
	    }
	
	
}

class Media
{
	public  String type;
	public  String expanded_url;
	public  String media_url;
	public  String host_expanded_url;
	public  String host_media_url;
	public  String id;
	public JsonNode video_info;
	
	@Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
            // if deriving: appendSuper(super.hashCode()).
            append(type).append(media_url).
            toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
       if (!(obj instanceof Media))
            return false;
        if (obj == this)
            return true;

        Media rhs = (Media) obj;
        return new EqualsBuilder().
            // if deriving: appendSuper(super.equals(obj)).
            append(type, rhs.type).append(media_url, rhs.media_url).
            isEquals();
    }
}


class Video_info 
{
	public long duration_millis;
	public Variants[] variants;

}
class Variants
{
	public String url;
	public String content_type;
	
}

class Coordinates
{
	
	 public String type;
	 public JsonNode coordinates;
	
}