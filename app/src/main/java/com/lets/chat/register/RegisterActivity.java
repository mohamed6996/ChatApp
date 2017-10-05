package com.lets.chat.register;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.lets.chat.MainActivity;
import com.lets.chat.R;
import com.lets.chat.utility.Utility;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    EditText edt_name, edt_email, edt_password;
    Button btn_register;
    Toolbar toolbar;
    ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    Utility utility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        edt_name = (EditText) findViewById(R.id.register_name_edt);
        edt_email = (EditText) findViewById(R.id.register_email_edt);
        edt_password = (EditText) findViewById(R.id.register_password_edt);
        btn_register = (Button) findViewById(R.id.register_btn_reg);
        btn_register.setOnClickListener(this);

        toolbar = (Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Registering User");
        progressDialog.setMessage("Please wait while we create your account");
        progressDialog.setCanceledOnTouchOutside(false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");

        utility = new Utility(this);


    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.register_btn_reg:
                getUSerData();
                break;
        }
    }

    private void getUSerData() {
        String name = edt_name.getText().toString().trim();
        String email = edt_email.getText().toString().trim();
        String password = edt_password.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "please enter your name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "please enter your password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (utility.isNetworkAvailable()) {
            progressDialog.show();
            registerUser(name, email, password);
        } else {
            Toast.makeText(this, "Please check your network connection", Toast.LENGTH_SHORT).show();
        }


    }

    private void registerUser(final String name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            if (currentUser != null) {
                                String userID = currentUser.getUid();
                                String user_token = FirebaseInstanceId.getInstance().getToken();

                                Map<String, String> map = new HashMap<>();
                                map.put("name", name);
                                map.put("status", "Your status goes here");
                                map.put("image", "default");
                                map.put("thumb_image", "default");
                                map.put("user_token",user_token);

                                mDatabase.child(userID).setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressDialog.dismiss();
                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                    }
                                });
                            }

                        } else {
                            progressDialog.hide();
                            Toast.makeText(RegisterActivity.this, "failed",
                                    Toast.LENGTH_SHORT).show();
                        }


                    }
                });
    }
}
