package com.example.openmapvalidator.service.request;

import com.example.openmapvalidator.helper.Const;
import com.example.openmapvalidator.model.microsoft.MicrosoftResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
public class MicrosoftRequestHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MicrosoftRequestHandler.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public MicrosoftRequestHandler(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * get detail of a place which is near to given latitude and longitude
     *
     * its just applied to USA but in any short time other countries will added
     *
     * @param lat latitude and @param log longitude
     */
    public MicrosoftResult handle(String lat, String log) throws IOException {

        String microsoftUriSearch = Const.MICROSOFTMAP_SEARCH_WITH_LONG.replace("@LAT", lat)
                .replace("@LOG", log);

        String microsoftResultStr = restTemplate.getForObject(
                microsoftUriSearch, String.class);

        LOGGER.debug(microsoftResultStr);

        return objectMapper.readValue(microsoftResultStr, MicrosoftResult.class);
    }
}
