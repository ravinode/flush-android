package com.flush.kira.flush;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.flush.kira.flush.pojo.InfoWindowData;
import com.flush.kira.flush.pojo.LatLongReq;
import com.flush.kira.flush.util.LoadProperties;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.exit;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback ,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,GoogleMap.OnInfoWindowClickListener {

    private static GoogleMap mMap;
    //variable
    private static final int PERMISSION_REQUEST_CODE = 7001;
    private static final int PLAY_SERVICE_REQUEST = 7002;

    private static final int UPDATE_INTERVAL = 5000;//5 detik
    private static final int FASTEST_INTERVAL = 3000;//3detik
    private static final int DISPLACEMENT = 10;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;

    private LatLngBounds bounds;
    private LatLngBounds.Builder builder;


    private PlaceAutocompleteFragment placeAutocompleteFragment;

    static Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        statusCheck();
        placeAutocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        placeAutocompleteFragment.getView().setBackgroundColor(Color.TRANSPARENT);
        placeAutocompleteFragment.setFilter(new AutocompleteFilter.Builder().setCountry("ID").build());

        placeAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                final LatLng latLngLoc = place.getLatLng();
                Toast.makeText(MapsActivity.this, "" + latLngLoc.toString(), Toast.LENGTH_SHORT).show();

                if (marker != null) {
                    marker.remove();
                }
                marker = mMap.addMarker(new MarkerOptions().position(latLngLoc).title(place.getName().toString()));


                mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(MapsActivity.this, "" + status.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        setUpLocation();
    }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, PERMISSION_REQUEST_CODE);
        } else {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();
            }
        }
    }

    private void displayLocation() {
        LatLongReq req = new LatLongReq();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLocation != null) {
            final double latitude = mLocation.getLatitude();
            final double longitude = mLocation.getLongitude();
            Toast.makeText(MapsActivity.this, "Hell0o" +latitude , Toast.LENGTH_LONG).show();
            req.setLat(latitude);
            req.setLog(longitude);
            getLocation(req,MapsActivity.this);
            //show marker
            //
            mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("your position"));
            //Animate camera to your position
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 13));
        }
    }

    private static void drawMarker(final Context context, LatLng point, final String text, final String description, final String distance) {


        BitmapDescriptor markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.icon);
        marker = mMap.addMarker(new MarkerOptions().position(point).snippet(description).title(text).alpha(0.7f).icon(markerIcon));


        InfoWindowData info = new InfoWindowData();
        info.setImage("snowqualmie");
        info.setPrice("Free");
        info.setDistance("Distance :"+distance);
        //info.setTransport("Reach the site by bus, car and train.");

        marker.setTag(info);
        CustomInfoWindowGoogleMap customInfoWindow = new CustomInfoWindowGoogleMap(context);
        mMap.setInfoWindowAdapter(customInfoWindow);





        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {

               Log.i("+++++","Ravi");



                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                final RatingBar rating = new RatingBar(context);

                rating.setNumStars(5);
                dialog.setTitle( marker.getTitle()).setView(rating)

                        .setMessage( marker.getSnippet())
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface, int i) {
                            }
                        }).show();
                rating.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;

            }
        });

    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //restart location updates with the new interval
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    private void buildGoogleApiClient() {



        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICE_REQUEST).show();
            else {
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        builder = new LatLngBounds.Builder();

        LatLongReq req = new LatLongReq();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            return;
        }

    }

    //Oh iya karena kita menggunakan permission kita override method onPermissionRequestResult


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayServices()) {
                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();
                    }
                }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        displayLocation();
    }

    public static void getLocation(LatLongReq latLong, final Context context) {
        try {
            String url = null;

            JSONObject json = new JSONObject();
            try {

                json.put("latitude",latLong.getLat().toString());
                json.put("longtitude",latLong.getLog().toString());
                json.put("distance","40");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                url = LoadProperties.getProperty("GET_LOCATION", context);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(context, "This is my Toast message!" + url,
                    Toast.LENGTH_LONG).show();
            JsonObjectRequest jsonObjRequest = new JsonObjectRequest(Request.Method.POST,
                    url, json,
                    new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
    try {
        JSONArray jsonArray = response.getJSONArray("flushLoc");

        for (int i = 0, size = jsonArray.length(); i < size; i++)
        {
            Log.i("Length",""+jsonArray.length());
            JSONObject objectInArray = jsonArray.getJSONObject(i);
            Log.i("VALUE",""+objectInArray.getString("name"));
            Double lat = Double.parseDouble(objectInArray.getString("latitude"));
            Double log = Double.parseDouble(objectInArray.getString("longitude"));
            drawMarker(context,new LatLng(lat, log), objectInArray.getString("name"),objectInArray.getString("description"),objectInArray.getString("distance"));

            Toast.makeText(context, "Hello" + objectInArray, Toast.LENGTH_LONG).show();
        }
    }
    catch (JSONException e)
    {

    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(context, "Error", Toast.LENGTH_LONG).show();

                }
            })

            {

                @Override
                public Map getHeaders() throws AuthFailureError {
                    HashMap headers = new HashMap();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
                @Override
                protected VolleyError parseNetworkError(VolleyError volleyError) {
                    return volleyError;
                }
            };
            RequestQueue queue = Volley.newRequestQueue(context);
            queue.add(jsonObjRequest);
        }
        catch (Exception e)

        {
e.printStackTrace();
        }

    }



@Override
public void onInfoWindowClick(Marker marker) {
        Toast.makeText(this, "Info window clicked",
        Toast.LENGTH_SHORT).show();
        }

    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        }
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }
    private void buildAlertMessageNoGps() {
        if (!isLocationEnabled(this)) {

            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }

    }


    @Override
    public void onResume() {
        super.onResume();

        setUpLocation();
    }


}