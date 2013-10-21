package com.mac.register;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Encryptor {
	static byte[] encrypt(String cleartext) {
		MessageDigest sha2 = null;
		try {
			sha2 = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return sha2.digest(cleartext.getBytes());
	}
}