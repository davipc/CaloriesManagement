package com.toptal.calories.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.toptal.calories.constants.RestPaths;
import com.toptal.calories.constants.WebResources;
import com.toptal.calories.entity.RoleType;
import com.toptal.calories.security.RESTBasedAuthenticationProvider;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled=true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	private static Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class); 
	
	@Autowired
    private RESTBasedAuthenticationProvider authProvider;
	
	@Autowired
	private RestAwareAccessDeniedHandler accessDeniedHandler;
	
	@Override
    protected void configure(HttpSecurity http) throws Exception {
        logger.debug("Setting up security...");

        // will disable this security constraint so functional tests are less troublesome
    	// CSRF increases security by tying a session ID to the logged user's physical address
    	// by disabling it, a hacker could hijack a user's session by copying his session ID and using it from wherever he is
    	// on a real world application, either the rest API would be behind a firewall (not directly accessible to the external world), 
    	// or would have security setup through some more powerful security mechanism (OAuth2 ?)
    	http.csrf().disable();
    	
    	http.authorizeRequests()
    			.antMatchers(WebResources.SIGNUP_PAGE, RestPaths.AUTH, RestPaths.ROLES + "/**", RestPaths.USERS + "/**", WebResources.ACCESS_DENIED_PAGE).permitAll()
    			.antMatchers(RestPaths.REST_BASE_URI + "/**", WebResources.LANDING_PAGE).authenticated()
                .antMatchers(WebResources.MEAL_MANAGE_PAGE, WebResources.USER_MANAGE_PAGE, WebResources.USER_SESSION_UPDATE_CHECK).access("hasRole('ROLE_" + RoleType.DEFAULT + "') or hasRole('ROLE_" + RoleType.MANAGER + "') or hasRole('ROLE_" + RoleType.ADMIN + "')")
            .and()
	            // ATTENTION: For the form login to work, the attribute names must be EXACTLY these: username=<username>, password=<password> AND Submit=Login
	            // IF YOU WANT THE 2 FIRST TO BE DIFFERENT, YOU HAVE TO SET IT LIKE THIS:
	            //.formLogin().loginPage("/login.jsp").usernameParameter("<other name>").passwordParameter("<other name>");
            	// on a side note, the defaultSuccessUrl is forcing the user to always go to the index page
	            .formLogin().loginPage(WebResources.LOGIN_PAGE).defaultSuccessUrl(WebResources.LANDING_PAGE, true)
            //.and().exceptionHandling().accessDeniedPage("/notAuthorized.html")
            .and().exceptionHandling().accessDeniedHandler(accessDeniedHandler)
            .and().exceptionHandling().authenticationEntryPoint(new RestAwareAuthenticationEntryPoint(WebResources.LOGIN_PAGE))	            
            .and().logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout")).logoutSuccessUrl(WebResources.LOGIN_PAGE);
        
		logger.debug("Finished setting up security");
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        logger.debug("Configuring authentication...");

        auth.authenticationProvider(authProvider);
        
        logger.debug("Finished configuring authentication");
    }
}
