package controllers;

import models.GoogleOAuth2;
import models.ModelManager;
import models.OAuth2;
import models.User;
import play.Logger;
import play.Play;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.mvc.Controller;

import com.google.gson.JsonObject;

/**
 * Authentification controller Handles OAuth exchanges between third part
 * authentification services (Facebook, Google, ...)
 * 
 * @author Alexandre Bourdin
 * 
 */
public class Authentifier extends Controller {

	public static OAuth2 FACEBOOK = new OAuth2("https://www.facebook.com/dialog/oauth",
			"https://graph.facebook.com/oauth/access_token",
			Play.configuration.getProperty("facebook.clientid"),
			Play.configuration.getProperty("facebook.secret"), "email");

	/**
	 * Authentification via Facebook
	 */

	public static void facebookAuth() {
		if (OAuth2.isCodeResponse()) {
			OAuth2.Response response = FACEBOOK.retrieveAccessToken(fullURL());
			String fbAccessToken = response.accessToken;
			JsonObject fbInfo = null;
			String fbId = null;
			String fbEmail = null;
			// If user allows application to access his data
			if (fbAccessToken != null) {
				// Get his info
				fbInfo = WS.url("https://graph.facebook.com/me?access_token=%s", WS.encode(fbAccessToken))
						.get().getJson().getAsJsonObject();
				if (fbInfo != null) {
					fbId = fbInfo.get("id").getAsString();
					fbEmail = fbInfo.get("email").getAsString();
					if (fbId != null) {
						// Find user by Facebook id
						User uByFbId = User.find("byFbId", fbId).first();
						User uByFbEmail = User.find("byEmail", fbEmail).first();
						// If user is already fb-registered
						if (uByFbId != null) {
							// Connect and update his access token
							User u = ModelManager.get().connect(uByFbId.email, uByFbId.password);
							u.fbAccessToken = fbAccessToken;
							if (u != null) {
								Logger.info("User connected with facebook : " + u);
								session.put("userid", u.id);
								Application.index();
							}
							// Already registered with fb email, but first time
							// connecting with fb
						} else if (uByFbEmail != null) {
							User u = ModelManager.get().connect(uByFbEmail.email, uByFbEmail.password);
							u.fbId = fbId;
							u.save();
							u.fbAccessToken = fbAccessToken;
							if (u != null) {
								Logger.info("User connected with facebook : " + u);
								session.put("userid", u.id);
								Application.index();
							}
							// Else : first time connecting with fb
							// -> redirect to registration page with infos in
							// session
						} else {
							session.put("fbAccessToken", fbAccessToken);
							session.put("fbId", fbId);
							Application.register();
						}
					}
				}
			}
			flash.error("Facebook login procedure has encountered an error.");
			Application.login();
		}
		FACEBOOK.retrieveVerificationCode(fullURL());
	}

	public static void refreshFbAccessToken(User u, String url) {
		if (OAuth2.isCodeResponse()) {
			OAuth2.Response response = FACEBOOK.retrieveAccessToken(url);
			if (response.accessToken != null) {
				u.fbAccessToken = response.accessToken;
			}
		}
		FACEBOOK.retrieveVerificationCode(url);
	}

	public static GoogleOAuth2 GOOGLE = new GoogleOAuth2("https://accounts.google.com/o/oauth2/auth",
			"https://accounts.google.com/o/oauth2/token", Play.configuration.getProperty("google.clientid"),
			Play.configuration.getProperty("google.secret"), "https://www.googleapis.com/auth/userinfo#email");

	public static void googleAuth() {
		if (GoogleOAuth2.isCodeResponse()) {
			GoogleOAuth2.Response response = GOOGLE.retrieveAccessToken(fullURL());
			String googleAccessToken = response.accessToken;
			HttpResponse googleInfo = null;
			String googleEmail = null;
			// If user allows application to access his data
			if (googleAccessToken != null) {
				// Get his info
				Logger.info("GAT : " + googleAccessToken);
				googleInfo = WS.url("https://www.googleapis.com/userinfo/email?access_token=%s",
						WS.encode(googleAccessToken)).get();
				if (googleInfo != null) {
					googleEmail = googleInfo.getString().split("=")[1];
					googleEmail = googleEmail.split("&")[0];
					if (googleEmail != null) {
						// Find user by Google id
						User uByGoogleEmail = User.find("byEmail", googleEmail).first();
						// If user is already google-registered
						if (uByGoogleEmail != null) {
							// Connect and update his access token
							User u = ModelManager.get()
									.connect(uByGoogleEmail.email, uByGoogleEmail.password);
							u.googleAccessToken = googleAccessToken;
							u.save();
							if (u != null) {
								Logger.info("User connected with google : " + u);
								session.put("userid", u.id);
								Application.index();
							}
							// Already registered with google email, but first
							// time
							// connecting with google
						} else {
							session.put("googleAccessToken", googleAccessToken);
							session.put("googleEmail", googleEmail);
							Application.register();
						}
					}
				}
			}
			flash.error("Google login procedure has encountered an error.");
			Application.login();
		}
		GOOGLE.retrieveVerificationCode(fullURL());
	}

	public static void refreshGoogleAccessToken(User u, String url) {
		if (GoogleOAuth2.isCodeResponse()) {
			GoogleOAuth2.Response response = GOOGLE.retrieveAccessToken(url);
			if (response.accessToken != null) {
				u.googleAccessToken = response.accessToken;
			}
		}
		GOOGLE.retrieveVerificationCode(url);
	}

	private static String fullURL() {
		String url = "Authentifier." + Thread.currentThread().getStackTrace()[2].getMethodName();
		return play.mvc.Router.getFullUrl(url);
	}

	private static String fullURL(String url) {
		return play.mvc.Router.getFullUrl(url);
	}
}
