package com.example.harishmanikantan.verifyd;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Event implements Serializable{

    private String endTime;
    private String eventName;
    private double latitude;
    private String locationName;
    private double longitude;
    private int radius;
    private String host;
    private String startTime;
    private String eventUid;
    private String access;
    List<String> invitees;
    private String status;
    List<Boolean> attendance;

    public static int count;

    private DatabaseReference databaseReference;

    public Event(String eventUid) {
        invitees = new ArrayList<>();
        this.eventUid = eventUid;
        /*databaseReference = FirebaseDatabase.getInstance().getReference();
        Log.d("Result","In event 1");
        databaseReference.child("Events").child("-KXSgSIeo4P-d0xfCVA0").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("Result","in Event listener");
                endTime = dataSnapshot.child("End time").getValue(String.class);
                eventName = dataSnapshot.child("Event Name").getValue(String.class);
                latitude = dataSnapshot.child("Latitude").getValue(Double.class);
                locationName = dataSnapshot.child("Location Name").getValue(String.class);
                longitude = dataSnapshot.child("Longitude").getValue(Double.class);
                radius = dataSnapshot.child("Radius").getValue(Integer.class);
                startTime = dataSnapshot.child("Start time").getValue(String.class);
                Log.d("Result",startTime);
                for (DataSnapshot snapshot : dataSnapshot.child("invitees").getChildren()){
                    invitees.add(snapshot.getKey());
                }
                count--;
                Log.d("Result",""+count);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/

    }

    public String getEventUid() {
        return eventUid;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getRadius() {
        return radius;
    }

    public List<String> getInvitees() {
        return invitees;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getEventName() {
        return eventName;
    }

    public String getLocationName() {
        return locationName;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getAccess() {
        return access;
    }

    public String getStatus() {
        return status;
    }

    public String getHost(){
        return host;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setInvitees(List<String> invitees) {
        this.invitees = invitees;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setAttendance(List<Boolean> attendance) {
        this.attendance = attendance;
    }

    public List<Boolean> getAttendance() {
        return attendance;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
