package com.toptal.calories.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
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
public class Application extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }
}

//@Configuration
//@EnableWebSecurity
//class SecurityConfig extends WebSecurityConfigurerAdapter {
//
//    @Autowired
//    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//        auth.inMemoryAuthentication().withUser("eu").password("123456").roles("USER");
//        auth.inMemoryAuthentication().withUser("admin").password("123456").roles("ADMIN");
//        auth.inMemoryAuthentication().withUser("dba").password("123456").roles("DBA");
//    }
//
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        // will disable this security constraint so functional tests are less troublesome
//    	// CSRF increases security by tying a session ID to the logged user's physical address
//    	// by disabling it, a hacker could hijack a user's session by copying his session ID and using it from wherever he is
//    	// on a real world application, either the rest API would be behind a firewall (not directly accessible to the external world), 
//    	// or would have security setup through some more powerful security mechanism (OAuth2 ?)
//    	http.csrf().disable();
//    	
//    	http.authorizeRequests()
//                .antMatchers("/api/**").access("hasRole('ROLE_ADMIN')")
//                .antMatchers("/*.html").access ("hasRole('ROLE_ADMIN')")
//                .and().formLogin();
//
//    }
//}
