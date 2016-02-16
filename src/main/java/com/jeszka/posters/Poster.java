package com.jeszka.posters;

import com.jeszka.domain.Post;

public interface Poster<T> {
    void create(Post post, String appName, String masterPassword);

    boolean authorize(String authObject);
}
