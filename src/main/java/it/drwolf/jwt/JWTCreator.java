package it.drwolf.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.typesafe.config.Config;
import it.drwolf.base.interfaces.Loggable;
import org.joda.time.DateTime;
import play.Logger;
import play.libs.Json;

import javax.inject.Inject;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Crea il token JWT utilizzando la secret definita nella configurazione di play in play.http.secret.key oppure la secrete che viene
 * specificata dall'utente.
 */
public class JWTCreator<U> implements Loggable {


}
