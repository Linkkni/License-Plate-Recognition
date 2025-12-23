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

    // URL của Python Server
    private final String PYTHON_API_URL = "http://localhost:5000/detect";

    public String extractPlateNumber(String imagePath) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            File file = new File(imagePath);

            // 1. Tạo Request gửi file sang Python
            HttpPost uploadFile = new HttpPost(PYTHON_API_URL);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();

            // Đính kèm file ảnh
            builder.addBinaryBody("image", file, ContentType.IMAGE_JPEG, file.getName());
            HttpEntity multipart = builder.build();
            uploadFile.setEntity(multipart);

            // 2. Gửi và nhận kết quả
            try (CloseableHttpResponse response = httpClient.execute(uploadFile)) {
                HttpEntity responseEntity = response.getEntity();
                String responseString = EntityUtils.toString(responseEntity);

                // 3. Parse JSON từ Python trả về
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(responseString);

                String status = rootNode.path("status").asText();
                if ("success".equals(status)) {
                    return rootNode.path("plate").asText(); // Trả về biển số: "GR35383"
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "No Plate Found";
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
