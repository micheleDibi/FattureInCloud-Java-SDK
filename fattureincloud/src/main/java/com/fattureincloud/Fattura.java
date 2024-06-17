package com.fattureincloud;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// import io.vavr.collection.Iterator;
import io.vavr.control.Either;

import java.io.IOException;

import java.time.LocalDate;

import it.fattureincloud.sdk.ApiClient;
import it.fattureincloud.sdk.ApiException;
import it.fattureincloud.sdk.Configuration;
import it.fattureincloud.sdk.api.InfoApi;
import it.fattureincloud.sdk.api.IssuedDocumentsApi;
import it.fattureincloud.sdk.auth.OAuth;
import it.fattureincloud.sdk.auth.OAuth2AuthorizationCodeError;
import it.fattureincloud.sdk.auth.OAuth2AuthorizationCodeManager;
import it.fattureincloud.sdk.auth.OAuth2AuthorizationCodeResponse;
import it.fattureincloud.sdk.model.CreateIssuedDocumentRequest;
import it.fattureincloud.sdk.model.CreateIssuedDocumentResponse;
import it.fattureincloud.sdk.model.Entity;
import it.fattureincloud.sdk.model.IssuedDocument;
import it.fattureincloud.sdk.model.IssuedDocumentEiData;
import it.fattureincloud.sdk.model.IssuedDocumentItemsListItem;
import it.fattureincloud.sdk.model.IssuedDocumentPaymentsListItem;
import it.fattureincloud.sdk.model.IssuedDocumentStatus;
import it.fattureincloud.sdk.model.IssuedDocumentType;
import it.fattureincloud.sdk.model.ListIssuedDocumentsResponse;
import it.fattureincloud.sdk.model.ListPaymentAccountsResponse;
import it.fattureincloud.sdk.model.PaymentAccount;

import java.util.Iterator;

public class Fattura {

    private static int companyId = 1169419; // UNIVERSO SRL
    private static String redirectURI = "http://localhost:8000";

    private static String clientId = "CYLs5KUab44D1abl3vZjdhcI9PjWRNIR";
    private static String clientSecret = "j5dGmQs7LBqTxTnC0qYBiZc805Cnc32l36gaQr7yjc1eBzEsQQQYQPMmhhnG9P15";

    private static String accessToken = "a/eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJyZWYiOiJVWkp4YmxHRmRuZ2JSM2p3NG5xRjlsNGJtUWlEMXlXTyIsImV4cCI6MTcxNzc3MTYxNX0.eq4yMI7gyndvbPXmAvRAKOdkK4xgUhCE5q6B6UTIxOo";

    private List<IssuedDocumentPaymentsListItem> elencoPagamenti = new ArrayList<>();  

    public static String refreshAccessToken(String clientId, String clientSecret, String redirectURI,
            String refreshToken) {

        OAuth2AuthorizationCodeManager oauth = new OAuth2AuthorizationCodeManager(clientId, clientSecret, redirectURI);

        try {
            Either<OAuth2AuthorizationCodeError, OAuth2AuthorizationCodeResponse> refreshedAccessToken = oauth
                    .refreshToken(refreshToken);
            if (refreshedAccessToken.isRight()) {
                OAuth2AuthorizationCodeResponse tokenObj = refreshedAccessToken.get();
                return tokenObj.getAccessToken();
            }
        } catch (IOException e) {
            System.out.println("[Main - refreshAccessToken - IOException]: " + e.getMessage());
        }

        return null;
    }

    public void aggiungiPagamento(BigDecimal importo, String date, int bancaId) {
        LocalDate localDate = LocalDate.parse(date);
        
        IssuedDocumentPaymentsListItem pagamento = new IssuedDocumentPaymentsListItem();
        pagamento.setAmount(importo);
        pagamento.setDueDate(localDate);
        pagamento.setPaidDate(localDate);
        
        pagamento.setStatus(IssuedDocumentStatus.PAID);

        // BANCA QONTO - 1277592
        // BANCA SELLA - 1277593
        PaymentAccount paymentAccount = new PaymentAccount();
        paymentAccount.setId(bancaId);

        pagamento.setPaymentAccount(paymentAccount);

        elencoPagamenti.add(pagamento);
    }

