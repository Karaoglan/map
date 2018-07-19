package com.example.openmapvalidator.service.file;

import com.example.openmapvalidator.helper.Const;
import com.example.openmapvalidator.model.open.GeographicRectangle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class XMLFileParser {

    private final DocumentBuilderFactory dbFactory;
    private final FileHandler fileHandler;

    @Autowired
    public XMLFileParser(DocumentBuilderFactory dbFactory, FileHandler fileHandler) {
        this.dbFactory = dbFactory;
        this.fileHandler = fileHandler;
    }

    /**
     * returns rectangle min and max latitude and longitudes coordinates by parsing given osm map file
     * @param fileName
     * @return
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public GeographicRectangle parseRectangleCoordinates(String fileName)
            throws IOException, SAXException, ParserConfigurationException {

        File localFile = new File(new ClassPathResource(Const.MAP_FOLDER_ROOT).getFile(), fileName);

        //String osmXmlContent = new String(Files.readAllBytes(localFile.toPath()));

        // in order to read som file content as xml
       // File tmpFile = fileHandler.createTmpFileAndPutContent(osmXmlContent);

        Document doc = getDocumentForXmlParse(localFile);
        NodeList nList = doc.getElementsByTagName("bounds");
        Node nNode = nList.item(0);

        Element eElement = (Element) nNode;

        String minlat = eElement.getAttribute("minlat");
        String minlon = eElement.getAttribute("minlon");
        String maxlat = eElement.getAttribute("maxlat");
        String maxlon = eElement.getAttribute("maxlon");

        GeographicRectangle rectangle = new GeographicRectangle();
        rectangle.setMinLongitude(Double.valueOf(minlon));
        rectangle.setMinLatitude(Double.valueOf(minlat));
        rectangle.setMaxLongitude(Double.valueOf(maxlon));
        rectangle.setMaxLatitude(Double.valueOf(maxlat));

        return rectangle;
    }

    public Document getDocumentForXmlParse(File file) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder dBuilder;

        dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);
        doc.getDocumentElement().normalize();
        return doc;
    }
}
