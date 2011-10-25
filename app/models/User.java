package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.*;

import com.google.gson.JsonObject;

import play.Logger;
import play.db.jpa.*;

/**
 * User model and Entity class, used by JPA to save user data into the connected
 * database
 * 
 * @author Alexandre Bourdin
 * 
 */
@Entity
@Table(name = "users")
public class User extends Model {
	public String email;
	public String password;
	public String firstname;
	public String lastname;
	public String gender;
	public String fbId;
	public String googleEmail;
	public String mailnotif;
	@ElementCollection
	public List<String> eventTopicIds;
	@Transient
	UserEventBuffer eventBuffer;
	@Transient
	public String fbAccessToken;
	public String googleAccessToken;

	public User(String email, String password, String firstname, String lastname, String gender,
			String mailnotif, ArrayList<String> eventTopicIds) {
		this.email = email;
		this.password = password;
		this.firstname = firstname;
		this.lastname = lastname;
		this.gender = gender;
		this.fbId = null;
		this.googleEmail = null;
		this.eventTopicIds = eventTopicIds;
		this.mailnotif = mailnotif;
		UserEventBuffer eventBuffer = new UserEventBuffer();
	}

	public User(String email, String password, String firstname, String lastname, String gender,
			String mailnotif) {
		this(email, password, firstname, lastname, gender, mailnotif, new ArrayList<String>());
	}

	/**
	 * Subscribe to a topic
	 * 
	 * @param et
	 * @return
	 */
	public boolean subscribe(EventTopic et) {
		if (eventTopicIds.contains(et.getId())) {
			return false;
		}
		eventTopicIds.add(et.getId());
		Collections.sort(eventTopicIds);
		et.subscribersCount++;
		update();
		return true;
	}

	/**
	 * Unsubscribe to a topic
	 * 
	 * @param topicId
	 * @return
	 */
	public boolean unsubscribe(EventTopic et) {
		if (!eventTopicIds.contains(et.getId())) {
			return false;
		}
		eventTopicIds.remove(et.getId());
		et.subscribersCount--;
		update();
		return true;
	}

	/**
	 * Get topics the user has subscribed to
	 * 
	 * @return
	 */
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

	/**
	 * Do subscriptions to all topics Called on user connection to add user to
	 * the subscribing user list of all topics he has subscribed to (avoids
	 * database calls)
	 * 
	 * public void doSubscriptions() { for (String esid : eventTopicIds) {
	 * EventTopic eb = ModelManager.get().getTopicById(esid); if (eb != null) {
	 * eb.addUser(this); } } }
	 */

	public void update() {
		User u = User.find("byId", this.id).first();
		u.email = email;
		u.password = password;
		u.firstname = firstname;
		u.lastname = lastname;
		u.gender = gender;
		u.mailnotif = mailnotif;
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
		return "User id=" + id + " [email=" + email + ", firstname=" + firstname + ", lastname=" + lastname
				+ ", gender=" + gender + ", eventTopicIds=" + eventTopicIds + "]";
	}

}
