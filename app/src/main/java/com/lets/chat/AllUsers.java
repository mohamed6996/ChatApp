package com.lets.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AllUsers extends AppCompatActivity {
    Toolbar toolbar;
    RecyclerView recyclerView;

    DatabaseReference reference;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);
        toolbar = (Toolbar) findViewById(R.id.usersToolbar);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview_alluser);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("users");
        reference.keepSynced(true);

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<UserModel, UserViewHolder> adapter = new FirebaseRecyclerAdapter<UserModel, UserViewHolder>
                (UserModel.class, R.layout.user_list_item, UserViewHolder.class, reference) {

            @Override
            protected void populateViewHolder(UserViewHolder viewHolder, UserModel model, int position) {

                final String user_id = getRef(position).getKey();
              /*  if (user_id.equals(mAuth.getCurrentUser().getUid())) {
                    viewHolder.itemView.setVisibility(View.GONE);
                    return;
                }*/
                viewHolder.setName(model.getName());
                viewHolder.setStatus(model.getStatus());
                viewHolder.setImage(model.getThumb_image(), AllUsers.this);

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(AllUsers.this, UserProfile.class);
                        intent.putExtra("user_id", user_id);
                        startActivity(intent);
                    }
                });

            }


        };

        recyclerView.setAdapter(adapter);
    }
}
