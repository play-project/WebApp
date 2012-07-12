package controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import models.ModelManager;
import models.User;
import models.eventstream.Event;
import models.eventstream.EventTopic;

import org.ontoware.rdf2go.RDF2Go;
import org.ontoware.rdf2go.Reasoning;
import org.ontoware.rdf2go.impl.jena29.ModelImplJena26;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.node.Resource;
import org.ontoware.rdf2go.model.node.Variable;

import play.Logger;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Util;

import com.google.gson.reflect.TypeToken;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import eu.play_project.play_commons.constants.Constants;
import eu.play_project.play_commons.eventtypes.EventHelpers;
import fr.inria.eventcloud.api.responses.SparqlConstructResponse;
import fr.inria.eventcloud.api.responses.SparqlSelectResponse;
import fr.inria.eventcloud.api.wrappers.ResultSetWrapper;
import fr.inria.eventcloud.webservices.api.EventCloudManagementWsApi;
import fr.inria.eventcloud.webservices.api.PutGetWsApi;
import fr.inria.eventcloud.webservices.factories.WsClientFactory;

/**
 * 
 * @author Alexandre Bourdin
 * @author Roland Stühmer
 * @author Laurent Pellegrino
 *
 * @version $Revision$
 *
 */
public class HistoricalEvents extends Controller {
	
	public static String EC_MANAGEMENT_WS_SERVICE = 
	        Constants.getProperties().getProperty("eventcloud.default.putget.endpoint");
	
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
     * Retrieves the last 30 historical events for a given topic.
     * 
     * @param et event topic.
     * 
     * @return Returns null if topics doesn't exist. Returns an empty 
     * ArrayList if no events were found.
	 * @throws IOException 
     */
    @Util
    public static ArrayList<models.eventstream.Event> getHistorical(EventTopic et) throws IOException {
            // Creates an Event Cloud Management Web Service Client
            EventCloudManagementWsApi eventCloudManagementWsClient =
                    WsClientFactory.createWsClient(
                            EventCloudManagementWsApi.class, EC_MANAGEMENT_WS_SERVICE);

            String topicUrl = et.getTopicUrl();

            if (!eventCloudManagementWsClient.isCreated(topicUrl)) {
                throw new IOException("Error: The Web Service client for this topic could not be created. Aborting.");
            }

            String putgetProxyEndpoint =
                    findPutGetProxyEndpoint(
                            eventCloudManagementWsClient, topicUrl);

            PutGetWsApi putgetProxyClient = 
                    WsClientFactory.createWsClient(PutGetWsApi.class, putgetProxyEndpoint);

            String sparqlQuery = "PREFIX : <http://events.event-processing.org/types/>\n";
            sparqlQuery += "SELECT ?id WHERE {\n    GRAPH ?g {\n";
            // sparqlQuery += "        ?id :stream <" + topicUrl + "#stream> .\n";
            sparqlQuery += "        ?id :endTime ?publicationDateTime .\n";
            sparqlQuery += "    }\n} ORDER BY DESC(?publicationDateTime) LIMIT 10";

            Logger.info("Executing the following historical SPARQL query " + sparqlQuery);

            SparqlSelectResponse response = 
                    putgetProxyClient.executeSparqlSelect(sparqlQuery);

            ResultSetWrapper result = response.getResult();
            
            ArrayList<models.eventstream.Event> events = 
                    new ArrayList<models.eventstream.Event>();
            
            while (result.hasNext()) {
                QuerySolution qs = result.next();
                Node id = qs.get("id").asNode();

                sparqlQuery =
                        "CONSTRUCT { ?g ?p ?o } WHERE {\n    GRAPH ?g {\n" + 
                        "        <" + id.getURI()
                        + "> ?p ?o .\n    }\n" + "}";

                SparqlConstructResponse constructResponse =
                        putgetProxyClient.executeSparqlConstruct(sparqlQuery);

                Model rdf = new ModelImplJena26(constructResponse.getResult()).open();
                EventHelpers.addNamespaces(rdf);

                events.add(models.eventstream.Event.eventFromRdf(rdf));
            }
            return events;
            
    }
 
	@Util
    private static String findPutGetProxyEndpoint(EventCloudManagementWsApi eventCloudManagementWsClient,
                                                  String topicUrl) {
        List<String> putgetProxyEndpoints = 
                eventCloudManagementWsClient.getPutgetProxyEndpointUrls(topicUrl);
        
        if (putgetProxyEndpoints == null || putgetProxyEndpoints.size() == 0) {
            Logger.info("Creating new putget proxy for eventcloud " + topicUrl);
            return eventCloudManagementWsClient.createPutGetProxy(topicUrl);
        } else {
            return putgetProxyEndpoints.get(0);
        }
    }
    
}
