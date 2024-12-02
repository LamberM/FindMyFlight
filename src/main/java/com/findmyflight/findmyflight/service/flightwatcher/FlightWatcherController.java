package com.findmyflight.findmyflight.service.flightwatcher;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/flight-watcher")
@Validated
@RequiredArgsConstructor
public class FlightWatcherController {
    private final FlightWatcherService flightWatcherService;

    @GetMapping()
    public ResponseEntity<Collection<FlightWatcherResponse>> findAll() {
        return ResponseEntity.ok(flightWatcherService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlightWatcherResponse> findById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(flightWatcherService.findById(id));
    }

    @PostMapping()
    public ResponseEntity<FlightWatcherResponse> create(@Valid @RequestBody CreateOrUpdateFlightWatcherRequest request) {
        return ResponseEntity.ok(flightWatcherService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FlightWatcherResponse> update(@PathVariable("id") Long id,
                                                        @Valid @RequestBody CreateOrUpdateFlightWatcherRequest request) {
        return ResponseEntity.ok(flightWatcherService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        flightWatcherService.delete(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/suspend")
    public ResponseEntity<Void> suspend(@PathVariable("id") Long id) {
        flightWatcherService.suspend(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/resume")
    public ResponseEntity<Void> resume(@PathVariable("id") Long id) {
        flightWatcherService.resume(id);
        return ResponseEntity.ok().build();
    }
}
