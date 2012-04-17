package controllers;

import models.ModelManager;
import models.User;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.mvc.Controller;
import securesocial.provider.ProviderType;
import securesocial.provider.SocialUser;

import com.google.gson.JsonObject;

import controllers.securesocial.SecureSocial;

/**
 * Authentification controller Handles OAuth exchanges between third part
 * authentification services (Facebook, Google, ...)
 * 
 * @author Alexandre Bourdin
 * 
 */
public class Authentifier extends Controller {
	
	public static void facebookAuth() {
		SecureSocial.authenticate(ProviderType.facebook);
	}

	public static void googleAuth() {
		SecureSocial.authenticate(ProviderType.google);
	}
	
	public static void twitterAuth() {
		SecureSocial.authenticate(ProviderType.twitter);
	}

	private static String fullURL() {
		String url = "Authentifier." + Thread.currentThread().getStackTrace()[2].getMethodName();
		return play.mvc.Router.getFullUrl(url);
	}

	private static String fullURL(String url) {
		return play.mvc.Router.getFullUrl(url);
	}
}
