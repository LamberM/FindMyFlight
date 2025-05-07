package com.findmyflight.findmyflight.service.emailnotificationreceiver;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EmailNotificationReceiverRepository extends JpaRepository<EmailNotificationReceiver, Long> {
    @Query("select e from EmailNotificationReceiver e " +
            "join fetch e.flightWatchers fw " +
            "join fetch fw.flightResult fr " +
            "where fw.suspended = false and fr is not null")
    List<EmailNotificationReceiver> findEmailNotificationReceiversWithActiveFlightWatchers();
}