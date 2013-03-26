package models.eventstream;

import static eu.play_project.play_commons.constants.Event.EVENT_ICON_DEFAULT;

import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.Syntax;

import play.utils.HTML;
import eu.play_project.play_commons.eventtypes.EventTypeMetadata;

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
		this(title, content, EVENT_ICON_DEFAULT);
	}
	
	public Event(String title, String content, String icon) {
		super();
		this.title = title;
		this.content = content;
		this.icon = icon;
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
	
	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
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
		String eventIcon;
		
		eventText = rdf.serialize(Syntax.Turtle);
		// FIXME stuehmer: this is a hack to hide the many namespace declarations... we should nicely "fold"/"collapse" instead of deleting
		eventText = eventText.replaceAll("@prefix.*?> \\.", "").trim();
		eventText = HTML.htmlEscape(eventText).replaceAll("\n", "<br />").replaceAll("\\s{4}", "&nbsp;&nbsp;&nbsp;&nbsp;");

		eventIcon = EventTypeMetadata.getEventTypeIcon(EventTypeMetadata.getEventType(rdf));
		
		return new Event(createEventTitle(rdf), eventText, eventIcon);

	}
	
	/**
	 * Factory method to create HTML-formatted {@linkplain Event}s from RDF {@linkplain Model}s.
	 */
	public static Event eventPrettyPrintFromRdf(Model rdf) {
		String eventText = "";
		String eventIcon = "";

		return new Event(createEventTitle(rdf), eventText, eventIcon);
	
	}
	
	public static String createEventTitle(Model rdf) {
		String eventTitle = EventTypeMetadata.getEventType(rdf);

		return eventTitle.substring(eventTitle.lastIndexOf("/") + 1);
	}

}
