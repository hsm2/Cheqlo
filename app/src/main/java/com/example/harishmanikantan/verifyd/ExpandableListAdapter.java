package com.example.harishmanikantan.verifyd;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

public class ExpandableListAdapter extends BaseExpandableListAdapter{

    private List<String> headerItems;
    private HashMap<String,List<String>> childItems;
    private Context context;

    public ExpandableListAdapter(Context context,List<String> headerItems, HashMap<String,List<String>> childItems){
        this.context = context;
        this.headerItems = headerItems;
        this.childItems = childItems;
    }

    @Override
    public int getGroupCount() {
        return headerItems.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return childItems.get(headerItems.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return headerItems.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childItems.get(headerItems.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        String headerTitle = (String) getGroup(groupPosition);

        if (convertView==null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item,null);
        }

        TextView textView = (TextView) convertView.findViewById(R.id.list_item_primary);
        //textView.setTypeface(null, Typeface.BOLD);
        textView.setText(headerTitle);

        TextView textView2 = (TextView) convertView.findViewById(R.id.list_item_secondary);
        textView2.setText("Wednesday, 6:30 PM");

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        String childText = (String) getChild(groupPosition,childPosition);

        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.child_item,null);
        }

        TextView textView = (TextView) convertView.findViewById(R.id.child);
        textView.setText(childText);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
