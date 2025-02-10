package com.findmyflight.findmyflight;

import com.findmyflight.findmyflight.samplecreator.EmailNotificationReceiverCreator;
import com.findmyflight.findmyflight.samplecreator.FlightResultCreator;
import com.findmyflight.findmyflight.samplecreator.FlightWatcherCreator;
import com.findmyflight.findmyflight.utils.TextConverter;
import com.findmyflight.findmyflight.utils.TransactionHelper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@ActiveProfiles("test")
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
public abstract class IntegrationTest {

    @Autowired
    protected WebTestClient webTestClient;
    @Autowired
    protected TransactionHelper transactionHelper;
    @Autowired
    protected TextConverter textConverter;

    @Autowired
    protected FlightWatcherCreator flightWatcherCreator;
    @Autowired
    protected EmailNotificationReceiverCreator emailNotificationReceiverCreator;
    @Autowired
    protected FlightResultCreator flightResultCreator;
}
