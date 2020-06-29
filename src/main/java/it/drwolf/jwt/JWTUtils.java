package it.drwolf.jwt;

import play.Logger;
import play.mvc.Http;

import java.util.List;
import java.util.Map;

public final class JWTUtils {

    private static final Logger.ALogger LOGGER = Logger.of(JWTUtils.class);

    /**
     * Acquisisce il JWT token dalla richiesta Http.
     * Prima controlla se il token è presente nell'header Authorization se l'header non è presente, ricerca il token nella query string prendendolo dal parametro "token".
     * @param request richiesta Http
     * @return il token o null se il token non è presente.
     */
    public static String getTokenFromRequest(Http.Request request) {
        String token = JWTUtils.getTokenFromHeaders(request.getHeaders().toMap());
        if (token == null) {
            token = JWTUtils.getTokenFromQueryString(request.queryString());
        }
        return token;
    }

    /**
     * Acquisisce il token dall'header.Se l'header inizia con "Bearer " restituisce il testo senza "Bearer "
     * @param header
     * @return
     */
    public static String stripBearer(String header) {
        if (header.startsWith("Bearer ")) {
            return header.replace("Bearer ", "");
        }
        return null;
    }

    private static String getTokenFromHeaders(Map<String, List<String>> headers) {
        List<String> headerAuth = headers.get(JWTConstants.AUTHORIZATION_HEADER);
        if ((headerAuth != null) && (headerAuth.size() == 1) && (headerAuth.get(0) != null)) {
            String authorization = headerAuth.get(0);
            return JWTUtils.stripBearer(authorization);
        }
        JWTUtils.LOGGER.warn("JWT not found in " + JWTConstants.AUTHORIZATION_HEADER + " headers!");
        return null;
    }

    private static String getTokenFromQueryString(Map<String, String[]> queryString) {
        if (queryString != null && queryString.containsKey("token")) {
            String[] tokens = queryString.get("token");
            if (tokens != null && tokens.length > 0) {
                return tokens[0];
            }
        }
        return null;
    }



    private JWTUtils() {
    }

}
