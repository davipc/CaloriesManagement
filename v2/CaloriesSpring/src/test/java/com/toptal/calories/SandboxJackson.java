package com.toptal.calories;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toptal.calories.entity.Gender;
import com.toptal.calories.entity.Role;
import com.toptal.calories.entity.RoleType;
import com.toptal.calories.entity.User;

public class SandboxJackson {

	public static void main(String[] args) throws Exception {
		
		Role role = new Role(RoleType.DEFAULT);
		
		User user = new User("aTest", "aPassword", "aName", Gender.M, 1235, Arrays.asList(new Role[]{role}), null, new Timestamp(System.currentTimeMillis()));
		
		ObjectMapper mapper = new ObjectMapper();
		
		String jsonInString = mapper.writeValueAsString(user);
		
		System.out.println("As string: " + jsonInString);
		
		User user2 = mapper.readValue(jsonInString, User.class);

		System.out.println("Back to object: " + user2);
	
		
		System.out.println(new Date());
	}
	
}
