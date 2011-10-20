package controllers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import models.BoyerMoore;
import models.EventTopic;
import models.ModelManager;
import models.PutGetClient;
import models.SupportedTopicsXML;
import models.translator.TranslationUtils;

import org.jdom.input.SAXBuilder;

import play.Logger;
import play.Play;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;
import play.mvc.Controller;
import play.mvc.Util;
import play.templates.TemplateLoader;

import com.hp.hpl.jena.graph.Triple;

import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.generators.QuadrupleGenerator;
import fr.inria.eventcloud.translators.wsnotif.WsNotificationTranslator;

/**
 * The WebService controller is in charge of SOAP connection with the DSB.
 * 
 * @author Alexandre Bourdin
 */
public class WebService extends Controller {

	private static QName TOPIC_SET_QNAME = new QName("http://docs.oasis-open.org/wsn/t-1", "TopicSet");

	public static String DSB_SUBSCRIBE_SERVICE = "http://94.23.221.97:8084/petals/services/NotificationProducerPortService";

	// public static String dsbNotify =
	// "http://94.23.221.97:8084/petals/services/NotificationConsumerPortService";
	public static String DSB_NOTIFY_SERVICE = "http://www.postbin.org/y83a5d";

	/**
	 * SOAP endpoint to receive WS-Notifications from the DSB.
	 * 
	 * @param topicId
	 *            : necessary to have a unique endpoint for each topic.
	 */
	public static void soapNotifEndPoint(String topicId) {

		URI eventId = generateRandomUri();
		Event event = TranslationUtils.translateWsNotifNotificationToEvent(request.body,
				inputStreamFrom("public/xml/xsd-01.xml"), eventId);

		Collection<Triple> triples = event.getTriples();
		String title = "No title";
		String content = "No data";
		for (Triple t : triples) {
			title = "Topic: " + t.getObject().getLiteralLexicalForm();
			content = t.getSubject().toString() + " : " + t.getPredicate().toString()
					+ " : " + t.getObject().toString();
		}

		ModelManager.get().getTopicById(topicId).multicast(new models.Event(title, content));
	}

	/**
	 * Sends a request to the DSB to get the list of supported topics
	 */
	@Util
	public static ArrayList<EventTopic> getSupportedTopics() {
		Map<String, Object> map = new HashMap<String, Object>();

		String rendered = TemplateLoader.load("WebService/gettopicstemplate.xml").render(map);

		WSRequest request = WS.url(DSB_SUBSCRIBE_SERVICE).setHeader("content-type", "application/soap+xml")
				.body(rendered);

		try {
			HttpResponse response = request.post();
			String topicsString = response.getString();

			Logger.info("topics=" + topicsString);

			ArrayList<EventTopic> topics = new ArrayList<EventTopic>();

			SAXBuilder sxb = new SAXBuilder();
			org.jdom.Document xml = new org.jdom.Document();
			org.jdom.Element root = null;
			try {
				xml = sxb.build(new StringReader(topicsString));
				root = xml.getRootElement();
			} catch (Exception e) {
				Logger.error("jDom : Error while parsing XML document");
				e.printStackTrace();
			}
			SupportedTopicsXML.parseXMLTree(topics, root, "");
			return topics;
		} catch (RuntimeException e) {
			renderText("Error : " + e.getMessage());
			return null;
		}
	}

	/**
	 * Subscription action, forwards the subscription to the DSB
	 * 
	 * @param et
	 */
	@Util
	public static int subscribe(EventTopic et) {
		Map<String, Object> map = new HashMap<String, Object>();

		map.put("topicName", et.name);
		map.put("topicURL", et.uri);
		map.put("topicPrefix", et.namespace);
		map.put("subscriber", "http://demo.play-project.eu/webservice/soapnotifendpoint/" + et.getId());

		String rendered = TemplateLoader.load("WebService/subscribetemplate.xml").render(map);

		WSRequest request = WS.url(DSB_SUBSCRIBE_SERVICE).setHeader("content-type", "application/soap+xml")
				.body(rendered);
		request.post();

		HttpResponse response = request.post();
		String result = response.getString();

		Logger.info("subscribe response = " + result);

		return 0;
	}

