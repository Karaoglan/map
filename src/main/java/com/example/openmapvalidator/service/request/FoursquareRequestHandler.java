package com.example.openmapvalidator.service.request;

import com.example.openmapvalidator.helper.ConfigurationService;
import com.example.openmapvalidator.model.foursquare.FoursquareResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
public class FoursquareRequestHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FoursquareRequestHandler.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ConfigurationService configurationService;

    @Autowired
    public FoursquareRequestHandler(RestTemplate restTemplate, ObjectMapper objectMapper, ConfigurationService configurationService) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.configurationService = configurationService;
    }

    /**
     * get detail of a place which is located in lat, lon
     *
     * @param lat latitude
     * @param lon longitude
     * @return
     */
    public FoursquareResult handle(String lat, String lon) throws IOException {

        String foursquareUriSearch = configurationService.getFOURSQUARE_URI_SEARCH_WITH_LONG().replace("@LAT", lat)
                .replace("@LON", lon);

        String foursquareResultStr = restTemplate.getForObject(
                foursquareUriSearch, String.class);

        LOGGER.debug(foursquareResultStr);

        return objectMapper.readValue(foursquareResultStr, FoursquareResult.class);
    }

}