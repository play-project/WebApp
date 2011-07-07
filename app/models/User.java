package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import play.db.jpa.*;
import play.libs.F.ArchivedEventStream;

@Entity
public class User extends Model {
	public String login;
	public String password;
	public String name;
	public String email;
	@ManyToMany(mappedBy = "subscribingUsers")
	public List<StreamEventBuffer> eventStreams;
	@Transient private UserEventBuffer eventBuffer;

	public User(String login, String password, String name, String email) {
		this.login = login;
		this.password = password;
		this.name = name;
		this.email = email;
		this.eventStreams = new ArrayList<StreamEventBuffer>();
		this.eventBuffer = new UserEventBuffer();
	}

	public boolean subscribe(Long streamId) {
		StreamEventBuffer eb = ModelManager.get().getStreamById(streamId);
		if (eb != null) {
			eb.addUser(this);
			eventStreams.add(eb);
			return true;
		}
		return false;
	}

	public boolean unsubscribe(Long streamId) {
		StreamEventBuffer eb = ModelManager.get().getStreamById(streamId);
		if (eb != null) {
			eb.removeUser(this);
			eventStreams.remove(eb);
			return true;
		}
		return false;
	}
	
	public void publishEvent(Event e){
		eventBuffer.publish(e);
	}

	/**
	 * GETTERS AND SETTERS
	 */
	
	public UserEventBuffer getEventBuffer() {
		return eventBuffer;
	}
}
