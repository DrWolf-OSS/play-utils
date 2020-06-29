package it.drwolf.jwt;

import com.typesafe.config.Config;
import play.mvc.Http;
import play.Logger;
import play.Logger.ALogger;

import java.util.NoSuchElementException;

/**
 *
 * @param <U> classe User per l'applicazione corrente
 */
public class JWTHelper<U> {

    private JWTHelper() {

    }

    /**
     * Acquisisce l'utente loggato dal token inviato con la richiesta http.
     *La richiesta deve contenere l'header "Authoirization" con il token oppure il token deve essere passato nella query string nel parametro "token".
     * Il token viene verificato utilizzato come secret quella specificata nella configurazione di Play e come issuer drwolf.it
     * @param request
     * @param config
     * @param userClass
     * @param <U>
     * @return null se il token non è presente, l'utente se il token è vcalido e può essere deserializzato come utente.
     * @throws SecurityException
     */
    public static <U> U getUserFromRequest(Http.Request request, Config config,Class<U> userClass) throws SecurityException {

        try {

            String token = JWTHelper.getTokenFromRequest(request);

            if (token == null) {
                return (U)null;
            }

           token=JWTUtils.stripBearer(token);

            if (token == null || token.trim().isEmpty()) {
                return null;
            }

            JWTVerifier verif = new JWTVerifier(config);
            JWTDecoder<U> decoder = new JWTDecoder<U>(verif);
            U user = decoder.getUser(token,userClass);
            return user;

        }

        catch (Exception ex) {

            final String msg = "Error acquring token from request header:" + ex.getMessage();

            Logger.of(JWTHelper.class).error(msg);

            ex.printStackTrace(System.err);

            return null;

        }
    }

    private static String getTokenFromRequest(Http.Request request) throws SecurityException {

        String token;

        try {
            token = request.getHeaders().get(JWTConstants.AUTHORIZATION_HEADER).get();

        } catch (NoSuchElementException ex) {
            //controlla il token nella query string se non lo trova
            //il token viene inviato nella query string quando si rochiede un url che scarica il file dal server
            //ad esempio nella generazione dei report
            token = request.getQueryString(JWTConstants.AUTHORIZATION_HEADER);

        }

        if (token == null || token.isEmpty()) {
            return null;
        } else {
            return token;
        }

    }
}