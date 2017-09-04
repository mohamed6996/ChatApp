package com.lets.chat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.lets.chat.register.StartActivity;
import com.lets.chat.utility.GoogleConfig;

import io.fabric.sdk.android.Fabric;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    Toolbar toolbar;
    GoogleApiClient mGoogleApiClient;

    ViewPager viewPager;
    TabLayout tabLayout;
    PagerAdapter pagerAdapter;
    List<Fragment> fragmentList;
    List<String> titleList;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        fragmentList = new ArrayList<>();
        titleList = new ArrayList<>();
        initData();
        pagerAdapter = new PagerAdapter(getSupportFragmentManager(), fragmentList, titleList);
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setLayoutDirection(ViewPager.LAYOUT_DIRECTION_LTR); // fix arabic isuue

        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.logout:
                        logOut();
                        break;
                    case R.id.accountSettings:
                        userProfile();
                        break;
                    case R.id.allUsers:
                        sendToAllUsers();
                        break;

                }
                return true;
            }
        });

        mGoogleApiClient = new GoogleConfig(this).initConfig();

    }

    private void sendToAllUsers() {
        startActivity(new Intent(this, AllUsers.class));
    }

    private void userProfile() {
        Intent intent = new Intent(MainActivity.this, AccountSettings.class);
        startActivity(intent);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            sendToStart();
        }

        //  mGoogleApiClient = new GoogleConfig(this).initConfig();  cause error when return from profile activity
    }


    private void logOut() {
        // firebase sign out
        FirebaseAuth.getInstance().signOut();
        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        //  updateUI(null);
                    }
                });
        sendToStart();
    }


    private void sendToStart() {
        Intent intent = new Intent(this, StartActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


    private void initData() {
        addData(new RequestFragment(), "Requests");
        addData(new ChatFragment(), "Chat");
        addData(new FriendsFragment(), "Friends");
    }

    private void addData(Fragment fragment, String title) {
        fragmentList.add(fragment);
        titleList.add(title);
    }
}
