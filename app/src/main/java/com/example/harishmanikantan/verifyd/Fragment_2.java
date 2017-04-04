package com.example.harishmanikantan.verifyd;

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

public class Fragment_2 extends Fragment {


    private ListView hostEvents;

    private DatabaseReference database;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private List<String> hostEventUids;
    private List<String> hostEventNames;
    private List<Event> hostEventList;

    private int count;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        View view = inflater.inflate(R.layout.fragment_two,container,false);

        database = FirebaseDatabase.getInstance().getReference();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        hostEventUids = new ArrayList<String>();
        hostEventNames = new ArrayList<String>();
        hostEventList = new ArrayList<Event>();

        hostEvents = (ListView) view.findViewById(R.id.hostList);

        hostEvents.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Event selectedEvent = hostEventList.get(position);
                String status = selectedEvent.getStatus();
                Log.d("Time",status);
                if (status.equals("Done")){
                    Intent intent = new Intent(getActivity(),HostEventData.class);
                    Bundle b = new Bundle();
                    b.putSerializable("Host Event", (Serializable) hostEventList.get(position));
                    intent.putExtra("Host Event",b);
                    startActivity(intent);
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
        database.child("users").child(mFirebaseUser.getUid()).child("host events").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                hostEventUids.clear();
                for (DataSnapshot snap:dataSnapshot.getChildren()){
                    hostEventUids.add(snap.getKey());
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

        count = hostEventUids.size();
        hostEventList.clear();
        hostEventNames.clear();

        for (int i = 0;i<hostEventUids.size();i++){
            hostEventList.add(new Event(hostEventUids.get(i)));
            hostEventNames.add("");
            loadingInvitedEvent(hostEventUids.get(i),i);
            //hostEventList.add(new Event(hostEventUids.get(i)));
        }
    }

    public static EventAdapter hostAdapter;

    public void makeLists(){
        Log.d("Result","makeLists");
        hostAdapter = new EventAdapter(getActivity().getBaseContext(),R.layout.list_item_event,hostEventList);
        hostEvents.setAdapter(hostAdapter);
    }

    public void loadingInvitedEvent(final String eventUid, final int index){
        database.child("Events").child(eventUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //hostEventList.add(new Event(eventUid));

                    Log.d("Result", "Invited Event listener");

                    //int index = hostEventList.size() - 1;

                    String eventName = dataSnapshot.child("Event Name").getValue(String.class);

                    hostEventList.get(index).setEndTime(dataSnapshot.child("End time").getValue(String.class));
                    hostEventList.get(index).setEventName(eventName);
                    hostEventList.get(index).setHost(dataSnapshot.child("Host").getValue(String.class));
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
