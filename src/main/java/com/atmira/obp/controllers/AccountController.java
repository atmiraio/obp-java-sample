package com.atmira.obp.controllers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.atmira.obp.ApiCallFailedException;
import com.atmira.obp.models.Account;
import com.fasterxml.jackson.databind.ObjectMapper;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

@Controller
public class AccountController {
	@Autowired
	OAuthConsumer consumer;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	// TODO move Configuration bean
	private final String ACCOUNTS_URL = "https://apisandbox.openbankproject.com/obp/v2.0.0/my/accounts";

	@RequestMapping(value = "/accounts", method = RequestMethod.GET)
	public ModelAndView getAccounts(HttpServletRequest request) throws ApiCallFailedException{
		
		log.debug("Peticion de cuentas de usuario. URL:" + ACCOUNTS_URL);
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpGet requestGet = new HttpGet(ACCOUNTS_URL);
		ModelAndView model= null;
		
		try {
			consumer.sign(requestGet);
		} catch (OAuthMessageSignerException | OAuthExpectationFailedException | OAuthCommunicationException e) {
			log.error("Error al firmar la peticion", e);
			throw new ApiCallFailedException(e.getMessage(), e);
		}
		org.apache.http.HttpResponse response = null;
		try {
			response = httpClient.execute(requestGet);
		} catch (IOException e) {
			log.error("Error al invocar al API", e);
			throw new ApiCallFailedException(e.getMessage(), e);
		}

		HttpEntity entity = response.getEntity();
		int statusCode = response.getStatusLine().getStatusCode();

		if (statusCode == 200 || statusCode == 201) {
			 Account[] accounts;
			try {
				accounts = parseJsonResponse(entity);
			} catch (ParseException | IOException e) {
				log.error("Error al parsear la respuesta", e);
				throw new ApiCallFailedException(e.getMessage(), e);
			}
             model = new ModelAndView("accounts");
             model.addObject("accounts", accounts);
             return model;
		} else {
			log.error("Error en la respuesta de la petcion [" + statusCode + "]");
			throw new ApiCallFailedException("Error en la respuesta de la petcion [" + statusCode + "]");
		}
	}

	private Account[] parseJsonResponse(HttpEntity entity) throws ParseException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		Account[] accountsList = null;
		String jsonString = EntityUtils.toString(entity);
		accountsList = mapper.readValue(jsonString, Account[].class);

		return accountsList;
	}

}