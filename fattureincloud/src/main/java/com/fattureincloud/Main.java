package com.fattureincloud;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import it.fattureincloud.sdk.auth.OAuth2AuthorizationCodeError;
import it.fattureincloud.sdk.auth.OAuth2AuthorizationCodeManager;
import it.fattureincloud.sdk.auth.OAuth2AuthorizationCodeParams;
import it.fattureincloud.sdk.auth.OAuth2AuthorizationCodeResponse;
import it.fattureincloud.sdk.auth.Scope;

import java.util.List;

import io.vavr.control.Either;

public class Main {
    public static void main(String[] args) {
        String redirectUri = "http://localhost:3000/oauth";
        OAuth2AuthorizationCodeManager oauth = new OAuth2AuthorizationCodeManager(
            "", 
            "", 
            redirectUri);

        List<Scope> scopes = Arrays.asList(Scope.SETTINGS_ALL, Scope.ISSUED_DOCUMENTS_INVOICES_READ);


        /*
         * Il parametro “state” è un valore che viene inviato al server di autorizzazione durante la richiesta di autorizzazione 
         * e poi restituito indietro al client nella risposta di autorizzazione.
         * 
         * Nel tuo codice, “EXAMPLE_STATE” è solo un valore di esempio per il parametro “state”. 
         * Dovresti sostituirlo con un valore generato in modo sicuro e univoco per la tua sessione o richiesta. 
         * Se non hai bisogno di mantenere lo stato dell’applicazione, puoi semplicemente ignorare il valore restituito. 
         * Tuttavia, dovresti comunque verificare che il valore “state” restituito corrisponda a quello inviato per mitigare gli attacchi CSRF.
         */

        String stateParam = "EXAMPLE_STATE";
 
        /*
        SecureRandom sr = new SecureRandom();
        byte[] state = new byte[24];
        sr.nextBytes(state);

        stateParam = Base64.getUrlEncoder().withoutPadding().encodeToString(state);
        */

        System.out.println(oauth.getAuthorizationUrl(scopes, stateParam));

        try {
            OAuth2AuthorizationCodeParams params = oauth.getParamsFromUrl("http://localhost:3000/oauth?code=" + "EXAMPLE_CODE" + "&state="  + "EXAMPLE_STATE");
        
            String authorizationCode = params.getAuthorizationCode();
            // String statusCode = params.getState();

            Either<OAuth2AuthorizationCodeError, OAuth2AuthorizationCodeResponse> result = oauth.fetchToken(authorizationCode);

            if (result.isRight()) {
                OAuth2AuthorizationCodeResponse tokenObj = result.get();
                String accessToken = tokenObj.getAccessToken();

                System.out.println(accessToken);
            } else {
                OAuth2AuthorizationCodeError error = result.getLeft();
                System.out.println("error: " + error.toString());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}