package controllers;

import java.util.List;

import play.mvc.Controller;
import models.ModelManager;
import models.User;

public class Monitoring extends Controller {
	
	public static void displayInfo(){
		List<User> connectedUsers = ModelManager.get().getConnectedUsers();
		render(connectedUsers);
	}

}
