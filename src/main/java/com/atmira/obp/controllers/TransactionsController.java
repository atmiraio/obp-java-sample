package com.atmira.obp.controllers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.atmira.obp.ApiCallFailedException;
import com.atmira.obp.models.Transaction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

@Controller
public class TransactionsController {

	@Autowired
	OAuthConsumer consumer;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final String TRANSACTION_URL = "https://apisandbox.openbankproject.com/obp/v2.0.0/banks/%1$s/accounts/%2$s/%3$s/transaction-request-types/SANDBOX_TAN/transaction-requests";

	// TODO move Configuration bean
	// TODO Finalizar con datos reales
	// Actualmente pertenecen al usuario de pruebas
	private final String BANK_ID = "rbs";
	private final String ACCOUNT_ID = "5dHBvPFLLbnnBi2fOYOy";
	private final String VIEW_ID = "owner";
	private final String CHALLENGE_TYPE = "SANDBOX_TAN";
	private final String CURRENCY = "EUR";

	@RequestMapping(value = "/transactions", method = RequestMethod.GET)
	public String getTransactions(Model model) {

		model.addAttribute("transaction", new Transaction());
		return "transactions";
	}

	@RequestMapping(value = "/transactions", method = RequestMethod.POST)
	public String getTransactions(@ModelAttribute("transaction") Transaction transaction, BindingResult bindingResult,
			final Model model) throws ApiCallFailedException {
		String url = String.format(TRANSACTION_URL, BANK_ID, ACCOUNT_ID, VIEW_ID);

		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost request = new HttpPost(url);

		StringEntity params = null;
		try {
			String jsonRequest = createJsonRequest(transaction);
			params = new StringEntity(jsonRequest);
		} catch (UnsupportedEncodingException | JsonProcessingException e) {
			log.error("Error al crear el JSON de la peticion", e);
			throw new ApiCallFailedException(e.getMessage(), e);
		}
		request.addHeader("content-type", "application/json");
		request.setEntity(params);
		try {
			consumer.sign(request);
		} catch (OAuthMessageSignerException | OAuthExpectationFailedException | OAuthCommunicationException e) {
			log.error("Error al firmar la peticion", e);
			throw new ApiCallFailedException(e.getMessage(), e);
		}

		//  TODO Finalizar con datos reales
		org.apache.http.HttpResponse response = null;
		try {
			response = httpClient.execute(request);
			HttpEntity entity = response.getEntity();
			String jsonString = EntityUtils.toString(entity);
			log.info("Salida de la peticion: " + jsonString);
		} catch (IOException e) {
			log.error("Error al realizar la peticion", e);
			throw new ApiCallFailedException(e.getMessage(), e);
		}

		return "transactions";
	}

	private String createJsonRequest(Transaction transaction) throws JsonProcessingException {

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode main = mapper.createObjectNode();

		ObjectNode toNode = mapper.createObjectNode();
		toNode.put("bank_id", transaction.getBankId());
		toNode.put("account_id", transaction.getAccountId());
		toNode.put("description", transaction.getDescription());
		toNode.put("challenge_type", CHALLENGE_TYPE);

		ObjectNode valueNode = mapper.createObjectNode();
		valueNode.put("currency", CURRENCY);
		valueNode.put("amount", "100.00");

		toNode.set("value", valueNode);

		main.set("to", toNode);

		return mapper.writeValueAsString(main);
	}

}
