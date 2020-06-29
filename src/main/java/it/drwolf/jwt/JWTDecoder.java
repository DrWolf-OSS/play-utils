package it.drwolf.jwt;


import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;

import javax.inject.Inject;

public class JWTDecoder<U> {

    protected JWTVerifier jwtVerifier;

    @Inject
    public JWTDecoder(JWTVerifier jwtVerifier) {
        this.jwtVerifier = jwtVerifier;
    }

    /**
     * Acquisisce l'utente dal token JWT precedentemente creato con il JWTCreator
     * @param token
     * @param userClass
     * @return
     */
    public U getUser(String token,Class<U> userClass) {
        DecodedJWT decoded = this.jwtVerifier.verify(token);
        JsonNode userAsJson = Json.parse(decoded.getClaim(JWTConstants.JWT_USER_KEY).asString());
        return Json.fromJson(userAsJson, userClass);
    }
}
