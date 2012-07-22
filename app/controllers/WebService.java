package controllers;

import static eu.play_project.play_commons.constants.Event.EVENT_ID_SUFFIX;

import java.beans.EventSetDescriptor;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import models.ModelManager;
import models.PredefinedPatterns;
import models.SupportedTopicsXML;
import models.eventstream.EventTopic;

import org.event_processing.events.types.Event;
import org.event_processing.events.types.FacebookStatusFeedEvent;
import org.hibernate.event.AbstractEvent;
import org.jdom.input.SAXBuilder;
import org.ontoware.rdf2go.ModelFactory;
import org.ontoware.rdf2go.RDF2Go;
import org.ontoware.rdf2go.Reasoning;
import org.ontoware.rdf2go.exception.ModelRuntimeException;
import org.ontoware.rdf2go.impl.jena29.ModelImplJena26;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.Syntax;
import org.ontoware.rdf2go.model.node.Resource;
import org.ontoware.rdf2go.model.node.Variable;
import org.ontoware.rdf2go.model.node.impl.URIImpl;
import org.ontoware.rdf2go.util.ModelUtils;
import org.petalslink.dsb.notification.client.http.HTTPNotificationProducerRPClient;
import org.petalslink.dsb.notification.client.http.simple.HTTPProducerClient;
import org.petalslink.dsb.notification.client.http.simple.HTTPSubscriptionManagerClient;
import org.petalslink.dsb.notification.commons.NotificationException;
import org.w3c.dom.Document;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Router;
import play.mvc.Util;
import play.utils.HTML;

import com.ebmwebsourcing.easycommons.xml.XMLHelper;
import com.ebmwebsourcing.wsstar.basefaults.datatypes.impl.impl.WsrfbfModelFactoryImpl;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.impl.impl.WsnbModelFactoryImpl;
import com.ebmwebsourcing.wsstar.resource.datatypes.impl.impl.WsrfrModelFactoryImpl;
import com.ebmwebsourcing.wsstar.resourcelifetime.datatypes.impl.impl.WsrfrlModelFactoryImpl;
import com.ebmwebsourcing.wsstar.resourceproperties.datatypes.impl.impl.WsrfrpModelFactoryImpl;
import com.ebmwebsourcing.wsstar.topics.datatypes.api.WstopConstants;
import com.ebmwebsourcing.wsstar.topics.datatypes.impl.impl.WstopModelFactoryImpl;
import com.ebmwebsourcing.wsstar.wsnb.services.INotificationProducerRP;
import com.ebmwebsourcing.wsstar.wsnb.services.impl.util.Wsnb4ServUtils;

import eu.play_project.play_commons.constants.Constants;
import eu.play_project.play_commons.constants.Stream;
import eu.play_project.play_commons.eventformat.EventFormatHelpers;
import eu.play_project.play_commons.eventtypes.EventHelpers;
import eu.play_project.play_eventadapter.AbstractReceiver;
import eu.play_project.play_eventadapter.AbstractSender;
import eu.play_project.play_platformservices.api.QueryDispatchApi;
import fr.inria.eventcloud.api.responses.SparqlConstructResponse;
import fr.inria.eventcloud.webservices.api.EventCloudManagementWsApi;
import fr.inria.eventcloud.webservices.api.PutGetWsApi;
import fr.inria.eventcloud.webservices.factories.WsClientFactory;

/**
 * The WebService controller is in charge of SOAP connection with the DSB.
 * 
 * @author Alexandre Bourdin
 */
public class WebService extends Controller {
    
	public static String DSB_RESOURCE_SERVICE = Constants.getProperties().getProperty("dsb.notify.endpoint");
	private static AbstractReceiver receiver = new AbstractReceiver() {};

	static {
		Wsnb4ServUtils.initModelFactories(new WsrfbfModelFactoryImpl(), new WsrfrModelFactoryImpl(),
				new WsrfrlModelFactoryImpl(), new WsrfrpModelFactoryImpl(), new WstopModelFactoryImpl(),
				new WsnbModelFactoryImpl());
	}

