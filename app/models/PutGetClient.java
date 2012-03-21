package models;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jws.WebMethod;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.JaxWsClientFactoryBean;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;

import play.Logger;

import eu.play_project.play_commons.constants.Constants;
import fr.inria.eventcloud.api.PutGetApi;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.responses.SparqlAskResponse;
import fr.inria.eventcloud.api.responses.SparqlConstructResponse;
import fr.inria.eventcloud.api.responses.SparqlDescribeResponse;
import fr.inria.eventcloud.api.responses.SparqlResponse;
import fr.inria.eventcloud.api.responses.SparqlSelectResponse;
import fr.inria.eventcloud.webservices.api.PutGetWsApi;

public class PutGetClient implements PutGetWsApi {
	private Client client;
	private Map<String, String> operationNames;
	private static String EC_PUTGET_SERVICE = Constants.getProperties().getProperty("eventcloud.putget.endpoint");

	public PutGetClient(String wsUrl) {
		JaxWsClientFactoryBean factory = new JaxWsClientFactoryBean();
		factory.setServiceClass(PutGetWsApi.class);
		factory.setAddress(EC_PUTGET_SERVICE);
		client = factory.create();

		operationNames = new HashMap<String, String>();
		Method[] methods = PutGetWsApi.class.getMethods();
		for (Method method : methods) {
			if (method.isAnnotationPresent(WebMethod.class)) {
				WebMethod webMethodAnnotation = method.getAnnotation(WebMethod.class);
				if (!webMethodAnnotation.operationName().equals("")) {
					operationNames.put(method.getName(), webMethodAnnotation.operationName());
					continue;
				}
			}
			operationNames.put(method.getName(), method.getName());
		}
	}

	public PutGetWsApi getClient(String wsUrl) {
		JaxWsClientFactoryBean factory = new JaxWsClientFactoryBean();
		factory.setServiceClass(PutGetWsApi.class);
		factory.setAddress(wsUrl);
		return (PutGetWsApi) factory.create();
	}

	public <T> T getClientFromFinalURL(String fullURL, Class<T> clazz) {
		JaxWsClientFactoryBean factory = new JaxWsClientFactoryBean();
		factory.setAddress(fullURL);
		factory.setServiceClass(clazz);
		client = factory.create();
		Logger.info("client : " + client.getClass().getName());
		return clazz.cast(client);
	}

	private Object callWS(String methodName, Object[] args) {
		if (client != null) {
			try {
				Object[] results = client.invoke(operationNames.get(methodName), args);
				Logger.info("result : " + results.length + " - " + results[0]);
				return results[0];
			} catch (Exception e) {
				System.err.println("[JaxWsCXFWSCaller] Failed to invoke web service: "
						+ client.getEndpoint().getEndpointInfo().getAddress() + " : " + e.getMessage());
			}
		} else {
			System.err
					.println("[JaxWsCXFWSCaller] Cannot invoke web service since the set up has not been done");
		}
		return null;
	}

	@Override
	public boolean addQuadruple(Quadruple arg0) {
		return (Boolean) callWS("addQuadruple", new Object[] { arg0 });
	}

	@Override
	public boolean addQuadrupleCollection(Collection<Quadruple> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsQuadruple(Quadruple arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteQuadruple(Quadruple arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteQuadrupleCollection(Collection<Quadruple> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Quadruple> deleteQuadruplePattern(QuadruplePattern arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SparqlAskResponse executeSparqlAsk(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SparqlConstructResponse executeSparqlConstruct(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SparqlDescribeResponse executeSparqlDescribe(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SparqlResponse<?> executeSparqlQuery(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SparqlSelectResponse executeSparqlSelect(String arg0) {
		return (SparqlSelectResponse) callWS("executeSparqlSelect", new Object[] { arg0 });
	}

	@Override
	public List<Quadruple> findQuadruplePattern(QuadruplePattern arg0) {
		return (List<Quadruple>) callWS("findQuadruplePattern", new Object[] { arg0 });
	}
}
