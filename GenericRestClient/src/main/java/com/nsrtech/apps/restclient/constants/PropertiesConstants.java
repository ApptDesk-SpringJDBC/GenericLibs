package com.nsrtech.apps.restclient.constants;


public enum PropertiesConstants {
	REST_CLIENT_PROP("restclient.properties");
	
	private String propertyFileName;
	private PropertiesConstants(String propertyFileName) {
		this.setPropertyFileName(propertyFileName);
	}

	public String getPropertyFileName() {
		return propertyFileName;
	}

	public void setPropertyFileName(String propertyFileName) {
		this.propertyFileName = propertyFileName;
	}
}
