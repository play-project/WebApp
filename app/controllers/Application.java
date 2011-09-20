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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.ExecutionException;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import com.ebmwebsourcing.easycommons.xml.XMLHelper;
import com.ebmwebsourcing.wsstar.basefaults.datatypes.impl.impl.WsrfbfModelFactoryImpl;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Subscribe;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.SubscribeResponse;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.refinedabstraction.RefinedWsnbFactory;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.utils.WsnbException;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.impl.impl.WsnbModelFactoryImpl;
import com.ebmwebsourcing.wsstar.resource.datatypes.impl.impl.WsrfrModelFactoryImpl;
import com.ebmwebsourcing.wsstar.resourcelifetime.datatypes.impl.impl.WsrfrlModelFactoryImpl;
import com.ebmwebsourcing.wsstar.resourceproperties.datatypes.impl.impl.WsrfrpModelFactoryImpl;
import com.ebmwebsourcing.wsstar.topics.datatypes.impl.impl.WstopModelFactoryImpl;
import com.ebmwebsourcing.wsstar.wsnb.services.INotificationProducer;
import com.ebmwebsourcing.wsstar.wsnb.services.impl.util.Wsnb4ServUtils;
import com.ebmwebsourcing.wsstar.wsrfbf.services.faults.AbsWSStarFault;

import org.petalslink.dsb.notification.client.http.HTTPNotificationProducerClient;
import org.petalslink.dsb.notification.commons.NotificationException;
import org.petalslink.dsb.notification.commons.NotificationHelper;
import org.w3c.dom.Document;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import models.*;

/**
 * The Application controller is the main controller in charge of all basic
 * tasks and page rendering.
 * 
 * @author Alexandre Bourdin
 * 
 */
public class Application extends Controller {

