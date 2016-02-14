package com.jeszka.posters;

import com.jeszka.domain.Post;

public class GmailPoster implements Poster {
    @Override
    public boolean isAuthorized() {
        // TODO check if exists ~/.credentials/newsposter/[name]
        return false;
    }

    public boolean authorize() {
        // TODO use client_secret.json and create file in .credentials (after authorization in Gmail)
        return false;
    }

    @Override
    public void create(Post post, String appName, String masterPassword) {
        // TODO invoke GmailQuickstart stuff
    }
}
