package models.eventstream;

import java.util.Iterator;

import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.Syntax;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.Variable;
import org.ontoware.rdf2go.model.node.impl.URIImpl;
import org.ontoware.rdf2go.vocabulary.RDF;

import play.utils.HTML;

/**
 * Simple event class used to display events on the web page
 * 
 * @author Alexandre Bourdin
 * @author Roland Stühmer
 */
public class Event {
	private String topicId;
	private String title;
	private String content;
	private String icon;

	public Event(String title, String content) {
		super();
		this.title = title;
		this.content = content;
	}
	
	/**
	 * GETTERS AND SETTERS
	 */

	public String getTopicId() {
		return topicId;
	}

	public void setTopicId(String id) {
		this.topicId = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return "Event [topicId=" + getTopicId() + ", title=" + getTitle() + ", content=" + getContent() + "]";
	}
	
	/**
	 * Factory method to create {@linkplain Event}s from RDF {@linkplain Model}s.
	 */
	public static Event eventFromRdf(Model rdf) {
		String eventText;

		eventText = rdf.serialize(Syntax.Turtle);
		// FIXME stuehmer: this is a hack to hide the many namespace declarations... we should nicely "fold"/"collapse" instead of deleting
		eventText = eventText.replaceAll("@prefix.*?> \\.", "").trim();
		eventText = HTML.htmlEscape(eventText).replaceAll("\n", "<br />").replaceAll("\\s{4}", "&nbsp;&nbsp;&nbsp;&nbsp;");

		return new Event(createEventTitle(rdf), eventText);

	}
	
	/**
	 * Factory method to create HTML-formatted {@linkplain Event}s from RDF {@linkplain Model}s.
	 */
	public static Event eventPrettyPrintFromRdf(Model rdf) {
		String eventText = "";

		return new Event(createEventTitle(rdf), eventText);
	
	}
	
	public static String createEventTitle(Model rdf) {
		String eventTitle;

		// First try RDF types with the expected event ID
		if (rdf.getContextURI() != null) {
			URI eventId = new URIImpl(rdf.getContextURI().toString()
					+ eu.play_project.play_commons.constants.Event.EVENT_ID_SUFFIX);
			Iterator<Statement> it = rdf.findStatements(eventId, RDF.type,
					Variable.ANY);
			if (it.hasNext()) {
				Statement stat = it.next();
				eventTitle = stat.getObject().asURI().asJavaURI().getPath();
				eventTitle = eventTitle.substring(eventTitle.lastIndexOf("/") + 1);
				return eventTitle;
			}
		}
		// Then try any RDF types
		Iterator<Statement> it2 = rdf.findStatements(Variable.ANY, RDF.type,
				Variable.ANY);
		if (it2.hasNext()) {
			Statement stat = it2.next();
			eventTitle = stat.getObject().asURI().asJavaURI().getPath();
			eventTitle = eventTitle.substring(eventTitle.lastIndexOf("/") + 1);
			return eventTitle;
		}
		// Then fall back to a constant String
		else {
			eventTitle = "RDF Event";
			return eventTitle;
		}

	}

}
