package controllers;

import play.*;
import play.cache.Cache;
import play.data.validation.*;
import play.data.validation.Error;
import play.libs.Codec;
import play.libs.F.IndexedEvent;
import play.libs.F.Promise;
import play.libs.Images;
import play.libs.WS;
import play.mvc.*;

import java.util.*;
import java.util.concurrent.ExecutionException;

import org.w3c.dom.Document;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import models.*;

public class Application extends Controller {

	/**
	 * Action to call before each action requiring the user to be connected
	 */
	@Before(only = { "index", "sendEvent", "waitEvents", "subscribe", "unsubscribe" })
	private static void checkAuthentification() {
		if (session.get("userid") == null) {
			login();
		}
	}

	@Before(only = { "register", "processRegistration", "login", "processLogin" })
	private static void checkIsConnected() {
		if (session.get("userid") != null) {
			index();
		}
	}

	/**
	 * Index action, renders the main page of the web application
	 */
	public static void index() {
		User u = ModelManager.get().getUserById(Long.parseLong(session.get("userid")));
		if (u == null) {
			Logger.info("uid : " + session.get("userid"));
			logout();
		}
		JsonObject fbInfo = null;
		JsonObject googleInfo = null;
		if (u.fbAccessToken != null) {
			fbInfo = WS.url("https://graph.facebook.com/me?access_token=%s", WS.encode(u.fbAccessToken))
					.get().getJson().getAsJsonObject();
			if (fbInfo.get("error") != null) {
				refreshFbAccessToken(u, fullURL());
				fbInfo = WS.url("https://graph.facebook.com/me?access_token=%s", WS.encode(u.fbAccessToken))
						.get().getJson().getAsJsonObject();
			}
		}
		if (u.googleAccessToken != null) {
			googleInfo = WS
					.url("https://www.googleapis.com/userinfo/email?access_token==%s",
							WS.encode(u.googleAccessToken)).get().getJson().getAsJsonObject();
			if (googleInfo.get("error") != null) {
				refreshGoogleAccessToken(u, fullURL());
				googleInfo = WS
						.url("https://www.googleapis.com/userinfo/email?access_token==%s",
								WS.encode(u.googleAccessToken)).get().getJson().getAsJsonObject();
			}
		}
		ArrayList<EventTopic> topics = new ArrayList<EventTopic>();
		topics.addAll(ModelManager.get().getTopics());
		ArrayList<EventTopic> userTopics = u.getTopics();
		for (int i = 0; i < userTopics.size(); i++) {
			topics.remove(userTopics.get(i));
		}
		render(u, fbInfo, topics, userTopics);
	}

	/**
	 * Login action, renders the login page
	 */
	public static void login() {
		render();
	}

	/**
	 * Logout action
	 */
	public static void logout() {
		if (session.get("userid") != null) {
			User u = ModelManager.get().getUserById(Long.parseLong(session.get("userid")));
			ModelManager.get().disconnect(u);
		}
		session.clear();
		login();
	}

	/**
	 * Login processing action, receives the user's input sent by the login form
	 * 
	 * @param login
	 * @param password
	 */
	public static void processLogin(@Required String email, @Required String password) {
		if (validation.hasErrors()) {
			flash.error("Please enter your email and password.");
			login();
		}
		User u = ModelManager.get().connect(email, password);
		if (u != null) {
			Logger.info("User connected with standard login : " + u);
			session.put("userid", u.id);
			index();
		} else {
			flash.error("Invalid indentifiers, please try again.");
			login();
		}
	}

	/**
	 * Register action, renders the registration page
	 * 
	 * @param accessToken
	 */
	public static void register() {
		String randomID = Codec.UUID();
		String fbAccessToken = session.get("fbAccessToken");
		if (fbAccessToken != null) {
			JsonObject fbInfo = null;
			fbInfo = WS
					.url("https://graph.facebook.com/me?access_token=%s&perms=email",
							WS.encode(fbAccessToken)).get().getJson().getAsJsonObject();
			Logger.info(fbInfo.toString());
			render(fbInfo, randomID);
		}
		render(randomID);
	}

