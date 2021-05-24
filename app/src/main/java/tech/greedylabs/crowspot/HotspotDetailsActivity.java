package tech.greedylabs.crowspot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import tech.greedylabs.crowspot.model.Hotspot;
import tech.greedylabs.crowspot.room.AppDatabase;
import tech.greedylabs.crowspot.room.Vote;

public class HotspotDetailsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnMarkerClickListener {

    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    FirebaseDatabase database;
    DatabaseReference myRef;
    String key;
    TextView usernameTv, timeTv, threatTv, addressTv, upvoteTv, downvoteTv;
    ImageView upvoteIv, downvoteIv;
    Hotspot currentHotspot;
    AppDatabase appDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotspot_details);
        usernameTv = findViewById(R.id.usernameTv);
        timeTv = findViewById(R.id.timeTv);
        threatTv = findViewById(R.id.threatTv);
        addressTv = findViewById(R.id.addressTv);
        upvoteTv = findViewById(R.id.upvoteTv);
        downvoteTv = findViewById(R.id.downvoteTv);
        upvoteIv = findViewById(R.id.upvoteIv);
        downvoteIv = findViewById(R.id.downvoteIv);
        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFrag != null;
        mapFrag.getMapAsync(this);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("hotspot");
        key = getIntent().getStringExtra("key");
        appDatabase = AppDatabase.getInstance(this);
        getData(key);
    }

    private void getData(String key) {
        DatabaseReference databaseReference = myRef.child(key);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentHotspot = snapshot.getValue(Hotspot.class);
                    usernameTv.setText("Hotspot created by " + currentHotspot.getUsername());
                    timeTv.setText("@" + DateUtils.getRelativeTimeSpanString(
                            Long.parseLong(currentHotspot.getTime()),
                            System.currentTimeMillis(),
                            DateUtils.SECOND_IN_MILLIS
                    ));
                    threatTv.setText(currentHotspot.getThreatType());
                    addressTv.setText(currentHotspot.getAddress());
                    upvoteTv.setText(String.valueOf(currentHotspot.getUpVote()));
                    downvoteTv.setText(String.valueOf(currentHotspot.getDownVote()));
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(currentHotspot.getLatitude()), Double.parseDouble(currentHotspot.getLongitude())), 15));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
                mGoogleMap.setMyLocationEnabled(true);
                mGoogleMap.setOnMarkerClickListener(this);
                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        if (dataSnapshot.exists()) {
                            mGoogleMap.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Hotspot hotspot = snapshot.getValue(Hotspot.class);
                                String key = snapshot.getKey();
                                assert hotspot != null;
                                int riskLevel=(hotspot.getUpVote()-hotspot.getDownVote());
                                int resId=R.layout.threat_marker;
                                if((hotspot.getUpVote()==0 && hotspot.getDownVote()==0) || (hotspot.getUpVote()>hotspot.getDownVote()))
                                {
                                    resId=R.layout.threat_marker;
                                }
                                else if((hotspot.getDownVote()/2)>hotspot.getUpVote())
                                {
                                    resId=R.layout.risky_marker;
                                }
                                else if(hotspot.getUpVote()<hotspot.getDownVote())
                                {
                                    resId=R.layout.safe_marker;
                                }
                                if(Long.parseLong(hotspot.getTime())+(30*60*1000)>System.currentTimeMillis())
                                {
                                    Marker marker=mGoogleMap.addMarker(new MarkerOptions().position(
                                            new LatLng(
                                                    Double.parseDouble(hotspot.getLatitude()),
                                                    Double.parseDouble(hotspot.getLongitude())
                                            )
                                            ).icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(
                                            resId,
                                            riskLevel)))
                                                    .anchor(0.5f,0.5f)
                                    );
                                    marker.setTag(key);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.w("Firebase", "Failed to read value.", error.toException());
                    }
                });
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        } else {
            buildGoogleApiClient();
            mGoogleMap.setMyLocationEnabled(true);
            mGoogleMap.setOnMarkerClickListener(this);
            // Read from the database
            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    if (dataSnapshot.exists()) {
                        mGoogleMap.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Hotspot hotspot = snapshot.getValue(Hotspot.class);
                            String key = snapshot.getKey();
                            assert hotspot != null;
                            int riskLevel=(hotspot.getUpVote()-hotspot.getDownVote());
                            int resId=R.layout.threat_marker;
                            if((hotspot.getUpVote()==0 && hotspot.getDownVote()==0) || (hotspot.getUpVote()>hotspot.getDownVote()))
                            {
                                resId=R.layout.threat_marker;
                            }
                            else if((hotspot.getDownVote()/2)>hotspot.getUpVote())
                            {
                                resId=R.layout.risky_marker;
                            }
                            else if(hotspot.getUpVote()<hotspot.getDownVote())
                            {
                                resId=R.layout.safe_marker;
                            }
                            if(Long.parseLong(hotspot.getTime())+(30*60*1000)>System.currentTimeMillis())
                            {
                                Marker marker=mGoogleMap.addMarker(new MarkerOptions().position(
                                        new LatLng(
                                                Double.parseDouble(hotspot.getLatitude()),
                                                Double.parseDouble(hotspot.getLongitude())
                                        )
                                        ).icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(
                                        resId,
                                        riskLevel)))
                                                .anchor(0.5f,0.5f)
                                );
                                marker.setTag(key);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w("Firebase", "Failed to read value.", error.toException());
                }
            });
        }
    }

    private Bitmap getMarkerBitmapFromView(int resId, int number) {
        ViewGroup parent = findViewById(R.id.root_layout);
        FrameLayout customMarkerView = (FrameLayout) this.getLayoutInflater().inflate(resId, parent, true);
        TextView numberTv = (TextView) customMarkerView.findViewById(R.id.numberTextView);
        numberTv.setText(String.valueOf(number));
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }


    public void back(View view) {
        finish();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(HotspotDetailsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mGoogleMap.setMyLocationEnabled(true);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        key = (String) marker.getTag();
        getData(key);
        return true;
    }

    public boolean canVote(LatLng start, LatLng end) {
        try {
            Location location1 = new Location("locationA");
            location1.setLatitude(start.latitude);
            location1.setLongitude(start.longitude);
            Location location2 = new Location("locationB");
            location2.setLatitude(end.latitude);
            location2.setLongitude(end.longitude);
            double distance = location1.distanceTo(location2);
            return !(distance > 65);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void downVote(View view) {
        if (appDatabase.voteDao().voted(key) == 0) {
            if (mLastLocation != null) {
                if (canVote(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), new LatLng(Double.parseDouble(currentHotspot.getLatitude()), Double.parseDouble(currentHotspot.getLongitude())))) {
                    int dv = currentHotspot.getDownVote() + 1;
                    currentHotspot.setDownVote(dv);
                    downvoteTv.setText(String.valueOf(dv));
                    Vote vote = new Vote(key);
                    appDatabase.voteDao().insert(vote);
                    myRef.child(key).setValue(currentHotspot);
                } else {
                    Toast.makeText(this, "Votes can only be placed if you’re within 65 meters range.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Waiting for your location.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "You can vote only once", Toast.LENGTH_LONG).show();
        }
    }

    public void upVote(View view) {
        if (appDatabase.voteDao().voted(key) == 0) {
            if (mLastLocation != null) {
                if (canVote(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), new LatLng(Double.parseDouble(currentHotspot.getLatitude()), Double.parseDouble(currentHotspot.getLongitude())))) {
                    int uv = currentHotspot.getUpVote() + 1;
                    currentHotspot.setUpVote(uv);
                    upvoteTv.setText(String.valueOf(uv));
                    Vote vote = new Vote(key);
                    appDatabase.voteDao().insert(vote);
                    myRef.child(key).setValue(currentHotspot);
                } else {
                    Toast.makeText(this, "Votes can only be placed if you’re within 65 meters range.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Waiting for your location.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "You can vote only once", Toast.LENGTH_LONG).show();
        }
    }
}