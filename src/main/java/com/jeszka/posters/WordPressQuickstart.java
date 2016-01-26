package com.jeszka.posters;

import com.jeszka.domain.Post;
import org.glassfish.jersey.client.ClientConfig;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class WordPressQuickstart {
    private static final String BODY =
            "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n" +
                    "<methodCall>\n" +
                    "  <methodName>wp.getPost</methodName>\n" +
                    "  <params>\n" +
                    "   <param><value>1</value></param>\n" +
                    "   <param><value>jeszkam</value></param>\n" +
                    "   <param><value>" + System.getenv("NEWSPOSTER_WORDPRESS_PASSWORD") + "</value></param>\n" +
                    "   <param><value>20</value></param>\n" +
                    "  </params>\n" +
                    "</methodCall>";

    public static void main(String[] args) {
        ClientConfig config = new ClientConfig();

        Client client = ClientBuilder.newClient(config);

        WebTarget target = client.target(getBaseURI());

        String response = target
                .request()
                .post(Entity.entity(BODY, MediaType.TEXT_XML))
                .toString();

        System.out.println(response);
    }

    private static URI getBaseURI() {
        return UriBuilder.fromUri("http://localhost:8080/wordpress/xmlrpc.php").build();
    }

    public void create(Post post) {

    }
}
