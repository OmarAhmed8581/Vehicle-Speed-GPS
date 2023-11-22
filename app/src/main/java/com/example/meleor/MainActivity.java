package com.example.meleor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.Date;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {
    private LocationManager locationManager;
    private Button gpsbtn,stopbtn;

    private double perviouslat = 0.0;
    private double perviouslon = 0.0;
    private long pervioustime = 0;

    private MediaPlayer boot;
    private MediaPlayer accel;
    private MediaPlayer enginesound;
    private MediaPlayer enginesoundfast;
    private MediaPlayer gearupacc;
    private MediaPlayer gearupstrongaccel;
    private MediaPlayer speedsound;
    private int perviousspeed = 0;
    private double currentspeed = 0;
    private double currentlat = 0.0;
    private double currentlon = 0.0;
    private long currenttime = 0;

    private Handler speedhandler;
    private Runnable stopPlaybackRunnable;
    private Handler acchandler;
    private Runnable accstopPlaybackRunnable;

    private Handler samespeedhandler;
    private Runnable samespeedstopPlaybackRunnable;

    private Handler gearupacchandler;
    private Runnable gearupaccstopPlaybackRunnable;

    private  Boolean start_ign=false;

    private  float speedKMH = 0;

    private Handler gearupaccstronghandler;
    private Runnable gearupaccstrongstopPlaybackRunnable;

    private Handler boothandler;
    private Runnable boothandlerstopPlaybackRunnable;

    private static final int PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Get the location manager
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


        gpsbtn = findViewById(R.id.gpsbtn);
        stopbtn = findViewById(R.id.stopbtn);

        boot = MediaPlayer.create(this, R.raw.boot);
        accel = MediaPlayer.create(this, R.raw.accel);
        enginesound = MediaPlayer.create(this, R.raw.enginesound);
        enginesoundfast = MediaPlayer.create(this, R.raw.enginesoundfast);
        gearupacc = MediaPlayer.create(this, R.raw.gearupacc);
        gearupstrongaccel = MediaPlayer.create(this, R.raw.gearupstrongaccel);
        speedsound = MediaPlayer.create(this, R.raw.speedsound);

        stopbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopLocationUpdates();
                speedstopPlayback();
                accstopPlayback();
                samespeedstopPlayback();
                gearupaccstopPlayback();
                gearupstrongaccelstopPlayback();
                start_ign=false;
                Toast.makeText(MainActivity.this,"Stop Speed......",Toast.LENGTH_SHORT).show();
            }
        });


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        // Handle the received location

                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
