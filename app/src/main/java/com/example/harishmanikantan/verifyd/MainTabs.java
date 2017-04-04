package com.example.harishmanikantan.verifyd;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

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

import java.util.ArrayList;
import java.util.List;

public class MainTabs extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static FirebaseAuth mFirebaseAuth;
    private static FirebaseUser mFirebaseUser;
    private static GoogleApiClient mGoogleApiClient;

    private DatabaseReference database;

    private List<String> invitedEventUids;
    private static List<String> invitedEventNames;
    private static List<Event> invitedEventList;

    private int count;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tabs);

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        database = FirebaseDatabase.getInstance().getReference();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        tabLayout.addTab(tabLayout.newTab().setText("Home"));
        tabLayout.addTab(tabLayout.newTab().setText("Host"));
        tabLayout.addTab(tabLayout.newTab().setText("Profile"));

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        final PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());

        viewPager.setAdapter(pagerAdapter);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        //loadEvents();
    }


    public void loadEvents(){
        Log.d("Result","load events");
        database.child("users").child(mFirebaseUser.getUid()).child("invited events").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                invitedEventUids.clear();
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

        count = invitedEventUids.size();
        invitedEventList.clear();
        invitedEventNames.clear();
        for (int i = 0;i<invitedEventUids.size();i++){
            invitedEventList.add(new Event(invitedEventUids.get(i)));
            invitedEventNames.add("");
            loadingInvitedEvent(invitedEventUids.get(i),i);
            //invitedEventList.add(new Event(invitedEventUids.get(i)));
        }
    }

    public static ArrayAdapter<String> invitedAdapter;

    public static List<Event> getInvitedEventList() {
        return invitedEventList;
    }

    public static List<String> getInvitedEventNames() {
        return invitedEventNames;
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
                        //makeLists();
                    }

                    Log.d("Result", "" + count);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static void signOut(){

        mFirebaseAuth.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        mFirebaseUser = null;
        FirebaseAuth.getInstance().signOut();
        //mUsername = ANONYMOUS;
        //mPhotoUrl = null;

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
