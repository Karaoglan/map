package com.example.openmapvalidator.helper;

import org.springframework.beans.factory.annotation.Value;

import java.io.File;

public class Const {

    public static final double SIMILARITY_SCORE = 0.65;
    public static final String SPACE = " ";
    public static final String OSM_STYLE = "default.style";

    public static final String OS_NAME = "os.name";
    public static final String OS_WINDOWS_NAME = "windows";
    public static final String MAP_FOLDER_ROOT = "map";
    public static final String BASH_SCRIPTS_ROOT = ".." + File.separator + ".." + File.separator + ".." + File.separator
            + "bashscript";
    public static final String OSM_WINDOWS_ROOT = File.separator + "bashscript" + File.separator + "windows" + File
            .separator + "osm2pgsql-bin";
    public static final String OSM_UNIX_ROOT = File.separator + "bashscript" + File.separator + "unix" + File.separator + "osm2pgsql-ux";
    public static final String OSM_PSQL_PLACE_SELECT_QUERY_IDENTIFIER = "selectPlaces";

    public static final String OSM_COMMAND = "osm2pgsql";
    public static final String OSM_COMMAND_CREATE_OPTION = "--create";
    public static final String OSM_COMMAND_DATABASE_OPTION = "--database";
    public static final String OSM_COMMAND_USERNAME_OPTION = "-U";
    public static final String OSM_DEFAULT_STYLE_OPTION = "-S";

}
