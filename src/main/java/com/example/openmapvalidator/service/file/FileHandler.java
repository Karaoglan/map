package com.example.openmapvalidator.service.file;

import com.example.openmapvalidator.helper.Const;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;

/**
 * this class helps to save file into project classpath
 *
 * @author senan.ahmedov
 */
@Service
public class FileHandler {

    public String saveFile(MultipartFile file) {
        String fileName = file.getOriginalFilename().trim().replaceAll("\\s","");
        try {
            File localFile = new File(new ClassPathResource(Const.MAP_FOLDER_ROOT).getFile(), fileName);
            FileUtils.writeByteArrayToFile(localFile, file.getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileName;
    }


}
