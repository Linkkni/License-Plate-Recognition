package com.carlisenceplate.lisenceplate.storageService;


import com.carlisenceplate.lisenceplate.entity.Plate;
import com.carlisenceplate.lisenceplate.repository.LicensePlateRepository;
import net.sourceforge.tess4j.Tesseract;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

@Service
public class LicensePlateService {

    @Autowired
    private LicensePlateRepository licensePlateRepository;

    //this function read image original path -> response image path has processed








            }
        } catch (Exception e) {
            e.printStackTrace();
            }
            return "No Plate Found";
        }
    private BufferedImage matToBufferedImage(Mat mat) throws IOException {
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(".jpg", mat, mob);
        return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
    }
    public Map<String, Object> checkVehicle(String plateNumber){

        Map<String, Object> response = new HashMap<>();

        //Finding in Database
        Optional<Plate> plateOpt = licensePlateRepository.findByPlateNumber(plateNumber);

        if(plateOpt.isPresent()){
            Plate plate = plateOpt.get();

            //checking status
            if("ALLOWED".equalsIgnoreCase(plate.getStatus())) {
                response.put("status", "SUCCESS");
                response.put("message", "Gate Opened. Welcome " + plate.getVehicleOwner());
                response.put("data", plate);
            }else {
                response.put("status", "BLOCKED");
                response.put("message", "Access Denied. Vehicle is not allowed.");
                response.put("data", plate);
            }
        }else {
            //Not sign up yet(Stranger car)
            response.put("status", "UNKNOWN");
            response.put("message", "Unregistered Vehicle. Please contact security.");
            response.put("data", plateNumber);
        }
        return response;
    }

    public Plate registerVehical(Plate plate){
        //Checking Plate availability
        if(licensePlateRepository.existsByPlateNumber(plate.getPlateNumber())){
            throw new RuntimeException("Plate Already Exists");
        }

        if (plate.getStatus() == null) {
            plate.setStatus("ALLOWED");
        }

        return licensePlateRepository.save(plate);
    }


    public List<Plate> getAllPlates(){
        return licensePlateRepository.findAll();
    }
}
