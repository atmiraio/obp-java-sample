package com.atmira.obp;

import oauth.signpost.exception.OAuthException;

public class ApiCallFailedException extends Exception {

	private static final long serialVersionUID = 8906841349527670945L;
	
	public ApiCallFailedException(){
		super();
	}
	
	public ApiCallFailedException(String message) {
		super(message);
	}
	
	public ApiCallFailedException(String message, Throwable e) {
		super(message, e);
	}
	
}
