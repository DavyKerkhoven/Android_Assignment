package com.company.scott.smsapp2;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap googleMap;
    private LocationManager locationManager;
    private double latitude;
    private double longtitude;
    private String usersNumber;
    private FusedLocationProviderClient mFusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Intent intent = getIntent();
        latitude = intent.getDoubleExtra("latitude", 50); //50 is the default value
        longtitude = intent.getDoubleExtra("longitude", 50);
        usersNumber = intent.getStringExtra("phoneNumber");
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker corresponding to the latitutde and longtitude sent by the user.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(final GoogleMap googleMap) {

        this.googleMap = googleMap;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        LatLng friendsLocation = new LatLng(latitude, longtitude);
        this.googleMap.addMarker(new MarkerOptions().position(friendsLocation).title((usersNumber+"'s Location:")));
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(friendsLocation));
        this.googleMap.animateCamera( CameraUpdateFactory.zoomTo( 15.0f ) );
        googleMap.getUiSettings().setZoomControlsEnabled(true);

//        Toast.makeText(MapsActivity.this, "Map has loaded!", Toast.LENGTH_SHORT).show();
//        //minimum value=2.0 and maximum value=21.0.
//
//        //Check for permission to get access to current location:
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
//        {
//            Toast.makeText(MapsActivity.this, "Failed to get permission", Toast.LENGTH_SHORT).show();
//
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
//        }
//
//        //Double check we have got permission and then update map with current location:
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
//        {
//
//            Toast.makeText(MapsActivity.this, "Permission granted!", Toast.LENGTH_SHORT).show();
//            mFusedLocationClient.getLastLocation()
//                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
//                        @Override
//                        public void onSuccess(Location location) {
//                            // Got last known location. In some rare situations this can be null.
//                            if (location != null) {
//                                double longtidude = location.getLongitude();
//                                double latitude = location.getLatitude();
//
//                                currentLocation = new LatLng(latitude, longtidude);
//                                googleMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));
//                                googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
//                                googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
//                                googleMap.getUiSettings().setZoomControlsEnabled(true);
//                            }
//                        }
//                    });
//        }
//        else
//        {
//            Toast.makeText(MapsActivity.this, "Failed to get permission", Toast.LENGTH_SHORT).show();
//
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
//        }

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


}
