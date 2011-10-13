package models;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.jws.WebMethod;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.JaxWsClientFactoryBean;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.responses.SparqlAskResponse;
import fr.inria.eventcloud.api.responses.SparqlConstructResponse;
import fr.inria.eventcloud.api.responses.SparqlDescribeResponse;
import fr.inria.eventcloud.api.responses.SparqlResponse;
import fr.inria.eventcloud.api.responses.SparqlSelectResponse;
import fr.inria.eventcloud.webservices.api.PutGetWsApi;

public class PutGetClient {

	public static PutGetWsApi getClient(String wsUrl) {
		JaxWsClientFactoryBean factory = new JaxWsClientFactoryBean();
		factory.setServiceClass(PutGetWsApi.class);
		factory.setAddress(wsUrl);
		return (PutGetWsApi) factory.create();
	}
}
