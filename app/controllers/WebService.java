package controllers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
import play.libs.F.Promise;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;
import play.mvc.Controller;
import play.mvc.Util;
import play.templates.TemplateLoader;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QuerySolution;

import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.generators.QuadrupleGenerator;
import fr.inria.eventcloud.api.responses.SparqlSelectResponse;
import fr.inria.eventcloud.api.wrappers.ResultSetWrapper;
import fr.inria.eventcloud.translators.wsnotif.WsNotificationTranslator;

/**
 * The WebService controller is in charge of SOAP connection with the DSB.
 * 
 * @author Alexandre Bourdin
 */
public class WebService extends Controller {

	private static QName TOPIC_SET_QNAME = new QName("http://docs.oasis-open.org/wsn/t-1", "TopicSet");

	public static String DSB_RESOURCE_SERVICE = "http://94.23.221.97:8084/petals/services/NotificationProducerPortService";
	public static String DSB_SUBSCRIBE_SERVICE = "http://94.23.221.97:8084/petals/services/EventCloudSubscribePortService";
	public static String EC_SUBSCRIBE_SERVICE = "http://eventcloud.inria.fr:8950/proactive/services/EventCloud_subscribe-webservices";
	// public static String PUTGET_SERVICE =
	// "http://138.96.19.125:8952/proactive/services/EventCloud_putget-webservices";
	public static String PUTGET_SERVICE = "http://94.23.221.97:8084/petals/services/PutGetServicePortService";

	// public static String dsbNotify =
	// "http://94.23.221.97:8084/petals/services/NotificationConsumerPortService";
	public static String DSB_NOTIFY_SERVICE = "http://www.postbin.org/1abunq6";

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
		String title = "-";
		String content = "";
		for (Triple t : triples) {
			String predicate = t.getPredicate().toString();
			String object = t.getObject().getLiteralLexicalForm();
			if (BoyerMoore.match("Topic", predicate).size() > 0) {
				title = object;
			} else {
				content += splitUri(t.getSubject().toString())[1] + " : " + splitUri(predicate)[1] + " : "
						+ object + "<br/>";
			}
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

		WSRequest request = WS.url(DSB_RESOURCE_SERVICE).setHeader("Content-Type", "application/soap+xml")
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
		map.put("topicURI", et.uri);
		map.put("topicPrefix", et.namespace);
		map.put("subscriber", "http://demo.play-project.eu/webservice/soapnotifendpoint/" + et.getId());

		try {
			String rendered = TemplateLoader.load("WebService/subscribetemplate.xml").render(map);

			WSRequest request = WS.url(EC_SUBSCRIBE_SERVICE)
					.setHeader("Content-Type", "application/soap+xml").body(rendered);

			request.postAsync();
		} catch (Exception e) {
			return 0;
		}

		return 1;
	}

	/**
	 * Retreives historical events for a given topic
	 * 
	 * @param et
	 * @return
	 */
	@Util
	public static ArrayList<models.Event> getHistorical(EventTopic et) {
		ArrayList<models.Event> events = new ArrayList<models.Event>();
		// EventTopic et = ModelManager.get().getTopicById("dsb_TaxiUCIMA");

		PutGetClient pgc = new PutGetClient(PUTGET_SERVICE);

		SparqlSelectResponse response = pgc
				.executeSparqlSelect("SELECT ?g ?s ?p ?o WHERE { GRAPH ?g { <http://eventcloud.inria.fr/replace/me/with/a/correct/namespace/"
						+ et.namespace + ":" + et.name + "> ?p ?o } } LIMIT 30");
		String title = "-";
		String content = "";
		ResultSetWrapper result = response.getResult();
		while (result.hasNext()) {
			QuerySolution qs = result.next();
			String predicate = qs.get("p").toString();
			String object = qs.get("o").toString();
			if (BoyerMoore.match("Topic", predicate).size() > 0) {
				title = object;
			} else {
				content = et.namespace + ":" + et.name + " : " + predicate + " : " + object + "<br/>";
			}
			events.add(new models.Event(title, content));
		}
		ArrayList<models.Event> temp = new ArrayList<models.Event>();
		for (int i = 0; i < events.size(); i++) {
			temp.add(events.get(events.size() - i - 1));
		}
		return temp;
	}

	/*
	 * @Util public void connect() { URL wsdl = null; try { wsdl = new URL(
	 * "http://94.23.221.97:8084/petals/services/QueryDispatchApiPortService?wsdl"
	 * ); // wsdl = new URL("http://141.21.8.245:8891/jaxws/putQuery?wsdl"); }
	 * catch (MalformedURLException e) { e.printStackTrace(); }
	 * 
	 * QName serviceName = new
	 * QName("http://play_platformservices.play_project.eu/",
	 * "QueryDispatchApi");
	 * 
	 * Service service = Service.create(wsdl, serviceName); QueryDispatchApi
	 * queryDispatchApi = service.getPort(QueryDispatchApi.class);
	 * 
	 * String topic = "\"JEANS\"";
	 * 
	 * String prefix = "PREFIX : <http://events.event-processing.org/types/>";
	 * String queryString = prefix + "SELECT ?friend1 ?friend2 ?friend3 ?topic"
	 * + " WHERE " + "WINDOW{ " + "EVENT ?id1{" +
	 * "?s ?p :FacebookStatusFeedEvent. " + "?friend1 :status ?topic1} " +
	 * "FILTER fn:contains(?topic1, " + topic + ")" + "SEQ " + "EVENT ?id2 {" +
	 * "?s1 ?p1 :FacebookStatusFeedEvent. " + "?friend2 :status ?topic2} " +
	 * "FILTER fn:contains(?topic2, " + topic + ")" + "SEQ " + "EVENT ?id3 {" +
	 * "?s2 ?p2 :FacebookStatusFeedEvent. " + "?friend3 :status ?topic3} " +
	 * "FILTER fn:contains(?topic3, " + topic + ")" +
	 * "} (\"P1M\"^^xsd:duration, sliding)";
	 * 
	 * System.out.println(queryDispatchApi.putQuery(queryString));
	 * 
	 * }
	 */

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

	@Util
	private static String[] splitUri(String uri) {
		if (uri.endsWith("/")) {
			uri = uri.substring(0, uri.length() - 1);
		}

		int slashIndex = uri.lastIndexOf('/');

		if (slashIndex == -1) {
			return new String[] { "", uri };
		} else {
			return new String[] { uri.substring(0, slashIndex), uri.substring(slashIndex + 1) };
		}
	}

}