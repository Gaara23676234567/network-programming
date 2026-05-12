package com.emailsender;

import java.util.Collections;
import java.util.List;

/**
 * Configuration container for email message data.
 * Stores sender, recipients list, subject, HTML content, and attachment paths.
 * All lists are made immutable after object creation.
 */
public class EmailMessageConfig {

    private final String from;
    private final List<String> recipients;
    private final String subject;
    private final String htmlContent;
    private final List<String> attachmentPaths;

    public EmailMessageConfig(String from,
                              List<String> recipients,
                              String subject,
                              String htmlContent,
                              List<String> attachmentPaths) {
        this.from = from;
        this.recipients = Collections.unmodifiableList(recipients);
        this.subject = subject;
        this.htmlContent = htmlContent;
        this.attachmentPaths = Collections.unmodifiableList(attachmentPaths);
    }

    public String getFrom() { return from; }
    public List<String> getRecipients() { return recipients; }
    public String getSubject() { return subject; }
    public String getHtmlContent() { return htmlContent; }
    public List<String> getAttachmentPaths() { return attachmentPaths; }
}
