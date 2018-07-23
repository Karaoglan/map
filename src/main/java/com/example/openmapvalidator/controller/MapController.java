package com.example.openmapvalidator.controller;

import com.example.openmapvalidator.helper.Const;
import com.example.openmapvalidator.model.open.GeographicRectangle;
import com.example.openmapvalidator.service.MapPlacesValidationHandler;
import com.example.openmapvalidator.service.file.FileHandler;
import com.example.openmapvalidator.service.file.XMLFileParser;
import com.example.openmapvalidator.service.statistic.GoogleNearbyRequestHandler;
import com.example.openmapvalidator.service.statistic.OpenmapRequestHandler;
import com.example.openmapvalidator.service.statistic.RadiusHandler;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author senan.ahmedov
 */
@RestController
@RequestMapping("/maps")
public class MapController {

    private static final Logger logger = LoggerFactory.getLogger(MapController.class);

    private final FileHandler fileHandlerImpl;
    private final MapPlacesValidationHandler mapPlacesValidationHandlerService;
    private final GoogleNearbyRequestHandler googleNearbyRequestHandler;
    private final RadiusHandler radiusHandler;
    private final OpenmapRequestHandler openStreetMapRequestHandler;
    private final XMLFileParser xmlFileParserImpl;
    private final Gson gson;

    public MapController(MapPlacesValidationHandler mapPlacesValidationHandlerService,
                         GoogleNearbyRequestHandler googleNearbyRequestHandler,
                         OpenmapRequestHandler openStreetMapRequestHandler, RadiusHandler radiusHandler,
                         XMLFileParser xmlFileParserImpl,
                         FileHandler fileHandlerImpl, Gson gson) {
        this.mapPlacesValidationHandlerService = mapPlacesValidationHandlerService;
        this.googleNearbyRequestHandler = googleNearbyRequestHandler;
        this.openStreetMapRequestHandler = openStreetMapRequestHandler;
        this.radiusHandler = radiusHandler;
        this.fileHandlerImpl = fileHandlerImpl;
        this.xmlFileParserImpl = xmlFileParserImpl;
        this.gson = gson;
    }

    @GetMapping
    public Map<String, Integer> statisticValues(@RequestParam String fileName) {

        fileName = fileName.trim().replaceAll("\\s","");

        GeographicRectangle rectangle = null;
        try {
            rectangle = xmlFileParserImpl.parseRectangleCoordinates(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        Map<String, Double> geographicValueMap = radiusHandler.handle(rectangle);

        String lat = geographicValueMap.get(Const.LATITUDE).toString();
        String lon = geographicValueMap.get(Const.LONGITUDE).toString();
        double radius = geographicValueMap.get(Const.RADIUS);

        int numOfGooglePlaces = 0;
        int numOfOpenstreetMapPlaces = 0;

        try {
            numOfGooglePlaces = googleNearbyRequestHandler.handleNearbyWithRadius(lat, lon, radius);
            numOfOpenstreetMapPlaces = openStreetMapRequestHandler.countOpenmapPlaces();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Map<String, Integer> statisticValueMap = new HashMap<>();
        statisticValueMap.put(Const.NUMBER_OF_GOOGLE_PLACES, numOfGooglePlaces);
        statisticValueMap.put(Const.NUMBER_OF_OPENMAP_PLACES, numOfOpenstreetMapPlaces);


        return statisticValueMap;
        //return mockStatisticData();
    }

    @PostMapping
    public Map<String, Map<String, String>> uploadFile(@RequestParam MultipartFile file) {
        logger.info("NEW REQUEST");

        String fileName = fileHandlerImpl.saveFile(file);

        Map<String, Map<String, String>> map =
                mapPlacesValidationHandlerService.saveAndCallForPlaceCoordinates(fileName);

        logger.debug("****Returned Map Json");
        logger.debug(gson.toJson(map));

        return map;

        //return mockData();
    }

    private Map<String, Integer> mockStatisticData() {
        String json = "{\"numOfGooglePlaces\":8,\"numOfOpenstreetMapPlaces\":7}";
        // convert JSON string to Map
        Type type = new TypeToken<Map<String, Integer>>(){}.getType();
        return gson.fromJson(json, type);
    }

    private Map<String, Map<String, String>> mockData() {
        String json = "{\"48.1800796,16.3276520\":{\"foursquare\":\"NULL\",\"google\":\"Tiefgarage " +
                "F\u00FCchselhofpark\",\"openstreet\":\"F\u00FCchselhofparkgarage\"},\"48.1792934," +
                "16.3266492\":{\"foursquare\":\"NULL\",\"google\":\"UTOPIA\",\"openstreet\":\"Club UTOPIA\"},\"48.1798179,16.3273043\":{\"foursquare\":\"Hofer\",\"google\":\"HOFER\",\"openstreet\":\"Hofer\"},\"48.1790217,16.3264600\":{\"foursquare\":\"Eni Ruckergasse\",\"google\":\"eni ServiceStation\",\"openstreet\":\"Agip\"}}";

        // convert JSON string to Map
        Type type = new TypeToken<Map<String, Map<String, String>>>(){}.getType();
        return gson.fromJson(json, type);
    }
}