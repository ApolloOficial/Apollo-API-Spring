package org.apollo.api.recovery;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apollo.api.recovery.RecoveryDtos.OtpSendRequest;
import org.apollo.api.recovery.RecoveryDtos.OtpSentResponse;
import org.apollo.api.recovery.RecoveryDtos.OtpVerifyRequest;
import org.apollo.api.recovery.RecoveryDtos.OtpVerifyResponse;
import org.apollo.api.recovery.RecoveryDtos.PasswordResetRequest;
import org.apollo.api.recovery.RecoveryDtos.RecoveryStartRequest;
import org.apollo.api.recovery.RecoveryDtos.RecoveryStartResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/mobile/auth")
@RequiredArgsConstructor
@Tag(name = "Password recovery")
public class MobileAuthController {

    private final PasswordRecoveryService recoveryService;

    @PostMapping("/recovery")
    @Operation(summary = "Start a password recovery and list the available delivery methods")
    public RecoveryStartResponse start(@Valid @RequestBody RecoveryStartRequest request) {
        return recoveryService.start(request);
    }

    @PostMapping("/otp/send")
    @Operation(summary = "Send the verification code to the chosen method")
    public OtpSentResponse sendCode(@Valid @RequestBody OtpSendRequest request) {
        return recoveryService.sendCode(request);
    }

    @PostMapping("/otp/verify")
    @Operation(summary = "Verify the code and receive a single-use reset token")
    public OtpVerifyResponse verify(@Valid @RequestBody OtpVerifyRequest request) {
        return recoveryService.verifyCode(request);
    }

    @PostMapping("/password/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Set a new password using the reset token")
    public void reset(@Valid @RequestBody PasswordResetRequest request) {
        recoveryService.resetPassword(request);
    }
}
