package com.findmyflight.findmyflight;

import com.findmyflight.findmyflight.samplecreator.EmailNotificationReceiverCreator;
import com.findmyflight.findmyflight.samplecreator.FlightWatcherCreator;
import com.findmyflight.findmyflight.utils.TransactionHelper;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class IntegrationTest {

    @Autowired
    protected WebTestClient webTestClient;
    @Autowired
    protected TransactionHelper transactionHelper;

    @Autowired
    protected FlightWatcherCreator flightWatcherCreator;
    @Autowired
    protected EmailNotificationReceiverCreator emailNotificationReceiverCreator;
}
