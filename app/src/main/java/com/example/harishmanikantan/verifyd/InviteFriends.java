package com.example.harishmanikantan.verifyd;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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

public class InviteFriends extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private ListView friendsView;
    private DatabaseReference databaseReference;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friends);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        friendsView = (ListView) findViewById(R.id.friends);
        final List<GoogleUser> users = getUsersList();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.create_list);

        //users.add(new GoogleUser(user.getDisplayName(),user.getPhotoUrl()));
        friendsView.setAdapter(new FriendAdapter(this, R.layout.friends_view, users));
        friendsView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        /*friendsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (view.isSelected())
                    view.setSelected(false);
                else
                    view.setSelected(true);
            }
        });*/
        final List<String> uids = new ArrayList<String>();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < users.size(); i++) {
                    GoogleUser googleUser = users.get(i);
                    if (googleUser.isChecked()) {
                        uids.add(googleUser.getUid());
                        //databaseReference.child("Events").child("invitees").child(googleUser.getUid()).setValue("true");
                    }
                    else {
                        //uids.remove(googleUser.getUid());
                        //databaseReference.child("Events").child("invitees").child(googleUser.getUid()).removeValue();
                    }
                }
                Intent returnIntent = new Intent();
                returnIntent.putExtra("invitees", (ArrayList<String>) uids);
                setResult(2,returnIntent);
                finish();
            }
        });
    }

    List<GoogleUser> users = new ArrayList<GoogleUser>();

    public List<GoogleUser> getUsersList(){
        databaseReference.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snap:dataSnapshot.getChildren()){

                    Log.d("Result",snap.child("name").getValue(String.class));
                    Log.d("Result",snap.child("photo url").getValue(String.class));
                    Log.d("Result",""+Uri.parse(snap.child("photo url").getValue(String.class)));

                    String name = snap.child("name").getValue(String.class);
                    Uri uri  = Uri.parse(snap.child("photo url").getValue(String.class));
                    String uid = snap.getKey();

                    users.add(new GoogleUser(name,uri,uid));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return users;

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mFirebaseUser = null;
                FirebaseAuth.getInstance().signOut();
                //mUsername = ANONYMOUS;
                //mPhotoUrl = null;
                startActivity(new Intent(this, LoginActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
