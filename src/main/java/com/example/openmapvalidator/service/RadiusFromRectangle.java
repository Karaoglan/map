package com.example.openmapvalidator.service;

import com.example.openmapvalidator.model.google.GoogleResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

public class RadiusFromRectangle {

    public static void main(String[] args) throws IOException {

        new RadiusFromRectangle().main2();
    }

    public void main2() throws IOException {


        //<bounds minlat="48.1789100" minlon="16.3248500" maxlat="48.1801300" maxlon="16.3277300"/>

        //maxlat, minlon      maxlat, maxlon
        //minlat, minlon      minlat, maxlon

        double heightLatitudeUp = 48.1801300;
        double heightLongitudeUp = 16.3248500;

        double heightLatitudeDown = 48.1789100;
        double heightLongitudeDown = 16.3248500;

        double widthLatitudeLeft = 48.1789100;
        double widthLongitudeLeft = 16.3248500;
        double widthLatitudeRight = 48.1789100;
        double widthLongitudeRight = 16.3277300;

        LatLng heightPointUp = new LatLng(heightLatitudeUp, heightLongitudeUp);
        LatLng heightPointDown = new LatLng(heightLatitudeDown, heightLongitudeDown);
        double height = LatLngTool.distance(heightPointUp, heightPointDown, LengthUnit.METER);

        LatLng widthPointLeft = new LatLng(widthLatitudeLeft, widthLongitudeLeft);
        LatLng widthPointRight = new LatLng(widthLatitudeRight, widthLongitudeRight);
        double width = LatLngTool.distance(widthPointLeft, widthPointRight, LengthUnit.METER);

        System.out.println("width : " + width);
        System.out.println("height : " + height);

        double radius = 0.5 * Math.sqrt(width * width + height * height);

        /*
        (2 * s^2)^(1/2)
        (2 * 500^2)^(1/2) ~= 707
        The diagonal of this square is the diameter of the circumscribing circle. To get the radius, we divide by 2:

        707 / 2 ~= 353*/


        System.out.println("radius : " + radius);

        double bearingWidth = LatLngTool.initialBearing(widthPointLeft, widthPointRight);
        double bearingHeight = LatLngTool.initialBearing(heightPointUp, heightPointDown);

        LatLng widthMidPoint = LatLngTool.travel(widthPointLeft, bearingWidth, width / 2, LengthUnit.METER);
        LatLng heightMidPoint = LatLngTool.travel(heightPointUp, bearingHeight, height / 2, LengthUnit.METER);

        System.out.println(heightMidPoint.getLatitude() + ", " + widthMidPoint.getLongitude());



        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=48.179519," +
                "16.326289&radius=50&key=AIzaSyB3juajX9XgIufeRCrOwpY1WRixHMQ9HSk";

        String orjUrlWithNextPage = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=48.179519," +
                "16.326289&radius=50&key=AIzaSyB3juajX9XgIufeRCrOwpY1WRixHMQ9HSk&pagetoken=@PAGE_TOKEN";


        String googleResultStr = new RestTemplate().getForObject(
                url, String.class);

        System.out.println(googleResultStr);

        GoogleResult result = new ObjectMapper().readValue(googleResultStr, GoogleResult.class);
        int count = result.getResults().size();

        while (result.getNext_page_token() != null) {
            System.out.println("***************** next token : " + result.getNext_page_token());
            System.out.println("----------------------");

            synchronized(this) {
                String urlWithNextPage = orjUrlWithNextPage.replace("@PAGE_TOKEN", result.getNext_page_token());

                googleResultStr = new RestTemplate().getForObject(
                        urlWithNextPage, String.class);

                result = new ObjectMapper().readValue(googleResultStr, GoogleResult.class);
                System.out.println("count in while before adding : " + count);
                count += result.getResults().size();
            }


            System.out.println("++++++++++++++next token cikis : " + result.getNext_page_token());
        }

        System.out.println(count);
    }
}
