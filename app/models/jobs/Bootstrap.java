package models.jobs;

import java.util.List;

import models.ModelManager;
import models.User;
import play.Logger;
import play.jobs.*;

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
        ModelManager.get();
		List<User> users = User.findAll();
		for (User u : users) {
			u.connected = 0;
			u.save();
		}
        Logger.info("- Bootstrap executed -");
    }
}