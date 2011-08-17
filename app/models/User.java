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
	public List<String> eventStreamIds;
	@Transient
	UserEventBuffer eventBuffer;

	public User(String login, String password, String name, String email,
			ArrayList<String> eventStreamIds) {
		this.login = login;
		this.password = password;
		this.name = name;
		this.email = email;
		this.eventStreamIds = eventStreamIds;
		UserEventBuffer eventBuffer = new UserEventBuffer();
	}

	public User(String login, String password, String name, String email) {
		this(login, password, name, email, new ArrayList<String>());
	}

	public EventStreamMC subscribe(String streamId) {
		if (eventStreamIds.contains(streamId)) {
			return null;
		}
		EventStreamMC eb = ModelManager.get().getStreamById(streamId);
		if (eb != null) {
			eb.addUser(this);
			eventStreamIds.add(eb.id);
			this.merge();
			return eb;
		}
		return null;
	}

	public EventStreamMC unsubscribe(String streamId) {
		if (!eventStreamIds.contains(streamId)) {
			return null;
		}
		EventStreamMC eb = ModelManager.get().getStreamById(streamId);
		if (eb != null) {
			eb.removeUser(this);
			eventStreamIds.remove(eb.id);
			this.merge();
			return eb;
		}
		return null;
	}

	public ArrayList<EventStreamMC> getStreams() {
		ArrayList<EventStreamMC> result = new ArrayList<EventStreamMC>();
		for (String sid : eventStreamIds) {
			EventStreamMC es = ModelManager.get().getStreamById(sid);
			if (es != null) {
				result.add(es);
			}
		}
		return result;
	}

	public void doSubscribtions() {
		for (String esid : eventStreamIds) {
			EventStreamMC eb = ModelManager.get().getStreamById(esid);
			if (eb != null) {
				eb.addUser(this);
			}
		}
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
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof User))
			return false;
		User u = (User) o;
		if (u.id.equals(id) && u.name.equals(name) && u.password.equals(password)
				&& u.email.equals(email)) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", login=" + login + ", password=" + password + ", name=" + name
				+ ", email=" + email + "]";
	}
}
