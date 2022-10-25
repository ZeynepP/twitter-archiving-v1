package fr.ina.dlweb.twitter.indexer.utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import fr.ina.dlweb.utils.DateUtil;

public class CustomJsonDateDeserializer extends JsonDeserializer<String>
{
	
	final String TWITTER="EEE MMM dd HH:mm:ss Z yyyy";
	
	
	
	//public static final DateTimeFormatter TWITTER_FORMAT = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss Z yyyy");
    public String deserialize(JsonParser jsonparser, DeserializationContext deserializationcontext) throws IOException, JsonProcessingException {

    	//https://stackoverflow.com/questions/27529616/jackson-processing-issue 
    	// SimpleDateFormat is not thread safe
    	SimpleDateFormat sf = new SimpleDateFormat(TWITTER,Locale.ENGLISH);
    	String date = jsonparser.getText();
     	return String.valueOf(DateUtil.parseDate(sf,date).getTime());

    }
    
  

}


