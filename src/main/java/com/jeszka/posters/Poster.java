package com.jeszka.posters;

import com.jeszka.domain.Post;

public interface Poster {
    boolean isAuthorized();

    void create(Post post);
}
