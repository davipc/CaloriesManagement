package com.toptal.calories.security;

public class CurrentUserDetailsService{}


//@Service
//public class CurrentUserDetailsService implements UserDetailsService {
//
//	private static Logger logger = LoggerFactory.getLogger(CurrentUserDetailsService.class); 
//	
//	private final UserService userService;
//    
//    @Autowired
//    public CurrentUserDetailsService(UserService userService) {
//        this.userService = userService;
//    }
//
//    public CurrentUser loadUserByUsername(String username) throws UsernameNotFoundException {
//
//    	//userService.getUserByUsername(username);
//
//    	logger.debug("Started loadUserByUsername");
//    	
//        User user = new User();
//        user.setLogin("test_user");
//        user.setPassword("testpwd");
//        
//    	logger.debug("Finished loadUserByUsername");
//        
//        return new CurrentUser(user);
//    }
//}
