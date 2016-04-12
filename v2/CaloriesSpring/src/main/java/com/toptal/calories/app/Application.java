package com.toptal.calories.app;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@Configuration
@EnableAutoConfiguration
@EntityScan(basePackages = "com.toptal.calories.entity")
@ComponentScan(basePackages = "com.toptal.calories")
@EnableJpaRepositories(basePackages = "com.toptal.calories.repository")
public class Application extends SpringBootServletInitializer implements EmbeddedServletContainerCustomizer {

	private static Logger logger = LoggerFactory.getLogger(Application.class);
	
    @Value("${server.document-root}")
    private  String documentRoot;
	
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }

    @Override
    public void customize(ConfigurableEmbeddedServletContainer container) {
    	logger.debug("Setting document root to: " + new File(documentRoot).getAbsolutePath());
        container.setDocumentRoot(new File(documentRoot));
    }   
    
    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }
}
