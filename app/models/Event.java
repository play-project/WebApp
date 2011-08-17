package models;

public class Event {
	private String streamId;
	private String title;
	private String content;

	public Event(String title, String content) {
		super();
		this.title = title;
		this.content = content;
	}
	
	/**
	 * GETTERS AND SETTERS
	 */

	public String getStreamId() {
		return streamId;
	}

	public void setStreamId(String id) {
		this.streamId = id;
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
		return "Event [streamId=" + streamId + ", title=" + title + ", content=" + content + "]";
	}
}
