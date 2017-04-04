package com.example.harishmanikantan.verifyd;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HostEventData extends AppCompatActivity {

    private ListView listView;
    private Event event;
    private ArrayList<String> attendedNames;
    private ArrayList<String> attended;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_event_data);
        attendedNames = new ArrayList<>();

        listView = (ListView) findViewById(R.id.list);

        Bundle b = this.getIntent().getBundleExtra("Host Event");
        if (b!=null){
            event = (Event) b.getSerializable("Host Event");
        }

        updateUI();
    }

    private void updateUI() {

        List<String> invitees = event.getInvitees();
        List<Boolean> attendance = event.getAttendance();
        attended = new ArrayList<>();
        for (int i = 0;i<attendance.size();i++){
            if (attendance.get(i))
                attended.add(invitees.get(i));
        }

        getAttendedNames();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(HostEventData.this,android.R.layout.simple_list_item_1,attendedNames);
        listView.setAdapter(arrayAdapter);
    }


    public void getAttendedNames() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        for (int i = 0;i<attended.size();i++){
            databaseReference.child(attended.get(i)).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    attendedNames.add(dataSnapshot.child("name").getValue(String.class));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        //return attendedNames;
    }
}
