package com.jeszka.posters;

import com.google.api.client.auth.oauth2.*;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Draft;
import com.google.api.services.gmail.model.Message;
import com.jeszka.domain.AppCredentials;
import com.jeszka.domain.Post;
import com.jeszka.security.PasswordStore;
import org.springframework.beans.factory.annotation.Autowired;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class GmailPoster implements Poster {

    @Autowired
    PasswordStore passwordStore;

    @Autowired
    DataStoreFactory dataStoreFactory;

    private static final String APPLICATION_NAME = "GmailPoster";

    private static final JsonFactory JSON_FACTORY =
            JacksonFactory.getDefaultInstance();

    private static HttpTransport HTTP_TRANSPORT;

    static final String REDIRECT_URL = "https://safe-woodland-63868.herokuapp.com/googleAuthorized.html";  // TODO dynamically
    static final String TOKEN_ADDRESS = "https://accounts.google.com/o/oauth2/token";
    static final String AUTH_ADDRESS = "https://accounts.google.com/o/oauth2/auth";
    static final String GMAIL_CLIENT_ID = "GMAIL_CLIENT_ID";
    static final String GMAIL_CLIENT_SECRET = "GMAIL_CLIENT_SECRET";
    static final String GRANT_TYPE = "authorization_code";

    static final List<String> SCOPES =
            Collections.singletonList(GmailScopes.GMAIL_COMPOSE);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Throwable t) {
            System.out.println("Error initializing GmailPoster " + t.getMessage());
        }
    }

    // TODO how to handle multiple google accounts? gmail service creation each time from scratch?
    private Gmail gmailService;
    private AuthorizationCodeFlow flow;

    @Override
    public String authorize(String email) {
        try {
            return getAuthorizationUrl(email);
        } catch (IOException e) {
            System.out.println("Error authorizing GmailPoster " + e.getMessage());
        }
        return null;
    }

    /**
     * https://developers.google.com/api-client-library/java/google-oauth-java-client/oauth2#credential_and_credential_store
     */
    private String getAuthorizationUrl(String email) throws IOException {

        flow = new AuthorizationCodeFlow.Builder(
                BearerToken.authorizationHeaderAccessMethod(),
                HTTP_TRANSPORT,
                JSON_FACTORY,
                new GenericUrl(TOKEN_ADDRESS),
                new BasicAuthentication(System.getenv(GMAIL_CLIENT_ID), System.getenv(GMAIL_CLIENT_SECRET)),
                System.getenv(GMAIL_CLIENT_ID),
                AUTH_ADDRESS)
                .setDataStoreFactory(dataStoreFactory)
                .build();

        return flow.newAuthorizationUrl()
                   .setRedirectUri(REDIRECT_URL)
                   .setScopes(SCOPES)
                   .setState(email)
                   .build();
    }

    @Override
    public boolean storeCredentials(AppCredentials appCredentials) {

        if (getFlow() == null) {
            System.out.println("Can't store credentials, not yet authorized");
            return false;
        }

        final AuthorizationCodeTokenRequest tokenRequest =
                getFlow().newTokenRequest(appCredentials.getPassword())
                         .setGrantType(GRANT_TYPE)
                         .setRedirectUri(REDIRECT_URL)
                         .setScopes(SCOPES);

        try {
            final Credential credential =
                    getFlow().createAndStoreCredential(tokenRequest.execute(), appCredentials.getAppName());
            gmailService = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            return passwordStore.storeCredentials(appCredentials.getAppName(), "", "");

        } catch (IOException e) {
            System.out.println("Error creating Gmail credential " + e);
            return false;
        }
    }

    @Override
    public void create(Post post, String appName, String masterPassword) {
        try {
            if (gmailService == null) {
                initGmailFromStoredCredential(appName);
            }

            if (gmailService != null) {
                createDraft(gmailService, "me", new MimeMessage(
                    createEmail(appName, appName, post.getTopic(), post.getBody())));
            }
            else {
                System.out.println("Cannot create e-mail, Gmail not yet authorized");
            }
        } catch (MessagingException | IOException e) {
            System.out.println("Error creating Gmail e-mail " + e.getMessage());
        }
    }

    private void initGmailFromStoredCredential(String appName) throws IOException {
        // TODO handle this during initialization
        final DataStore<Serializable> dataStore = dataStoreFactory.getDataStore(StoredCredential.DEFAULT_DATA_STORE_ID);
        if (dataStore.containsKey(appName)) {
            final StoredCredential storedCredential = (StoredCredential) dataStore.get(appName);
            GoogleCredential credential = new GoogleCredential.Builder()
                    .setTransport(HTTP_TRANSPORT)
                    .setJsonFactory(JSON_FACTORY)
                    .setClientSecrets(System.getenv(GMAIL_CLIENT_ID), System.getenv(GMAIL_CLIENT_SECRET)).build();
            credential.setAccessToken(storedCredential.getAccessToken());
            gmailService = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        }
    }

    private Draft createDraft(Gmail service, String userId, MimeMessage email)
            throws MessagingException, IOException {
        Message message = createMessageWithEmail(email);
        Draft draft = new Draft();
        draft.setMessage(message);
        draft = service.users().drafts().create(userId, draft).execute();

        System.out.println("draft id: " + draft.getId());
        System.out.println(draft.toPrettyString());
        return draft;
    }

    private Message createMessageWithEmail(MimeMessage email) throws IOException, MessagingException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        email.writeTo(outputStream);
        String encodedEmail = Base64.encodeBase64URLSafeString(outputStream.toByteArray());
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }

    private MimeMessage createEmail(String to, String from, String subject, String bodyText) throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO,
                new InternetAddress(to));
        email.setSubject(subject);
        email.setText(bodyText);
        return email;
    }

    AuthorizationCodeFlow getFlow() {
        return flow;
    }
}
