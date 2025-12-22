package com.carlisenceplate.lisenceplate.repository;

import com.carlisenceplate.lisenceplate.entity.Plate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LicensePlateRepository extends JpaRepository<Plate, Long> {

    Optional<Plate> findByPlateNumber(String plateNumber);

    boolean existsByPlateNumber(String plateNumber);
}
