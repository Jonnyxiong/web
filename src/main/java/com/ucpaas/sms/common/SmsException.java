package com.ucpaas.sms.common;

@SuppressWarnings("serial")
public class SmsException extends Exception{

	private String code;
	
	private String messge;

	
	
	public SmsException(String code, String messge) {
		super(messge);
		this.code = code;
		this.messge = messge;
	}
	
	public SmsException(String messge) {
		super(messge);
		this.messge = messge;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessge() {
		return messge;
	}

	public void setMessge(String messge) {
		this.messge = messge;
	}
	
	
}
