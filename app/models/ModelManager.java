package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ModelManager {

	public static ModelManager instance = null;
	private ArrayList<User> connectedUsers;
	private List<StreamEventBuffer> streams;

	public ModelManager() {
		connectedUsers = new ArrayList<User>();
		streams = StreamEventBuffer.findAll();

		testInit();
	}

	public void testInit() {

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
			connectedUsers.add(u);
			for (StreamEventBuffer eb : u.eventStreams) {
				eb.addUser(u);
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

	public StreamEventBuffer getStreamById(Long id) {
		for (StreamEventBuffer eb : streams) {
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

	public List<StreamEventBuffer> getStreams() {
		return streams;
	}
}
