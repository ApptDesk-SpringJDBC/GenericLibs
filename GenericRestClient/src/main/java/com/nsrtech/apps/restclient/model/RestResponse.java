package com.nsrtech.apps.restclient.model;

public class RestResponse {
	private boolean success = true;
	private String responseBody;
	private RestError error;
	
	public String getResponseBody() {
		return responseBody;
	}
	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}
	
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public RestError getError() {
		return error;
	}
	public void setError(RestError error) {
		this.error = error;
	}
	@Override
	public String toString() {
		return "RestResponse [success=" + success + ", responseBody=" + responseBody + ", error=" + error + "]";
	}
}	
