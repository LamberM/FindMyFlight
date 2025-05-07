package com.findmyflight.findmyflight.service.emailnotificationsender;

import com.findmyflight.findmyflight.service.emailnotificationreceiver.EmailNotificationReceiver;
import com.findmyflight.findmyflight.service.emailnotificationreceiver.EmailNotificationReceiverRepository;
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
    private final JavaMailSender mailSender;
    private final EmailNotificationReceiverRepository emailNotificationReceiverRepository;
    private final SpringTemplateEngine springTemplateEngine;

    @Scheduled(cron = "${email.notification.sender.cron}")
    public void sendMailToSubscribers() throws MessagingException {
        var context = new Context();
        var emailNotificationReceivers = emailNotificationReceiverRepository.findEmailNotificationReceiversWithActiveFlightWatchers();
        for (EmailNotificationReceiver emailNotificationReceiver : emailNotificationReceivers) {
            context.setVariable("flightWatchers", emailNotificationReceiver.getFlightWatchers());
            var htmlContent = springTemplateEngine.process("email-template", context);
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true);
            helper.setTo(emailNotificationReceiver.getAddress());
            helper.setSubject("Your observed flights in SkyDealHunter");
            helper.setText(htmlContent, true);
            mailSender.send(helper.getMimeMessage());
        }
    }
}