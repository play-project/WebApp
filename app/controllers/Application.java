package controllers;

import play.*;
import play.data.validation.*;
import play.libs.F.IndexedEvent;
import play.libs.F.Promise;
import play.libs.WS;
import play.mvc.*;

import java.util.*;
import java.util.concurrent.ExecutionException;

import org.w3c.dom.Document;

import com.google.gson.reflect.TypeToken;

import models.*;

public class Application extends Controller {

	@Before(unless = { "login", "processLogin", "logout", "test", "test2", "testWS", "returnWS" })
	public static void checkAuthentification() {
		if (session.get("userid") == null) {
			login();
		}
	}

	public static void index() {
		User u = ModelManager.get().getUserById(Long.parseLong(session.get("userid")));
		if (u == null) {
			logout();
		}
		String username = u.name;
		ArrayList<EventTopic> topics = new ArrayList<EventTopic>();
		topics.addAll(ModelManager.get().getTopics());
		ArrayList<EventTopic> userTopics = u.getTopics();
		for (int i = 0; i < userTopics.size(); i++) {
			topics.remove(userTopics.get(i));
		}
		render(username, topics, userTopics);
	}

	public static void login() {
		if (session.get("userid") != null) {
			index();
		}
		render();
	}

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

	public static void sendEvent(@Required String title, @Required String content, @Required String topic) {
		Logger.info("Event: " + title + "\nContent: " + content + "\nTopic " + topic);
		ModelManager.get().getTopicById(topic).multicast(new Event(title, content));
	}

	public static void waitEvents(@Required Long lastReceived) throws InterruptedException,
			ExecutionException {
		User u = ModelManager.get().getUserById(Long.parseLong(session.get("userid")));
		List events = await(u.getEventBuffer().nextEvents(lastReceived));
		renderJSON(events, new TypeToken<List<IndexedEvent<Event>>>() {
		}.getType());
	}

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

	public static void unsubscribe(@Required String topicId) {
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