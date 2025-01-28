package com.findmyflight.findmyflight.service.emailnotificationreceiver;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EmailNotificationReceiverRepository extends JpaRepository<EmailNotificationReceiver, Long> {
    @Query("select e from EmailNotificationReceiver e join fetch e.flightWatchers fw where fw.suspended = false")
    List<EmailNotificationReceiver> findEmailNotificationReceiversWithActiveFlightWatchers();

    @Query("select e from EmailNotificationReceiver e  where e.address = ?1")
    EmailNotificationReceiver findEmailNotificationReceiverByAddress(String address);
}