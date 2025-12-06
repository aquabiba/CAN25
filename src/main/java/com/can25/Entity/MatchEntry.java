package com.can25.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class MatchEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;  // clé primaire interne unique

    private String matchId;
    private String spectatorId;
    private LocalDateTime entryTime;
    private String gate;
    private String ticketNumber;
    private String ticketType;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "location_id")
    private SeatLocation seatLocation;
}
