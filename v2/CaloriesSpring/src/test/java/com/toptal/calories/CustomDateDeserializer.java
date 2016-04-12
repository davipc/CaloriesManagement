package com.toptal.calories;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class CustomDateDeserializer extends JsonDeserializer<Date> {
		 
	private static final String[] DATE_FORMATS = new String[] {
	        "HH:mm:ss",
	        "yyyy-MM-dd"
	};
	
	private static final TimeZone DEFAULT_TIMEZONE = TimeZone.getDefault();
	
    @Override
    public Date deserialize(JsonParser jp, DeserializationContext ctxt) 
    throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);

        for (String format : DATE_FORMATS) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                sdf.setTimeZone(DEFAULT_TIMEZONE);
            	return sdf.parse(node.asText());
            } catch (ParseException e) {
            }
        }
        
        // it might also be in the long format
        try {
        	long value = Long.parseLong(node.asText());
            if (Long.toString(value).equals(node.asText()))
				return new Date(value);
        } catch (NumberFormatException nfe) {
        }
        
        throw new IOException("Unable to parse input date using unexpected format: \"" + node.asText() + "\"");
    }
}
