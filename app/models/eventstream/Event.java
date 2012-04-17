package models.eventstream;

/**
 * Simple event class used to display events on the web page
 * 
 * @author Alexandre Bourdin
 *
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
		return "Event [topicId=" + topicId + ", title=" + title + ", content=" + content + "]";
	}
}
