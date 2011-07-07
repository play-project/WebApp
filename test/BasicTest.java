import org.junit.*;
import java.util.*;
import java.util.concurrent.ExecutionException;

import play.test.*;
import models.*;

public class BasicTest extends UnitTest {

	@SuppressWarnings("deprecation")
	@Before
	public void setup() {
		Fixtures.deleteAll();
	}

	@Test
	public void createAndRetrieveUser() {
		// Create a new user and save it
		new User("Claw", "pwd", "Alex", "abourdin@polytech.unice.fr").save();

		// Test
		User claw = User.find("byEmail", "abourdin@polytech.unice.fr").first();
		assertNotNull(claw);
		assertEquals("Alex", claw.name);
		assertEquals("Claw", claw.login);
	}

	@Test
	public void tryConnectAsUser() {
		// Create a new user and save it
		new User("Claw", "pwd", "Alex", "abourdin@polytech.unice.fr").save();

		ModelManager ep = ModelManager.get();
		// Test
		assertNotNull(ep.connect("Claw", "pwd"));
		assertNull(ep.connect("Claw", "wrong"));
		assertNull(ep.connect("Test", "blabla"));
	}

	@Test
	public void createAndRetrieveStreamEventBuffer() {
		// Create a new stream and save it
		new StreamEventBuffer("http://www.wservice.com/stream1").save();

		// Test
		StreamEventBuffer eb = StreamEventBuffer.find("bySource", "http://www.wservice.com/stream1")
				.first();
		assertNotNull(eb);
		assertEquals("http://www.wservice.com/stream1", eb.source);
	}

	@Test
	public void trySubscribeStream() throws InterruptedException, ExecutionException {
		new StreamEventBuffer("http://www.wservice.com/stream1").save();
		new User("Claw", "pwd", "Alex", "abourdin@polytech.unice.fr").save();

		// Test
		StreamEventBuffer eb = StreamEventBuffer.find("bySource", "http://www.wservice.com/stream1")
				.first();
		User claw = User.find("byEmail", "abourdin@polytech.unice.fr").first();

		ModelManager.get().getConnectedUsers().add(claw);
		ModelManager.get().getStreams().add(eb);

		claw.subscribe(eb.id);
		assertEquals(1, eb.subscribingUsers.size());
		eb.publishEvent(new Event(1L, eb.id, "event1", "event 1 content"));
		assertNotNull(claw.getEventBuffer().getEventStream().nextEvent());
	}
}
