package com.example.harishmanikantan.verifyd;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Sample extends AppCompatActivity {

    ExpandableListAdapter expandableListAdapter;
    ExpandableListView expandableListView;
    List<String> headerItems;
    HashMap<String, List<String>> childItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        expandableListView = (ExpandableListView) findViewById(R.id.expandableView);

        prepareListData();

        expandableListAdapter = new ExpandableListAdapter(this,headerItems,childItems);

        expandableListView.setAdapter(expandableListAdapter);

    }

    private void prepareListData() {
        headerItems = new ArrayList<String>();
        childItems = new HashMap<String, List<String>>();

        // Adding child data
        headerItems.add("Top 250");
        headerItems.add("Now Showing");
        headerItems.add("Coming Soon..");

        // Adding child data
        List<String> top250 = new ArrayList<String>();
        top250.add("The Shawshank Redemption");
        top250.add("The Godfather");
        top250.add("The Godfather: Part II");
        top250.add("Pulp Fiction");
        top250.add("The Good, the Bad and the Ugly");
        top250.add("The Dark Knight");
        top250.add("12 Angry Men");

        List<String> nowShowing = new ArrayList<String>();
        nowShowing.add("The Conjuring");
        nowShowing.add("Despicable Me 2");
        nowShowing.add("Turbo");
        nowShowing.add("Grown Ups 2");
        nowShowing.add("Red 2");
        nowShowing.add("The Wolverine");

        List<String> comingSoon = new ArrayList<String>();
        comingSoon.add("2 Guns");
        comingSoon.add("The Smurfs 2");
        comingSoon.add("The Spectacular Now");
        comingSoon.add("The Canyons");
        comingSoon.add("Europa Report");

        childItems.put(headerItems.get(0), top250); // Header, Child data
        childItems.put(headerItems.get(1), nowShowing);
        childItems.put(headerItems.get(2), comingSoon);
    }
}
