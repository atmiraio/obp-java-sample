package com.atmira.obp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

// TODO implementar seguridad

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.antMatcher("/**")
			.authorizeRequests()
				.antMatchers("/", "/login**", "/callback", "/bootstrap/**", "/css/**", "/images/**", "/js/**", "/vendors/**", "/tmp**", "/accounts**", "/transaction**")
				.permitAll()
			.anyRequest()
				.authenticated();
	}


}
