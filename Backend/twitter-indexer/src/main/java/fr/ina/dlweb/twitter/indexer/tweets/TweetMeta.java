package fr.ina.dlweb.twitter.indexer.tweets;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vdurmont.emoji.Fitzpatrick;

import fr.ina.dlweb.twitter.commons.utils.Utils;
import fr.ina.dlweb.twitter.indexer.utils.CustomJsonDateDeserializer;
import fr.ina.dlweb.twitter.indexer.utils.Settings;
import fr.ina.dlweb.twitter.indexer.utils.UtilsIndexer;
import fr.ina.dlweb.utils.URLUtils;




@SuppressWarnings("unused")
//TODO remove this class once everything is ok: it could be used to do mapping with gson
public class TweetMeta implements Serializable {
	

	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	
	// INA related data fields
	private long archived_at;
	private long indexed_at;
	private String source_type[];
	private String method_archive[];
	private String source_path[];
	private String  collection[];
	private int[]  is_extended;
	
	
	@JsonDeserialize(using = CustomJsonDateDeserializer.class)
	private  String created_at;
	private String id;
	
	private  String text;
	private  String full_text; // Twitter updates data 23 03 2017, we also use for enriched text qt etc.
	
	
	private  long  timestamp_ms;
	private  String  filter_level;

	
	@JsonDeserialize(using =NumericBooleanDeserializer.class)
	private  int retweeted ;
	@JsonDeserialize(using =NumericBooleanDeserializer.class)
	private  int quoted ;
	@JsonDeserialize(using =NumericBooleanDeserializer.class)
	private  int favorited;
	@JsonDeserialize(using =NumericBooleanDeserializer.class)
	private  int possibly_sensitive = 0;
	
	
	
	private  String lang;
	

	@JsonIgnore
	private JsonNode retweeted_status = null;
	@JsonIgnore
	private JsonNode quoted_status = null;	
	
	@JsonProperty("retweeted_status")
	private RetweetedQuatedStatus retweeted_tweet = null;
	@JsonProperty("quoted_status")
	private RetweetedQuatedStatus quoted_tweet = null;
	
	
	private  String in_reply_to_user_id;
	private  String in_reply_to_screen_name;
	private  String in_reply_to_status_id;
	private int retweet_count;
	private int favorite_count;
	private String source; 

	
	private Coordinates coordinates;
	private Place place;
	private User user;
	
	
	@JsonIgnore private Set<User_mentions> mentions_set;
	@JsonIgnore private Set< Urls> urls_set;
	@JsonIgnore private Set<String> hashtags_set;
	@JsonIgnore private Set<Media> media_set;
	@JsonIgnore private Set<String> symbols_set;
	@JsonIgnore private Set<String> emojis_set;
	
	public User_mentions user_mentions[];
	public Urls urls[];
	public String hashtags[];
	public Media media[];
	public String symbols[];
	public String emojis[];
	
	
	
