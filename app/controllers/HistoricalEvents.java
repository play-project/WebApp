package controllers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ontoware.rdf2go.RDF2Go;
import org.ontoware.rdf2go.Reasoning;
import org.ontoware.rdf2go.impl.jena29.ModelImplJena26;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.node.Resource;
import org.ontoware.rdf2go.model.node.Variable;

import models.ModelManager;
import models.User;
import models.eventstream.Event;
import models.eventstream.EventTopic;

import com.ebmwebsourcing.wsstar.basefaults.datatypes.impl.impl.WsrfbfModelFactoryImpl;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.impl.impl.WsnbModelFactoryImpl;
import com.ebmwebsourcing.wsstar.resource.datatypes.impl.impl.WsrfrModelFactoryImpl;
import com.ebmwebsourcing.wsstar.resourcelifetime.datatypes.impl.impl.WsrfrlModelFactoryImpl;
import com.ebmwebsourcing.wsstar.resourceproperties.datatypes.impl.impl.WsrfrpModelFactoryImpl;
import com.ebmwebsourcing.wsstar.topics.datatypes.impl.impl.WstopModelFactoryImpl;
import com.ebmwebsourcing.wsstar.wsnb.services.impl.util.Wsnb4ServUtils;
import com.google.gson.reflect.TypeToken;

import eu.play_project.play_commons.constants.Constants;
import eu.play_project.play_commons.constants.Stream;
import eu.play_project.play_eventadapter.AbstractSender;
import fr.inria.eventcloud.api.responses.SparqlConstructResponse;
import fr.inria.eventcloud.webservices.api.EventCloudManagementWsApi;
import fr.inria.eventcloud.webservices.api.PutGetWsApi;
import fr.inria.eventcloud.webservices.factories.WsClientFactory;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Util;

public class HistoricalEvents extends Controller {
	
	public static String EC_MANAGEMENT_WS_SERVICE = Constants.getProperties().getProperty(
			"eventcloud.default.putget.endpoint");
	private static Model eventHierarchy;

	static {
		eventHierarchy = new ModelImplJena26(Reasoning.rdfs);
		try {
			eventHierarchy.readFrom(WebService.class.getClassLoader().getResourceAsStream("types.n3"));
		} catch (Exception e) {
			Logger.error(e, "The event hierarchy could not be read from the classpath. This prevents us from parsing historic events.");
		}
	}

	/**
	 * Historical events
	 */
	public static void historicalEvents() {
		User u = (User) request.args.get("user");
		if (u == null) {
			Application.logout();
		}
		ArrayList<EventTopic> userTopics = u.getTopics();
		render(userTopics);
	}

	public static void historicalByTopic(String topicId) {
		EventTopic et = ModelManager.get().getTopicById(topicId);
		if(et == null){
			renderJSON("{\"error\":\"Error: Topic not found.\"}");
		} else {
			ArrayList<Event> events;
			try {
				events = getHistorical(et);
				if (events.size() == 0) {
					renderJSON("{\"error\":\"No events were found in this topic.\"}");
				}
				renderJSON(events, new TypeToken<ArrayList<Event>>() {}.getType());
			} catch (Exception e) {
				renderJSON("{\"error\":\"" + e.getMessage() + "\"}");
			}
		}
	}

	/**
	 * Retreives historical events for a given topic Returns null if topics
	 * doesn't exist Returns an empty ArrayList if no events were found
	 * 
	 * @param et
	 * @return
	 * @throws Exception 
	 */
	@Util
	public static ArrayList<models.eventstream.Event> getHistorical(EventTopic et) throws Exception {
		ArrayList<models.eventstream.Event> events = new ArrayList<models.eventstream.Event>();
		
		SparqlConstructResponse response;
		try{
			// Creates an Event Cloud Management Web Service Client
			EventCloudManagementWsApi eventCloudManagementWsClient = WsClientFactory.createWsClient(
					EventCloudManagementWsApi.class, EC_MANAGEMENT_WS_SERVICE);

			List<String> ecIds = eventCloudManagementWsClient.getEventCloudIds();
			String topicId = et.uri + et.name;
			if (ecIds == null || !ecIds.contains(topicId)) {
				throw new Exception("Error: Event Cloud not found.");
			}

			List<String> listPutgetEndpoints = eventCloudManagementWsClient.getPutgetProxyEndpointUrls(topicId);
			String putgetProxyEndpoint = null;
			if (listPutgetEndpoints == null || listPutgetEndpoints.size() == 0) {
				putgetProxyEndpoint = eventCloudManagementWsClient.createPutGetProxy(topicId);
				Logger.info("New putget proxy for " + topicId + " Event Cloud has been created");
			} else {
				putgetProxyEndpoint = listPutgetEndpoints.get(0);
			}

			PutGetWsApi pgc = WsClientFactory.createWsClient(PutGetWsApi.class, putgetProxyEndpoint);

			response = pgc
					.executeSparqlConstruct("CONSTRUCT {?s ?p ?o} WHERE { GRAPH ?g {?s ?p ?o } } LIMIT 30");
			
		} catch(Exception e) {
			Logger.error(e.getMessage());
			throw new Exception("Error: A problem occurred while asking for historic events: " + e.getMessage());
		}

		// Load the statements
		Model rdf = new ModelImplJena26(null, response.getResult(), Reasoning.rdfs);
		// Load event hierarchy information
		rdf.addModel(eventHierarchy);
		
		Iterator<Resource> eventIds = org.event_processing.events.types.Event.getAllInstances(rdf);
		while (eventIds.hasNext()) {
			Resource id = eventIds.next();
			Iterator<Statement> statements = rdf.findStatements(id, Variable.ANY, Variable.ANY);
			Model event = RDF2Go.getModelFactory().createModel();
			event.addAll(statements);
			events.add(models.eventstream.Event.eventFromRdf(event));
		}

		return events;
	}
}
