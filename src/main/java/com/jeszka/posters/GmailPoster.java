package com.jeszka.posters;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
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
import com.jeszka.domain.Post;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class GmailPoster implements Poster {

    private static final String APPLICATION_NAME = "GmailPoster";

    private static final File DATA_STORE_DIR = new File("credentials_gmail");

    private static final Path CLIENT_SECRET_PATH = Paths.get("client_secret");

    private static FileDataStoreFactory DATA_STORE_FACTORY;

    private static final JsonFactory JSON_FACTORY =
            JacksonFactory.getDefaultInstance();

    private static HttpTransport HTTP_TRANSPORT;

    /** Global instance of the required scopes. */
    private static final List<String> SCOPES =
            Arrays.asList(GmailScopes.GMAIL_COMPOSE, GmailScopes.GMAIL_MODIFY, GmailScopes.MAIL_GOOGLE_COM);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            System.out.println("Error initializing GmailPoster " + t.getMessage());
        }
    }

    private Gmail gmailService;

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

        final AuthorizationCodeFlow flow = new AuthorizationCodeFlow.Builder(
                BearerToken.authorizationHeaderAccessMethod(),
                HTTP_TRANSPORT,
                JSON_FACTORY,
                new GenericUrl("https://accounts.google.com/o/oauth2/token"),
                new BasicAuthentication("25524555561-kq1j6b1cdgkoe5ahru1airv1qrd026ub.apps.googleusercontent.com", "9U0_AxMLLCuVMA57xj5YU4Kb"),
                "25524555561-kq1j6b1cdgkoe5ahru1airv1qrd026ub.apps.googleusercontent.com",
                "https://accounts.google.com/o/oauth2/auth")
                .setDataStoreFactory(DATA_STORE_FACTORY) // TODO don't use file
                .build();

        return flow.newAuthorizationUrl()
                   .setRedirectUri("https://agile-plains-30447.herokuapp.com/index.html")
                   .setScopes(SCOPES)
                   .setState(email)
                   .build();
    }


//        final AuthorizationCodeTokenRequest tokenRequest =
//                flow.newTokenRequest("4/q_kgzR8Tv3tepQKXX1CemxKWQT6nBUtr9I4vkxysuFE#")
//                    .setRedirectUri("https://agile-plains-30447.herokuapp.com/index.html")
//                    .setScopes(SCOPES);
//
//
//        try {
//            final Credential user = flow.createAndStoreCredential(tokenRequest.execute(), "user");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }

    private Gmail getGmailService(String clientSecret) throws IOException {
        Credential credential = authorizeGmail(clientSecret);
        return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private Credential authorizeGmail(String clientSecret) throws IOException {
        // store client_secret file, if needed
        Files.write(CLIENT_SECRET_PATH, clientSecret.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);

        // Load client secrets
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new StringReader(clientSecret));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(DATA_STORE_FACTORY)
                        .setAccessType("offline")
                        .build();
        return new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver()).authorize("user");
    }

    @Override
    public void create(Post post, String appName, String masterPassword) {
        try {
            if (gmailService == null) {
                gmailService =
                        getGmailService(new String(Files.readAllBytes(CLIENT_SECRET_PATH), StandardCharsets.UTF_8));
            }
            createDraft(gmailService, "me", new MimeMessage(
                createEmail(appName, appName, post.getTopic(), post.getBody())));
        } catch (MessagingException | IOException e) {
            System.out.println("Error creating Gmail post " + e.getMessage());
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
}
