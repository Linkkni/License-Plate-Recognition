package com.carlisenceplate.lisenceplate.controller;

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

@RestController
@RequestMapping("/api/images")
public class ImageController {
    @Autowired
    private StorageService storageService;

    @Autowired
    private LicensePlateService licensePlateService;

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
            String imagePath = storageService.uploadImage(file);
            String plateNumber = licensePlateService.extracPlateNumber(imagePath);
            if(plateNumber.equals("No Plate Found")){
                return ResponseEntity.badRequest().body("Could not detect any license plate.");
            }

            Map<String, Object> result = licensePlateService.checkVehicle(plateNumber);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }

    }
}
