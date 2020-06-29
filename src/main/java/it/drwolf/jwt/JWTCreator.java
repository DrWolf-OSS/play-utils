package it.drwolf.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.typesafe.config.Config;
import org.joda.time.DateTime;
import play.Logger;

import javax.inject.Inject;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Crea il token JWT utilizzando la secret definita nella configurazione di play in play.http.secret.key oppure la secrete che viene
 * specificata dall'utente.
 */
public class JWTCreator {

    private final Logger.ALogger logger = Logger.of(this.getClass());

    private final Config config;

    @Inject
    public JWTCreator(Config configuration) {
        this.config = configuration;
    }

    /**
     * Crea il token autorizzativo usando i valori di default:
     * la secret per la firma viene acquisita dalla configurazione di Play dalla chiave play.http.secret.key
     * il token scade alle dopo un giorno e 2 ore dalla data di creazione,l'issuer è drwolf.it
     * @param payloadClaims
     * @return
     */
    public String create(Map<String, Object> payloadClaims)
    {
        return create(payloadClaims,null,0,null);
    }

    /**
     * Crea il token autorizzativo JWT
     * @param payloadClaims coppia chiavi-volori da inserire nel token
     * @param secret valore usato per la firma del token.Se non specificata,
     *               utilizza la secret presente nella configurazione di play nella chiave play.http.secret.key
     * @param expireSeconds scadenza del token in secondi a partire dal momento della creazione, se impostato su zero o valore negativo utilizza il valore di default (alle 2 di notte)
     * @param issuer token issuer, se non specificato utizza drwolf.it
     * @return il token JWT con issuer drwolf.it
     */
    public String create(Map<String, Object> payloadClaims,String secret,int expireSeconds,String issuer) {

        secret = secret == null || secret.trim().isEmpty() ? this.config.getString("play.http.secret.key"):secret;

        Algorithm hmac256 = Algorithm.HMAC256(secret);
        com.auth0.jwt.JWTCreator.Builder builder = JWT.create().withIssuer(issuer==null || issuer.trim().isEmpty() ? JWTConstants.ISSUER:issuer);

        if (expireSeconds<=0) {
            builder.withExpiresAt(this.computeDefaultExpirationDate());
        }
        else {
            builder.withExpiresAt(new DateTime().plusSeconds(expireSeconds).toDate());
        }

        if (payloadClaims != null && !payloadClaims.isEmpty()) {
            Set<Map.Entry<String, Object>> entrySet = payloadClaims.entrySet();
            Iterator<Map.Entry<String, Object>> iterator = entrySet.iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                builder.withClaim(entry.getKey(), entry.getValue().toString());
            }
        } else {
            this.logger.warn("You are building a JWT without header claims!");
        }

        return builder.sign(hmac256);

    }

    private Date computeDefaultExpirationDate() {
        return  new DateTime().withTimeAtStartOfDay().plusDays(1).plusHours(2).toDate();
    }

}
