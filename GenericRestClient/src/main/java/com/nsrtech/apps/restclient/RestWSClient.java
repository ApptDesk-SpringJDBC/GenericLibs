package com.nsrtech.apps.restclient;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.nsrtech.apps.common.logger.LoggerUtil;
import com.nsrtech.apps.common.property.util.PropertyUtils;
import com.nsrtech.apps.https.ConnectionManager;
import com.nsrtech.apps.https.ConnectionProperties;
import com.nsrtech.apps.https.HttpsHandler;
import com.nsrtech.apps.restclient.constants.ClientErrorConstants;
import com.nsrtech.apps.restclient.constants.PropertiesConstants;
import com.nsrtech.apps.restclient.exceptions.RestClientException;
import com.nsrtech.apps.restclient.model.RestError;
import com.nsrtech.apps.restclient.model.RestRequest;
import com.nsrtech.apps.restclient.model.RestResponse;

public class RestWSClient {	
	private static RestWSClient instance;

	public static RestWSClient getInstance() {
		if (instance == null) {
			synchronized (RestWSClient.class) {
				if (instance == null)
					instance = new RestWSClient();
			}
		}
		return instance;
	}

	public RestResponse callRestService(RestRequest restRequest) {
		RestResponse restResponse = new RestResponse();
		try {
			Logger logger = getLogger();
			String responseBody = callBackEndService(logger, restRequest);
			restResponse.setResponseBody(responseBody);
		} catch(RestClientException rce) {
			restResponse.setSuccess(false);
			RestError error = new RestError();
			error.setErrorCode(rce.getCode());
			error.setErrorMessage(rce.getMessage());
			restResponse.setError(error);
		}
		return restResponse;
	}
	

	private String callBackEndService(Logger logger, RestRequest restRequest) throws RestClientException {
		String response = "";
		try {
			Map<String,String> mapHeader = new HashMap<String,String>();
			mapHeader.put("Content-Type", "application/json");
			ConnectionProperties conProp = getConnectionProperties(logger, restRequest);
			HttpsHandler httpsHandler = ConnectionManager.getInstance().getHTTPSHandler(logger, conProp, mapHeader);
			String postData = getPostData(restRequest);
			logger.info("POST data:"+postData);
			response = httpsHandler.processRequest(logger, postData);
		} catch(IOException ioe) {
			logger.error("Error: "+ioe, ioe);
			throw new RestClientException(ClientErrorConstants.ERROR_04.getCode(), ClientErrorConstants.ERROR_04.getMessage());
		} catch(UnrecoverableKeyException | KeyStoreException | CertificateException | NoSuchAlgorithmException | KeyManagementException ke) {
			logger.error("Error: "+ke, ke);
			throw new RestClientException(ClientErrorConstants.ERROR_03.getCode(), ClientErrorConstants.ERROR_03.getMessage());
		} 
		return response;
	}
	
	private ConnectionProperties getConnectionProperties(Logger logger, RestRequest restRequest) throws RestClientException {
		ConnectionProperties cp = new ConnectionProperties();
		String url = restRequest.getUrl();
		cp.setRequestMethod(restRequest.getRequestMethod());
		cp.setUrl(url);
		try {
			if (url.contains("https")) {
				String trustFile = PropertyUtils.getValueFromProperties("TRUST_STORE_FILE", PropertiesConstants.REST_CLIENT_PROP.getPropertyFileName());
				String trustPasspharase = PropertyUtils.getValueFromProperties("TRUST_STORE_FILE_PASSPHARSE", PropertiesConstants.REST_CLIENT_PROP.getPropertyFileName());
				if (trustFile != null && trustPasspharase != null) {
					cp.setCertFilePath(trustFile);
					if (trustPasspharase != null && !("".equals(trustPasspharase))) {
						cp.setTrustFilePassPhrase(trustPasspharase);
					}
	
				}
	
				// Do 2-way SSL
				String keyFile = PropertyUtils.getValueFromProperties("KEY_STORE_FILE", PropertiesConstants.REST_CLIENT_PROP.getPropertyFileName());
				String keyPasspharase = PropertyUtils.getValueFromProperties("KEY_STORE_FILE_PASSPHARSE", PropertiesConstants.REST_CLIENT_PROP.getPropertyFileName());
				if (keyFile != null && keyPasspharase != null) {
					cp.setClientKeyCertPath(keyFile);
					cp.setKeyFilePassPhrase(keyPasspharase);
				} else {
					logger.debug("Not Configured for 2-way SSL");
				}
			}
		} catch(IOException ioe) {
			throw new RestClientException(ClientErrorConstants.ERROR_02.getCode(), ClientErrorConstants.ERROR_02.getMessage());
		}
		return cp;
	}
	
	
	private String getPostData(RestRequest restRequest) throws RestClientException {
		String jsonStr = null;

		ObjectMapper mapper = new ObjectMapper();
		try {
			jsonStr = mapper.writeValueAsString(restRequest.getRequestObject());
		} catch (JsonGenerationException e) {
			throw new RestClientException(ClientErrorConstants.ERROR_05.getCode(), ClientErrorConstants.ERROR_05.getMessage());
		} catch (JsonMappingException e) {
			throw new RestClientException(ClientErrorConstants.ERROR_05.getCode(), ClientErrorConstants.ERROR_05.getMessage());
		} catch (IOException e) {
			throw new RestClientException(ClientErrorConstants.ERROR_05.getCode(), ClientErrorConstants.ERROR_05.getMessage());
		}
		return jsonStr;
	}

	private Logger getLogger() throws RestClientException {
		Logger logger = null;
		try {
			String logFileLocation = PropertyUtils.getValueFromProperties("LOG_LOCATION_PATH", PropertiesConstants.REST_CLIENT_PROP.getPropertyFileName());
			String logLevel = PropertyUtils.getValueFromProperties("LOG_LEVEL", PropertiesConstants.REST_CLIENT_PROP.getPropertyFileName());
			String logFileName = PropertyUtils.getValueFromProperties("LOG_FILE_NAME", PropertiesConstants.REST_CLIENT_PROP.getPropertyFileName());
			if(logFileLocation == null) {
				logFileLocation = "/opt/tomcat8/logs";
			}
			logger = LoggerUtil.getLogger(logFileName, logFileLocation, logLevel);
		} catch(Exception e) {
			throw new RestClientException(ClientErrorConstants.ERROR_01.getCode(), ClientErrorConstants.ERROR_01.getMessage());
		}
		return logger;
	}
}