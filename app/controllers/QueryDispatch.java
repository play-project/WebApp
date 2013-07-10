package controllers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

import models.ModelManager;
import models.PredefinedPatterns;
import models.User;
import play.Logger;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Util;
import eu.play_project.play_commons.constants.Constants;
import eu.play_project.play_platformservices.api.QueryDispatchApi;
import eu.play_project.play_platformservices.api.QueryDispatchException;

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
	public static boolean sendTokenPatternQuery(String token) throws QueryDispatchException {
		String defaultQueryString = PredefinedPatterns.getPattern("play-epsparql-m12-jeans-example-query.eprq");
		String queryString = defaultQueryString.replaceAll("\"JEANS\"", "\"" + token + "\"");

		return sendFullPatternQuery(queryString);
	}

	@Util
	public static Boolean sendFullPatternQuery(String queryString) throws QueryDispatchException {

		URL wsdl = null;
		try {
			wsdl = new URL(Constants.getProperties().getProperty("platfomservices.querydispatchapi.endpoint") + "?wsdl");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		QName serviceName = new QName("http://play_platformservices.play_project.eu/", "QueryDispatchApi");
		try {
			Service service = Service.create(wsdl, serviceName);
			QueryDispatchApi queryDispatchApi = service
					.getPort(eu.play_project.play_platformservices.api.QueryDispatchApi.class);
	
			String s = queryDispatchApi.registerQuery("webapp_" + UUID.randomUUID(), queryString);
			Logger.info(s);
		}
		catch (WebServiceException e) {
			throw new QueryDispatchException("Problem while sending event pattern: " + e.getMessage());
		}
		return true;
		
	}

	
}
