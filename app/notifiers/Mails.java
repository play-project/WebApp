package notifiers;

import play.*;
import play.mvc.*;
import java.util.*;

import models.User;
import models.eventstream.Event;

public class Mails extends Mailer {

	public static void mailEvent(Event e, User u) {
		setSubject("Notification received on topic : %s", e.getTopicId());
		addRecipient(u.email);
		setFrom("noreply@play-project.eu");
		send(e, u);
	}
}
