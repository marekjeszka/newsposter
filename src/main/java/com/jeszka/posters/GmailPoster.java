package com.jeszka.posters;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
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
import org.springframework.core.env.Environment;

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
    Environment env;

    @Autowired
    PasswordStore passwordStore;

    @Autowired
    DataStoreFactory dataStoreFactory;

    private static final String APPLICATION_NAME = "GmailPoster";

    private static final JsonFactory JSON_FACTORY =
            JacksonFactory.getDefaultInstance();

    private static HttpTransport HTTP_TRANSPORT;

    static final String REDIRECT_URL = "https://safe-woodland-63868.herokuapp.com/googleAuthorized.html";  // TODO dynamically
    static final String GMAIL_CLIENT_ID = "GMAIL_CLIENT_ID";
    static final String GMAIL_CLIENT_SECRET = "GMAIL_CLIENT_SECRET";
    static final String GRANT_TYPE = "authorization_code";
    static final String ACCESS_TYPE = "offline";

    static final List<String> SCOPES =
            Collections.singletonList(GmailScopes.GMAIL_COMPOSE);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Throwable t) {
            System.out.println("Error initializing GmailPoster " + t.getMessage());
        }
    }

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

        flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT,
                JSON_FACTORY,
                System.getenv(GMAIL_CLIENT_ID),
                System.getenv(GMAIL_CLIENT_SECRET),
                SCOPES)
                .setDataStoreFactory(dataStoreFactory)
                .build();

        final GoogleAuthorizationCodeRequestUrl authorizationCodeRequestUrl =
                (GoogleAuthorizationCodeRequestUrl) flow.newAuthorizationUrl();
        return authorizationCodeRequestUrl
                .setRedirectUri(REDIRECT_URL)
                .setScopes(SCOPES)
                .setState(email)
                .setAccessType(ACCESS_TYPE) // to receive refresh token
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

            getFlow().createAndStoreCredential(tokenRequest.execute(), appCredentials.getAppName());
            return passwordStore.storeCredentials(appCredentials.getAppName(), "", "");

        } catch (IOException e) {
            System.out.println("Error creating Gmail credential " + e);
            return false;
        }
    }

    @Override
    public boolean create(Post post, String appName, String masterPassword) {
        try {
            Gmail gmailService = initGmailFromStoredCredential(appName);
            if (gmailService == null) {
                System.out.println("Cannot create e-mail, Gmail not yet authorized");
            }
            else  {
                final Draft draft = createDraft(gmailService, new MimeMessage(
                        createEmail(appName, appName, post.getTopic(), post.getBody())));
                return draft != null;
            }
        } catch (MessagingException | IOException e) {
            System.out.println("Error creating Gmail e-mail " + e.getMessage());
        }
        return false;
    }

    private Gmail initGmailFromStoredCredential(String appName) throws IOException {
        final DataStore<Serializable> dataStore = dataStoreFactory.getDataStore(StoredCredential.DEFAULT_DATA_STORE_ID);
        if (dataStore.containsKey(appName)) {
            final StoredCredential storedCredential = (StoredCredential) dataStore.get(appName);

            // offline mode - expiration date ignored

            GoogleCredential credential = new GoogleCredential.Builder()
                    .setTransport(HTTP_TRANSPORT)
                    .setJsonFactory(JSON_FACTORY)
                    .setClientSecrets(env.getProperty(GMAIL_CLIENT_ID), env.getProperty(GMAIL_CLIENT_SECRET))
                    .build()
                    .setAccessToken(storedCredential.getAccessToken())
                    .setRefreshToken(storedCredential.getRefreshToken());

            return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        }
        return null;
    }

    private Draft createDraft(Gmail service, MimeMessage email)
            throws MessagingException, IOException {
        Message message = createMessageWithEmail(email);
        Draft draft = new Draft();
        draft.setMessage(message);
        draft = service.users()
                       .drafts()
                       .create("me", draft)
                       .execute();

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
