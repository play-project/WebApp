import org.junit.*;
import java.util.*;
import java.util.concurrent.ExecutionException;

import play.libs.F.*;
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
		//new User("Claw", "pwd", "Alex", "abourdin@polytech.unice.fr").save();

		// Test
		/*
		User claw = User.find("byEmail", "abourdin@polytech.unice.fr").first();
		assertNotNull(claw);
		assertEquals("Alex", claw.firstname);
		claw = User.find("byLoginAndPassword", "Claw", "pwd").first();
		assertNotNull(claw);
		assertEquals("Claw", claw.login);
		*/
	}

	@Test
	public void tryConnectAsUser() {
		// Create a new user and save it
		//new User("Claw", "pwd", "Alex", "abourdin@polytech.unice.fr").save();

		ModelManager ep = ModelManager.get();
		// Test
		assertNotNull(ep.connect("Claw", "pwd"));
		assertNull(ep.connect("Claw", "wrong"));
		assertNull(ep.connect("Test", "blabla"));
	}

	@Test
	public void trySubscribeStreamAndPublish() throws InterruptedException, ExecutionException {
		//new EventStreamMC("http://www.wservice.com/stream1").save();
		//new User("Claw", "pwd", "Alex", "abourdin@polytech.unice.fr").save();

		// Prepare
		//EventStreamMC eb = EventStreamMC.find("bySource", "http://www.wservice.com/stream1").first();
		User claw = User.find("byEmail", "abourdin@polytech.unice.fr").first();

		ModelManager.get().getConnectedUsers().add(claw);
		//ModelManager.get().getStreams().add(eb);

		// Subscribe
		//assertTrue(claw.subscribe(eb.id));
		//assertEquals(1, eb.subscribingUsers.size());
		//assertNotNull(eb.subscribingUsers.get(0));

		// Publish and multicast
		//Promise<List<IndexedEvent<Event>>> p1 = ModelManager.get().getUserEventBufferMap().get(claw.id)
		//		.getArchivedEventStream().nextEvents(0);
		//Promise<List<IndexedEvent<Event>>> p2 = ModelManager.get().getUserEventBufferMap().get(claw.id)
		//		.getArchivedEventStream().nextEvents(0);
		Event e1 = new Event("event1", "event 1 content");
		//eb.multicast(e1);
		//assertTrue(p1.isDone());
		//assertFalse(p2.isDone());
		//assertEquals(1, p1.get().size());
		//assertEquals("event1", p1.get().get(0).data.getTitle());
		//assertEquals(e1.toString(), p1.get().get(0).data.toString());
	}
}
