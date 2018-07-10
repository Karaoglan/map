package com.example.openmapvalidator.service;

import com.example.openmapvalidator.helper.Const;
import com.example.openmapvalidator.model.foursquare.FoursquareResult;
import com.example.openmapvalidator.model.google.GoogleResult;
import com.example.openmapvalidator.model.microsoft.MicrosoftResult;
import com.example.openmapvalidator.mybatis.PlaceDBModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.debatty.java.stringsimilarity.JaroWinkler;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author senan.ahmedov
 */
@Service
public class FileToDB {

    private static final Logger logger = LoggerFactory.getLogger(FileToDB.class);

    private static int SIZE_OF_OSM_PLACES;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DocumentBuilderFactory dbFactory;

    @Autowired
    private JaroWinkler jaroWinklerApproach;

    private class StreamWrapper extends Thread {
        InputStream is = null;
        String type = null;
        String message = null;

        public String getMessage() {
            return message;
        }

        StreamWrapper(InputStream is, String type) {
            this.is = is;
            this.type = type;
        }

        public void run() {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuffer buffer = new StringBuffer();
                String line = null;
                while ( (line = br.readLine()) != null) {
                    buffer.append(line);//.append("\n");
                }
                message = buffer.toString();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    public StreamWrapper getStreamWrapper(InputStream is, String type){
        return new StreamWrapper(is, type);
    }

    private void osmFileToDB(String fileName) {

        boolean isWindows = System.getProperty(Const.OS_NAME)
                .toLowerCase().startsWith(Const.OS_WINDOWS_NAME);

        try {

            String OSM_ROOT;
            String cmd;

            if (isWindows) {
                OSM_ROOT = Const.OSM_WINDOWS_ROOT;

                String path = new ClassPathResource(OSM_ROOT).getFile().getAbsolutePath();

                cmd = path + File.separator +
                        Const.OSM_COMMAND + Const.SPACE + Const.OSM_COMMAND_CREATE_OPTION + Const.SPACE +
                        Const.OSM_COMMAND_USERNAME_OPTION + Const.SPACE + Const.PSQL_USERNAME +
                        Const.SPACE + Const.OSM_COMMAND_DATABASE_OPTION + Const.SPACE + Const.OSM_COMMAND_DATABASE_ARGUMENT +
                        Const.SPACE + "-S" + Const.SPACE + path + File.separator + "default.style " + Const.SPACE +
                        new ClassPathResource(Const.MAP_FOLDER_ROOT).getFile().getAbsolutePath() + File.separator + fileName;

            } else {
                OSM_ROOT = Const.OSM_UNIX_ROOT;
                String path = new ClassPathResource(OSM_ROOT).getFile().getAbsolutePath();

                cmd = path + File.separator + Const.OSM_COMMAND + Const.SPACE + Const.OSM_COMMAND_CREATE_OPTION +
                        Const.SPACE + Const.OSM_COMMAND_DATABASE_OPTION + Const.SPACE + Const.OSM_COMMAND_DATABASE_ARGUMENT +
                        Const.SPACE + Const.MAP_FOLDER_ROOT + fileName;
            }

            logger.info(cmd);

            Runtime rt = Runtime.getRuntime();
            StreamWrapper error, output;

            Process proc = rt.exec(cmd);
            error = new FileToDB().getStreamWrapper(proc.getErrorStream(), "ERROR");
            output = new FileToDB().getStreamWrapper(proc.getInputStream(), "OUTPUT");
            int exitVal;

            error.start();
            output.start();
            error.join(3000);
            output.join(3000);
            exitVal = proc.waitFor();
            logger.info("exitVal: {}\nOutput: {}\nError: {}", exitVal, output.message, error.message);

            if (exitVal != 0) {
                logger.error("Please resolve error");
                System.exit(1);
            }
        } catch(Exception e) {
            logger.error(e.toString());
            e.printStackTrace();
        }

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

        logger.debug("place data, openstreet from db");

        try  {

            osmFileToDB(fileName);

            SqlSession session = getDBSession();
            List<PlaceDBModel> list = session.selectList(Const.OSM_PSQL_PLACE_SELECT_QUERY_IDENTIFIER);

            SIZE_OF_OSM_PLACES = list.size();
            ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            long beforeTime = System.currentTimeMillis();

            for (PlaceDBModel model : list) {

                logger.debug("Id: {} & Name: {}", model.getOsm_id(), model.getName());

                executorService.execute(() -> {
                    nameMap.putAll(makeApiCallForPlaceToCompare(model));
                });

            }

            executorService.shutdown();
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

            logger.debug("");

            long endTime = System.currentTimeMillis();

            logger.debug("---******* run time for putting values inside map after compare -> {}", (int) ((endTime -
                    beforeTime) / 1000) % 60);
            session.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return nameMap;


    }

    /**
     * Get latitude and longitude of a place which is stored in db retrieve with osm_id
     *
     * @param node
     * @return
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    private Map<String, String> makeOpenStreetApiCallWithOSMID(PlaceDBModel node) throws IOException, SAXException,
            ParserConfigurationException {

        Map<String, String> longAndLatMap = new HashMap<>();

        String openStreetUriGet = Const.OPENSTREET_URI_GET_LONG_WITH_OSM_ID.replace("@OSM_ID", node.getOsm_id());

        String result = restTemplate.getForObject(openStreetUriGet, String.class);

        File tmpFile = File.createTempFile("test", ".xml");
        FileWriter writer = new FileWriter(tmpFile);
        writer.write(result);
        writer.close();

        DocumentBuilder dBuilder;

        dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(tmpFile);
        doc.getDocumentElement().normalize();
        logger.debug("Root element : {}", doc.getDocumentElement().getNodeName());
        NodeList nodeList = doc.getElementsByTagName("node");
        //now XML is loaded as Document in memory, lets convert it to Object List
        NamedNodeMap map = nodeList.item(0).getAttributes();
        String LAT = map.getNamedItem("lat").getNodeValue();
        String LON = map.getNamedItem("lon").getNodeValue();

        longAndLatMap.put("lat", LAT);
        longAndLatMap.put("lon", LON);

        return longAndLatMap;
    }

    private FoursquareResult makeFourSquareApiCall(String lat, String lon) throws IOException {

        String foursquareUriSearch = Const.FOURSQUARE_URI_SEARCH_WITH_LONG.replace("@LAT", lat)
                .replace("@LON", lon);

        String foursquareResultStr = restTemplate.getForObject(
                foursquareUriSearch, String.class);

        logger.debug(foursquareResultStr);

        return objectMapper.readValue(foursquareResultStr, FoursquareResult.class);
    }

    /**
     *
     * get google places result with latitude and longitude category of a place is mapped from openstreet
     *
     * @param lat
     * @param log
     * @return
     * @throws IOException
     */
    private GoogleResult makeGooglePlaceApiCall(String lat, String log) throws IOException {

        String googleUriSearch = Const.GOOGLE_SEARCH_NEARBY.replace("@LAT", lat)
                .replace("@LONG", log);

        String googleResultStr = restTemplate.getForObject(
                googleUriSearch, String.class);

        logger.debug(googleResultStr);

        return objectMapper.readValue(googleResultStr, GoogleResult.class);
    }

    /**
     *
     * its just applied to USA but in any short time other countries will added
     *
     * @param lat
     */
    private MicrosoftResult makeMicrosoftPlaceApiCall(String lat, String log) throws IOException {

        String microsoftUriSearch = Const.MICROSOFTMAP_SEARCH_WITH_LONG.replace("@LAT", lat)
                .replace("@LOG", log);

        String microsoftResultStr = restTemplate.getForObject(
                microsoftUriSearch, String.class);

        logger.debug(microsoftResultStr);

        return objectMapper.readValue(microsoftResultStr, MicrosoftResult.class);
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

            Map<String, String> latitudeAndLongitudeMap = makeOpenStreetApiCallWithOSMID(node);
            logger.debug("{}, {}", latitudeAndLongitudeMap.get("lat"), latitudeAndLongitudeMap.get("lon"));

            String lat = latitudeAndLongitudeMap.get("lat");
            String lon = latitudeAndLongitudeMap.get("lon");

            // GOOGLE
            GoogleResult googleResult = makeGooglePlaceApiCall(lat, lon);

            String nameResultFromGooglePlace = "NOT PRESENT";

            if (!googleResult.getResults().isEmpty()) {
                nameResultFromGooglePlace = googleResult.getResults().get(0).getName();
            }

            // FOURSQUARE
            FoursquareResult foursquareResult = makeFourSquareApiCall(lat, lon);

            String foursquareName = "NOT PRESENT";
            if (!foursquareResult.getResponse().getVenues().isEmpty()) {
                foursquareName = foursquareResult.getResponse().getVenues().get(0).getName();
            }


            // MICROSOFT
            MicrosoftResult microsoftResult = makeMicrosoftPlaceApiCall(lat, lon);
            String microsoftPlaceName = "NOT PRESENT";
            if (!microsoftResult.getResourceSets().get(0).getResources().get(0).getBusinessesAtLocation().isEmpty()) {
                microsoftPlaceName = microsoftResult.getResourceSets().get(0).
                        getResources().get(0).
                        getBusinessesAtLocation().get(0).
                        getBusinessInfo().getEntityName();
            }


            logger.debug("\n" + "*******COMPARE************");
            logger.debug("openst -> {}", node.getName());
            logger.debug("googleMap -> {}", nameResultFromGooglePlace);
            logger.debug("foursq -> {}", foursquareName);
            logger.debug("microsoft -> {}", microsoftPlaceName);

            //logger.debug("compare -> " + nameResultFromGooglePlace.equals(node.getName()));
            logger.debug("*********FINISH**************" + "\n");




            double similarity = jaroWinklerApproach.similarity(node.getName(), nameResultFromGooglePlace);
            logger.info("******* similarity : {}", similarity);
            if (similarity < Const.SIMILARITY_SCORE) {
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
