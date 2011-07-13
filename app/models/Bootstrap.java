package models;

import play.Logger;
import play.jobs.*;

@OnApplicationStart
public class Bootstrap extends Job {
    
    public void doJob() {
        ModelManager.get();
        Logger.info("Bootstrap executed !");
    }
}