package org.apollo.api.notification;

public interface PushSender {

    enum Result { SENT, INVALID_TOKEN, FAILED }

    Result send(String token, String title, String body, java.util.Map<String, String> data);
}
