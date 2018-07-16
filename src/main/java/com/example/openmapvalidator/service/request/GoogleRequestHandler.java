package com.example.openmapvalidator.service.request;

import com.example.openmapvalidator.helper.ConfigurationService;
import com.example.openmapvalidator.helper.Const;
import com.example.openmapvalidator.model.google.GoogleResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
public class GoogleRequestHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleRequestHandler.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ConfigurationService configurationService;

    @Autowired
    public GoogleRequestHandler(RestTemplate restTemplate, ObjectMapper objectMapper, ConfigurationService configurationService) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.configurationService = configurationService;
    }

    /**
     *
     * get google places result with latitude and longitude category of a place is mapped from openstreet
     *
     * @param lat latitude
     * @param lon longitude
     * @return
     * @throws IOException
     */
    public GoogleResult handle(String lat, String lon) throws IOException {

        String googleUriSearch = configurationService.getGOOGLE_SEARCH_NEARBY()
                .replace(Const.LATITUDE_REPLACEMENT_SHORTCUT, lat)
                .replace(Const.LONGITUDE_REPLACEMENT_SHORTCUT, lon);
                //.replace("@LONG", log);

        String googleResultStr = restTemplate.getForObject(
                googleUriSearch, String.class);

        LOGGER.debug(googleResultStr);

        return objectMapper.readValue(googleResultStr, GoogleResult.class);
    }
}
