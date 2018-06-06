package com.nsrtech.apps.https;
public class ConnectionProperties{

	private String url = null;
	/**
	 * can be a certificate or JKS
	 */
	private String certFilePath = null;
	private String clientKeyCertPath = null;
	private String keyFilePassPhrase = "";
	private boolean additionalParams = false;
	/**
	 * Passphrase to access the truststore in case its a JKS bundle
	 */
	private String trustFilePassPhrase = null;
	private String requestMethod = "POST";
	private String disableServerCertCheck = null;
	private String query = null;
	private boolean isSSL = false;
	
	public ConnectionProperties(){

	}

	/**
	 * @return Returns the certFilePath.
	 */
	public String getCertFilePath() {
		return certFilePath;
	}

	/**
	 * @param certFilePath The certFilePath to set.
	 */
	public void setCertFilePath(String certFilePath) {
		this.certFilePath = certFilePath;
	}

	/**
	 * @return Returns the clientKeyCertPath.
	 */
	public String getClientKeyCertPath() {
		return clientKeyCertPath;
	}

	/**
	 * @param clientKeyCertPath The clientKeyCertPath to set.
	 */
	public void setClientKeyCertPath(String clientKeyCertPath) {
		this.clientKeyCertPath = clientKeyCertPath;
	}

	/**
	 * @return Returns the keyFilePassPhrase.
	 */
	public String getKeyFilePassPhrase() {
		return keyFilePassPhrase;
	}

	/**
	 * @param keyFilePassPhrase The keyFilePassPhrase to set.
	 */
	public void setKeyFilePassPhrase(String keyFilePassPhrase) {
		this.keyFilePassPhrase = keyFilePassPhrase;
	}

	/**
	 * @return Returns the requestMethod.
	 */
	public String getRequestMethod() {
		return requestMethod;
	}

	/**
	 * @param requestMethod The requestMethod to set.
	 */
	public void setRequestMethod(String requestMethod) {
		this.requestMethod = requestMethod;
	}

	/**
	 * @return Returns the url.
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url The url to set.
	 */
	public void setUrl(String url) {
		this.url = url;
		if(url != null && url.length() > 5)
			if("https".equalsIgnoreCase(url.substring(0,5)))
				isSSL = true;
			else
				isSSL = false;
	}

	/**
	 * @return Returns the isSSL.
	 */
	public boolean isSSL() {
		return isSSL;
	}

	
	
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public void setDisableServerCertCheck(String disableServerCertCheck) {
		this.disableServerCertCheck = disableServerCertCheck;
	}

	public String getDisableServerCertCheck() {
		return disableServerCertCheck;
	}

	public String getTrustFilePassPhrase() {
		return trustFilePassPhrase;
	}

	public void setTrustFilePassPhrase(String trustFilePassPhrase) {
		this.trustFilePassPhrase = trustFilePassPhrase;
	}

	public void setAdditionalParams(boolean additionalParams) {
		this.additionalParams = additionalParams;
	}

	public boolean isAdditionalParams() {
		return additionalParams;
	}

}
