package controllers;

import play.*;
import play.data.validation.*;
import play.libs.F.IndexedEvent;
import play.libs.F.Promise;
import play.mvc.*;

import java.util.*;
import java.util.concurrent.ExecutionException;

import com.google.gson.reflect.TypeToken;

import models.*;

public class Application extends Controller {

	@Before(unless = { "login", "processLogin", "logout", "test", "test2" })
	public static void checkAuthentification() {
		if (session.get("userid") == null) {
			login();
		}
	}

	public static void index() {
		User u = ModelManager.get().getUserById(Long.parseLong(session.get("userid")));
		if(u == null){
			logout();
		}
		String username = u.name;
		ArrayList<StreamDesc> streams = ModelManager.get().getAllStreamsDesc();
		ArrayList<StreamDesc> userStreams = u.getStreamsDesc();
		Logger.info("ustreams size : " + userStreams.size());
		render(username, streams, userStreams);
	}

	public static void login() {
		if (session.get("userid") != null) {
			index();
		}
		render();
	}

	public static void logout() {
		session.clear();
		login();
	}

	public static void test() {
		session.put("userid", null);
		User u = ModelManager.get().connect("claw", "pwd");
		Logger.info("u : " + u);
		session.put("userid", u.id);
		index();
	}
	
	public static void test2() {
		session.put("userid", null);
		User u = ModelManager.get().connect("claw2", "pwd2");
		Logger.info("u : " + u);
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
			Logger.info("u : " + u);
			session.put("userid", u.id);
			index();
		} else {
			flash.error("Invalid indentifiers, please try again.");
			login();
		}
	}

	public static void sendEvent(@Required String title, @Required String content, @Required int channel) {
		Logger.info("Event: " + title + " " + content + "on channel " + channel);
		ModelManager.get().getStreams().get(channel).multicast(new Event(title, content));
	}

	public static void waitEvents(@Required Long lastReceived) throws InterruptedException,
			ExecutionException {
		Logger.info("Waiting ...");
		User u = ModelManager.get().getUserById(Long.parseLong(session.get("userid")));
		List events = await(u.getEventBuffer().nextEvents(lastReceived));
		Logger.info("Push !");
		renderJSON(events, new TypeToken<List<IndexedEvent<Event>>>() {}.getType());
	}
}