package com.atmira.obp.models;

public class Transaction {
	
	private String bankId;
	private String accountId;
	private Long ammount;
	private String description;
	private String challengeType;
	public String getBankId() {
		return bankId;
	}
	public void setBankId(String bankId) {
		this.bankId = bankId;
	}
	public String getAccountId() {
		return accountId;
	}
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}
	public Long getAmmount() {
		return ammount;
	}
	public void setAmmount(Long ammount) {
		this.ammount = ammount;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getChallengeType() {
		return challengeType;
	}
	public void setChallengeType(String challengeType) {
		this.challengeType = challengeType;
	}
	@Override
	public String toString() {
		return "Transaction [bankId=" + bankId + ", accountId=" + accountId + ", ammount=" + ammount + ", description="
				+ description + ", challengeType=" + challengeType + "]";
	}
	
	
		
	
	
}
