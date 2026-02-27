package vn.io.oldmoon.shopizer.user.infra.data;

public final class EmailTemplate {
    public static final String RESET_PASSWORD_SUBJECT = "Reset Password";
    public static final String EMAIL_VERIFICATION_SUBJECT = "Email Verification";
    public static final String PASSWORD_CHANGED_SUBJECT =
            "Security Alert: Your Password Was Changed";

    public static String resetPassword(String fullName, String code) {
        return """
                Hello %s,
                
                We received a request to reset your password.
                
                Your reset code is:
                %s
                
                This code will expire in 1 minute.
                
                If you did not request a password reset, you can safely ignore this email.
                
                Best regards,
                Hogwarts Support
                """.formatted(fullName, code);
    }

    public static String passwordChanged(String fullName) {
        return """
                Hello %s,
                
                This is a confirmation that your password was successfully changed.
                
                If you did NOT make this change, please contact our support team immediately
                and secure your account as soon as possible.
                
                Best regards,
                Hogwarts Support Team
                """.formatted(fullName);
    }

    public static String verifyEmail(String fullName, String code) {
        return """
                Hello %s. Welcome to Hogwarts Forum!
                
                To complete your registration, please verify your email address
                using the code below:
                
                %s
                
                This code will expire in 5 minutes.
                
                If you did not create an account, please ignore this email.
                
                Thanks for joining us,
                Hogwarts Team
                """.formatted(fullName, code);
    }

    public static String greeting(String appName, String username) {
        return """
                Hi %s,
                
                Welcome to %s! 🎉
                
                Your account is ready to use.
                We’re excited to have you on board.
                
                Best regards,
                %s Team
                """.formatted(username, appName, appName);
    }
}


