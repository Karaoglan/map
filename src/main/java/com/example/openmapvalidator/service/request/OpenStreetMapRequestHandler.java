package com.example.openmapvalidator.service.request;

import com.example.openmapvalidator.helper.ConfigurationService;
import com.example.openmapvalidator.helper.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class OpenStreetMapRequestHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenStreetMapRequestHandler.class);

    private final RestTemplate restTemplate;
    private final DocumentBuilderFactory dbFactory;
    private final ConfigurationService configurationService;

    @Autowired
    public OpenStreetMapRequestHandler(RestTemplate restTemplate, DocumentBuilderFactory factory,
                                       ConfigurationService configurationService) {
        this.restTemplate = restTemplate;
        this.dbFactory = factory;
        this.configurationService = configurationService;
    }

    /**
     * Get latitude and longitude of a place which is stored in db retrieve with osm_id
     *
     * @param osmId openstreet map id
     * @return
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public Map<String, String> handle(String osmId) throws IOException, SAXException,
            ParserConfigurationException {

        Map<String, String> longAndLatMap = new HashMap<>();

        String openStreetUriGet = configurationService.getOPENSTREET_URI_GET_LONG_WITH_OSM_ID()
                .replace(Const.OSMID_REPLACEMENT_SHORTCUT, osmId);

        String result = restTemplate.getForObject(openStreetUriGet, String.class);

        File tmpFile = File.createTempFile("test", ".xml");
        FileWriter writer = new FileWriter(tmpFile);
        writer.write(result);
        writer.close();

        DocumentBuilder dBuilder;

        dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(tmpFile);
        doc.getDocumentElement().normalize();
        LOGGER.debug("Root element : {}", doc.getDocumentElement().getNodeName());
        NodeList nodeList = doc.getElementsByTagName("node");
        //now XML is loaded as Document in memory, lets convert it to Object List
        NamedNodeMap map = nodeList.item(0).getAttributes();
        String LAT = map.getNamedItem("lat").getNodeValue();
        String LON = map.getNamedItem("lon").getNodeValue();

        longAndLatMap.put(Const.LATITUDE, LAT);
        longAndLatMap.put(Const.LONGITUDE, LON);

        return longAndLatMap;
    }

}
