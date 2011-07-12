package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import play.Logger;
import play.db.jpa.*;
import play.libs.F.ArchivedEventStream;

@Entity
public class User extends Model {
	public String login;
	public String password;
	public String name;
	public String email;
	@ElementCollection
	public List<Long> eventStreamIds;
	@Transient
	UserEventBuffer eventBuffer;

	public User(String login, String password, String name, String email, ArrayList<Long> eventStreamIds) {
		this.login = login;
		this.password = password;
		this.name = name;
		this.email = email;
		this.eventStreamIds = eventStreamIds;
		UserEventBuffer eventBuffer = new UserEventBuffer();
	}

	public User(String login, String password, String name, String email) {
		this(login, password, name, email, new ArrayList<Long>());
	}

	public boolean subscribe(Long streamId) {
		EventStreamMC eb = ModelManager.get().getStreamById(streamId);
		if (eb != null) {
			eb.addUser(this);
			eventStreamIds.add(eb.id);
			return true;
		}
		return false;
	}

	public boolean unsubscribe(Long streamId) {
		EventStreamMC eb = ModelManager.get().getStreamById(streamId);
		if (eb != null) {
			eb.removeUser(this);
			eventStreamIds.remove(eb.id);
			return true;
		}
		return false;
	}

	/**
	 * GETTERS AND SETTERS
	 */

	public UserEventBuffer getEventBuffer() {
		return eventBuffer;
	}

	public void setEventBuffer(UserEventBuffer eventBuffer) {
		this.eventBuffer = eventBuffer;
	}

	@Override
	public String toString() {
		return "User [login=" + login + ", password=" + password + ", name=" + name + ", email=" + email
				+ "]";
	}
}
