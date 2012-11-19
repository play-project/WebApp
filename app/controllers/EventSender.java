package controllers;

import static eu.play_project.play_commons.constants.Event.EVENT_ID_SUFFIX;
import static eu.play_project.play_commons.constants.Namespace.EVENTS;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import models.ModelManager;
import models.User;

import org.event_processing.events.types.CrisisMeasureEvent;
import org.event_processing.events.types.FacebookStatusFeedEvent;
import org.event_processing.events.types.TwitterEvent;
import org.event_processing.events.types.UcTelcoCall;
import org.event_processing.events.types.UcTelcoComposeMail;
import org.event_processing.events.types.UcTelcoEsrRecom;
import org.ontoware.rdf2go.model.Syntax;
import org.ontoware.rdf2go.model.node.impl.URIImpl;

import play.Logger;
import play.data.validation.Required;
import play.mvc.Before;
import play.mvc.Controller;
import eu.play_project.play_commons.constants.Source;
import eu.play_project.play_commons.constants.Stream;
import eu.play_project.play_commons.eventtypes.EventHelpers;
import eu.play_project.play_eventadapter.AbstractSender;

/**
 * Allows the simulation of some artifical events for demonstration purposes.
 * 
 * @author Roland Stühmer
 */
public class EventSender extends Controller {

	private static AbstractSender sender = new AbstractSender(Stream.FacebookStatusFeed.getTopicQName());
	
	private static Random random = new Random();
	
	private static long lastSequenceNumber = 0;
	
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
	
    /**
     * Ensures that the first 2^63 - 1 calls return a unique number.
     * If we want to guarantee that for any call, we could detect when the
     * nextSequenceNumber is Long.MAX_VALUE. If so, we restart from 0 but we
     * append a new letter to the event id URL.
     */
    private static synchronized long nextSequenceNumber() {

        lastSequenceNumber++;

        return lastSequenceNumber;
    }
	
