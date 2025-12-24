package com.carlisenceplate.lisenceplate.entity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "license_plate")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plate_number", unique = true, nullable = false)
    private String plateNumber;
    @Column(name = "owner_name")
    private String vehicleOwner;

    @Column(name = "status")
    private String status;


    //Creation Time
    @Column(name = "create_at")
    private LocalDateTime createAt;


    //Automation set creation time for save at first time
    @PrePersist
    protected void onCreate() {
        createAt = LocalDateTime.now();
    }
}
