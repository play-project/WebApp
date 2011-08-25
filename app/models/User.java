package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.*;

import com.google.gson.JsonObject;

import play.Logger;
import play.db.jpa.*;

@Entity
public class User extends Model {
	public String login;
	public String password;
	public String firstname;
	public String lastname;
	public String email;
	public String gender;
    public String fbId;
	@ElementCollection
	public List<String> eventTopicIds;
	@Transient
	UserEventBuffer eventBuffer;
	@Transient
	public String fbAccessToken;

	public User(String login, String password, String firstname, String lastname, String email,
			String gender, String fbId, ArrayList<String> eventTopicIds) {
		this.login = login;
		this.password = password;
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
		this.gender = gender;
		this.fbId = fbId;
		this.eventTopicIds = eventTopicIds;
		UserEventBuffer eventBuffer = new UserEventBuffer();
	}

	public User(String login, String password, String firstname, String lastname, String email,
			String gender, String fbId) {
		this(login, password, firstname, lastname, email, gender, fbId, new ArrayList<String>());
	}

	public EventTopic subscribe(String topicId) {
		if (eventTopicIds.contains(topicId)) {
			return null;
		}
		EventTopic eb = ModelManager.get().getTopicById(topicId);
		if (eb != null) {
			eb.addUser(this);
			eventTopicIds.add(eb.id);
			Collections.sort(eventTopicIds);
			update();
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
			update();
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
	
	private void update(){
		User u = User.find("byId", this.id).first();
		u.login = login;
		u.password = password;
		u.firstname = firstname;
		u.lastname = lastname;
		u.email = email;
		u.gender = gender;
		u.eventTopicIds = eventTopicIds;
		u.save();
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
		if (u.id.equals(id) && u.firstname.equals(firstname) && u.password.equals(password)
				&& u.email.equals(email)) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "User id=" + id + " [login=" + login + ", password=" + password + ", firstname="
				+ firstname + ", lastname=" + lastname + ", email=" + email + ", gender=" + gender
				+ ", eventTopicIds=" + eventTopicIds + "]";
	}

}
