package com.example.android.healthme;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.android.healthme.Api.DirectionsService;
import com.example.android.healthme.Api.ServiceGenerator;
import com.example.android.healthme.DataModel.Data;
import com.example.android.healthme.DataModel.Leg;
import com.example.android.healthme.DataModel.Trip;
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
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, MedicalActivity.OnHospitalClickListener, StationActivity.OnStationClickListener {

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.number_picker)
    NumberPicker numberPicker;


    private final String LOG_TAG = this.getClass().toString();
    private Location myLocation;
    private MyDataBaseHelper helper;
    SQLiteDatabase db;
    GoogleMap mGoogleMap;
    protected GoogleApiClient mGoogleApiClient;
    LocationManager mLocationManager;
    LocationRequest mLocationRequest;
    RequestQueue queue;
    VolleyControl controller;
    ArrayList<Integer> rbls, rbls2;
    MenuItem refreshButton;
    static ProgressDialog progressDialog, progressDialog2;
    static int makeAsyncTasks = 0;
    private FusedLocationProviderClient mFusedLocationClient;
    ArrayList<Marker> markerList, markerList2;
    ArrayList<LatLng> routeRequestStationLocations, secondFootpath, firstFootpath, points;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    ArrayList<JourneyLeg> footpathsLegs, rideLegs;
    List<Trip> trips;
    ArrayList<Leg> footpathLegs2, rideLegs2;
    SharedPreferences sharedPreferences;





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
        Log.i(LOG_TAG, "Lifecycle: onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        fab.setVisibility(View.INVISIBLE);
        rbls = new ArrayList<>();
        rbls2 = new ArrayList<>();
        points = new ArrayList<>();
        helper = MyDataBaseHelper.getsInstance(getApplicationContext());
        rideLegs = new ArrayList<>();
        rideLegs2 = new ArrayList<>();
        markerList = new ArrayList<>();
        controller = VolleyControl.getInstance(getApplicationContext());
        db = helper.getWritableDatabase();
        numberPicker.setVisibility(View.INVISIBLE);
        footpathLegs2 = new ArrayList<>();
        firstFootpath = new ArrayList<>();
        footpathsLegs = new ArrayList<>();
        secondFootpath = new ArrayList<>();
        progressDialog = new ProgressDialog(this);
        progressDialog2 = new ProgressDialog(this);
        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        queue = controller.getRequestQueue();
        routeRequestStationLocations = new ArrayList<>();

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
              //  new GetHospitalInformationTask(MainActivity.this).execute(WienerLinenApi.buildHospitalDatabaseRequestUrl());
                //new GetStationInformationTask(MainActivity.this).execute(WienerLinenApi.buildStationDatabaseRequestUrl());
                new GetHospitalInformationTask(MainActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, WienerLinenApi.buildHospitalDatabaseRequestUrl());
                new GetStationInformationTask(MainActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, WienerLinenApi.buildStationDatabaseRequestUrl());
            }
        }
        else {
            Toast.makeText(this, "Problem retrieving necessary data, please check internet connection and clear app data!", Toast.LENGTH_LONG).show();
        }

    }

    /**
     * saves the state of the location, so when retrieved from background, the app doesn't set a new marker
    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        if(myLocation!= null){

            outState.putDoubleArray("myLocation", new double[] {myLocation.getLatitude(), myLocation.getLongitude()});
        }
    }*/



    @Override
    public void onBackPressed() {
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

    /**
     * The app bar holds one option item, the position marker which brings the user to the map with his/her current position shown by a marker*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {

            makeLocationRequest();
           // Location location = new Location(" ");

            //  location.setLatitude(myLocation.getLatitude());
            //  location.setLongitude(myLocation.getLongitude());
            mGoogleMap.clear();
            numberPicker.setVisibility(View.INVISIBLE);
            fab.setVisibility(View.INVISIBLE);
            Fragment fragment = getFragmentManager().findFragmentById(R.id.content_frame);
            if (fragment != null) {
                getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.content_frame)).commit();
            }
            //  goToLocation(location.getLatitude(), location.getLongitude(), 18);
            //  mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())));

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Handles navigation view item clicks
     *
     * **/
    @SuppressWarnings("StatementWithEmptyBody")
    @Override

    public boolean onNavigationItemSelected(MenuItem item) {



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
                removeLocationUpdates();
            }

            mGoogleMap.clear();
            fab.setVisibility(View.INVISIBLE);
            numberPicker.setVisibility(View.INVISIBLE);


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
            removeLocationUpdates();
            manager.beginTransaction()
                    .replace(R.id.content_frame,
                            new MedicalActivity()).addToBackStack("hospitalList")
                    .commit();
            /**
             * if R.id.nav_station is selected, the map is cleared and the Fragment StationActivity is started
             * */
        } else if (id == R.id.nav_station) {
            mGoogleMap.clear();
            removeLocationUpdates();
            fab.setVisibility(View.INVISIBLE);
            numberPicker.setVisibility(View.INVISIBLE);
            manager.beginTransaction()
                    .replace(R.id.content_frame,
                            new StationActivity()).addToBackStack("stationSearch")
                    .commit();


        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * two AsyncTasks, handle the feeding of the internal SQLite database during the initial installation*/

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
            progressDialog.setTitle("Writing to storage...");
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
                    String platform = rbls.getJSONObject(j).getString("STEIG");


                    myWeakReference.get().helper.addRbl(rbl, lat, lon, tempId, platform);
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
            progressDialog2.setIndeterminate(false);
            progressDialog2.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog2.setCancelable(true);
            progressDialog2.setTitle("Writing to storage...");
            progressDialog2.show();
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
            progressDialog2.dismiss();
        }
    }
    /**
     * the following two requests use Google's Volley Http Library to make networking requests, as it has significant advantages over AsyncTask
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

    /**
     * This method is defined as an interface in the activity_medical fragment*/
    @Override
    public void onHospitalSelected(Location selected, String name) {

        getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.content_frame)).commit();


        mGoogleMap.clear();
        // goToLocation(selected.getLatitude(), selected.getLongitude(), 18);
        mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(selected.getLatitude(), selected.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_red_hospital)).title(name));
        mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(myLocation.getLatitude(), myLocation.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_red_position)));

        if(isConnected()){
            // routeToSelectedHospitalRequest(myLocation, selected);
            routeToHospital(myLocation, selected, name);
        }else {
            Toast.makeText(getApplicationContext(), "Internet connection necessary for routing!", Toast.LENGTH_LONG).show();
        }
        fab.setVisibility(View.VISIBLE);


    }

    /**
     * routToHospital() uses the networking library Retrofit by Square to request the routing response and maps the information to POJOs*/

    public void routeToHospital(final Location myLocation, final Location hospitalLocation, final String name){

        DirectionsService directionsService = ServiceGenerator.createService(DirectionsService.class);
        Map<String, String> map = new HashMap<>();

        /*map.put("origin", Double.toString(myLocation.getLatitude()) + ", " + Double.toString( myLocation.getLongitude()) );
        map.put("destination",Double.toString(hospitalLocation.getLatitude()) + ", " + Double.toString( hospitalLocation.getLongitude()) );
        map.put("mode", "walking");
        map.put("key", API_KEY2);*/

        map.put("outputFormat", "JSON");
        map.put("type_origin", "coord");
        map.put("name_origin", Double.toString( myLocation.getLongitude()) + ":" + Double.toString(myLocation.getLatitude()) + ":WGS84");
        map.put("type_destination", "coord");
        map.put("name_destination", Double.toString( hospitalLocation.getLongitude()) + ":" + Double.toString(hospitalLocation.getLatitude()) + ":WGS84");



        Call<Data> call = directionsService.getTrips(map);

        trips = new ArrayList<>();
        final Data[] data = new Data[1];
        final SharedPreferences.Editor editor = sharedPreferences.edit();


        call.enqueue(new Callback<Data>() {
            @Override
            public void onResponse(Call<Data> call, retrofit2.Response<Data> response) {
                Log.d(LOG_TAG, "onResponse: " + response.body());
                int error = response.code();
                data[0] = response.body();
                assert data[0] != null;
                trips = data[0].getTrips();
                numberPicker.setMinValue(0);
                int size = trips.size()-1;
                numberPicker.setMaxValue(size);
                /*int size = trips.size();
                editor.putInt("tripNo", 0);
                editor.putInt("size", size);
                editor.commit();
                getRouteFromData(data[0], hospitalLocation, 0);*/



                List<Leg> legs =  trips.get(0).getTrip().getLegs();
                routeRequestStationLocations.clear();
                firstFootpath.clear();
                secondFootpath.clear();
                footpathLegs2.clear();
                rideLegs2.clear();
                int legsLength = trips.get(0).getTrip().getLegs().size();

                //loop to iterate the  legs
                for(int i = 0; i < legsLength; i++){
                    //loop to check if either point of leg has -1 (= footpath id)
                    if(Integer.parseInt(legs.get(i).getMode().getCode()) == -1 ){
                        footpathLegs2.add(legs.get(i));

                        //else if both points have id 50 (= ride id)
                    }else  {
                        rideLegs2.add(legs.get(i));

                    }
                    //stopsequence for all ridelegs
                }for(Leg leg: rideLegs2){
                    int stopLength = leg.getStopSeq().size();
                    for(int s = 0; s < stopLength;s++){

                        int diva = Integer.parseInt(leg.getStopSeq().get(s).getRef().getId());
                        routeRequestStationLocations.add(helper.getStationLocation(helper.getStationId(diva)));

                    }
                }drawRideLegs(routeRequestStationLocations, hospitalLocation);
                firstFootpath.add(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));
                firstFootpath.add(new LatLng(routeRequestStationLocations.get(0).latitude, routeRequestStationLocations.get(0).longitude));
                secondFootpath.add(new LatLng(routeRequestStationLocations.get(routeRequestStationLocations.size()-1).latitude, routeRequestStationLocations.get(routeRequestStationLocations.size()-1).longitude));
                secondFootpath.add(new LatLng(hospitalLocation.getLatitude(), hospitalLocation.getLongitude()));
                footpathRequests(secondFootpath);
                footpathRequests(firstFootpath);

            }

            @Override
            public void onFailure(Call<Data> call, Throwable t) {
                Log.e(LOG_TAG, "onFailure: " + t.getMessage());
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("footpath", footpathLegs2);
                bundle.putParcelableArrayList("ridepath", rideLegs2);
                RouteDetail fragment = new RouteDetail();
                fragment.setArguments(bundle);
                numberPicker.invalidate();
                getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).addToBackStack("detail").commit();




            }
        });
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
                mGoogleMap.clear();
                mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(hospitalLocation.getLatitude(), hospitalLocation.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_red_hospital)).title(name));
                mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(myLocation.getLatitude(), myLocation.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_red_position)));
                getRouteFromData(data[0], hospitalLocation, newValue);
            }
        });
    }

    /**
     * necessary for retrieving the information to the respective route */
    void getRouteFromData(Data data, Location hospitalLocation, int tripNo){

        List<Leg> legs =  data.getTrips().get(tripNo).getTrip().getLegs();
        routeRequestStationLocations.clear();
        firstFootpath.clear();
        secondFootpath.clear();
        footpathLegs2.clear();
        rideLegs2.clear();
        int legsLength = data.getTrips().get(tripNo).getTrip().getLegs().size();

        //loop to iterate the  legs
        for(int i = 0; i < legsLength; i++){
            //loop to check if either point of leg has -1 (= footpath id)
            if(Integer.parseInt(legs.get(i).getPoints().get(0).getPlaceID()) == -1 || Integer.parseInt(legs.get(i).getPoints().get(1).getPlaceID()) == -1 ){
                footpathLegs2.add(legs.get(i));

                //else if both points have id 50 (= ride id)
            }else if(Integer.parseInt(legs.get(i).getPoints().get(0).getPlaceID()) == 50 && Integer.parseInt(legs.get(i).getPoints().get(1).getPlaceID()) == 50) {
                rideLegs2.add(legs.get(i));

            }
            //stopsequence for all ridelegs
        }for(Leg leg: rideLegs2){
            int stopLength = leg.getStopSeq().size();
            for(int s = 0; s < stopLength;s++){

                int diva = Integer.parseInt(leg.getStopSeq().get(s).getRef().getId());
                routeRequestStationLocations.add(helper.getStationLocation(helper.getStationId(diva)));

            }
        }drawRideLegs(routeRequestStationLocations, hospitalLocation);
        firstFootpath.add(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));
        firstFootpath.add(new LatLng(routeRequestStationLocations.get(0).latitude, routeRequestStationLocations.get(0).longitude));
        secondFootpath.add(new LatLng(routeRequestStationLocations.get(routeRequestStationLocations.size()-1).latitude, routeRequestStationLocations.get(routeRequestStationLocations.size()-1).longitude));
        secondFootpath.add(new LatLng(hospitalLocation.getLatitude(), hospitalLocation.getLongitude()));
        footpathRequests(secondFootpath);
        footpathRequests(firstFootpath);

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("footpath", footpathLegs2);
        bundle.putParcelableArrayList("ridepath", rideLegs2);
        RouteDetail fragment = new RouteDetail();
        fragment.setArguments(bundle);
        //getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
    }

    /**
     * uses Volley for the requests*/
    public void footpathRequests(ArrayList<LatLng> footPathLegs){
        String url = null;

        try {
            url = GoogleMapsApi.buildGoogleDirectionsUrl(
                    footPathLegs.get(0).latitude,
                    footPathLegs.get(0).longitude,
                    footPathLegs.get(1).latitude,
                    footPathLegs.get(1).longitude);

        }catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }


        StringRequest footPathRouteRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    points.clear();
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray steps = jsonResponse.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");
                    int s = steps.length();

                    for(int i = 0; i < s ;i++){

                        Double startLat = Double.parseDouble(steps.getJSONObject(i).getJSONObject("start_location").getString("lat"));
                        Double startLng = Double.parseDouble(steps.getJSONObject(i).getJSONObject("start_location").getString("lng"));
                        Double endLat = Double.parseDouble(steps.getJSONObject(i).getJSONObject("end_location").getString("lat"));
                        Double endLng = Double.parseDouble(steps.getJSONObject(i).getJSONObject("end_location").getString("lng"));
                        points.add(new LatLng(startLat, startLng));
                        points.add(new LatLng(endLat, endLng));
                    }drawFootpathLegs(points);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(footPathRouteRequest);


    }

    /**
     * draws polylines on the map and adjusts the zoom level so that initially the whole route can be seen on the map */
    public void drawRideLegs(ArrayList<LatLng> list, Location selectedHospital){

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        numberPicker.setVisibility(View.VISIBLE);

        for(int i = 0; i < list.size() - 1; i++) {
            LatLng lng = new LatLng(list.get(i).latitude, list.get(i).longitude);
            LatLng lng1 = new LatLng(list.get(i+1).latitude, list.get(i+1).longitude);
            mGoogleMap.addPolyline(new PolylineOptions().add(lng, lng1).color(getResources().getColor(R.color.logoBlue)));


        }

        //TODO: extend Waypoint to include platform, write method to extract RBL for one specific platform,
        // make sure that makeRealTimeRequest can handle showing one rbl without clearing the map if a flag is sent or smthg of the like

        /***  rbls.clear();
         int diva = rideLegs.get(0).getPoints()[0].getDiva();
         int id = helper.getStationId(diva);
         rbls = helper.getRBLs(id);
         makeRealTimeRequest(WienerLinenApi.buildWienerLinienMonitorUrl(rbls));
         **/
        builder.include(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));
        builder.include(new LatLng(selectedHospital.getLatitude(), selectedHospital.getLongitude()));
        LatLngBounds bounds = builder.build();
        int padding = 100; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mGoogleMap.moveCamera(cu);


    }

    /**
     * draws polylines on the map for the footpaths */
    public void drawFootpathLegs(ArrayList<LatLng> list){

        List<PatternItem> dashedPattern = Arrays.asList(new Dot(), new Gap(50));

        for(int i = 0; i < list.size() - 1; i++) {
            LatLng lng = new LatLng(list.get(i).latitude, list.get(i).longitude);
            LatLng lng1 = new LatLng(list.get(i+1).latitude, list.get(i+1).longitude);
            mGoogleMap.addPolyline(new PolylineOptions().add(lng, lng1).pattern(dashedPattern).color(getResources().getColor(R.color.sunsent_orange)));


        }


    }

    /**
     * ensures, that platform information concerning multiple lines are displayed in a single info window*/
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

    /**
     * establishes the Google API client in order to access the Google Location Services*/
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

    /**
     * makes necessary permission check, if app is allowed access to user location*/
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

    /**
     * requests permission to user location if not granted*/
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

   /**
    * callback, gets called when ActivityCompat.requestPermissions() is called, to handle result */
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

    /**
     * gets called, when location permissions are granted */

    void makeLocationRequest() {

            addLocationUpdates();

            checkUserLocationSettings();

    }

    /** checks if user has location turned on and prompts user to turn location on if it is off */
    private void checkUserLocationSettings(){
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


    /**
     * handles user response to location prompt  */
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

    /**
     * gets last location of user*/

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

    /** is called when connect request has completed with success*/
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(myLocation == null){
            requestPermission();
        }
        addLocationUpdates();
    }

    /**
     * zooms to given location on map*/
    private void goToLocation(double lat, double lng, float zoom) {
        LatLng ll = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
        mGoogleMap.moveCamera(update);

    }

    @Override
    public void onConnectionSuspended(int i) {
        removeLocationUpdates();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * handles a detected location change, if the distance between current location and last known location is greater than 20 feet, the map is updated**/
    @Override
    public void onLocationChanged(Location location) {
        if(myLocation != null){
            float distance = 20.0f;
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

    /**
     * called when map fragment is ready. global map variable initialized with map
     * custom info window is set*/
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



    /**
     * method to check for working network connection*/
    private boolean isConnected() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * This method is defined as an interface in the activity_station fragment
     * triggers the makeRealTimeRequest()*/
    @Override
    public void onStationSelected(ArrayList<Integer> rbls2) {

        getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.content_frame)).commit();
        mGoogleMap.clear();
        String url = WienerLinenApi.buildWienerLinienMonitorUrl(rbls2);
        makeRealTimeRequest(url);
    }

    /**
     * stops location updates when unnecessary*/
    private void removeLocationUpdates(){

        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    /**
     * starts location updates when necessary*/
    private void addLocationUpdates() {
        checkPermission();
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * activity lifecycle calls
     * in onStart and onStop the GoogleAPIClient is connected and disconnected so as not to use too many resources while the app is in the background**/

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();

        Log.i(LOG_TAG, "Lifecycle: onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();

        Log.i(LOG_TAG, "Lifecycle: onStop");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LOG_TAG, "Lifecycle: onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "Lifecycle: onResume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "Lifecycle: onDestroy");
    }
}