	public static void processRegistration(
			@Required(message = "Email is required") @Email(message = "Invalid email") @Equals("emailconf") String email,
			@Required(message = "Email confirmation is required") @Email String emailconf,
			@Required(message = "Password is required") @Equals("passwordconf") String password,
			@Required(message = "Passsword confirmation is required") String passwordconf,
			@Required(message = "First name is required") String firstname,
			@Required(message = "Last name is required") String lastname,
			@Required(message = "Gender is required") String gender,
			@Required(message = "Please type the code") String code, String randomID) {
		validation.equals(code, Cache.get(randomID)).message("Invalid code. Please type it again");
		validation.isTrue(User.find("byEmail", email).first() == null).message("Email already in use");
		if (validation.hasErrors()) {
			ArrayList<String> errorMsg = new ArrayList<String>();
			for (Error error : validation.errors()) {
				errorMsg.add(error.message());
			}
			flash.put("error", errorMsg);
			renderTemplate("Application/register.html", email, emailconf, firstname, lastname, gender,
					randomID);
		}
		Cache.delete(randomID);
		User u = new User(email, password, firstname, lastname, gender);
		u.fbId = session.get("fbId");
		u.googleId = session.get("googleId");
		u.create();
		// Connect
		User uc = ModelManager.get().connect(u.email, u.password);
		if (u != null) {
			Logger.info("User registered : " + uc);
			u.fbAccessToken = session.get("fbAccessToken");
			session.put("userid", uc.id);
		}
		index();
	}

	/*************************
	 ** Facebook login **
	 *************************/

