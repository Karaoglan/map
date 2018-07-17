package com.example.openmapvalidator.service.file;

import com.example.openmapvalidator.helper.Const;
import com.example.openmapvalidator.model.open.GeographicRectangle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.print.Doc;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class XMLFileParser {

    private final DocumentBuilderFactory dbFactory;

    @Autowired
    public XMLFileParser(DocumentBuilderFactory dbFactory) {
        this.dbFactory = dbFactory;
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

        byte[] encoded = Files.readAllBytes(Paths.get(URI.create(localFile.getAbsolutePath())));
        return new String(encoded, encoding);


        Document doc = getDocumentForXmlParse(localFile);
        NodeList nodeList = doc.getElementsByTagName("node");
        //now XML is loaded as Document in memory, lets convert it to Object List
        Node map = nodeList.item(0);
        Node node = map.getChildNodes().item(0); //bounds
        NamedNodeMap nodeMap = node.getAttributes();

        String minlat = nodeMap.getNamedItem("minlat").getNodeValue();
        String minlon = nodeMap.getNamedItem("minlon").getNodeValue();
        String maxlat = nodeMap.getNamedItem("maxlat").getNodeValue();
        String maxlon = nodeMap.getNamedItem("maxlon").getNodeValue();
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
