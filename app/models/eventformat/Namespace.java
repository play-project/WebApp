package models.eventformat;

/*
 * Enum of namespaces commonly used in PLAY events.
 */
public enum Namespace {

	/* 
	 * PLAY specific namespaces:
	 */
	TYPES("http://events.event-processing.org/types/", ""),
	EVENTS("http://events.event-processing.org/ids/", "e"),
	STREAMS("http://streams.event-processing.org/ids/", "s"),
	SOURCE("http://sources.event-processing.org/ids/", "src"),
	
	/*
	 * generic namespaces:
	 */
	XSD_NS("http://www.w3.org/2001/XMLSchema#", "xsd"),
	RDF("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf"),
	RDFS("http://www.w3.org/2000/01/rdf-schema#", "rdfs"),
	OWL("http://www.w3.org/2002/07/owl#", "owl"),
	GEONAMES("http://www.geonames.org/ontology#", "gn"),
	USER("http://graph.facebook.com/schema/user#", "user"),
	SIOC("http://rdfs.org/sioc/ns#", "sioc");
	
	private final String NS;
	private final String NS_PREFIX;
	
	Namespace(String NS, String NS_PREFIX) {
		this.NS = NS;
		this.NS_PREFIX = NS_PREFIX;
	}
	
	public String getUri() {
		return NS;
	}

	public String getPrefix() {
		return NS_PREFIX;
	}

}
