package com.jeszka.posters;

import com.jeszka.domain.Post;

public interface Poster {
    void create(Post post, String appName, String masterPassword);

    String authorize(String email);
}
