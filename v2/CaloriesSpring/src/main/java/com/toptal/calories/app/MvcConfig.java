package com.toptal.calories.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

// Configuration not currently needed
@Configuration
public class MvcConfig extends WebMvcConfigurerAdapter {

	private static Logger logger = LoggerFactory.getLogger(MvcConfig.class); 
	
	@Override
    public void addViewControllers(ViewControllerRegistry registry) {
		logger.debug("Setting up view controllers...");
		
//        registry.addViewController("/api/v2/**").setViewName("rest");
//        registry.addViewController("/index.jsp").setViewName("index");
//        registry.addViewController("/login.jsp").setViewName("login");
		registry.addViewController("/").setViewName("/login.jsp");
//        registry.addViewController("/api/v2/auth").setViewName("auth");
        
		logger.debug("Finished setting up view controllers");
    }
	
	@Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		logger.debug("Enabling serlvet handling...");
		
        configurer.enable();

        logger.debug("Finished enabling serlvet handling");
    }
}
