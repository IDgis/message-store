package controllers;

import java.util.UUID;

import play.Logger;
import play.cache.Cache;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class MessageStore extends Controller {

	@BodyParser.Of (value = BodyParser.TolerantText.class, maxLength = 32 * 1024)
	public static Result put () {
		if (request ().body ().isMaxSizeExceeded ()) {
			final ObjectNode node = Json.newObject ();
			node.set ("status", Json.toJson ("failure"));
			node.set ("message", Json.toJson (String.format ("Message body is too long")));
			return badRequest (node);
		}
		
		final String body = request ().body ().asText();
		
		final String key = UUID.randomUUID ().toString ();
		Cache.set (key, new Message (request ().getHeader ("Content-Type"), body), 30);
		
		Logger.debug (String.format ("Storing %d bytes with key %s", body == null ? 0 : body.length (), key));
		
		final ObjectNode node = Json.newObject ();
		node.set ("status", Json.toJson ("success"));
		node.set ("key", Json.toJson (key));
		
		return ok (node);
	}
	
	public static Result get (final String messageKey) {
		final Object value = Cache.get (messageKey);
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