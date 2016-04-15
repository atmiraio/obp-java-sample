package com.atmira.obp.controllers;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.atmira.obp.ApiCallFailedException;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

@Controller
public class LoginController {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	// TODO move Configuration bean
	private final String CALLBACK_URL =  "http://127.0.0.1:8080/callback";
	
	@Autowired
	OAuthConsumer consumer;
	@Autowired
	OAuthProvider provider;
	
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String greetingForm(HttpServletRequest request) throws ApiCallFailedException {

		log.debug("Obteniendo el token de OBP...");

		String authUrl = "";
		try {
			authUrl = provider.retrieveRequestToken(consumer, CALLBACK_URL);
		} catch (OAuthMessageSignerException | OAuthNotAuthorizedException | OAuthExpectationFailedException
				| OAuthCommunicationException e) {
			log.error("Error al solicitar el token OBP ", e);
			throw new ApiCallFailedException(e.getMessage(), e);
		}

		log.debug("Request token: " + consumer.getToken());
		log.debug("Token secret: " + consumer.getTokenSecret());
		
		// TODO guardar en session para poder tener mas de un usuario a la vez
		request.getSession().setAttribute("oauthRequestToken", consumer.getToken());
		request.getSession().setAttribute("oauthRequestTokenSecret", consumer.getTokenSecret());
				
		return "redirect:" + authUrl;
	}

	@RequestMapping(value = "/callback", method = RequestMethod.GET)
	public String callback(HttpServletRequest request, @RequestParam("oauth_verifier") String oauthVerifier) throws ApiCallFailedException {
		// TODO obtener el token de session para poder tener mas de un usuario a la vez
		String token = (String) request.getSession().getAttribute("oauthRequestToken");
		String tokenRequest = (String) request.getSession().getAttribute("oauthRequestTokenSecret");
		// TODO guardar en session para poder tener mas de un usuario a la vez
		//request.getSession().setAttribute("oauthRequestTokenSecret", consumer.getTokenSecret());
		log.debug("Obteniendo un access token de OBP...");
		consumer.setTokenWithSecret(token, tokenRequest);
		try {
			provider.retrieveAccessToken(consumer, oauthVerifier);
		} catch (OAuthMessageSignerException | OAuthNotAuthorizedException | OAuthExpectationFailedException
				| OAuthCommunicationException e) {
			log.error("Error obteniendo un access token de OBPP ", e);
			throw new ApiCallFailedException(e.getMessage(), e);
		}

		log.debug("Access token: " + consumer.getToken());
		log.debug("Token secret: " + consumer.getTokenSecret());

		return "redirect: tmp";
	}	
	

}
