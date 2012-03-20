package controllers;

import play.Play;
import models.*;

public class Security extends Secure.Security {

	static boolean authenticate(String username, String password) {
		return Play.configuration.getProperty("admin.login").equals(username)
				&& Play.configuration.getProperty("admin.password").equals(password);
	}

}
