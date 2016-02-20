package com.jeszka.posters;

import com.jeszka.domain.AppCredentials;
import com.jeszka.domain.Post;

public interface Poster {
    void create(Post post, String appName, String masterPassword);

    String authorize(String email);

    boolean storeCredentials(AppCredentials appCredentials);
}
