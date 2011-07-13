package models;

import java.util.*;

import javax.persistence.*;

import play.Logger;
import play.db.jpa.*;
import play.libs.F.*;

@Entity
public class EventStreamMC extends Model {
	@OneToOne
	public StreamDesc desc;
	@Transient
	public List<User> subscribingUsers;

	public EventStreamMC(String source, String title, String content) {
		desc = new StreamDesc(source, title, content);
		desc.save();
		subscribingUsers = new ArrayList<User>();
	}

	public void addUser(User u) {
		subscribingUsers.add(u);
	}

	public void removeUser(User u) {
		subscribingUsers.remove(u);
	}

	public void multicast(Event e) {
		e.setStreamId(id);
		Logger.info("id : " + id);
		for (User u : subscribingUsers) {
			u.getEventBuffer().publish(e);
		}
	}

	/**
	 * GETTERS AND SETTERS
	 */
}
