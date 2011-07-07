package models;

import java.util.*;

import javax.persistence.*;

import play.db.jpa.*;
import play.libs.F.*;

@Entity
public class StreamEventBuffer extends Model {
	public String source;
	@ManyToMany
	public List<User> subscribingUsers;
	@Transient
	public final ArchivedEventStream eventStream = new ArchivedEventStream(50);

	public StreamEventBuffer(String streamSource) {
		this.source = streamSource;
		subscribingUsers = new ArrayList<User>();
	}

	public void addUser(User u) {
		subscribingUsers.add(u);
	}

	public void removeUser(User u) {
		subscribingUsers.remove(u);
	}

	public void publishEvent(Event e) {
		for (User u : subscribingUsers) {
			u.publishEvent(e);
		}
	}
	
	/**
	 * GETTERS AND SETTERS
	 */

	public EventStream getStream() {
		return eventStream.eventStream();
	}
}
