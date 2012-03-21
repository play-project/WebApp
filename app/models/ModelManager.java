package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;

import controllers.WebService;

import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.mvc.Controller;
import play.mvc.Scope.Session;

/**
 * This persistant singleton class manages the model part of the web
 * application's MVC pattern. It handles the lists of connected users and
 * available topics.
 * 
 * @author Alexandre Bourdin
 * 
 */
public class ModelManager {

	public static ModelManager instance = null;
	private List<User> connectedUsers = Collections.synchronizedList(new ArrayList<User>());
	private ArrayList<EventTopic> topics = new ArrayList<EventTopic>();

	/**
	 * ModelManager initialization
	 */
	public ModelManager() {
		Logger.info("MODELMANAGER INITIALIZED");

		topics = WebService.getSupportedTopics();
	}

	public static ModelManager get() {
		if (instance == null) {
			instance = new ModelManager();
			instance.initializeSubscriptions();
		}
		return instance;
	}

	public void initializeSubscriptions() {
		for (EventTopic et : topics) {
			et.subscribersCount = User
					.count("Select count(*) from User as u inner join u.eventTopicIds as strings where ? in strings",
							et.getId());
			if (et.subscribersCount > 0) {
				if (WebService.subscribe(et) == 1) {
					et.alreadySubscribedDSB = true;
				}
			}
		}
	}

	/**
	 * User connection method
	 * 
	 * @param login
	 * @param password
	 * @return User
	 */
	public User connect(String email, String password) {
		User u = User.find("byEmailAndPassword", email, PasswordEncrypt.encrypt(password)).first();
		if (u != null) {
			synchronized(connectedUsers){
				disconnect(u);
				connectedUsers.add(u);
			}
			Collections.sort(u.eventTopicIds);
			u.setEventBuffer(new UserEventBuffer());
		}
		return u;
	}

	/**
	 * User disconnection method
	 * 
	 * @param user
	 * @return boolean
	 */
	public boolean disconnect(User user) {
		synchronized (connectedUsers) {
			if (user != null && connectedUsers.contains(user)) {
				connectedUsers.remove(user);
				return true;
			}
			return false;
		}
	}

	public boolean isConnected(User u) {
		if (connectedUsers.contains(u)) {
			return true;
		}
		return false;
	}

	/**
	 * GETTERS AND SETTERS
	 */

	public List<User> getConnectedUsers() {
		return connectedUsers;
	}

	public ArrayList<EventTopic> getTopics() {
		return topics;
	}

	/**
	 * Returns the user having the given id
	 * 
	 * @param id
	 * @return
	 */
	public User getUserById(Long id) {
		for (User u : connectedUsers) {
			if (u.id.equals(id)) {
				return u;
			}
		}
		return null;
	}

	/**
	 * Returns the topic having the given id
	 * 
	 * @param topicId
	 * @return
	 */
	public EventTopic getTopicById(String topicId) {
		for (EventTopic et : topics) {
			if (topicId.equals(et.getId())) {
				return et;
			}
		}
		return null;
	}

	public ArrayList<EventTopic> getMatchingTopics(String search, boolean matchTitle, boolean matchDesc) {
		ArrayList<EventTopic> result = new ArrayList<EventTopic>();
		for (EventTopic et : topics) {
			if (matchTitle) {
				if (BoyerMoore.match(search, et.title).size() > 0) {
					result.add(et);
				}
			}
			if (matchDesc) {
				if (BoyerMoore.match(search, et.content).size() > 0) {
					result.add(et);
				}
			}
		}
		return result;
	}
}