	public static OAuth2 FACEBOOK = new OAuth2("https://www.facebook.com/dialog/oauth",
			"https://graph.facebook.com/oauth/access_token", "235987216437776",
			"f2a40e9775f5244924188445fff09d27", "email");

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
						// If user is already facebook-registered
						if (uByFbId != null) {
							// Connect and update his access token
							User u = ModelManager.get().connect(uByFbId.email, uByFbId.password);
							u.fbAccessToken = fbAccessToken;
							if (u != null) {
								Logger.info("User connected with facebook : " + u);
								session.put("userid", u.id);
								index();
							}
							// Else : first time connecting with facebook
							// -> redirect to registration page with infos in
							// session
						} else if (uByFbEmail != null) {
							User u = ModelManager.get().connect(uByFbEmail.email, uByFbEmail.password);
							u.fbId = fbId;
							u.fbAccessToken = fbAccessToken;
							if (u != null) {
								Logger.info("User connected with facebook : " + u);
								session.put("userid", u.id);
								index();
							}

						} else {
							session.put("fbAccessToken", fbAccessToken);
							session.put("fbId", fbId);
							register();
						}
					}
				}
			}
			flash.error("Facebook login procedure has encountered an error.");
			login();
		}
		FACEBOOK.retrieveVerificationCode(fullURL());
	}

	public static OAuth2 GOOGLE = new OAuth2("https://accounts.google.com/o/oauth2/auth",
			"https://accounts.google.com/o/oauth2/token", "554450533169.apps.googleusercontent.com",
			"4wCqvN8pIPoPjNAI-YnV565J",
			"https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email");

	// https://www.googleapis.com/auth/userinfo#email

	public static void googleAuth() {
		Logger.info("googleAuth");
		if (OAuth2.isCodeResponse()) {
			OAuth2.Response response = GOOGLE.retrieveAccessToken(fullURL());
			Logger.info("response : " + response.httpResponse.getString());
			String googleAccessToken = response.accessToken;
			Logger.info("GAT : " + googleAccessToken);
			JsonObject googleInfo = null;
			String googleId = null;
			// If user allows application to access his data
			if (googleAccessToken != null) {
				// Get his info
				Logger.info("GAT != NULL");
				googleInfo = WS
						.url("https://www.googleapis.com/userinfo/email?access_token=%s",
								WS.encode(googleAccessToken)).get().getJson().getAsJsonObject();
				if (googleInfo != null) {
					Logger.info("<googleInfo>");
					Logger.info(googleInfo.getAsString());
					Logger.info("</googleInfo>");
					googleId = googleInfo.get("id").getAsString();
					if (googleId != null) {
						// Find user by Facebook id
						User uByGoogleId = User.find("byGoogleId", googleId).first();
						// If user is already facebook-registered
						if (uByGoogleId != null) {
							// Connect and update his access token
							User u = ModelManager.get().connect(uByGoogleId.email, uByGoogleId.password);
							u.googleAccessToken = googleAccessToken;
							if (u != null) {
								Logger.info("User connected with google : " + u);
								session.put("userid", u.id);
								index();
							}
							// Else : first time connecting with google
							// -> redirect to registration page with infos in
							// session
						} else {
							session.put("googleAccessToken", googleAccessToken);
							session.put("googleId", googleId);
							register();
						}
					}
				}
			}
			flash.error("Google login procedure has encountered an error.");
			login();
		}
		GOOGLE.retrieveVerificationCode(fullURL());
	}

	private static String fullURL() {
		String url = "Application." + Thread.currentThread().getStackTrace()[2].getMethodName();
		return play.mvc.Router.getFullUrl(url);
	}

	private static String fullURL(String url) {
		return play.mvc.Router.getFullUrl(url);
	}

	private static void refreshFbAccessToken(User u, String url) {
		if (OAuth2.isCodeResponse()) {
			OAuth2.Response response = FACEBOOK.retrieveAccessToken(url);
			if (response.accessToken != null) {
				u.fbAccessToken = response.accessToken;
			}
		}
		FACEBOOK.retrieveVerificationCode(url);
	}

	private static void refreshGoogleAccessToken(User u, String url) {
		if (OAuth2.isCodeResponse()) {
			OAuth2.Response response = GOOGLE.retrieveAccessToken(url);
			if (response.accessToken != null) {
				u.googleAccessToken = response.accessToken;
			}
		}
		GOOGLE.retrieveVerificationCode(url);
	}

	/**
	 * Events handlers
	 */

	public static void sendEvent(@Required String title, @Required String content, @Required String topic) {
		Logger.info("Event: " + title + "\nContent: " + content + "\nTopic " + topic);
		ModelManager.get().getTopicById(topic).multicast(new Event(title, content));
	}

	/**
	 * Long polling action called by the frontend page via AJAX Returns events
	 * to the page in a JSON list of events
	 * 
	 * @param lastReceived
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static void waitEvents(@Required Long lastReceived) throws InterruptedException,
			ExecutionException {
		User u = ModelManager.get().getUserById(Long.parseLong(session.get("userid")));
		if (u == null) {
			renderJSON("{\"error\":\"disconnected\"}");
		}
		List events = await(u.getEventBuffer().nextEvents(lastReceived));
		renderJSON(events, new TypeToken<List<IndexedEvent<Event>>>() {
		}.getType());
	}

	/**
	 * Subscription action
	 * 
	 * @param topicId
	 */
	public static void subscribe(@Required String topicId) {
		Long id = Long.parseLong(session.get("userid"));
		User u = ModelManager.get().getUserById(id);
		String result = "{\"id\":\"-1\"}";
		if (u != null) {
			EventTopic sd = u.subscribe(topicId);
			if (sd != null) {
				result = "{\"id\":\"" + sd.getId() + "\",\"title\":\"" + sd.title
						+ "\",\"content\":\"" + sd.content + "\"}";
			}
		}
		renderJSON(result);
	}

	/**
	 * Unsubscription action
	 * 
	 * @param topicId
	 */
	public static void unsubscribe(@Required String topicId) {
		Long id = Long.parseLong(session.get("userid"));
		User u = ModelManager.get().getUserById(id);
		String result = "{\"id\":\"-1\"}";
		if (u != null) {
			EventTopic sd = u.unsubscribe(topicId);
			if (sd != null) {
				result = "{\"id\":\"" + sd.getId() + "\",\"title\":\"" + sd.title + "\",\"content\":\""
						+ sd.content + "\"}";
			}
		}
		renderJSON(result);
	}

	/**
	 * Captcha generator
	 */
	public static void captcha(String id) {
		Images.Captcha captcha = Images.captcha();
		String code = captcha.getText("#33FF6F");
		Cache.set(id, code, "10mn");
		renderBinary(captcha);
	}
}