	/**
	 * Notify action triggered by buttons on the web interface Generates a
	 * Facebook status event event and sends it to the DSB.
	 * 
	 * This method selects a different type of event based on the parameter
	 * passed from JavaScript.
	 */
	public static void simulate(@Required String eventType) {
		
		String uniqueId = "webapp/" + Long.toString(nextSequenceNumber()) + "_" + eventType + "_" + Math.abs(random.nextLong());
		String eventId = EVENTS.getUri() + uniqueId;
		Logger.info("An event with type '%s' was requested.", eventType);
		final String CALLEE = "49123456789";
		
		if (eventType.equals("fb")) {
			FacebookStatusFeedEvent event = new FacebookStatusFeedEvent(EventHelpers.createEmptyModel(eventId),
					eventId + EVENT_ID_SUFFIX, true);
			event.setFacebookName("Roland Stühmer");
			event.setFacebookId("100000058455726");
			event.setFacebookLink(new URIImpl("http://graph.facebook.com/roland.stuehmer#"));
			event.setStatus("I bought some JEANS this morning");
			event.setFacebookLocation("Karlsruhe, Germany");
			event.setEndTime(Calendar.getInstance());
			event.setStream(new URIImpl(Stream.FacebookStatusFeed.getUri()));
			event.setSource(new URIImpl(Source.WebApp.toString()));

			Logger.debug("Sending event: %s", event.getModel().serialize(Syntax.Turtle));
			sender.notify(event, Stream.FacebookStatusFeed.getTopicQName());
		}
		else if (eventType.equals("call")) {
			UcTelcoCall event = new UcTelcoCall(EventHelpers.createEmptyModel(eventId),
					eventId + EVENT_ID_SUFFIX, true);
			// Run some setters of the event
			event.setUcTelcoCalleePhoneNumber(CALLEE);
			event.setUcTelcoCallerPhoneNumber("49123498765");
			event.setUcTelcoDirection("incoming");
			// Create a Calendar for the current date and time
			event.setEndTime(Calendar.getInstance());
			event.setStream(new URIImpl(Stream.TaxiUCCall.getUri()));
			event.setSource(new URIImpl(Source.WebApp.toString()));
			EventHelpers.setLocationToEvent(event, 111, 222);
			
			Logger.debug("Sending event: %s", event.getModel().serialize(Syntax.Turtle));
			sender.notify(event, Stream.TaxiUCCall.getTopicQName());
		}
		else if (eventType.equals("tweet")) {
			TwitterEvent event = new TwitterEvent(EventHelpers.createEmptyModel(eventId),
					eventId + EVENT_ID_SUFFIX, true);
			// Run some setters of the event
			event.setUcTelcoPhoneNumber(CALLEE);
			event.setTwitterScreenName("roland.stuehmer");
			event.setContent("Tweet test.");
			// Create a Calendar for the current date and time
			event.setEndTime(Calendar.getInstance());
			event.setStream(new URIImpl(Stream.TaxiUCTwitter.getUri()));
			event.setSource(new URIImpl(Source.WebApp.toString()));
			EventHelpers.setLocationToEvent(event, 111, 222);
			
			Logger.debug("Sending event: %s", event.getModel().serialize(Syntax.Turtle));
			sender.notify(event, Stream.TaxiUCTwitter.getTopicQName());
		}
		else if (eventType.equals("measure")) {
			CrisisMeasureEvent event = new CrisisMeasureEvent(EventHelpers.createEmptyModel(eventId),
					eventId + EVENT_ID_SUFFIX, true);
			// Run some setters of the event
			event.setCrisisValue("110");
			event.setCrisisUnit("mSv");
			event.setCrisisLocalisation("Karlsruhe");
			// Create a Calendar for the current date and time
			event.setEndTime(Calendar.getInstance());
			event.setStream(new URIImpl(Stream.SituationalEventStream.getUri()));
			event.setSource(new URIImpl(Source.WebApp.toString()));
			
			Logger.debug("Sending event: %s", event.getModel().serialize(Syntax.Turtle));
			sender.notify(event, Stream.SituationalEventStream.getTopicQName());
		}		
		else {
			Logger.error("A dummy event was to be simulated but it's type '%s' is unknown.", eventType);
		}
	}
	
	public static void simulateRecommendation(@Required String calleePhoneNumber, @Required String callerPhoneNumber, @Required String message) {
		
		String uniqueId = "webapp/" + Long.toString(nextSequenceNumber()) + "_" + Math.abs(random.nextLong());
		String eventId = EVENTS.getUri() + uniqueId;
		Logger.info("A recommendation event was requested.");

		UcTelcoEsrRecom event = new UcTelcoEsrRecom(EventHelpers.createEmptyModel(eventId),
				eventId + EVENT_ID_SUFFIX, true);
		event.setEndTime(Calendar.getInstance());
		event.setStream(new URIImpl(Stream.TaxiUCESRRecom.getUri()));
		event.setSource(new URIImpl(Source.WebApp.toString()));
		event.setUcTelcoCalleePhoneNumber(calleePhoneNumber);
		event.setUcTelcoCallerPhoneNumber(callerPhoneNumber);
		event.setUcTelcoUniqueId(uniqueId);
		event.setMessage(message);
		event.setEsrRecommendation(new URIImpl("http://imu.ntua.gr/san/esr/1.1/recommendation/" + uniqueId));
		event.setUcTelcoAnswerRequired(true);
		event.setUcTelcoAckRequired(true);
		
		// Create an action object and connect it to the event:
		UcTelcoComposeMail action1 = new UcTelcoComposeMail(event.getModel(), true);
		action1.setUcTelcoMailAddress(new URIImpl("mailto:roland.stuehmer@fzi.de"));
		action1.setUcTelcoMailSubject("Regards");
		action1.setUcTelcoMailContent("Hello world,\nwith kind regards,\n...");
		event.addUcTelcoAction(action1);

		Logger.debug("Sending event: %s", event.getModel().serialize(Syntax.Turtle));
		sender.notify(event, Stream.TaxiUCESRRecom.getTopicQName());
}

}
