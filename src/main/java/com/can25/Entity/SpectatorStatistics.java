package com.can25.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class SpectatorStatistics {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long statsId ;

    private String spectatorId  ;
    private String behaviorCategory  ;
    private Integer totalMatches;


    public SpectatorStatistics() {}


}
