package controllers;

import static eu.play_project.play_commons.constants.Event.EVENT_ID_SUFFIX;
import static eu.play_project.play_commons.constants.Namespace.EVENTS;

import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Random;

import org.event_processing.events.types.FacebookStatusFeedEvent;
import org.event_processing.events.types.TaxiUCCall;
import org.ontoware.rdf2go.model.Syntax;
import org.ontoware.rdf2go.model.node.impl.URIImpl;

import com.ebmwebsourcing.wsstar.basefaults.datatypes.impl.impl.WsrfbfModelFactoryImpl;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.impl.impl.WsnbModelFactoryImpl;
import com.ebmwebsourcing.wsstar.resource.datatypes.impl.impl.WsrfrModelFactoryImpl;
import com.ebmwebsourcing.wsstar.resourcelifetime.datatypes.impl.impl.WsrfrlModelFactoryImpl;
import com.ebmwebsourcing.wsstar.resourceproperties.datatypes.impl.impl.WsrfrpModelFactoryImpl;
import com.ebmwebsourcing.wsstar.topics.datatypes.impl.impl.WstopModelFactoryImpl;
import com.ebmwebsourcing.wsstar.wsnb.services.impl.util.Wsnb4ServUtils;

import play.Logger;
import play.mvc.Controller;
import eu.play_project.play_commons.constants.Stream;
import eu.play_project.play_commons.eventtypes.EventHelpers;
import eu.play_project.play_eventadapter.AbstractSender;

public class EventSender extends Controller {

	private static AbstractSender sender = new AbstractSender(Stream.FacebookStatusFeed.getTopicQName());
	private static Random random = new Random();
	
	/**
	 * Notify action triggered by buttons on the web interface Generates a
	 * Facebook status event event and sends it to the DSB.
	 * 
	 * This method selects a different type of event based on the parameter
	 * passed from JavaScript.
	 */
	public static void simulate(String eventType) {
		
		String eventId = EVENTS.getUri() + "webapp" + Math.abs(random.nextLong());

		if (eventType == "fb") {
			FacebookStatusFeedEvent event = new FacebookStatusFeedEvent(EventHelpers.createEmptyModel(eventId),
					eventId + EVENT_ID_SUFFIX, true);
			event.setName("Roland Stühmer");
			event.setId("100000058455726");
			event.setLink(new URIImpl("http://graph.facebook.com/roland.stuehmer#"));
			event.setStatus("I bought some JEANS this morning");
			event.setUserLocation("Karlsruhe, Germany");
			event.setEndTime(Calendar.getInstance());
			event.setStream(new URIImpl(Stream.FacebookStatusFeed.getUri()));
			Logger.info("Sending event: %s", event.getModel().serialize(Syntax.Turtle));
			
			sender.notify(event, Stream.FacebookStatusFeed.getTopicQName());
		}
		else if (eventType == "call") {
			TaxiUCCall event = new TaxiUCCall(EventHelpers.createEmptyModel(eventId),
					eventId + EVENT_ID_SUFFIX, true);
			// Run some setters of the event
			event.setUctelcoCalleePhoneNumber("49123456789");
			event.setUctelcoCallerPhoneNumber("49123498765");
			event.setUctelcoDirection("Alice");
			// Create a Calendar for the current date and time
			event.setEndTime(Calendar.getInstance());
			event.setStream(new URIImpl(Stream.TaxiUCCall.getUri()));
			
			sender.notify(event, Stream.TaxiUCCall.getTopicQName());
		}
		else {
			Logger.error("A dummy event was to be simulated but it's type is unknown.");
		}
	}
}
