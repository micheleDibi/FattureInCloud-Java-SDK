package com.fattureincloud;

import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.vavr.control.Either;

import java.io.IOException;

import it.fattureincloud.sdk.ApiClient;
import it.fattureincloud.sdk.Configuration;
import it.fattureincloud.sdk.api.IssuedDocumentsApi;
import it.fattureincloud.sdk.auth.OAuth;
import it.fattureincloud.sdk.auth.OAuth2AuthorizationCodeError;
import it.fattureincloud.sdk.auth.OAuth2AuthorizationCodeManager;
import it.fattureincloud.sdk.auth.OAuth2AuthorizationCodeParams;
import it.fattureincloud.sdk.auth.OAuth2AuthorizationCodeResponse;
import it.fattureincloud.sdk.auth.Scope;
import it.fattureincloud.sdk.model.CreateIssuedDocumentRequest;
import it.fattureincloud.sdk.model.CreateIssuedDocumentResponse;
import it.fattureincloud.sdk.model.Entity;
import it.fattureincloud.sdk.model.IssuedDocument;
import it.fattureincloud.sdk.model.IssuedDocumentItemsListItem;
import it.fattureincloud.sdk.model.IssuedDocumentType;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.net.URI;

import java.awt.Desktop;

public class Main {

    // private static int companyId = 850680; // ENTE DI RICERCA SCIENTIFICA ED ALTA FORMAZIONE
    private static int companyId = 1169419; // UNIVERSO SRL

    private static String clientId = "CYLs5KUab44D1abl3vZjdhcI9PjWRNIR";
    private static String clientSecret = "j5dGmQs7LBqTxTnC0qYBiZc805Cnc32l36gaQr7yjc1eBzEsQQQYQPMmhhnG9P15";

    public static void main(String[] args) {

        // Inizializzazione dell'oggetto OAuth2AuthorizationCodeManager
        String redirectURI = "http://localhost:8000";
        OAuth2AuthorizationCodeManager oauth = new OAuth2AuthorizationCodeManager(clientId, clientSecret, redirectURI);

        // Reindirizzamento dell'utente alla pagina di autorizzazione di fatture in
        // cloud
        List<Scope> scopes = Arrays.asList(Scope.SETTINGS_ALL, Scope.ISSUED_DOCUMENTS_INVOICES_READ);

        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(oauth.getAuthorizationUrl(scopes, "EXAMPLE_STATE")));
            }

            HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
            server.createContext("/", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) {
                    URI requestURI = exchange.getRequestURI();
                    String query = requestURI.getQuery();

                    // String urlAuthorization = new BufferedReader(new
                    // InputStreamReader(System.in)).readLine();

                    try {
                        OAuth2AuthorizationCodeParams params = oauth.getParamsFromUrl("http://localhost:8000?" + query);

                        String code = params.getAuthorizationCode();
                        // String state = params.getState();

                        Either<OAuth2AuthorizationCodeError, OAuth2AuthorizationCodeResponse> fetchToken = oauth
                                .fetchToken(code);

                        if (fetchToken.isRight()) {
                            OAuth2AuthorizationCodeResponse tokenObj = fetchToken.get();

                            String accessToken = tokenObj.getAccessToken();
                            String refreshToken = tokenObj.getRefreshToken();

                            System.out.println("Access Token: " + accessToken);
                        }

                    } catch (Exception e) {
                        System.out.println("[Main - main - Exception]: " + e.getMessage());
                    }
                }
            });

            server.start();

        } catch (IOException e) {
            System.out.println("[Main - main - IOException]: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[Main - main - Exception]: " + e.getMessage());
        }

        // Main m = new Main();

        /*
         * m.creazioneFattura(
         * companyId,
         * "Michele",
         * "Dibisceglia",
         * "DBSMHL02A14C514V",
         * "Via Giovanni Laterza 3",
         * "Bari",
         * "70125",
         * "BA",
         * "micheledibi2002@gmail.com",
         * "Articolo di prova",
         * "Descrizione articolo di prova",
         * new BigDecimal(0));
         * 
         */
    }

    public static void creazioneFattura(
            String accessToken,
            int companyId,
            String clienteNome,
            String clienteCognome,
            String clienteCodiceFiscale,
            String clienteIndirizzo,
            String clienteCitta,
            String clienteCAP,
            String clienteProvincia,
            String clienteEmail,
            String articoloNome,
            String articoloDescrizione,
            BigDecimal articoloPrezzo) {
        // Creazione della fattura
        IssuedDocument fattura = new IssuedDocument();

        fattura.setType(IssuedDocumentType.INVOICE);
        fattura.seteInvoice(true);

        // Creazione del cliente
        Entity cliente = new Entity();
        cliente.setName(clienteNome + " " + clienteCognome);
        cliente.setFirstName(clienteNome);
        cliente.setLastName(clienteCognome);
        cliente.setAddressCity(clienteCitta);
        cliente.setAddressPostalCode(clienteCAP);
        cliente.setAddressProvince(clienteProvincia);
        cliente.setAddressStreet(clienteIndirizzo);
        cliente.setEmail(clienteEmail);

        fattura.setEntity(cliente);

        // Creazione della lista di articoli
        IssuedDocumentItemsListItem articolo = new IssuedDocumentItemsListItem();
        articolo.setName(articoloNome);
        articolo.setDescription(articoloDescrizione);

        articolo.setNetPrice(articoloPrezzo);

        List<IssuedDocumentItemsListItem> listaArticoli = new ArrayList<>();
        listaArticoli.add(articolo);

        fattura.setItemsList(listaArticoli);

        try {
            ApiClient defaultClient = Configuration.getDefaultApiClient();
            defaultClient.setBasePath("https://api-v2.fattureincloud.it");

            OAuth OAuth2AuthenticationCodeFlow = (OAuth) defaultClient
                    .getAuthentication("OAuth2AuthenticationCodeFlow");

            OAuth2AuthenticationCodeFlow.setAccessToken(
                    accessToken);

            IssuedDocumentsApi apiInstance = new IssuedDocumentsApi(defaultClient);

            CreateIssuedDocumentRequest request = new CreateIssuedDocumentRequest();
            request.data(fattura);

            CreateIssuedDocumentResponse result = apiInstance.createIssuedDocument(companyId, request);

            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}