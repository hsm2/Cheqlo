package com.example.harishmanikantan.verifyd;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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

/**
 * Created by harishmanikantan on 1/20/17.
 */

public class Fragment_1 extends Fragment {

    private ListView invitedEvents;

    private DatabaseReference database;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private List<String> invitedEventUids;
    private List<String> invitedEventNames;
    private List<Event> invitedEventList;

    private int count;

    public static Context context;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_one,container,false);

        database = FirebaseDatabase.getInstance().getReference();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        invitedEventUids = new ArrayList<String>();
        invitedEventNames = new ArrayList<String>();
        invitedEventList = new ArrayList<Event>();

        invitedEvents = (ListView) view.findViewById(R.id.invitedList);

        //invitedEventList = MainTabs.getInvitedEventList();
        //invitedEventNames = MainTabs.getInvitedEventNames();

        //Log.d("Result",invitedEventNames.get(0));
        //makeLists();

        invitedEvents.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Event selectedEvent = invitedEventList.get(position);
                String status = selectedEvent.getStatus();
                if (status.equals("Ongoing")) {

                    Intent intent = new Intent(getActivity(),Result.class);
                    Bundle b = new Bundle();
                    b.putSerializable("Event", (Serializable) selectedEvent);
                    intent.putExtra("Event",b);
                    startActivity(intent);

                    /*EventName eventName = new EventName();

                    eventName.setEventLat(selectedEvent.getLatitude());
                    eventName.setEventLon(selectedEvent.getLongitude());
                    eventName.setRadius(selectedEvent.getRadius());
                    eventName.setEventUid(selectedEvent.getEventUid());

                    startActivity(new Intent(getActivity(), EventName.class));*/
                }
                else if (status.equals("Not yet")){
                    /*Intent intent = new Intent(getActivity(),Result.class);
                    Bundle b = new Bundle();
                    b.putSerializable("Event", (Serializable) selectedEvent);
                    intent.putExtra("Event",b);
                    startActivity(intent);*/
                    Toast.makeText(getContext(),"It's not yet time!",Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getContext(),"The event is over",Toast.LENGTH_SHORT).show();
                }
            }
        });

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.create_event);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), CreateEvent.class));
            }
        });

        loadEvents();

        return view;
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

    public void makeLists(){
        Log.d("Result","makeLists");
        invitedAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, invitedEventNames);
        invitedEvents.setAdapter(invitedAdapter);
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
                    invitedEventList.get(index).setHost(dataSnapshot.child("Host").getValue(String.class));

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
