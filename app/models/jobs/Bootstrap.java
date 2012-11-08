package models.jobs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import models.ModelManager;
import models.User;
import play.Logger;
import play.jobs.*;
import play.mvc.Router;

/**
 * Bootstrap commands on application start
 * 
 * @author Alexandre Bourdin
 *
 */
@OnApplicationStart
public class Bootstrap extends Job {
    
    public void doJob() {
    	Logger.info("- Application Start -");

		// Generate dynamic route for this application lifetime to avoid receiving events from old subscriptions:
		Router.prependRoute("POST", "/webservice/soapnotifendpoint" + Math.abs(new Random().nextLong()) + "/{topicId}", "WebService.soapNotifEndPoint", "", "");
		Map params = new HashMap<String, Object>();
		params.put("topicId", "someTopicId");
		String notificationsEndPoint = Router.getFullUrl("WebService.soapNotifEndPoint",
				params);
		Logger.info("Generated dynamic route to avoid old events: " + notificationsEndPoint);

    	ModelManager.get();
		List<User> users = User.findAll();
		for (User u : users) {
			u.connected = 0;
			u.save();
		}

        Logger.info("- Bootstrap executed -");
    }
}