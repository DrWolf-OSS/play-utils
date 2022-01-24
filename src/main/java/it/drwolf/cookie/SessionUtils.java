package it.drwolf.cookie;

import java.util.Optional;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.drwolf.base.interfaces.LoggedUser;
import play.Logger;
import play.libs.Json;
import play.mvc.Http.Session;

public class SessionUtils {

	private SessionUtils() {
	}

	/**
	 * @param session
	 * @return serialized LoggedUser
	 */
	public static LoggedUser getLoggedUser(Session session) {
		try {
			Optional<String> userAsOptional = session.get(LoggedUser.SESSION_KEY);
			if (userAsOptional.isPresent() && !userAsOptional.get().trim().isEmpty()) {
				ObjectMapper mapper = new ObjectMapper();
				JsonParser parser = mapper.getFactory().createParser(userAsOptional.get());
				return Json.fromJson(mapper.readTree(parser), LoggedUser.class);
			}
		} catch (Exception e) {
			Logger.of(SessionUtils.class.getName()).error("Error getting User from Session", e);
		}
		return null;
	}

	public static void setLoggedUser(Session session, LoggedUser user) {
		final JsonNode userAsJsonNode = Json.toJson(user);
		SessionUtils.setLoggedUser(session, userAsJsonNode);
	}

	public static void setLoggedUser(Session session, JsonNode user) {
		session.adding(LoggedUser.SESSION_KEY, user.toString());
	}

}
