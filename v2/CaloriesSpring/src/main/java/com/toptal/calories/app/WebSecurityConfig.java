package com.toptal.calories.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	private static Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class); 
	
	//@Autowired
    //private UserDetailsService userDetailsService;
	
	@Override
    protected void configure(HttpSecurity http) throws Exception {
        logger.debug("Setting up security...");
//		http
//            .authorizeRequests()
//                .antMatchers("/", "/login*", "/hello.html", "/api/v2/**", "/font-awesome-4.2.0/**", "/css/**", "/fonts/**", "/images/**", "/jquery/**", "/js/**", "/less/**").permitAll()
//                .anyRequest().authenticated()
//                .and()
//            .formLogin()
//                .loginPage("/login.jsp").passwordParameter("password").usernameParameter("login")
//                .permitAll()
//                .and()
//            .logout()
//                .permitAll();

        // will disable this security constraint so functional tests are less troublesome
    	// CSRF increases security by tying a session ID to the logged user's physical address
    	// by disabling it, a hacker could hijack a user's session by copying his session ID and using it from wherever he is
    	// on a real world application, either the rest API would be behind a firewall (not directly accessible to the external world), 
    	// or would have security setup through some more powerful security mechanism (OAuth2 ?)
    	http.csrf().disable();
    	
    	http.authorizeRequests()
                .antMatchers("/api/v2/**", "/index.html").access("hasRole('ROLE_Default') or hasRole('ROLE_Manager') or hasRole('ROLE_Admin')")
                .antMatchers("/userEdit.html").access ("hasRole('ROLE_Admin')")
                .antMatchers("/calendarCalories.html").access ("hasRole('ROLE_Default') or hasRole('ROLE_Manager')")
            .and()
	            // ATTENTION: For the form login to work, the attribute names must be EXACTLY these: username=<username>, password=<password> AND Submit=Login
	            // IF YOU WANT THE 2 FIRST TO BE DIFFERENT, YOU HAVE TO SET IT LIKE THIS:
	            //.formLogin().loginPage("/login.jsp").usernameParameter("<other name>").passwordParameter("<other name>");
            	// also, the defaultSuccessUrl is forcing the user to always go to the index page
	            .formLogin().loginPage("/login.jsp").defaultSuccessUrl("/index.html", true)
            .and().exceptionHandling().accessDeniedPage("/notAuthorized.html")
            .and().logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout")).logoutSuccessUrl("/login.jsp");
        
		logger.debug("Finished setting up security");
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        logger.debug("Configuring authentication...");

        auth.inMemoryAuthentication().withUser("eu").password("123456").roles("Default");
        auth.inMemoryAuthentication().withUser("manager").password("123456").roles("Manager");
        auth.inMemoryAuthentication().withUser("admin").password("123456").roles("Admin");
        
//    	auth.inMemoryAuthentication()
//                .withUser("user").password("password").roles("USER");
    	
//        auth.userDetailsService(userDetailsService);

        
        logger.debug("Finished configuring authentication");
    }
    
    
    
}
