package com.carlisenceplate.lisenceplate.storageService;


import com.carlisenceplate.lisenceplate.entity.ImageData;
import com.carlisenceplate.lisenceplate.repository.FileDataRepository;
import com.carlisenceplate.lisenceplate.repository.StorageRepository;
import com.carlisenceplate.lisenceplate.util.ImageUtils;
import org.apache.pdfbox.debugger.ui.ImageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Service
public class StorageService {
    @Autowired
    private StorageRepository storageRepository;
    @Autowired
    private FileDataRepository fileDataRepository;

    private final String FOLDER_PATH = "C:\\Users\\fixah\\Pictures\\Saved Pictures\\";

    public String uploadImage(MultipartFile file) throws IOException {

        //Create File Path
        String fullPath = FOLDER_PATH + file.getOriginalFilename();

        // save file information to database
        ImageData imageData = storageRepository.save(ImageData.builder()
                        .name(file.getOriginalFilename())
                        .type(file.getContentType())
                        .filePath(fullPath)
                        .build());


        file.transferTo(new File(fullPath));

        if (imageData != null) {
            return fullPath;
        }
        return null;

    }
}
