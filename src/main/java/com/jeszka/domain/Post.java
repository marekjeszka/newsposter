package com.jeszka.domain;

public class Post {
    private String topic;
    private String body;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "Post{" +
                "topic='" + topic + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
