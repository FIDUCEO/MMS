package com.bc.fiduceo;

import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.db.QueryParameter;
import com.bc.fiduceo.db.Storage;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.GeometryUtil;
import com.bc.fiduceo.geometry.TimeAxis;
import com.bc.fiduceo.util.TimeUtils;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

public class TestDeleteMe {

    @Test
    @Ignore
    public void testAvhrrFRAC_fromDB() throws SQLException {
        final BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://localhost:5432/test");
        dataSource.setUsername("fiduceo");
        dataSource.setPassword("oecudif");

        /*
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://udb1.jasmin.ac.uk:5432/fiduceo");
        dataSource.setUsername("fiduceo");
        dataSource.setPassword("rtjhYythwrtN");
         */

        final GeometryFactory geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        final Storage storage = Storage.create(dataSource, geometryFactory);
        //storage.initialize();

        try {
            final QueryParameter parameter = new QueryParameter();
            parameter.setSensorName("avhrr-frac-ma");
//            parameter.setStartTime(TimeUtils.parse("2019-09-25 00:00:00", "yyyy-MM-dd HH:mm:ss"));
//            parameter.setStopTime(TimeUtils.parse("2019-09-25 23:59:59", "yyyy-MM-dd HH:mm:ss"));

            final List<SatelliteObservation> satelliteObservations = storage.get(parameter);
            System.out.println("num results: " + satelliteObservations.size());
            for (final SatelliteObservation obs : satelliteObservations) {
                final Path fileName = obs.getDataFilePath().getFileName();
                System.out.println(fileName);

                final Geometry geoBounds = obs.getGeoBounds();
                final Geometry[] subGeometries = GeometryUtil.getSubGeometries(geoBounds);

                final TimeAxis[] timeAxes = obs.getTimeAxes();
                if (subGeometries.length != timeAxes.length) {
                    System.out.println("!!!!!!!! DISCREPANCY !!!!!!!!!!!!!!!!!");
                }

                System.out.println("---------------------------------------------------------");
            }
        } finally {
            storage.close();
        }
    }
}
