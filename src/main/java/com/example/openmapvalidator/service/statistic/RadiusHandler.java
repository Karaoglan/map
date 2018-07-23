package com.example.openmapvalidator.service.statistic;

import com.example.openmapvalidator.model.open.GeographicRectangle;
import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


/**
 * @author Sanan.Ahmadzada
 */
@Service
public class RadiusHandler {

    public Map<String, Double> handle(GeographicRectangle rectangle) {

        if (rectangle == null) {
            return null;
        }

        Map<String, Double>  geographicValueMap = new HashMap<>();

        //<bounds minlat="48.1789100" minlon="16.3248500" maxlat="48.1801300" maxlon="16.3277300"/>

        //maxlat, minlon      maxlat, maxlon
        //minlat, minlon      minlat, maxlon

        double heightLatitudeUp = rectangle.getMaxLatitude();
        double heightLongitudeUp = rectangle.getMinLongitude();

        double heightLatitudeDown = rectangle.getMinLatitude();
        double heightLongitudeDown = rectangle.getMinLongitude();

        double widthLatitudeLeft = rectangle.getMinLatitude();
        double widthLongitudeLeft = rectangle.getMinLongitude();
        double widthLatitudeRight = rectangle.getMinLatitude();
        double widthLongitudeRight = rectangle.getMaxLongitude();

        LatLng heightPointUp = new LatLng(heightLatitudeUp, heightLongitudeUp);
        LatLng heightPointDown = new LatLng(heightLatitudeDown, heightLongitudeDown);
        double height = LatLngTool.distance(heightPointUp, heightPointDown, LengthUnit.METER);

        LatLng widthPointLeft = new LatLng(widthLatitudeLeft, widthLongitudeLeft);
        LatLng widthPointRight = new LatLng(widthLatitudeRight, widthLongitudeRight);
        double width = LatLngTool.distance(widthPointLeft, widthPointRight, LengthUnit.METER);

        System.out.println("width : " + width);
        System.out.println("height : " + height);

        double radius = 0.5 * Math.sqrt(width * width + height * height);

        System.out.println("radius : " + radius);
        geographicValueMap.put("radius", radius);

        double bearingWidth = LatLngTool.initialBearing(widthPointLeft, widthPointRight);
        double bearingHeight = LatLngTool.initialBearing(heightPointUp, heightPointDown);

        LatLng widthMidPoint = LatLngTool.travel(widthPointLeft, bearingWidth, width / 2, LengthUnit.METER);
        LatLng heightMidPoint = LatLngTool.travel(heightPointUp, bearingHeight, height / 2, LengthUnit.METER);

        geographicValueMap.put("latitude", heightMidPoint.getLatitude());
        geographicValueMap.put("longitude", widthMidPoint.getLongitude());

        System.out.println(heightMidPoint.getLatitude() + ", " + widthMidPoint.getLongitude());
        return geographicValueMap;
    }
}
