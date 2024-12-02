package com.findmyflight.findmyflight.service.flightwatcher;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.Collection;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FlightWatcherMapper {
    FlightWatcher map(CreateOrUpdateFlightWatcherRequest request);

    FlightWatcherResponse map(FlightWatcher flightWatcher);

    Collection<FlightWatcherResponse> map(Collection<FlightWatcher> flightWatchers);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromRequest(CreateOrUpdateFlightWatcherRequest request, @MappingTarget FlightWatcher flightWatcher);
}
