package it.drwolf.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import it.drwolf.exceptions.HttpException;
import play.libs.Json;
import play.mvc.Http;

import javax.inject.Inject;
import java.lang.reflect.ParameterizedType;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class JWTUtils<U> {

    protected final Class<U> resourceClass;

    private final com.auth0.jwt.JWTVerifier delegate;

    public JWTUtils() {
        this.delegate = JWT.require(getAlgorithm()).withIssuer(getIssuer()).build();
        this.resourceClass = (Class<U>) ((ParameterizedType) this.getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
    }

    @Inject
    private Config config;

    public String create(U user) {
        com.auth0.jwt.JWTCreator.Builder builder = JWT.create().withIssuer(getIssuer());
        builder.withExpiresAt(getExpiration());
        builder.withClaim(getUserClaim(), Json.toJson(user).toString());
        return builder.sign(getAlgorithm());

    }

    protected Algorithm getAlgorithm() {
        return Algorithm.HMAC256(getSecret());
    }

    protected String getSecret() {
        return this.config.getString("play.http.secret.key");
    }

    protected String getUserClaim() {
        return Optional.of(this.config.getString("jwt.userClaim")).orElse("user");
    }

    protected String getIssuer(){
        return Optional.ofNullable(config.getString("jwt.issuer")).orElse("drwolf.it");
    }

    protected Date getExpiration(){
        return new Date(
                new Date().getTime() +
                        Optional.ofNullable(
                                config.getDuration("jwt.expiration", TimeUnit.MILLISECONDS))
                                .orElse(8*3600000l));
    }

    public Optional<String> getTokenFromRequest(Http.Request request) {
        Optional<String> h = getTokenFromHeaders(request.getHeaders().asMap());
        if (h.isPresent()){
            return h;
        }
        return getTokenFromQueryString(request.queryString());
    }

    public U getUser(Http.Request request) {
        DecodedJWT decoded = this.verify(getTokenFromRequest(request).orElseThrow(() -> new HttpException("Token not found",
                HttpException.Status.UNAUTHORIZED)));
        try {
            JsonNode userAsJson = Json.parse(decoded.getClaim(getUserClaim()).asString());
            return Json.fromJson(userAsJson, resourceClass);
        } catch (Exception e) {
            throw new HttpException("Invalid user data", HttpException.Status.UNAUTHORIZED);
        }
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

    private DecodedJWT verify(String token) {
        try {
            return this.delegate.verify(token);
        } catch (Exception e){
            throw new HttpException("Invalid token", e, HttpException.Status.UNAUTHORIZED);
        }
    }


}
