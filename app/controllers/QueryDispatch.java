package controllers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import eu.play_project.play_commons.constants.Constants;
import eu.play_project.play_platformservices.api.QueryDispatchApi;

import models.ModelManager;
import models.PredefinedPatterns;
import models.User;
import play.Logger;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Util;

public class QueryDispatch extends Controller {

	@Before
	private static void checkAuthentification() {
		if (session.get("socialauth") != null) {
			session.remove("socialauth");
			Application.register();
			return;
		}
		String uid = session.get("userid");
		if (uid == null) {
			Application.login();
			return;
		}
		User user = ModelManager.get().getUserById(Long.parseLong(uid));
		if (user == null) {
			Application.logout();
			return;
		}
		user.lastRequest = new Date();
		request.args.put("user", user);
	}
	
	
	@Util
	public static boolean sendTokenPatternQuery(String token) {
		String defaultQueryString = PredefinedPatterns.getPattern("play-epsparql-m12-jeans-example-query.eprq");
		String queryString = defaultQueryString.replaceAll("\"JEANS\"", "\"" + token + "\"");

		URL wsdl = null;
		try {
			wsdl = new URL(Constants.getProperties().getProperty("platfomservices.querydispatchapi.endpoint") + "?wsdl");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		QName serviceName = new QName("http://play_platformservices.play_project.eu/", "QueryDispatchApi");

		Service service = Service.create(wsdl, serviceName);
		QueryDispatchApi queryDispatchApi = service.getPort(QueryDispatchApi.class);

		try {
		String s = queryDispatchApi.registerQuery("http://patterns.event-processing.org/ids/webapp_" + Math.random(), queryString);
		Logger.info(s);
		} catch (Exception e) {
			Logger.error(e.toString());
			return false;
		}
		return true;
	}

	@Util
	public static Boolean sendFullPatternQuery(String queryString) {

		URL wsdl = null;
		try {
			wsdl = new URL(Constants.getProperties().getProperty("platfomservices.querydispatchapi.endpoint") + "?wsdl");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		QName serviceName = new QName("http://play_platformservices.play_project.eu/", "QueryDispatchApi");

		Service service = Service.create(wsdl, serviceName);
		QueryDispatchApi queryDispatchApi = service
				.getPort(eu.play_project.play_platformservices.api.QueryDispatchApi.class);
		String s = queryDispatchApi.registerQuery("http://patterns.event-processing.org/ids/webapp_" + Math.random(), queryString);
		Logger.info(s);
		return true;
	}

	
}
