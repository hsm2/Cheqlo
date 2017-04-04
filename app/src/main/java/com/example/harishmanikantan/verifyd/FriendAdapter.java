package com.example.harishmanikantan.verifyd;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * Created by harishmanikantan on 11/21/16.
 */

public class FriendAdapter extends ArrayAdapter<GoogleUser> {

    private Context context;
    private int resource;
    private LayoutInflater inflater;

    private DatabaseReference databaseReference;
    private List<GoogleUser> usersList;


    public FriendAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public FriendAdapter(Context context, int resource, List<GoogleUser> objects) {
        super(context, resource, objects);
        this.resource=resource;
        this.context=context;
        inflater = LayoutInflater.from(context);
        usersList=objects;
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        convertView = (LinearLayout) inflater.inflate(resource,null);
        final GoogleUser user;
        user=usersList.get(position);

        user.setChecked(false);

        TextView nameView = (TextView) convertView.findViewById(R.id.email_id);
        nameView.setText(user.getName());

        ImageView proPic = (ImageView) convertView.findViewById(R.id.profile_picture);
        Picasso.with(context).load(user.getPhotoUrl()).into(proPic);

        CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                user.setChecked(isChecked);
            }
        });

        return convertView;

    }
}