//                        Toast.makeText(MainActivity.this, "latitude: "+latitude+" and longitude: "+longitude, Toast.LENGTH_SHORT).show();

                        if(!start_ign){
                            start_ign=true;
                            bootstartPlayback(11000);
                            Toast.makeText(MainActivity.this, "Boot Acceleration",
                                    Toast.LENGTH_SHORT).show();
                        }

                        if(Objects.equals(perviouslat, 0.0) && Objects.equals(perviouslon, 0.0) && pervioustime ==0){
                            perviouslat = latitude;
                            perviouslon = longitude;
                            pervioustime = new Date().getTime();
                        }
                        else{
                            currentlat = latitude;
                            currentlon = longitude;
                            currenttime = new Date().getTime();
//                            double speed = VehicleSpeed.calculateSpeed(perviouslat, perviouslon, currentlat, currentlon, pervioustime,currenttime);
                            float speedlocation = location.getSpeed();
                            speedKMH = speedlocation * 3.6f;
                            int speed = (int)speedKMH;
                            perviouslat = currentlat;
                            perviouslon = currentlon;
                            pervioustime = currenttime;
                            System.out.println("Speed: "+speed);
                            Toast.makeText(MainActivity.this, "Speed: "+speed,
                                    Toast.LENGTH_SHORT).show();


                            if(speed==0){
                                perviousspeed = speed;

                                Toast.makeText(MainActivity.this, "Vehicle Not Moving",
                                        Toast.LENGTH_SHORT).show();

                                bootstartPlayback(19000);
                            }
                            else if(perviousspeed==0){
                                perviousspeed = speed;
                                if(speed==0){
                                    Toast.makeText(MainActivity.this, "Vehicle Not Moving",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                            else if(perviousspeed<0){
                                perviousspeed = speed;
                            }
                            else if(perviousspeed>speed){
                                perviousspeed = speed;
                                Toast.makeText(MainActivity.this, "decelerate,",
                                        Toast.LENGTH_SHORT).show();

                                bootstartPlayback(19000);
                            }
                            else if(perviousspeed<20 ){
                                if(speed>=20 && speed<=50){
                                    // Gear up acceleration: You accelerate rapidly and gain +10% of speed within 2 seconds between 20km/h to 50 km/h
                                    Toast.makeText(MainActivity.this, "Gear up acceleration",
                                            Toast.LENGTH_SHORT).show();
                                    perviousspeed = speed;
                                    gearupaccstartPlayback();
                                }
                                else if(speed>=55 && speed<=110){
                                    // Gear up strong acceleration: You accelerate rapidly and gain +20% of speed within 2 seconds between 55km/h to 110 Km/h
                                    Toast.makeText(MainActivity.this, "Gear up strong acceleration",
                                            Toast.LENGTH_SHORT).show();
                                    perviousspeed = speed;
                                    gearupstrongaccelstartPlayback();
                                }
                                else if(speed>=0 && speed<=30){
                                    //Acceleration: You acceleration normaly from 0 to 30km/h
                                    Toast.makeText(MainActivity.this, "Acceleration",
                                            Toast.LENGTH_SHORT).show();


                                    perviousspeed = speed;
                                    accstartPlayback();
                                }

                            }
                            else if(perviousspeed == speed){
                                //Engine sound: this has to be a loop when you keep the same speed
                                Toast.makeText(MainActivity.this, "Engine sound",
                                        Toast.LENGTH_SHORT).show();
                                  perviousspeed = speed;
                                  samespeedstartPlayback();
                            }
                            else if(speed>=80){
                                Toast.makeText(MainActivity.this, "Speed sound: Vehicle reach 80km/h",
                                        Toast.LENGTH_SHORT).show();
                                //Speed sound: this is a sound to show speed play it everytime the vehicule reach 80km/h
                                perviousspeed = speed;
                                speedstartPlayback();
                            }

                        }

                    }
                }
            }
        };

        gpsbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "GPS Location Start", Toast.LENGTH_SHORT).show();
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates();
                } else {
                    requestLocationPermissions();
                }
            }
        });

        speedhandler = new Handler();
        stopPlaybackRunnable = new Runnable() {
            @Override
            public void run() {
                speedstopPlayback();
            }
        };

        acchandler = new Handler();
        accstopPlaybackRunnable = new Runnable() {
            @Override
            public void run() {
                accstopPlayback();
            }
        };

        samespeedhandler = new Handler();
        samespeedstopPlaybackRunnable = new Runnable() {
            @Override
            public void run() {
                samespeedstopPlayback();
            }
        };

        gearupacchandler = new Handler();
        gearupaccstopPlaybackRunnable = new Runnable() {
            @Override
            public void run() {
                gearupaccstopPlayback();
            }
        };

        gearupaccstronghandler = new Handler();
        gearupaccstrongstopPlaybackRunnable = new Runnable() {
            @Override
            public void run() {
                gearupstrongaccelstopPlayback();
            }
        };

        boothandler = new Handler();
        boothandlerstopPlaybackRunnable = new Runnable() {
            @Override
            public void run() {
                bootstopPlayback();
            }
        };



    }

    //    Boot Handler

    private void bootstartPlayback(int speed) {
        speedsound.start();
        // Stop playback after 4 seconds
        speedhandler.postDelayed(stopPlaybackRunnable, speed);
    }

    private void bootstopPlayback() {
        if (speedsound.isPlaying()) {
            speedsound.stop();
        }
    }

    private void speedstartPlayback() {
        speedsound.start();
        // Stop playback after 4 seconds
        speedhandler.postDelayed(stopPlaybackRunnable, 10000);
    }

    private void speedstopPlayback() {
        if (speedsound.isPlaying()) {
            speedsound.stop();
        }
    }

    private void accstartPlayback() {
        accel.start();
        // Stop playback after 4 seconds
        acchandler.postDelayed(accstopPlaybackRunnable, 100000);
    }
    private void accstopPlayback() {
        if (accel.isPlaying()) {
            accel.stop();
        }
    }

    private void samespeedstopPlayback() {
        if (enginesound.isPlaying()) {
            enginesound.stop();
        }
    }

    private void samespeedstartPlayback() {
        enginesound.start();
        // Stop playback after 4 seconds
        samespeedhandler.postDelayed(samespeedstopPlaybackRunnable, 10000);
    }

    private void gearupaccstopPlayback() {
        if (gearupacc.isPlaying()) {
            gearupacc.stop();
        }
    }

    private void gearupaccstartPlayback() {
        gearupacc.start();
        // Stop playback after 4 seconds
        gearupacchandler.postDelayed(gearupaccstopPlaybackRunnable, 10000);
    }

    private void gearupstrongaccelstopPlayback() {
        if (gearupstrongaccel.isPlaying()) {
            gearupstrongaccel.stop();
        }
    }

    private void gearupstrongaccelstartPlayback() {
        gearupstrongaccel.start();
        // Stop playback after 4 seconds
        gearupacchandler.postDelayed(gearupaccstopPlaybackRunnable, 10000);
    }



    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(2000); // Update interval in milliseconds
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void requestLocationPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Explain to the user why location permissions are needed (optional)
        }

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                // Handle denied permission (optional)
            }
        }
    }

}