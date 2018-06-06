package com.nsrtech.apps.https;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;

import com.nsrtech.apps.common.property.util.FileUtils;
import com.nsrtech.apps.common.property.util.PropertyUtils;
import com.nsrtech.apps.restclient.constants.PropertiesConstants;


public class ConnectionManager {
	private static Map conPropMap = null;
	// making the returnable instance of ConnectionManager as static and private
	private static ConnectionManager connectionManager = null;

	// keeping the constructor protected or private to implement singleton class
	protected ConnectionManager() {
	}

	// exposing a static synchronized method to return the only available
	// instance of singleton implementation
	public static synchronized ConnectionManager getInstance() throws KeyManagementException, NoSuchAlgorithmException, CertificateException, KeyStoreException,
			UnrecoverableKeyException, IOException {
		// Singleton implementation
		if (connectionManager == null) {
			connectionManager = new ConnectionManager();
			conPropMap = new HashMap();
		}
		return connectionManager;
	}

	// method for making connection, the return type and signature will be
	// implemented as xmlObjects
	public HttpsHandler getHTTPSHandler(Logger logger, ConnectionProperties conProp) throws KeyManagementException, NoSuchAlgorithmException, CertificateException,
			KeyStoreException, UnrecoverableKeyException, IOException {
		String protocolHandlerPackage = System.getProperty("java.protocol.handler.pkgs");
		if (protocolHandlerPackage != null && !"".equals(protocolHandlerPackage)) {
			if (protocolHandlerPackage.indexOf("javax.net.ssl") < 0) {
				logger.debug("Current HTTP handler packages: " + protocolHandlerPackage + " does not contain javax.net.ssl. Let's ADD it to system property!");
				protocolHandlerPackage = "javax.net.ssl|" + protocolHandlerPackage;
				System.setProperty("java.protocol.handler.pkgs", protocolHandlerPackage);
				logger.debug("Updated HTTP handler packages are: " + protocolHandlerPackage);
			}
		} else {
			logger.debug("java.protocol.handler.pkgs is null, setting it to javax.net.ssl");
			System.setProperty("java.protocol.handler.pkgs", "javax.net.ssl");
		}

		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

		HttpsHandler httpsHandler = null;
		loadConnectionProperties(logger, conProp);
		httpsHandler = new HttpsHandler(conProp, createConnection(logger, conProp));
		return httpsHandler;
	}

	// method to release connection created in getConnection
	public boolean releaseHTTPSHandler(HttpsHandler httpsHandler) {
		// release the connection to the pool or release it //for future use
		return true;
	}

	// Runtime Hostnameverifier, currently verifies even when cert and request
	// URL don't match
	javax.net.ssl.HostnameVerifier hv = new javax.net.ssl.HostnameVerifier() {
		public boolean verify(String urlHostname, SSLSession sslSession) {
			if (urlHostname == null)
				urlHostname = "";
			System.out.println("Host name verifier returning true");
			return true;
		}
	};

	private static void loadConnectionProperties(Logger logger, ConnectionProperties cp) throws NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException,
			UnrecoverableKeyException, KeyManagementException {
		if (conPropMap.get(cp.getUrl()) == null) {
			if (cp.isSSL()) {
				/*
				 * 1. Create the truststore to store server root cert 2. Create
				 * the keystore to store client key and cert chain 3. Create the
				 * SSL Context and initialize it with abovementioned cert and
				 * key store
				 */
				KeyManagerFactory keyMgrFactory = createKeyStore(logger, cp.getClientKeyCertPath(), cp.getKeyFilePassPhrase());
				TrustManager[] trustManager = null;
				if (cp.getDisableServerCertCheck() != null && "true".equalsIgnoreCase(cp.getDisableServerCertCheck())) {
					trustManager = new TrustManager[] { new X509TrustManager() {
						public X509Certificate[] getAcceptedIssuers() {
							return null;
						}

						public void checkClientTrusted(X509Certificate[] certs, String authType) {
							return;
						}

						public void checkServerTrusted(X509Certificate[] certs, String authType) {
							return;
						}
					} };
				} else {
					TrustManagerFactory trustMgrFactory = null;
					// check if the Cert File Path is NULL or NOT if Null
					// trustManager will be NULL
					if (cp.getCertFilePath() != null) {
						if (cp.getTrustFilePassPhrase() == null) {
							/**
							 * truststore is a Certficate
							 */
							trustMgrFactory = createTrustStore(logger, cp.getCertFilePath());
						} else {
							/**
							 * truststore is a JKS bundle
							 */
							trustMgrFactory = createTrustStore(logger, cp.getCertFilePath(), cp.getTrustFilePassPhrase());
						}
						trustManager = trustMgrFactory.getTrustManagers();
					}
				}

				SSLContext sslContext = SSLContext.getInstance("SSLv3");
				if (keyMgrFactory == null) {
					sslContext.init(null, trustManager, null);
				} else {
					sslContext.init(keyMgrFactory.getKeyManagers(), trustManager, null);
				}

				conPropMap.put(cp.getUrl(), sslContext);
			} else {
				conPropMap.put(cp.getUrl(), null);
			}
		}
	}

