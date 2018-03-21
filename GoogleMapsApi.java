package com.example.android.healthme;

import android.net.Uri;
import android.support.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import static com.example.android.healthme.BuildConfig.API_KEY2;

class GoogleMapsApi {

    private final static String MAPS_AUTHORITY = "maps.googleapis.com";
    private final static String MAPS_PATH = "maps";
    private final static String API_PATH = "api";
    private final static String JSON_PATH = "json";
    private final static String DIRECTIONS_PATH = "directions";
    private final static String ORIGIN_PARAM = "origin";
    private final static String DESTINATION_PARAM = "destination";
    private final static String KEY_PARAM = "key";
    private final static String MODE_PARAM = "mode";
    private final static String MODE_VALUE = "walking";


    static String buildGoogleDirectionsUrl(Double originLat, Double originLon, Double destLat, Double destLon){

        Uri directionsUri;
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .encodedAuthority(MAPS_AUTHORITY)
                .appendEncodedPath(MAPS_PATH)
                .appendEncodedPath(API_PATH)
                .appendEncodedPath(DIRECTIONS_PATH)
                .appendEncodedPath(JSON_PATH)
                .appendQueryParameter(ORIGIN_PARAM, originLat.toString() + ", " + originLon.toString())
                .appendQueryParameter(DESTINATION_PARAM, destLat.toString() + ", " + destLon.toString())
                .appendQueryParameter(MODE_PARAM, MODE_VALUE)
                .appendQueryParameter(KEY_PARAM, API_KEY2);

        directionsUri = builder.build();

        URL url = null;
        try {
            url = new URL(directionsUri.toString());
        }catch (MalformedURLException e){
            e.printStackTrace();
        }

        assert url != null;
        return url.toString();
    }

    @Nullable
    static String getHttpResponse(URL url) throws IOException {

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if(hasInput){
                return scanner.next();
            }else {
                return null;
            }

        } finally {
            urlConnection.disconnect();
        }
    }
}
