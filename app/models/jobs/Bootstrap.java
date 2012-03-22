package models.jobs;

import models.ModelManager;
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
        Logger.info("- Bootstrap executed -");
    }
}