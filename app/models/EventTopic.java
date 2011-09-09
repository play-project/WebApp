package models;

import java.util.*;

import javax.persistence.*;

import notifiers.Mails;

import play.Logger;
import play.db.jpa.*;
import play.libs.F.*;

public class EventTopic {
	public String name;
	public String namespace;
	public String uri;
	public String title;
	public String content;
	public List<User> subscribingUsers;

	public EventTopic(String namespace, String name, String uri, String title, String content) {
		this.namespace = namespace;
		this.name = name;
		this.uri = uri;
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
		List<User> subscribers = User.find(
				"Select u from User as u inner join u.eventTopicIds as strings where ? in strings", getId())
				.fetch();
		for (User uSub : subscribers) {
			User uCon = ModelManager.get().getUserById(uSub.id);
			if (uCon != null) {
				uCon.getEventBuffer().publish(e);
			} else {
				Logger.info("Mailing to:" + uSub.email);
				Mails.mailEvent(e, uSub);
			}
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
