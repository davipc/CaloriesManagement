package com.toptal.calories;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.toptal.calories.entity.Gender;
import com.toptal.calories.entity.Meal;
import com.toptal.calories.entity.Role;
import com.toptal.calories.entity.RoleType;
import com.toptal.calories.entity.User;
import com.toptal.calories.repository.MealRepository;
import com.toptal.calories.repository.RoleRepository;
import com.toptal.calories.repository.UserRepository;
import com.toptal.calories.rest.util.EncryptionHelper;

@Service
@Scope(value = "singleton")
public class TestData {

	Logger logger = LoggerFactory.getLogger(TestData.class); 
	
	@Autowired
	private RoleRepository roleRepo;
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private MealRepository mealRepo;
	
	private EncryptionHelper encHelper = new EncryptionHelper(); 
	
	public Role roleDefault;
	public Role roleManager;
	public Role roleAdmin; 
	
	public User user1;
	public User user2;
	public User user3;
	public User user4;
	public User user5;
	public User user6;
	public User user7;
	public User user8;
	public User user9;
	public User user10;	
	public User userManager;
	public User userAdmin; 	
	
	public Map<User, List<Meal>> userMeals;
	
	private TestData() {
	}
	
	@PostConstruct
	private void load() {
		
			logger.info("Resetting database with test data...");
			
			// first cleanup current data
			userRepo.deleteAll();
			roleRepo.deleteAll();
	
			
			// populate roles
			roleDefault = new Role(RoleType.DEFAULT);
			roleManager = new Role(RoleType.MANAGER);
			roleAdmin = new Role(RoleType.ADMIN);
			
			roleRepo.save(roleDefault);
			roleRepo.save(roleManager);
			roleRepo.save(roleAdmin);
	
			// populate users
			user1 = new User("user1", encHelper.encrypt("1"), "User 1", Gender.M, 1800, Arrays.asList(new Role[]{roleDefault}), null, new Timestamp(System.currentTimeMillis()));
			user2 = new User("user2", encHelper.encrypt("2"), "User 2", Gender.F, 1500, Arrays.asList(new Role[]{roleDefault}), null, new Timestamp(System.currentTimeMillis()));
			user3 = new User("user3", encHelper.encrypt("3"), "User 3", Gender.M, 2000, Arrays.asList(new Role[]{roleDefault}), null, new Timestamp(System.currentTimeMillis()));
			user4 = new User("user4", encHelper.encrypt("4"), "User 4", Gender.F, 2200, Arrays.asList(new Role[]{roleDefault}), null, new Timestamp(System.currentTimeMillis()));
			user5 = new User("user5", encHelper.encrypt("5"), "User 5", Gender.M, 2600, Arrays.asList(new Role[]{roleDefault}), null, new Timestamp(System.currentTimeMillis()));
			user6 = new User("user6", encHelper.encrypt("6"), "User 6", Gender.F, 2900, Arrays.asList(new Role[]{roleDefault}), null, new Timestamp(System.currentTimeMillis()));
			user7 = new User("user7", encHelper.encrypt("7"), "User 7", Gender.M, 3100, Arrays.asList(new Role[]{roleDefault}), null, new Timestamp(System.currentTimeMillis()));
			user8 = new User("user8", encHelper.encrypt("8"), "User 8", Gender.F, 3200, Arrays.asList(new Role[]{roleDefault}), null, new Timestamp(System.currentTimeMillis()));
			user9 = new User("user9", encHelper.encrypt("9"), "User 9", Gender.M, 3300, Arrays.asList(new Role[]{roleDefault}), null, new Timestamp(System.currentTimeMillis()));
			user10 = new User("user10", encHelper.encrypt("10"), "User 10", Gender.F, 3500, Arrays.asList(new Role[]{roleDefault}), null, new Timestamp(System.currentTimeMillis()));
			userManager = new User("manager1", encHelper.encrypt("1"), "Manager 1", Gender.F, 1800, Arrays.asList(new Role[]{roleManager}), null, new Timestamp(System.currentTimeMillis()));
			userAdmin = new User("admin1", encHelper.encrypt("1"), "Admin 1", Gender.M, 1800, Arrays.asList(new Role[]{roleAdmin}), null, new Timestamp(System.currentTimeMillis()));
			List<User> usersWithMeals = new ArrayList<>();
			List<User> usersZeroMeals = new ArrayList<>();
			usersWithMeals.add(user1);
			usersWithMeals.add(user2);
			usersZeroMeals.add(user3);
			usersWithMeals.add(user4);
			usersZeroMeals.add(user5);
			usersZeroMeals.add(user6);
			usersZeroMeals.add(user7);
			usersZeroMeals.add(user8);
			usersZeroMeals.add(user9);
			usersWithMeals.add(user10);
			usersWithMeals.add(userManager);
			usersWithMeals.add(userAdmin);
			
			userRepo.save(usersWithMeals);
			userRepo.save(usersZeroMeals);
			

			// populate meals
			
			// each user will start on the next day, and have an extra day worth of meals
			List<Meal> allMeals = new ArrayList<>();
			
			int firstUserMealDays = 5;
			int userCnt = 0;
			
			Calendar c = Calendar.getInstance();
			c.set(Calendar.DAY_OF_MONTH, 1);
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			Date firstOfTheMonth = c.getTime(); 
			
			long ONE_DAY = (long) 24*60*60*1000;
	
			int[] mealTimeHours = {9, 12, 18};
			int[] mealTimeMinutes = {0, 30, 45};
			String[] mealTimeDesc = new String[]{"Breakfast", "Lunch", "Dinner"};
			int[] mealCalories = {500, 600, 400};
			
			long firstUserDay;
			long day;
			userMeals = new HashMap<>(5);
			List<Meal> thisUserMeals;
			for (User user: usersWithMeals) {
				thisUserMeals = new ArrayList<Meal>();
				firstUserDay = firstOfTheMonth.getTime() + userCnt * ONE_DAY;
	
				for (int i = 0; i < firstUserMealDays + userCnt; i++) {
					day = firstUserDay + i * ONE_DAY;
					for (int j = 0; j < mealTimeHours.length; j++) {
						Meal meal = new Meal(user, new Date(day), getTime(mealTimeHours[j], mealTimeMinutes[j]), mealTimeDesc[j], mealCalories[j]);
						thisUserMeals.add(meal);
						allMeals.add(meal);
					}
				}
				userMeals.put(user, thisUserMeals);
				
				userCnt++;
			}
			
			mealRepo.save(allMeals);
			
			logger.info("Finished resetting database with test data!");
		//}
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
