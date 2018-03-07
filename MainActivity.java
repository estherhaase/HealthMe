package com.example.android.healthme;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, MedicalActivity.OnHospitalClickListener, StationActivity.OnStationClickListener {

    @BindView(R.id.drawer_layout)
            DrawerLayout drawer;
    @BindView(R.id.toolbar)
            Toolbar toolbar;
    @BindView(R.id.nav_view)
            NavigationView navigationView;

    private Location myLocation;
    private MyDataBaseHelper helper;
    SQLiteDatabase db;
    GoogleMap mGoogleMap;
    protected GoogleApiClient mGoogleApiClient;
    LocationManager mLocationManager;
    RequestQueue queue;
    VolleyControl controller;
    ArrayList<Integer>rbls, rbls2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        helper = MyDataBaseHelper.getsInstance(getApplicationContext());
        db = helper.getWritableDatabase();
        controller = VolleyControl.getInstance(getApplicationContext());
        queue = controller.getRequestQueue();
        rbls = new ArrayList<>();
        rbls2 = new ArrayList<>();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);
        initializeMap();
        buildGoogleApiClient();


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        FragmentManager manager = getFragmentManager();

        Fragment fragment = getFragmentManager().findFragmentById(R.id.content_frame);



        if (id == R.id.nav_map){

            if (fragment != null){
            getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.content_frame)).commit();
            }

            mGoogleMap.clear();


            String realtimeUrl = WienerLinenApi.buildWienerLinienMonitorUrl(rbls);
            makeRealTimeRequest(realtimeUrl);

        }

        else if (id == R.id.nav_hospital) {
            mGoogleMap.clear();

            manager.beginTransaction()
                    .replace(R.id.content_frame,
                            new MedicalActivity())
                    .commit();

        } else if (id == R.id.nav_station) {
            mGoogleMap.clear();

            manager.beginTransaction()
                    .replace(R.id.content_frame,
                            new StationActivity())
                    .commit();

        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    void makeRealTimeRequest(String url){
        StringRequest realtimeRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                JSONObject realtimeJSON;
                try {
                    realtimeJSON = new JSONObject(response);
                    JSONObject monitorData = realtimeJSON.getJSONObject("data");
                    JSONArray monitors = monitorData.getJSONArray("monitors");
                    //Testing variable
                    int test = monitors.length();


                    for (int i = 0; i < test; i++) {

                        String[] depTime = new String[2];
                        boolean barrierFree = false;
                        JSONObject currentMonitor = monitors.getJSONObject(i);

                        JSONArray coords = currentMonitor.getJSONObject("locationStop").getJSONObject("geometry").getJSONArray("coordinates");
                        Double lat = coords.getDouble(1);
                        Double lon = coords.getDouble(0);
                        String lineName = currentMonitor.getJSONArray("lines").getJSONObject(0).getString("name");
                        String lineDirection = currentMonitor.getJSONArray("lines").getJSONObject(0).getString("towards");
                        barrierFree = currentMonitor.getJSONArray("lines").getJSONObject(0).getBoolean("barrierFree");


                        for (int j = 0; j < currentMonitor.getJSONArray("lines").getJSONObject(0).getJSONObject("departures").getJSONArray("departure").length() && j < 2; j++) {

                            depTime[j] = currentMonitor.getJSONArray("lines").getJSONObject(0).getJSONObject("departures").getJSONArray("departure").getJSONObject(j).getJSONObject("departureTime").getString("countdown");

                        }
                        mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).title(lineName  + " " + lineDirection).snippet("Next Departures: " + depTime[0] + " | " + depTime[1]));

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }





            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(realtimeRequest);

    }

    protected void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private void initializeMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);
    }

    public void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            System.out.println("Location Manger Problem");
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            System.out.println("Location Manager started!");
            mLocationManager = (LocationManager)
                    getSystemService(Context.LOCATION_SERVICE);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        checkPermission();
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(2000);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    private void goToLocation(double lat, double lng,  float zoom) {
        LatLng ll = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
        mGoogleMap.moveCamera(update);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if(myLocation == null) {

            myLocation = new Location("");
            myLocation.setLatitude(location.getLatitude());
            myLocation.setLongitude(location.getLongitude());

            goToLocation(myLocation.getLatitude(), myLocation.getLongitude(), 17);
            mGoogleMap.clear();
            mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())));

            String url = WienerLinenApi.buildNearbyStationRequestUrl().toString();
            final String lat = Double.toString(myLocation.getLatitude());
            final String lon = Double.toString(myLocation.getLongitude());
            final String radius = "200";

            StringRequest getNearbyStationsRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray stations = jsonResponse.getJSONArray("stations");
                        for(int i=0; i<stations.length();i++){
                            rbls.add(stations.getJSONObject(i).getInt("RBL"));
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {

                    Map<String, String>  params = new HashMap<>();
                    params.put("latitude", lat);
                    params.put("longitude", lon);
                    params.put("radius", radius);

                    return params;

                }
            };
            queue.add(getNearbyStationsRequest);
        }



        //TODO: move this code to a refresh button, which destroys and recreates the activity; that takes care of the final variables needed for onResponse()
      /*  if(location.distanceTo(myLocation) > 10){
            myLocation = new Location("");
            myLocation.setLatitude(location.getLatitude());
            myLocation.setLongitude(location.getLongitude());

            goToLocation(myLocation.getLatitude(), myLocation.getLongitude(), 18);
            mGoogleMap.clear();
            mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())));
        }*/


    }



    @Override
    public void onMapReady(GoogleMap googleMap) {

        if(mGoogleMap == null){
            mGoogleMap = googleMap;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    @Override
    public void onHospitalSelected(Location selected) {

        getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.content_frame)).commit();

        mGoogleMap.clear();
        goToLocation(selected.getLatitude(), selected.getLongitude(), 18);
        mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(selected.getLatitude(), selected.getLongitude())));


    }


    @Override
    public void onStationSelected(ArrayList<Integer> rbls2) {

        getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.content_frame)).commit();
        mGoogleMap.clear();
        String url = WienerLinenApi.buildWienerLinienMonitorUrl(rbls2);
        makeRealTimeRequest(url);
    }
}
