package com.carlisenceplate.lisenceplate.storageService;


import com.carlisenceplate.lisenceplate.entity.Plate;
import com.carlisenceplate.lisenceplate.repository.LicensePlateRepository;
import net.sourceforge.tess4j.Tesseract;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class LicensePlateService {

    @Autowired
    private LicensePlateRepository licensePlateRepository;




    // URL's Python Server
    private final String PYTHON_API_URL = "http://localhost:9091/detect";

    public String extractPlateNumber(String imagePath) {

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            File file = new File(imagePath);


            //Use apache HttpClient to send image to Python server

            // 1. create Request and send imgage to  Python
            HttpPost uploadFile = new HttpPost(PYTHON_API_URL);


            MultipartEntityBuilder builder = MultipartEntityBuilder.create();// create a box

            // assigned file

            builder.addBinaryBody("image", file, ContentType.IMAGE_JPEG, file.getName()); //put thing(image ) into box
            HttpEntity multipart = builder.build(); //build the box before send
            uploadFile.setEntity(multipart); // put on carrier

            // 2. send and get response from Python
            try (CloseableHttpResponse response = httpClient.execute(uploadFile)) { // send the box
                HttpEntity responseEntity = response.getEntity(); // get response box
                String responseString = EntityUtils.toString(responseEntity); // get thing(string) from response box

                // 3. Parse JSON response from Python
                ObjectMapper mapper = new ObjectMapper(); // need a Parser
                JsonNode rootNode = mapper.readTree(responseString); //let him read the string

                String status = rootNode.path("status").asText(); // find status in JSON
                if ("success".equals(status)) {
                    return rootNode.path("plate").asText(); // if is success get plate number
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "No Plate Found";
    }

    public Plate registerVehicel(Plate plate){
        //Checking Plate availability
        if(licensePlateRepository.existsByPlateNumber(plate.getPlateNumber())){
            throw new RuntimeException("Plate Already Exists");
        }

        if (plate.getStatus() != null) {
            plate.setStatus("ALLOWED");
        }
        return licensePlateRepository.save(plate);
    }


    public List<Plate> getAllPlates(){
        return licensePlateRepository.findAll();
    }
}
