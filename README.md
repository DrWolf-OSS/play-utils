# play-utils

[![](https://jitpack.io/v/drwolf-oss/play-utils.svg)](https://jitpack.io/#drwolf-oss/play-utils)

## JWT

Utilizzare la classe `it.drwolf.jwt.JWTUtils<U>` 

i metodi pubblici sono `create` , `getTokenFromRequest` e `getUser`

è possibile aggiungere questi parametri a `applications.conf`: (questi sono i default)

```
jwt {
  issuer = drwolf.it
  userClaim = user
  expiration = 8 h
}
```

per il formato della scadenza vedere [qui](https://www.playframework.com/documentation/2.8.x/ConfigFile#Duration-format)

## verifica JWKS

`it.drwolf.jwt.JWTUtils.verifyJWKS(String authToken)`

il dominio per la verifica va inserito nella configurazione

```
jwks.domain =  "https://TENANT.eu.auth0.com/"
```

## Eccezioni

aggiungere ad `application.conf` il global handler

```
play.http.errorHandler = it.drwolf.exceptions.ErrorHandler
```
in gnerale lanciare sempre `it.drwolf.exceptions.HttpException`, non restituire `Results.badRequest` o `Results.internalServerError` nei controller

```java
try {
    // do things...
} catch (Exception e){
    throw new HttpException("Your message", e, HttpException.Status.SOME_STATUS);
}
```

## Logger

fate implementare `it.drwolf.base.interfaces.Loggable` alla vostra classe e loggate con `this.logger().info(...)`  ecc..