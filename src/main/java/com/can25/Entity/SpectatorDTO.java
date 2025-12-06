package com.can25.Entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SpectatorDTO {
    // Informations spectateur
    private String spectatorId;
    private int age;
    private String nationality;

    // Informations d'entrée au match
    private String matchId;
    private LocalDateTime entryTime;
    private String gate;
    private String ticketNumber;
    private String ticketType;

    // Localisation
    private SeatLocation seatLocation;

    // Calculé par le processor
    private BehaviorCategory category;
    private int totalMatches;
}