package models.jobs;

import java.util.Date;
import java.util.Iterator;

import models.ModelManager;
import models.User;
import play.jobs.Every;
import play.jobs.Job;

@Every("15min")
public class TimeoutKicker extends Job {
	public static final int timeoutDelayMs = 600000;

	public void doJob(){
		Date d = new Date();
		Iterator<User> itu = ModelManager.get().getConnectedUsers().iterator();
		while (itu.hasNext()) {
			User u = itu.next();
			if (d.getTime() - u.lastRequest.getTime() > timeoutDelayMs) {
				itu.remove();
			}
		}
	}
}
