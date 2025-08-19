package com.epam.gym_crm.security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.epam.gym_crm.db.entity.User;

class JwtUserDetailsTest {

	private User testUser;

	@BeforeEach
	void setUp() {
		testUser = new User();
		testUser.setId(1L);
		testUser.setUsername("test.user");
		testUser.setPassword("encodedPassword");
		testUser.setActive(true);
	}

	// --- create Tests ---

	@Test
	void shouldCreateJwtUserDetailsFromUserWhenAccountIsNotLocked() {
		boolean isAccountNonLocked = true;

		JwtUserDetails userDetails = JwtUserDetails.create(testUser, isAccountNonLocked);

		assertNotNull(userDetails);
		assertEquals(testUser.getId(), userDetails.getId());
		assertEquals(testUser.getUsername(), userDetails.getUsername());
		assertEquals(testUser.getPassword(), userDetails.getPassword());
		assertTrue(userDetails.isEnabled(), "isEnabled should reflect user's isActive status.");
		assertTrue(userDetails.isAccountNonLocked(), "isAccountNonLocked should be true.");
	}

	@Test
	void shouldCreateJwtUserDetailsFromUserWhenAccountIsLocked() {
		boolean isAccountNonLocked = false;

		JwtUserDetails userDetails = JwtUserDetails.create(testUser, isAccountNonLocked);

		assertNotNull(userDetails);
		assertEquals(testUser.getId(), userDetails.getId());
		assertEquals(testUser.getUsername(), userDetails.getUsername());
		assertFalse(userDetails.isAccountNonLocked(), "isAccountNonLocked should be false.");
	}

	@Test
	void shouldCreateJwtUserDetailsForInactiveUser() {
		testUser.setActive(false);
		boolean isAccountNonLocked = true;

		JwtUserDetails userDetails = JwtUserDetails.create(testUser, isAccountNonLocked);

		// Assert
		assertNotNull(userDetails);
		assertFalse(userDetails.isEnabled(), "isEnabled should be false for an inactive user.");
		assertTrue(userDetails.isAccountNonLocked(), "Account lock status should be independent of active status.");
	}

	@Test
	void shouldAssignDefaultRoleCorrectly() {
		JwtUserDetails userDetails = JwtUserDetails.create(testUser, true);

		Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
		assertNotNull(authorities);
		assertEquals(1, authorities.size(), "Should have exactly one authority.");

		GrantedAuthority expectedAuthority = new SimpleGrantedAuthority("ROLE_AUTH");
		assertTrue(authorities.contains(expectedAuthority), "Authorities should contain 'ROLE_AUTH'.");
	}

	// --- Spring Security Methods Test ---

	@Test
	void shouldReturnCorrectValuesForInterfaceMethods() {
		// Arrange
		List<GrantedAuthority> authoritiesList = List.of(new SimpleGrantedAuthority("ROLE_TEST"));
		JwtUserDetails userDetails = new JwtUserDetails(1L, "test.user", "pass", true, true, authoritiesList);

		// Act & Assert
		assertEquals("test.user", userDetails.getUsername());
		assertEquals("pass", userDetails.getPassword());
		assertTrue(userDetails.isEnabled());
		assertTrue(userDetails.isAccountNonLocked());

		assertTrue(userDetails.isAccountNonExpired(), "isAccountNonExpired should always be true.");
		assertTrue(userDetails.isCredentialsNonExpired(), "isCredentialsNonExpired should always be true.");

		assertEquals(authoritiesList, userDetails.getAuthorities());
	}
}
