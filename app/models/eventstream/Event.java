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
	private String htmlContent;
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
	
	public Event(String title, String content, String htmlContent, String icon) {
		this(title, content, icon);
		this.htmlContent = htmlContent;
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

	public String getHtmlContent() {
		if (htmlContent == null) {
			return content;
		}
		return htmlContent;
	}

	public void setHtmlContent(String htmlContent) {
		this.htmlContent = htmlContent;
	}

	@Override
	public String toString() {
		return "Event [topicId=" + getTopicId() + ", title=" + getTitle() + ", content=" + getContent() + "]";
	}

	/**
	 * Factory method to create {@linkplain Event}s from RDF {@linkplain Model}s.
	 */
	public static Event eventFromRdf(Model rdf) {
		String content;
		String htmlContent;
		String eventIcon;
		String eventTitle;
		
		// Title:
		eventTitle = createEventTitle(rdf);
		
		// Icon:
		eventIcon = EventTypeMetadata.getIcon(rdf);
	
		// Plain text representation:
		content = rdf.serialize(Syntax.Turtle);
		
		// HTML representation:
		// FIXME stuehmer: this is a hack to hide the many namespace declarations... we should nicely "fold"/"collapse" instead of deleting
		htmlContent = content.replaceAll("@prefix.*?>\\s*?\\.", "").trim();
		htmlContent = HTML.htmlEscape(htmlContent).replaceAll("\n", "<br />").replaceAll("\\s{4}", "&nbsp;&nbsp;&nbsp;&nbsp;");
		htmlContent = "<table width='100%'><tr><td width='16'><img src='" + eventIcon + "' alt='' align='middle' align='left' /></td><td><h3>" + eventTitle + "</h3></td></tr><tr><td></td><td>" + htmlContent + "</td></tr></table>";
		
		
		return new Event(eventTitle, content, htmlContent, eventIcon);
	
	}

	public static String createEventTitle(Model rdf) {
		String eventTitle = EventTypeMetadata.getType(rdf);
	
		return eventTitle.substring(eventTitle.lastIndexOf("/") + 1);
	}

}
