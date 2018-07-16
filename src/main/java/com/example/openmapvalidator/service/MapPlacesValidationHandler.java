package com.example.openmapvalidator.service;

import com.example.openmapvalidator.helper.Const;
import com.example.openmapvalidator.model.PlaceDBModel;
import com.example.openmapvalidator.model.foursquare.FoursquareResult;
import com.example.openmapvalidator.model.google.GoogleResult;
import com.example.openmapvalidator.model.microsoft.MicrosoftResult;
import com.example.openmapvalidator.service.convert.OsmToDBHandler;
import com.example.openmapvalidator.service.database.DatabaseSession;
import com.example.openmapvalidator.service.request.FoursquareRequestHandler;
import com.example.openmapvalidator.service.request.GoogleRequestHandler;
import com.example.openmapvalidator.service.request.MicrosoftRequestHandler;
import com.example.openmapvalidator.service.request.OpenStreetMapRequestHandler;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
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

    private final OpenStreetMapRequestHandler osmRequestHandler;
    private final MicrosoftRequestHandler microsoftRequestHandler;
    private final OsmToDBHandler osmToDBHandler;
    private final FoursquareRequestHandler foursquareRequestHandler;
    private final GoogleRequestHandler googleRequestHandler;
    private final SimilarityCheckHandler similarityCheckHandler;
    private final DatabaseSession databaseSession;

    @Autowired
    public MapPlacesValidationHandler(MicrosoftRequestHandler microsoftRequestHandler, OpenStreetMapRequestHandler osmRequestHandler,
                                      SimilarityCheckHandler similarityCheckHandler, OsmToDBHandler osmToDBHandler,
                                      FoursquareRequestHandler foursquareRequestHandler, GoogleRequestHandler
                                                  googleRequestHandler, DatabaseSession databaseSession) {

        this.osmRequestHandler = osmRequestHandler;
        this.microsoftRequestHandler = microsoftRequestHandler;
        this.osmToDBHandler = osmToDBHandler;
        this.foursquareRequestHandler = foursquareRequestHandler;
        this.googleRequestHandler = googleRequestHandler;
        this.similarityCheckHandler = similarityCheckHandler;
        this.databaseSession = databaseSession;
    }


    /**
     * this method gets the file specified with given name and pass it to the osm db handler
     * to put the geographic data to the database then queries places from db and pass it to the
     * compare function.
     * @param fileName osm file uploaded into project
     * @return all the places
     */
    public Map<String, Map<String, String>> saveAndCallForPlaceCoordinates(String fileName) {

        Map<String, Map<String, String>> nameMap = new ConcurrentHashMap<>();

        LOGGER.debug("place data, openstreet from db");

        try  {

            osmToDBHandler.handle(fileName);

            SqlSession session = databaseSession.getDBSession();
            List<PlaceDBModel> list = session.selectList(Const.OSM_PSQL_PLACE_SELECT_QUERY_IDENTIFIER);

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

            LOGGER.debug("---******* run time for putting values inside map after compare -> {} sec.",
                    (double) ((endTime - beforeTime) / 1000) % 60);
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

            String lat = latitudeAndLongitudeMap.get(Const.LATITUDE);
            String lon = latitudeAndLongitudeMap.get(Const.LONGITUDE);

            // GOOGLE
            GoogleResult googleResult = googleRequestHandler.handle(lat, lon);

            String nameResultFromGooglePlace = Const.NOT_PRESENT;

            if (!googleResult.getResults().isEmpty()) {
                nameResultFromGooglePlace = googleResult.getResults().get(0).getName();
            }

            // FOURSQUARE
            FoursquareResult foursquareResult = foursquareRequestHandler.handle(lat, lon);

            String foursquareName = Const.NOT_PRESENT;
            if (!foursquareResult.getResponse().getVenues().isEmpty()) {
                foursquareName = foursquareResult.getResponse().getVenues().get(0).getName();
            }


            // MICROSOFT
            MicrosoftResult microsoftResult = microsoftRequestHandler.handle(lat, lon);
            String microsoftPlaceName = Const.NOT_PRESENT;
            if (!microsoftResult.getResourceSets().get(0).getResources().get(0).getBusinessesAtLocation().isEmpty()) {
                microsoftPlaceName = microsoftResult.getResourceSets().get(0).
                        getResources().get(0).
                        getBusinessesAtLocation().get(0).
                        getBusinessInfo().getEntityName();
            }

            boolean isNotSimilar = similarityCheckHandler.handle(node, nameResultFromGooglePlace, foursquareName,
                    microsoftPlaceName);

            if (isNotSimilar) {
                String lngLat = latitudeAndLongitudeMap.get(Const.LATITUDE) + "," + latitudeAndLongitudeMap.get(Const.LONGITUDE);

                Map<String, String> mapOfNames = new HashMap<>();
                mapOfNames.put(Const.OPENSTREET, node.getName());
                mapOfNames.put(Const.GOOGLE, nameResultFromGooglePlace);
                mapOfNames.put(Const.FOURSQUARE, foursquareName);
                mapOfNames.put(Const.MICROSOFT, microsoftPlaceName);
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
