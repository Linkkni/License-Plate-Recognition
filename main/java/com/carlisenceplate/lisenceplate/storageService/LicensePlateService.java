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
    public String extracPlateNumber(String imagePath){

        //Reading Image

        Mat source = Imgcodecs.imread(imagePath);

        if(source.empty()){
            throw new RuntimeException("Cannot load image at: " + imagePath);
        }

        //Create variables to hold Intermediary Images
        Mat gray = new Mat();
        Mat binary = new Mat();

        // Gray Scale: Conver image to gray(0-255)

        Imgproc.cvtColor(source, gray, Imgproc.COLOR_BGR2GRAY);

        // Gaussin Blur
        Imgproc.GaussianBlur(gray, gray, new Size(3,3), 0);

        // Thresholding: Conver image to absolutely black/white
        // use Otsu algorithms to automation finding brightness
        Imgproc.threshold(gray, binary, 0, 255, Imgproc.THRESH_BINARY| Imgproc.THRESH_OTSU);


        //ROI extraction will be here
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binary,contours,hierarchy,Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        Mat plateImage = null; // Initialize plateImage after cut
        String debugPath = imagePath.replace(".jpg", "_debug.jpg");

        // Loop through contours to find potential license plates
        for(MatOfPoint contour : contours){
            //Create rectangle around contour

            Rect rect = Imgproc.boundingRect(contour);

            // filter logic
            double aspectRatio = (double) rect.width / rect.height;

            // filter basic condition, (>500 pixel to not take dirty small contour)
            //width/height ratio 2.0 to 6.0

            if( rect.width > 100 && rect.height>20 && aspectRatio > 2.0 && aspectRatio < 6.0){
                //Draw rectangle on original image
                Imgproc.rectangle(source, rect,  new Scalar(0,255,0),3);


                //Crop license plate from original image

                plateImage = binary.submat(rect);


                break;
            }
        }

        Imgcodecs.imwrite(debugPath,source);

        if(plateImage != null){

            //Invert

            int whitePixelsCount = Core.countNonZero(plateImage);
            int totalPixalCount = plateImage.rows() * plateImage.cols();

            //if image have alot black (> 50%) -> that mean white text on black background -> need invert
            if(whitePixelsCount < totalPixalCount * 0.5) {
                Core.bitwise_not(plateImage, plateImage);
            }

            //save image after processed
            String finalPath = imagePath.replace(".jpg", "_plate.jpg");
            Imgcodecs.imwrite(finalPath,plateImage);

            //OCR
            try {
                Tesseract tesseract = new Tesseract();
                tesseract.setDatapath("tessdata"); // path to tessdata directory
                tesseract.setLanguage("eng");

                //setting whitelist characters to skip trash characters
                tesseract.setTessVariable("tessedit_char_whitelist", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");

                //convert Mat to BufferedImage

                BufferedImage bufferedImage = matToBufferedImage(plateImage);

                String result = tesseract.doOCR(bufferedImage);

                return result.replaceAll("\n", "").trim(); //remove all whitespace characters
            } catch (Exception e) {
                e.printStackTrace();
                return "Error Reading Plate";
            }

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
