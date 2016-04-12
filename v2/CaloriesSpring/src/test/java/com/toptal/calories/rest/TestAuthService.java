package com.toptal.calories.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;

import java.sql.Timestamp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.toptal.calories.builder.UserBuilder;
import com.toptal.calories.entity.Gender;
import com.toptal.calories.entity.User;
import com.toptal.calories.repository.UserRepository;
import com.toptal.calories.rest.exceptions.UnauthorizedException;

@RunWith(MockitoJUnitRunner.class)
public class TestAuthService {

	private static final User USER = new UserBuilder().login("login").password("password").build();
	
	// password generated through the Encryption helper for the raw password "password" 
	private static final User USER_RETURNED = new UserBuilder(USER).id(1).password("XohImNooBHFR0OVvjcYpJ3NgPQ1qq73WKhHvch0VQtg=").name("aName").gender(Gender.F).dailyCalories(1500).creationDt(new Timestamp(System.currentTimeMillis())).build();
	
	@InjectMocks
	private AuthService authService;
	
	@Mock
	private UserRepository userRepository;

	// required since we are annotating authService to take mocks for the injected references 
//	@Spy
//	private EncryptionHelper encHelper = new EncryptionHelper();

	
	@Test
	public void testAuthenticateUserNullUser() {
		// make the service call
		
		Throwable thrown = catchThrowable(() -> { authService.authenticateUser(null); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Missing login and/or password");
	}
	
	@Test
	public void testAuthenticateUserNullLogin() {
		// make the service call
		
		Throwable thrown = catchThrowable(() -> { authService.authenticateUser(new UserBuilder(USER).login(null).build()); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Missing login and/or password");
	}
	
	@Test
	public void testAuthenticateUserNullPassword() {
		// make the service call
		
		Throwable thrown = catchThrowable(() -> { authService.authenticateUser(new UserBuilder(USER).password(null).build()); } );
		assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Missing login and/or password");
	}
	
	@Test
	public void testAuthenticateUserLoginNotFound() {
		// setup the mock repository
		given(userRepository.findByLogin(anyString())).willReturn(null);

		// make the service call
		Throwable thrown = catchThrowable(() -> { authService.authenticateUser(USER); } );
		assertThat(thrown).isNotNull().isInstanceOf(UnauthorizedException.class).hasMessageContaining("Invalid login/password");
	}

	@Test
	public void testAuthenticateUserBadPassword() {
		// setup the mock repository
		given(userRepository.findByLogin(anyString())).willReturn(new UserBuilder(USER_RETURNED).password("").build());

		// make the service call
		Throwable thrown = catchThrowable(() -> { authService.authenticateUser(USER); } );
		assertThat(thrown).isNotNull().isInstanceOf(UnauthorizedException.class).hasMessageContaining("Invalid login/password");
	}

	@Test
	public void testAuthenticateUserOK() {
		// setup the mock repository
		given(userRepository.findByLogin(anyString())).willReturn(new UserBuilder(USER_RETURNED).build());

		// make the service call
		try {
			assertThat(authService.authenticateUser(USER)).isEqualTo(USER_RETURNED);
		} catch (Exception e) {
			fail("Error testing authenticateUser: " + e.getMessage());
		}	
	}
	
}
