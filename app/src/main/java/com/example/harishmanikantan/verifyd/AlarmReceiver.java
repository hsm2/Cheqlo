package com.example.harishmanikantan.verifyd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class AlarmReceiver extends BroadcastReceiver
{

    private String eventUid;
    private String status;
    private String time;
    private DatabaseReference databaseReference;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Time","Alarm Received");

        eventUid = intent.getStringExtra("Event");
        status = intent.getStringExtra("Status");

        Log.d("Time","Status = " + status);
        Log.d("Time","eventUid = " + eventUid);

        if (status.equals("Ongoing")) {
            time = "Start time";
        }
        else {
            time = "End time";
        }

        Log.d("Time",time);

        //CreateEvent.updateStatus(eventUid, status);
        FirebaseApp.initializeApp(context);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference().child("Events").child(eventUid);
        //databaseReference.child("Status").setValue(status);
        //databaseReference.child("Events").child(eventUid).child("Status").setValue(status);
        checkIfCorrectTime();
        /*if (checkIfCorrectTime())
            databaseReference.child("Status").setValue(status);*/
    }

    private String currentTime;
    private boolean f;

    private boolean checkIfCorrectTime() {
       //final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentTime = dataSnapshot.child(time).getValue(String.class);
                Log.d("Time",currentTime);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

                try {
                    Date date = sdf.parse(currentTime);
                    long time1 = Math.abs(date.getTime() - System.currentTimeMillis());
                    Log.d("Time",time + "");
                    Log.d("Time",time1+"");
                    if (time1 < 20000) {
                        f = true;
                        databaseReference.child("Status").setValue(status);
                    }
                    else {
                        f = false;
                    }
                    Log.d("Time","f = "+f);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
        Log.d("Time",f+"");
        return f;
    }

    /*@Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d("Time","Alarm Received");
        eventUid = intent.getStringExtra("Access");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Events").child(eventUid).child("Access");
        databaseReference.setValue("true");
    }*/


}