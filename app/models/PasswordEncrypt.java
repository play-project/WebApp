package models;

import play.Play;
import play.libs.Crypto;

public class PasswordEncrypt {

	public static String encrypt(String password) {
		String s = Play.configuration.getProperty("crypt.seed") + password;
		return Crypto.encryptAES(s);
	}

}