	private HttpURLConnection createConnection(Logger logger, ConnectionProperties cp) throws MalformedURLException, IOException {
		HttpURLConnection urlc = null;
		if (cp.isSSL()) {
			/*
			 * 1. Get the SSL context factory from the SSL context 2. Set the
			 * host name verifier on the HTTPSConnection to one that we created
			 * 3. Set the socket factory for SSL handshake as in 1 4. Open the
			 * connection on the desired URL 5. Return this connection
			 *
			 * Currently creating new connection for every request for a new
			 * HTTPSHandler, might change to a pooled approach later
			 */
			SSLContext sslContext = (SSLContext) conPropMap.get(cp.getUrl());
			SSLSocketFactory sslSocFactory = sslContext.getSocketFactory();
			HttpsURLConnection.setDefaultHostnameVerifier(hv);
			if (cp.getQuery() == null)
				urlc = (HttpsURLConnection) new URL(cp.getUrl()).openConnection();
			else
				urlc = (HttpsURLConnection) new URL(cp.getUrl() + "?" + cp.getQuery()).openConnection();

			((HttpsURLConnection) urlc).setSSLSocketFactory(sslSocFactory);
			logger.debug("SSL Socket factory set for : " + urlc);

		} else {
			if (cp.getQuery() == null)
				urlc = (HttpURLConnection) new URL(cp.getUrl()).openConnection();
			else
				urlc = (HttpURLConnection) new URL(cp.getUrl() + "?" + cp.getQuery()).openConnection();

		}

		urlc.setRequestMethod((cp.getRequestMethod() == null) ? "POST" : cp.getRequestMethod());
		urlc.setDoOutput(true);
		return urlc;
	}

