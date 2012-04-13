package models.eventformat;

import javax.xml.namespace.QName;

/*
 * Enum of stream identifiers (also used for topic names) commonly used in PLAY.
 */
public enum Stream {

	/* 
	 * PLAY streams:
	 */
	FacebookStatusFeed("http://streams.event-processing.org/ids/", "FacebookStatusFeed", Namespace.STREAMS.getPrefix()),
	FacebookCepResults("http://streams.event-processing.org/ids/", "FacebookCepResults", Namespace.STREAMS.getPrefix()),
	PachubeFeed("http://streams.event-processing.org/ids/", "PachubeFeed", Namespace.STREAMS.getPrefix()),
	TwitterFeed("http://streams.event-processing.org/ids/", "TwitterFeed", Namespace.STREAMS.getPrefix());
	
	private final QName qname;
	
	Stream(String namespaceUri, String localpart) {
		this.qname = new QName(namespaceUri, localpart, Namespace.STREAMS.getPrefix());
	}

	Stream(String namespaceUri, String localpart, String prefix) {
		this.qname = new QName(namespaceUri, localpart, prefix);
	}
	
	Stream(QName qname) {
		this.qname = qname;
	}
	
	public QName getQName() {
		return qname;
	}

	public String getUri() {
		return qname.getNamespaceURI() + qname.getLocalPart();
	}
	
	public String toString() {
		return getUri();
	}

}
