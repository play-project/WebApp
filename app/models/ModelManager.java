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
	private List<EventStreamMC> streams = new ArrayList<EventStreamMC>();

	public ModelManager() {
		User u = new User("claw", "pwd", "Alex", "test@gmail.com");
		User u2 = new User("claw2", "pwd", "Alex2", "test@gmail.com");
		EventStreamMC eb = new EventStreamMC("http://www.wservice.com/stream1");
		streams.add(eb);
		eb.save();
		u.eventStreamIds.add(eb.id);
		u2.eventStreamIds.add(eb.id);
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
		if (u != null) {
			Logger.info("connect es : " + u.eventStreamIds.size());
			for(int i=0; i<connectedUsers.size(); i++){
				if(connectedUsers.get(i).id == u.id){
					connectedUsers.remove(i);
					i--;
				}
			}
			connectedUsers.add(u);
			
			//TODO : TEST
			u.eventStreamIds.add(streams.get(0).id);
			
			u.setEventBuffer(new UserEventBuffer());
			EventStreamMC es;
			for (Long id : u.eventStreamIds) {
				es = getStreamById(id);
				if (es != null) {
					es.addUser(u);
				}
			}
		}
		return u;
	}

	public boolean disconnect(User user) {
		if (connectedUsers.contains(user)) {
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
			if (eb.id == id) {
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

	public List<EventStreamMC> getStreams() {
		return streams;
	}
}
