package com.can25.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;


@Entity
@Data
public class SeatLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long locationId;

    private String tribune;
    private String bloc;
    private int rang;
    private int siege;

}
