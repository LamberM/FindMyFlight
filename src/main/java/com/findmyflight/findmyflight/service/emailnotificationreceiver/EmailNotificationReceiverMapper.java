package com.findmyflight.findmyflight.service.emailnotificationreceiver;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.Optional;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface EmailNotificationReceiverMapper {
    EmailNotificationReceiver map(EmailNotificationReceiverDto emailNotificationReceiverDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEmailNotificationReceiverFromDto(EmailNotificationReceiverDto emailNotificationReceiverDto, @MappingTarget EmailNotificationReceiver emailNotificationReceiver);
}
