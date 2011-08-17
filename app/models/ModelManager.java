package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.mvc.Controller;

public class ModelManager {

	public static ModelManager instance = null;
	private ArrayList<User> connectedUsers = new ArrayList<User>();
	private ArrayList<EventTopic> streams = new ArrayList<EventTopic>();

	public ModelManager() {
		Logger.info("NEW MODELMANAGER");
		User u = new User("claw", "pwd", "Alex", "test@gmail.com");
		User u2 = new User("claw2", "pwd2", "Alex2", "test2@gmail.com");
		EventTopic eb1 = new EventTopic("topic1", "http://www.wservice.com/stream1", "Stream 1", "A first Stream for tests");
		EventTopic eb2 = new EventTopic("topic2", "http://www.wservice.com/stream1", "Stream 2", "A second Stream for tests");
		EventTopic eb3 = new EventTopic("topic3", "http://www.wservice.com/stream1", "Stream 3", "A third Stream for tests");
		EventTopic eb4 = new EventTopic("topic4", "http://www.wservice.com/stream1", "Stream 4", "A fourth Stream for tests");
		EventTopic eb5 = new EventTopic("topic5", "http://www.wservice.com/stream1", "Stream 5", "A fourth Stream for tests");
		EventTopic eb6 = new EventTopic("topic6", "http://www.wservice.com/stream1", "Stream 6", "A sixth Stream for tests");
		EventTopic eb7 = new EventTopic("topic7", "http://www.wservice.com/stream1", "Stream 7", "A seventh Stream for tests");
		EventTopic eb8 = new EventTopic("topic8", "http://www.wservice.com/stream1", "Stream 8", "A eighth Stream for tests");
		EventTopic eb9 = new EventTopic("topic9", "http://www.wservice.com/stream1", "Stream 9", "A ninth Stream for tests");
		streams.add(eb1);
		streams.add(eb2);
		streams.add(eb3);
		streams.add(eb4);
		streams.add(eb5);
		streams.add(eb6);
		streams.add(eb7);
		streams.add(eb8);
		streams.add(eb9);
		u.eventStreamIds.add(eb1.id);
		u.eventStreamIds.add(eb3.id);
		u2.eventStreamIds.add(eb1.id);
		u2.eventStreamIds.add(eb2.id);
		u2.eventStreamIds.add(eb4.id);
		u.save();
		u2.save();
	}

	public void reset() {
		connectedUsers.clear();
	}

	public static ModelManager get() {
		if (instance == null) {
			instance = new ModelManager();
		}
		return instance;
	}

	public User connect(String login, String password) {
		User u = User.find("byLoginAndPassword", login, password).first();
		Logger.info("u : " + u);
		if (u != null) {
			disconnect(u);
			connectedUsers.add(u);
			u.doSubscribtions();
			u.setEventBuffer(new UserEventBuffer());
		}
		return u;
	}

	public boolean disconnect(User user) {
		if (user != null && connectedUsers.contains(user)) {
			for (int i = 0; i < user.eventStreamIds.size(); i++) {
				EventTopic es = getStreamById(user.eventStreamIds.get(i));
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

	public User getUserById(Long id) {
		for (User u : connectedUsers) {
			if (u.id == id) {
				return u;
			}
		}
		return null;
	}

	public EventTopic getStreamById(String streamId) {
		Logger.info("streamId : " + streamId);
		for (EventTopic eb : streams) {
			Logger.info("id : " + eb.id);
			if (streamId.equals(eb.id)) {
				Logger.info("equal !");
				return eb;
			}
		}
		Logger.info("fail :(");
		return null;
	}

	/**
	 * GETTERS AND SETTERS
	 */

	public ArrayList<User> getConnectedUsers() {
		return connectedUsers;
	}

	public ArrayList<EventTopic> getStreams() {
		return streams;
	}
}
