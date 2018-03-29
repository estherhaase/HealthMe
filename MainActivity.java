package com.example.android.healthme;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, MedicalActivity.OnHospitalClickListener, StationActivity.OnStationClickListener, SplashActivity.OnStartAppListener {

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
    ArrayList<Integer> rbls, rbls2;
    MenuItem refreshButton;
    static ProgressDialog progressDialog;
    static int makeAsyncTasks = 0;
    private FusedLocationProviderClient mFusedLocationClient;
    ArrayList<Marker> markerList, markerList2;
    ArrayList<LatLng> routeRequestStationLocations;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;


    /**
     * The MainActivity holds and continuously displays the Google Map while the app is running
     * when first started, the users current position is saved in a Location Object and displayed (see onConnected())
     * It has a navigation drawer with three options (nearby stations, hospital search and station search)
     * The selection is handled in onNavigationItemSelected()
     * each selection is represented by a fragment
     * each fragment either displays locations or handles user input
     **/

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        progressDialog = new ProgressDialog(this);
        helper = MyDataBaseHelper.getsInstance(getApplicationContext());
        db = helper.getWritableDatabase();
        markerList = new ArrayList<>();
        routeRequestStationLocations = new ArrayList<>();

        /** The VolleyControl class makes sure that there is always only one instance of itself and of the RequestQueue which handles the network requests (singleton class)
         * It is recommended so that the RequestQueue lasts for the lifetime of the app and that the same RequestQueue is used when the activity is destroyed and recreated (like screen rotation)
         * **/
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

        //getFragmentManager().beginTransaction().replace(R.id.content_frame, new SplashActivity()).commit();

            initializeMap();
            buildGoogleApiClient();
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if(isConnected()){
            if (makeAsyncTasks == 1) {
                new GetHospitalInformationTask(MainActivity.this).execute(WienerLinenApi.buildHospitalDatabaseRequestUrl());
                new GetStationInformationTask(MainActivity.this).execute(WienerLinenApi.buildStationDatabaseRequestUrl());
            }
        }
        else {
            Toast.makeText(this, "Problem retrieving necessary data, please check internet connection and clear app data!", Toast.LENGTH_LONG).show();
        }


    }


    @Override
    public void onBackPressed() {
        //DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
        refreshButton = menu.getItem(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {

            Location location = new Location(" ");

            location.setLatitude(myLocation.getLatitude());
            location.setLongitude(myLocation.getLongitude());
            mGoogleMap.clear();
            Fragment fragment = getFragmentManager().findFragmentById(R.id.content_frame);
            if (fragment != null) {
                getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.content_frame)).commit();
            }
            goToLocation(location.getLatitude(), location.getLongitude(), 18);
            mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())));

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        /**
         * Handles navigation view item clicks via the FragmentManager
         *
         * **/

        int id = item.getItemId();
        FragmentManager manager = getFragmentManager();
        Fragment fragment = getFragmentManager().findFragmentById(R.id.content_frame);

        /**
         * When selected, the choice R.id.nav_map removes all fragments from the main activity clears any previous markers
         * it then makes a request for the realtime data from the Wiener Linien API
         * the required information is obtained in onLocationChanged() and stored in the ArrayList rbls
         * the URL needed to make the request is built with the WienerLinienApi class method buildWienerLinienMonitorUrl()
         * the request is made using the method makeRealTimeRequest()
         * **/

        if (id == R.id.nav_map) {

            if (fragment != null) {
                getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.content_frame)).commit();
            }

            mGoogleMap.clear();


            if (myLocation != null) {

                buildNearbyStationsRequest();
                // makeRealTimeRequest(WienerLinenApi.buildWienerLinienMonitorUrl(rbls));
            } else {
                Toast.makeText(this, "Position not available!", Toast.LENGTH_LONG).show();
            }


            /**
             * if R.id.nav_hospital is selected, the map is cleared and the Fragment MedicalActivity is started
             * */
        } else if (id == R.id.nav_hospital) {
            mGoogleMap.clear();

            manager.beginTransaction()
                    .replace(R.id.content_frame,
                            new MedicalActivity())
                    .commit();
            /**
             * if R.id.nav_station is selected, the map is cleared and the Fragment StationActivity is started
             * */
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

    /**
     * the method makeRealTimeRequest() uses the Volley Http Library to make networking requests, as it has significant advantages over AsyncTask
     * it simply sends a String Request with the parameters Request Method, URL as a String and Listeners, that check for Response and Error
     * the String Response converted to a JSONObject, which is then parsed for the necessary information
     * **/
    void buildNearbyStationsRequest(){

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
                    for (int i = 0; i < stations.length(); i++) {
                        rbls.add(stations.getJSONObject(i).getInt("RBL"));
                    }
                    makeRealTimeRequest(WienerLinenApi.buildWienerLinienMonitorUrl(rbls));

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

                Map<String, String> params = new HashMap<>();
                params.put("latitude", lat);
                params.put("longitude", lon);
                params.put("radius", radius);

                return params;

            }
        };
        queue.add(getNearbyStationsRequest);

    }

    void makeRealTimeRequest(String url) {

        StringRequest realtimeRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                JSONObject realtimeJSON;
                markerList.clear();

                try {
                    realtimeJSON = new JSONObject(response);
                    JSONObject monitorData = realtimeJSON.getJSONObject("data");
                    JSONArray monitors = monitorData.getJSONArray("monitors");
                    JSONArray coordsOnMap = monitors.getJSONObject(0).getJSONObject("locationStop").getJSONObject("geometry").getJSONArray("coordinates");
                    LatLng ll = new LatLng(coordsOnMap.getDouble(1), coordsOnMap.getDouble(0));
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
                        Marker marker = mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).title(lineName + " " + lineDirection).snippet(" " + depTime[0] + " | " + depTime[1]).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_red_station)));

                        markerList.add(marker);


                    }
                    goToLocation(ll.latitude, ll.longitude, 16);
                    markerList2 = joinOverlappingMarkers(markerList);
                    mGoogleMap.clear();
                    for (Marker marker : markerList2) {
                        mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude)).title(marker.getTitle()).snippet(marker.getSnippet()).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_red_station)));
                    }
                    mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {


                          /*  if (!marker.getPosition().equals(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()))) {
                                makeFootpathRequest(GoogleMapsApi.buildGoogleDirectionsUrl(myLocation.getLatitude(), myLocation.getLongitude(), marker.getPosition().latitude, marker.getPosition().longitude));



                            }*/
                            marker.showInfoWindow();

                            return true;
                        }
                    });
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

    ArrayList<Marker> joinOverlappingMarkers(ArrayList<Marker> markerList) {
        int c = 0;

        do {
            for (int i = 0; i < markerList.size() - 1; i++) {
                if (markerList.get(i).getPosition().equals(markerList.get(i + 1).getPosition())) {
                    Marker temp = markerList.get(i + 1);
                    Marker contemp = markerList.get(i);
                    String tempTitle = temp.getTitle();
                    String tempSnippet = temp.getSnippet();
                    String tempTitle2 = contemp.getTitle();
                    String tempSnippet2 = contemp.getSnippet();
                    contemp.setTitle(tempTitle2 + "\n" + tempTitle);
                    contemp.setSnippet(tempSnippet2 + "\n" + tempSnippet);

                    markerList.remove(i + 1);
                }

            }
            c++;
        } while (c < markerList.size());
        return markerList;
    }

    void makeFootpathRequest(String url) {

        StringRequest footpathRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                int i = response.length();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(footpathRequest);


    }

    protected void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);
    }

    public void checkPermission() {

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            System.out.println("Location Manger Problem");

                   } else {
            System.out.println("Location Manager started!");
            mLocationManager = (LocationManager)
                    getSystemService(Context.LOCATION_SERVICE);


        }

    }

    public void requestPermission() {

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            System.out.println("Requesting permissions");
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            System.out.println("Permissions already granted!");
            mLocationManager = (LocationManager)
                    getSystemService(Context.LOCATION_SERVICE);
             makeLocationRequest();
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        try {

            if(grantResults[0] == 0){
                makeLocationRequest();
            }else {
                //requestPermission();
                Toast.makeText(this, "Functionality of app is limited!", Toast.LENGTH_SHORT).show();

        }

        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }



    }

    void makeLocationRequest() {
            checkPermission();
            LocationRequest mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(10000);
            mLocationRequest.setFastestInterval(5000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest);
            SettingsClient client = LocationServices.getSettingsClient(this);
            Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
            task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    getMyLocation();
                }
            });

            task.addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (e instanceof ResolvableApiException) {
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(MainActivity.this,
                                    REQUEST_CHECK_SETTINGS);

                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                    }
                }
            });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CHECK_SETTINGS) {

            if (resultCode == RESULT_OK) {
                Log.i("onActivityResult: ", "if resultCode == RESULT_OK");
                getMyLocation();
            } else {
                Toast.makeText(this, "Location Services off", Toast.LENGTH_SHORT).show();
            }
        }
    }


    void getMyLocation() {
        checkPermission();
            Log.i("getMyLocation: ", "entered method");

            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Log.i("onSuccess: ", location.toString());
                            // Got last known location. In some rare situations this can be null.
                            if (location == null) {
                                Log.i("onSuccess: ", "location = null");
                                //  getMyLocation();
                            } else {
                                myLocation = new Location("");
                                myLocation.setLatitude(location.getLatitude());
                                myLocation.setLongitude(location.getLongitude());
                                mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())));
                                refreshButton.setVisible(true);
                                goToLocation(myLocation.getLatitude(), myLocation.getLongitude(), 17);
                                Log.i("onSuccess: ", location.toString());
                            }
                        }
                    });





    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
         requestPermission();
         //makeLocationRequest();



    }



    private void goToLocation(double lat, double lng, float zoom) {
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


    //TODO: sort out onLocationChanged
    @Override
    public void onLocationChanged(Location location) {
        if(myLocation != null){
            float distance = 0.0f;
            float realDistance = myLocation.distanceTo(location);

            if(getFragmentManager().findFragmentById(R.id.content_frame) != null){
                if(realDistance > distance){
                    mGoogleMap.clear();
                    myLocation.setLatitude(location.getLatitude());
                    myLocation.setLongitude(location.getLongitude());
                    mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())));
                    goToLocation(myLocation.getLatitude(), myLocation.getLongitude(), 17);
                }
            }
        }

        else {
            myLocation = new Location("");
            myLocation.setLatitude(location.getLatitude());
            myLocation.setLongitude(location.getLongitude());
            mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())));
            refreshButton.setVisible(true);
            goToLocation(myLocation.getLatitude(), myLocation.getLongitude(), 17);
        }


    }




    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (mGoogleMap == null) {
            mGoogleMap = googleMap;
        }
        mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View view = ( MainActivity.this).getLayoutInflater().inflate(R.layout.custom_info_window, null);
                TextView tvDeparture = view.findViewById(R.id.tv_departure_times);
                TextView tvLineNames = view.findViewById(R.id.tv_line_name);
                tvDeparture.setText(marker.getSnippet());
                tvLineNames.setText(marker.getTitle());

                return view;
            }
        });
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

    static class GetStationInformationTask extends AsyncTask<URL, Void, String[]> {

        WeakReference<MainActivity> myWeakReference;

        GetStationInformationTask(MainActivity context){
            myWeakReference = new WeakReference<>(context);
        }



        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.setIndeterminate(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected String[] doInBackground(URL... urls) {


            String response;

            try {
                response = WienerLinenApi.getHttpResponse(urls[0]);

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            try {
                JSONObject stationJSON = new JSONObject(response);
                JSONArray rbls = stationJSON.getJSONArray("rbls");
                JSONArray stations = stationJSON.getJSONArray("stations");

                for (int i = 0; i < stations.length(); i++) {
                    int tempId = Integer.parseInt(stations.getJSONObject(i).getString("HALTESTELLEN_ID"));
                    String tempName = stations.getJSONObject(i).getString("NAME");
                    int tempDiva = Integer.parseInt(stations.getJSONObject(i).getString("DIVA"));
                    myWeakReference.get().helper.addStation(tempId, tempName, tempDiva);
                }

                for (int j = 0; j < rbls.length(); j++) {
                    int rbl = Integer.parseInt(rbls.getJSONObject(j).getString("RBL_NUMMER"));
                    double lat = Double.parseDouble(rbls.getJSONObject(j).getString("STEIG_WGS84_LAT"));
                    double lon = Double.parseDouble(rbls.getJSONObject(j).getString("STEIG_WGS84_LON"));
                    int tempId = Integer.parseInt(rbls.getJSONObject(j).getString("FK_HALTESTELLEN_ID"));

                    myWeakReference.get().helper.addRbl(rbl, lat, lon, tempId);
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

            return new String[0];
        }

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);
            progressDialog.dismiss();
        }
    }

    static class GetHospitalInformationTask extends AsyncTask<URL, Void, String[]>{

        WeakReference<MainActivity> myWeakReference;

        GetHospitalInformationTask(MainActivity context){

            myWeakReference = new WeakReference<>(context);
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
           // ProgressDialog progressDialog = new ProgressDialog(myWeakReference.get());
            progressDialog.setIndeterminate(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(true);
            progressDialog.show();
        }


        @Override
        protected String[] doInBackground(URL... urls) {

            String response;

            try {
                response = WienerLinenApi.getHttpResponse(urls[0]);

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            try {

                JSONObject khJSON = new JSONObject(response);
                JSONArray khArray = khJSON.getJSONArray("hospitals");

                for( int i = 0; i<khArray.length(); i++){
                    JSONObject temp = khArray.getJSONObject(i);

                    String name = temp.getString("NAME");
                    String adress = temp.getString("ADRESSE");
                    int district = temp.getInt("BEZIRK");
                    Double lat = temp.getDouble("WGS84_LAT");
                    Double lon = temp.getDouble("WGS84_LON");

                    myWeakReference.get().helper.addHospital(name, adress, district, lat, lon);

                }

            } catch (JSONException e){
                e.printStackTrace();
            }

            return new String[0];
        }

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);
            progressDialog.dismiss();
        }
    }

    @Override
    public void onHospitalSelected(Location selected, String name) {

        getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.content_frame)).commit();

        mGoogleMap.clear();
        goToLocation(selected.getLatitude(), selected.getLongitude(), 18);
        mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(selected.getLatitude(), selected.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_red_hospital)));
        routeToSelectedHospitalRequest(myLocation, selected);


    }

    public void routeToSelectedHospitalRequest(final Location myLocation, final Location hospitalLocation){

        String url = WienerLinenApi.buildRoutesRequest(myLocation, hospitalLocation).toString();
        routeRequestStationLocations.clear();
        StringRequest routeRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject routeResponse = new JSONObject(response);
                    JSONObject trip = routeResponse.getJSONArray("trips").getJSONObject(0);
                    JSONArray legs = trip.getJSONObject("trip").getJSONArray("legs");


                    for(int i = 0; i < legs.length() - 1; i++) {
                        JSONObject point = legs.getJSONObject(i).getJSONArray("points").getJSONObject(1);
                        JSONArray stopSeq = legs.getJSONObject(i).getJSONArray("stopSeq");

                        for(int z = 0; z < stopSeq.length(); z++){
                            int diva = stopSeq.getJSONObject(z).getJSONObject("ref").getInt("id");
                            routeRequestStationLocations.add(helper.getStationLocation(helper.getStationId(diva)));
                        }
                      //  int diva = point.getJSONObject("ref").getInt("id");
                        //routeRequestStationLocations.add(helper.getStationLocation(helper.getStationId(diva)));

                    }
                    drawPolyline(routeRequestStationLocations);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(routeRequest);

    }

    public void drawPolyline(ArrayList<LatLng> list){

        LatLngBounds.Builder builder = new LatLngBounds.Builder();



        for(int i= 0; i < list.size() - 1; i++) {
            LatLng lng = new LatLng(list.get(i).latitude, list.get(i).longitude);
            LatLng lng1 = new LatLng(list.get(i+1).latitude, list.get(i+1).longitude);
            mGoogleMap.addPolyline(new PolylineOptions().add(lng, lng1).color(getResources().getColor(R.color.logoBlue)));


        }
        builder.include(list.get(0));
        builder.include(list.get(list.size()-1));
        LatLngBounds bounds = builder.build();
        int padding = 50; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mGoogleMap.moveCamera(cu);

    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onStationSelected(ArrayList<Integer> rbls2) {

        getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.content_frame)).commit();
        mGoogleMap.clear();
        String url = WienerLinenApi.buildWienerLinienMonitorUrl(rbls2);
        makeRealTimeRequest(url);
    }

    @Override
    public void onStartApp() {

        getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.content_frame)).commit();
        //refreshButton.setVisible(true);
        //checkPermission();
        //makeLocationRequest();

    }
}
