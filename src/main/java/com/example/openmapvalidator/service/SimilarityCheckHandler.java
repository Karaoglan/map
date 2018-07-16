package com.example.openmapvalidator.service;

import com.example.openmapvalidator.helper.ConfigurationService;
import com.example.openmapvalidator.helper.Const;
import com.example.openmapvalidator.model.PlaceDBModel;
import info.debatty.java.stringsimilarity.JaroWinkler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SimilarityCheckHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimilarityCheckHandler.class);

    private final JaroWinkler jaroWinklerApproach;
    private final ConfigurationService configurationService;

    @Autowired
    public SimilarityCheckHandler(JaroWinkler jaroWinkler,
                                  ConfigurationService configurationService) {
        this.jaroWinklerApproach = jaroWinkler;
        this.configurationService = configurationService;
    }

    /**
     * compare given google and openstreetmap place name value for similarity by defined score
     *
     * @param node from open street map
     * @param nameResultFromGooglePlace google places api model
     * @param foursquareName
     * @param microsoftPlaceName
     * @return true if they are not similar otherwise false
     */
    public boolean handle(PlaceDBModel node, String nameResultFromGooglePlace, String foursquareName,
                       String microsoftPlaceName) {

        LOGGER.debug("\n" + "*******COMPARE************");
        LOGGER.debug("openst -> {}", node.getName());
        LOGGER.debug("googleMap -> {}", nameResultFromGooglePlace);
        LOGGER.debug("foursq -> {}", foursquareName);
        LOGGER.debug("microsoft -> {}", microsoftPlaceName);

        //LOGGER.debug("compare -> " + nameResultFromGooglePlace.equals(node.getName()));
        LOGGER.debug("*********FINISH**************" + "\n");


        double similarity = jaroWinklerApproach.similarity(node.getName(), nameResultFromGooglePlace);
        LOGGER.info("******* similarity : {}", similarity);

        return similarity < Double.valueOf(configurationService.getSIMILARITY_SCORE());
    }
}
