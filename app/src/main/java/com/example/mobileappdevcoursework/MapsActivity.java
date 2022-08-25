package com.example.mobileappdevcoursework;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static java.lang.String.valueOf;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private GeofencingClient geofencingClient;
    private GeoFenceHelper geofenceHelper;
    private DatabaseManipulator dm;

    private List<Geofence> geofenceList = new ArrayList<Geofence>();
    private Stack<Circle> circleStack = new Stack<Circle>(); //Use these two for storage and removal of geofences

    private int FINE_LOCATION_ACCESS_REQUEST_CODE = 101;
    private int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 102;
    float radius = 200;

    private String geoID = "";
    private Boolean displayOnly = false;
    private static final String TAG = "MapsActivity";

    SearchView searchView;
    SeekBar seekBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Receive data from previous intent and set them as global variables
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            geoID = extras.getString("Name");
            displayOnly = extras.getBoolean("displayOnly");
        }

        if(displayOnly == false)
        {
            useSeekBar();
        }
        else
        {
            seekBar = (SeekBar)findViewById(R.id.seekBar);
            seekBar.setVisibility(View.GONE);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);


        searchView = findViewById(R.id.sv_location);
        // Query listener for  search view.
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // location name from search view.
                String location = searchView.getQuery().toString();

                List<Address> addressList = null;

                // checking if the entered location is null or not.
                if (location != null || location.equals("")) {
                    Geocoder geocoder = new Geocoder(MapsActivity.this);
                    try {
                        addressList = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //Get first address
                    Address address = addressList.get(0);

                    //LatLng of the location
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                    //Move camera to address
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        mapFragment.getMapAsync(this);
        geofenceHelper = new GeoFenceHelper(this);
        geofencingClient = LocationServices.getGeofencingClient(this);
    }

    //Seekbar code used to change and monitor radius via user input
    public void useSeekBar()
    {
        seekBar=(SeekBar)findViewById(R.id.seekBar);
        // perform seek bar change listener event used for getting the progress value
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                radius = progressChangedValue;
                Toast.makeText(MapsActivity.this, "Radius is: " + radius,
                        Toast.LENGTH_SHORT).show();

            }
        });
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        enableUserLocation();

        mMap.clear();

        //Get data from database, format it into usable variables, then create geofences
        //using those variables
        List<String[]> names2 = null;
        String[] stg1;
        dm = new DatabaseManipulator(this);

        names2 = dm.selectAll();

        for (String[] name : names2) {

            name[2] = name[2].replace("(", "");
            name[2] = name[2].replace(")", "");
            name[2] = name[2].replaceAll("lat/lng: ", "");
            String[] latitudeLongitude =  name[2].split(",");

            String latitude = latitudeLongitude[0];
            String longitude = latitudeLongitude[1];

            double Hlat = 0;
            double Hlong = 0;

            if ( !latitude.trim().equals("") && !longitude.trim().equals("") )
            {
                Hlat = Double.parseDouble(latitude.trim());
                Hlong= Double.parseDouble(longitude.trim());
            }

            LatLng location = new LatLng(Hlat, Hlong);

            float rad = Float.parseFloat(name[3]);

            marker(location, name[1]);
            circle(location, rad);
            createGeofences(name[1], location, rad);
        }

        mMap.setOnMapLongClickListener(this);
    }

    private void enableUserLocation()
    {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            mMap.setMyLocationEnabled(true);
        }
        else
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            }
            else
            {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                //Permission granted
                mMap.setMyLocationEnabled(true);
            }
            else
            {
                //Permission not granted
                Toast.makeText(this, "Location access is necessary", Toast.LENGTH_SHORT).show();
            }
        }

        if(requestCode == BACKGROUND_LOCATION_ACCESS_REQUEST_CODE)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                //Permission granted
                Toast.makeText(this, "You can add geofences", Toast.LENGTH_SHORT).show();
            }
            else
            {
                //Permission not granted
                Toast.makeText(this, "Background location access is necessary", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        //If displayOnly is false, allow user to create a geofence
        if (displayOnly == false)
        {

            //Background Permission request
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == getPackageManager().PERMISSION_GRANTED) {
                new AlertDialog.Builder(this)
                        .setTitle("Add Geofence")
                        .setMessage("Confirm adding geofence?")

                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                InsertGeofence(latLng);
                                displayOnly = true;
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
            //Check & Request permissions
            else
            {
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION))
                {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);

                }
                else
                {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                }
            }
        }
        else
        {
            new AlertDialog.Builder(this)
                    .setTitle("Adding Geofences")
                    .setMessage("To add a Geofence, you need to give it a name. Go to name selection?")

                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i = new Intent(MapsActivity.this, GeofenceOptionsActivity.class);
                            startActivity(i);
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

    }

    //Method used to create new geofence + display(Circle/Marker)
    private void InsertGeofence(LatLng latLng)
    {
        if(radius == 0) //Make sure app doesn't crash when user selects 0 on seekbar
        {
            radius = 1;
        }
        marker(latLng, geoID);
        circle(latLng, radius);
        addGeofence(geoID, latLng, radius);
    }
    //Simple map marker method
    private void marker(LatLng latLng, String geoID)
    {
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(geoID);
        mMap.addMarker(markerOptions);
    }
    //Simple map circle method
    private void circle(LatLng latLng, float radius)
    {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.argb(255,128,0,128));
        circleOptions.fillColor(Color.argb(64,128,0,128));
        circleOptions.strokeWidth(4);

        if(mMap != null) {
            Circle newCircle = mMap.addCircle(circleOptions);
            if(newCircle != null) {
                circleStack.push(newCircle);
            } else {
                Log.d(TAG, "newCircle is null");
            }
        } else {
            Log.d(TAG, "mMap is null");
        }
    }
    //Add Geofence method, used to create geofences via geofenceHelper class
    @SuppressLint("MissingPermission")
    private void addGeofence(String geoID, LatLng latLng, float radius)
    {
        geofenceList.add(geofenceHelper.getGeofence(geoID, latLng, radius, Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT));
        GeofencingRequest geofencingRequest = null;
        for (Geofence geofence : geofenceList)
        {
            if(geoID == geofence.getRequestId())
            {
                geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
            }
            else
            {
                Log.d(TAG, "geofencingRequest error");
            }
        }


        PendingIntent pendingIntent = geofenceHelper.getPendingIntent();
        dm = new DatabaseManipulator(this);
        dm.insert(geoID, latLng.toString(), valueOf(radius));
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Geofence added");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        String error = geofenceHelper.getError(e);
                        Log.d(TAG, "onFailure" + error);
                    }
                });
    }
    //Add geofences method, but without inserting geofence data into database
    @SuppressLint("MissingPermission")
    private void createGeofences(String geoID, LatLng latLng, float radius)
    {
        geofenceList.add(geofenceHelper.getGeofence(geoID, latLng, radius, Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT));
        GeofencingRequest geofencingRequest = null;
        for (Geofence geofence : geofenceList)
        {
            if(geoID == geofence.getRequestId())
            {
                geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
            }
            else
            {
                Log.d(TAG, "geofencingRequest error");
            }
        }


        PendingIntent pendingIntent = geofenceHelper.getPendingIntent();
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Geofence added");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        String error = geofenceHelper.getError(e);
                        Log.d(TAG, "onFailure" + error);
                    }
                });
    }
}