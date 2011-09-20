package models;

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
        ModelManager.get();
        Logger.info("Bootstrap executed !");
    }
}