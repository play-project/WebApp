package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.Model;

public class ModelManager {

	public static ModelManager instance = null;
	private ArrayList<User> connectedUsers = new ArrayList<User>();
	private ArrayList<EventStreamMC> streams = new ArrayList<EventStreamMC>();

	public ModelManager() {
		Logger.info("NEW MODELMANAGER");
		User u = new User("claw", "pwd", "Alex", "test@gmail.com");
		User u2 = new User("claw2", "pwd2", "Alex2", "test2@gmail.com");
		EventStreamMC eb1 = new EventStreamMC("http://www.wservice.com/stream1", "Stream 1", "A first Stream for tests");
		EventStreamMC eb2 = new EventStreamMC("http://www.wservice.com/stream1", "Stream 2", "A second Stream for tests");
		EventStreamMC eb3 = new EventStreamMC("http://www.wservice.com/stream1", "Stream 3", "A third Stream for tests");
		EventStreamMC eb4 = new EventStreamMC("http://www.wservice.com/stream1", "Stream 4", "A fourth Stream for tests");
		streams.add(eb1);
		streams.add(eb2);
		streams.add(eb3);
		streams.add(eb4);
		eb1.save();
		eb2.save();
		eb3.save();
		eb4.save();
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
		if (connectedUsers.contains(user)) {
			for (int i = 0; i < user.eventStreamIds.size(); i++) {
				EventStreamMC es = getStreamById(user.eventStreamIds.get(i));
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

	public EventStreamMC getStreamById(Long id) {
		for (EventStreamMC eb : streams) {
			if (id.equals(eb.id)) {
				return eb;
			}
		}
		return null;
	}

	/**
	 * GETTERS AND SETTERS
	 */

	public ArrayList<User> getConnectedUsers() {
		return connectedUsers;
	}

	public ArrayList<EventStreamMC> getStreams() {
		return streams;
	}
	
	public ArrayList<StreamDesc> getAllStreamsDesc() {
		ArrayList<StreamDesc> result = new ArrayList<StreamDesc>();
		for(EventStreamMC es : streams){
			result.add(es.desc);
		}
		return result;
	}
}
