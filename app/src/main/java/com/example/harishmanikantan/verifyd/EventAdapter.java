package com.example.harishmanikantan.verifyd;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import static android.app.PendingIntent.getActivity;

/**
 * Created by harishmanikantan on 1/24/17.
 */

public class EventAdapter extends ArrayAdapter<Event> {

    private Context context;
    private int resource;
    private LayoutInflater inflater;

    private DatabaseReference databaseReference;
    private List<Event> eventList;

    public EventAdapter(Context context, int resource, List<Event> eventList) {

        super(context, resource, eventList);

        this.context = context;
        this.resource = resource;
        this.eventList = eventList;

        inflater = LayoutInflater.from(context);

        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent){

        convertView = (LinearLayout) inflater.inflate(resource,null);

        TextView eventName = (TextView) convertView.findViewById(R.id.event_name);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.edit_event);

        eventName.setText(eventList.get(position).getEventName());

        if (eventList.get(position).getStatus().equals("Not yet")) {
            imageView.setImageResource(R.drawable.ic_edit);
        }
        else{
            imageView.setVisibility(View.GONE);
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onEditClicked(position);
            }
        });

        return convertView;
    }

    public void onEditClicked(int position){

        if (eventList.get(position).getStatus().equals("Not yet")) {

            Bundle b = new Bundle();
            b.putSerializable("event",eventList.get(position));

            Intent intent = new Intent(context, CreateEvent.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("new?", false);
            intent.putExtra("event", b);

            context.startActivity(intent);
        }
    }

}
