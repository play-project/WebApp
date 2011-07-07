package models;

public class Event {
	private Long eventId;
	private Long streamId;
	private String name;
	private String content;

	public Event(Long eventId, Long id, String name, String content) {
		super();
		this.eventId = eventId;
		this.streamId = id;
		this.name = name;
		this.content = content;
	}
	
	/**
	 * GETTERS AND SETTERS
	 */

	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}

	public Long getStreamId() {
		return streamId;
	}

	public void setStreamId(Long streamId) {
		this.streamId = streamId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return "Event [eventId=" + eventId + ", streamId=" + streamId + ", name=" + name + ", content="
				+ content + "]";
	}
}
