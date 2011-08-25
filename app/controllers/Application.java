package controllers;

import play.*;
import play.data.validation.*;
import play.libs.F.IndexedEvent;
import play.libs.F.Promise;
import play.libs.OAuth2;
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

	/**
	 * Index action, renders the main page of the web application
	 */
	public static void index() {
		User u = ModelManager.get().getUserById(Long.parseLong(session.get("userid")));
		if (u == null) {
			logout();
		}
		JsonObject fbInfo = null;
		if (u.fbId != null) {
			if (u.fbAccessToken != null) {
				fbInfo = WS.url("https://graph.facebook.com/me?access_token=%s", WS.encode(u.fbAccessToken))
						.get().getJson().getAsJsonObject();
			}
			if (u.fbAccessToken == null || fbInfo.get("error") != null) {
				refreshAccessToken(u, fullURL());
				fbInfo = WS.url("https://graph.facebook.com/me?access_token=%s", WS.encode(u.fbAccessToken))
						.get().getJson().getAsJsonObject();
			}
		}
		String username = u.firstname;
		ArrayList<EventTopic> topics = new ArrayList<EventTopic>();
		topics.addAll(ModelManager.get().getTopics());
		ArrayList<EventTopic> userTopics = u.getTopics();
		for (int i = 0; i < userTopics.size(); i++) {
			topics.remove(userTopics.get(i));
		}
		render(username, fbInfo, topics, userTopics);
	}

	/**
	 * Login action, renders the login page
	 */
	public static void login() {
		if (session.get("userid") != null) {
			index();
		}
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

	public static void test() {
		session.put("userid", null);
		User u = ModelManager.get().connect("claw", "pwd");
		session.put("userid", u.id);
		index();
	}

	public static void test2() {
		session.put("userid", null);
		User u = ModelManager.get().connect("claw2", "pwd2");
		session.put("userid", u.id);
		index();
	}

	/**
	 * Login processing action, receives the user's input sent by the login form
	 * 
	 * @param login
	 * @param password
	 */
	public static void processLogin(@Required String login, @Required String password) {
		if (validation.hasErrors()) {
			flash.error("Please enter your login and password.");
			login();
		}
		User u = ModelManager.get().connect(login, password);
		if (u != null) {
			Logger.info("User connected : " + u);
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
	 * @param withFB
	 * @param accessToken
	 */
	public static void register(Boolean withFB, String accessToken) {
		if (withFB) {
			JsonObject fbInfo = null;
			if (accessToken != null) {
				fbInfo = WS.url("https://graph.facebook.com/me?access_token=%s", WS.encode(accessToken))
						.get().getJson().getAsJsonObject();
				Logger.info(fbInfo.toString());
			}
			render(fbInfo);
		}
	}

	public static void processRegistration() {

	}

	/*************************
	 ** Facebook login **
	 *************************/

	public static OAuth2 FACEBOOK = new OAuth2("https://graph.facebook.com/oauth/authorize",
			"https://graph.facebook.com/oauth/access_token", "235987216437776",
			"f2a40e9775f5244924188445fff09d27");

	/**
	 * Authentification via Facebook
	 */
	public static void facebookAuth() {
		if (OAuth2.isCodeResponse()) {
			OAuth2.Response response = FACEBOOK.retrieveAccessToken(fullURL());
			String accessToken = response.accessToken;
			JsonObject fbInfo = null;
			String fbId = null;
			// If user allows application to access his data
			if (accessToken != null) {
				// Get his info
				fbInfo = WS.url("https://graph.facebook.com/me?access_token=%s", WS.encode(accessToken))
						.get().getJson().getAsJsonObject();
				if (fbInfo != null) {
					fbId = fbInfo.get("id").getAsString();
					if (fbId != null) {
						// Find user by Facebook id
						User uByFbId = User.find("byFbId", fbId).first();
						// If user is already facebook-registered
						if (uByFbId != null) {
							// Connect and update his access token
							User u = ModelManager.get().connect(uByFbId.login, uByFbId.password);
							u.fbAccessToken = accessToken;
							if (u != null) {
								Logger.info("User connected with facebook : " + u);
								session.put("userid", u.id);
								index();
							}
							// Else : first time connecting with facebook
							// -> redirect to registration page
						} else {
							register(true, accessToken);
						}
					}
				}
			}
			flash.error("Facebook login procedure has encountered an error.");
			login();
		}
		FACEBOOK.retrieveVerificationCode(fullURL());
	}

	private static String fullURL() {
		String url = "Application." + Thread.currentThread().getStackTrace()[2].getMethodName();
		return play.mvc.Router.getFullUrl(url);
	}

	private static String fullURL(String url) {
		return play.mvc.Router.getFullUrl(url);
	}

	private static void refreshAccessToken(User u, String url) {
		if (OAuth2.isCodeResponse()) {
			OAuth2.Response response = FACEBOOK.retrieveAccessToken(url);
			if (response.accessToken != null) {
				u.fbAccessToken = response.accessToken;
			}
		}
		FACEBOOK.retrieveVerificationCode(url);
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
				result = "{\"id\":\"" + sd.id + "\",\"title\":\"" + sd.title + "\",\"content\":\""
						+ sd.content + "\"}";
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
		checkAuthentification();
		Long id = Long.parseLong(session.get("userid"));
		User u = ModelManager.get().getUserById(id);
		String result = "{\"id\":\"-1\"}";
		if (u != null) {
			EventTopic sd = u.unsubscribe(topicId);
			if (sd != null) {
				result = "{\"id\":\"" + sd.id + "\",\"title\":\"" + sd.title + "\",\"content\":\""
						+ sd.content + "\"}";
			}
		}
		renderJSON(result);
	}
}