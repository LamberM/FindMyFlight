package com.findmyflight.findmyflight.service.flightwatcher;

import com.findmyflight.findmyflight.service.emailnotificationreceiver.EmailNotificationReceiver;
import com.findmyflight.findmyflight.service.flightresult.FlightResult;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FlightWatcher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String fromCity;

    @NotBlank
    private String toCity;

    @Temporal(TemporalType.DATE)
    private LocalDate fromDate;

    @Temporal(TemporalType.DATE)
    private LocalDate toDate;

    @NotNull
    private Boolean suspended;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private EmailNotificationReceiver emailNotificationReceiver;

    @OneToOne(mappedBy = "flightWatcher",fetch = FetchType.LAZY)
    private FlightResult flightResult;
}