	/**
	 * Retreives historical events for a given topic
	 * 
	 * @param et
	 * @return
	 */
	public static ArrayList<Event> getHistorical(EventTopic et) {
		/*
		 * PutGetWsApi pgc = PutGetClient.getClientFromFinalURL(
		 * "http://94.23.221.97:8084/petals/services/EventCloudPutGetPortService"
		 * , PutGetWsApi.class);
		 */

		PutGetClient pgc = new PutGetClient(
				"http://eventcloud.inria.fr:8951/proactive/services/EventCloud_putget-webservices");

		pgc.addQuadruple(QuadrupleGenerator.create());
		// SparqlSelectResponse response = pgc
		// .executeSparqlSelect("SELECT ?g ?s ?p ?o WHERE { GRAPH ?g { ?s ?p ?o } } LIMIT 30");
		Collection<Quadruple> response = pgc.findQuadruplePattern(QuadruplePattern.ANY);
		/*
		 * ResultSetWrapper result = response.getResult(); while
		 * (result.hasNext()) { QuerySolution qs = result.next();
		 * Logger.info(qs.get("g").toString());
		 * Logger.info(qs.get("s").toString());
		 * Logger.info(qs.get("p").toString());
		 * Logger.info(qs.get("o").toString()); }
		 */
		for (Quadruple q : response) {
			Logger.info("q : " + q.toString());
		}
		return new ArrayList<Event>();
	}

	/**
	 * Notify action triggered by buttons on the web interface Generates an
	 * event and sends it to the DSB on the specified topic
	 * 
	 * @param name
	 * @param status
	 * @param location
	 * @param topic
	 */
	public static void notif(String name, String status, String location, String topic) {
		// Model model = RDF2Go.getModelFactory().createModel(new
		// URIImpl("http://www.inria.fr"));
		// model.open();
		// model.setNamespace("", "http://events.event-processing.org/types/");
		// model.setNamespace("e", "http://events.event-processing.org/ids/");
		// model.setNamespace("xsd", "http://www.w3.org/2001/XMLSchema#");
		// model.setNamespace("rdf",
		// "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		// model.setNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		// model.setNamespace("owl", "http://www.w3.org/2002/07/owl#");
		// FacebookStatusFeedEvent e2 = new FacebookStatusFeedEvent(model,
		// "http://events.event-processing.org/ids/e2#event", true);
		// e2.setName(name);
		// e2.setStatus(status);
		// e2.setLocation(location);
		// e2.setEndTime(javax.xml.bind.DatatypeConverter.parseDateTime("2011-08-24T14:42:01.011"));
		// String modelString = model.serialize(Syntax.RdfXml);
		//
		// String producerAddress = "http://localhost:9998/foo/Producer";
		// String endpointAddress = "http://localhost:9998/foo/Endpoint";
		// String uuid = UUID.randomUUID().toString();
		//
		// QName topicUsed = new QName("http://dsb.petalslink.org/notification",
		// "Sample", "dsbn");
		// String dialect =
		// WstopConstants.CONCRETE_TOPIC_EXPRESSION_DIALECT_URI.toString();
		// try {
		// Document notifPayload =
		// XMLHelper.createDocumentFromString(modelString);
		// Notify notify;
		// DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		// dbf.setNamespaceAware(true);
		// notify = NotificationHelper.createNotification(producerAddress,
		// endpointAddress, uuid, topicUsed,
		// dialect, notifPayload);
		// Document dom =
		// Wsnb4ServUtils.getWsnbWriter().writeNotifyAsDOM(notify);
		// XMLHelper.writeDocument(dom, System.out);
		// INotificationConsumer consumerClient = new
		// HTTPNotificationConsumerClient(dsbNotify);
		// consumerClient.notify(notify);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}

	public static void notifTest() {
		// String producerAddress = "http://localhost:9998/foo/Producer";
		// String endpointAddress = "http://localhost:9998/foo/Endpoint";
		// String uuid = UUID.randomUUID().toString();
		//
		// QName topicUsed = new QName("http://dsb.petalslink.org/notification",
		// "NuclearUC", "tns");
		// String dialect =
		// WstopConstants.CONCRETE_TOPIC_EXPRESSION_DIALECT_URI.toString();
		// // TODO initialize notifPayload another way
		// Document notifPayload = null;
		// Notify notify;
		// DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		// dbf.setNamespaceAware(true);
		// try {
		// notify = NotificationHelper.createNotification(producerAddress,
		// endpointAddress, uuid, topicUsed,
		// dialect, notifPayload);
		// Document dom =
		// Wsnb4ServUtils.getWsnbWriter().writeNotifyAsDOM(notify);
		// XMLHelper.writeDocument(dom, System.out);
		// INotificationConsumer consumerClient = new
		// HTTPNotificationConsumerClient(dsbNotify);
		// consumerClient.notify(notify);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}

	/**
	 * Puts the content of a file on an InputStream
	 * 
	 * @param file
	 * @return
	 */
	@Util
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

	@Util
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