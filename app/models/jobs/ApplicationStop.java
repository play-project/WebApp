package models.jobs;

import java.util.ArrayList;
import java.util.List;

import models.ModelManager;
import models.User;
import models.eventstream.EventTopic;
import play.Logger;
import play.jobs.Job;
import play.jobs.OnApplicationStop;

@OnApplicationStop
public class ApplicationStop extends Job {

	public void doJob() {
		ModelManager.get().unregisterSubscriptions();
		Logger.info("- Application stop -");
	}
}
