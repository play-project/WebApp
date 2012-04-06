package controllers;

import models.PasswordEncrypt;
import play.Logger;
import play.mvc.Controller;

public class QuickTest extends Controller {

	public static void test() {
		Logger.info(PasswordEncrypt.encrypt("666666"));
		
		Application.index();
	}
	
}
