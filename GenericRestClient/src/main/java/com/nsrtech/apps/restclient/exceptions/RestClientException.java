package com.nsrtech.apps.restclient.exceptions;

public class RestClientException extends Exception {	
	public String code;
	public String message; // userMessage
	
	
	public RestClientException(String code, String message) {
		setCode(code);
		setMessage(message);
	}

	public RestClientException() {

	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
