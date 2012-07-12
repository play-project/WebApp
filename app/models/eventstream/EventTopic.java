package models.eventstream;

import java.util.*;

import models.ModelManager;
import models.User;
import notifiers.Mails;

import play.Logger;
import play.db.jpa.*;
import play.libs.F.*;

/**
 * Class in charge of multicasting events to subscribers
 * 
 * @author Alexandre Bourdin
 * 
 */
public class EventTopic {
	public String name;
	public String namespace;
	public String uri;
	public String title;
	public String icon;
	public String content;
	public String path;
	public long subscribersCount;
	public String subscriptionID;
	public boolean alreadySubscribedDSB;

	public EventTopic(String namespace, String name, String uri, String title, String icon, String content,
			String path) {
		this.namespace = namespace;
		this.name = name;
		this.uri = uri;
		this.title = title;
		this.icon = icon;
		this.content = content;
		this.path = path;
		this.subscribersCount = 0;
		this.alreadySubscribedDSB = false;
		this.subscriptionID = "";
	}

	/**
	 * Multicasts events to subscribing users. Pushes the event to the web
	 * application for connected users, and/or sends an email if specified in
	 * the account preferences.
	 * 
	 * @param e
	 */
	public void multicast(Event e) {
		e.setTopicId(getId());
		List<User> subscribers = User.find(
				"Select u from User as u inner join u.eventTopicIds as strings where ? in strings", getId())
				.fetch();
		for (User uSub : subscribers) {
			User uCon = ModelManager.get().getUserById(uSub.id);
			if (uCon != null) { // if the user is connected
				uCon.getEventBuffer().publish(e);
				if (uSub.mailnotif.equals("A")) { // if user always wants to
													// receive mails
					Mails.mailEvent(e, uSub);
				}
			} else if (uSub.mailnotif.equals("Y")) { // if user wants to receive
														// mails when
														// disconnected
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
	
	public String getTopicUrl() {
	    String result = this.uri;
	    
	    if (!this.uri.endsWith("/")) {
	        result += '/';
	    }
	    
	    return result + this.name;
	}
	
}
