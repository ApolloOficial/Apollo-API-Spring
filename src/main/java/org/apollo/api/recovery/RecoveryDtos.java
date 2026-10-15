package org.apollo.api.recovery;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public final class RecoveryDtos {

    private RecoveryDtos() {
    }

    public record RecoveryStartRequest(
            @NotBlank(message = "Email is required")
            @Size(max = 120, message = "Email is too long")
            String email) {
    }

    public record RecoveryMethod(String method, String destination) {
    }

    public record RecoveryStartResponse(String challengeId, List<RecoveryMethod> methods) {
    }

    public record OtpSendRequest(
            @NotBlank(message = "Challenge is required") String challengeId,
            @NotBlank(message = "Method is required") String method) {
    }

    public record OtpSentResponse(String destination, int resendInSeconds, int expiresInSeconds) {
    }

    public record OtpVerifyRequest(
            @NotBlank(message = "Challenge is required") String challengeId,
            @NotBlank(message = "Code is required")
            @Size(max = 12, message = "Code is too long")
            String code) {
    }

    public record OtpVerifyResponse(String resetToken) {
    }

    public record PasswordResetRequest(
            @NotBlank(message = "Reset token is required") String resetToken,
            @NotBlank(message = "New password is required")
            @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,72}$",
                    message = "Password must have 8 to 72 characters with uppercase, lowercase, digit and special character")
            String newPassword) {
    }
}
