package com.findmyflight.findmyflight.service.flightwatcher;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface FlightWatcherRepository extends JpaRepository<FlightWatcher, Long> {
    @Modifying
    @Query("update FlightWatcher fw set fw.suspended = true where fw.id = ?1")
    void suspend(Long id);

    @Modifying
    @Query("update FlightWatcher fw set fw.suspended = false where fw.id = ?1")
    void resume(Long id);
}