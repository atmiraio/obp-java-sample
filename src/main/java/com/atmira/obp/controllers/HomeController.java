package com.atmira.obp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import oauth.signpost.OAuthConsumer;

@Controller
public class HomeController {
	
	@Autowired
	OAuthConsumer consumer;
	
    @RequestMapping("/")
    public String home(){
    	return "home";
    }
}