	/**
	 * Adding user defined http headers
	 *
	 * @param cp
	 * @param mapHeader
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private HttpURLConnection createConnection(Logger logger, ConnectionProperties cp, Map mapHeader) throws MalformedURLException, IOException {
		HttpURLConnection urlc = null;
		if (cp.isSSL()) {
			SSLContext sslContext = (SSLContext) conPropMap.get(cp.getUrl());
			SSLSocketFactory sslSocFactory = sslContext.getSocketFactory();
			HttpsURLConnection.setDefaultHostnameVerifier(hv);
			urlc = (HttpsURLConnection) new URL(cp.getUrl()).openConnection();

			if (cp.getQuery() == null)
				urlc = (HttpsURLConnection) new URL(cp.getUrl()).openConnection();
			else
				urlc = (HttpsURLConnection) new URL(cp.getUrl() + "?" + cp.getQuery()).openConnection();

			((HttpsURLConnection) urlc).setSSLSocketFactory(sslSocFactory);
			logger.info("SSL Socket factory set for : " + urlc);
		} else {
			if (cp.getQuery() == null)
				urlc = (HttpURLConnection) new URL(cp.getUrl()).openConnection();
			else
				urlc = (HttpURLConnection) new URL(cp.getUrl() + "?" + cp.getQuery()).openConnection();

		}

		urlc.setRequestMethod((cp.getRequestMethod() == null) ? "POST" : cp.getRequestMethod());
		urlc.setDoOutput(true);
		// set the headers
		Set set = mapHeader.entrySet();
		Iterator i = set.iterator();
		// System.out.println("\n");
		while (i.hasNext()) {
			Entry headerPair = (Entry) i.next();
			String headerName = "";
			String headerValue = "";
			headerName = (String) headerPair.getKey();
			headerValue = (String) headerPair.getValue();
			if (!headerName.equals("")) {
				urlc.setRequestProperty(headerName, headerValue);
				logger.debug(headerName + " : " + headerValue);
			}
		}
		return urlc;
	}

	/**
	 * Adding user defined http headers
	 *
	 * @param conProp
	 * @param mapHeader
	 * @return
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws KeyStoreException
	 * @throws UnrecoverableKeyException
	 * @throws IOException
	 */
	public HttpsHandler getHTTPSHandler(Logger logger, ConnectionProperties conProp, Map mapHeader) throws KeyManagementException, NoSuchAlgorithmException, CertificateException,
			KeyStoreException, UnrecoverableKeyException, IOException {
		String protocolHandlerPackage = System.getProperty("java.protocol.handler.pkgs");
		if (protocolHandlerPackage != null && !"".equals(protocolHandlerPackage)) {
			if (protocolHandlerPackage.indexOf("javax.net.ssl") < 0) {
				logger.debug("Current HTTP handler packages: " + protocolHandlerPackage + " does not contain javax.net.ssl. Let's ADD it to system property!");
				protocolHandlerPackage = "javax.net.ssl|" + protocolHandlerPackage;
				System.setProperty("java.protocol.handler.pkgs", protocolHandlerPackage);
				logger.debug("Updated HTTP handler packages are: " + protocolHandlerPackage);
			}
		} else {
			logger.debug("java.protocol.handler.pkgs is null, setting it to javax.net.ssl");
			System.setProperty("java.protocol.handler.pkgs", "javax.net.ssl");
		}

		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
		HttpsHandler httpsHandler = null;
		loadConnectionProperties(logger, conProp);
		httpsHandler = new HttpsHandler(conProp, createConnection(logger, conProp, mapHeader));
		return httpsHandler;
	}

