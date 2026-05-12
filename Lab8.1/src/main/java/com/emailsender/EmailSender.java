package com.emailsender;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.File;
import java.util.Properties;

/**
 * Handles email sending logic.
 * Creates mail session with SMTP settings, builds HTML message,
 * adds recipients and attachments, then sends via Transport.send().
 */
public class EmailSender {

    private final SmtpConfig smtpConfig;

    public EmailSender(SmtpConfig smtpConfig) {
        this.smtpConfig = smtpConfig;
    }

    /**
     * Sends an HTML email with optional attachments to all specified recipients.
     *
     * @param messageConfig email message configuration
     * @throws MessagingException if sending fails
     */
    public void send(EmailMessageConfig messageConfig) throws Exception { 
        Properties props = smtpConfig.toProperties();

        // Create mail session with authenticator
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        smtpConfig.getUsername(),
                        smtpConfig.getPassword()
                );
            }
        });

        // Build the message
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(messageConfig.getFrom()));
        message.setSubject(messageConfig.getSubject(), "UTF-8");

        // Add all recipients
        for (String recipient : messageConfig.getRecipients()) {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
        }

        // Create multipart body (HTML + attachments)
        Multipart multipart = new MimeMultipart();

        // Part 1: HTML content
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(messageConfig.getHtmlContent(), "text/html; charset=UTF-8");
        multipart.addBodyPart(htmlPart);

        // Part 2+: Attachments (if any)
        for (String filePath : messageConfig.getAttachmentPaths()) {
            File file = new File(filePath);
            if (!file.exists()) {
                System.err.println("[WARNING] Attachment not found, skipping: " + filePath);
                continue;
            }
            MimeBodyPart attachPart = new MimeBodyPart();
            DataSource source = new FileDataSource(file);
            attachPart.setDataHandler(new DataHandler(source));
            attachPart.setFileName(MimeUtility.encodeText(file.getName(), "UTF-8", null));
            multipart.addBodyPart(attachPart);
            System.out.println("[INFO] Attached: " + file.getName());
        }

        message.setContent(multipart);

        // Send
        Transport.send(message);
        System.out.println("[SUCCESS] Email sent to " + messageConfig.getRecipients().size()
                + " recipient(s).");
    }
}
