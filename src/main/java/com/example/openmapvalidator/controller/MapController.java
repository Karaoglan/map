package com.example.openmapvalidator.controller;

import com.example.openmapvalidator.helper.Const;
import com.example.openmapvalidator.model.open.GeographicRectangle;
import com.example.openmapvalidator.service.MapPlacesValidationHandler;
import com.example.openmapvalidator.service.request.OpenStreetMapRequestHandler;
import com.example.openmapvalidator.service.statistic.GoogleNearbyRequestHandler;
import com.example.openmapvalidator.service.statistic.OpenmapRequestHandler;
import com.example.openmapvalidator.service.statistic.RadiusHandler;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/maps")
public class MapController {

    private static final Logger logger = LoggerFactory.getLogger(MapController.class);

    private final MapPlacesValidationHandler mapPlacesValidationHandlerService;
    private final GoogleNearbyRequestHandler googleNearbyRequestHandler;
    private final RadiusHandler radiusHandler;
    private final OpenmapRequestHandler openStreetMapRequestHandler;
    private final Gson gson;

    @Autowired
    public MapController(MapPlacesValidationHandler mapPlacesValidationHandlerService,
                         GoogleNearbyRequestHandler googleNearbyRequestHandler,
                         OpenmapRequestHandler openStreetMapRequestHandler, RadiusHandler radiusHandler,
                         Gson gson) {
        this.mapPlacesValidationHandlerService = mapPlacesValidationHandlerService;
        this.googleNearbyRequestHandler = googleNearbyRequestHandler;
        this.openStreetMapRequestHandler = openStreetMapRequestHandler;
        this.radiusHandler = radiusHandler;
        this.gson = gson;
    }

    @GetMapping
    public Map<String, Integer> statisticValues(@RequestParam GeographicRectangle rectangle) {

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
    }

    //TODO brk add, strategy pattern uygulanabilir mi similarity algo icin?

    @PostMapping
    public Map<String, Map<String, String>> uploadFile(@RequestParam MultipartFile file) {
        logger.info("NEW REQUEST");
        String fileName = file.getOriginalFilename().trim().replaceAll("\\s","");
        try {
            File localFile = new File(new ClassPathResource(Const.MAP_FOLDER_ROOT).getFile(), fileName);
            FileUtils.writeByteArrayToFile(localFile, file.getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<String, Map<String, String>> map =
                mapPlacesValidationHandlerService.saveAndCallForPlaceCoordinates(fileName);

        logger.debug("****Returned Map Json");
        logger.debug(gson.toJson(map));

        return map;

        /*Map<String, Map<String, String>> map = null;
        try {
            map = mockData();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return map;*/
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