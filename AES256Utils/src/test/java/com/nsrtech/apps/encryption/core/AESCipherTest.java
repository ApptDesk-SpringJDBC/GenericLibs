package com.nsrtech.apps.encryption.core;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.nsrtech.apps.encryption.core.AESCipher;
import com.nsrtech.apps.encryption.core.KeystoreUtil;

/**
 * 
 * @author Balaji Nandarapu
 *
 */
public class AESCipherTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void encryptMessage() throws UnsupportedEncodingException,InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException ,InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {

		String key = "770A8A65DA156D24EE2A093277530142";
		AESCipher cipher = new AESCipher(key.getBytes("UTF-8"));

		String encryptedMessage = cipher.encrypt("this is message");
		System.out.println("Message is: "+encryptedMessage);

		assertThat(encryptedMessage, is(notNullValue()));
		assertThat(encryptedMessage, is(not("this is message")));
	}

	@Test
	public void decryptMessage() throws UnsupportedEncodingException,InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException  {

		String key = "770A8A65DA156D24EE2A093277530142";
		AESCipher cipher = new AESCipher(key.getBytes("UTF-8"));

		String messageToEncrypt = "this is the secret message I want to encode";

		String encryptedMessage = cipher.encrypt(messageToEncrypt);
		String decryptedMessage = cipher.decrypt(encryptedMessage);

		System.out.println("Original Message: "+messageToEncrypt+", Encrypted Message: "+encryptedMessage+", Decrypted Message: "+decryptedMessage);
		assertThat(decryptedMessage, is(messageToEncrypt));
	}

	@Test
	public void encryptMessageFromKeystore() throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException  {
		Key key = KeystoreUtil.getKeyFromKeyStore("src/test/resources/aes-keystore.jck", "tel@ppt1234", "jceksaes", "tel@ppt1234");
		AESCipher cipher = new AESCipher(key);

		String encryptedMessage = cipher.encrypt("this is message");
		System.out.println("Message is: "+encryptedMessage);

		assertThat(encryptedMessage, is(notNullValue()));
		assertThat(encryptedMessage, is(not("this is message")));
	}

	@Test
	public void decryptMessageFromKeystore() throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException  {

		Key key = KeystoreUtil.getKeyFromKeyStore("src/test/resources/aes-keystore.jck", "tel@ppt1234", "jceksaes", "tel@ppt1234");
		AESCipher cipher = new AESCipher(key);

		String messageToEncrypt = "this is the secret message I want to encode";

		String encryptedMessage = cipher.encrypt(messageToEncrypt);
		String decryptedMessage = cipher.decrypt(encryptedMessage);

		System.out.println("Original Message: "+messageToEncrypt+", Encrypted Message: "+encryptedMessage+", Decrypted Message: "+decryptedMessage);
		assertThat(decryptedMessage, is(messageToEncrypt));
	}

	@Test
	public void usingStringBasedIV() throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {

		Key key = KeystoreUtil.getKeyFromKeyStore("src/test/resources/aes-keystore.jck", "tel@ppt1234", "jceksaes", "tel@ppt1234");
		String iv = "NBDY37FHETIJSSIG";
		            

		AESCipher cipher = new AESCipher(key, iv.getBytes());

		String encryptedMessage = cipher.encrypt("1231231235");
		System.out.println("Message is: "+encryptedMessage);
		
		String decryptedMessage = cipher.decrypt(encryptedMessage);
		System.out.println("Message is: "+decryptedMessage);

		assertThat(encryptedMessage, is(notNullValue()));
		assertThat(encryptedMessage, is(not("this is message")));
	}

	@Test
	public void usingStringBasedIVWithUTF8ExtendedCharacter() throws UnsupportedEncodingException,InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException  {

		thrown.expectCause(isA(InvalidAlgorithmParameterException.class));
		thrown.expectMessage("Wrong IV length: must be 16 bytes long");

		Key key = KeystoreUtil.getKeyFromKeyStore("src/test/resources/aes-keystore.jck", "tel@ppt1234", "jceksaes", "tel@ppt1234");
		String iv = "111111111111111\u4111";

		AESCipher cipher = new AESCipher(key, iv.getBytes("UTF-8"));

		String encryptedMessage = cipher.encrypt("this is message");
		System.out.println("Message is: "+encryptedMessage);

		assertThat(encryptedMessage, is(notNullValue()));
		assertThat(encryptedMessage, is(not("this is message")));
	}

	@Test
	public void usingStringBasedIVWithIncorrectLength() throws UnsupportedEncodingException,InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException  {

		thrown.expectCause(isA(InvalidAlgorithmParameterException.class));
		thrown.expectMessage("Wrong IV length: must be 16 bytes long");

		Key key = KeystoreUtil.getKeyFromKeyStore("src/test/resources/aes-keystore.jck", "tel@ppt1234", "jceksaes", "tel@ppt1234");
		String iv = "11111111";

		AESCipher cipher = new AESCipher(key, iv.getBytes("UTF-8"));

		cipher.encrypt("this is message");
	}

	@Test
	public void encryptMessageFromKeystoreWithIv() throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException  {

		Key key = KeystoreUtil.getKeyFromKeyStore("src/test/resources/aes-keystore.jck", "tel@ppt1234", "jceksaes", "tel@ppt1234");
		byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

		AESCipher cipher = new AESCipher(key, iv);

		String encryptedMessage = cipher.encrypt("this is message");
		System.out.println("Message is: "+encryptedMessage);

		assertThat(encryptedMessage, is(notNullValue()));
		assertThat(encryptedMessage, is(not("this is message")));
	}

	@Test
	public void decryptMessageFromKeystoreWithIv() throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException  {

		Key key = KeystoreUtil.getKeyFromKeyStore("src/test/resources/aes-keystore.jck", "tel@ppt1234", "jceksaes", "tel@ppt1234");
		byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

		AESCipher cipher = new AESCipher(key, iv);

		String messageToEncrypt = "this is the secret message I want to encode";

		String encryptedMessage = cipher.encrypt(messageToEncrypt);
		String decryptedMessage = cipher.decrypt(encryptedMessage);

		System.out.println("Original Message: "+messageToEncrypt+", Encrypted Message: "+encryptedMessage+", Decrypted Message: "+decryptedMessage);
		assertThat(decryptedMessage, is(messageToEncrypt));
	}

	@Test
	public void encryptDecryptUsingDifferentCiphersSameIV() throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {

		Key key = KeystoreUtil.getKeyFromKeyStore("src/test/resources/aes-keystore.jck", "tel@ppt1234", "jceksaes", "tel@ppt1234");
		byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		byte[] differentIV = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

		AESCipher cipher = new AESCipher(key, iv);
		AESCipher differentCipher = new AESCipher(key, differentIV);

		String messageToEncrypt = "this is the secret message I want to encode";

		String encryptedMessage = cipher.encrypt(messageToEncrypt);
		String decryptedMessage = differentCipher.decrypt(encryptedMessage);

		System.out.println("Original Message: "+messageToEncrypt+", Encrypted Message: "+encryptedMessage+", Decrypted Message: "+decryptedMessage);
		assertThat(decryptedMessage, is(messageToEncrypt));
	}

	@Test
	public void encryptDecryptUsingDifferentCiphersDifferentIV() throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException  {

		Key key = KeystoreUtil.getKeyFromKeyStore("src/test/resources/aes-keystore.jck", "tel@ppt1234", "jceksaes", "tel@ppt1234");
		byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		byte[] differentIV = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };

		AESCipher cipher = new AESCipher(key, iv);
		AESCipher differentCipher = new AESCipher(key, differentIV);

		String messageToEncrypt = "this is the secret message I want to encode";

		String encryptedMessage = cipher.encrypt(messageToEncrypt);
		String decryptedMessage = differentCipher.decrypt(encryptedMessage);

		System.out.println("Encrypted: ["+encryptedMessage+"], Decrypted["+decryptedMessage+"]");

		assertThat("Original message should have not been the same after decoding with a different IV", decryptedMessage, is(not(messageToEncrypt)));
	}

}
