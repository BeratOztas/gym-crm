package com.epam.gym_crm.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.epam.gym_crm.db.entity.User;

import lombok.Data;

@Data
public class JwtUserDetails implements UserDetails {

	private static final long serialVersionUID = -5436394044263886799L;

	private final Long id;
	private final String username;
	private final String password;
	private final boolean isEnabled; 
	private final boolean accountNonLocked;
	private final Collection<? extends GrantedAuthority> authorities;
	
	public JwtUserDetails(Long id, String username, String password, boolean isEnabled, boolean accountNonLocked,
			Collection<? extends GrantedAuthority> authorities) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.isEnabled = isEnabled;
		this.accountNonLocked = accountNonLocked;
		this.authorities = authorities;
	}

	public static JwtUserDetails create(User user, boolean isAccountNonLocked) {
		List<GrantedAuthority> authoritiesList = List.of(new SimpleGrantedAuthority("ROLE_AUTH"));
		
		return new JwtUserDetails(user.getId(), user.getUsername(), user.getPassword(), user.isActive(), isAccountNonLocked, authoritiesList);
	}
	

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return accountNonLocked;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return isEnabled;
	}


}
