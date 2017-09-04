package com.lets.chat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfile extends AppCompatActivity implements View.OnClickListener {

    CircleImageView user_pic;
    TextView user_name, user_status;
    Button friend_req;

    private String current_state;
    String user_id;

    DatabaseReference mDatabase;
    DatabaseReference request_database;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        user_pic = (CircleImageView) findViewById(R.id.profile_user_pic);
        user_name = (TextView) findViewById(R.id.profile_DisplayName);
        user_status = (TextView) findViewById(R.id.profile_status);
        friend_req = (Button) findViewById(R.id.profile_friend_request);
        friend_req.setOnClickListener(this);

         user_id = getIntent().getStringExtra("user_id");
        current_state = "not_friends";

        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(user_id);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                updateUi(name, status, image);
                Log.i("INFO", name + status + image);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        request_database = FirebaseDatabase.getInstance().getReference().child("friend_req");
        mAuth = FirebaseAuth.getInstance();


    }

    private void updateUi(String name, String status, String image) {
        user_name.setText(name);
        user_status.setText(status);
        Picasso.with(this).load(image)
                .placeholder(R.drawable.noun_323186_cc)
                .into(user_pic);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.profile_friend_request:
                sendReq();
                break;
        }
    }

    private void sendReq() {
        if (current_state.equals("not_friends")) {
            request_database.child(mAuth.getCurrentUser().getUid()).child(user_id).child("request_type").setValue("sent").addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    request_database.child(user_id).child(mAuth.getCurrentUser().getUid()).child("request_type").setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(UserProfile.this, "Sent", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }
}
