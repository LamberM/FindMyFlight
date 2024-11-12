package com.findmyflight.findmyflight.service.emailnotificationreceiver;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.Collection;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface EmailNotificationReceiverMapper {
    EmailNotificationReceiver map(CreateOrUpdateEmailNotificationReceiverRequest createOrUpdateEmailNotificationReceiverRequest);

    EmailNotificationReceiverResponse map(EmailNotificationReceiver emailNotificationReceiver);

    Collection<EmailNotificationReceiverResponse> map (Collection<EmailNotificationReceiver> emailNotificationReceiverCollection);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromRequest(CreateOrUpdateEmailNotificationReceiverRequest createOrUpdateEmailNotificationReceiverRequest, @MappingTarget EmailNotificationReceiver emailNotificationReceiver);
}
