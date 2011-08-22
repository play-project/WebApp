package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import com.google.gson.JsonObject;

import play.Logger;
import play.db.jpa.*;

@Entity
public class User extends Model {
	public String login;
	public String password;
	public String name;
	public String email;
	@ElementCollection
	public List<String> eventTopicIds;
	@Transient
	UserEventBuffer eventBuffer;

	public User(String login, String password, String name, String email,
			ArrayList<String> eventTopicIds) {
		this.login = login;
		this.password = password;
		this.name = name;
		this.email = email;
		this.eventTopicIds = eventTopicIds;
		UserEventBuffer eventBuffer = new UserEventBuffer();
	}

	public User(String login, String password, String name, String email) {
		this(login, password, name, email, new ArrayList<String>());
	}

	public EventTopic subscribe(String topicId) {
		if (eventTopicIds.contains(topicId)) {
			return null;
		}
		EventTopic eb = ModelManager.get().getTopicById(topicId);
		if (eb != null) {
			eb.addUser(this);
			eventTopicIds.add(eb.id);
			this.merge();
			return eb;
		}
		return null;
	}

	public EventTopic unsubscribe(String topicId) {
		if (!eventTopicIds.contains(topicId)) {
			return null;
		}
		EventTopic eb = ModelManager.get().getTopicById(topicId);
		if (eb != null) {
			eb.removeUser(this);
			eventTopicIds.remove(eb.id);
			this.merge();
			return eb;
		}
		return null;
	}

	public ArrayList<EventTopic> getTopics() {
		ArrayList<EventTopic> result = new ArrayList<EventTopic>();
		for (String sid : eventTopicIds) {
			EventTopic es = ModelManager.get().getTopicById(sid);
			if (es != null) {
				result.add(es);
			}
		}
		return result;
	}

	public void doSubscribtions() {
		for (String esid : eventTopicIds) {
			EventTopic eb = ModelManager.get().getTopicById(esid);
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
