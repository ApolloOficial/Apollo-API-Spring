package org.apollo.api.recovery;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
@ConditionalOnProperty(name = "MAIL_PROVIDER", havingValue = "brevo")
public class BrevoRecoveryMailSender implements RecoveryMailSender {

    private final BrevoMailClient client;

    public BrevoRecoveryMailSender(
            @Value("${BREVO_API_KEY:}") String apiKey,
            @Value("${MAIL_FROM:}") String from,
            @Value("${MAIL_FROM_NAME:Apollo}") String fromName,
            @Value("${BREVO_API_URL:https://api.brevo.com/v3/smtp/email}") String apiUrl) {
        this.client = new BrevoMailClient(apiUrl, apiKey, from, fromName);
    }

    @Override
    public void sendCode(String to, String name, String code, int validMinutes) {
        String body = "Olá, " + displayName(name) + "!\n\n"
                + "Seu código para redefinir a senha do Apollo é: " + code + "\n\n"
                + "Ele vale por " + validMinutes + " minutos e só pode ser usado uma vez.\n"
                + "Se você não pediu a redefinição, ignore este e-mail: sua senha continua a mesma.\n\n"
                + "Equipe Apollo";
        client.send(to, "Seu código de recuperação de senha - Apollo", body);
    }

    @Override
    public void sendPasswordChanged(String to, String name) {
        String body = "Olá, " + displayName(name) + "!\n\n"
                + "A senha da sua conta Apollo acabou de ser alterada.\n"
                + "Se não foi você, entre em contato com o administrador da sua empresa imediatamente.\n\n"
                + "Equipe Apollo";
        client.send(to, "Sua senha do Apollo foi alterada", body);
    }

    private static String displayName(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }
        return name.trim().split("\\s+")[0];
    }
}
