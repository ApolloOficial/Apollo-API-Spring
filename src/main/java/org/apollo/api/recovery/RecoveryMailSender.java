package org.apollo.api.recovery;

public interface RecoveryMailSender {

    void sendCode(String to, String name, String code, int validMinutes);

    void sendPasswordChanged(String to, String name);
}
