package com.example.openmapvalidator.service.statistic;

import com.example.openmapvalidator.helper.Const;
import com.example.openmapvalidator.service.database.DatabaseSession;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OpenmapRequestHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenmapRequestHandler.class);

    private final DatabaseSession databaseSession;

    @Autowired
    public OpenmapRequestHandler(DatabaseSession databaseSession) {
        this.databaseSession = databaseSession;
    }

    public int countOpenmapPlaces() throws IOException {

        SqlSession session = databaseSession.getDBSession();
        Integer count = session.selectOne(Const.OSM_PSQL_PLACE_COUNT_QUERY_IDENTIFIER);

        LOGGER.debug("Openmap statistic request handler returns count : {}", count);
        return count;
    }

}
