package com.findmyflight.findmyflight.service.emailnotificationreceiver;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface EmailNotificationReceiverRepository extends JpaRepository<EmailNotificationReceiver,Long> {
    EmailNotificationReceiver findByAddress(@Param("address") String address);
    boolean existsByAddress(@Param("address") String address);
}
