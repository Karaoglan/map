package com.example.openmapvalidator.helper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;


@Configuration
@ComponentScan(basePackages = { "com.example.*" })
@PropertySource({"classpath:config.properties", "classpath:application.properties"})
public class Const {

    public static final double SIMILARITY_SCORE = 0.8;

    public static final String OS_NAME = "os.name";
    public static final String OS_WINDOWS_NAME = "windows";
    public static final String BASH_SCRIPTS_ROOT = "../../../bashscript/";
    public static final String OSM_WINDOWS_ROOT = "windows/osm2pgsql-bin/";
    public static final String OSM_UNIX_ROOT = "unix/osm2pgsql/bin/";
    public static final String OSM_FILE_DIR = "map";
    public static final String OSM_PSQL_PLACE_SELECT_QUERY_IDENTIFIER = "selectPlaces";


    public static String OPENSTREET_URI_GET_LONG_WITH_OSM_ID;
    public static String GOOGLE_SEARCH_NEARBY;
    public static String FOURSQUARE_URI_SEARCH_WITH_LONG;
    public static String MICROSOFTMAP_SEARCH_WITH_LONG;

    public static String OSM_COMMAND;
    public static String OSM_COMMAND_CREATE_OPTION;
    public static String OSM_COMMAND_DATABASE_OPTION;
    public static String OSM_COMMAND_DATABASE_ARGUMENT;
    public static String OSM_COMMAND_USERNAME_OPTION;

    public static String PSQL_USERNAME;

    @Value("${microsoftmap.searchWithLong}")
    public void setMicrosoftmapSearchWithLong(String microsoftMapSearchWithLong) {
        MICROSOFTMAP_SEARCH_WITH_LONG = microsoftMapSearchWithLong;
    }

    @Value("${googlemap.search.nearby}")
    public void setGoogleSearchNearby(String googleSearchNearby) {
        GOOGLE_SEARCH_NEARBY = googleSearchNearby;
    }

    @Value("${spring.datasource.username}")
    public void setPsqlUsername(String psqlUsername) {
        PSQL_USERNAME = psqlUsername;
    }

    @Value("${osm.command.username.option}")
    public void setOsmCommandUsernameOption(String usernameOption) {
        OSM_COMMAND_USERNAME_OPTION = usernameOption;
    }

    @Value("${openstreet.getlongwithosmid}")
    public void setOpenstreetUriGetLongWithOsmId(String openWithLong) {
        OPENSTREET_URI_GET_LONG_WITH_OSM_ID = openWithLong;
    }

    @Value("${foursquare.searchplacewithlong}")
    public void setFoursquareUriSearchWithLong(String searchWithLong) {
        FOURSQUARE_URI_SEARCH_WITH_LONG = searchWithLong;
    }

    @Value("${osm.command}")
    public void setOsmCommand(String osmCommand) {
        OSM_COMMAND = osmCommand;
    }

    @Value("${osm.command.create.option}")
    public void setOsmCommandCreateOption(String osmCommandCreateOption) {
        OSM_COMMAND_CREATE_OPTION = osmCommandCreateOption;
    }
    @Value("${osm.command.database.option}")
    public void setOsmCommandDatabaseOption(String osmCommandDatabaseOption) {
        OSM_COMMAND_DATABASE_OPTION = osmCommandDatabaseOption;
    }
    @Value("${osm.command.database.argument}")
    public void setOsmCommandDatabaseArgument(String osmCommandDatabaseArgument) {
        OSM_COMMAND_DATABASE_ARGUMENT = osmCommandDatabaseArgument;
    }
}
