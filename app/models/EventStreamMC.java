package models;

import java.util.*;

import javax.persistence.*;

import play.db.jpa.*;
import play.libs.F.*;

@Entity
public class EventStreamMC extends Model {
	public String source;
	@ManyToMany
	public List<User> subscribingUsers;

	public EventStreamMC(String streamSource) {
		this.source = streamSource;
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
		for (User u : subscribingUsers) {
			u.publishEvent(e);
		}
	}

	/**
	 * GETTERS AND SETTERS
	 */

}
