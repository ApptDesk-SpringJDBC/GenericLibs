package com.nsrtech.apps.https;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import javax.net.ssl.HttpsURLConnection;
import org.apache.log4j.Logger;

public class HttpsHandler {
	HttpURLConnection connection = null;
	private final int defaultTimeout = 60000;
	boolean additionalParams = false;

	public HttpsHandler(ConnectionProperties conProp, HttpURLConnection connection) {
		this.connection = connection;
		this.additionalParams = conProp.isAdditionalParams();
	}

	public String processRequest(Logger logger, String postData) throws IOException {
		return processRequest(logger, postData, defaultTimeout);
	}

	/*
	 * Helper method to print connection properties onece the connection is
	 * created, used for debugging
	 */
	public static void printConnectionProperties(Logger logger, HttpURLConnection urlc) throws IOException {
		if (urlc instanceof HttpsURLConnection) {
			logger.info("SocketFactory: " + ((HttpsURLConnection) urlc).getSSLSocketFactory());
		}

		logger.info("Connected to: " + urlc);
		logger.info("Content Length: " + urlc.getContentLength());
		logger.info("Response code and msg: " + urlc.getResponseCode() + " : " + urlc.getResponseMessage());
		logger.info("getAllowUserInteraction: " + urlc.getAllowUserInteraction());
		logger.info("getDoOutput: " + urlc.getDoOutput());
		logger.info("getContentEncoding: " + urlc.getContentEncoding());
		logger.info("usingProxy: " + urlc.usingProxy());
		logger.info("getRequestMethod: " + urlc.getRequestMethod());
		logger.info("getURL: " + urlc.getURL());
	}

	public String processRequest(Logger logger, String postData, int timeout) throws IOException {
		connection.setConnectTimeout(timeout);
		connection.setReadTimeout(timeout);

		logger.info("HTTPSHandler processRequest " + additionalParams);
		if (additionalParams) {
			connection.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
			connection.setRequestProperty("Content-transfer-encoding", "UTF-8");
			connection.setDoOutput(true);
			connection.setDoInput(true);
		}
		
		OutputStream os = null;
		BufferedReader br = null;
		try {
			os = connection.getOutputStream();
			os.write(postData.getBytes());
			os.flush();
			br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			if (br == null) {
				br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
			}
			StringBuffer response = new StringBuffer();
			String temp = br.readLine();
			while (temp != null) {
				response.append(temp);
				response.append("\n");
				temp = br.readLine();
			}
			logger.debug("IP Fetched - " + InetAddress.getByName(connection.getURL().getHost()).getHostAddress());
			return response.toString().trim();
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				if (os != null) {
					os.close();
				}
	
				if (br != null) {
					br.close();
				}
			} catch(IOException ioe) {
				throw ioe;
			}
		}
	}
}
