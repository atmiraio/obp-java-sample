package com.atmira.obp.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Bank {
	
	private String id;
	@JsonProperty("short_name")
	private String shortName;
	@JsonProperty("full_name")
	private String fullName;
	private String website;
	private String logo;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getShortName() {
		return shortName;
	}
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public String getWebsite() {
		return website;
	}
	public void setWebsite(String website) {
		this.website = website;
	}
	public String getLogo() {
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}
	
	@Override
	public String toString() {
		return "Bank [id=" + id + ", shortName=" + shortName + ", fullName=" + fullName + ", website=" + website
				+ ", logo=" + logo + "]";
	}
	
	
}
