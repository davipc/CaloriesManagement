package com.toptal.calories;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.toptal.calories.app.Application;
import com.toptal.calories.entity.Gender;
import com.toptal.calories.entity.Meal;
import com.toptal.calories.entity.Role;
import com.toptal.calories.entity.RoleType;
import com.toptal.calories.entity.User;
import com.toptal.calories.repository.MealRepository;
import com.toptal.calories.repository.RoleRepository;
import com.toptal.calories.repository.UserRepository;
import com.toptal.calories.rest.util.EncryptionHelper;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(Application.class)
@IntegrationTest
public class LoadTestData {

	Logger logger = LoggerFactory.getLogger(LoadTestData.class); 
	
	@Autowired
	private RoleRepository roleRepo;
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private MealRepository mealRepo;
	
	private EncryptionHelper encHelper = new EncryptionHelper(); 
	
	private static final boolean LOAD_ENABLED = false;
	
	@Test
	public void loadTestData() {
		
		if (LOAD_ENABLED) {
		
			logger.info("Resetting database with test data...");
			
			// first cleanup current data
			userRepo.deleteAll();
			roleRepo.deleteAll();
	
			
			// populate roles
			Role roleDefault = new Role(RoleType.DEFAULT);
			Role roleManager = new Role(RoleType.MANAGER);
			Role roleAdmin = new Role(RoleType.ADMIN);
			
			roleRepo.save(roleDefault);
			roleRepo.save(roleManager);
			roleRepo.save(roleAdmin);
	
			// populate users
			List<User> users = new ArrayList<>();
			User user1 = new User("user1", encHelper.encrypt("password1"), "User 1", Gender.M, 1800, Arrays.asList(new Role[]{roleDefault}), null, new Timestamp(System.currentTimeMillis()));
			User user2 = new User("user2", encHelper.encrypt("password2"), "User 2", Gender.F, 1500, Arrays.asList(new Role[]{roleDefault}), null, new Timestamp(System.currentTimeMillis()));
			User user3 = new User("user3", encHelper.encrypt("password3"), "User 3", Gender.M, 2000, Arrays.asList(new Role[]{roleDefault}), null, new Timestamp(System.currentTimeMillis()));
			User userManager = new User("manager1", encHelper.encrypt("password1"), "Manager 1", Gender.F, 1800, Arrays.asList(new Role[]{roleManager}), null, new Timestamp(System.currentTimeMillis()));
			User userAdmin = new User("admin1", encHelper.encrypt("password1"), "Admin 1", Gender.M, 1800, Arrays.asList(new Role[]{roleAdmin}), null, new Timestamp(System.currentTimeMillis()));
			users.add(user1);
			users.add(user2);
			users.add(user3);
			users.add(userManager);
			users.add(userAdmin);
			
			userRepo.save(users);
			
			// populate meals
			
			// each user will start on the next day, and have an extra day worth of meals
			List<Meal> allMeals = new ArrayList<>();
			
			int firstUserMealDays = 5;
			int userCnt = 0;
			
			Calendar c = Calendar.getInstance();
			c.set(Calendar.DAY_OF_MONTH, 1);
			Date firstOfTheMonth = c.getTime(); 
			
			long ONE_DAY = (long) 24*60*60*1000;
	
			int[] mealTimeHours = {9, 12, 18};
			int[] mealTimeMinutes = {0, 30, 45};
			String[] mealTimeDesc = new String[]{"Breakfast", "Lunch", "Dinner"};
			int[] mealCalories = {500, 600, 400};
			
			long firstUserDay;
			long day;
			for (User user: users) {
				firstUserDay = firstOfTheMonth.getTime() + userCnt * ONE_DAY;
	
				for (int i = 0; i < firstUserMealDays + userCnt; i++) {
					day = firstUserDay + i * ONE_DAY;
					for (int j = 0; j < mealTimeHours.length; j++) {
						Meal meal = new Meal(user, new Date(day), getTime(mealTimeHours[j], mealTimeMinutes[j]), mealTimeDesc[j], mealCalories[j]);
						allMeals.add(meal);
					}
				}
				
				userCnt++;
			}
			
			mealRepo.save(allMeals);
			
			logger.info("Finished resetting database with test data!");
		}
	}
	
	private static Date getTime(int hour, int minute) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 1970);
		cal.set(Calendar.MONTH, 0);
		cal.set(Calendar.DATE, 1);
		
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		return cal.getTime();
	}
	
}
