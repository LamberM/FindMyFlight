package com.findmyflight.findmyflight.service.flightwatcher;

import com.findmyflight.findmyflight.service.emailnotificationreceiver.EmailNotificationReceiverService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class FlightWatcherService {
    private final FlightWatcherRepository flightWatcherRepository;
    private final FlightWatcherMapper mapper;
    private final EmailNotificationReceiverService emailNotificationReceiverService;

    @Transactional
    public FlightWatcherResponse create(CreateOrUpdateFlightWatcherRequest request) {
        var flightWatcher = mapper.map(request);

        var emailNotificationReceiver = emailNotificationReceiverService.findEntityById(request.emailNotificationReceiverId())
                .orElseThrow(() -> new EntityNotFoundException(String.format("Email notification receiver with ID %s not found!", request.emailNotificationReceiverId())));
        flightWatcher.setEmailNotificationReceiver(emailNotificationReceiver);

        flightWatcherRepository.save(flightWatcher);
        return mapper.map(flightWatcher);
    }

    @Transactional(readOnly = true)
    public Collection<FlightWatcherResponse> findAll() {
        return mapper.map(flightWatcherRepository.findAll());
    }

    @Transactional(readOnly = true)
    public FlightWatcherResponse findById(Long id) {
        return mapper.map(flightWatcherRepository.findById(id).orElseThrow(EntityNotFoundException::new));
    }

    @Transactional
    public FlightWatcherResponse update(Long id, CreateOrUpdateFlightWatcherRequest request) {
        var flightWatcher = flightWatcherRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        mapper.updateFromRequest(request, flightWatcher);
        flightWatcherRepository.save(flightWatcher);
        return mapper.map(flightWatcher);
    }

    @Transactional
    public void delete(Long id) {
        flightWatcherRepository.deleteById(id);
    }

    @Transactional
    public void suspend(Long id) {
        flightWatcherRepository.suspend(id);
    }

    @Transactional
    public void resume(Long id) {
        flightWatcherRepository.resume(id);
    }
}
