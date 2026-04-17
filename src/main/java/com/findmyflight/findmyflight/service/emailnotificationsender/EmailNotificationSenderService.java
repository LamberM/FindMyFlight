package com.findmyflight.findmyflight.service.emailnotificationsender;

import com.findmyflight.findmyflight.service.emailnotificationreceiver.EmailNotificationReceiver;
import com.findmyflight.findmyflight.service.emailnotificationreceiver.EmailNotificationReceiverRepository;
import com.findmyflight.findmyflight.service.error.handler.EmailSendingException;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
public class EmailNotificationSenderService {
    public static final String EMAIL_TO_SUBSCRIBERS_TEMPLATE = "email-to-subscribers-template";
    public static final String EMAIL_FORGOT_PASSWORD_TEMPLATE = "email-forgot-password-template";
    public static final String YOUR_OBSERVED_FLIGHTS_IN_SKY_DEAL_HUNTER = "Your observed flights in SkyDealHunter";
    public static final String PASSWORD_RECOVERY_IN_SKY_DEAL_HUNTER = "Password recovery in SkyDealHunter";
    private final JavaMailSender mailSender;
    private final EmailNotificationReceiverRepository emailNotificationReceiverRepository;
    private final SpringTemplateEngine springTemplateEngine;

    @Scheduled(cron = "${email.notification.sender.cron}")
    public void sendMailToSubscribers() {
        var emailNotificationReceivers = emailNotificationReceiverRepository.findEmailNotificationReceiversWithActiveFlightWatchers();
        for (EmailNotificationReceiver emailNotificationReceiver : emailNotificationReceivers) {
            var context = new Context();
            context.setVariable("flightWatchers", emailNotificationReceiver.getFlightWatchers());
            sendEmail(emailNotificationReceiver.getAddress(), EMAIL_TO_SUBSCRIBERS_TEMPLATE, context, YOUR_OBSERVED_FLIGHTS_IN_SKY_DEAL_HUNTER);
        }
    }

    public void sendForgotPasswordMail(String email, String link) {
        var context = new Context();
        context.setVariable("link", link);
        sendEmail(email, EMAIL_FORGOT_PASSWORD_TEMPLATE, context, PASSWORD_RECOVERY_IN_SKY_DEAL_HUNTER);
    }

    private void sendEmail(String recipient, String templateName, Context context, String subject) {
        var htmlContent = springTemplateEngine.process(templateName, context);
        var message = mailSender.createMimeMessage();
        try {
            var helper = new MimeMessageHelper(message, true);
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            throw new EmailSendingException("Failed to send email. Please try again later.", e);
        }
    }
}