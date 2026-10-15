package org.apollo.api.notification;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

/** So existe quando ha credencial do Firebase (FIREBASE_CREDENTIALS_B64 ou FIREBASE_CREDENTIALS_FILE). */
@Slf4j
@Component
@ConditionalOnExpression("!'${app.firebase.credentials-b64:}'.isEmpty() or !'${app.firebase.credentials-file:}'.isEmpty()")
public class FcmPushSender implements PushSender {

    private final FirebaseMessaging messaging;

    public FcmPushSender(@Value("${app.firebase.credentials-b64:}") String b64,
                         @Value("${app.firebase.credentials-file:}") String file) throws IOException {
        try (InputStream in = b64.isBlank() ? new FileInputStream(file)
                : new ByteArrayInputStream(Base64.getDecoder().decode(b64.trim()))) {
            FirebaseOptions options = FirebaseOptions.builder().setCredentials(GoogleCredentials.fromStream(in)).build();
            FirebaseApp app = FirebaseApp.getApps().isEmpty() ? FirebaseApp.initializeApp(options) : FirebaseApp.getInstance();
            this.messaging = FirebaseMessaging.getInstance(app);
        }
        log.info("FCM push enabled");
    }

    @Override
    public Result send(String token, String title, String body, Map<String, String> data) {
        try {
            Message.Builder message = Message.builder().setToken(token)
                    .setNotification(Notification.builder().setTitle(title).setBody(body).build());
            data.forEach(message::putData);
            messaging.send(message.build());
            return Result.SENT;
        } catch (FirebaseMessagingException exception) {
            MessagingErrorCode code = exception.getMessagingErrorCode();
            if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT) {
                return Result.INVALID_TOKEN;
            }
            log.warn("FCM send failed: {}", exception.getMessage());
            return Result.FAILED;
        }
    }
}
