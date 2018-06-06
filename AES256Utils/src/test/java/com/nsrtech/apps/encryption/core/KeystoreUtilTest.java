package com.nsrtech.apps.encryption.core;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.nsrtech.apps.encryption.core.KeystoreUtil;

import java.security.Key;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * 
 * @author Balaji Nandarapu
 *
 */
public class KeystoreUtilTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void getKeystoreResource() {
    	Key key = KeystoreUtil.getKeyFromKeyStore("src/test/resources/aes-keystore.jck", "tel@ppt1234", "jceksaes", "tel@ppt1234");
        assertThat(key, is(notNullValue()));
    }

    @Test
    public void getKeystoreResourceMissingKeystore() {
        exception.expect(RuntimeException.class);
        KeystoreUtil.getKeyFromKeyStore("notFound.jck", "tel@ppt1234", "jceksaes", "tel@ppt1234");
    }
}