    public static void main(String[] args) {
        
        /*

        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-v2.fattureincloud.it");
        
        // Configure OAuth2 access token for authorization: OAuth2AuthenticationCodeFlow
        OAuth OAuth2AuthenticationCodeFlow = (OAuth) defaultClient.getAuthentication("OAuth2AuthenticationCodeFlow");
        OAuth2AuthenticationCodeFlow.setAccessToken(accessToken);

        InfoApi apiInstance = new InfoApi(defaultClient);
        try {
            ListPaymentAccountsResponse result = apiInstance.listPaymentAccounts(companyId, "id,name", "", "");
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling InfoApi#listPaymentAccounts");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }

        */
        
        Fattura f = new Fattura();

        f.aggiungiPagamento(new BigDecimal(100), "2024-06-01", 1277592);
        f.aggiungiPagamento(new BigDecimal(300), "2024-06-02", 1277592);
        f.aggiungiPagamento(new BigDecimal(1000), "2024-06-03", 1277592);

        String urlFattura = f.creazioneFattura(accessToken, companyId, "CP00001", "Michele", "Dibisceglia", "DBSMHL02A14C514V", "Via Giovanni Laterza 3", "Bari", "70125", "BA", "micheledibi2002@gmail.com", "CORSO DI PERFEZIONAMENTO", new BigDecimal(1400));
        
        System.out.println("Puoi trovare la tua fattura all'indirizzo url: " + urlFattura);
        
    
    }

    private static void eliminazioneFatture() {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-v2.fattureincloud.it");
        
        OAuth OAuth2AuthenticationCodeFlow = (OAuth) defaultClient.getAuthentication("OAuth2AuthenticationCodeFlow");
        OAuth2AuthenticationCodeFlow.setAccessToken(accessToken);
    
        IssuedDocumentsApi apiInstance = new IssuedDocumentsApi(defaultClient);

        boolean check = true;
        int numeroFattureEliminate = 0;

        System.out.println("Inizio eliminazione di tutte le fatture...");

        while(check == true) {

            try {
                ListIssuedDocumentsResponse result = apiInstance.listIssuedDocuments(companyId, "invoice", "id,entity", "", "", 1, 100, "", 0);
    
                List<IssuedDocument> elencoDocumenti = result.getData();
    
                check = elencoDocumenti.size() == 100;

                Iterator<IssuedDocument> elencoDocumentiIterator = elencoDocumenti.iterator();
    
                while (elencoDocumentiIterator.hasNext()) {
                    IssuedDocument document = elencoDocumentiIterator.next();
    
                    apiInstance.deleteIssuedDocument(companyId, document.getId());
                    numeroFattureEliminate++;
                }    
            } catch (ApiException e) {
                System.err.println("Exception when calling IssuedDocumentsApi#listIssuedDocuments");
                System.err.println("Status code: " + e.getCode());
                System.err.println("Reason: " + e.getResponseBody());
                System.err.println("Response headers: " + e.getResponseHeaders());
                e.printStackTrace();
            }
        }

        System.out.println("Operazione completata! Sono state eliminate " + numeroFattureEliminate + " fatture");
    }

    public String creazioneFattura(
            String accessToken,
            int companyId,
            String praticaCodice,
            String clienteNome,
            String clienteCognome,
            String clienteCodiceFiscale,
            String clienteIndirizzo,
            String clienteCitta,
            String clienteCAP,
            String clienteProvincia,
            String clienteEmail,
            String articoloNome,
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

        cliente.setTaxCode(clienteCodiceFiscale);

        fattura.setEntity(cliente);

        // Creazione della lista di articoli
        IssuedDocumentItemsListItem articolo = new IssuedDocumentItemsListItem();
        articolo.setName(articoloNome + " - Pratica Nr. " + praticaCodice);

        articolo.setNetPrice(articoloPrezzo);
        articolo.setGrossPrice(articoloPrezzo);

        List<IssuedDocumentItemsListItem> listaArticoli = new ArrayList<>();
        listaArticoli.add(articolo);

        fattura.setItemsList(listaArticoli);

        // Definizione del metodo di pagamento
        IssuedDocumentEiData issuedDocumentEiData = new IssuedDocumentEiData();
        issuedDocumentEiData.setPaymentMethod("MP05");

        fattura.setEiData(issuedDocumentEiData);

        fattura.paymentsList(elencoPagamenti);

        try {
            ApiClient defaultClient = Configuration.getDefaultApiClient();
            defaultClient.setBasePath("https://api-v2.fattureincloud.it");

            OAuth OAuth2AuthenticationCodeFlow = (OAuth) defaultClient
                    .getAuthentication("OAuth2AuthenticationCodeFlow");

            OAuth2AuthenticationCodeFlow.setAccessToken(
                    accessToken);

            IssuedDocumentsApi apiInstance = new IssuedDocumentsApi(defaultClient);

            CreateIssuedDocumentRequest request = new CreateIssuedDocumentRequest();
            request.setData(fattura);

            CreateIssuedDocumentResponse result = apiInstance.createIssuedDocument(companyId, request);

            IssuedDocument createdDocument = result.getData();
            String documentURL = createdDocument.getUrl();

            return documentURL;
        } catch (Exception e) {
            return "Exception: " + e.getMessage();
        }
    }
}