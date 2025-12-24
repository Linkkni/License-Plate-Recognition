package com.carlisenceplate.lisenceplate.controller;

import com.carlisenceplate.lisenceplate.dto.PlateCheckResponse;
import com.carlisenceplate.lisenceplate.entity.Plate;
import com.carlisenceplate.lisenceplate.repository.LicensePlateRepository;
import com.carlisenceplate.lisenceplate.storageService.LicensePlateService;
import com.carlisenceplate.lisenceplate.storageService.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/images")
public class ImageController {
    @Autowired
    private StorageService storageService;

    @Autowired
    private LicensePlateService licensePlateService;


    @Autowired
    private LicensePlateRepository licensePlateRepository;


//
//    @PostMapping("/upload")
//    public ResponseEntity<String> upload(@RequestParam("image") MultipartFile file) {
//        try{
//            String uploadResult = storageService.uploadImage(file);
//
//
//            if(uploadResult != null){
//                String processPath = licensePlateService.processImage(uploadResult);
//                return ResponseEntity.ok("Upload successful: "+ uploadResult);
//            }else {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Upload Failed. File path: " + uploadResult);
//            }
//
//
//
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }


    @PostMapping("/check")
    public ResponseEntity<?> checkPlate(@RequestParam("image") MultipartFile file) {

        try {

            //upload image

            String imagePath = storageService.uploadImage(file);
            String plateNumber = licensePlateService.extractPlateNumber(imagePath);
            if(plateNumber.equals("No Plate Found")){
                System.out.println("No plate detected, returning error");
                PlateCheckResponse response = new PlateCheckResponse(
                        "UNKNOWN",
                        "Could not detect any license plate.",
                        null
                );
                return  ResponseEntity.badRequest().body(response);
            }


            if(licensePlateRepository.existsByPlateNumber(plateNumber)){
                Plate existingPlate = licensePlateRepository.findByPlateNumber(plateNumber)
                .orElseThrow(() -> new RuntimeException("Plate not found"));

                PlateCheckResponse response = new PlateCheckResponse(
                        existingPlate.getStatus(),
                        "Plate found in the system.",
                        existingPlate
                );
                return ResponseEntity.ok(response);
            } else {
                // Plate not in database - BLOCKED by default
                System.out.println("Plate NOT in database: " + plateNumber);

                PlateCheckResponse response = new PlateCheckResponse(
                        "BLOCKED",
                        "Vehicle not registered in system",
                        null
                );
                return ResponseEntity.status(403).body(response);
            }
            //Map<String, Object> result = licensePlateService.registerVehicel(plateNumber);

        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getMessage());
            e.printStackTrace();

            PlateCheckResponse response = new PlateCheckResponse(
                    "UNKNOWN",
                    "System Error: " + e.getMessage(),
                    null
            );
            return ResponseEntity.internalServerError().body(response);
        }

    }
}
