package com.example.harishmanikantan.verifyd;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.example.harishmanikantan.verifyd.R.attr.icon;

public class EventsList extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference database;

    private ListView hostEvents;
    private ListView invitedEvents;

    private List<String> hostEventUids;
    private List<String> invitedEventUids;
    private List<String> hostEventNames2;
    private List<String> invitedEventNames2;
    private List<String> hostEventNames;
    private List<String> invitedEventNames;
    private List<Event> hostEventList;
    private List<Event> invitedEventList;

    private static ProgressDialog progressDialog;

    private int count;

    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;
        super.onCreate(savedInstanceState);
        //Log.d("Result","count!=0");
        setContentView(R.layout.activity_events_list);
        //progressDialog = ProgressDialog.show(this, "", "Loading...", true);
        hostEventUids = new ArrayList<String>();
        hostEventNames = new ArrayList<String>();
        invitedEventUids = new ArrayList<String>();
        invitedEventNames = new ArrayList<String>();
        hostEventList = new ArrayList<Event>();
        invitedEventList = new ArrayList<Event>();
        invitedEventNames2 = new ArrayList<String>();
        hostEventNames2 = new ArrayList<String>();

        hostEvents = (ListView) findViewById(R.id.hostList);
        invitedEvents = (ListView) findViewById(R.id.invitedList);

        database = FirebaseDatabase.getInstance().getReference();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        Log.d("Result","count=0");
        //progressDialog.dismiss();
        //setContentView(R.layout.activity_events_list);
        invitedEvents.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Event selectedEvent = invitedEventList.get(position);
                String status = selectedEvent.getStatus();
                if (status.equals("Ongoing")) {

                    EventName eventName = new EventName();

                    eventName.setEventLat(selectedEvent.getLatitude());
                    eventName.setEventLon(selectedEvent.getLongitude());
                    eventName.setRadius(selectedEvent.getRadius());
                    eventName.setEventUid(selectedEvent.getEventUid());

                    startActivity(new Intent(EventsList.this, EventName.class));
                }
                else if (status.equals("Not yet")){
                    Toast.makeText(EventsList.this,"It's not yet time!",Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(EventsList.this,"The event is over",Toast.LENGTH_SHORT).show();
                }
            }
        });

        hostEvents.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Event selectedEvent = hostEventList.get(position);
                String status = selectedEvent.getStatus();
                Log.d("Time",status);
                if (status.equals("Done")){
                    Intent intent = new Intent(EventsList.this,HostEventData.class);
                    Bundle b = new Bundle();
                    b.putSerializable("Host Event", (Serializable) hostEventList.get(position));
                    intent.putExtra("Host Event",b);
                    startActivity(intent);
                }
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.create_event);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EventsList.this, CreateEvent.class));
            }
        });

        loadEvents();
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

    public void loadEvents(){
        Log.d("Result","load events");
        database.child("users").child(mFirebaseUser.getUid()).child("host events").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snap:dataSnapshot.getChildren()){
                    Log.d("Result",snap.getKey());
                    hostEventUids.add(snap.getKey());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        database.child("users").child(mFirebaseUser.getUid()).child("invited events").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snap:dataSnapshot.getChildren()){
                    invitedEventUids.add(snap.getKey());
                }
                makeEvents();
                //while (Event.count!=0){

                //}
                //loadLists();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void makeEvents(){

        count = hostEventUids.size() + invitedEventUids.size();

        for (int i = 0;i<hostEventUids.size();i++){
            hostEventList.add(new Event(hostEventUids.get(i)));
            hostEventNames.add("");
            loadingHostEvent(hostEventUids.get(i),i);
        }

        for (int i = 0;i<invitedEventUids.size();i++){
            invitedEventList.add(new Event(invitedEventUids.get(i)));
            invitedEventNames.add("");
            loadingInvitedEvent(invitedEventUids.get(i),i);
            //invitedEventList.add(new Event(invitedEventUids.get(i)));
        }
    }

    public void loadLists(){
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (hostEventList!=null) {
            Log.d("Result","sup");
            for (int i = 0; i < hostEventUids.size(); i++) {
                Log.d("Result",hostEventUids.get(i));
                hostEventNames2.add(hostEventList.get(i).getEventName());
                //Log.d("Result",event.getEventName());
            }
            ArrayAdapter<String> hostAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,hostEventNames);
            hostEvents.setAdapter(hostAdapter);
        }
        else{
            Log.d("Result","null");
        }

        if (invitedEventList!=null) {

            for (int i = 0; i < invitedEventUids.size(); i++) {
                invitedEventNames2.add(invitedEventList.get(i).getEventName());
            }
            //ArrayAdapter<String> invitedAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, invitedEventNames);
            //invitedEvents.setAdapter(invitedAdapter);
        }

        else
        {
            Log.d("Result","null");
        }

        /*for (int i = 0; i < invitedEventUids.size(); i++) {
            invitedEventNames.add(invitedEventList.get(i).getEventName());
            Log.d("Result","2nd loop");
            Log.d("Result",invitedEventList.get(i).getStartTime());
        }
        ArrayAdapter<String> invitedAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, invitedEventNames);
        invitedEvents.setAdapter(invitedAdapter);

        for (int i = 0; i < hostEventUids.size(); i++) {
            //Log.d("Result",hostEventUids.get(i));
            hostEventNames.add(hostEventList.get(i).getEventName());
            Log.d("Result",hostEventList.get(i).getStartTime());
        }
        ArrayAdapter<String> hostAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,hostEventNames);
        hostEvents.setAdapter(hostAdapter);*/
    }

    public static ArrayAdapter<String> invitedAdapter;
    public static ArrayAdapter<String> hostAdapter;

    public void makeLists(){
        Log.d("Result","makeLists");
        invitedAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, invitedEventNames);
        invitedEvents.setAdapter(invitedAdapter);

        hostAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,hostEventNames);
        hostEvents.setAdapter(hostAdapter);

    }

    public void loadingHostEvent(final String eventUid, final int index){
        database.child("Events").child(eventUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //hostEventList.add(new Event(eventUid));

                    Log.d("Result", "Host Event listener");

                    //int index = hostEventList.size() - 1;
                    Log.d("Result","Index = "+index);
                    String eventName = dataSnapshot.child("Event Name").getValue(String.class);

                    hostEventList.get(index).setEndTime(dataSnapshot.child("End time").getValue(String.class));
                    hostEventList.get(index).setEventName(eventName);
                    hostEventList.get(index).setLatitude(dataSnapshot.child("Latitude").getValue(Double.class));
                    hostEventList.get(index).setLocationName(dataSnapshot.child("Location Name").getValue(String.class));
                    hostEventList.get(index).setLongitude(dataSnapshot.child("Longitude").getValue(Double.class));
                    hostEventList.get(index).setRadius(dataSnapshot.child("Radius").getValue(Integer.class));
                    hostEventList.get(index).setStartTime(dataSnapshot.child("Start time").getValue(String.class));
                    hostEventList.get(index).setAccess(dataSnapshot.child("Access").getValue(String.class));
                    hostEventList.get(index).setStatus(dataSnapshot.child("Status").getValue(String.class));

                    hostEventNames.set(index,eventName);

                    Log.d("Result", hostEventList.get(index).getEventName());

                    List<String> invitees = new ArrayList<String>();
                    List<Boolean> attendance = new ArrayList<Boolean>();

                    invitees.clear();
                    attendance.clear();

                    for (DataSnapshot snapshot : dataSnapshot.child("invitees").getChildren()) {
                        invitees.add(snapshot.getKey());
                        attendance.add(snapshot.child("attendance").getValue(Boolean.class));
                    }

                    hostEventList.get(index).setInvitees(invitees);
                    hostEventList.get(index).setAttendance(attendance);

                    count--;

                    Log.d("Result", "" + count);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void loadingInvitedEvent(final String eventUid, final int index){
        database.child("Events").child(eventUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //invitedEventList.add(new Event(eventUid));

                    Log.d("Result", "Invited Event listener");

                    //int index = invitedEventList.size() - 1;

                    String eventName = dataSnapshot.child("Event Name").getValue(String.class);

                    invitedEventList.get(index).setEndTime(dataSnapshot.child("End time").getValue(String.class));
                    invitedEventList.get(index).setEventName(eventName);
                    invitedEventList.get(index).setLatitude(dataSnapshot.child("Latitude").getValue(Double.class));
                    invitedEventList.get(index).setLocationName(dataSnapshot.child("Location Name").getValue(String.class));
                    invitedEventList.get(index).setLongitude(dataSnapshot.child("Longitude").getValue(Double.class));
                    invitedEventList.get(index).setRadius(dataSnapshot.child("Radius").getValue(Integer.class));
                    invitedEventList.get(index).setStartTime(dataSnapshot.child("Start time").getValue(String.class));
                    invitedEventList.get(index).setAccess(dataSnapshot.child("Access").getValue(String.class));
                    invitedEventList.get(index).setStatus(dataSnapshot.child("Status").getValue(String.class));

                    invitedEventNames.set(index,eventName);

                    Log.d("Result", invitedEventList.get(index).getEventName());

                    List<String> invitees = new ArrayList<String>();

                    invitees.clear();

                    for (DataSnapshot snapshot : dataSnapshot.child("invitees").getChildren()) {
                        invitees.add(snapshot.getKey());
                    }

                    invitedEventList.get(index).setInvitees(invitees);

                    count--;

                    if (count == 0) {
                        makeLists();
                    }

                    Log.d("Result", "" + count);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}