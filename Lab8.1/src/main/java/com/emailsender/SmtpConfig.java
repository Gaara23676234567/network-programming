package com.emailsender;

import java.util.Properties;

/**
 * Configuration container for SMTP server connection parameters.
 * Stores server info, port, credentials, and security settings.
 * Converts config data into Properties format required for mail session.
 */
public class SmtpConfig {

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final boolean useTls;
    private final boolean useAuth;

    public SmtpConfig(String host, int port, String username, String password,
                      boolean useTls, boolean useAuth) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.useTls = useTls;
        this.useAuth = useAuth;
    }

    /**
     * Converts SMTP config into Properties for JavaMail session creation.
     */
    public Properties toProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));
        props.put("mail.smtp.auth", String.valueOf(useAuth));
        if (useTls) {
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
        }
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        return props;
    }

    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public boolean isUseTls() { return useTls; }
    public boolean isUseAuth() { return useAuth; }
}
