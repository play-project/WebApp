package models;

import java.util.*;

import javax.persistence.*;

import play.Logger;
import play.db.jpa.*;
import play.libs.F.*;

public class EventTopic {
	public String source;
	public String name;
	public String namespace;
	public String title;
	public String content;
	public List<User> subscribingUsers;

	public EventTopic(String namespace, String name, String source, String title, String content) {
		this.namespace = namespace;
		this.name = name;
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
		e.setTopicId(getId());
		for (User u : subscribingUsers) {
			u.getEventBuffer().publish(e);
		}
	}

	public String getId() {
		return namespace + "_" + name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof EventTopic))
			return false;
		EventTopic u = (EventTopic) o;
		if (u.namespace.equals(namespace) && u.name.equals(name)) {
			return true;
		}
		return false;
	}
}
