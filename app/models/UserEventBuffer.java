package models;

import java.util.*;

import javax.persistence.*;

import play.db.jpa.*;
import play.libs.F.ArchivedEventStream;
import play.libs.F.EventStream;

public class UserEventBuffer {
	private final ArchivedEventStream<Event> stream = new ArchivedEventStream<Event>(20);

	public UserEventBuffer() {

	}

	public void publish(Event e) {
		stream.publish(e);
	}

	/**
	 * GETTERS AND SETTERS
	 */

	public ArchivedEventStream getArchivedEventStream() {
		return stream;
	}

	public EventStream getEventStream() {
		return stream.eventStream();
	}
}
