package com.example.harishmanikantan.verifyd;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CreateEvent extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private EditText eventNameView;
    private EditText dateView;
    private EditText startTimeView;
    private EditText endTimeView;
    private EditText radiusView;
    private EditText locationNameView;
    private Button setLocationView;
    private Button createEventView;
    private Button inviteView;
    private Button setDateView;
    private Button startTimeButton;
    private Button endTimeButton;
    private Button deleteButton;

    private String eventName;
    private String startTime;
    private String endTime;
    private String date;
    private String locationName;

    private int radius;
    private String radiusStr;
    private String eventUid;
    private double lat;
    private double lon;
    private List<String> uids;

    private Timer timer1;
    private Timer timer2;
    private TimerTask startTimerTask;
    private TimerTask endTimerTask;
    final Handler handler = new Handler();
    private boolean flag1 = true;
    private boolean flag2 = true;

    private Place place;
    private EventName eventNameObj;

    private Event existingEvent;

    private Context context;

    private DatabaseReference databaseReference;
    private DatabaseReference myRef;
    private FirebaseUser mFirebaseUser;
    private FirebaseAuth mFirebaseAuth;
    private GoogleApiClient mGoogleApiClient;

    private int PLACE_PICKER_REQUEST = 1;
    private int INVITE_FRIENDS_REQUEST = 2;
    private int START_TIME_ID = 3;
    private int END_TIME_ID = 4;
    private int DATE_ID = 5;
    private int startHour;
    private int startMin;
    private int endHour;
    private int endMin;
    private int year_x;
    private int month_x;
    private int day_x;

    private boolean eventFlag;

    private static DatabaseReference databaseReference2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        Intent intent = getIntent();
        eventFlag = intent.getBooleanExtra("new?",true);

        context = this;

        databaseReference = FirebaseDatabase.getInstance().getReference();
        myRef = FirebaseDatabase.getInstance().getReference();

        //databaseReference.child("Events");

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        eventNameView = (EditText) findViewById(R.id.event_name);
        startTimeView = (EditText) findViewById(R.id.start_time);
        endTimeView = (EditText) findViewById(R.id.end_time);
        radiusView = (EditText) findViewById(R.id.radius);
        dateView = (EditText) findViewById(R.id.date);
        locationNameView = (EditText) findViewById(R.id.locationName);
        setLocationView = (Button) findViewById(R.id.set_location);
        createEventView = (Button) findViewById(R.id.create_event);
        inviteView = (Button) findViewById(R.id.invite);
        setDateView = (Button) findViewById(R.id.set_date);
        startTimeButton = (Button) findViewById(R.id.startTimeButton);
        endTimeButton = (Button) findViewById(R.id.endTimeButton);
        deleteButton = (Button) findViewById(R.id.delete_event);

        //deleteButton.setVisibility(View.GONE);

        Calendar cal = Calendar.getInstance();

        year_x = cal.get(Calendar.YEAR);
        month_x = cal.get(Calendar.MONTH);
        day_x = cal.get(Calendar.DAY_OF_MONTH);

        setDateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DATE_ID);
            }
        });

        startTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(START_TIME_ID);
            }
        });

        endTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(END_TIME_ID);
            }
        });

        inviteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateEvent.this,InviteFriends.class);
                startActivityForResult(intent,INVITE_FRIENDS_REQUEST);
                //showDialog(DIALOG_ID);
            }
        });

        setLocationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAutoCompleteActivity();
            }
        });

        createEventView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (eventFlag) {

                    eventName = eventNameView.getText().toString();
                    startTime = getUTCTime(startMin, startHour, day_x, month_x, year_x);
                    endTime = getUTCTime(endMin, endHour, day_x, month_x, year_x);
                    locationName = locationNameView.getText().toString();
                    date = dateView.getText().toString();
                    radiusStr = radiusView.getText().toString();

                    Calendar cur_cal = new GregorianCalendar();
                    cur_cal.setTimeInMillis(System.currentTimeMillis());
                    Log.d("Time", "" + cur_cal.toString());

                    if (attemptCreate()) {
                        radius = Integer.valueOf(radiusStr);
                        eventNameObj = new EventName();

                        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
                        //myRef.child("users");

                        databaseReference = databaseReference.child("Events").push();

                        eventUid = databaseReference.getKey();

                        databaseReference.child("Event Name").setValue(eventName);
                        databaseReference.child("Location Name").setValue(locationName);
                        databaseReference.child("Latitude").setValue(lat);
                        databaseReference.child("Longitude").setValue(lon);
                        databaseReference.child("Start time").setValue(startTime);
                        databaseReference.child("End time").setValue(endTime);
                        databaseReference.child("Radius").setValue(radius);
                        databaseReference.child("Host").setValue(mFirebaseUser.getUid());
                        databaseReference.child("Access").setValue("false");
                        databaseReference.child("Status").setValue("Not yet");
                        for (int i = 0; i < uids.size(); i++) {
                            myRef.child("users").child(uids.get(i)).child("invited events").child(eventUid).setValue("");
                            databaseReference.child("invitees").child(uids.get(i)).child("attendance").setValue(false);
                            databaseReference.child("invitees").child(uids.get(i)).child("check in").setValue("");
                            databaseReference.child("invitees").child(uids.get(i)).child("check out").setValue("");
                        }
                        myRef.child("users").child(mFirebaseUser.getUid()).child("host events").child(eventUid).setValue("true");
                        try {
                            scheduleTime();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        //eventNameObj.setEventLat(place.getLatLng().latitude);
                        //eventNameObj.setEventLon(place.getLatLng().longitude);
                        //eventNameObj.setRadius(radius);
                        Log.d("eventRadiusAttempt", "" + radius);
                        //startActivity(new Intent(CreateEvent.this,EventsList.class));
                        finish();
                    }
                }
                else {
                    saveEvent();
                }
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteEvent();
            }
        });

        deleteButton.setVisibility(View.GONE);
        Log.d("Result","eventFlag = "+eventFlag);
        if (!eventFlag){
            Bundle b = intent.getBundleExtra("event");
            if (b!=null){
                existingEvent = (Event) b.getSerializable("event");
            }
            Log.d("Result","Save");
            try {
                updateUI();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        else{
            startHour = cal.get(Calendar.HOUR_OF_DAY);
            startMin = cal.get(Calendar.MINUTE);
        }
    }

    private void saveEvent() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Events").child(existingEvent.getEventUid());

        eventUid = existingEvent.getEventUid();

        eventName = eventNameView.getText().toString();
        startTime = getUTCTime(startMin, startHour, day_x, month_x, year_x);
        endTime = getUTCTime(endMin, endHour, day_x, month_x, year_x);
        date = dateView.getText().toString();
        radiusStr = radiusView.getText().toString();
        locationName = locationNameView.getText().toString();

        myRef = FirebaseDatabase.getInstance().getReference();

        if (place!=null){
            ref.child("Latitude").setValue(lat);
            ref.child("Longitude").setValue(lon);
        }

        if (uids!=null){

            List<String> old = existingEvent.getInvitees();

            for (int i = 0; i < old.size(); i++){
                myRef.child("users").child(old.get(i)).child("invited events").child(existingEvent.getEventUid()).setValue(null);
            }
            ref.child("invitees").setValue(null);
            for (int i = 0; i < uids.size(); i++) {
                Log.d("Result",uids.get(i));
                myRef.child("users").child(uids.get(i)).child("invited events").child(existingEvent.getEventUid()).setValue("");
                ref.child("invitees").child(uids.get(i)).child("attendance").setValue(false);
                ref.child("invitees").child(uids.get(i)).child("check in").setValue("");
                ref.child("invitees").child(uids.get(i)).child("check out").setValue("");
            }
        }

        try {
            scheduleTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        ref.child("Event Name").setValue(eventName);
        ref.child("Start time").setValue(startTime);
        ref.child("End time").setValue(endTime);
        ref.child("Radius").setValue(Integer.valueOf(radiusStr));

        finish();

    }

    private void deleteEvent() {

        myRef = FirebaseDatabase.getInstance().getReference().child("users");

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Events");
        databaseReference.child(existingEvent.getEventUid()).child("invitees").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    myRef.child(snapshot.getKey()).child("invited events").child(existingEvent.getEventUid()).setValue(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        myRef.child(mFirebaseUser.getUid()).child("host events").child(existingEvent.getEventUid()).setValue(null);

        databaseReference.child(existingEvent.getEventUid()).setValue(null);
        Log.d("Result",existingEvent.getEventUid());

        finish();
    }

    private void updateUI() throws ParseException {

        eventNameView.setText(existingEvent.getEventName());
        radiusView.setText(existingEvent.getRadius()+"");
        createEventView.setText("Save");
        deleteButton.setVisibility(View.VISIBLE);
        locationNameView.setText(existingEvent.getLocationName());

        lat = existingEvent.getLatitude();
        lon = existingEvent.getLongitude();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("gmt"));

        Date d1 = sdf.parse(existingEvent.getStartTime());
        Date d2 = sdf.parse(existingEvent.getEndTime());

        startTimeView.setText(d1.getHours() + ":" + d1.getMinutes());
        endTimeView.setText(d2.getHours() + ":" + d2.getMinutes());

        startHour = d1.getHours();
        startMin = d1.getMinutes();
        endHour = d2.getHours();
        endMin = d2.getMinutes();

        dateView.setText((d1.getMonth()+1)+"-"+d1.getDay()+"-"+d1.getYear());
        //updateTime();

    }


    private void updateTime() {

        String time1 = existingEvent.getStartTime();
        String time2 = existingEvent.getEndTime();

        startHour = Integer.valueOf(time1.substring(11,13))-6;
        startMin = Integer.valueOf(time1.substring(14,16))-6;

        endHour = Integer.valueOf(time2.substring(11,13));
        endMin = Integer.valueOf(time2.substring(14,16));

        day_x = Integer.valueOf(time1.substring(8,10));
        month_x = Integer.valueOf(time1.substring(5,7))-1;
        year_x = Integer.valueOf(time1.substring(0,4));

        if (startHour<0)
            startHour = 24 + startHour;
        if (endHour<0)
            endHour = 24 + endHour;
    }

    private String getUTCTime(int min, int hour, int day, int month, int year) {
        Calendar cal = new GregorianCalendar(year,month,day,hour,min);
        long time = cal.getTimeInMillis();
        Log.d("Time",(new Date(time)).toString());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(new Date(time));
    }

    @Override
    protected Dialog onCreateDialog(int id){

        if (START_TIME_ID == id){
            return new TimePickerDialog(this,startTimePickerListener, startHour, startMin, false);
        }
        else if(END_TIME_ID == id){
            return new TimePickerDialog(this, endTimePickerListener, startHour, startMin, false);
        }
        else if(DATE_ID == id){
            return new DatePickerDialog(this,datePickerListener, year_x, month_x, day_x);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            year_x = year;
            month_x = month;
            day_x = dayOfMonth;

            dateView.setText((month_x+1)+"-"+day_x+"-"+year_x);
        }
    };

    private TimePickerDialog.OnTimeSetListener startTimePickerListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            startHour = hourOfDay;
            startMin = minute;
            Log.d("Time","endHour"+hourOfDay);

            startTimeView.setText(hourOfDay+":"+minute);
        }
    };

    private TimePickerDialog.OnTimeSetListener endTimePickerListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            endHour = hourOfDay;
            endMin = minute;
            Log.d("Time","endHour"+hourOfDay);

            endTimeView.setText(hourOfDay+":"+minute);
        }
    };

    public boolean attemptCreate() {

        boolean flag = true;

        if (eventName.isEmpty()) {
            flag=false;
            eventNameView.setError("Not a valid name");
        }
        if (!isValidTime(startTime)){
            flag=false;
            startTimeView.setError("Not a valid time");
        }
        if (!isValidTime(endTime)) {
            flag=false;
            endTimeView.setError("Not a valid time");
        }
        if (radiusStr.isEmpty()) {
            flag=false;
            radiusView.setError("Not a valid number");
        }
        return flag;
    }

    public boolean isValidTime(String time){
        time = time.trim();
        int length = time.length();
        return true;
        /*if (length!=5 && length!=4)
            return false;
        if (length == 4 && (!Character.isDigit(Integer.valueOf(time.charAt(0))) || Character.is)
            return false;*/
    }

    public boolean isValidName(String name){
        name=name.trim();
        int n=0;
        for (int i=0;i<name.length();i++){
            if (name.charAt(i)==' '){
                n++;
                if (n==2)
                    return false;
            }
            if (!Character.isLetter(name.charAt(i)))
                return false;
        }
        return true;
    }

    private void openAutoCompleteActivity(){
        try {
            // The autocomplete activity requires Google Play Services to be available. The intent
            // builder checks this and throws an exception if it is not the case.

            PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(CreateEvent.this);
            // Start the Intent by requesting a result, identified by a request code.
            startActivityForResult(intent, PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            // Indicates that Google Play Services is either not installed or not up to date. Prompt
            // the user to correct the issue.
            GoogleApiAvailability.getInstance().getErrorDialog(this, e.getConnectionStatusCode(),
                    0 /* requestCode */).show();
        } catch (GooglePlayServicesNotAvailableException e) {
            // Indicates that Google Play Services is not available and the problem is not easily
            // resolvable.
            String message = "Google Play Services is not available: " +
                    GoogleApiAvailability.getInstance().getErrorString(e.errorCode);

            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                place = PlacePicker.getPlace(this,data);
                lat = place.getLatLng().latitude;
                lon = place.getLatLng().longitude;
            }
            else{
                setLocationView.setError("Select Location");
            }
        }
        if (requestCode == INVITE_FRIENDS_REQUEST){
            uids = data.getStringArrayListExtra("invitees");
        }
    }

    private boolean exists;

    public void scheduleTime() throws ParseException {

        /*DateFormat df = DateFormat.getTimeInstance();
        df.setTimeZone(TimeZone.getTimeZone("gmt"));
        String gmtTime = df.format(new Date());*/

        Calendar startCal = new GregorianCalendar(year_x,month_x,day_x,startHour,startMin,0);
        Calendar endCal = new GregorianCalendar(year_x,month_x,day_x,endHour,endMin,0);

        Intent intent = new Intent(getBaseContext(),AlarmReceiver.class);
        intent.putExtra("Event",eventUid);
        intent.putExtra("Status","Ongoing");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(),1,intent,PendingIntent.FLAG_ONE_SHOT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Log.d("Time",startCal.toString());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,startCal.getTimeInMillis(),pendingIntent);
        }
        else{
            alarmManager.set(AlarmManager.RTC_WAKEUP,startCal.getTimeInMillis(),pendingIntent);
        }
        Intent intent2 = new Intent(getBaseContext(),AlarmReceiver.class);
        intent2.putExtra("Event",eventUid);
        intent2.putExtra("Status","Done");

        Log.d("Time",endCal.toString());

        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(getBaseContext(),2,intent2,PendingIntent.FLAG_ONE_SHOT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,endCal.getTimeInMillis(),pendingIntent2);
        }
        else{
            alarmManager.set(AlarmManager.RTC_WAKEUP,endCal.getTimeInMillis(),pendingIntent2);
        }
        //alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,startCal.getTimeInMillis(),endCal.getTimeInMillis()-startCal.getTimeInMillis(),pendingIntent);

        /*DateFormat df = DateFormat.getTimeInstance();
        df.setTimeZone(TimeZone.getTimeZone("gmt"));
        String gmtTime = df.format(new Date());

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        Date date = sdf.parse(startTime);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY,calendar.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE,calendar.get(Calendar.MINUTE));
        cal.set(Calendar.SECOND,0);
        Log.d("Time",cal.toString());

        date.setTime(cal.getTimeInMillis());
        Log.d("Time",date.toString());

        timer1 = new Timer();
        //initializeStartTimerTask();

        Date date2 = sdf.parse(endTime);

        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);

        Calendar cal2 = Calendar.getInstance();
        cal2.set(Calendar.HOUR_OF_DAY,calendar2.get(Calendar.HOUR_OF_DAY));
        cal2.set(Calendar.MINUTE,calendar2.get(Calendar.MINUTE));
        cal2.set(Calendar.SECOND,0);
        Log.d("Time",cal2.toString());

        date2.setTime(cal2.getTimeInMillis());*/

        //initializeStartTimerTask();
        //timer1.scheduleAtFixedRate(startTimerTask,cal.getTimeInMillis()-Calendar.getInstance().getTimeInMillis(),cal2.getTimeInMillis()-cal.getTimeInMillis());//,cal2.getTimeInMillis()-cal.getTimeInMillis());
        //timer1.scheduleAtFixedRate(startTimerTask,cal.getTimeInMillis()-Calendar.getInstance().getTimeInMillis(),(long)60000);
        /*timer1 = new Timer();
        ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(1);

        long initialDelay = startCal.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
        long delay = endCal.getTimeInMillis()-startCal.getTimeInMillis();
        Log.d("Time","initial delay = "+initialDelay);
        Log.d("Time","delay = "+delay);
        scheduleTaskExecutor.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("Time", "Outside here");
                    Log.d("Time",eventUid);
                    //if (!flag2) {
                    Log.d("Time", "Here " + flag1);
                    databaseReference = FirebaseDatabase.getInstance().getReference();
                    exists = true;
                    databaseReference.child("Events").child(eventUid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.d("Time",dataSnapshot.getKey());
                            if (dataSnapshot.exists())
                                exists = true;
                            else
                                exists = false;
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    Log.d("Time","exists "+exists);
                    //checkIfCorrectTime();
                    //Log.d("Time","Sup");
                    if (exists) {
                        if (flag1) {
                            Log.d("Time", "Changed Access");
                            databaseReference = FirebaseDatabase.getInstance().getReference();
                            databaseReference.child("Events").child(eventUid).child("Access").setValue("true");
                            databaseReference.child("Events").child(eventUid).child("Status").setValue("Ongoing");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Fragment_1.invitedAdapter.notifyDataSetChanged();
                                    Fragment_2.hostAdapter.notifyDataSetChanged();
                                }
                            });
                            flag1 = false;
                        } else {
                            //Log.d("Time","flag1 = false");
                            Log.d("Time", "End time");
                            databaseReference.child("Events").child(eventUid).child("Status").setValue("Done");
                            timer1.cancel();
                            //flag1=true;
                        }
                    }
                }
                catch (Exception e){
                    Log.d("Time","Error");
                }
            }
        }, initialDelay,TimeUnit.MILLISECONDS);
        long total = initialDelay + delay;
        scheduleTaskExecutor.schedule(new Runnable() {
            @Override
            public void run() {
                Log.d("Time","Second alarm");
                //System.out.println("");
                Log.d("Time", "End time");
                databaseReference = FirebaseDatabase.getInstance().getReference();
                databaseReference.child("Events").child(eventUid).child("Status").setValue("Done");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Fragment_1.invitedAdapter.notifyDataSetChanged();
                        Fragment_2.hostAdapter.notifyDataSetChanged();
                    }
                });
            }
        },total,TimeUnit.MILLISECONDS);*/

        /*Runnable task = new Runnable() {
            @Override
            public void run() {
                Log.d("Time","Outside here");
                if (flag2) {
                    Log.d("Time","Here "+flag1);
                    if (flag1) {
                        Log.d("Time", "Changed Access");
                        databaseReference = FirebaseDatabase.getInstance().getReference();
                        databaseReference.child("Events").child(eventUid).child("Access").setValue("true");
                        databaseReference.child("Events").child(eventUid).child("Status").setValue("Ongoing");
                        flag1 = false;
                    } else {
                        //Log.d("Time","flag1 = false");
                        Log.d("Time", "End time");
                        databaseReference = FirebaseDatabase.getInstance().getReference();
                        databaseReference.child("Events").child(eventUid).child("Status").setValue("Done");
                        timer1.cancel();
                        //flag1=true;
                    }
                }
                else{
                    flag2=true;
                }
            }
        };

        scheduleTaskExecutor.scheduleWithFixedDelay(task,cal.getTimeInMillis()-Calendar.getInstance().getTimeInMillis(),cal2.getTimeInMillis()-cal.getTimeInMillis(), TimeUnit.MILLISECONDS);
*/
        //timer2 = new Timer();
        //initializeEndTimerTask();
        //timer2.schedule(endTimerTask,date2);

        /*try {
            Log.d("Time","Entered");
            date = sdf.parse(time);
            Log.d("Time",date.toString());
            Calendar calendar = GregorianCalendar.getInstance();
            calendar.setTime(date);
            int hour = calendar.get(Calendar.HOUR);
            Log.d("Time",""+hour);
            int min = calendar.get(Calendar.MINUTE);
            Log.d("Time",""+min);

            Calendar cur_cal = new GregorianCalendar();
            cur_cal.setTimeInMillis(System.currentTimeMillis());//set the current time and date for this calendar
            Log.d("Time",cur_cal.toString());
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            //cal.set(Calendar.YEAR,2016);
            //cal.add(Calendar.MONTH,);
            //cal.add(Calendar.DAY_OF_YEAR, cur_cal.get(Calendar.DAY_OF_YEAR));
            //Log.d("Time",""+cur_cal.get(Calendar.DAY_OF_YEAR));
            cal.set(Calendar.HOUR_OF_DAY, hour+12);
            cal.set(Calendar.MINUTE, min);
            //cal.set(Calendar.SECOND, cur_cal.get(Calendar.SECOND));
            //cal.set(Calendar.MILLISECOND, cur_cal.get(Calendar.MILLISECOND));
            //cal.set(Calendar.DATE, cur_cal.get(Calendar.DATE));
            //cal.set(Calendar.MONTH, cur_cal.get(Calendar.MONTH));

            Long time2 = new GregorianCalendar().getTimeInMillis()+60*1000;

            Log.d("Time",cal.toString());
            Log.d("Time",""+cal.getTimeInMillis());
            Log.d("Time",""+time2);
            Intent intent = new Intent(CreateEvent.this,AlarmReceiver.class);
            intent.putExtra("Event UID", eventUid);

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,time2,1000*60,PendingIntent.getBroadcast(CreateEvent.this,0,intent,0));
            //alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), PendingIntent.getBroadcast(CreateEvent.this, 0, intent, 0));
            alarmManager.set(AlarmManager.RTC_WAKEUP,time2, PendingIntent.getBroadcast(this,1,  intent, PendingIntent.FLAG_UPDATE_CURRENT));

        } catch (ParseException e) {
            e.printStackTrace();
        }*/

    }

    private String currentTime;
    private boolean f;

    private boolean checkIfCorrectTime() {
        //return true;

        if (eventFlag){
            databaseReference = FirebaseDatabase.getInstance().getReference().child("Events").child(eventUid);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    currentTime = dataSnapshot.child("Start time").getValue(String.class);
                    Log.d("Time",currentTime);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                    try {
                        Date date = sdf.parse(currentTime);
                        Log.d("Time",""+date.getTime());
                        Log.d("Time",""+System.currentTimeMillis());
                        Log.d("Time",""+Math.abs(date.getTime() - System.currentTimeMillis()));
                        long time = Math.abs(date.getTime() - System.currentTimeMillis());
                        long k = 5000;
                        if (time <= k) {
                            f = true;
                            Log.d("Time","f = "+f);
                        }
                        else {
                            f = false;
                            Log.d("Time","f = "+f);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        Log.d("Time","return"+f);
        boolean h = f;
        return h;
        //return f;
        /*Log.d("Time",currentTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            Date date = sdf.parse(currentTime);
            Log.d("Time",""+date.getTime());
            Log.d("Time",""+System.currentTimeMillis());
            Log.d("Time",""+(date.getTime()-System.currentTimeMillis()));
            if (date.getTime() - System.currentTimeMillis() <= 5000)
                return true;
            else
                return false;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;*/
    }

    private void initializeEndTimerTask() {
        endTimerTask = new TimerTask() {
            @Override
            public void run() {
                Log.d("Time","End time");
                if (!flag2){
                    Log.d("Time","End time");
                    databaseReference = FirebaseDatabase.getInstance().getReference();
                    databaseReference.child("Events").child(eventUid).child("Status").setValue("Done");
                }
                else{
                    Log.d("Time","flag2 = false");
                    flag2 = true;
                }
            }
        };
    }

    private void initializeStartTimerTask() {
        startTimerTask = new TimerTask() {
            @Override
            public void run() {
                Log.d("Time","Outside here");
                //if (!flag2) {
                    Log.d("Time","Here "+flag1);
                    if (flag1) {
                        Log.d("Time", "Changed Access");
                        databaseReference = FirebaseDatabase.getInstance().getReference();
                        databaseReference.child("Events").child(eventUid).child("Access").setValue("true");
                        databaseReference.child("Events").child(eventUid).child("Status").setValue("Ongoing");
                        flag1 = false;
                    }
                    else {
                        //Log.d("Time","flag1 = false");
                        Log.d("Time", "End time");
                        databaseReference = FirebaseDatabase.getInstance().getReference();
                        databaseReference.child("Events").child(eventUid).child("Status").setValue("Done");
                        timer1.cancel();
                        //flag1=true;
                    }
                //}
                //else{Log.d("Time","flag2 = false");
                //    flag2=false;
                //}
            }
        };
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
                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mFirebaseUser = null;
                FirebaseAuth.getInstance().signOut();
                //mUsername = ANONYMOUS;
                //mPhotoUrl = null;
                startActivity(new Intent(this, LoginActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public static void updateStatus(String eventUid, String status) {

        databaseReference2 = FirebaseDatabase.getInstance().getReference().child("Events").child(eventUid);
        databaseReference2.setValue(status);
    }
}
