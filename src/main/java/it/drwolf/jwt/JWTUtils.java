package it.drwolf.jwt;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import it.drwolf.exceptions.HttpException;
import play.libs.Json;
import play.mvc.Http;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.security.Principal;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Singleton
public class JWTUtils {

	private final com.auth0.jwt.JWTVerifier delegate;
	private final JwkProvider jwkProvider;
	private Config config;

	@Inject
	public JWTUtils(Config config) {
		this.config = config;
		this.delegate = JWT.require(getAlgorithm()).withIssuer(getIssuer()).build();
		this.jwkProvider = new JwkProviderBuilder(config.getString("jwks.domain")).build();
	}

	public String create(Principal user) {
		com.auth0.jwt.JWTCreator.Builder builder = JWT.create().withIssuer(getIssuer());
		builder.withExpiresAt(getExpiration());
		try {
			builder.withClaim(getUserClaim(), new ObjectMapper().writeValueAsString(Json.toJson(user)));
		} catch (JsonProcessingException e) {
			throw new HttpException("Error encoding user in token", e);
		}
		return builder.sign(getAlgorithm());

	}

	protected Algorithm getAlgorithm() {
		return Algorithm.HMAC256(getSecret());
	}

	protected Date getExpiration() {
		return new Date(
				new Date().getTime() + Optional.ofNullable(config.getDuration("jwt.expiration", TimeUnit.MILLISECONDS))
						.orElse(8 * 3600000l));
	}

	protected String getIssuer() {
		return Optional.ofNullable(config.getString("jwt.issuer")).orElse("drwolf.it");
	}

	protected String getSecret() {
		return this.config.getString("play.http.secret.key");
	}

	private Optional<String> getTokenFromHeaders(Map<String, List<String>> headers) {
		List<String> headerAuth = headers.get("Authorization");
		if ((headerAuth != null) && (headerAuth.size() == 1) && (headerAuth.get(0) != null)) {
			String authorization = headerAuth.get(0);
			return Optional.of(authorization.replace("Bearer ", ""));
		}
		return Optional.empty();
	}

	private Optional<String> getTokenFromQueryString(Map<String, String[]> queryString) {
		if (queryString != null && queryString.containsKey("token")) {
			String[] tokens = queryString.get("token");
			if (tokens != null && tokens.length > 0) {
				return Optional.of(tokens[0]);
			}
		}
		return Optional.empty();
	}

	public Optional<String> getTokenFromRequest(Http.Request request) {
		Optional<String> h = getTokenFromHeaders(request.getHeaders().asMap());
		if (h.isPresent()) {
			return h;
		}
		return getTokenFromQueryString(request.queryString());
	}

	public ObjectNode getUser(Http.Request request) {
		DecodedJWT decoded = this.verify(getTokenFromRequest(request).orElseThrow(
				() -> new HttpException("Token not found", HttpException.Status.UNAUTHORIZED)));
		try {
			return (ObjectNode) Json.parse(decoded.getClaim(getUserClaim()).asString());
		} catch (Exception e) {
			throw new HttpException("Invalid user data", HttpException.Status.UNAUTHORIZED);
		}
	}

	protected String getUserClaim() {
		return Optional.of(this.config.getString("jwt.userClaim")).orElse("user");
	}

	private DecodedJWT verify(String token) {
		try {
			return this.delegate.verify(token);
		} catch (Exception e) {
			throw new HttpException("Invalid token", e, HttpException.Status.UNAUTHORIZED);
		}
	}

	public DecodedJWT verifyJWKS(String accessToken) {
		try {
			DecodedJWT decodedJwt = JWT.decode(accessToken);
			Jwk jwk = jwkProvider.get(decodedJwt.getKeyId());
			Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
			Verification verifier = JWT.require(algorithm);
			verifier.build().verify(decodedJwt);
			return decodedJwt;
		} catch (JwkException e) {
			throw new HttpException("Error verifying JWKS Token", e);
		}
	}

}
