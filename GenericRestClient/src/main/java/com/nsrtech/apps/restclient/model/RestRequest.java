package com.nsrtech.apps.restclient.model;

import java.util.Map;

public class RestRequest {
	private String url;
	private String requestMethod;

	// query string for GET method
	private Map<String,String> queryMap;

	// Any type of request bean for POST
	private Object requestObject;
	
	public String getRequestMethod() {
		return requestMethod;
	}

	public void setRequestMethod(String requestMethod) {
		this.requestMethod = requestMethod;
	}

	public Object getRequestObject() {
		return requestObject;
	}

	public void setRequestObject(Object requestObject) {
		this.requestObject = requestObject;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Map<String,String> getQueryMap() {
		return queryMap;
	}

	public void setQueryMap(Map<String,String> queryMap) {
		this.queryMap = queryMap;
	}
}
