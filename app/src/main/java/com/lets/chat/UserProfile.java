package com.lets.chat;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
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
    Button friend_req, cancle_friend_req_btn;

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
        cancle_friend_req_btn = (Button) findViewById(R.id.profile_cancle_friend_request);
        cancle_friend_req_btn.setVisibility(View.GONE);
        cancle_friend_req_btn.setOnClickListener(this);

        user_id = getIntent().getStringExtra("user_id");
        current_state = "not_friends";

        request_database = FirebaseDatabase.getInstance().getReference().child("users");
        mAuth = FirebaseAuth.getInstance();
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


        request_database.child(mAuth.getCurrentUser().getUid()).child("friend_req").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(user_id)) {
                    String request_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                    if (request_type.equals("received")) {
                        friend_req.setText("Accept Friend Request");
                        current_state = "friends";
                        cancle_friend_req_btn.setVisibility(View.VISIBLE);
                    } else {
                        friend_req.setText("cancel friend request");
                        current_state = "req_sent";
                        cancle_friend_req_btn.setVisibility(View.GONE);
                    }

                } else {
                    // check if they are friends or not
                    isFriend();


                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void isFriend() {
        request_database.child(mAuth.getCurrentUser().getUid()).child("friends").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(user_id)) {
                    current_state = "friends";
                    friend_req.setText("unfriend this person");
                } else {
                    friend_req.setText("send friend request");
                    current_state = "not_friends";
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
                if (friend_req.getText().equals("send friend request")) {
                    sendReq();
                } else if (friend_req.getText().equals("Accept Friend Request")) {
                    acceptRequest();
                } else if (friend_req.getText().equals("unfriend this person")) {
                    unfriend();
                } else {
                    cancelReq();
                }

                break;
            case R.id.profile_cancle_friend_request:
                request_database.child(mAuth.getCurrentUser().getUid()).child("friend_req").child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        request_database.child(user_id).child("friend_req").child(mAuth.getCurrentUser().getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                cancle_friend_req_btn.setVisibility(View.GONE);
                                Toast.makeText(UserProfile.this, "request canceled", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                break;
        }
    }

    private void unfriend() {
        request_database.child(mAuth.getCurrentUser().getUid()).child("friends").child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                request_database.child(user_id).child("friends").child(mAuth.getCurrentUser().getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(UserProfile.this, "Done", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void acceptRequest() {
        request_database.child(mAuth.getCurrentUser().getUid()).child("friends").child(user_id).setValue("done").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                request_database.child(user_id).child("friends").child(mAuth.getCurrentUser().getUid()).setValue("done").addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        request_database.child(mAuth.getCurrentUser().getUid()).child("friend_req").child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                request_database.child(user_id).child("friend_req").child(mAuth.getCurrentUser().getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(UserProfile.this, "request canceled", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }


    private void sendReq() {
        if (current_state.equals("not_friends")) {

            request_database.child(mAuth.getCurrentUser().getUid()).child("friend_req").child(user_id).child("request_type").setValue("sent").addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    request_database.child(user_id).child("friend_req").child(mAuth.getCurrentUser().getUid()).child("request_type").setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });

        }
    }

    private void cancelReq() {
        if (current_state.equals("req_sent")) {

            request_database.child(mAuth.getCurrentUser().getUid()).child("friend_req").child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    request_database.child(user_id).child("friend_req").child(mAuth.getCurrentUser().getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(UserProfile.this, "request canceled", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

        }
    }
}
