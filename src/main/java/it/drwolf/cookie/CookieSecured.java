package it.drwolf.cookie;

import java.util.Optional;

import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security.Authenticator;

/**
 * EX:
 *
 * @Authenticated(CookieSecured.class) public Result authenticatedRoute() {
 * return Results.ok(Json.toJson("Autentication checked"));
 * }
 */
public class CookieSecured extends Authenticator {

	@Override
	public Optional<String> getUsername(Http.Request request) {
		try {
			return Optional.of(SessionUtils.getLoggedUser(request.session()).getUsername());
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	@Override
	public Result onUnauthorized(Http.Request request) {
		return Results.unauthorized(Json.toJson("Invalid or expired session"));
	}

}
