package com.findmyflight.findmyflight.service.emailnotificationsender;

import com.findmyflight.findmyflight.service.flightwatcher.FlightWatcher;
import com.findmyflight.findmyflight.service.flightwatcher.FlightWatcherRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
public class EmailNotificationSenderService {
    private final JavaMailSender mailSender;
    private final FlightWatcherRepository flightWatcherRepository;
    private final SpringTemplateEngine springTemplateEngine;

    public void sendMailToSubscribers() throws MessagingException {
        var context = new Context();
        var flightWatchers = flightWatcherRepository.findSuspendedFlightWatchers();
        for (FlightWatcher flightWatcher : flightWatchers) {
            context.setVariable("flightWatchers", flightWatchers);
            var htmlContent = springTemplateEngine.process("email-template", context);
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true);
            helper.setTo(flightWatcher.getEmailNotificationReceiver().getAddress());
            helper.setSubject("Your observed flights in SkyDealHunter");
            helper.setText(htmlContent, true);
            mailSender.send(helper.getMimeMessage());
        }
    }
}
