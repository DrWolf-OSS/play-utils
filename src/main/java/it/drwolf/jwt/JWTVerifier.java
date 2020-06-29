package it.drwolf.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.typesafe.config.Config;

import play.Logger;

import javax.inject.Inject;

/**
 * Verificatore token JWT
 */
public class JWTVerifier {

    private static final String PLAY_SECRET_KEY = "play.http.secret.key";

    private static final String ERR_MSG="Error creating the JWT verifier:";

    private static final Logger.ALogger logger = Logger.of(JWTVerifier.class);

    private com.auth0.jwt.JWTVerifier delegate;

    private String secret;

    private String issuer;

    /**
     * Crea il verificatore per il token JWT usando come secret quella specificata in play.http.secret.key e come issuer drwolf.it
     * @param config configurazione di Play (application.conf)
     * @throws JWTVerificationException se la chiave play.http.secret.key non è definita nel file di configurazione di play.
     */
    @Inject
    public JWTVerifier(Config config) throws JWTVerificationException {
        this.secret = config.getString(JWTVerifier.PLAY_SECRET_KEY);

        if (this.secret == null || this.secret.trim().isEmpty()) {
            throw new JWTVerificationException(JWTVerifier.ERR_MSG+ JWTVerifier.PLAY_SECRET_KEY + " was not found in play configuration");
        }

        this.issuer = JWTConstants.ISSUER;

        this.init();
    }

    /**
     * Verifica il token utilizzando l'issuer e il secret specificati.L'issuer e il secret devono essere gli stessi che sono
     * stati usati per creare il token
     * @param secret
     * @param issuer
     * @throws JWTVerificationException
     */
    public JWTVerifier(String secret,String issuer) throws JWTVerificationException {

        if (this.secret==null || this.secret.trim().isEmpty())
        {
            throw new JWTVerificationException(JWTVerifier.ERR_MSG+" secret not set.");
        }
        else if (issuer==null || issuer.trim().isEmpty())
        {
            throw new JWTVerificationException(JWTVerifier.ERR_MSG+" issuer not set.");
        }

        this.secret=secret;
        this.issuer=issuer;

    }

    public DecodedJWT verify(String token)  throws JWTVerificationException {
        return this.delegate.verify(token);
    }

    protected void init() {
        Algorithm algorithm = Algorithm.HMAC256(this.secret);
        this.delegate = JWT.require(algorithm).withIssuer(this.issuer).build();
    }

}
