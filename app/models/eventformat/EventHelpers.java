package models.eventformat;

import org.ontoware.rdf2go.RDF2Go;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.impl.URIImpl;

public final class EventHelpers {
	
	/**
	 * Create an empty RDF2Go model with some useful namespaces predefined.
	 */
	public static Model createEmptyModel() {
		return createEmptyModel(new URIImpl("http://www.inria.fr"));
	}
	
	/**
	 * Create an empty RDF2Go model with some useful namespaces predefined.
	 * 
	 * @param contextUri RDF context URI for quads.
	 */
	public static Model createEmptyModel(URI contextUri) {
		Model model = RDF2Go.getModelFactory().createModel(
				contextUri);
		model.open();
		
		for (Namespace ns : Namespace.values()) {
			model.setNamespace(ns.getPrefix(), ns.getUri());
		}

		return model;
	}

	/**
	 * Create an empty RDF2Go model with some useful namespaces predefined.
	 * 
	 * @param contextUri RDF context URI for quads.
	 */
	public static Model createEmptyModel(String contextUri) {
		return createEmptyModel(new URIImpl(contextUri));
	}

}
