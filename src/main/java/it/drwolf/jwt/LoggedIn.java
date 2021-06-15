package it.drwolf.jwt;

import play.mvc.Http;
import play.mvc.Security.Authenticator;

import javax.inject.Inject;
import java.util.Optional;

public class LoggedIn extends Authenticator {

	@Inject
	private JWTUtils jwtUtils;

	@Inject
	public LoggedIn() {
	}

	@Override
	public Optional<String> getUsername(Http.Request req) {
		return Optional.of(jwtUtils.getUser(req).toString());
	}
}