	/**
	 * SOAP endpoint to receive WS-Notifications from the DSB.
	 * 
	 * @param topicId
	 *            : necessary to have a unique endpoint for each topic.
	 */
	public static void soapNotifEndPoint(String topicId) {

		String notifyMessage;

		// A trick to read a Stream into to String:
		try {
			notifyMessage = new java.util.Scanner(request.body).useDelimiter("\\A").next();
		} catch (java.util.NoSuchElementException e) {
			notifyMessage = "";
		}

		 //Print some event to debug output:
//		if (ModelManager.get().getTopicById(topicId).getId()
//				.equals("s_FacebookCepResults")) {
//			Logger.info(notifyMessage);
//		} // FIXME stuehmer: added by roland, not important
		
		Model rdf;
		try {
			/*
			 * Deal with RDF events:
			 */
			rdf = receiver.parseRdf(notifyMessage);

			ModelManager.get().getTopicById(topicId)
					.multicast(models.eventstream.Event.eventFromRdf(rdf));
		} catch (Exception e) {
			/*
			 * Deal with non-RDF events:
			 */
			String eventTitle = "Event";
			String eventText = HTML.htmlEscape(EventFormatHelpers.unwrapFromNativeMessageElement(notifyMessage))
					.replaceAll("\n", "<br />").replaceAll("\\s{4}", "&nbsp;&nbsp;&nbsp;&nbsp;");
			ModelManager.get().getTopicById(topicId)
					.multicast(new models.eventstream.Event(eventTitle, eventText));
		}
	}

	/**
	 * Sends a request to the DSB to get the list of supported topics
	 */
	@Util
	public static ArrayList<EventTopic> getSupportedTopics() {
		Logger.info("Getting topics from DSB at %s", DSB_RESOURCE_SERVICE);

		INotificationProducerRP resourceClient = new HTTPNotificationProducerRPClient(DSB_RESOURCE_SERVICE);

		ArrayList<EventTopic> topics = new ArrayList<EventTopic>();

		try {
			QName qname = WstopConstants.TOPIC_SET_QNAME;
			com.ebmwebsourcing.wsstar.resourceproperties.datatypes.api.abstraction.GetResourcePropertyResponse response = resourceClient
					.getResourceProperty(qname);

			Document dom = Wsnb4ServUtils.getWsrfrpWriter().writeGetResourcePropertyResponseAsDOM(response);
			String topicsString = XMLHelper.createStringFromDOMDocument(dom);

			Logger.info("TOPICS STRING: " + topicsString);

			SAXBuilder sxb = new SAXBuilder();
			org.jdom.Document xml = new org.jdom.Document();
			org.jdom.Element root = null;
			xml = sxb.build(new StringReader(topicsString));
			root = xml.getRootElement();

			SupportedTopicsXML.parseXMLTree(topics, root, "");

		} catch (Exception e) {
			Logger.warn(
					e,
					"A problem occurred while fetching the list of available topics from the DSB. Continuing with empty set of topics.");
		}

		return topics;
	}

	/**
	 * Subscription action, forwards the subscription to the DSB
	 * 
	 * @param et
	 */
	@Util
	public static int subscribe(EventTopic et) {
		Logger.info("Subscribing to topic '%s%s' at broker '%s'", et.uri, et.name, DSB_RESOURCE_SERVICE);

		HTTPProducerClient client = new HTTPProducerClient(DSB_RESOURCE_SERVICE);
		QName topic = new QName(et.uri, et.name, et.namespace);

		Map params = new HashMap<String, Object>();
		params.put("topicId", et.getId());
		String notificationsEndPoint = Router.getFullUrl("WebService.soapNotifEndPoint", params);
		try {
			et.subscriptionID = client.subscribe(topic, notificationsEndPoint);
			et.alreadySubscribedDSB = true;
		} catch (NotificationException e) {
			e.printStackTrace();
		}

		return 1;
	}

	/**
	 * Unsubscription action, forwards the unsubscription to the DSB
	 * 
	 * @param et
	 */
	@Util
	public static int unsubscribe(EventTopic et) {
		HTTPSubscriptionManagerClient subscriptionManagerClient = new HTTPSubscriptionManagerClient(
				DSB_RESOURCE_SERVICE);
		try {
			subscriptionManagerClient.unsubscribe(et.subscriptionID);
			et.alreadySubscribedDSB = false;
		} catch (NotificationException e) {
			e.printStackTrace();
			return 0;
		}

		return 1;
	}
}
