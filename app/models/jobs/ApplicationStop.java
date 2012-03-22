package models.jobs;

import java.util.ArrayList;

import models.EventTopic;
import models.ModelManager;
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
