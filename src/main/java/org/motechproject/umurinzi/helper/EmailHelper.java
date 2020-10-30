package org.motechproject.umurinzi.helper;

import java.util.List;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EmailHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailHelper.class);

    public void sendNewMessage(String host, final String user, final String password, Integer port, String subject, //NO CHECKSTYLE ParameterNumber
                                  List<String> recipients, String content, DataSource source, String fileName) {
        if (StringUtils.isBlank(host)) {
            throw new IllegalArgumentException("Email host is empty");
        }

        if (StringUtils.isBlank(user) || StringUtils.isBlank(password)) {
            throw new IllegalArgumentException("Sender name or password is empty");
        }

        if (recipients == null || recipients.isEmpty()) {
            throw new IllegalArgumentException("Recipients list is empty");
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(user, password);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user));
            message.setSubject(subject);

            for (String to : recipients) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            }

            Multipart multipart = new MimeMultipart();
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(content);
            multipart.addBodyPart(messageBodyPart);

            if (source != null) {
                messageBodyPart = new MimeBodyPart();
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName(fileName);
                multipart.addBodyPart(messageBodyPart);
            }

            message.setContent(multipart);
            Transport.send(message);
        } catch (MessagingException e) {
            LOGGER.error("Exception occurred when sending email: " + e.getMessage(), e);
        }
    }
}
