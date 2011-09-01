package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;

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
 * @author Claw
 * 
 */
public class ModelManager {

	public static ModelManager instance = null;
	private ArrayList<User> connectedUsers = new ArrayList<User>();
	private ArrayList<EventTopic> topics = new ArrayList<EventTopic>();

	/**
	 * ModelManager initialization
	 */
	public ModelManager() {
		Logger.info("MODELMANAGER INITIALIZED");
		/*
		 * EventTopic et1 = new EventTopic("topic1",
		 * "http://www.wservice.com/topic1", "Topic 1",
		 * "A first topic for tests"); EventTopic et2 = new EventTopic("topic2",
		 * "http://www.wservice.com/topic1", "Topic 2",
		 * "A second Topic for tests"); EventTopic et3 = new
		 * EventTopic("topic3", "http://www.wservice.com/topic1", "Topic 3",
		 * "A third Topic for tests"); EventTopic et4 = new EventTopic("topic4",
		 * "http://www.wservice.com/topic1", "Topic 4",
		 * "A fourth Topic for tests"); EventTopic et5 = new
		 * EventTopic("topic5", "http://www.wservice.com/topic1", "Topic 5",
		 * "A fourth Topic for tests"); EventTopic et6 = new
		 * EventTopic("topic6", "http://www.wservice.com/topic1", "Topic 6",
		 * "A sixth Topic for tests"); EventTopic et7 = new EventTopic("topic7",
		 * "http://www.wservice.com/topic1", "Topic 7",
		 * "A seventh Topic for tests"); EventTopic et8 = new
		 * EventTopic("topic8", "http://www.wservice.com/topic1", "Topic 8",
		 * "A eighth Topic for tests"); EventTopic et9 = new
		 * EventTopic("topic9", "http://www.wservice.com/topic1", "Topic 9",
		 * "A ninth Topic for tests");
		 * 
		 * topics.add(et1); topics.add(et2); topics.add(et3); topics.add(et4);
		 * topics.add(et5); topics.add(et6); topics.add(et7); topics.add(et8);
		 * topics.add(et9);
		 */
		topics = SupportedTopicsXML.getSupportedTopics();
	}

	public static ModelManager get() {
		if (instance == null) {
			instance = new ModelManager();
		}
		return instance;
	}

	/**
	 * User connection method
	 * 
	 * @param login
	 * @param password
	 * @return User
	 */
	public User connect(String email, String password) {
		User u = User.find("byEmailAndPassword", email, password).first();
		Logger.info("u : " + u);
		if (u != null) {
			disconnect(u);
			connectedUsers.add(u);
			Collections.sort(u.eventTopicIds);
			u.doSubscribtions();
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
		if (user != null && connectedUsers.contains(user)) {
			for (int i = 0; i < user.eventTopicIds.size(); i++) {
				EventTopic es = getTopicById(user.eventTopicIds.get(i));
				if (es != null) {
					es.removeUser(user);
				}
			}
			connectedUsers.remove(user);
			return true;
		}
		return false;
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

	public ArrayList<User> getConnectedUsers() {
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
