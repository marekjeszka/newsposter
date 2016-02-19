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
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

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

    private final WebTarget target;

    public WordPressPoster() {
        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);

        target = client.target(getBaseURI());
    }

    private static URI getBaseURI() {
        return UriBuilder.fromUri("http://localhost:8080/wordpress/xmlrpc.php").build();
    }

    /**
     * Invokes POST method to create a new post.
     * @param post post to be created
     * @param masterPassword password to encode credentials stored in file
     */
    public void create(Post post, String appName, String masterPassword) {
        String postAsString = newWordpressPost(post.getTopic(), post.getBody(), appName, masterPassword);

        String response = target
                .request()
                .post(Entity.entity(postAsString, MediaType.TEXT_XML))
                .toString();

        System.out.println(response);
    }

    @Override
    public String authorize(String email) {
        // no need to authorize
        return "";
    }

    @Override
    public void storeCredentials(AppCredentials appCredentials) {
        // TODO implement
    }

    private String newWordpressPost(String topic, String body, String appName, String masterPassword) {
        final AppCredentials myApp = passwordStore.getCredentials(appName, masterPassword);
        return String.format(NEW_WORDPRESS_POST_FORMAT,
                myApp.getUsername(), myApp.getPassword(),
                topic, body);
    }
}
