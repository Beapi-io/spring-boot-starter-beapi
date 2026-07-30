package io.beapi.api.service


import io.beapi.api.domain.User;
import io.beapi.api.properties.ApiProperties;

import java.io.File;
import java.io.IOException;
import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory

/**
 *
 * This class provides basic methods for calltype=1 (ie endpoints requested like '/v0.1/')
 * @author Owen Rubel
 *
 * @see ApiExchange
 *
 */
public class MailService {

    @Autowired
    ApiProperties apiProperties;

    @Autowired
    private JavaMailSender mailSender;

    private final Logger log = LoggerFactory.getLogger(MailService.class);

    public MailService() {}

   void sendVerificationEmail(User user,String url) {
        try {
            //String siteURL = apiProperties.getApiServer()
String content = """Dear ${user.getUsername()},
Please click the link below to verify your registration:
${url}?id=${user.getVerificationCode()}

Thank you,
Your company name."""

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("orubel@beapi.io");
            message.setTo(user.email);
            message.setSubject("Registration Email");
            message.setText(content);
            mailSender.send(message);
        }catch(Exception e){
            if (log.isDebugEnabled()) {
                throw new Exception("Email could not be sent to user ", e);
            } else {
                println("Email could not be sent to user "+ e.getMessage());
            }
        }
    }

}