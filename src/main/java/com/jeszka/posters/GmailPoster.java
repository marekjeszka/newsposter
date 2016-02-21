package com.jeszka.posters;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.store.FileDataStoreFactory;
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
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class GmailPoster implements Poster {

    @Autowired
    PasswordStore passwordStore;

    private static final String APPLICATION_NAME = "GmailPoster";

    private static final File DATA_STORE_DIR = new File("credentials_gmail");

    private static FileDataStoreFactory DATA_STORE_FACTORY;

    private static final JsonFactory JSON_FACTORY =
            JacksonFactory.getDefaultInstance();

    private static HttpTransport HTTP_TRANSPORT;

    static final String REDIRECT_URL = "https://agile-plains-30447.herokuapp.com/googleAuthorized.html";
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
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
            System.getenv(GMAIL_CLIENT_ID);
            System.getenv(GMAIL_CLIENT_SECRET);
        } catch (Throwable t) {
            System.out.println("Error initializing GmailPoster " + t.getMessage());
        }
    }

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
                .setDataStoreFactory(DATA_STORE_FACTORY) // TODO don't in memory data store
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
