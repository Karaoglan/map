package com.example.openmapvalidator.helper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

@Service
@ComponentScan(basePackages = { "com.example.*" })
@PropertySource({"classpath:config.properties", "classpath:application.properties"})
public class ConfigurationService {

    @Value("${openstreet.getlongwithosmid}")
    private String OPENSTREET_URI_GET_LONG_WITH_OSM_ID;

    @Value("${googlemap.search.nearby}")
    private String GOOGLE_SEARCH_NEARBY;

    @Value("${foursquare.searchplacewithlong}")
    private String FOURSQUARE_URI_SEARCH_WITH_LONG;

    @Value("${microsoftmap.searchWithLong}")
    private String MICROSOFTMAP_SEARCH_WITH_LONG;

    @Value("${spring.datasource.username}")
    private String PSQL_USERNAME;

    @Value("${osm.command.database.argument}")
    private String OSM_COMMAND_DATABASE_ARGUMENT = "map-db";

    public String getOSM_COMMAND_DATABASE_ARGUMENT() {
        return OSM_COMMAND_DATABASE_ARGUMENT;
    }

    public void setOSM_COMMAND_DATABASE_ARGUMENT(String OSM_COMMAND_DATABASE_ARGUMENT) {
        this.OSM_COMMAND_DATABASE_ARGUMENT = OSM_COMMAND_DATABASE_ARGUMENT;
    }

    public String getOPENSTREET_URI_GET_LONG_WITH_OSM_ID() {
        return OPENSTREET_URI_GET_LONG_WITH_OSM_ID;
    }

    public void setOPENSTREET_URI_GET_LONG_WITH_OSM_ID(String OPENSTREET_URI_GET_LONG_WITH_OSM_ID) {
        this.OPENSTREET_URI_GET_LONG_WITH_OSM_ID = OPENSTREET_URI_GET_LONG_WITH_OSM_ID;
    }

    public String getGOOGLE_SEARCH_NEARBY() {
        return GOOGLE_SEARCH_NEARBY;
    }

    public void setGOOGLE_SEARCH_NEARBY(String GOOGLE_SEARCH_NEARBY) {
        this.GOOGLE_SEARCH_NEARBY = GOOGLE_SEARCH_NEARBY;
    }

    public String getFOURSQUARE_URI_SEARCH_WITH_LONG() {
        return FOURSQUARE_URI_SEARCH_WITH_LONG;
    }

    public void setFOURSQUARE_URI_SEARCH_WITH_LONG(String FOURSQUARE_URI_SEARCH_WITH_LONG) {
        this.FOURSQUARE_URI_SEARCH_WITH_LONG = FOURSQUARE_URI_SEARCH_WITH_LONG;
    }

    public String getMICROSOFTMAP_SEARCH_WITH_LONG() {
        return MICROSOFTMAP_SEARCH_WITH_LONG;
    }

    public void setMICROSOFTMAP_SEARCH_WITH_LONG(String MICROSOFTMAP_SEARCH_WITH_LONG) {
        this.MICROSOFTMAP_SEARCH_WITH_LONG = MICROSOFTMAP_SEARCH_WITH_LONG;
    }

    public String getPSQL_USERNAME() {
        return PSQL_USERNAME;
    }

    public void setPSQL_USERNAME(String PSQL_USERNAME) {
        this.PSQL_USERNAME = PSQL_USERNAME;
    }
}
