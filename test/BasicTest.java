import org.junit.*;
import java.util.*;
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
	    new User("user1", "Claw", "pwd", "Alex", "abourdin@polytech.unice.fr").save();
	    
	    // Test 
	    User claw = User.find("byEmail", "abourdin@polytech.unice.fr").first();
	    assertNotNull(claw);
	    assertEquals("Alex", claw.name);
	    
	    claw = User.find("byUserId", "user1").first();
	    assertNotNull(claw);
	    assertEquals("Claw", claw.login);
	}
	
	@Test
	public void tryConnectAsUser() {
	    // Create a new user and save it
	    new User("user1", "Claw", "pwd", "Alex", "abourdin@polytech.unice.fr").save();

		EntityPool ep = EntityPool.get();
	    // Test 
	    assertNotNull(ep.connect("Claw", "pwd"));
	    assertNull(ep.connect("Claw", "wrong"));
	    assertNull(ep.connect("Test", "blabla"));
	}
}
