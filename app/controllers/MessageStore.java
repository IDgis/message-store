package controllers;

import javax.inject.Inject;
import java.util.UUID;

import play.Logger;
import play.cache.CacheApi;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.http.HttpErrorHandler;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class MessageStore extends Controller {
	
	private CacheApi cache;
	
	@Inject
	public MessageStore(CacheApi cache) {
		this.cache = cache;
	}
	
	public static class Text32Kb extends BodyParser.TolerantText {
		
		@Inject
		public Text32Kb (HttpErrorHandler errorHandler) {
			super (32 * 1024, errorHandler);
		}
	}
	
	@BodyParser.Of (Text32Kb.class)
	public Result put () {
		final String body = request ().body ().asText();
		
		final String key = UUID.randomUUID ().toString ();
		cache.set (key, new Message (request ().getHeader ("Content-Type"), body), 30);
		
		Logger.debug (String.format ("Storing %d bytes with key %s", body == null ? 0 : body.length (), key));
		
		final ObjectNode node = Json.newObject ();
		node.set ("status", Json.toJson ("success"));
		node.set ("key", Json.toJson (key));
		
		return ok (node);
	}
	
	public Result get (final String messageKey) {
		final Object value = cache.get (messageKey);
		if (value == null || !(value instanceof Message)) {
			final ObjectNode node = Json.newObject ();
			node.set ("status", Json.toJson ("failure"));
			node.set ("message", Json.toJson ("Message not found"));
			
			return notFound (node);
		}
		
		final Message message = (Message)value;
		
		return ok (message.message ()).as (message.contentType ());
	}

	private final static class Message {
		private final String contentType;
		private final String message;
		
		public Message (final String contentType, final String message) {
			this.contentType = contentType;
			this.message = message;
		}
		
		public String contentType () {
			return contentType;
		}
		
		public String message () {
			return message;
		}
	}
}