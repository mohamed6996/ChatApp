package com.lets.chat.register;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.lets.chat.MainActivity;
import com.lets.chat.R;
import com.lets.chat.utility.GoogleConfig;
import com.lets.chat.utility.Utility;

public class StartActivity extends AppCompatActivity implements View.OnClickListener {
    Button login_btn;
    TextView sign_up;
    EditText edt_email, edt_password;
    ProgressDialog progressDialog;

    // google sign in
    private final static int RC_SIGN_IN = 1;
    GoogleApiClient mGoogleApiClient;
    SignInButton signInButton;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    Utility utility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        edt_email = (EditText) findViewById(R.id.start_login_email);
        edt_password = (EditText) findViewById(R.id.start_login_password);

        login_btn = (Button) findViewById(R.id.start_sign_in);
        sign_up = (TextView) findViewById(R.id.start_sign_up);
        // set onClick listener
        login_btn.setOnClickListener(this);
        sign_up.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Signing in");
        progressDialog.setMessage("Please wait while we`re checking your credentials");
        progressDialog.setCanceledOnTouchOutside(false);

        signInButton = (SignInButton) findViewById(R.id.google_sign_in);
        signInButton.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");

        mGoogleApiClient = new GoogleConfig(this).initConfig();

        utility = new Utility(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_sign_in:
                initSignIn();
                break;
            case R.id.google_sign_in:
                signInWithGoogle();
                break;
            case R.id.start_sign_up:
                initSignUp();
                break;
        }
    }

    private void signInWithGoogle() {
        if (utility.isNetworkAvailable()) {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
        } else {
            Toast.makeText(this, "Please check your network connection", Toast.LENGTH_SHORT).show();
        }

    }

    private void initSignUp() {
        // Toast.makeText(this, "up", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(StartActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    private void initSignIn() {
        String email = edt_email.getText().toString().trim();
        String password = edt_password.getText().toString().trim();

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

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {


                            if (task.isSuccessful()) {


                                String user_uid = mAuth.getCurrentUser().getUid();
                                String user_token = FirebaseInstanceId.getInstance().getToken();

                                mDatabase.child(user_uid).child("user_token").setValue(user_token).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        progressDialog.dismiss();
                                        senToMainActivity();
                                    }
                                });


                            } else {
                                progressDialog.hide();
                                Toast.makeText(StartActivity.this, "failed",
                                        Toast.LENGTH_SHORT).show();
                            }


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //  progressDialog.dismiss();
                    String error_msg = e.getMessage();
                    //    Toast.makeText(StartActivity.this, error_msg, Toast.LENGTH_SHORT).show();

                    if (error_msg.contains("badly formatted")) {
                        Toast.makeText(StartActivity.this, "please enter a valid email", Toast.LENGTH_SHORT).show();
                    } else if (error_msg.contains("no user record")) {
                        Toast.makeText(StartActivity.this, "create account", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(StartActivity.this, "something went wrong", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, "Please check your network connection", Toast.LENGTH_SHORT).show();
        }


    }

    private void senToMainActivity() {
        Intent intent = new Intent(StartActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);

            } else {
                // Google Sign In failed, update UI appropriately

            }
        }
    }

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        progressDialog.show();
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            senToMainActivity();
                            // Sign in success, update UI with the signed-in user's information
                            // FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            progressDialog.hide();
                            Toast.makeText(StartActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }


}
