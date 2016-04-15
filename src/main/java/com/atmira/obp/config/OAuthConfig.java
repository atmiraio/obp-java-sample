package com.atmira.obp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.web.context.WebApplicationContext;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;

@Configuration
public class OAuthConfig {

	@Bean
	//@Scope(WebApplicationContext.SCOPE_SESSION)
	public OAuthConsumer oauthConsumer(){
		return new CommonsHttpOAuthConsumer(
                "qnpinz2oqftigpwpsoww0fxt1aiy1a5qh4tzjkv2",
                "hrv4ob1tr2rzpqp5nthquklj3yfcblf5syzwgdbd");
	}
	
	@Bean
	//@Scope(WebApplicationContext.SCOPE_SESSION)
	public OAuthProvider oauthProvider(){
		return new CommonsHttpOAuthProvider(
                "https://apisandbox.openbankproject.com/oauth/initiate",
                "https://apisandbox.openbankproject.com/oauth/token",
                "https://apisandbox.openbankproject.com/oauth/authorize");
	}
}