	public String getCreated_at() {
		return created_at;
	}

	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}


	// INA related data fields



	public long getArchived_at() {
		return archived_at;
	}

	public void setArchived_at(long archived_at) {
		this.archived_at = archived_at;
	}

	

	public long getIndexed_at() {
		return indexed_at;
	}

	public void setIndexed_at(long indexed_at) {
		this.indexed_at = indexed_at;
	}

	public String[] getSource_type() {
		return source_type;
	}

	public void setSource_type(String source_type) {
		this.source_type =  new String[]{source_type};
	}

	public String[] getMethod_archive() {
		return method_archive;
	}

	public void setMethod_archive(String method_archive) {
		this.method_archive =  new String[]{method_archive};
	}

	public String[] getSource_path() {
		return source_path;
	}

	public void setSource_path(String source_path) {
		this.source_path = new String[]{source_path};
	}
	// INA related data fields

	

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}



	public String getFull_text() {
		return full_text;
	}

	public void setFull_text(String full_text) {
		this.full_text = full_text;
	}

	public long getTimestamp_ms() {
		return timestamp_ms;
	}

	public void setTimestamp_ms(long timestamp_ms) {
		this.timestamp_ms = timestamp_ms;
	}

	public String getFilter_level() {
		return filter_level;
	}

	public void setFilter_level(String filter_level) {
		this.filter_level = filter_level;
	}


	

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}



	public String getIn_reply_to_user_id() {
		return in_reply_to_user_id;
	}

	public void setIn_reply_to_user_id(String in_reply_to_user_id) {
		this.in_reply_to_user_id = in_reply_to_user_id;
	}

	public String getIn_reply_to_screen_name() {
		return in_reply_to_screen_name;
	}

	public void setIn_reply_to_screen_name(String in_reply_to_screen_name) {
		this.in_reply_to_screen_name = in_reply_to_screen_name;
	}

	public String getIn_reply_to_status_id() {
		return in_reply_to_status_id;
	}

	public void setIn_reply_to_status_id(String in_reply_to_status_id) {
		this.in_reply_to_status_id = in_reply_to_status_id;
	}

	public int getRetweet_count() {
		return retweet_count;
	}

	public void setRetweet_count(int retweet_count) {
		this.retweet_count = retweet_count;
	}

	public int getFavorite_count() {
		return favorite_count;
	}

	public void setFavorite_count(int favorite_count) {
		this.favorite_count = favorite_count;
	}

	public Place getPlace() {
		return place;
	}

	public void setPlace(Place place) {
		this.place = place;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}


	public Coordinates getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(Coordinates coordinates) {
		this.coordinates = coordinates;
	}

	@JsonIgnore
	public JsonNode getRetweeted_status() {
		return retweeted_status;
	}
	
	@JsonProperty
	public void setRetweeted_status(JsonNode retweeted_status) {
		this.retweeted_status = retweeted_status;
	}
	
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = UtilsIndexer.html2text(source);
		
	}
	
	@JsonIgnore
	public JsonNode getQuoted_status() {
		return quoted_status;
	}
	@JsonProperty
	public void setQuoted_status(JsonNode quoted_status) {
		this.quoted_status = quoted_status;
	}


	
	public void update(JsonNode json)
	{

		// we are using retweeted and quoted to say if tweet is a retweet or quote not for if this tweet is retweeted 
		// it can be done by number of retweets > 0 
		
		setRetweeted(0);
		setQuoted(0);
		
		
		if(this.text == null)
			this.text = this.full_text;

		if(this.full_text == null)
			this.full_text = this.text;
		
		
		mergeGeo(json);
		
		//Top-level entities for Retweets are consistent with the original Tweet ones. We strongly recommend using entities from the original retweeted_status,
		//The Retweet text attribute is composed of the original Tweet text with “RT @username: ” prepended. If the display character count then exceeds 140 characters, this text is truncated and an ellipsis “…” is added. Consequently, some top-level entities might be incorrect or missing, for instance in the case of a truncated hashtag entry.
		
		// entities and extended_entities + entities from extended_tweet
		// https://twitter.com/Chedon18/status/844549956140765184
		
		if(json.has("extended_tweet"))
		{
			this.full_text = json.get("extended_tweet").get("full_text").asText(); // has only full_text no text https://developer.twitter.com/en/docs/tweets/tweet-updates
			this.text = this.full_text ;
			collectEntities(json.get("extended_tweet"));
		}
		
		
		collectEntities(json);
		collectEmojis();
		
		
		//problem metadata with quoted and retweeted getting just retweeted
		if(retweeted_status!=null)
		{
			setRetweeted(1);
			collectEntities(json.get("retweeted_status"));
			// need to do this unless text is null for new tweets
			if(retweeted_status.get("text") == null)
				((ObjectNode)retweeted_status).put("text", retweeted_status.get("full_text").asText() );
			

			if(retweeted_status.get("full_text") == null)
				((ObjectNode)retweeted_status).put("full_text", retweeted_status.get("text").asText() );
			
			
			
		}
		
		if(quoted_status!=null)
		{
			setQuoted(1);
			// need to do this unless text is null for new tweets
			if(quoted_status.get("text") == null)
				((ObjectNode)quoted_status).put("text", quoted_status.get("full_text").asText() );
			

			if(quoted_status.get("full_text") == null)
				((ObjectNode)quoted_status).put("full_text", quoted_status.get("text").asText() );
			
			
			this.full_text = this.full_text  + " :QT: " +  quoted_status.get("text").asText();//OR FULL8TEXT, 
		}	
		

		convertEntities();
		
	}
	
	
	private void convertEntities()
	{
		if(hashtags_set!= null)
			hashtags = hashtags_set.toArray(new String[hashtags_set.size()]);
		if(symbols_set != null)
			symbols = symbols_set.toArray(new String[symbols_set.size()]);
		if(emojis_set != null)
			emojis = emojis_set.toArray(new String[emojis_set.size()]);
		if(mentions_set != null)
			user_mentions = mentions_set.toArray(new User_mentions[mentions_set.size()]);
		if(media_set != null)
			media = media_set.toArray(new Media[media_set.size()]);
		if(urls_set != null)
			urls = urls_set.toArray(new Urls[urls_set.size()]);
		
		
	}
	
	
	private void mergeGeo(JsonNode json)
	{
		
		//it is too complicated with geo 
		// geo  lat, long
		// coordinates long, lat 
		// bounding_box in place = geo_shape
		// a tweet can have  bounding_box without geo/coord and vice-versa  so we do some manipulations 
		// "coordinates": null,
		// "place": null,
		if(json.get("coordinates").isNull() && !json.get("place").isNull() && !json.get("place").get("bounding_box").isNull())
		{
			try{
				JsonNode bounding = this.place.getBounding_box();
				this.coordinates = new Coordinates();
				this.coordinates.type="Point";
				this.coordinates.coordinates = UtilsIndexer.getCoordinatesFromPlace(bounding.get("coordinates"));
			}
			catch(Exception ex)
			{
				this.coordinates.coordinates = null;
				//Log.debug( Utils.convertToJsonMessage(Settings.applicationname,  Utils.createJsonLogMessage("debug", "exception", "No place or coord  " + this.id + " - " + ex , 1)));
				
			}
			
		}

	}
	

	@SuppressWarnings("unchecked")
	private void collectEntities(JsonNode jsonentities)
	{
		for (String ent : Settings.ENTITIES_TYPES)
		{
			for (String type : Settings.ENTITIES_OBJECTS) {
				JsonNode objNode = jsonentities.get(ent)!=null?jsonentities.get(ent).get(type):null;
				if(objNode!=null){
						if(type.equals( "urls"))
							collectUrls(objNode);
						else if(type.equals("media" ))
							collectMedias(objNode);
						else if(type.equals( "user_mentions"))
							collectMentions(objNode);
						else if(type.equals( "hashtags"))
							collectHashtags(objNode);
						else if(type.equals( "symbols"))
							collectSymbols(objNode);
					}
			}
					
		}

	}
	
	
	
	private void collectMentions(JsonNode objNode)
	{
	
		if(mentions_set ==null)
			mentions_set = new HashSet<User_mentions>();
		
        for (final JsonNode obj : objNode) {

	        	User_mentions mention = new User_mentions();
	        	mention.id = obj.get("id").asText();
	        	mention.name = obj.get("name").asText();
	        	mention.screen_name = obj.get("screen_name").asText();
	        	mentions_set.add( mention);
	        	
        	
        }
		
	}
	
	private void collectHashtags(JsonNode objNode)
	{
		
		if(hashtags_set ==null)
			hashtags_set = new HashSet<String>();
		
        for (final JsonNode obj : objNode) 
        	hashtags_set.add(obj.get("text").asText());
        
		
	}
	private void collectSymbols(JsonNode objNode)
	{
		if(symbols_set == null )
			symbols_set = new HashSet<String>();
	
		for (final JsonNode obj : objNode) 
			symbols_set.add( obj.get("text").asText());
   
        
		
	}
	
	// Il y a des bugs dans les metadonnées  https://twitter.com/PorcherThomas/status/844855200347185153
