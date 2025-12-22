package com.carlisenceplate.lisenceplate.controller;


import com.carlisenceplate.lisenceplate.entity.Plate;
import com.carlisenceplate.lisenceplate.storageService.LicensePlateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/plates")
public class LicensePlateController {
    @Autowired
    private LicensePlateService licensePlateService;

    @PostMapping("/add")
    public ResponseEntity<?> addPlate(@RequestBody Plate plate){
        try {
            return ResponseEntity.ok(licensePlateService.registerVehical(plate));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/getall")
    public ResponseEntity<?> getAllPlates(){
        return ResponseEntity.ok(licensePlateService.getAllPlates());
    }
}