	/**
	 * Action to call before each action requiring the user to be connected
	 */
	@Before(only = { "index", "settings", "updateSettings", "sendEvent", "subscribe", "unsubscribe",
			"getTopics" })
	private static void checkAuthentification() {
		String uid = session.get("userid");
		if (uid == null) {
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
			logout();
		}
		JsonObject fbInfo = null;
		JsonObject googleInfo = null;
		if (u.fbAccessToken != null) {
			fbInfo = WS.url("https://graph.facebook.com/me?access_token=%s", WS.encode(u.fbAccessToken))
					.get().getJson().getAsJsonObject();
			if (fbInfo.get("error") != null) {
				Authentifier.refreshFbAccessToken(u, fullURL());
				fbInfo = WS.url("https://graph.facebook.com/me?access_token=%s", WS.encode(u.fbAccessToken))
						.get().getJson().getAsJsonObject();
			}
		}
		if (u.googleAccessToken != null) {
			googleInfo = WS
					.url("https://www.googleapis.com/userinfo/email?access_token==%s",
							WS.encode(u.googleAccessToken)).get().getJson().getAsJsonObject();
			if (googleInfo.get("error") != null) {
				Authentifier.refreshGoogleAccessToken(u, fullURL());
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

	private static String fullURL() {
		String url = "Application." + Thread.currentThread().getStackTrace()[2].getMethodName();
		return play.mvc.Router.getFullUrl(url);
	}

	private static String fullURL(String url) {
		return play.mvc.Router.getFullUrl(url);
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
			@Required(message = "Mail notification choice is required") String mailnotif, String code,
			String randomID) {
		String fbId = session.get("fbId");
		String googleId = session.get("googleId");
		if (fbId == null && googleId == null) {
			validation.equals(code, Cache.get(randomID)).message("Invalid code. Please type it again");
		}
		validation.isTrue(User.find("byEmail", email).first() == null).message("Email already in use");
		if (validation.hasErrors()) {
			ArrayList<String> errorMsg = new ArrayList<String>();
			for (Error error : validation.errors()) {
				errorMsg.add(error.message());
			}
			flash.put("error", errorMsg);
			renderTemplate("Application/register.html", email, emailconf, firstname, lastname, gender,
					mailnotif, randomID);
		}
		Cache.delete(randomID);
		User u = new User(email, password, firstname, lastname, gender, mailnotif);
		u.fbId = fbId;
		u.googleId = googleId;
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

	/**
	 * Captcha generator
	 */
	public static void captcha(String id) {
		Images.Captcha captcha = Images.captcha();
		String code = captcha.getText("#33FF6F");
		Cache.set(id, code, "10mn");
		renderBinary(captcha);
	}

	/**
	 * Settings page
	 */
	public static void settings() {
		Long id = Long.parseLong(session.get("userid"));
		User u = ModelManager.get().getUserById(id);
		render(u);
	}

	/**
	 * Update settings
	 */
	public static void updateSettings(String password, String newpassword, String newpasswordconf,
			@Required(message = "First name is required") String firstname,
			@Required(message = "Last name is required") String lastname,
			@Required(message = "Gender is required") String gender,
			@Required(message = "Mail notification choice is required") String mailnotif) {
		Long id = Long.parseLong(session.get("userid"));
		User u = ModelManager.get().getUserById(id);
		if (!newpassword.equals("")) {
			validation.equals(password, u.password).message("Wrong password");
			validation.equals(newpassword, newpasswordconf).message(
					"New password and confirmation don't match.");
			if (!validation.hasErrors()) {
				u.password = newpassword;
			}
		}
		if (validation.hasErrors()) {
			ArrayList<String> errorMsg = new ArrayList<String>();
			for (Error error : validation.errors()) {
				errorMsg.add(error.message());
			}
			flash.put("error", errorMsg);
			settings();
		}
		u.firstname = firstname;
		u.lastname = lastname;
		u.gender = gender;
		u.mailnotif = mailnotif;
		u.update();
		settings();
	}

	/**
	 * Historical events
	 */
	public static void historicalEvents() {
		render();
	}

	/**
	 * Events handlers
	 */

	public static void sendEvent(@Required String title, @Required String content, @Required String topic) {
		ModelManager.get().getTopicById(topic).multicast(new models.Event(title, content));
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

	public static void searchTopics(String search, String title, String desc) {
		ArrayList<EventTopic> topics = ModelManager.get().getTopics();
		boolean searchTitle = Boolean.parseBoolean(title);
		boolean searchContent = Boolean.parseBoolean(desc);
		Long id = Long.parseLong(session.get("userid"));
		User u = ModelManager.get().getUserById(id);
		ArrayList<EventTopic> result = new ArrayList<EventTopic>();
		ArrayList<EventTopic> userTopics = u.getTopics();
		for (int i = 0; i < topics.size(); i++) {
			EventTopic currentTopic = topics.get(i);
			if (!userTopics.contains(currentTopic)) {
				if (search.equals("")) {
					result.add(topics.get(i));
					continue;
				} else {
					if (searchTitle) {
						if (BoyerMoore.match(search, currentTopic.getId()).size() > 0) {
							result.add(topics.get(i));
							continue;
						}
					}
					if (searchContent) {
						if (BoyerMoore.match(search, currentTopic.content).size() > 0) {
							result.add(topics.get(i));
							continue;
						}
					}
				}
			}
		}
		render(result);
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
			EventTopic et = u.subscribe(topicId);
			if (et != null) {
				if (et.subscribersCount < 1) {
					WebService.subscribe(et);
				}
				et.subscribersCount++;
				result = "{\"id\":\"" + et.getId() + "\",\"title\":\"" + et.title + "\",\"icon\":\""
						+ et.icon + "\",\"content\":\"" + et.content + "\",\"path\":\"" + et.path + "\"}";
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
			EventTopic et = u.unsubscribe(topicId);
			if (et != null) {
				et.subscribersCount--;
				/*
				 * TODO : ADD WHEN WE HAVE UNSUBSCRIPTIONS TO THE DSB
				 * if(et.subscribersCount < 1){
				 * WebService.unsubscribe(topicId); }
				 */
				result = "{\"id\":\"" + et.getId() + "\",\"title\":\"" + et.title + "\",\"icon\":\""
						+ et.icon + "\",\"content\":\"" + et.content + "\",\"path\":\"" + et.path + "\"}";
			}
		}
		renderJSON(result);
	}

	public static void about() {
		render();
	}
}