//	 "urls": [
//	          {
//	            "url": "",
//	            "expanded_url": null,
//	            "indices": [
//	              136,
//	              136
//	            ]
//	          }
	
	private void collectUrls(JsonNode objNode)
	{
		if(urls_set ==null)
			urls_set = new HashSet<Urls>();
		
		for (final JsonNode obj : objNode) {
        	Urls med = new Urls();
        	med.expanded_url = obj.get("expanded_url").asText();
        	med.url =  obj.get("url").asText();
        	String url = med.url;
        	// old tweets do not contain http ex: id : 227157562628403200  "url": "t.co/77sKQeBO"
			if(!url.startsWith("http"))
					url = "http://" + url;
        	try{
	
	   			 if( Settings.unshortenUrls)
	   			 {	 
	   				 med.expanded_url = UtilsIndexer.expand(url);
	   				 med.host_url = URLUtils.getHost(url);
	   				 med.host_expanded_url = URLUtils.getHost(med.expanded_url);
	   			 }
	   			 else
	   			 {
	   				 med.host_url = URLUtils.getHost(url);
	   				 med.host_expanded_url = URLUtils.getHost(med.expanded_url);
	   			 }
	   		     
	   		    
	   		}
	   	    catch(Exception ex)
	   		{
	   	    		med.host_url = "";
	   	    		med.host_expanded_url = "";
	   		}
        	
        	if(!med.url.isEmpty())
        		urls_set.add( med);
     
        }
		
		
	}
	
	private void collectMedias(JsonNode objNode)
	{

		if(media_set ==null )
			media_set = new HashSet<Media>();

		
		for (final JsonNode obj : objNode) {
			
        	Media med = new Media();
        	med.id = obj.get("id").asText();
        	med.expanded_url = obj.get("expanded_url").asText();
        	med.media_url =  obj.get("media_url").asText();
        	med.type =  obj.get("type").asText();
        	med.video_info =  obj.get("video_info");
        
        	String url = med.media_url;
        	// old tweets do not contain http ex: id : 227157562628403200  "url": "t.co/77sKQeBO"
			if(!url.startsWith("http"))
					url = "http://" + url;
			med.host_media_url = URLUtils.getHost(url);
			
			url = med.expanded_url;
			if(!url.startsWith("http"))
					url = "http://" + url;
			med.host_expanded_url = URLUtils.getHost(url);
			
        	media_set.add(med);
        	
        }
		
		
	}
	
	
	private void collectEmojis()
	{
		if(emojis_set ==null )
			emojis_set = new HashSet<String>();
		 
			    char[] inputCharArray = text.toCharArray();
			    int counter = 0;
			    for (int i = 0; i < text.length(); i++) {
			      int emojiEnd = UtilsIndexer.getEmojiEndPos(inputCharArray, i);
			      String emoji;
			      if (emojiEnd != -1) {
			    	  	emoji = text.substring(i, emojiEnd);
			    	  	int count = 0;
			    	  
			    	  		String unicode = text.substring(i, emojiEnd);
			    	        int stringLength = unicode.length();
			    	        String[] pointCodesHex = new String[stringLength];

			    	        for (int offset = 0; offset < stringLength; ) {
			    	          final int codePoint = unicode.codePointAt(offset);
			    	          pointCodesHex[count++] = String.format("%x-", codePoint);
			    	          offset += Character.charCount(codePoint);
			    	        }


			    	        emojis_set.add( UtilsIndexer.stringJoin(pointCodesHex, count));
			    	        counter ++;
			    	
							  
							String fitzpatrickString = (emojiEnd + 2 <= text.length()) ? new String(inputCharArray, emojiEnd, 2) : null;
							
							Fitzpatrick f = Fitzpatrick.fitzpatrickFromUnicode(fitzpatrickString);
							
							i = i + unicode.length() + (f != null ? 2 : 0) - 1;
			        
			      }
			    }


			  
		
	}

	
	public String[] getCollection() {
		return collection;
	}

	/*public void setCollection(String collection[]) {
		this.collection = collection;
	}*/
	
	public int[] getIs_extended() {
		return is_extended;
	}

	public void setIs_extended(int is_extended) {
		this.is_extended = new int[]{is_extended};
	}

	public void setCollection(String collection) {
		this.collection = new String[]{collection};
		
	}
	

	public RetweetedQuatedStatus getRetweeted_tweet() {
		return retweeted_tweet;
	}

	public void setRetweeted_tweet(RetweetedQuatedStatus retweeted_tweet) {
		this.retweeted_tweet = retweeted_tweet;
	}

	public RetweetedQuatedStatus getQuoted_tweet() {
		return quoted_tweet;
	}

	public void setQuoted_tweet(RetweetedQuatedStatus quoted_tweet) {
		this.quoted_tweet = quoted_tweet;
	}

	public int getRetweeted() {
		return retweeted;
	}

	public void setRetweeted(int retweeted) {
		this.retweeted = retweeted;
	}

	public int getQuoted() {
		return quoted;
	}

	public void setQuoted(int quoted) {
		this.quoted = quoted;
	}

	public int getFavorited() {
		return favorited;
	}

	public void setFavorited(int favorited) {
		this.favorited = favorited;
	}

	public int getPossibly_sensitive() {
		return possibly_sensitive;
	}

	public void setPossibly_sensitive(int possibly_sensitive) {
		this.possibly_sensitive = possibly_sensitive;
	}







}


class RetweetedQuatedStatus
{
	
	@JsonDeserialize(using = CustomJsonDateDeserializer.class)
	public  String created_at;
	public String id;
	public User user;
	public String text;
	public String full_text;
	
	@JsonDeserialize(using =NumericBooleanDeserializer.class)
	public  int possibly_sensitive = 0;
	
	
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
	@JsonIgnore
	private JsonNode bounding_box;
	
	@JsonIgnore
	public JsonNode getBounding_box() {
		return bounding_box;
	}
	
	@JsonProperty
	public void setBounding_box(JsonNode bounding_box) {
		this.bounding_box = bounding_box;
	}
	
	

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
	@JsonDeserialize(using = CustomJsonDateDeserializer.class)
	public String created_at;
	public String name;
	public  String screen_name;
	//	public String name_raw;
	//	public  String screen_name_raw;
	public  String profile_image_url;
	public  String url;
	@JsonDeserialize(using =NumericBooleanDeserializer.class)
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




class NumericBooleanDeserializer extends JsonDeserializer<Integer> {

    @Override
    public Integer deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
        return parser.getValueAsInt();
    }       
}