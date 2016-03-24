package com.toptal.calories.rest.provider;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class CustomJsonProvider extends JacksonJaxbJsonProvider {

	private static Logger logger = LoggerFactory.getLogger(CustomJsonProvider.class);	
	
    private static ObjectMapper mapper = new ObjectMapper();

    static {
        //mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    	mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        //mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        // mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // mapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        logger.info("Mapper configured by Provider");        
     }

    public CustomJsonProvider() {
        super();
        logger.info("Setting Provider...");
        // NOT USING FOR NOW AS IT SEEMS TO FORCE FETCHING OF LAZY FIELDS, CAUSING CYCLIC DEPENDENCY ERRORS DURING UNMARSHALL
        //setMapper(mapper);
        logger.info("Provider set");
    }
}