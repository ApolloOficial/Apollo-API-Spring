package org.apollo.api.recovery;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SmtpRecoveryMailSender implements RecoveryMailSender {

    private final JavaMailSender mailSender;
    private final String username;
    private final String from;

    public SmtpRecoveryMailSender(
            JavaMailSender mailSender,
            @Value("${MAIL_USERNAME:}") String username,
            @Value("${MAIL_FROM:}") String from) {
        this.mailSender = mailSender;
        this.username = username;
        this.from = from == null || from.isBlank() ? username : from;
    }

    @Override
    public void sendCode(String to, String name, String code, int validMinutes) {
        String body = "Olá, " + displayName(name) + "!\n\n"
                + "Seu código para redefinir a senha do Apollo é: " + code + "\n\n"
                + "Ele vale por " + validMinutes + " minutos e só pode ser usado uma vez.\n"
                + "Se você não pediu a redefinição, ignore este e-mail: sua senha continua a mesma.\n\n"
                + "Equipe Apollo";
        send(to, "Seu código de recuperação de senha - Apollo", body);
    }

    @Override
    public void sendPasswordChanged(String to, String name) {
        String body = "Olá, " + displayName(name) + "!\n\n"
                + "A senha da sua conta Apollo acabou de ser alterada.\n"
                + "Se não foi você, entre em contato com o administrador da sua empresa imediatamente.\n\n"
                + "Equipe Apollo";
        send(to, "Sua senha do Apollo foi alterada", body);
    }

    private void send(String to, String subject, String body) {
        if (username == null || username.isBlank() || from == null || from.isBlank()) {
            throw new IllegalStateException("Mail is not configured");
        }
        if (mailSender instanceof JavaMailSenderImpl impl
                && (impl.getPassword() == null || impl.getPassword().isBlank())) {
            throw new IllegalStateException("Mail is not configured");
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    private static String displayName(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }
        return name.trim().split("\\s+")[0];
    }
}
