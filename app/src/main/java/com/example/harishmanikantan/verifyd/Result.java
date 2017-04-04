package com.example.harishmanikantan.verifyd;

import android.*;
import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.orm.SugarContext;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static com.example.harishmanikantan.verifyd.R.id.date;

public class Result extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<LocationSettingsResult>, LocationListener {

    private static final String TAG="Result";

    protected GoogleApiClient googleApiClient;
    private static final int PERMISSION_ACCESS_COARSE_LOCATION = 15;
    protected static final int REQUEST_CHECK_SETTINGS = 1;

    private String eventUid;
    private String eventName;
    private String locationName;
    private String timeLeft;
    private String hostName;
    private String timeDuration;
    private double eventLat;
    private double eventLon;
    private double userLat;
    private double userLon;
    private int radius;

    private Toolbar toolbar;

    private Event event;

    private Button checkInButton;
    private TextView checkInTimeView;
    private TextView verifiedView;
    private TextView eventNameView;
    private TextView hostedByView;
    private TextView hostNameView;
    private TextView atView;
    private TextView locationNameView;
    private TextView fromView;
    private TextView timeDurationView;

    UserLocalStore userLocalStore;
    private Context context;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private GoogleApiClient mGoogleApiClient;

    DatabaseReference ref;

    private boolean flag=false;

    protected LocationSettingsRequest mLocationSettingsRequest;
    protected LocationRequest locationRequest;
    private Location lastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        checkInButton = (Button) findViewById(R.id.check_in);
        checkInTimeView = (TextView) findViewById(R.id.checkInTime);
        verifiedView = (TextView) findViewById(R.id.verified);
        eventNameView = (TextView) findViewById(R.id.eventName);
        hostedByView = (TextView) findViewById(R.id.hostedBy);
        hostNameView = (TextView) findViewById(R.id.hostName);
        atView = (TextView) findViewById(R.id.at);
        locationNameView = (TextView) findViewById(R.id.locationName);
        fromView = (TextView) findViewById(R.id.from);
        timeDurationView = (TextView) findViewById(R.id.timeDuration);

        Intent intent=getIntent();
        Bundle b = intent.getBundleExtra("Event");
        event = (Event) b.getSerializable("Event");

        eventLat = event.getLatitude();
        eventLon = event.getLongitude();
        radius = event.getRadius();
        eventUid = event.getEventUid();
        locationName = event.getLocationName();
        timeDuration = getTimeDuration();
        eventName = event.getEventName();
        setHostNameView();
        //hostName = "Harish Manikantan";

        checkInButton.setText("Check in");
        verifiedView.setText("You have not been verified for");
        eventNameView.setText(eventName);
        hostNameView.setText(hostName);
        locationNameView.setText(locationName);
        timeDurationView.setText(timeDuration);

        userLocalStore=new UserLocalStore(this);
        context=this;

        Log.d("eventLatResult",""+eventLat);
        Log.d("eventLonResult",""+eventLon);
        Log.d("eventRadius",""+radius);

        checkInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkInButton.getText().toString().equals("Check in"))
                    checkIn();
                else{
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Events").child(eventUid).child("invitees").child(mFirebaseUser.getUid());
                    databaseReference.child("check out").setValue(getCurrentUTCTime());
                    checkInTimeView.setText("check out : " + convertUTCtoSimpleTime(new Date()));
                }
            }
        });

    }

    private void setHostNameView() {

        Log.d("Result",event.getHost());
        ref = FirebaseDatabase.getInstance().getReference().child("users").child(event.getHost());

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                hostNameView.setText(dataSnapshot.child("name").getValue(String.class));
                Log.d("Result","Host name = "+hostName);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private Date date1;
    private Date date2;

    private String getTimeDuration() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("gmt"));
        String startTime = event.getStartTime();
        String endTime = event.getEndTime();

        try {
            date1 = sdf.parse(startTime);
            date2 = sdf.parse(endTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String start = convertUTCtoSimpleTime(date1);
        String end = convertUTCtoSimpleTime(date2);

        return start + " - " + end;
    }

    private void checkIn() {

        buildLocationRequest();

        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        Log.d("Result","start");

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            googleApiClient.connect();
        }
        else {

            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.d("Result","Connected first time");
                googleApiClient.connect();
            }

            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Snackbar.make(verifiedView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                        .setAction(android.R.string.ok, new View.OnClickListener() {
                            @Override
                            @TargetApi(Build.VERSION_CODES.M)
                            public void onClick(View v) {
                                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_ACCESS_COARSE_LOCATION);
                            }
                        });
            } else {
                Log.d("Result","request permissions");
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_ACCESS_COARSE_LOCATION);
            }
        }

    }

    @Override
    protected void onStart(){
        super.onStart();
        User user=userLocalStore.getLoggedInUser();
    }

    private void buildLocationRequest(){
        locationRequest = new LocationRequest();
        locationRequest.setInterval(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        mLocationSettingsRequest = builder.build();
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d("Result", "onConnected");
            checkLocationSettings();
            //lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            getLatLon();
            //boolean result = hasCheckedIn();
            //updateUI(result);
        }
    }

    protected void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        googleApiClient,
                        mLocationSettingsRequest
                );
        result.setResultCallback(Result.this);
    }

    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        Log.d("Result","onResult");
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.i(TAG, "All location settings are satisfied.");
                getLatLon();
                flag=true;
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.d("Result", "Location settings are not satisfied. Show the user a dialog to" +
                        "upgrade location settings ");

                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    status.startResolutionForResult(Result.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    Log.i(TAG, "PendingIntent unable to execute request.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog " +
                        "not created.");
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        flag=true;
                        getLatLon();
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        //startActivity(new Intent(Result.this,EventName.class));
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        //startActivity(new Intent(Result.this,EventName.class));
                        break;
                }
                break;
        }
    }

    public void getLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationRequest,this);
            lastLocation=LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            getLatLon();
            //Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            /*if (lastLocation != null) {
                userLat = lastLocation.getLatitude();
                userLon = lastLocation.getLongitude();
                Log.d("Lat", "" + userLat);
                Log.d("Lon", "" + userLon);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_ACCESS_COARSE_LOCATION);
                }
            }*/
            //boolean result = hasCheckedIn();
            //updateUI(result);
        }
    }

    public void getLatLon(){
        Log.d("Result","getLatLon");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationRequest,this);
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (lastLocation != null) {
                userLat = lastLocation.getLatitude();
                userLon = lastLocation.getLongitude();
                Log.d("Lat", "" + userLat);
                Log.d("Lon", "" + userLon);
                boolean result = hasCheckedIn();
                Log.d("has checked in?",""+result);
                updateUI(result);
                updateDatabase(result);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_ACCESS_COARSE_LOCATION);
                }
            }
        }
    }

    private void updateDatabase(boolean result) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Events").child(eventUid).child("invitees").child(mFirebaseUser.getUid());
        databaseReference.child("attendance").setValue(result);
        databaseReference.child("check in").setValue(getCurrentUTCTime());
    }

    private String getCurrentUTCTime() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(date);
    }

    @Override
    public void onConnectionSuspended ( int i){

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_COARSE_LOCATION:
                Log.d("Result","Length of grantResults"+grantResults.length);
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Result","Connected");
                    //googleApiClient.connect();
                    //setLocation();
                    //getLatLon();
                } else {
                    //checkLocationSettings();
                    Toast.makeText(this, "Need your location!", Toast.LENGTH_SHORT).show();
                    //startActivity(new Intent(Result.this,EventName.class));
                }
                break;
        }
    }

    public void setLocation(){
        //lastLocation=LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
    }

    public boolean hasCheckedIn(){
        if (distance(eventLat,eventLon,userLat,userLon)<radius)
            return true;
        else
            return false;
    }

    public double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist)*1000;
    }

    public double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    public double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    public void updateUI(boolean result){
        if (result) {
            checkInButton.setText("Check out");
            checkInTimeView.setText("check in : " + convertUTCtoSimpleTime(new Date()));

            verifiedView.setTextColor(getResources().getColor(R.color.verifydGreen));
            verifiedView.setText("You have been verified for");

        }
        else {
            verifiedView.setTextColor(getResources().getColor(R.color.red));
            verifiedView.setText("You have not been verified for");
        }
    }

    private String convertUTCtoSimpleTime(Date currentUTCTime) {

        int hour = currentUTCTime.getHours();
        int min = currentUTCTime.getMinutes();
        String ampm = "";

        if (hour == 12) {
            ampm = "PM";
        }
        else if(hour >= 12){
            ampm = "PM";
            hour = hour%12;
        }
        else {
            ampm = "AM";
        }

        return hour+":"+min+" "+ampm;
    }

    @Override
    protected void onDestroy() {
        SugarContext.terminate();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void signOut(){
        mFirebaseAuth.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        mFirebaseUser = null;
        FirebaseAuth.getInstance().signOut();
        //mUsername = ANONYMOUS;
        //mPhotoUrl = null;
        startActivity(new Intent(this, LoginActivity.class));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("Result","Location changed");
        lastLocation = location;
    }
}