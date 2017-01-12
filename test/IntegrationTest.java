import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

import org.junit.Test;

import play.libs.Json;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;

public class IntegrationTest {

	@Test
	public void testMessageStore () {
		
		running (fakeApplication (), new Runnable () {
			@Override
			public void run () {
				final Result result = callAction (
						controllers.routes.ref.MessageStore.put (),
						fakeRequest ()
							.withTextBody ("<a>Hello, World!</a>")
							.withHeader ("Content-Type", "text/xml")
					);
				
				assertThat (status (result)).isEqualTo (OK);
				assertThat (contentType (result)).isEqualTo ("application/json");
				
				final JsonNode node = Json.parse (contentAsString (result));
				
				assertThat (node).isNotNull ();
				assertThat (node.path ("key").isTextual ()).isTrue ();
				
				final String key = node.path ("key").asText ();
				
				final Result getResult = callAction (controllers.routes.ref.MessageStore.get (key));
				
				assertThat (status (getResult)).isEqualTo (OK);
				assertThat (contentType (getResult)).isEqualTo ("text/xml");
				assertThat (contentAsString (getResult)).isEqualTo ("<a>Hello, World!</a>");
			}
		});
	}
}