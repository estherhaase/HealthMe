package com.example.android.healthme.Api;

import com.example.android.healthme.DataModel.Data;
import com.example.android.healthme.DataModel.Trip;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;


public interface DirectionsService {

    String BASE_URL =  "https://maps.googleapis.com/maps/api/directions/";



    @GET("XML_TRIP_REQUEST2")
    Call<Data> getTrips(@QueryMap Map<String, String> options);


}
