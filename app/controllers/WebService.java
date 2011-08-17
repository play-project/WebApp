package controllers;

import play.Logger;
import play.Play;
import play.data.validation.*;
import play.libs.F.IndexedEvent;
import play.libs.F.Promise;
import play.libs.IO;
import play.libs.WS;
import play.libs.XML;
import play.libs.XPath;
import play.mvc.*;
import play.mvc.Http.Header;
import play.mvc.Http.StatusCode;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;

import java.util.*;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilderFactory;

import org.ow2.event.notification.client.NotifyEventsClient;
import org.ow2.event.notification.client.SubscribeEventsClient;
import org.w3c.dom.Document;

import com.google.gson.reflect.TypeToken;

import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.translators.wsnotif.WsNotificationTranslator;
import fr.inria.eventcloud.translators.wsnotif.WsNotificationTranslatorImpl;

public class WebService extends Controller {

	public static void soapNotifEndPoint() {
		Logger.info("soapNotifEndPoint called");

		// Parse the soap enveloppe
		// String body = IO.readContentAsString(request.body);
		// Logger.info("Body : " + body);

		WsNotificationTranslator translator = new WsNotificationTranslatorImpl();
		URI eventId = generateRandomUri();
		Event event = translator.translateWsNotifNotificationToEvent(request.body,
				inputStreamFrom("public/xml/xsd-01.xml"), eventId);
		render(event);
	}

	public static void subscribe() {
		String topic = "internalns:rootTopic1";
		String address = "http://127.0.0.1:8080/alert-0.0.1-SNAPSHOT/NotificationConsumer";
		String urlName = "http://krake03.perimeter.fzi.de:8085/petals/services/NotificationBrokerService";

		try {
			Logger.info("before");
			SubscribeEventsClient.subscribe(topic, address, urlName);
			Logger.info("After");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void notif() {
		String topic = "internalns:rootTopic1";
		String message = "Test notif";
		String urlName = "http://krake03.perimeter.fzi.de:8085/petals/services/NotificationBrokerService";

		try {
			NotifyEventsClient.notifyEvent(topic, message, urlName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static InputStream inputStreamFrom(String file) {
		InputStream is = null;

		if (file != null) {
			try {
				is = new FileInputStream(Play.getFile(file));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		return is;
	}

	private static URI generateRandomUri() {
		String legalChars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

		StringBuilder result = new StringBuilder("http://www.inria.fr/");
		SecureRandom random = new SecureRandom();

		for (int i = 0; i < 20; i++) {
			result.append(random.nextInt(legalChars.length()));
		}

		try {
			return new URI(result.toString());
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}
}