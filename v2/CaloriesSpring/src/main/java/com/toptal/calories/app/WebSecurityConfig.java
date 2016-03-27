package com.toptal.calories.app;

public class WebSecurityConfig {}

//@Configuration
//@EnableWebSecurity
//public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
//
//	private static Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class); 
//	
//	@Autowired
//    private UserDetailsService userDetailsService;
//	
//	@Override
//    protected void configure(HttpSecurity http) throws Exception {
//        logger.debug("Setting up security...");
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
//
//		logger.debug("Finished setting up security");
//    }
//
//    @Autowired
//    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//        logger.debug("Configuring authentication...");
//
////    	auth.inMemoryAuthentication()
////                .withUser("user").password("password").roles("USER");
//    	
//        auth.userDetailsService(userDetailsService);
//
//        
//        logger.debug("Finished configuring authentication");
//    }
//    
//    
//    
//}
