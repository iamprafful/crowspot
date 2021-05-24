package tech.greedylabs.crowspot;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.database.FirebaseDatabase;

import javax.security.auth.login.LoginException;

public class ScanActivity extends AppCompatActivity {

    TextView userTv;
    SharedPreferences sharedPreferences;
    ImageView scanIv;
    LottieAnimationView animationView;
    TextView scanTv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        userTv = findViewById(R.id.userTv);
        scanIv = findViewById(R.id.scanIv);
        scanTv = findViewById(R.id.scanTv);
        animationView = findViewById(R.id.animation_view);
        sharedPreferences = getSharedPreferences(Helper.SHARED_PREF_KEY, MODE_PRIVATE);
        userTv.setText(sharedPreferences.getString(Helper.USERNAME, "UserName"));
        checkLocationPermission();
        try{
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }
        catch (Exception e)
        {
            Log.e("Error", "onCreate: Firebase", e);
        }
    }

    public void startScan(View view) {
        scanIv.setVisibility(View.GONE);
        animationView.setVisibility(View.VISIBLE);
        scanTv.setText("SCANNING HOTSPOTS");
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(ScanActivity.this, ShowHotspotActivity.class));
                scanIv.setVisibility(View.VISIBLE);
                animationView.setVisibility(View.GONE);
                scanTv.setText("TAP TO SCAN");
            }
        }, 3000);
    }

    public void newHotspot(View view) {
        scanIv.setVisibility(View.VISIBLE);
        animationView.setVisibility(View.GONE);
        scanTv.setText("TAP TO SCAN");
        startActivity(new Intent(this, CreateHotspotActivity.class));
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
                                ActivityCompat.requestPermissions(ScanActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
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
}