package com.example.openmapvalidator.service.examples;

import com.example.openmapvalidator.model.google.GoogleResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.maps.GeoApiContext;
import com.google.maps.NearbySearchRequest;
import com.google.maps.PendingResult;
import com.google.maps.PlacesApi;
import com.google.maps.model.PlacesSearchResponse;
import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

public class GoogleJavaRequest {
    public static void main(String[] args) throws IOException {

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

            String urlWithNextPage = orjUrlWithNextPage.replace("@PAGE_TOKEN", result.getNext_page_token());

            System.out.println("**************!!!!!!!!!!with next page token : " + urlWithNextPage);

            String googleResultStrWithNext = new RestTemplate().getForObject(
                     urlWithNextPage, String.class);

            result = new ObjectMapper().readValue(googleResultStrWithNext, GoogleResult.class);
            System.out.println("count in while before adding : " + count);
            System.out.println("size before adding: " + result.getResults().size());
            count += result.getResults().size();


            System.out.println("++++++++++++++next token cikis : " + result.getNext_page_token());
        }

        System.out.println(count);


        /*GeoApiContext context = new GeoApiContext.Builder()
                .apiKey("AIzaSyB3juajX9XgIufeRCrOwpY1WRixHMQ9HSk")
                .build();

        com.google.maps.model.LatLng bony = new com.google.maps.model.LatLng(48.17951,16.326289);

        NearbySearchRequest request = PlacesApi.nearbySearchQuery(context, bony);

        // Synchronous
        try {
            request.await();


            System.out.println("handle succ. request");
        } catch (Exception e) {
            // Handle error
            System.out.println("handle error. request");
        }

        //request.awaitIgnoreError(); // No checked exception.

        request.setCallback(
                new PendingResult.Callback<PlacesSearchResponse>() {
                    @Override
                    public void onResult(PlacesSearchResponse placesSearchResponse) {
                        if (!placesSearchResponse.nextPageToken.isEmpty()) {
                            System.out.println("another call");
                        }
                        System.out.println(placesSearchResponse);
                    }

                    @Override
                    public void onFailure(Throwable throwable) {

                        System.out.println("fail " + throwable.getMessage());
                    }
                }
        );

        request.radius(50);

        System.out.println();

        //NearbySearchRequest.Response*/

        // Async
        /*request.setCallback(new PendingResult.Callback<Deneme>() {
            @Override
            public void onResult(Deneme[] result) {
                // Handle successful request.
            }

            @Override
            public void onFailure(Throwable e) {
                // Handle error.
            }
        });*/


        //System.out.println(request);

        //new RadiusFromRectangle().main2();

        /*String orjUrlWithNextPage = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=48.179519," +
                "16.326289&radius=50&key=AIzaSyB3juajX9XgIufeRCrOwpY1WRixHMQ9HSk&pagetoken=@PAGE_TOKEN";

        String urlWithNextPage = orjUrlWithNextPage.replace("@PAGE_TOKEN", "CqQCGQEAAK1VlmKIxAkZ7iSdxZDFAqXBCYKXzw9igxK2DnNjDATO2uNgUCAhQ8s_RBHyD4z2eOMNok00XsKikj3areYKpDJomLy0RIE_51ZJYaHvuf06SeLStr1RfxbH51HmtmqedePxyzbEpU3GUkYyHL3DJYji-8dwb4P3qiO2ZJVd7QCmMLfTWoV1VUILTxqH05nO2jak6i3_MW2jPWSPR7CFDS6DKTQnm4DoN8qERVqOszPCy8dWOz1_uRkZypRGUFoa-mucb-hrN_fPDbX8O3Bay_PnKOQcz8a4fmaWYSNPm8OYKxr-SbLNeSLdtw1HCBzVM7rcIqFbSt-AxYoL8pqbI6FsNYFMUKm9z5AsXk5mAKyWyXoKDiCRoyyBJ6g3FIDHZxIQAk7BmmHSZqyShU_b0CWsyBoUSo_HdsU8tHBVWnK_yRFfwnQBt7E");


        String googleResultStr = new RestTemplate().getForObject(
                urlWithNextPage, String.class);

        System.out.println(googleResultStr);

        GoogleResult result = new ObjectMapper().readValue(googleResultStr, GoogleResult.class);
        int count = result.getResults().size();
        System.out.println(count);*/
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
    }
}
