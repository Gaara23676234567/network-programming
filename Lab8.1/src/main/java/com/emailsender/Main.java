package com.emailsender;

import jakarta.mail.MessagingException;

import java.util.Arrays;
import java.util.List;

/**
 * Entry point of the email sender application.
 * Reads SMTP and message settings from environment variables (or uses defaults),
 * builds SmtpConfig and EmailMessageConfig objects, prints recipients/attachments
 * to console, and calls EmailSender to dispatch the message.
 */
public class Main {

    public static void main(String[] args) {

        // ── SMTP configuration ─────────────────────────────────────────
        // Override via environment variables for production use:
        //   SMTP_HOST, SMTP_PORT, SMTP_USER, SMTP_PASS
        String smtpHost     = getEnv("SMTP_HOST", "smtp.gmail.com");
        int    smtpPort     = Integer.parseInt(getEnv("SMTP_PORT", "587"));
        String smtpUser     = getEnv("SMTP_USER", "tiktokgaara777@gmail.com");
        String smtpPass     = getEnv("SMTP_PASS", "Your password");

        SmtpConfig smtpConfig = new SmtpConfig(
                smtpHost, smtpPort, smtpUser, smtpPass,
                true,   // useTls
                true    // useAuth
        );

        // ── Message configuration ──────────────────────────────────────
        String       from        = getEnv("MAIL_FROM", smtpUser);
        List<String> recipients  = Arrays.asList(
                getEnv("MAIL_TO", "uruisolovov777@gmail.com").split(",")
        );
        String subject           = getEnv("MAIL_SUBJECT", "Hello from Java + Maven");

        String htmlContent = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; background: #f4f4f4; padding: 20px; }
                        .container { background: white; border-radius: 8px; padding: 30px;
                                     max-width: 600px; margin: auto; box-shadow: 0 2px 8px rgba(0,0,0,.1); }
                        h1  { color: #2c3e50; }
                        p   { color: #555; line-height: 1.6; }
                        .footer { margin-top: 30px; font-size: 12px; color: #aaa; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h1>Hello!</h1>
                        <p>This is an <strong>HTML email</strong> sent via Java and Jakarta Mail.</p>
                        <p>It may contain one or more file attachments.</p>
                        <div class="footer">Sent by EmailSender &mdash; Network Programming Lab 8.1</div>
                    </div>
                </body>
                </html>
                """;

        // Comma-separated file paths; empty string = no attachments
        List<String> attachments = Arrays.asList(
                getEnv("MAIL_ATTACHMENTS", "").split(",")
        );
        // Filter out empty strings (no attachments case)
        List<String> filteredAttachments = attachments.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        EmailMessageConfig messageConfig = new EmailMessageConfig(
                from, recipients, subject, htmlContent, filteredAttachments
        );

        // ── Print info ─────────────────────────────────────────────────
        System.out.println("=== Email Sender — Practical Work #8.1 ===");
        System.out.println("SMTP Host    : " + smtpHost + ":" + smtpPort);
        System.out.println("From         : " + from);
        System.out.println("Recipients   : " + recipients);
        System.out.println("Attachments  : " +
                (filteredAttachments.isEmpty() ? "(none)" : filteredAttachments));
        System.out.println("-------------------------------------------");

        // ── Send ───────────────────────────────────────────────────────
        EmailSender sender = new EmailSender(smtpConfig);
        try {
            sender.send(messageConfig);
       } catch (Exception e) {
    System.err.println("[ERROR] Failed to send email: " + e.getMessage());
            System.err.println("Check your SMTP credentials and network connection.");
            e.printStackTrace();
        }
    }

    /** Returns environment variable value or a default fallback. */
    private static String getEnv(String name, String defaultValue) {
        String value = System.getenv(name);
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }
}
