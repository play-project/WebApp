package models;

import java.util.*;

import javax.persistence.*;

import play.Logger;
import play.db.jpa.*;
import play.libs.F.*;

@Entity
public class EventStreamMC extends Model {
	public String source;
	@Transient
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
		Logger.info("size : " + subscribingUsers.size());
		for (User u : subscribingUsers) {
			Logger.info(u + "");
			u.getEventBuffer().publish(e);
		}
	}

	/**
	 * GETTERS AND SETTERS
	 */

}
