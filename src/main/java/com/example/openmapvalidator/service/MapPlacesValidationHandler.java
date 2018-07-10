package com.example.openmapvalidator.service;

import com.example.openmapvalidator.helper.Const;
import com.example.openmapvalidator.model.PlaceDBModel;
import com.example.openmapvalidator.model.foursquare.FoursquareResult;
import com.example.openmapvalidator.model.google.GoogleResult;
import com.example.openmapvalidator.model.microsoft.MicrosoftResult;
import com.example.openmapvalidator.service.convert.OsmToDBHandler;
import com.example.openmapvalidator.service.request.FoursquareRequestHandler;
import com.example.openmapvalidator.service.request.GoogleRequestHandler;
import com.example.openmapvalidator.service.request.MicrosoftRequestHandler;
import com.example.openmapvalidator.service.request.OpenStreetMapRequestHandler;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author senan.ahmedov
 */
@Service
public class MapPlacesValidationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapPlacesValidationHandler.class);

    private static int SIZE_OF_OSM_PLACES;

    private final OpenStreetMapRequestHandler osmRequestHandler;
    private final MicrosoftRequestHandler microsoftRequestHandler;
    private final OsmToDBHandler osmToDBHandler;
    private final FoursquareRequestHandler foursquareRequestHandler;
    private final GoogleRequestHandler googleRequestHandler;
    private final SimilarityCheckHandler similarityCheckHandler;

    @Autowired
    public MapPlacesValidationHandler(MicrosoftRequestHandler microsoftRequestHandler, OpenStreetMapRequestHandler osmRequestHandler,
                                      SimilarityCheckHandler similarityCheckHandler, OsmToDBHandler osmToDBHandler,
                                      FoursquareRequestHandler foursquareRequestHandler, GoogleRequestHandler googleRequestHandler) {

        this.osmRequestHandler = osmRequestHandler;
        this.microsoftRequestHandler = microsoftRequestHandler;
        this.osmToDBHandler = osmToDBHandler;
        this.foursquareRequestHandler = foursquareRequestHandler;
        this.googleRequestHandler = googleRequestHandler;
        this.similarityCheckHandler = similarityCheckHandler;
    }

    private SqlSession getDBSession() throws IOException {
        String resource = "mybatis/config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        SqlSessionFactory sqlSessionFactory = new
                SqlSessionFactoryBuilder().build(inputStream);

        return sqlSessionFactory.openSession();
    }

    public Map<String, Map<String, String>> saveAndCallForPlaceCoordinates(String fileName) {

        Map<String, Map<String, String>> nameMap = new ConcurrentHashMap<>();

        LOGGER.debug("place data, openstreet from db");

        try  {

            osmToDBHandler.handle(fileName);

            SqlSession session = getDBSession();
            List<PlaceDBModel> list = session.selectList(Const.OSM_PSQL_PLACE_SELECT_QUERY_IDENTIFIER);

            SIZE_OF_OSM_PLACES = list.size();
            ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            long beforeTime = System.currentTimeMillis();

            for (PlaceDBModel model : list) {

                LOGGER.debug("Id: {} & Name: {}", model.getOsm_id(), model.getName());

                executorService.execute(() -> {
                    nameMap.putAll(makeApiCallForPlaceToCompare(model));
                });

            }

            executorService.shutdown();
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

            LOGGER.debug("");

            long endTime = System.currentTimeMillis();

            LOGGER.debug("---******* run time for putting values inside map after compare -> {}", (int) ((endTime -
                    beforeTime) / 1000) % 60);
            session.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return nameMap;


    }

    /**
     *
     * get result from google and compare with node
     *
     * @param node
     * @return
     */
    private Map<String, Map<String, String>> makeApiCallForPlaceToCompare(PlaceDBModel node) {

        Map<String, Map<String, String>> nameMap = new HashMap<>();

        try {

            Map<String, String> latitudeAndLongitudeMap = osmRequestHandler.handle(node.getOsm_id());
            LOGGER.debug("{}, {}", latitudeAndLongitudeMap.get("lat"), latitudeAndLongitudeMap.get("lon"));

            String lat = latitudeAndLongitudeMap.get("lat");
            String lon = latitudeAndLongitudeMap.get("lon");

            // GOOGLE
            GoogleResult googleResult = googleRequestHandler.handle(lat, lon);

            String nameResultFromGooglePlace = "NOT PRESENT";

            if (!googleResult.getResults().isEmpty()) {
                nameResultFromGooglePlace = googleResult.getResults().get(0).getName();
            }

            // FOURSQUARE
            FoursquareResult foursquareResult = foursquareRequestHandler.handle(lat, lon);

            String foursquareName = "NOT PRESENT";
            if (!foursquareResult.getResponse().getVenues().isEmpty()) {
                foursquareName = foursquareResult.getResponse().getVenues().get(0).getName();
            }


            // MICROSOFT
            MicrosoftResult microsoftResult = microsoftRequestHandler.handle(lat, lon);
            String microsoftPlaceName = "NOT PRESENT";
            if (!microsoftResult.getResourceSets().get(0).getResources().get(0).getBusinessesAtLocation().isEmpty()) {
                microsoftPlaceName = microsoftResult.getResourceSets().get(0).
                        getResources().get(0).
                        getBusinessesAtLocation().get(0).
                        getBusinessInfo().getEntityName();
            }

            boolean isNotSimilar = similarityCheckHandler.handle(node, nameResultFromGooglePlace, foursquareName,
                    microsoftPlaceName);

            if (isNotSimilar) {
                String lngLat = latitudeAndLongitudeMap.get("lat") + "," + latitudeAndLongitudeMap.get("lon");

                Map<String, String> mapOfNames = new HashMap<>();
                mapOfNames.put("openstreet", node.getName());
                mapOfNames.put("google", nameResultFromGooglePlace);
                mapOfNames.put("foursquare", foursquareName);
                mapOfNames.put("microsoft", microsoftPlaceName);
                nameMap.put(lngLat, mapOfNames);

            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        return nameMap;

    }

}
