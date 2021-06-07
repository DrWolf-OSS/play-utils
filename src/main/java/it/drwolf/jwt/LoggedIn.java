package it.drwolf.jwt;

import com.typesafe.config.Config;
import play.mvc.Http;
import play.mvc.Security.Authenticator;

import javax.inject.Inject;
import java.util.Optional;

public class LoggedIn extends Authenticator {

	private final String usernameField;

	@Inject
	private JWTUtils jwtUtils;

	@Inject
	public LoggedIn(Config config) {
		super();
		usernameField = Optional.of(config.getString("jwt.username")).orElse("username");
	}

	@Override
	public Optional<String> getUsername(Http.Request req) {
		return Optional.of(jwtUtils.getUser(req).get(usernameField).asText());
	}
}
