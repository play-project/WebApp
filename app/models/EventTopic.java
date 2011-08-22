package models;

import java.util.*;

import javax.persistence.*;

import play.Logger;
import play.db.jpa.*;
import play.libs.F.*;

public class EventTopic {
	public String id;
	public String source;
	public String title;
	public String content;
	public List<User> subscribingUsers;

	public EventTopic(String id, String source, String title, String content) {
		this.id = id;
		this.source = source;
		this.title = title;
		this.content = content;
		this.subscribingUsers = new ArrayList<User>();
	}

	public void addUser(User u) {
		subscribingUsers.add(u);
	}

	public void removeUser(User u) {
		subscribingUsers.remove(u);
	}

	public void multicast(Event e) {
		e.setTopicId(id);
		for (User u : subscribingUsers) {
			u.getEventBuffer().publish(e);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof EventTopic))
			return false;
		EventTopic u = (EventTopic) o;
		if (u.id.equals(id) && u.source.equals(source)) {
			return true;
		}
		return false;
	}
}