	/**
	 * @param certFilePath
	 *            : TrustStore is a JKS bundle
	 * @param passphrase
	 *            : Passphrase to access the bundle
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws IOException
	 * @throws KeyStoreException
	 */
	private static TrustManagerFactory createTrustStore(Logger logger, String certFilePath, String passphrase) throws NoSuchAlgorithmException, CertificateException, IOException,
			KeyStoreException {
		TrustManagerFactory trustMgrFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		KeyStore trustStore = null;
		if (certFilePath.endsWith("p12") || certFilePath.endsWith("P12"))
			trustStore = KeyStore.getInstance("PKCS12");
		else
			trustStore = KeyStore.getInstance("JKS");
		InputStream in = null;
		try {
			in = FileUtils.getResourceAsStream(logger, certFilePath);
			trustStore.load(in, passphrase.toCharArray());
			trustMgrFactory.init(trustStore);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				in.close();
			}
		}
		// new code
		// lets not stop if the validity check fails for some reason
		try {
			checkValidityOfBundle(logger, certFilePath, trustStore);
		} catch (Exception e) {
			logger.error("Error:" + e, e);
		}
		return trustMgrFactory;
	}

	private static TrustManagerFactory createTrustStore(Logger logger, String certFilePath) throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException {
		TrustManagerFactory trustMgrFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		CertificateFactory certFactory = CertificateFactory.getInstance("X.509");

		BufferedReader bfr = null;
		StringBuffer certBuf = new StringBuffer();

		try {
			bfr = new BufferedReader(new FileReader(certFilePath));
			for (String line = bfr.readLine(); line != null; line = bfr.readLine()) {
				certBuf.append(line).append("\n");
			}
		} catch (Exception e) {
			logger.error("Error:" + e, e);
		} finally {
			if (null != bfr) {
				bfr.close();
			}
		}

		ByteArrayInputStream byteinputstream = new ByteArrayInputStream(certBuf.toString().getBytes());
		Collection c = certFactory.generateCertificates(byteinputstream);
		Iterator it = c.iterator();
		String alias = null;
		KeyStore trustStore = KeyStore.getInstance("JKS");
		trustStore.load(null, null);
		while (it.hasNext()) {
			X509Certificate cert = (X509Certificate) it.next();
			alias = cert.getSerialNumber().toString();
			alias += cert.getIssuerDN().getName();
			logger.debug("Found: [" + alias + "]");
			trustStore.setCertificateEntry(alias, cert);
		}
		trustMgrFactory.init(trustStore);
		// new code
		// lets not stop if the validity check fails for some reason
		try {
			checkValidityOfBundle(logger, certFilePath, trustStore);
		} catch (Exception e) {
			logger.error("Error:" + e, e);
		}

		return trustMgrFactory;
	}

	private static KeyManagerFactory createKeyStore(Logger logger, String keyFilePath, String passphrase) throws NoSuchAlgorithmException, KeyStoreException, CertificateException,
			IOException, UnrecoverableKeyException {
		if (keyFilePath == null || passphrase == null)
			return null;
		KeyManagerFactory keyMgrFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		KeyStore keyStore = null;
		if (keyFilePath.endsWith("p12") || keyFilePath.endsWith("P12"))
			keyStore = KeyStore.getInstance("PKCS12");
		else
			keyStore = KeyStore.getInstance("JKS");

		if (keyFilePath != null && !"".equals(keyFilePath.trim())) {
			InputStream in = FileUtils.getResourceAsStream(logger, keyFilePath);
			in = new BufferedInputStream(in);
			keyStore.load(in, passphrase.toCharArray());
			keyMgrFactory.init(keyStore, passphrase.toCharArray());
			// new code
			// lets not stop if the validity check fails for some reason
			try {
				checkValidityOfBundle(logger, keyFilePath, keyStore);
			} catch (Exception e) {
				logger.error("Error:" + e, e);
			}
		} else {
			keyStore.load(null, null);
			keyMgrFactory.init(null, null);
		}

		Enumeration keyEnum = keyStore.aliases();
		String elem = null;
		while (keyEnum.hasMoreElements()) {
			elem = (String) keyEnum.nextElement();
			logger.info(elem);
		}
		return keyMgrFactory;
	}

	/**
	 * Will print info with regards to a certificate in a bundle if its about to
	 * expire in the next 30 days period
	 *
	 * @param storePath
	 * @param keyStore
	 * @throws KeyStoreException
	 */
	private static void checkValidityOfBundle(Logger logger, String storePath, KeyStore keyStore) throws KeyStoreException {
		Enumeration keyEnum = keyStore.aliases();
		String elem = null;
		String threshold = null;
		try {
			threshold = PropertyUtils.getValueFromProperties("CERT_THRESHOLD", PropertiesConstants.REST_CLIENT_PROP.getPropertyFileName());
		} catch (Exception e) {
			logger.error("Error:" + e, e);
		}
		if (threshold == null || threshold.length() == 0) {
			threshold = "30";
		}
		while (keyEnum.hasMoreElements()) {
			elem = (String) keyEnum.nextElement();
			if (keyStore.getCertificate(elem).getType().equals("X.509")) {
				Date expiryDateOfCert = ((X509Certificate) keyStore.getCertificate(elem)).getNotAfter();
				Date todayDate = new Date();
				Calendar calendarExpDate = Calendar.getInstance();
				Calendar calendarToday = Calendar.getInstance();
				calendarExpDate.setTime(expiryDateOfCert);
				calendarToday.setTime(todayDate);
				long millisecondsExpDate = calendarExpDate.getTimeInMillis();
				long millisecondsToday = calendarToday.getTimeInMillis();
				long diff = millisecondsExpDate - millisecondsToday;
				long dayAlert = diff / (24 * 60 * 60 * 1000);
				if (dayAlert < Integer.parseInt(threshold)) {
					logger.info("SSL CERT EXPIRY INFO :: STORE [ " + storePath + " ]  about to expire in " + dayAlert + " days. Certificate alias [ " + elem + " ] "
							+ "Subject DN [" + ((X509Certificate) keyStore.getCertificate(elem)).getSubjectDN() + " ] Issuer DN [ "
							+ ((X509Certificate) keyStore.getCertificate(elem)).getIssuerDN() + " ] Valid FROM [ "
							+ ((X509Certificate) keyStore.getCertificate(elem)).getNotBefore() + " ] " + "TO [ " + ((X509Certificate) keyStore.getCertificate(elem)).getNotAfter()
							+ " ]");
				}
			}
		}
	}
}