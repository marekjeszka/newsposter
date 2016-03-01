package com.jeszka.posters;

import com.jeszka.domain.AppCredentials;
import com.jeszka.domain.Post;
import com.jeszka.security.PasswordStore;
import org.glassfish.jersey.client.ClientConfig;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Optional;

public class WordPressPoster implements Poster {
    @Autowired
    private PasswordStore passwordStore;

    private static final String NEW_WORDPRESS_POST_FORMAT =
            "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n" +
                "<methodCall>\n" +
                "  <methodName>wp.newPost</methodName>\n" +
                "  <params>\n" +
                "   <param><value>1</value></param>\n" +
                "   <param><value>" + "%s" + "</value></param>\n" +
                "   <param><value>" + "%s" + "</value></param>\n" +
                "   <struct>\n" +
                "      <member>\n" +
                "        <name>post_title</name>\n" +
                "        <value>%s</value>\n" +
                "      </member>\n" +
                "      <member>\n" +
                "        <name>post_content</name>\n" +
                "        <value>%s</value>\n" +
                "      </member>\n" +
                "   </struct>\n" +
                "  </params>\n" +
                "</methodCall>";

    private final Client client;

    public WordPressPoster() {
        ClientConfig config = new ClientConfig();
        client = ClientBuilder.newClient(config);
    }

    WebTarget getWebTarget(String appName) {
        return client.target(getBaseURI(appName));
    }

    private static URI getBaseURI(String appName) {
        return UriBuilder.fromUri("http://" + appName + "/xmlrpc.php").build();
    }

    /**
     * Invokes POST method to create a new post.
     * @param post post to be created
     * @param masterPassword password to encode stored credentials
     */
    public boolean create(Post post, String appName, String masterPassword) {
        Optional<String> postAsString = newWordpressPost(post.getTopic(), post.getBody(), appName, masterPassword);

        if (postAsString.isPresent()) {
            Response response = getWebTarget(appName)
                    .request()
                    .post(Entity.entity(postAsString.get(), MediaType.TEXT_XML));

            System.out.println("Wordpress post creation: " + response);
            return response.getStatus() == Response.Status.OK.getStatusCode();
        } else {
            return false;
        }
    }

    @Override
    public String authorize(String email) {
        // TODO think about checking here if provided password is correct
        // no need to authorize
        return "";
    }

    @Override
    public boolean storeCredentials(AppCredentials appCredentials) {
        // TODO implement
        return true;
    }

    private Optional<String> newWordpressPost(String topic, String body, String appName, String masterPassword) {
        final Optional<AppCredentials> myApp = Optional.ofNullable(passwordStore.getCredentials(appName, masterPassword));
        return myApp.isPresent() ?
                   Optional.of(
                       String.format(NEW_WORDPRESS_POST_FORMAT,
                           myApp.get().getUsername(), myApp.get().getPassword(),
                           topic, body)) :
                   Optional.empty();
    }
}
