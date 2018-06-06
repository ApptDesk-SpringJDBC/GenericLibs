package com.nsrtech.apps.encryption.core;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;

/**
 *
 * @author Balaji Nandarapu
 *
 */
public class KeystoreUtil {
	public static Key getKeyFromKeyStore(final String keystoreLocation, final String keystorePass, final String alias, final String keyPass) {
		try {
			InputStream keystoreStream = new FileInputStream(keystoreLocation);
			KeyStore keystore = KeyStore.getInstance("JCEKS");
			keystore.load(keystoreStream, keystorePass.toCharArray());
			if (!keystore.containsAlias(alias)) {
				throw new RuntimeException("Alias for key not found");
			}
			Key key = keystore.getKey(alias, keyPass.toCharArray());
			return key;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
